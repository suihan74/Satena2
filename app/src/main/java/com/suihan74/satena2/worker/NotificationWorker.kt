package com.suihan74.satena2.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.suihan74.hatena.CertifiedHatenaClient
import com.suihan74.hatena.model.account.Notice
import com.suihan74.satena2.Application
import com.suihan74.satena2.NoticesRepository
import com.suihan74.satena2.R
import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.NoticeVerb
import com.suihan74.satena2.scene.bookmarks.BookmarksActivity
import com.suihan74.satena2.scene.bookmarks.BookmarksActivityContract
import com.suihan74.satena2.scene.entries.EntriesActivity
import com.suihan74.satena2.scene.entries.EntriesActivityContract
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.utility.extension.putObjectExtra
import com.suihan74.satena2.utility.hatena.firstBookmarkMetadata
import com.suihan74.satena2.utility.hatena.message
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * バックグラウンドで通知を確認する[CoroutineWorker]
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val hatenaRepo: HatenaAccountRepository,
    private val noticeRepo: NoticesRepository,
    appDatabase: AppDatabase
) : CoroutineWorker(appContext, params) {

    private val noticeDao = appDatabase.noticeDao()

    private val isEnabledFlow = noticeRepo.notificationEnabled

    // ------ //

    override suspend fun doWork(): Result {
        runCatching {
            if (isEnabledFlow.value) {
                hatenaRepo.withSignedClient { client ->
                    checkNotices(client)
                }
            }
        }.onFailure {
            Log.e("NotificationWorker",it.stackTraceToString())
        }
        return Result.success()
    }

    /**
     * 通知を取得し、新しい通知があったらシステムに通知する
     */
    private suspend fun checkNotices(client: CertifiedHatenaClient) {
        runCatching {
            val noticeSameComment = noticeRepo.noticeSameComment.value
            val response = client.user.getNotices()
            for (n in response.notices) {
                val active =
                    noticeDao.find(n)?.let { existed ->
                        noticeSameComment && existed.modified < n.modified
                    } ?: true
                if (active) {
                    sendNotification(
                        context = applicationContext,
                        notice = n
                    )
                }
                noticeDao.insert(n)
            }
            if (noticeRepo.updateReadFlagOnNotification.value) {
                client.user.readNotices()
            }
        }.onFailure {
            throw it
        }
    }

    /**
     * Androidシステムの通知を発生させる
     */
    private fun sendNotification(context: Context, notice: Notice) {
        val notificationManager = NotificationManagerCompat.from(context)
        val title = context.getString(R.string.notice_title)
        val message = notice.message()
        var actions : List<NotificationCompat.Action>? = null

        val intent = when (notice.verb) {
            NoticeVerb.STAR.str -> {
                runCatching {
                    val openEntryIntent = Intent(context, BookmarksActivity::class.java).apply {
                        putExtra(BookmarksActivityContract.EXTRA_ENTRY_ID, notice.eid)
                    }

                    val openBookmarkIntent = Intent(context, BookmarksActivity::class.java).apply {
                        putObjectExtra(BookmarksActivityContract.EXTRA_NOTICE, notice)
                    }

                    val openNoticesIntent = Intent(context, EntriesActivity::class.java).apply {
                        putExtra(EntriesActivityContract.EXTRA_LAUNCH_NOTICES, true)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    actions = listOf(
                        NotificationCompat.Action(
                            0,
                            context.getString(R.string.notice_action_open_entry),
                            PendingIntent.getActivity(
                                context,
                                1,
                                openEntryIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        ),
                        NotificationCompat.Action(
                            0,
                            context.getString(R.string.notice_action_open_bookmark),
                            PendingIntent.getActivity(
                                context,
                                2,
                                openBookmarkIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        ),
                        NotificationCompat.Action(
                            0,
                            context.getString(R.string.notice_action_open_notices),
                            PendingIntent.getActivity(
                                context,
                                3,
                                openNoticesIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )

                    // デフォルトで開くのはブコメ詳細
                    openBookmarkIntent
                }.getOrElse {
                    actions = null
                    Intent(Intent.ACTION_VIEW, Uri.parse(notice.link))
                }
            }

            NoticeVerb.ADD_FAVORITE.str -> {
                Intent(Intent.ACTION_VIEW, Uri.parse(notice.link))
            }

            NoticeVerb.FIRST_BOOKMARK.str -> {
                runCatching {
                    Intent(context, BookmarksActivity::class.java).apply {
                        putExtra(
                            BookmarksActivityContract.EXTRA_URL,
                            notice.metadata!!.firstBookmarkMetadata!!.entryCanonicalUrl
                        )
                    }
                }.getOrElse {
                    /* todo
                    FirebaseCrashlytics.getInstance().recordException(
                        RuntimeException("NotificationWorker for VERB_FIRST_BOOKMARK")
                    )
                     */
                    Intent(context, EntriesActivity::class.java).apply {
                        putExtra(EntriesActivityContract.EXTRA_LAUNCH_NOTICES, true)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            }

            else -> {
                Intent(context, EntriesActivity::class.java).apply {
                    putExtra(EntriesActivityContract.EXTRA_LAUNCH_NOTICES, true)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        }

        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val style = NotificationCompat.BigTextStyle()
            .bigText(message)

        val builder = NotificationCompat.Builder(context, Application.NOTIFICATION_CHANNEL_ID)
            .setGroup(Application.NOTIFICATION_CHANNEL_ID)
            .setStyle(style)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        actions?.forEach {
            builder.addAction(it)
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(0, builder.build())
        }
    }
}

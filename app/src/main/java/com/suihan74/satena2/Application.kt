package com.suihan74.satena2

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.suihan74.satena2.hilt.ApplicationModule.appDatabase
import com.suihan74.satena2.model.dataStore.environmentDataStore
import com.suihan74.satena2.model.theme.default.TemporaryTheme
import com.suihan74.satena2.serializer.HatenaAccessTokenSerializer
import com.suihan74.satena2.serializer.MastodonAccessTokenSerializer
import com.suihan74.satena2.ui.theme.CurrentThemePreset
import com.suihan74.satena2.worker.NotificationWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@Suppress("unused")
@HiltAndroidApp
class Application : Application(), Configuration.Provider {

    companion object {
        /** 通知チャンネルID */
        const val NOTIFICATION_CHANNEL_ID = "com.suihan74.satena2.Application.NOTIFICATION_CHANNEL_ID"

        /** 通知ワーカータグ */
        const val NOTIFICATION_WORKER_TAG = "worker_checking_notifications"
    }

    // ------ //

    /** アプリのバージョン番号 */
    val versionCode : Long by lazy {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    /** アプリのバージョン名 */
    val versionName : String by lazy {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName
    }

    /** アプリのメジャーバージョン */
    val majorVersionCode : Long by lazy { getMajorVersion(versionCode) }

    /** アプリのマイナーバージョン */
    val minorVersionCode : Long by lazy { getMinorVersion(versionCode) }

    /** アプリの修正バージョン */
    val fixVersionCode : Long by lazy { getFixVersion(versionCode) }

    /** アプリの開発バージョン */
    val developVersionCode : Long by lazy { getDevelopVersion(versionCode) }

    /** バージョンコード値からメジャーバージョンを計算する */
    private fun getMajorVersion(versionCode: Long) : Long = versionCode / 100000000

    /** バージョンコード値からマイナーバージョンを計算する */
    private fun getMinorVersion(versionCode: Long) : Long {
        val upperMask = 100000000
        val lowerMask = 1000000
        return (versionCode % upperMask) / lowerMask
    }

    /** バージョンコード値から修正バージョンを計算する */
    private fun getFixVersion(versionCode: Long) : Long {
        val upperMask = 1000000
        val lowerMask = 1000
        return (versionCode % upperMask) / lowerMask
    }

    /** バージョンコード値から修正バージョンを計算する */
    private fun getDevelopVersion(versionCode: Long) : Long = versionCode / 1000

    // ------ //

    @Inject
    lateinit var noticesRepository: NoticesRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    // ------ //

    val coroutineScope = CoroutineScope(Dispatchers.Main)

    // ------ //

    override fun onCreate() {
        super.onCreate()
        initializeSerializers()
        initializeNotification()

        CurrentThemePreset.value = TemporaryTheme.colors
        appDatabase.themeDao().currentThemeFlow()
            .onEach {
                CurrentThemePreset.value = it.colors
            }
            .launchIn(coroutineScope)
    }

    // ------ //

    /**
     * Hatenaの通知確認用のWorkerを開始
     */
    private fun startWorkers(intervals: Long) {
        // ネットワークに接続されているときだけ実行する
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // intervals毎に定期実行する
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(intervals, TimeUnit.MINUTES)
            .addTag(NOTIFICATION_WORKER_TAG)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).run {
            cancelAllWork()
            enqueue(workRequest)
        }

        Log.i("WorkManager", "start notification checking in the background")
    }

    // ------ //

    /**
     * 秘密情報のシリアライザ初期化
     */
    private fun initializeSerializers() {
        val uuid = runBlocking {
            val ds = environmentDataStore.data.first()
            ds.uuid ?: UUID.randomUUID().toString().also { uuid ->
                environmentDataStore.updateData {
                    it.copy(uuid = uuid)
                }
            }
        }
        val path = filesDir.absolutePath
        HatenaAccessTokenSerializer.initialize(uuid, path)
        MastodonAccessTokenSerializer.initialize(uuid, path)
    }

    /**
     * 通知関連の初期化
     */
    private fun initializeNotification() {
        createNotificationChannel()
        noticesRepository.intervals
            .onEach {
                startWorkers(it.toLong())
            }
            .launchIn(coroutineScope)
    }

    /**
     * Androidシステム用の通知チャンネルを作成
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val name = getString(R.string.notification_channel_name)
        val descriptionText = getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        getSystemService<NotificationManager>()?.run {
//            createNotificationChannelGroup(id, name))
            createNotificationChannel(channel)
        }
    }
}

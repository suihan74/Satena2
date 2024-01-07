package com.suihan74.satena2.scene.browser

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.browser.FaviconInfo
import com.suihan74.satena2.model.browser.History
import com.suihan74.satena2.model.browser.HistoryLog
import com.suihan74.satena2.model.browser.HistoryPage
import com.suihan74.satena2.utility.DigestUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneId

interface HistoryRepository {
    val histories : StateFlow<List<History>>

    /**
     * 履歴リストを更新
     */
    suspend fun loadHistories()

    /**
     * 履歴リストを更新
     */
    suspend fun additionalLoadHistories()

    /**
     * 履歴を追加
     */
    suspend fun insertOrUpdateHistory(context: Context, url: String, title: String)

    /**
     * faviconを保存
     */
    suspend fun saveFavicon(context: Context, url: String, bitmap: Bitmap)
}

// ------ //

class HistoryRepositoryImpl(
    private val appDatabase: AppDatabase
) : HistoryRepository {
    private val dao = appDatabase.browserHistoryDao()

    override val histories = MutableStateFlow<List<History>>(emptyList())

    // ------ //

    private val historiesMutex = Mutex()

    private val faviconMutex = Mutex()

    // ------ //

    private suspend fun insertHistory(context: Context, url: String, title: String) = withContext(Dispatchers.Default) {
        val now = Instant.now()
        val today = now.atZone(ZoneId.systemDefault()).toLocalDate()
        val decodedUrl = Uri.decode(url)

        val faviconInfoId =
            Uri.parse(url).estimatedHierarchy?.let { dao.findFaviconInfo(it)?.id } ?: 0L

        val page =
            dao.getHistoryPage(decodedUrl)?.let {
                it.copy(visitTimes = it.visitTimes + 1, faviconInfoId = faviconInfoId)
            } ?: HistoryPage(
                url = decodedUrl,
                title = title,
                lastVisited = now,
                faviconInfoId = faviconInfoId
            )

        val visited = HistoryLog(visitedAt = now)

        dao.insertHistory(page = page, log = visited)
        dao.getHistory(now)?.let { existed ->
            clearOldHistories(context)
            historiesMutex.withLock {
                val items = histories.value
                    .filterNot {
                        val visitedDate = it.log.visitedAt.atZone(ZoneId.systemDefault()).toLocalDate()
                        visitedDate == today && it.page.page.url == existed.page.page.url
                    }.plus(existed)
                histories.emit(items)
            }
        }
    }

    override suspend fun insertOrUpdateHistory(
        context: Context,
        url: String,
        title: String
    ) { withContext(Dispatchers.Default) {
        if (!url.startsWith("http")) return@withContext

        val decodedUrl = Uri.decode(url)
        val existed = dao.getRecentHistories(limit = 1).firstOrNull()
        val existedPage = existed?.page
        if (existedPage == null || existedPage.page.url != decodedUrl) {
            insertHistory(context, url, title)
        }
        else {
            historiesMutex.withLock {
                val domain = Uri.parse(url).estimatedHierarchy!!
                val faviconInfo = existedPage.faviconInfo ?: dao.findFaviconInfo(domain)
                val updated = existedPage.page.copy(
                    title = title,
                    faviconInfoId = faviconInfo?.id ?: 0
                )
                dao.updateHistoryPage(updated)
                histories.emit(
                    histories.value.map {
                        if (it.page.page.id == existedPage.page.id) it.copy(page = existedPage.copy(page = updated))
                        else it
                    }
                )
            }
        }
    } }

    /** 履歴を削除する */
    suspend fun deleteHistory(history: History) = withContext(Dispatchers.Default) {
        historiesMutex.withLock {
            dao.deleteHistoryLog(history.log)
            histories.emit(
                histories.value.filterNot { it.log.id == history.log.id }
            )
        }
    }

    /**
     * 履歴リストを更新
     */
    override suspend fun loadHistories() { withContext(Dispatchers.Default) {
        historiesMutex.withLock {
            runCatching {
                histories.emit(
                    dao.getRecentHistories()
                )
            }.onFailure {
//                FirebaseCrashlytics.getInstance().recordException(RuntimeException("v190 history issue has fixed"))
            }
        }
    } }

    /**
     * 履歴リストを更新
     */
    override suspend fun additionalLoadHistories() { withContext(Dispatchers.Default) {
        historiesMutex.withLock {
            runCatching {
                val existedItems = histories.value
                val items = dao.getRecentHistories(offset = existedItems.size)
                histories.emit(
                    existedItems.plus(items)
                )
            }.onFailure {
//                FirebaseCrashlytics.getInstance().recordException(RuntimeException("v190 history issue has fixed"))
            }
        }
    } }

    /** 寿命切れの履歴を削除する */
    private suspend fun clearOldHistories(context: Context) {
        /*
        val now = Instant.now()
        val today = now.atZone(ZoneId.systemDefault()).toLocalDate()
        val lifeSpanDays = prefs.getInt(BrowserSettingsKey.HISTORY_LIFESPAN)
        val lastRefreshed = prefs.getObject<ZonedDateTime>(BrowserSettingsKey.HISTORY_LAST_REFRESHED)
        if (lifeSpanDays == 0 || lastRefreshed != null && lastRefreshed.toLocalDate() >= today) {
            return
        }
        val threshold = now.toLocalDateTime().minusDays(lifeSpanDays.toLong())

        runCatching {
            dao.deleteHistory(Instant.MIN, threshold)
            dao.deleteHistoryPages(Instant.MIN, threshold)
        }

        historiesMutex.withLock {
            histories.emit(
                histories.value.filter { it.log.visitedAt >= threshold }
            )
        }

        // 参照されなくなったfaviconキャッシュを削除する
        clearOldFavicons(context)

        prefs.editSync {
            putObject(BrowserSettingsKey.HISTORY_LAST_REFRESHED, now)
        }
        */
    }

    // ------ //

    suspend fun findFaviconInfo(url: String): FaviconInfo? = faviconMutex.withLock {
        runCatching {
            val site = Uri.parse(url).estimatedHierarchy ?: return null
            dao.findFaviconInfo(site)
        }.getOrNull()
    }

    override suspend fun saveFavicon(
        context: Context,
        url: String,
        bitmap: Bitmap
    ) { withContext(Dispatchers.IO) {
        faviconMutex.withLock {
            runCatching {
                val site = Uri.parse(url).estimatedHierarchy!!
                val existed = dao.findFaviconInfo(site)
                val filename =
                    ByteBuffer.allocate(bitmap.byteCount).let { buffer ->
                        bitmap.copyPixelsToBuffer(buffer)
                        DigestUtility.sha256(buffer.array())
                    }

                if (existed?.filename == filename) {
                    return@runCatching
                }

                File(context.filesDir, FAVICON_CACHE_DIR).let { dir ->
                    dir.mkdirs()
                    val outFile = File(dir, filename)
                    runCatching {
                        if (!outFile.exists()) {
                            val byteArray =
                                ByteArrayOutputStream().use { ostream ->
                                    val compressFormat =
                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) Bitmap.CompressFormat.PNG
                                        else Bitmap.CompressFormat.WEBP_LOSSY
                                    bitmap.compress(compressFormat, 100, ostream)
                                    ostream.toByteArray()
                                }
                            outFile.outputStream().use { it.write(byteArray) }
                        }
                        insertFaviconInfo(filename, url, site, existed)
                    }.onFailure {
                        runCatching { outFile.delete() }
                    }
                }
            }.onFailure {
                Log.e("favicon", "failed to save a favicon cache for URL($url)")
            }
        }
    } }

    private suspend fun insertFaviconInfo(
        filename: String,
        url: String,
        site: String,
        existed: FaviconInfo?
    ) {
        if (existed != null) {
            dao.updateFaviconInfo(
                existed.copy(
                    site = site,
                    filename = filename,
                    lastUpdated = Instant.now()
                )
            )
        }
        else {
            dao.insertFaviconInfo(
                FaviconInfo(
                    site = site,
                    filename = filename,
                    lastUpdated = Instant.now()
                )
            )
        }
        dao.getHistoryPage(Uri.decode(url))?.let { historyPage ->
            val faviconInfo = dao.findFaviconInfo(site)!!
            dao.updateHistoryPage(
                historyPage.copy(faviconInfoId = faviconInfo.id)
            )
            updateHistoryFavicon(faviconInfo)
        }
    }

    private suspend fun updateHistoryFavicon(faviconInfo: FaviconInfo) = withContext(Dispatchers.Default) {
        historiesMutex.withLock {
            histories.emit(
                histories.value
                    .map { h ->
                        val faviconInfoId = h.page.page.faviconInfoId
                        val site = Uri.parse(h.page.page.url).estimatedHierarchy
                        if (faviconInfoId == faviconInfo.id || (faviconInfoId == 0L && site == faviconInfo.site)) {
                            val updatedPage = h.page.page.copy(faviconInfoId = faviconInfo.id)
                            if (faviconInfoId == 0L) {
                                dao.updateHistoryPage(updatedPage)
                            }
                            h.copy(
                                page = h.page.copy(
                                    page = updatedPage,
                                    faviconInfo = faviconInfo
                                )
                            )
                        }
                        else h
                    }
            )
        }
    }

    private suspend fun clearOldFavicons(context: Context) = withContext(Dispatchers.IO) {
        faviconMutex.withLock {
            runCatching {
                val oldItems = dao.findOldFaviconInfo()
                dao.deleteFaviconInfo(oldItems)
                File(context.filesDir, FAVICON_CACHE_DIR).let { dir ->
                    for (item in oldItems) {
                        File(dir, item.filename).delete()
                    }
                }
            }.onFailure {
                // todo
                // FirebaseCrashlytics.getInstance().recordException(it)
            }
        }
        clearUnmanagedFavicons(context)
    }

    private suspend fun clearUnmanagedFavicons(context: Context) = withContext(Dispatchers.IO) {
        faviconMutex.withLock {
            runCatching {
                buildList {
                    File(context.filesDir, FAVICON_CACHE_DIR).listFiles { dir, filename ->
                        add(filename)
                    }
                }.filterNot { filename ->
                    dao.existFaviconInfo(filename)
                }.forEach { filename ->
                    File("${context.filesDir.absoluteFile}/$FAVICON_CACHE_DIR/$filename").delete()
                }
            }.onFailure {
                // todo
                // FirebaseCrashlytics.getInstance().recordException(it)
            }
        }
    }

    // ------ //

    companion object {
        const val FAVICON_CACHE_DIR = "favicon_cache"
    }
}

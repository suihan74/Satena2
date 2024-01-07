package com.suihan74.satena2.model.browser

import androidx.room.*
import java.time.*

@Dao
interface BrowserHistoryDao {
    /**
     * 直近の閲覧履歴を指定件数取得する
     */
    @Transaction
    @Query("""
        SELECT * FROM browser_history_items
        ORDER BY visitedAt DESC
        LIMIT :offset, :limit
    """)
    suspend fun getRecentHistories(offset: Int = 0, limit: Int = 10) : List<History>

    /**
     * 指定日時の閲覧履歴を取得する
     *
     * 完全に一致する時刻を指定する必要があるため、
     * 主に履歴追加直後にIDが付加されたインスタンスを再取得するために使用する
     */
    @Transaction
    @Query("""
        SELECT * FROM browser_history_items
        WHERE visitedAt = :visited
        LIMIT 1
    """)
    suspend fun getHistory(visited: Instant): History?

    /**
     * 指定URLのページ情報を取得する
     */
    @Query("""
        SELECT * FROM browser_history_pages
        WHERE url = :url
        LIMIT 1
    """)
    suspend fun getHistoryPage(url: String): HistoryPage?

    @Query("""
        SELECT * FROM browser_history_pages
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getHistoryPage(id: Long): HistoryPage?

    /**
     * 最後に訪れたのが指定期間のページ情報を取得する
     */
    @Query("""
        SELECT * FROM browser_history_pages
        WHERE lastVisited>=:start AND lastVisited<:end
    """)
    suspend fun getHistoryPages(start: Instant, end: Instant): List<HistoryPage>

    // ------ //

    /**
     * ページ情報を追加する
     *
     * 外部から直接使用しない
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun __insertHistoryPage(page: HistoryPage): Long

    /**
     * ログを追加する
     *
     * 外部から直接使用しない
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun __insertHistoryLog(item: HistoryLog): Long

    @Transaction
    suspend fun insertHistory(page: HistoryPage, log: HistoryLog) {
        val pageId = __insertHistoryPage(page)

        // 同日内の同一URL履歴を削除する
        val zoneId = ZoneId.systemDefault()
        val date = log.visitedAt.atZone(zoneId).toLocalDate()
        val startOfDay = LocalTime.ofSecondOfDay(0L)
        val start = ZonedDateTime.of(LocalDateTime.of(date, startOfDay), zoneId).toInstant()
        val end = ZonedDateTime.of(LocalDateTime.of(date.plusDays(1L), startOfDay), zoneId).toInstant()
        deleteHistoryLogs(pageId, start, end)

        val item = log.copy(pageId = pageId)
        __insertHistoryLog(item)
    }

    // ------ //

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHistoryPage(page: HistoryPage)

    // ------ //

    /**
     * 指定期間内で指定ページを参照している閲覧履歴を削除する
     */
    @Query("""
        DELETE FROM browser_history_items
        WHERE visitedAt >= :start AND visitedAt < :end AND pageId = :pageId
    """)
    suspend fun deleteHistoryLogs(pageId: Long, start: Instant, end: Instant)

    /**
     * ページ情報を削除する
     *
     * 加えて、該当のページ情報を参照するすべてのログを削除する
     */
    @Transaction
    suspend fun deleteHistoryPage(page: HistoryPage) {
        deleteHistoryLogsWithPage(page.id)
        __deleteHistoryPageImpl(page)
    }

    /**
     * ページ情報を削除する
     *
     * 外部からは直接使用しない
     */
    @Delete
    suspend fun __deleteHistoryPageImpl(page: HistoryPage)

    @Transaction
    suspend fun deleteHistoryPages(start: Instant, end: Instant) {
        val pages = getHistoryPages(start, end)
        for (p in pages) {
            deleteHistoryLogsWithPage(p.id)
            deleteHistoryPage(p)
        }
    }

    @Query("""
        DELETE FROM browser_history_items
        WHERE pageId = :pageId
    """)
    suspend fun deleteHistoryLogsWithPage(pageId: Long)

    @Delete
    suspend fun deleteHistoryLog(log: HistoryLog)

    /**
     * 指定した期間内のすべてのログを削除する
     */
    @Query("""
        DELETE FROM browser_history_items
        WHERE visitedAt >= :start AND visitedAt < :end
    """)
    suspend fun deleteHistory(start: Instant, end: Instant)

    // ------ //

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaviconInfo(faviconInfo: FaviconInfo): Long

    @Update
    suspend fun updateFaviconInfo(faviconInfo: FaviconInfo)

    @Query("""
        SELECT * FROM browser_favicon_info
        WHERE site = :site
    """)
    suspend fun findFaviconInfo(site: String): FaviconInfo?

    @Query("""
        SELECT EXISTS (SELECT * FROM browser_favicon_info WHERE filename = :filename)
    """)
    suspend fun existFaviconInfo(filename: String): Boolean

    @Query("""
        SELECT * FROM browser_favicon_info f
        WHERE NOT EXISTS (SELECT * FROM browser_history_pages WHERE faviconInfoId = f.id)
            AND NOT EXISTS (SELECT * FROM favorite_site WHERE faviconInfoId = f.id)
    """)
    suspend fun findOldFaviconInfo(): List<FaviconInfo>

    @Delete
    suspend fun deleteFaviconInfo(items: List<FaviconInfo>)
}

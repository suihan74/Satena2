package com.suihan74.satena2.model.browser

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant

/**
 * ページ情報
 *
 * 何回訪れたか、最後にいつ訪れたかなどの情報
 */
@Entity(
    tableName = "browser_history_pages"
)
data class HistoryPage(
    val url: String,

    val title: String,

    val lastVisited: Instant,

    val visitTimes: Long = 1L,

    val faviconInfoId: Long = 0L,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)

// ------ //

/**
 * favicon情報と関連付けた状態のページ情報
 */
data class HistoryPageWithFaviconInfo(
    @Embedded
    val page: HistoryPage,

    @Relation(
        parentColumn = "faviconInfoId",
        entityColumn = "id"
    )
    val faviconInfo: FaviconInfo?
)

// ------ //

/**
 * 閲覧履歴
 *
 * 訪れた日時とどのページを訪れたかという情報
 */
@Entity(
    tableName = "browser_history_items"
)
data class HistoryLog(
    val visitedAt: Instant,

    val pageId: Long = 0L,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)

// ------ //

/**
 * アプリ側で閲覧履歴リストに表示するデータ
 */
data class History(
    @Embedded
    val log: HistoryLog,

    @Relation(
        entity = HistoryPage::class,
        parentColumn = "pageId",
        entityColumn = "id"
    )
    val page: HistoryPageWithFaviconInfo
)

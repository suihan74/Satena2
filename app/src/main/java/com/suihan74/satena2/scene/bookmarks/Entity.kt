package com.suihan74.satena2.scene.bookmarks

import android.net.Uri
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.bookmark.BookmarksDigest
import com.suihan74.hatena.model.bookmark.BookmarksEntry
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.entry.EntryItem
import com.suihan74.hatena.model.entry.RelatedEntriesResponse
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.FilterState
import java.time.Instant

data class Entity(
    /**
     * エントリを取得した実際のURL
     */
    val requestUrl : String,
    /**
     * エントリ
     */
    val entry : Entry,
    /**
     * 全ブクマ情報を含むエントリ
     */
    val bookmarksEntry : BookmarksEntry,
    /**
     * 人気ブクマ・フォローブクマ
     */
    val bookmarksDigest : BookmarksDigest,
    /**
     * 新着ブクマリスト
     */
    val bookmarks : List<Bookmark>,
    /**
     * 新着ブクマリストの読み込みカーソル
     */
    val recentCursor : String?,
    /**
     * エントリに対するスター
     */
    val entryStars : StarsEntry?,
    /**
     * 各ブクマにつけられた全スター（ユーザー名とスターのペア）
     */
    val starsMap : Map<String, StarsEntry>,

    /**
     * 関連エントリ
     */
    val relatedEntriesResponse: RelatedEntriesResponse
) {
    val relatedEntries by lazy {
        relatedEntriesResponse.entries.map {
            DisplayEntry(
                entry = it,
                read = null,
                filterState = FilterState.UNHANDLED
            )
        }
    }

    /**
     * 上階のURL
     */
    val upperStairUrl by lazy { "https://b.hatena.ne.jp/entry?url=${Uri.encode(entry.url)}" }

    /**
     * 下階のURL
     */
    val lowerStairUrl : String? by lazy {
        val regex = Regex("""^https://b\.hatena\.ne\.jp/entry/(s/)?(.+)$""")
        val matchResult = regex.matchEntire(entry.url)
        matchResult?.let {
            if (it.groupValues.size == 3) {
                "https://${Uri.decode(it.groupValues[2])}"
            }
            else {
                "http://${Uri.decode(it.groupValues[1])}"
            }
        }
    }

    // ------ //

    companion object {
        val EMPTY = Entity(
            requestUrl = "",
            entry = EntryItem(
                eid = 0L,
                url = "",
                title = "",
                description = "",
                count = 0,
                createdAt = Instant.EPOCH  // [Instant.MIN]を使用するとJson化処理中にoverflowで死ぬので、空の場合は"1970-01-01T00:00:00Z"に設定
            ),
            bookmarksEntry = BookmarksEntry(
                id = 0L,
                title = "",
                count = 0,
                url = "",
                entryUrl = "",
                requestedUrl = "",
                screenshot = "",
                bookmarks = emptyList()
            ),
            bookmarksDigest = BookmarksDigest(
                referredBlogEntries = emptyList(),
                scoredBookmarks = emptyList(),
                favoriteBookmarks = emptyList()
            ),
            bookmarks = emptyList(),
            recentCursor = null,
            entryStars = null,
            starsMap = emptyMap(),
            relatedEntriesResponse = RelatedEntriesResponse(
                entries = emptyList(),
                metaEntry = null,
                referredBlogEntries = emptyList(),
                referredEntries = emptyList(),
                topics = emptyList(),
                prEntries = emptyList()
            )
        )
    }
}

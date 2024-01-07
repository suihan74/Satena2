package com.suihan74.satena2.model.ignoredEntry

import androidx.room.*
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.utility.hatena.actualUrl

/**
 * 非表示エントリ設定データ
 */
@Entity(
    tableName = "ignored_entry",
    indices = [Index(value = ["type", "query"], name = "ignoredEntry_type_query", unique = true)]
)
@TypeConverters(
    IgnoredEntryTypeConverter::class,
    IgnoreTargetConverter::class
)
data class IgnoredEntry (
    /**
     * 非表示設定を判別する対象（URL or TEXT）
     */
    var type : IgnoredEntryType = IgnoredEntryType.URL,

    /**
     * 含まれていたら非表示にする文字列
     */
    var query : String = "",

    /**
     * 非表示設定の適用範囲
     */
    var target : IgnoreTarget = IgnoreTarget.ALL,

    /**
     * 正規表現として使用する
     */
    val asRegex : Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    companion object {
        /** 登録前のダミーデータを作成する */
        fun createDummy(
            type: IgnoredEntryType = IgnoredEntryType.URL,
            query: String = "",
            target: IgnoreTarget = IgnoreTarget.ALL,
            asRegex: Boolean = false,
        ): IgnoredEntry =
            IgnoredEntry(type, query, target, asRegex, id = 0)
    }

    // ------ //

    @delegate:Ignore
    private val regex : Regex by lazy {
        when (type) {
            IgnoredEntryType.URL -> {
                if (asRegex) Regex(query)
                else Regex("""^https?://\Q${query}\E""")
            }

            IgnoredEntryType.TEXT -> {
                if (asRegex) Regex(query)
                else Regex("""\Q${query}\E""")
            }
        }
    }

    // ------ //

    fun match(entry: Entry) : Boolean =
        if (asRegex) matchWithRegex(entry)
        else matchWithPlain(entry)

    private fun matchWithRegex(entry: Entry) : Boolean = when (type) {
        IgnoredEntryType.URL -> {
            if (entry.isPr) {
                regex.containsMatchIn(entry.url) || regex.containsMatchIn(entry.actualUrl())
            }
            else {
                regex.containsMatchIn(entry.url)
            }
        }

        IgnoredEntryType.TEXT -> {
            target.contains(IgnoreTarget.ENTRY) && regex.containsMatchIn(entry.title)
        }
    }

    private fun matchWithPlain(entry: Entry) : Boolean = when (type) {
        IgnoredEntryType.URL -> {
            if (entry.isPr) {
                matchPlainUrl(entry.url) || matchPlainUrl(entry.actualUrl())
            }
            else {
                matchPlainUrl(entry.url)
            }
        }

        IgnoredEntryType.TEXT -> {
            target.contains(IgnoreTarget.ENTRY) && entry.title.contains(query)
        }
    }

    private fun matchPlainUrl(url: String) : Boolean {
        val targetUrl = when {
            url.startsWith("https://") -> "https://$query"
            else -> "http://$query"
        }
        return url.startsWith(targetUrl)
    }

    // ------ //

    fun match(bookmark: Bookmark) : Boolean =
        if (asRegex) matchWithRegex(bookmark)
        else matchWithPlain(bookmark)

    private fun matchWithRegex(bookmark: Bookmark) : Boolean = when(type) {
        IgnoredEntryType.URL -> {
            target.contains(IgnoreTarget.BOOKMARK)
                    && (bookmark.comment.contains("https://$query") || bookmark.comment.contains("http://$query"))
        }

        IgnoredEntryType.TEXT -> {
            target.contains(IgnoreTarget.BOOKMARK)
                    && (
                    regex.containsMatchIn(bookmark.comment)
                            || regex.containsMatchIn(bookmark.user)
                            || bookmark.tags.any { regex.containsMatchIn(it) }
                    )
        }
    }

    private fun matchWithPlain(bookmark: Bookmark) : Boolean = when(type) {
        IgnoredEntryType.URL -> {
            target.contains(IgnoreTarget.BOOKMARK)
                    && (bookmark.comment.contains("https://$query") || bookmark.comment.contains("http://$query"))
        }

        IgnoredEntryType.TEXT -> {
            target.contains(IgnoreTarget.BOOKMARK)
                    && (
                    bookmark.comment.contains(query)
                            || bookmark.user.contains(query)
                            || bookmark.tags.any { it.contains(query) }
                    )
        }
    }
}

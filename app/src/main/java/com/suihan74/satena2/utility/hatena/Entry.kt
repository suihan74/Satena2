package com.suihan74.satena2.utility.hatena

import android.net.Uri
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.entry.EntryItem
import com.suihan74.hatena.model.entry.IssueEntry
import com.suihan74.hatena.model.entry.MyHotEntry
import com.suihan74.hatena.model.entry.UserEntry
import com.suihan74.hatena.model.entry.UserEntryComment

/**
 * PR用のURLからエントリの実際のURLを取得する
 */
fun Entry.actualUrl() : String {
    if (!this.isPr) return this.url
    val uri = Uri.parse(this.url)
    return uri.getQueryParameter("url")?.let { Uri.decode(it) } ?: this.url
}

/**
 * エントリのブクマ情報を更新したコピーを作成する
 */
fun Entry.copy(bookmarkResult: BookmarkResult?) : Entry {
    return when (this) {
        is EntryItem -> copy(title = title, bookmarkedData = bookmarkResult)
        is IssueEntry -> copy(title = title, bookmarkedData = bookmarkResult)
        is MyHotEntry -> copy(title = title, bookmarkedData = bookmarkResult)
        is UserEntry -> {
            copy(
                eid = eid,
                comment = bookmarkResult?.let {
                    UserEntryComment(
                        raw = it.commentRaw,
                        tags = it.tags,
                        body = it.comment
                    )
                } ?: UserEntryComment(raw = "", tags = emptyList(), body = "")
            )
        }
        else -> throw NotImplementedError()
    }
}

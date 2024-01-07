package com.suihan74.satena2.utility.hatena

import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.bookmark.BookmarksEntry
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.star.Star
import com.suihan74.hatena.model.star.StarColor
import com.suihan74.hatena.model.star.StarCount

/**
 * ブクマ結果`BookmarkResult`からブクマ`Bookmark`に変換する
 */
fun BookmarkResult.toBookmark() = Bookmark(
    _user = Bookmark.User(
        name = this.user,
        profileImageUrl = this.userIconUrl
    ),
    comment = this.comment,
    isPrivate = this.private ?: false,
    link = this.permalink,
    tags = this.tags,
    timestamp = this.timestamp,
    starCount = buildList {
        starsCount.toStarCount(StarColor.YELLOW)?.let { add(it) }
        starsCount.toStarCount(StarColor.RED)?.let { add(it) }
        starsCount.toStarCount(StarColor.GREEN)?.let { add(it) }
        starsCount.toStarCount(StarColor.BLUE)?.let { add(it) }
        starsCount.toStarCount(StarColor.PURPLE)?.let { add(it) }
    }
)

fun BookmarksEntry.Bookmark.toBookmark(entry: BookmarksEntry) = toBookmark(entry.id)

fun BookmarksEntry.Bookmark.toBookmark(entry: Entry) = toBookmark(entry.eid)

fun BookmarksEntry.Bookmark.toBookmark(eid: Long) = Bookmark(
    _user = Bookmark.User(
        name = this.user,
        profileImageUrl = "https://cdn.profile-image.st-hatena.com/users/${this.user}/profile.png"
    ),
    comment = this.comment,
    isPrivate = false,
    link = "https://b.hatena.ne.jp/entry/${eid}/comment/${this.user}",
    tags = this.tags,
    timestamp = this.timestamp,
    starCount = emptyList()
)

// ------ //

/**
 * `Star`のリストから`StarCount`のリストを作成する
 */
private fun List<Star>?.toStarCount(color: StarColor) : StarCount? {
    return this?.filter { it.color == color }?.sumOf { it.count }?.let { count ->
        StarCount(StarColor.YELLOW, count)
    }
}

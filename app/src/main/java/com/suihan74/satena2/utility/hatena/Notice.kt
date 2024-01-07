package com.suihan74.satena2.utility.hatena

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.suihan74.hatena.model.account.Notice
import com.suihan74.satena2.model.NoticeVerb
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.rgbCode

private val spamRegex by lazy { Regex("""はてなブックマーク\s*-\s*\d+に関する.+のブックマーク""") }

/** スパムからのスターの特徴に当てはまるか確認する */
fun Notice.checkFromSpam() =
    spamRegex.matches(this.metadata?.subjectTitle.orEmpty())

/**
 * 通知に含まれるユーザー名を抽出する
 */
val Notice.users get() =
    objects
        .groupBy { it.user }
        .mapNotNull { it.value.firstOrNull() }
        .reversed()
        .map { it.user }

/**
 * 通知メッセージ(テキストオンリー)
 */
fun Notice.message() : String {
    val comment = metadata?.subjectTitle.orEmpty().toCharArray()
    val sourceComment = comment.joinToString(
        separator = "",
        limit = 9,
        truncated = "..."
    )

    val users = this.users
    val usersStr = this.users.joinToString(
        separator = "、",
        limit = 3,
        truncated = "ほか${users.count() - 3}人",
        transform = { "$it>さん" }
    )

    return when (verb) {
        NoticeVerb.STAR.str -> buildString {
            append(
                users.joinToString(
                    separator = "、",
                    limit = 3,
                    truncated = "ほか${users.count() - 3}人",
                    transform = { "${it}さん" }
                )
            )
            append(
                if (link.startsWith("https://b.hatena.ne.jp/")) {
                    "があなたのブコメ($sourceComment)に"
                }
                else {
                    "があなたの($sourceComment)に"
                }
            )
            append("★をつけました")
        }

        NoticeVerb.ADD_FAVORITE.str -> buildString {
            append(
                users.joinToString(
                    separator = "、",
                    limit = 3,
                    truncated = "ほか${users.count() - 3}人",
                    transform = { "${it}さん" }
                ),
                "があなたのブックマークをお気に入りに追加しました"
            )
        }

        NoticeVerb.BOOKMARK.str -> buildString {
            append(
                users.joinToString(
                    separator = "、",
                    limit = 3,
                    truncated = "ほか${users.count() - 3}人",
                    transform = { "${it}さん" }
                ),
                "があなたのエントリをブックマークしました"
            )
        }

        else -> "[sorry, not implemented notice] users: $usersStr , verb: $verb"
    }
}

/**
 * 通知メッセージ(Compose上で装飾して使用)
 */
@Composable
fun Notice.annotatedMessage() : AnnotatedString {
    val comment = metadata?.subjectTitle.orEmpty().toCharArray()
    val sourceComment = comment.joinToString(
        separator = "",
        limit = 9,
        truncated = "..."
    )

    val nameColor = CurrentTheme.primary.rgbCode()
    val users = this.users
    val usersStr = this.users.joinToString(
        separator = "、",
        limit = 3,
        truncated = "ほか${users.count() - 3}人",
        transform = { "<font color=\"$nameColor\">${it}</font>さん" }
    )

    return when (verb) {
        NoticeVerb.STAR.str -> {
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = CurrentTheme.primary)) {
                    append(
                        users.joinToString(
                            separator = "、",
                            limit = 3,
                            truncated = "ほか${users.count() - 3}人",
                            transform = { "${it}さん" }
                        )
                    )
                }
                append(
                    if (link.startsWith("https://b.hatena.ne.jp/")) {
                        "があなたのブコメ($sourceComment)に"
                    }
                    else {
                        "があなたの($sourceComment)に"
                    }
                )
                objects.distinctBy { it.color }
                    .reversed()
                    .forEach {
                        withStyle(style = SpanStyle(color = it.color.color)) {
                            append("★")
                        }
                    }
                append("をつけました")
            }
        }

        NoticeVerb.ADD_FAVORITE.str -> buildAnnotatedString {
            withStyle(style = SpanStyle(color = CurrentTheme.primary)) {
                append(
                    users.joinToString(
                        separator = "、",
                        limit = 3,
                        truncated = "ほか${users.count() - 3}人",
                        transform = { "${it}さん" }
                    )
                )
            }
            append("があなたのブックマークをお気に入りに追加しました")
        }

        NoticeVerb.BOOKMARK.str -> buildAnnotatedString {
            withStyle(style = SpanStyle(color = CurrentTheme.primary)) {
                append(
                    users.joinToString(
                        separator = "、",
                        limit = 3,
                        truncated = "ほか${users.count() - 3}人",
                        transform = { "${it}さん" }
                    )
                )
            }
            append("があなたのエントリをブックマークしました")
        }

        else -> buildAnnotatedString {
            append("[sorry, not implemented notice] users: $usersStr , verb: $verb")
        }
    }
}

// ------ //

// todo
val Notice.NoticeMetadata.firstBookmarkMetadata : FirstBookmarkMetadata? get() {
/*
    val count = totalBookmarksAchievement ?: return null
    val url = entryCanonicalUrl ?: return null
    val title = entryTitle ?: return null
    return FirstBookmarkMetadata(count, url, title)
*/
    return null
}

// ------ //

data class FirstBookmarkMetadata (
    val totalBookmarksAchievement : Int,
    val entryCanonicalUrl : String,
    val entryTitle : String
)

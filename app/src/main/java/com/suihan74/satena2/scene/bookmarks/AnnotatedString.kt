package com.suihan74.satena2.scene.bookmarks

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.star.StarColor
import com.suihan74.hatena.model.star.StarCount
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.utility.extension.appendRegex
import com.suihan74.satena2.utility.hatena.color

/**
 * スター表示用のテキスト装飾
 */
private fun AnnotatedString.Builder.appendStarCount(starCount: StarCount?) {
    if (starCount == null) return
    if (starCount.count == 0) return
    withStyle(SpanStyle(color = starCount.color.color)) {
        if (starCount.count > 10) {
            append("★${starCount.count}")
        }
        else {
            repeat(starCount.count) { append("★") }
        }
    }
}

fun AnnotatedString.Builder.appendStarCountText(starCount: List<StarCount>) {
    if (starCount.isEmpty()) return
    val purple = starCount.firstOrNull { it.color == StarColor.PURPLE }
    val blue = starCount.firstOrNull { it.color == StarColor.BLUE }
    val red = starCount.firstOrNull { it.color == StarColor.RED }
    val green = starCount.firstOrNull { it.color == StarColor.GREEN }
    val yellow = starCount.firstOrNull { it.color == StarColor.YELLOW }
    append("  ")
    appendStarCount(purple)
    appendStarCount(blue)
    appendStarCount(red)
    appendStarCount(green)
    appendStarCount(yellow)
}

fun AnnotatedString.Builder.appendStarCountText(starsEntry: StarsEntry) {
    val purple = starsEntry.starsCount(StarColor.PURPLE)
    val blue = starsEntry.starsCount(StarColor.BLUE)
    val red = starsEntry.starsCount(StarColor.RED)
    val green = starsEntry.starsCount(StarColor.GREEN)
    val yellow = starsEntry.starsCount(StarColor.YELLOW)
    if (yellow > 0 || purple > 0 || blue > 0 || red > 0 || green > 0) {
        append("  ")
        appendStarCount(StarCount(color = StarColor.PURPLE, count = purple))
        appendStarCount(StarCount(color = StarColor.BLUE, count = blue))
        appendStarCount(StarCount(color = StarColor.RED, count = red))
        appendStarCount(StarCount(color = StarColor.GREEN, count = green))
        appendStarCount(StarCount(color = StarColor.YELLOW, count = yellow))
    }
}

// ------ //

/**
 * ブコメのテキスト装飾
 */
@OptIn(ExperimentalTextApi::class)
fun buildAnnotatedComment(
    bookmark: Bookmark,
    linkColor: Color
) : AnnotatedString = buildAnnotatedString {
    if (bookmark.comment.isBlank()) return@buildAnnotatedString
    val urlRegex =
        Regex("""id:[a-zA-Z0-9_]+|https?://([\w-]+\.)+[\w-]+(/[a-zA-Z0-9_\-+./!?%&=|^~#@*;:,<>()\[\]{}]*)?""")
    // todo: `DisplayBookmark`生成時に切り分けるのでこちらでの正規表現処理は無くす

    appendRegex(urlRegex, bookmark.comment) { m ->
        if (m.value.startsWith("http")) {
            withAnnotation(urlAnnotation = UrlAnnotation(m.value)) {
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(m.value)
                }
            }
        }
        else {
            withStyle(
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.None
                )
            ) {
                append(m.value)
            }
        }
    }
}

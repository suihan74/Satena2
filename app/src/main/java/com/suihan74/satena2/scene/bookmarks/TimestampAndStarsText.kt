package com.suihan74.satena2.scene.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.suihan74.hatena.model.star.StarCount
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.zonedString
import java.time.Instant

/**
 * タイムスタンプ+スターリスト部分の装飾テキストを作成する
 */
@Composable
fun buildTimestampAndStarsText(
    timestamp: Instant,
    starCount: List<StarCount>
) : AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = CurrentTheme.grayTextColor)) {
        append(timestamp.zonedString("yyyy-MM-dd HH:mm"))
    }
    appendStarCountText(starCount = starCount)
}

/**
 * タイムスタンプ+スターリスト部分の装飾テキストを作成する
 */
@Composable
fun buildTimestampAndStarsText(
    timestamp: Instant,
    starsEntry: StarsEntry
) : AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = CurrentTheme.grayTextColor)) {
        append(timestamp.zonedString("yyyy-MM-dd HH:mm"))
    }
    appendStarCountText(starsEntry = starsEntry)
}

package com.suihan74.satena2.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.suihan74.hatena.model.star.ColorStars
import com.suihan74.hatena.model.star.Star
import com.suihan74.hatena.model.star.StarColor
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.utility.hatena.color

@Composable
fun StarsCountText(
    starsEntry: StarsEntry,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val counts = remember(starsEntry) {
        mapOf(
            StarColor.RED to starsEntry.starsCount(StarColor.RED),
            StarColor.GREEN to starsEntry.starsCount(StarColor.GREEN),
            StarColor.BLUE to starsEntry.starsCount(StarColor.BLUE),
            StarColor.PURPLE to starsEntry.starsCount(StarColor.PURPLE),
            StarColor.YELLOW to starsEntry.starsCount(StarColor.YELLOW)
        )
    }
    val text = buildAnnotatedString {
        counts.forEach {
            StarsCountItem(it.key, it.value)
        }
    }

    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
private fun AnnotatedString.Builder.StarsCountItem(color: StarColor, count: Int) {
    if (count <= 0) return
    withStyle(SpanStyle(color = color.color)) {
        if (count > 10) {
            append("★$count")
        }
        else {
            repeat(count) { append("★") }
        }
    }
}

// ------ //

@Preview
@Composable
private fun StarsCountTextPreview() {
    val starsEntry = StarsEntry(
        url = "",
        stars = buildList { repeat(100) { add(Star("")) } },
        coloredStars = listOf(
            ColorStars(StarColor.RED, listOf(Star(""))),
            ColorStars(StarColor.GREEN, listOf(Star(""), Star(""))),
            ColorStars(StarColor.BLUE, listOf(Star(""), Star(""), Star(""))),
        )
    )
    Column(
        Modifier.width(300.dp)
    ) {
        StarsCountText(starsEntry = starsEntry)
    }
}

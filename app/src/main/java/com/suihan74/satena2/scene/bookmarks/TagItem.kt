package com.suihan74.satena2.scene.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R

@Composable
fun TagItem(
    text: String,
    background: Color,
    foreground: Color
) {
    val shape = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        lineTo(-size.height / 3.5f, size.height / 2)
        lineTo(0f, 0f)
        close()
    }

    Box(
        Modifier
            .padding(start = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = background,
                    shape = shape
                )
                .padding(
                    top = .25.dp,
                    bottom = .25.dp,
                    start = 1.5.dp,
                    end = 4.dp
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tag),
                contentDescription = "",
                tint = foreground,
                modifier = Modifier
                    .size(with(LocalDensity.current) { 11.sp.toDp() })
            )
            Text(
                text = text,
                fontSize = 11.sp,
                color = foreground,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TagPreview() {
    TagItem(
        text = "タグ",
        background = Color.White,
        foreground = Color.Black
    )
}

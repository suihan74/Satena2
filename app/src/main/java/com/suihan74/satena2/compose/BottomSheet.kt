package com.suihan74.satena2.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * ボトムシートメニュー用の汎用項目
 */
@Composable
fun BottomSheetMenuItem(
    background: Color = Color.Transparent,
    color: Color = CurrentTheme.drawerOnBackground,
    textStyle: TextStyle? = null,
    innerPadding: PaddingValues = PaddingValues(16.dp),
    onLongClick: ()->Unit = {},
    onClick: ()->Unit = {},
    content: @Composable ()->Unit
) {
    ProvideTextStyle(
        value = textStyle ?: TextStyle.Default.copy(
            color = color,
            fontSize = 16.sp,

        )
    ) {
        Box(
            Modifier
                .background(color = background)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

/**
 * ボトムシートメニュー用のシンプルなテキスト項目
 */
@Composable
fun BottomSheetMenuItem(
    text: String,
    icon: Painter? = null,
    background: Color = Color.Transparent,
    color: Color = CurrentTheme.onBackground,
    textStyle: TextStyle? = null,
    innerPadding: PaddingValues = PaddingValues(16.dp),
    onLongClick: ()->Unit = {},
    onClick: ()->Unit = {}
) {
    BottomSheetMenuItem(
        background = background,
        color = color,
        textStyle = textStyle,
        innerPadding = innerPadding,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Image(
                    painter = it,
                    colorFilter = ColorFilter.tint(color = color),
                    contentDescription = ""
                )
                Spacer(Modifier.width(8.dp))
            }
            MarqueeText(text = text)
        }
    }
}


@Composable
fun BottomSheetMenuToggleItem(
    text: String,
    value: Boolean,
    background: Color = Color.Transparent,
    colorOn: Color = CurrentTheme.primary,
    colorOff: Color = CurrentTheme.onBackground,
    textStyle: TextStyle? = null,
    innerPadding: PaddingValues = PaddingValues(16.dp),
    onLongClick: ()->Unit = {},
    onClick: ()->Unit = {}
) {
    BottomSheetMenuItem(
        text = text,
        background = background,
        icon =
            if (value) painterResource(id = R.drawable.ic_check_circle_outline)
            else painterResource(id = R.drawable.ic_check),
        color = if (value) colorOn else colorOff,
        textStyle = textStyle,
        innerPadding = innerPadding,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

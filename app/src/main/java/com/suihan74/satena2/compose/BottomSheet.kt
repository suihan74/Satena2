package com.suihan74.satena2.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Text(text = text)
    }
}

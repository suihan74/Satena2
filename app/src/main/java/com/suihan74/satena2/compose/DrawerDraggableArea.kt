package com.suihan74.satena2.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ドロワをドラッグで表示する判定とページャのスクロール判定が競合するので
 * 画面端24dpはページャをマスクしてドロワ表示を優先する
 */
@Composable
fun BoxScope.DrawerDraggableArea(
    alignment: Alignment.Horizontal,
    width: Dp = 24.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        Modifier
            .hoverable(interactionSource)
            .width(width)
            .fillMaxHeight()
            .background(Color.Transparent)
            .align(
                if (alignment == Alignment.Start) Alignment.CenterStart
                else Alignment.CenterEnd
            )
    )
}

package com.suihan74.satena2.compose

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.max

/**
 * `Tooltip`をアンカービューと中央揃えで表示するための`PopupPositionProvider`
 */
@Immutable
data class TooltipPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }
        val horizontalDisplayLowerBound = 0
        val verticalDisplayLowerBound = with(density) { 8.dp.roundToPx() }

        // compute horizontal position
        val left = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val x = contentOffsetX + max(left, horizontalDisplayLowerBound)

        // compute vertical position
        val top = anchorBounds.top - popupContentSize.height
        val y = contentOffsetY + max(top, verticalDisplayLowerBound)

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
        )
        return IntOffset(x, y)
    }
}

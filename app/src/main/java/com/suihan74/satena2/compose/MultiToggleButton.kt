package com.suihan74.satena2.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * `MultiToggleButton`の色設定
 */
@Stable
data class MultiToggleButtonColors(
    val selectedBorderColor : Color,
    val unselectedBorderColor : Color,
    val selectedBackgroundColor : Color,
    val selectedTextColor : Color,
    val unselectedBackgroundColor : Color,
    val unselectedTextColor : Color
)

// ------ //

/**
 * `MultiToggleButton`のデフォルトの色設定
 */
object MultiToggleButtonDefaults {
    @Composable
    fun colors(
        selectedBorderColor : Color = MaterialTheme.colors.primary,
        unselectedBorderColor : Color = Color.Gray,
        selectedBackgroundColor : Color = MaterialTheme.colors.primary,
        selectedTextColor : Color = MaterialTheme.colors.onPrimary,
        unselectedBackgroundColor : Color = MaterialTheme.colors.background,
        unselectedTextColor : Color = Color.Gray,
    ) = remember(
        selectedBorderColor,
        unselectedBorderColor,
        selectedBackgroundColor,
        selectedTextColor,
        unselectedBackgroundColor,
        unselectedTextColor
    ) {
        MultiToggleButtonColors(
            selectedBorderColor,
            unselectedBorderColor,
            selectedBackgroundColor,
            selectedTextColor,
            unselectedBackgroundColor,
            unselectedTextColor
        )
    }
}

// ------ //

/**
 * テキストトグルボタン
 */
@Composable
fun MultiToggleButton(
    items: List<String>,
    selectedIndex: MutableState<Int>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    verticalPadding: Dp = 4.dp,
    horizontalPadding: Dp = 24.dp,
    borderThick: Dp = 1.dp,
    borderRound: Dp = 8.dp,
    fontSize: TextUnit = 14.sp,
    colors: MultiToggleButtonColors = MultiToggleButtonDefaults.colors(),
    onLongClick: ((Int)->Unit)? = null,
    onToggleChange: ((Int)->Unit)? = null
) {
    MultiToggleButton(
        contents = items.map {
            {
                Text(
                    text = it,
                    fontSize = fontSize,
                    modifier = Modifier.padding(4.dp)
                )
            }
        },
        selectedIndex = selectedIndex,
        modifier = modifier,
        enabled = enabled,
        verticalPadding = verticalPadding,
        horizontalPadding = horizontalPadding,
        borderThick = borderThick,
        borderRound = borderRound,
        colors = colors,
        onLongClick = onLongClick,
        onToggleChange = onToggleChange
    )
}

/**
 * トグルボタン
 */
@Composable
fun MultiToggleButton(
    contents: List<@Composable ()->Unit>,
    selectedIndex: MutableState<Int>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    verticalPadding: Dp = 4.dp,
    horizontalPadding: Dp = 24.dp,
    borderThick: Dp = 1.dp,
    borderRound: Dp = 8.dp,
    colors: MultiToggleButtonColors = MultiToggleButtonDefaults.colors(),
    onLongClick: ((Int)->Unit)? = null,
    onToggleChange: ((Int)->Unit)? = null
) {
    Row(modifier.height(IntrinsicSize.Min)) {
        contents.forEachIndexed { index, composable ->
            val isSelected = selectedIndex.value == index
            val backgroundColor =
                if (isSelected) colors.selectedBackgroundColor
                else colors.unselectedBackgroundColor
            val contentColor =
                if (isSelected) colors.selectedTextColor
                else colors.unselectedTextColor
            val borderColor =
                if (isSelected) colors.selectedBorderColor
                else colors.unselectedBorderColor
            val shape =
                when(index) {
                    0 -> RoundedCornerShape(topStart = borderRound, bottomStart = borderRound)
                    contents.lastIndex -> RoundedCornerShape(topEnd = borderRound, bottomEnd = borderRound)
                    else -> RectangleShape
                }

            Box(
                Modifier
                    .border(
                        border = BorderStroke(borderThick, borderColor),
                        shape = shape
                    )
                    .clip(shape)
                    .background(backgroundColor)
                    .combinedClickable(
                        enabled = enabled,
                        onClick = {
                            if (selectedIndex.value != index) {
                                selectedIndex.value = index
                                onToggleChange?.invoke(index)
                            }
                        },
                        onLongClick = { onLongClick?.invoke(index) }
                    )
                    .padding(
                        vertical = verticalPadding,
                        horizontal = horizontalPadding
                    )
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides contentColor
                ) {
                    composable()
                }
            }
        }
    }
}

// ------ //

@Preview
@Composable
private fun MultiToggleButtonPreview() {
    val selectedIndex = remember { mutableStateOf(0) }
    MultiToggleButton(
        items = listOf("URL", "TEXT"),
        colors = MultiToggleButtonDefaults.colors(
            selectedTextColor = MaterialTheme.colors.onPrimary,
            selectedBackgroundColor = MaterialTheme.colors.primary,
            unselectedBackgroundColor = Color.Transparent
        ),
        selectedIndex = selectedIndex
    )
}

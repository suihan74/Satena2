package com.suihan74.satena2.compose.dialog

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class CustomDialogColors(
    val backgroundColor : Color,
    val textColor : Color,
    val positiveButtonTextColor : Color,
    val negativeButtonTextColor : Color,
    val neutralButtonTextColor : Color
)

// ------ //

object CustomDialogDefaults {
    val DEFAULT_TITLE_HORIZONTAL_PADDING = 16.dp

    val DEFAULT_TITLE_VERTICAL_PADDING = 16.dp

    const val DEFAULT_WIDTH_RATIO = 0.9f

    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colors.background,
        titleTextColor: Color = MaterialTheme.colors.onBackground,
        positiveButtonTextColor: Color = MaterialTheme.colors.primary,
        negativeButtonTextColor: Color = MaterialTheme.colors.primary,
        neutralButtonTextColor: Color = MaterialTheme.colors.primary
    ) = remember(
        backgroundColor,
        titleTextColor,
        positiveButtonTextColor,
        negativeButtonTextColor,
        neutralButtonTextColor
    ) {
        CustomDialogColors(
            backgroundColor,
            titleTextColor,
            positiveButtonTextColor,
            negativeButtonTextColor,
            neutralButtonTextColor
        )
    }
}

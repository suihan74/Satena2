package com.suihan74.satena2.ui.theme.themed

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.compose.dialog.SliderDialogDefaults
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun themedSliderDialogColors(
    backgroundColor: Color = CurrentTheme.background,
    titleTextColor: Color = CurrentTheme.onBackground,
    positiveButtonTextColor: Color = CurrentTheme.primary,
    negativeButtonTextColor: Color = CurrentTheme.primary,
    neutralButtonTextColor: Color = CurrentTheme.primary,
    thumbColor: Color = CurrentTheme.primary
) = SliderDialogDefaults.colors(
    backgroundColor = backgroundColor,
    textColor = titleTextColor,
    positiveButtonTextColor = positiveButtonTextColor,
    negativeButtonTextColor = negativeButtonTextColor,
    neutralButtonTextColor = neutralButtonTextColor,
    thumbColor = thumbColor
)

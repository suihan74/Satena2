package com.suihan74.satena2.ui.theme.themed

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.compose.dialog.CustomDialogDefaults
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun themedCustomDialogColors(
    backgroundColor: Color = CurrentTheme.background,
    titleTextColor: Color = CurrentTheme.onBackground,
    positiveButtonTextColor: Color = CurrentTheme.primary,
    negativeButtonTextColor: Color = CurrentTheme.primary,
    neutralButtonTextColor: Color = CurrentTheme.primary
) = CustomDialogDefaults.colors(
    backgroundColor,
    titleTextColor,
    positiveButtonTextColor,
    negativeButtonTextColor,
    neutralButtonTextColor
)

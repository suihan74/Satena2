package com.suihan74.satena2.ui.theme.themed

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.compose.MultiToggleButtonDefaults
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun themedMultiToggleButtonColors(
    selectedBorderColor : Color = CurrentTheme.primary,
    unselectedBorderColor : Color = CurrentTheme.grayTextColor,
    selectedBackgroundColor : Color = CurrentTheme.primary,
    selectedTextColor : Color = CurrentTheme.onPrimary,
    unselectedBackgroundColor : Color = CurrentTheme.background,
    unselectedTextColor : Color = CurrentTheme.grayTextColor,
) = MultiToggleButtonDefaults.colors(
    selectedBorderColor,
    unselectedBorderColor,
    selectedBackgroundColor,
    selectedTextColor,
    unselectedBackgroundColor,
    unselectedTextColor,
)

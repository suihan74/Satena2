package com.suihan74.satena2.ui.theme.themed

import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun themedCheckboxColors(
    checkedColor: Color = CurrentTheme.primary,
    uncheckedColor: Color = CurrentTheme.grayTextColor.copy(alpha = 0.6f),
    checkmarkColor: Color = CurrentTheme.onPrimary,
    disabledColor: Color = CurrentTheme.grayTextColor.copy(alpha = ContentAlpha.disabled),
    disabledIndeterminateColor: Color = checkedColor.copy(alpha = ContentAlpha.disabled)
) = CheckboxDefaults.colors(
    checkedColor,
    uncheckedColor,
    checkmarkColor,
    disabledColor,
    disabledIndeterminateColor
)

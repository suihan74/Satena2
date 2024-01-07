package com.suihan74.satena2.ui.theme.themed

import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun themedTextFieldColors(
    textColor: Color = CurrentTheme.onBackground,
    placeholderColor: Color = CurrentTheme.grayTextColor,
    cursorColor: Color = CurrentTheme.primary,
    focusedIndicatorColor: Color = CurrentTheme.primary,
    unfocusedIndicatorColor: Color = CurrentTheme.primary.copy(alpha = TextFieldDefaults.UnfocusedIndicatorLineOpacity),
    focusedLabelColor: Color = CurrentTheme.primary,
    unfocusedLabelColor: Color = CurrentTheme.grayTextColor,
    trailingIconColor: Color = CurrentTheme.primary
) = TextFieldDefaults.textFieldColors(
    textColor = textColor,
    placeholderColor = placeholderColor,
    cursorColor = cursorColor,
    focusedIndicatorColor = focusedIndicatorColor,
    unfocusedIndicatorColor = unfocusedIndicatorColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    trailingIconColor = trailingIconColor
)

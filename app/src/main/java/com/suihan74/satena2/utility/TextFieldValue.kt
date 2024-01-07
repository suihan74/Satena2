package com.suihan74.satena2.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 初期状態で文字列を全選択する`TextFieldValue`
 */
@Composable
fun rememberMutableTextFieldValue(
    text: String = "",
    selection: TextRange = TextRange(0, text.length),
    composition: TextRange? = null
) = remember {
    mutableStateOf(
        TextFieldValue(
            text = text,
            selection = selection,
            composition = composition
        )
    )
}

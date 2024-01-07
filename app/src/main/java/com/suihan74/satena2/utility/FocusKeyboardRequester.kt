package com.suihan74.satena2.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay

/**
 * [Modifier]#focusRequester()の引数に渡した直後に
 * フォーカスと共にソフトキーボードの表示をリクエストする[FocusRequester]
 *
 * [TextField]表示と同時に入力開始できるようにする用途
 *
 */
@Composable
fun focusKeyboardRequester() : FocusRequester {
    val softKeyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(true) {
        //delay(300)
        softKeyboardController?.show()
        focusRequester.requestFocus()
    }
    return focusRequester
}

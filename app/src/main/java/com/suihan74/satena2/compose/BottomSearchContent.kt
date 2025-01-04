package com.suihan74.satena2.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.rememberMutableTextFieldValue

/**
 * ボトムバー内に表示する検索コンテンツ
 */
@Composable
fun BottomSearchContent(
    initialSearchQuery: String,
    onSearch: (String)->Unit,
    onClose: ()->Unit
) {
    val textFieldValue = rememberMutableTextFieldValue(text = initialSearchQuery)
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchAction = {
        softwareKeyboardController?.hide()
        focusManager.clearFocus()
        onSearch(textFieldValue.value.text)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextField(
            value = textFieldValue.value,
            onValueChange = { textFieldValue.value = it },
            placeholder = { Text(stringResource(R.string.search)) },
            colors = TextFieldDefaults.textFieldColors(
                textColor = CurrentTheme.onBackground,
                placeholderColor = CurrentTheme.grayTextColor,
                cursorColor = CurrentTheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                trailingIconColor = CurrentTheme.primary
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { searchAction() }),
            shape = RectangleShape,
            modifier = Modifier.weight(1f)
        )
        BottomMenuItemButton(
            iconId = R.drawable.ic_category_search,
            textId = R.string.search,
            onClick = searchAction
        )
        BottomMenuItemButton(
            iconId = R.drawable.ic_close,
            textId = R.string.close,
            onClick = { onClose() }
        )
    }
}

package com.suihan74.satena2.scene.entries.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue

@Composable
fun BrowserLauncher(
    searchAction: (String)->Unit
) {
    val textFieldValue = rememberMutableTextFieldValue(text = "")

    val focusRequester = focusKeyboardRequester()
    val keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    val keyboardActions = KeyboardActions(onSearch = { searchAction(textFieldValue.value.text) })

    Column {
        Text(
            text = stringResource(R.string.inner_browser),
            fontSize = 18.sp,
            color = CurrentTheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )
        TextField(
            value = textFieldValue.value,
            onValueChange = { textFieldValue.value = it },
            placeholder = { Text(stringResource(R.string.search_setting_sheet_query_placeholder)) },
            colors = TextFieldDefaults.textFieldColors(
                textColor = CurrentTheme.onBackground,
                placeholderColor = CurrentTheme.grayTextColor,
                cursorColor = CurrentTheme.primary,
                focusedIndicatorColor = CurrentTheme.primary,
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

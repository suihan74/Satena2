package com.suihan74.satena2.scene.preferences.page.ngWords.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.MultiToggleButton
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.model.ignoredEntry.IgnoreTarget
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntryType
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCheckboxColors
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedMultiToggleButtonColors
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue

/**
 * URLテキストフィールドの初期選択範囲を作成する
 *
 * deleteキーいっぱつで選択範囲を一括で消せるようにするための処置
 *
 * TwitterやNoteの場合は"twitter.com/userName"より後の部分にする
 */
private fun makeUrlInitialSelection(query: String) : TextRange {
    val domains = listOf(
        "twitter.com/", "note.com/"
    )
    val separatorIdx =
        domains.firstOrNull { query.startsWith(it) }
            ?.let { query.indexOf('/', startIndex = it.length) }
            ?: query.indexOf('/')

    return if (separatorIdx < 0) TextRange(0, query.length)
    else TextRange(separatorIdx + if (separatorIdx == query.lastIndex) 0 else 1, query.length)
}

/**
 * 非表示設定を追加/更新するためのダイアログ
 *
 * @param item 編集元の非表示設定（これを渡すと更新モードで表示する）
 */
@Composable
fun NgWordEditionDialog(
    item: IgnoredEntry? = null,
    visibility: MutableState<Boolean>,
    initialText: String = "",
    initialUrl: String = "",
    initialTabIndex: Int = item?.type?.ordinal ?: 0,
    properties: DialogProperties = DialogProperties(),
    isError: (String, Boolean) -> Boolean = { _, _ -> false },
    onRegistration: (suspend (NgWordEditionResult)->Boolean)? = null
) {
    if (!visibility.value) return

    val urlInitialSelection = remember { makeUrlInitialSelection(item?.query ?: initialUrl) }
    val urlQuery = rememberMutableTextFieldValue(text = item?.query ?: initialUrl, selection = urlInitialSelection)
    val textQuery = rememberMutableTextFieldValue(text = item?.query ?: initialText)
    val selectedTabIndex = remember { mutableStateOf(initialTabIndex) }
    val asRegexChecked = remember { mutableStateOf(item?.asRegex ?: false) }
    val ignoreEntryChecked = remember { mutableStateOf(item?.target?.contains(IgnoreTarget.ENTRY) ?: true) }
    val ignoreCommentChecked = remember { mutableStateOf(item?.target?.contains(IgnoreTarget.BOOKMARK) ?: true) }
    val titleTextId = remember(item) {
        if (item == null) R.string.ng_word_setting_dialog_title_insertion
        else R.string.ng_word_setting_dialog_title_edition
    }

    when (selectedTabIndex.value) {
        IgnoredEntryType.TEXT.ordinal -> {
            NgWordEditionDialogImpl(
                title = stringResource(titleTextId),
                selectedTabIndex = selectedTabIndex,
                textQuery = textQuery.value.text,
                urlQuery = urlQuery.value.text,
                ignoreEntryChecked = ignoreEntryChecked.value,
                ignoreCommentChecked = ignoreCommentChecked.value,
                asRegexChecked = asRegexChecked.value,
                item = item,
                properties = properties,
                visibility = visibility,
                onRegistration = onRegistration
            ) {
                DialogContentForText(
                    textQuery,
                    asRegexChecked,
                    ignoreEntryChecked,
                    ignoreCommentChecked,
                    isError = isError
                )
            }
        }

        IgnoredEntryType.URL.ordinal -> {
            NgWordEditionDialogImpl(
                title = stringResource(titleTextId),
                selectedTabIndex = selectedTabIndex,
                textQuery = textQuery.value.text,
                urlQuery = urlQuery.value.text,
                ignoreEntryChecked = ignoreEntryChecked.value,
                ignoreCommentChecked = ignoreCommentChecked.value,
                asRegexChecked = asRegexChecked.value,
                item = item,
                properties = properties,
                visibility = visibility,
                onRegistration = onRegistration
            ) {
                DialogContentForUrl(urlQuery)
            }
        }
    }
}

@Composable
private fun NgWordEditionDialogImpl(
    title: String,
    selectedTabIndex: MutableState<Int>,
    textQuery: String,
    urlQuery: String,
    ignoreEntryChecked: Boolean,
    ignoreCommentChecked: Boolean,
    asRegexChecked: Boolean,
    properties: DialogProperties,
    visibility: MutableState<Boolean>,
    item: IgnoredEntry? = null,
    onRegistration: (suspend (NgWordEditionResult)->Boolean)? = null,
    content: @Composable ()->Unit = {}
) {
    CustomDialog(
        titleText = title,
        positiveButton = dialogButton(R.string.register) {
            val args = NgWordEditionResult(
                tabIndex = selectedTabIndex.value,
                text = textQuery,
                url = urlQuery,
                targetEntry = ignoreEntryChecked,
                targetBookmark = ignoreCommentChecked,
                asRegex = asRegexChecked
            )

            val result = onRegistration?.invoke(args) ?: true

            if (result) {
                visibility.value = false
            }
        },
        negativeButton = dialogButton(R.string.cancel) {
            visibility.value = false
        },
        colors = themedCustomDialogColors(),
        onDismissRequest = { visibility.value = false },
        properties = properties,
    ) {
        Column(
            Modifier
                .background(color = CurrentTheme.background)
                .fillMaxWidth()
        ) {
            if (item == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    MultiToggleButton(
                        items = IgnoredEntryType.values().map { it.name },
                        selectedIndex = selectedTabIndex,
                        colors = themedMultiToggleButtonColors(),
                        horizontalPadding = 40.dp
                    ) { index -> selectedTabIndex.value = index }
                }
            }

            content()
        }
    }
}

// ------ //

/**
 * NG TEXT設定用コンテンツ
 */
@Composable
private fun DialogContentForText(
    textFieldValue: MutableState<TextFieldValue>,
    asRegexChecked: MutableState<Boolean>,
    ignoreEntryChecked: MutableState<Boolean>,
    ignoreCommentChecked: MutableState<Boolean>,
    isError: (String, Boolean)->Boolean
) {
    val keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done
    )
    val focusRequester = focusKeyboardRequester()

    Column {
        TextField(
            value = textFieldValue.value,
            onValueChange = { textFieldValue.value = it },
            placeholder = { Text("非表示対象のキーワード") },
            colors = TextFieldDefaults.textFieldColors(
                textColor = CurrentTheme.onBackground,
                placeholderColor = CurrentTheme.grayTextColor,
                cursorColor = CurrentTheme.onBackground,
                focusedIndicatorColor = CurrentTheme.primary,
            ),
            maxLines = 3,
            isError = isError(textFieldValue.value.text, asRegexChecked.value),
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        Text(
            text = "↑の文字列を含むエントリ(orブコメ)を非表示にする",
            color = CurrentTheme.onBackground
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = asRegexChecked.value,
                onCheckedChange = { asRegexChecked.value = it },
                colors = themedCheckboxColors()
            )
            Text(
                text = stringResource(R.string.ng_word_setting_as_regex),
                color = CurrentTheme.onBackground,
                modifier = Modifier.clickable {
                    asRegexChecked.value = !asRegexChecked.value
                }
            )
        }
        Text(
            text = stringResource(R.string.ng_word_setting_target_desc),
            color = CurrentTheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = ignoreEntryChecked.value,
                onCheckedChange = { ignoreEntryChecked.value = it },
                colors = themedCheckboxColors()
            )
            Text(
                text = stringResource(R.string.ng_word_setting_target_entry),
                color = CurrentTheme.onBackground,
                modifier = Modifier.clickable { ignoreEntryChecked.value = !ignoreEntryChecked.value }
            )

            Checkbox(
                checked = ignoreCommentChecked.value,
                onCheckedChange = { ignoreCommentChecked.value = it },
                colors = themedCheckboxColors(),
                modifier = Modifier.padding(start = 16.dp)
            )
            Text(
                text = stringResource(R.string.ng_word_setting_target_bookmark),
                color = CurrentTheme.onBackground,
                modifier = Modifier.clickable { ignoreCommentChecked.value = !ignoreCommentChecked.value }
            )
        }
    }
}

// ------ //

/**
 * NG URL設定用コンテンツ
 */
@Composable
private fun DialogContentForUrl(urlQuery: MutableState<TextFieldValue>) {
    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Ascii,
        imeAction = ImeAction.Done
    )

    val focusRequester = focusKeyboardRequester()
    Column {
        TextField(
            value = urlQuery.value,
            onValueChange = { urlQuery.value = it },
            colors = TextFieldDefaults.textFieldColors(
                textColor = CurrentTheme.onBackground,
                placeholderColor = CurrentTheme.grayTextColor,
                cursorColor = CurrentTheme.onBackground,
                focusedIndicatorColor = CurrentTheme.primary,
                trailingIconColor = CurrentTheme.primary
            ),
            placeholder = { Text("非表示対象のURL") },
            maxLines = 3,
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        Text(
            text = "↑から始まるURLのエントリを非表示にする",
            color = CurrentTheme.onBackground
        )
    }
}

// ------ //

@Preview
@Composable
private fun InsertModePreview() {
    val visibility = remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        NgWordEditionDialog(
            visibility = visibility
        )
    }
}

@Preview
@Composable
private fun EditModePreview() {
    val visibility = remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        NgWordEditionDialog(
            item = IgnoredEntry.createDummy(),
            visibility = visibility
        )
    }
}

@Preview
@Composable
private fun DialogContentForTextPreview() {
    val query = rememberMutableTextFieldValue(text = "")
    val asRegexChecked = remember { mutableStateOf(true) }
    val ignoreEntryChecked = remember { mutableStateOf(true) }
    val ignoreBookmarkChecked = remember { mutableStateOf(false) }
    Box(Modifier.background(CurrentTheme.background)) {
        DialogContentForText(
            query,
            asRegexChecked,
            ignoreEntryChecked,
            ignoreBookmarkChecked,
            isError = { _, _ -> false }
        )
    }
}

@Preview
@Composable
private fun DialogContentForUrlPreview() {
    val query = rememberMutableTextFieldValue(text = "")
    Box(Modifier.background(CurrentTheme.background)) {
        DialogContentForUrl(query)
    }
}

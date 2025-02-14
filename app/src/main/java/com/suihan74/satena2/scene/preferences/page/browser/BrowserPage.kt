package com.suihan74.satena2.scene.preferences.page.browser

import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.model.browser.BrowserType
import com.suihan74.satena2.model.browser.WebViewTheme
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.scene.preferences.page.MutableComposableList
import com.suihan74.satena2.scene.preferences.page.buildComposableList
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedTextFieldColors
import com.suihan74.satena2.utility.extension.add
import com.suihan74.satena2.utility.extension.textId
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import kotlinx.coroutines.launch

/**
 * 「アプリ内ブラウザ」ページのコンテンツ
 */
@Composable
fun browserPageContents(viewModel: BrowserViewModel) = buildComposableList {
    val browserType by viewModel.browserType.collectAsState()
    basicSection(viewModel)
    if (browserType == BrowserType.WEB_VIEW) {
        appearanceSection(viewModel)
        functionSection(viewModel)
    }
}

private fun MutableComposableList.basicSection(viewModel: BrowserViewModel) = add(
    0 to { Section(R.string.pref_browser_section_basic) },
    R.string.pref_browser_type to {
        var dialogVisible by remember { mutableStateOf(false) }
        val current by viewModel.browserType.collectAsState()
        PrefButton(
            mainTextId = R.string.pref_browser_type,
            subTextId = current.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val items = BrowserType.entries.map {
                menuDialogItem(
                    textId = it.textId,
                    action = {
                        viewModel.browserType.value = it
                        true
                    }
                )
            }
            MenuDialog(
                titleText = stringResource(R.string.pref_browser_type),
                menuItems = items,
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    R.string.pref_browser_start_page to {
        var dialogVisible by remember { mutableStateOf(false) }
        val current by viewModel.startPageUrl.collectAsState()

        PrefButton(
            mainText = stringResource(R.string.pref_browser_start_page),
            subText = current,
            subTextPrefix = stringResource(R.string.pref_current_value_prefix)
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val coroutineScope = rememberCoroutineScope()
            val textFieldValue = rememberMutableTextFieldValue(text = current)
            val focusRequester = focusKeyboardRequester()

            CustomDialog(
                titleText = stringResource(R.string.pref_browser_start_page),
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                positiveButton = dialogButton(R.string.register) {
                    coroutineScope.launch {
                        viewModel.startPageUrl.value = textFieldValue.value.text
                        dialogVisible = false
                    }
                },
                neutralButton =
                    viewModel.currentUrl.value?.let { url ->
                        dialogButton(R.string.pref_browser_current_page) {
                            textFieldValue.value = textFieldValue.value.copy(
                                text = url,
                                selection = TextRange(start = 0, end = url.length)
                            )
                        }
                    },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            ) {
                TextField(
                    value = textFieldValue.value,
                    onValueChange = { textFieldValue.value = it },
                    placeholder = { Text(
                        stringResource(R.string.pref_browser_start_page_url_placeholder)
                    ) },
                    singleLine = true,
                    maxLines = 1,
                    colors = themedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        }
    }
)

private fun MutableComposableList.appearanceSection(viewModel: BrowserViewModel) = add(
    0 to { Section(R.string.pref_browser_section_appearance) },
    R.string.pref_browser_address_bar_alignment to {
        var dialogVisible by remember { mutableStateOf(false) }
        val current by viewModel.addressBarAlignment.collectAsState()
        PrefButton(
            mainTextId = R.string.pref_browser_address_bar_alignment,
            subTextId = current.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val aligns = listOf(Alignment.Top, Alignment.Bottom)
            val items = aligns.map {
                menuDialogItem(
                    textId = it.textId,
                    action = { viewModel.addressBarAlignment.value = it; true }
                )
            }
            MenuDialog(
                titleText = stringResource(R.string.pref_browser_address_bar_alignment),
                menuItems = items,
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    R.string.pref_browser_web_view_theme to {
        var dialogVisible by remember { mutableStateOf(false) }
        val current by viewModel.webViewTheme.collectAsState()
        PrefButton(
            mainTextId = R.string.pref_browser_web_view_theme,
            subTextId = current.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val items = WebViewTheme.values(Build.VERSION.SDK_INT).map {
                menuDialogItem(
                    textId = it.textId,
                    action = { viewModel.webViewTheme.value = it; true }
                )
            }
            MenuDialog(
                titleText = stringResource(R.string.pref_browser_web_view_theme),
                menuItems = items,
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    }
)

private fun MutableComposableList.functionSection(viewModel: BrowserViewModel) = add(
    0 to { Section(R.string.pref_browser_section_function) },
)

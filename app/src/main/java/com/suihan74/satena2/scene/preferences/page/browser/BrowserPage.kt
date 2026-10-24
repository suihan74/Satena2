package com.suihan74.satena2.scene.preferences.page.browser

import android.os.Build
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AnimatedListItem
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.icons.ArrowBack
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.model.browser.BrowserType
import com.suihan74.satena2.model.browser.WebViewTheme
import com.suihan74.satena2.model.theme.default.DefaultThemePresetLight
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.PrefItemDefaults
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.scene.preferences.page.BasicPreferencesPage
import com.suihan74.satena2.scene.preferences.page.MutableComposableList
import com.suihan74.satena2.scene.preferences.page.buildComposableList
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2Theme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedTextFieldColors
import com.suihan74.satena2.utility.extension.add
import com.suihan74.satena2.utility.extension.textId
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import kotlinx.coroutines.launch

/**
 * ブラウザ設定ページ
 */
@Composable
fun BrowserPageContents(
    viewModel: BrowserViewModel,
    pagerState: PagerState,
    navigationBarInset: Dp
) {
    val navController = rememberNavController()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect {
                navController.navigate("basic") { popUpTo(0) }
            }
    }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "basic",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable("basic") {
            val browserType by viewModel.browserType.collectAsState()
            val startPageUrl by viewModel.startPageUrl.collectAsState()
            val currentUrl by viewModel.currentUrl.collectAsState()
            val addressBarAlignment by viewModel.addressBarAlignment.collectAsState()
            val webViewTheme by viewModel.webViewTheme.collectAsState()

            BasicPreferencesPage(
                contents = basicPageContents(
                    browserType = browserType,
                    startPageUrl = startPageUrl,
                    currentUrl = currentUrl,
                    addressBarAlignment = addressBarAlignment,
                    webViewTheme = webViewTheme,
                    onUpdateBrowserType = { viewModel.browserType.value = it },
                    onUpdateStartPageUrl = { viewModel.startPageUrl.value = it },
                    onUpdateAddressBarAlignment = { viewModel.addressBarAlignment.value = it },
                    onUpdateWebViewTheme = { viewModel.webViewTheme.value = it },
                    onClickedBlockSettings = { navController.navigate("block_settings") },
                    dialogProperties = viewModel.dialogProperties()
                )
            )
        }

        composable(
            "block_settings",
            enterTransition = {
                slideInHorizontally(animationSpec = tween(300)) { it }
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween(300)) { it }
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(300)) { it }
            }
        ) {
            val blockedResources by viewModel.blockResources.collectAsState()

            BlockedResourcesPage(
                blockedResources = blockedResources,
                navigationBarInset = navigationBarInset,
                onBack = { navController.popBackStack() },
                onEditItem = {},
                onDeleteItem = { viewModel.deleteBlockedResource(it) },
                dialogProperties = viewModel.dialogProperties()
            )
        }
    }
}

/**
 * 「アプリ内ブラウザ」ページのコンテンツ
 */
@Composable
private fun basicPageContents(
    browserType: BrowserType,
    startPageUrl: String,
    currentUrl: String?,
    addressBarAlignment: Alignment.Vertical,
    webViewTheme: WebViewTheme,
    onUpdateBrowserType: (BrowserType) -> Unit = {},
    onUpdateStartPageUrl: (String) -> Unit = {},
    onUpdateAddressBarAlignment: (Alignment.Vertical) -> Unit = {},
    onUpdateWebViewTheme: (WebViewTheme) -> Unit = {},
    onClickedBlockSettings: () -> Unit = {},
    dialogProperties: DialogProperties = DialogProperties()
) = buildComposableList {
    basicSection(
        browserType = browserType,
        startPageUrl = startPageUrl,
        currentUrl = currentUrl,
        onUpdateBrowserType = onUpdateBrowserType,
        onUpdateStartPageUrl = onUpdateStartPageUrl,
        dialogProperties = dialogProperties
    )
    if (browserType == BrowserType.WEB_VIEW) {
        appearanceSection(
            addressBarAlignment = addressBarAlignment,
            webViewTheme = webViewTheme,
            onUpdateAddressBarAlignment = onUpdateAddressBarAlignment,
            onUpdateWebViewTheme = onUpdateWebViewTheme,
            dialogProperties = dialogProperties
        )
        functionSection(
            onClickedBlockSettings = onClickedBlockSettings
        )
    }
}

private fun MutableComposableList.basicSection(
    browserType: BrowserType,
    startPageUrl: String,
    currentUrl: String?,
    onUpdateBrowserType: (BrowserType) -> Unit,
    onUpdateStartPageUrl: (String) -> Unit,
    dialogProperties: DialogProperties
) = add(
    0 to { Section(R.string.pref_browser_section_basic) },
    R.string.pref_browser_type to {
        var dialogVisible by remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_browser_type,
            subTextId = browserType.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val items = BrowserType.entries.map {
                menuDialogItem(
                    textId = it.textId,
                    action = {
                        onUpdateBrowserType(it)
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
                properties = dialogProperties
            )
        }
    },
    R.string.pref_browser_start_page to {
        var dialogVisible by remember { mutableStateOf(false) }

        PrefButton(
            mainText = stringResource(R.string.pref_browser_start_page),
            subText = startPageUrl,
            subTextPrefix = stringResource(R.string.pref_current_value_prefix)
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val coroutineScope = rememberCoroutineScope()
            val textFieldValue = rememberMutableTextFieldValue(text = startPageUrl)
            val focusRequester = focusKeyboardRequester()

            CustomDialog(
                titleText = stringResource(R.string.pref_browser_start_page),
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                positiveButton = dialogButton(R.string.register) {
                    coroutineScope.launch {
                        onUpdateStartPageUrl(textFieldValue.value.text)
                        dialogVisible = false
                    }
                },
                neutralButton =
                    currentUrl?.let { url ->
                        dialogButton(R.string.pref_browser_current_page) {
                            textFieldValue.value = textFieldValue.value.copy(
                                text = url,
                                selection = TextRange(start = 0, end = url.length)
                            )
                        }
                    },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = dialogProperties
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

private fun MutableComposableList.appearanceSection(
    addressBarAlignment: Alignment.Vertical,
    webViewTheme: WebViewTheme,
    onUpdateAddressBarAlignment: (Alignment.Vertical) -> Unit,
    onUpdateWebViewTheme: (WebViewTheme) -> Unit,
    dialogProperties: DialogProperties
) = add(
    0 to { Section(R.string.pref_browser_section_appearance) },
    R.string.pref_browser_address_bar_alignment to {
        var dialogVisible by remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_browser_address_bar_alignment,
            subTextId = addressBarAlignment.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val aligns = listOf(Alignment.Top, Alignment.Bottom)
            val items = aligns.map {
                menuDialogItem(
                    textId = it.textId,
                    action = { onUpdateAddressBarAlignment(it); true }
                )
            }
            MenuDialog(
                titleText = stringResource(R.string.pref_browser_address_bar_alignment),
                menuItems = items,
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = dialogProperties
            )
        }
    },
    R.string.pref_browser_web_view_theme to {
        var dialogVisible by remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_browser_web_view_theme,
            subTextId = webViewTheme.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            val items = WebViewTheme.values(Build.VERSION.SDK_INT).map {
                menuDialogItem(
                    textId = it.textId,
                    action = { onUpdateWebViewTheme(it); true }
                )
            }
            MenuDialog(
                titleText = stringResource(R.string.pref_browser_web_view_theme),
                menuItems = items,
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = dialogProperties
            )
        }
    }
)

private fun MutableComposableList.functionSection(
    onClickedBlockSettings: () -> Unit
) = add(
    0 to { Section(R.string.pref_browser_section_function) },
    R.string.pref_browser_block_settings to {
        PrefButton(
            mainTextId = R.string.pref_browser_block_settings
        ) {
            onClickedBlockSettings()
        }
    }
)

// ------ //

/**
 * 現在有効なブロックリソース設定一覧
 */
@Composable
fun BlockedResourcesPage(
    blockedResources: List<String>,
    navigationBarInset: Dp,
    onBack: ()->Unit,
    onEditItem: (String)->Unit,
    onDeleteItem: (String)->Unit,
    dialogProperties: DialogProperties
) {
    val listState = rememberLazyListState()
    val clickedItem = remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // タイトル
            Column(
                Modifier
                    .background(CurrentTheme.bottomBarBackground)
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.pref_browser_block_settings),
                    color = CurrentTheme.bottomBarOnBackground,
                    fontSize = 18.sp
                )
            }

            // コンテンツ
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(
                        state = listState,
                        color = CurrentTheme.primary
                    )
            ) {
                items(blockedResources) { item ->
                    AnimatedListItem {
                        NgResourceItem(
                            text = item,
                            onClick = { clickedItem.value = item }
                        )
                    }
                    Divider(
                        color = CurrentTheme.listItemDivider,
                        thickness = 1.dp
                    )
                }
                emptyFooter(
                    height = 112.dp + navigationBarInset
                )
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp + navigationBarInset)
        ) {
            // 戻るボタン
            FloatingActionButton(
                backgroundColor = CurrentTheme.primary,
                contentColor = CurrentTheme.onPrimary,
                modifier = Modifier.size(36.dp),
                onClick = onBack
            ) {
                Icon(
                    ArrowBack,
                    contentDescription = "back a screen"
                )
            }
        }

        Dialogs(
            clickedItem = clickedItem,
            onEdit = { /* todo */ },
            onDelete = onDeleteItem,
            dialogProperties = dialogProperties
        )
    }
}

// ------ //

/**
 * NGリソース設定項目
 */
@Composable
private fun NgResourceItem(
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = onClick,
                onClick = onClick
            )
            .padding(
                vertical = PrefItemDefaults.listItemVerticalPadding,
                horizontal = PrefItemDefaults.listItemHorizontalPadding
            )
    ) {
        Text(
            text = text,
            color = CurrentTheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Dialogs(
    clickedItem: MutableState<String>,
    onEdit: (String)->Unit,
    onDelete: (String)->Unit,
    dialogProperties: DialogProperties
) {
    // 項目長押しメニュー
    if (clickedItem.value.isNotEmpty()) {
        MenuDialog(
            titleText = clickedItem.value,
            menuItems = listOf(
                menuDialogItem("編集") {
                    onEdit(clickedItem.value)
                    true
                },
                menuDialogItem("削除") {
                    onDelete(clickedItem.value)
                    true
                },
            ),
            negativeButton = dialogButton(R.string.close) { clickedItem.value = "" },
            onDismissRequest = { clickedItem.value = "" },
            colors = themedCustomDialogColors(),
            properties = dialogProperties
        )
    }
}

// ------ //

@Preview
@Composable
private fun BrowserPageContentsPreview() {
    Satena2Theme(theme = DefaultThemePresetLight) {
        BasicPreferencesPage(
            contents = basicPageContents(
                browserType = BrowserType.WEB_VIEW,
                startPageUrl = "https://b.hatena.ne.jp/",
                currentUrl = "https://b.hatena.ne.jp/",
                addressBarAlignment = Alignment.Bottom,
                webViewTheme = WebViewTheme.AUTO
            )
        )
    }
}

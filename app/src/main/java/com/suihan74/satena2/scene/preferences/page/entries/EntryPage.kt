package com.suihan74.satena2.scene.preferences.page.entries

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.CombinedIconButton
import com.suihan74.satena2.compose.VerticalScrollableIndicator
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.scene.entries.BottomMenuItem
import com.suihan74.satena2.scene.entries.Category
import com.suihan74.satena2.scene.entries.ClickEntryAction
import com.suihan74.satena2.scene.entries.EntryCategoryListType
import com.suihan74.satena2.scene.entries.EntryNavigationState
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.PrefItemDefaults
import com.suihan74.satena2.scene.preferences.PrefToggleButton
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.scene.preferences.page.BasicPreferencesPage
import com.suihan74.satena2.scene.preferences.page.MutableComposableList
import com.suihan74.satena2.scene.preferences.page.buildComposableList
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.utility.extension.add
import com.suihan74.satena2.utility.extension.textId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
internal fun EntryPage(viewModel: EntryViewModel) {
    val initialTabsSelectorVisible = remember { mutableStateOf(false) }

    BasicPreferencesPage(
        state = viewModel.lazyListState(),
        contents = entryPageContents(
            viewModel = viewModel,
            initialTabsSelectorVisible = initialTabsSelectorVisible
        )
    )

    if (initialTabsSelectorVisible.value) {
        InitialTabsSelector(viewModel, initialTabsSelectorVisible)
    }
}

/**
 * 「エントリ」ページのコンテンツ
 */
@Composable
fun entryPageContents(
    viewModel: EntryViewModel,
    initialTabsSelectorVisible: MutableState<Boolean>,
) = buildComposableList {
    bottomMenuSection(viewModel)
    actionsSection(viewModel)
    categorySection(viewModel, initialTabsSelectorVisible)
    behaviorSection(viewModel)
    historySection(viewModel)
    readLaterSection()
}

private fun MutableComposableList.bottomMenuSection(viewModel: EntryViewModel) = add(
    0 to { Section(R.string.pref_entry_section_bottom_menu) },
    R.string.pref_entry_use_bottom_menu to { PrefToggleButton(viewModel.useBottomMenu, R.string.pref_entry_use_bottom_menu) },
    R.string.pref_entry_bottom_menu_arrangement to item@ {
        val useBottomMenu by viewModel.useBottomMenu.collectAsState(initial = false)
        if (!useBottomMenu) return@item
        val dialogVisible = remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_entry_bottom_menu_arrangement,
            subTextPrefixId = R.string.pref_current_value_prefix,
            subTextId = viewModel.bottomMenuArrangement.collectAsState().value.textId
        ) {
            dialogVisible.value = true
        }

        if (dialogVisible.value) {
            MenuDialog(
                titleText = stringResource(R.string.pref_entry_bottom_menu_arrangement),
                menuItems = listOf(
                    menuDialogItem(R.string.arrangement_horizontal_start) {
                        viewModel.bottomMenuArrangement.value = Arrangement.Start
                        true
                    },
                    menuDialogItem(R.string.arrangement_horizontal_end) {
                        viewModel.bottomMenuArrangement.value = Arrangement.End
                        true
                    }
                ),
                negativeButton = dialogButton(R.string.cancel) { dialogVisible.value = false },
                onDismissRequest = { dialogVisible.value = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    R.string.pref_entry_bottom_menu_items to item@ {
        val useBottomMenu by viewModel.useBottomMenu.collectAsState(initial = false)
        if (!useBottomMenu) return@item
        val bottomMenuItems by viewModel.bottomMenuItems.collectAsState()
        val itemsArrangement by viewModel.bottomMenuArrangement.collectAsState()
        BottomMenuPrefItem(
            items = bottomMenuItems,
            itemsArrangement = itemsArrangement,
            dialogProperties = viewModel.dialogProperties(),
            onComplete = { index, item -> viewModel.setBottomMenuItems(index, item) }
        )
    }
)

private fun MutableComposableList.actionsSection(viewModel: EntryViewModel) = add(
    0 to { Section(R.string.pref_entry_section_actions) },
    R.string.pref_entry_click_action to {
        ClickEntryActionSelector(viewModel, viewModel.clickEntryAction, R.string.pref_entry_click_action)
    },
    R.string.pref_entry_long_click_action to {
        ClickEntryActionSelector(viewModel, viewModel.longClickEntryAction, R.string.pref_entry_long_click_action)
    },
    R.string.pref_entry_double_click_action to {
        ClickEntryActionSelector(viewModel, viewModel.doubleClickEntryAction, R.string.pref_entry_double_click_action)
    },
    R.string.pref_entry_click_edge_action to {
        ClickEntryActionSelector(viewModel, viewModel.clickEntryEdgeAction, R.string.pref_entry_click_edge_action)
    },
    R.string.pref_entry_long_edge_click_action to {
        ClickEntryActionSelector(viewModel, viewModel.longClickEntryEdgeAction, R.string.pref_entry_long_edge_click_action)
    },
    R.string.pref_entry_double_edge_click_action to {
        ClickEntryActionSelector(viewModel, viewModel.doubleClickEntryEdgeAction, R.string.pref_entry_double_edge_click_action)
    }
)

/**
 * タップ時処理の設定項目+ダイアログ
 */
@Composable
private fun ClickEntryActionSelector(
    viewModel: EntryViewModel,
    flow: MutableStateFlow<ClickEntryAction>,
    @StringRes titleId: Int
) {
    var dialogVisible by remember { mutableStateOf(false) }
    PrefButton(
        mainTextId = titleId,
        subTextPrefixId = R.string.pref_current_value_prefix,
        subTextId = flow.collectAsState().value.textId
    ) {
        dialogVisible = true
    }

    if (dialogVisible) {
        MenuDialog(
            titleText = stringResource(titleId),
            menuItems = ClickEntryAction.entries.map {
                menuDialogItem(it.textId) { flow.value = it; true }
            },
            negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
            onDismissRequest = { dialogVisible = false },
            colors = themedCustomDialogColors(),
            properties = viewModel.dialogProperties()
        )
    }
}

private fun MutableComposableList.categorySection(
    viewModel: EntryViewModel,
    initialStateSelectorVisible: MutableState<Boolean>
) = add(
    0 to { Section(R.string.pref_entry_section_category) },
    R.string.pref_entry_initial_state to {
        val current by viewModel.initialState.collectAsState()
        var dialogVisible by remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_entry_initial_state,
            subTextPrefixId = R.string.pref_current_value_prefix,
            subTextId = current.category.textId
        ) {
            dialogVisible = true
        }

        if (dialogVisible) {
            MenuDialog(
                titleText = stringResource(R.string.pref_entry_initial_state),
                menuItems = Category.entries
                    .filter { it.willBeHome }
                    .map {
                        menuDialogItem(
                            textId = it.textId,
                            iconId = it.iconId
                        ) {
                            viewModel.initialState.value = EntryNavigationState(category = it)
                            true
                        }
                    },
                negativeButton = dialogButton(R.string.cancel) { dialogVisible = false },
                onDismissRequest = { dialogVisible = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    R.string.pref_entry_initial_tabs to {
        PrefButton(
            mainTextId = R.string.pref_entry_initial_tabs
        ) {
            initialStateSelectorVisible.value = true
        }
    }
)

private fun MutableComposableList.behaviorSection(viewModel: EntryViewModel) = add(
    0 to { Section(R.string.pref_section_behavior) },
    R.string.pref_entry_category_list_type to {
        val dialogVisible = remember { mutableStateOf(false) }

        PrefButton(
            mainTextId = R.string.pref_entry_category_list_type,
            subTextPrefixId = R.string.pref_current_value_prefix,
            subTextId = viewModel.categoryListType.collectAsState().value.textId
        ) {
            dialogVisible.value = true
        }

        if (dialogVisible.value) {
            MenuDialog(
                titleText = stringResource(R.string.pref_entry_category_list_type),
                menuItems = EntryCategoryListType.entries.map {
                    menuDialogItem(it.textId) { viewModel.categoryListType.value = it; true }
                },
                negativeButton = dialogButton(R.string.cancel) { dialogVisible.value = false },
                onDismissRequest = { dialogVisible.value = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    R.string.pref_entry_ignored_entries_visibility_in_mybookmarks to {
        PrefToggleButton(
            mainTextId = R.string.pref_entry_ignored_entries_visibility_in_mybookmarks,
            flow = viewModel.ignoredEntriesVisibilityInMyBookmarks
        )
    },
    R.string.pref_entry_filtering_enabled to {
        PrefToggleButton(
            mainTextId = R.string.pref_entry_filtering_enabled,
            flow = viewModel.filteringEntriesEnabled
        )
    }
)

private fun MutableComposableList.historySection(viewModel: EntryViewModel) = add(
    0 to { Section(R.string.pref_entry_section_history) },
    R.string.pref_entry_record_read_entries_enabled to {
        PrefToggleButton(
            mainTextId = R.string.pref_entry_record_read_entries_enabled,
            flow = viewModel.recordReadEntriesEnabled
        )
    }
)

private fun MutableComposableList.readLaterSection() = add(
    R.string.pref_entry_section_read_later to { Section(R.string.pref_entry_section_read_later) }
)

// ------ //

@Composable
private fun BottomMenuPrefItem(
    items: List<BottomMenuItem>,
    itemsArrangement: Arrangement.Horizontal,
    dialogProperties: DialogProperties,
    onComplete: ((Int, BottomMenuItem?)->Unit)? = null,
) {
    var dialogTargetIndex by remember { mutableIntStateOf(-1) }
    val scrollState = rememberScrollState()
    Column(
        Modifier.padding(
            vertical = PrefItemDefaults.verticalPadding,
            horizontal = PrefItemDefaults.horizontalPadding
        )
    ) {
        Text(
            text = stringResource(id = R.string.pref_entry_bottom_menu_items),
            color = CurrentTheme.onBackground
        )
        Spacer(Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = itemsArrangement,
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
                .background(CurrentTheme.bottomBarBackground)
                .horizontalScroll(scrollState)
        ) {
            CombinedIconButton(
                modifier = Modifier.padding(horizontal = 12.dp),
                onClick = { dialogTargetIndex = 0 }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "add an item",
                    tint = CurrentTheme.bottomBarOnBackground
                )
            }
            items.forEachIndexed { index, item ->
                CombinedIconButton(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onClick = { dialogTargetIndex = index + 1 }
                ) {
                    Icon(
                        painterResource(id = item.iconId),
                        contentDescription = stringResource(id = item.textId),
                        tint = CurrentTheme.bottomBarOnBackground
                    )
                }
            }
        }

        if (dialogTargetIndex > -1) {
            MenuDialog(
                titleText = stringResource(R.string.pref_entry_bottom_menu_items),
                menuItems = BottomMenuItem.entries.map { item ->
                    menuDialogItem(item.textId) {
                        onComplete?.invoke(dialogTargetIndex - 1, item)
                        true
                    }
                },
                negativeButton = dialogButton(R.string.cancel) { dialogTargetIndex = -1 },
                neutralButton = if (dialogTargetIndex == 0) null else dialogButton(R.string.delete) {
                    onComplete?.invoke(dialogTargetIndex - 1, null)
                    dialogTargetIndex = -1
                },
                onDismissRequest = { dialogTargetIndex = -1 },
                colors = themedCustomDialogColors(),
                properties = dialogProperties
            )
        }
    }

    BackHandler(dialogTargetIndex > -1) {
        dialogTargetIndex = -1
    }
}

// ------ //

@Composable
private fun InitialTabsSelector(
    viewModel: EntryViewModel,
    visible: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val categories = remember { Category.entries.filter { !it.singleTab } }
    val tabsMap by viewModel.initialTabs.collectAsState()
    val labelsMap = remember {
        buildMap {
            categories.map { put(it, it.tabs(context)) }
        }
    }

    val dialogTarget = remember { mutableStateOf<Category?>(null) }

    val visibility = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val duration = 200

    val onClose : ()->Unit = {
        coroutineScope.launch {
            visibility.targetState = false
            delay(duration.toLong())
            visible.value = false
        }
    }

    AnimatedVisibility(
        visibleState = visibility,
        enter = slideInHorizontally(
            animationSpec = tween(duration),
            initialOffsetX = { it }
        ),
        exit = slideOutHorizontally(
            animationSpec = tween(duration),
            targetOffsetX = { it }
        ),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(CurrentTheme.background)
        ) {
            Text(
                text = stringResource(R.string.pref_entry_initial_tabs),
                fontSize = 16.sp,
                color = CurrentTheme.primary,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
            )
            Box(Modifier.fillMaxSize()) {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    dialogTarget.value = it
                                }
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Image(
                                painterResource(it.iconId),
                                contentDescription = "category icon",
                                colorFilter = ColorFilter.tint(CurrentTheme.onBackground),
                                modifier = Modifier.height(with(LocalDensity.current) { 16.sp.toDp() })
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(it.textId),
                                fontSize = 14.sp,
                                color = CurrentTheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = labelsMap[it]!![tabsMap.getOrDefault(it, 0)],
                                fontSize = 12.sp,
                                color = CurrentTheme.grayTextColor
                            )
                        }
                        Divider(
                            color = CurrentTheme.listItemDivider,
                            thickness = 1.dp
                        )
                    }
                    emptyFooter()
                }
                VerticalScrollableIndicator(lazyListState = listState, gradientColor = CurrentTheme.background)

                FloatingActionButton(
                    onClick = onClose,
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            }
        }
    }

    dialogTarget.value?.let { category ->
        MenuDialog(
            titleText = stringResource(
                R.string.pref_entry_initial_tab_for_category,
                stringResource(category.textId)
            ),
            menuItems = labelsMap[category]!!.mapIndexed { index, value ->
                menuDialogItem(text = value) {
                    viewModel.initialTabs.value =
                        buildMap {
                            putAll(viewModel.initialTabs.value)
                            put(category, index)
                        }
                    true
                }
            },
            negativeButton = dialogButton(R.string.cancel) { dialogTarget.value = null },
            onDismissRequest = { dialogTarget.value = null },
            colors = themedCustomDialogColors(),
            properties = viewModel.dialogProperties()
        )
    }

    BackHandler(true) {
        onClose()
    }
}

// ------ //

@Preview
@Composable
private fun EntryPagePreview() {
    Box(Modifier.background(CurrentTheme.background)) {
        BasicPreferencesPage(
            contents = entryPageContents(
                viewModel = FakeEntryViewModel(),
                initialTabsSelectorVisible = remember { mutableStateOf(false) }
            )
        )
    }
}

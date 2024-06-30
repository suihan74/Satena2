package com.suihan74.satena2.scene.preferences.page.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.CombinedIconButton
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.model.theme.ThemePreset
import com.suihan74.satena2.scene.preferences.PrefItemDefaults
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors

/**
 * 「テーマ」ページ
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ThemePage(
    viewModel: ThemeViewModel,
    pagerState: PagerState,
    navigationBarInset: Dp
) {
    val menuTarget = remember { mutableStateOf<ThemePreset?>(null) }
    val targetPreset = remember { mutableStateOf<ThemePreset?>(null) }

    val currentTheme by viewModel.currentThemeFlow.collectAsState(initial = null)
    val presets by viewModel.allPresetsFlow.collectAsState(initial = emptyList())

    val onUpdateCurrentTheme = { preset: ThemePreset -> viewModel.updateCurrentTheme(preset) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect {
                targetPreset.value = null
            }
    }

    // プリセット名一覧
    PresetsList(
        currentTheme = currentTheme,
        presets = presets,
        onEdit = { targetPreset.value = it },
        onClick = onUpdateCurrentTheme,
        onLongClick = {  menuTarget.value = it }
    )

    if (menuTarget.value != null) {
        PresetItemMenuDialog(
            viewModel = viewModel,
            preset = menuTarget,
            onUpdateCurrentTheme = onUpdateCurrentTheme
        )
    }

    // 選択中のプリセットの編集画面
    if (targetPreset.value != null) {
        PresetEditionScreen(
            preset = targetPreset,
            viewModel = viewModel,
            navigationBarInset = navigationBarInset
        )
    }
}

// ------ //

/**
 * 既存のプリセット名一覧
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetsList(
    currentTheme: ThemePreset?,
    presets: List<ThemePreset>,
    onEdit: (ThemePreset)->Unit = {},
    onClick: (ThemePreset) -> Unit = {},
    onLongClick: (ThemePreset)->Unit = {}
) {
    LazyColumn(Modifier.fillMaxSize()) {
        if (currentTheme != null) {
            stickyHeader {
                Box(Modifier.background(CurrentTheme.background)) {
                    Section(R.string.pref_theme_section_current_theme)
                }
            }
            item {
                PresetItem(
                    preset = currentTheme,
                    onEdit = onEdit,
                    onClick = {},
                    onLongClick = onLongClick
                )
            }
        }

        stickyHeader {
            Box(Modifier.background(CurrentTheme.background)) {
                Section(R.string.pref_theme_section_presets)
            }
        }
        items(presets) { preset ->
            PresetItem(
                preset = preset,
                onEdit = onEdit,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
        emptyFooter()
    }
}

/**
 * プリセット名リスト項目
 */
@Composable
private fun PresetItem(
    preset: ThemePreset,
    onEdit: (ThemePreset)->Unit,
    onClick: (ThemePreset)->Unit,
    onLongClick: (ThemePreset)->Unit
) {
    val isCurrentTheme = preset.id == ThemePreset.CURRENT_THEME_ID
    val name =
        if (isCurrentTheme) stringResource(R.string.pref_theme_current_theme)
        else preset.name

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isCurrentTheme) onEdit(preset)
                    else onClick(preset)
                },
                onLongClick = { onLongClick(preset) }
            )
            .padding(
                vertical = PrefItemDefaults.listItemVerticalPadding + 4.dp,
                horizontal = PrefItemDefaults.listItemHorizontalPadding
            )
    ) {
        Text(
            text = name,
            color = CurrentTheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (!isCurrentTheme) {
            CombinedIconButton(
                onClick = { onEdit(preset) }
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_settings),
                    contentDescription = "edit a theme item",
                    tint = CurrentTheme.onBackground
                )
            }
        }
    }
}

/**
 * プリセット名に対する長押しメニュー
 */
@Composable
private fun PresetItemMenuDialog(
    viewModel: ThemeViewModel,
    preset: MutableState<ThemePreset?>,
    onUpdateCurrentTheme: (ThemePreset) -> Unit
) {
    val value = preset.value ?: return
    val name =
        if (value.isCurrentTheme) stringResource(R.string.pref_theme_current_theme)
        else value.name

    val menuItems = buildList {
        if (!value.isCurrentTheme) {
            add(menuDialogItem(R.string.pref_theme_update_current) { viewModel.updateCurrentTheme(value); true })
        }
        if (!value.isSystemDefault) {
            add(menuDialogItem(R.string.edit) { onUpdateCurrentTheme(value); true })
        }
        add(menuDialogItem(R.string.copy) { viewModel.copy(value); true })
        if (!value.isCurrentTheme && !value.isSystemDefault) {
            add(menuDialogItem(R.string.delete) { viewModel.delete(value); true })
        }
    }

    MenuDialog(
        titleText = name,
        menuItems = menuItems,
        negativeButton = dialogButton(R.string.close) { preset.value = null },
        onDismissRequest = { preset.value = null },
        colors = themedCustomDialogColors(),
        properties = viewModel.dialogProperties()
    )
}

// ------ //

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun ThemePagePreview() {
    ThemePage(
        viewModel = FakeThemeViewModel(),
        pagerState = rememberPagerState(initialPage = 0) { 2 },
        navigationBarInset = 0.dp
    )
}


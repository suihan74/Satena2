package com.suihan74.satena2.scene.preferences.page.theme

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.model.theme.ThemeColors
import com.suihan74.satena2.model.theme.ThemePreset
import com.suihan74.satena2.scene.preferences.PrefToggleButton
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedTextFieldColors
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 選択中プリセットの各種色設定編集画面
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetEditionScreen(
    preset: MutableState<ThemePreset?>,
    viewModel: ThemeViewModel,
    initialVisibility: Boolean = false
) {
    val value = preset.value ?: return
    val coroutineScope = rememberCoroutineScope()
    val visibility = remember {
        MutableTransitionState(initialVisibility).apply { targetState = true }
    }
    val duration = 200
    val editName = remember { mutableStateOf(value.name) }
    val editColors = remember { mutableStateOf(value.colors) }

    val onClose : ()->Unit = {
        coroutineScope.launch {
            visibility.targetState = false
            delay(duration.toLong())
            preset.value = null
        }
    }

    val editNameTarget = remember { mutableStateOf<ThemePreset?>(null) }
    val colorPickerData = remember { mutableStateOf<Pair<Color, (Color)-> ThemeColors>?>(null) }

    BackHandler(true, onClose)

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
        Box(
            Modifier
                .fillMaxSize()
                .background(CurrentTheme.background)
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                if (!value.isCurrentTheme) {
                    stickyHeader {
                        Box(Modifier.background(CurrentTheme.background)) {
                            Section(R.string.pref_theme_edition_section_title)
                        }
                    }
                    item {
                        SingleLineText(
                            text = editName.value,
                            fontSize = 16.sp,
                            color = CurrentTheme.onBackground,
                            modifier = Modifier
                                .let {
                                    if (value.isSystemDefault) it
                                    else it.clickable { editNameTarget.value = preset.value }
                                }
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 16.dp)
                        )
                    }
                }
                stickyHeader {
                    Box(Modifier.background(CurrentTheme.background)) {
                        Section(R.string.pref_theme_edition_section_prefs)
                    }
                }
                colorPrefItems(editColors, colorPickerData)
                emptyFooter()
            }

            Row(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = onClose,
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Bottom),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }

                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.update(
                                value.copy(
                                    name = editName.value,
                                    colors = editColors.value
                                )
                            )
                            if (value.isSystemDefault) preset.value = null
                        }
                    },
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(48.dp)
                ) {
                    val iconResource =
                        if (value.isSystemDefault) R.drawable.ic_save_as
                        else R.drawable.ic_save
                    Icon(
                        painterResource(iconResource),
                        contentDescription = "save"
                    )
                }
            }
        }
    }

    if (editNameTarget.value != null) {
        PrefColorNameDialog(
            preset = editNameTarget,
            editName = editName,
            onCheckInsertable = { viewModel.checkInsertable(it) },
            dialogProperties = viewModel.dialogProperties()
        )
    }

    if (colorPickerData.value != null) {
        ColorPickerDialog(
            colorPickerData = colorPickerData,
            editItem = editColors,
            viewModel = viewModel
        )
    }
}

/**
 * 設定項目リスト
 */
private fun LazyListScope.colorPrefItems(
    editItem: MutableState<ThemeColors>,
    colorPickerData: MutableState<Pair<Color, (Color) -> ThemeColors>?>
) {
    colorPrefItem(
        textId = R.string.pref_theme_background,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.background },
        updater = { copy(background = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_on_background,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.onBackground },
        updater = { copy(onBackground = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_primary,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.primary },
        updater = { copy(primary = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_on_primary,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.onPrimary },
        updater = { copy(onPrimary = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_ripple,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.ripple },
        updater = { copy(ripple = it) }
    )
    item {
        PrefToggleButton(
            isOn = editItem.value.isTitleBarPrimary,
            mainText = stringResource(R.string.pref_theme_is_title_primary),
        ) {
            editItem.value = editItem.value.copy(isTitleBarPrimary = it)
        }
    }
    colorPrefItem(
        textId = R.string.pref_theme_tab_background,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.tabBackground },
        updater = { copy(tabBackground = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_tab_selected_text,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.tabSelectedColor },
        updater = { copy(tabSelectedColor = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_tab_unselected_text,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.tabUnSelectedColor },
        updater = { copy(tabUnSelectedColor = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_gray_text_color,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.grayTextColor },
        updater = { copy(grayTextColor = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_list_item_divider,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.listItemDivider },
        updater = { copy(listItemDivider = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_entry_comment_background,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.entryCommentBackground },
        updater = { copy(entryCommentBackground = it) }
    )
    colorPrefItem(
        textId = R.string.pref_theme_entry_comment_on_background,
        state = editItem,
        colorPickerData = colorPickerData,
        selector = { it.entryCommentOnBackground },
        updater = { copy(entryCommentOnBackground = it) }
    )
}

// ------ //

/**
 * 色設定項目
 */
private fun LazyListScope.colorPrefItem(
    @StringRes textId: Int,
    state: MutableState<ThemeColors>,
    colorPickerData: MutableState<Pair<Color, (Color) -> ThemeColors>?>,
    selector: (ThemeColors)-> Color,
    updater: ThemeColors.(Color)-> ThemeColors
) {
    item {
        val color = selector(state.value)
        ColorPrefItem(stringResource(textId), color) {
            colorPickerData.value = color to { c -> updater(state.value, c) }
        }
    }
}

// ------ //

@Composable
private fun PrefColorNameDialog(
    preset: MutableState<ThemePreset?>,
    editName: MutableState<String>,
    onCheckInsertable: suspend (ThemePreset)->Boolean,
    dialogProperties: DialogProperties
) {
    val item = preset.value ?: return
    val textFieldValue = rememberMutableTextFieldValue(text = item.name)
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = focusKeyboardRequester()

    CustomDialog(
        titleText = stringResource(R.string.pref_theme_preset_title_edition_dialog_title),
        positiveButton = dialogButton(R.string.register) {
            coroutineScope.launch {
                if (onCheckInsertable(item.copy(name = textFieldValue.value.text))) {
                    editName.value = textFieldValue.value.text
                    preset.value = null
                }
            }
        },
        negativeButton = dialogButton(R.string.cancel) { preset.value = null },
        onDismissRequest = { preset.value = null },
        colors = themedCustomDialogColors(),
        properties = dialogProperties
    ) {
        TextField(
            value = textFieldValue.value,
            onValueChange = { textFieldValue.value = it },
            placeholder = { Text(stringResource(R.string.pref_theme_edition_section_title)) },
            singleLine = true,
            maxLines = 1,
            colors = themedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

// ------ //

@Preview
@Composable
private fun PresetScreenPreview() {
    val vm = FakeThemeViewModel()
    val preset = vm.allPresetsFlow.collectAsState().value.first()
    val targetItem = remember { mutableStateOf<ThemePreset?>(preset) }
    PresetEditionScreen(targetItem, vm, initialVisibility = true)
}

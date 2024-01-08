package com.suihan74.satena2.scene.preferences.page.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.dialog.DialogButton
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.NumberPickerDialog
import com.suihan74.satena2.compose.dialog.SliderDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.PrefToggleButton
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.scene.preferences.page.BasicPreferencesPage
import com.suihan74.satena2.scene.preferences.page.MutableComposableList
import com.suihan74.satena2.scene.preferences.page.buildComposableList
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedSliderDialogColors
import com.suihan74.satena2.utility.VibratorCompat
import com.suihan74.satena2.utility.extension.add
import com.suihan74.satena2.utility.extension.textId

/**
 * 「基本」ページのコンテンツ
 */
@Composable
fun generalPageContents(viewModel: GeneralViewModel) = buildComposableList {
    behaviorSection(viewModel)
    noticeSection(viewModel)
    updateNoticeSection(viewModel)
    intentSection(viewModel)
    dialogSection(viewModel)
    backupSection(viewModel)
    cacheSection(viewModel)
}

private fun MutableComposableList.behaviorSection(viewModel: GeneralViewModel) = add(
    { Section(R.string.pref_section_behavior) },
    {
        PrefToggleButton(
            flow = viewModel.useSystemTimeZone,
            mainTextId = R.string.pref_general_behavior_use_system_timezone
        )
    },
    {
        val dialogVisible = remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_general_behavior_drawer_alignment,
            subTextPrefixId = R.string.pref_current_value_prefix,
            subTextId = viewModel.drawerAlignment.collectAsState().value.textId,
        ) {
            dialogVisible.value = true
        }

        if (dialogVisible.value) {
            MenuDialog(
                titleText = stringResource(R.string.pref_general_behavior_drawer_alignment),
                menuItems = listOf(
                    menuDialogItem(R.string.alignment_start) {
                        viewModel.drawerAlignment.value = Alignment.Start
                        true
                    },
                    menuDialogItem(R.string.alignment_end) {
                        viewModel.drawerAlignment.value = Alignment.End
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
    {
        val dialogVisible = remember { mutableStateOf(false) }
        val duration by viewModel.longClickVibrationDuration.collectAsState()
        val textDisable = stringResource(R.string.disable)
        val currentValueText = remember(duration) {
            if (duration == 0L) textDisable
            else "${duration}ms"
        }
        PrefButton(
            mainText = stringResource(R.string.pref_general_behavior_long_click_vibration_duration),
            subTextPrefix = stringResource(R.string.pref_current_value_prefix),
            subText = currentValueText
        ) {
            dialogVisible.value = true
        }
        
        if (dialogVisible.value) {
            val context = LocalContext.current
            SliderDialog(
                titleText = stringResource(R.string.pref_general_behavior_long_click_vibration_duration),
                min = 0f,
                max = 250f,
                initialValue = duration.toFloat(),
                properties = viewModel.dialogProperties(),
                colors = themedSliderDialogColors(),
                neutralButton = DialogButton(stringResource(R.string.default_value)) {
                    val value = 40L
                    viewModel.longClickVibrationDuration.value = value
                    VibratorCompat.vibrateOneShot(context, value)
                    dialogVisible.value = false
                },
                onValueChanged = {
                    VibratorCompat.vibrateOneShot(context, it.toLong())
                },
                onCompleted = {
                    viewModel.longClickVibrationDuration.value = it.toLong()
                    true
                },
                onDismissRequest = { dialogVisible.value = false })
        }
    }
)

/**
 * 通知セクション
 */
private fun MutableComposableList.noticeSection(viewModel: GeneralViewModel) = add(
    { Section(R.string.pref_general_section_notice) },
    {
        PrefToggleButton(
            flow = viewModel.backgroundCheckingNoticesEnabled,
            mainTextId = R.string.pref_general_notice_background_checking_enabled
        ) {
            viewModel.launchRequestNotificationPermission.value = true
        }
    },
    {
        val dialogVisible = remember { mutableStateOf(false) }

        val minutes = viewModel.checkingNoticesIntervals.collectAsState().value
        PrefButton(
            mainText = stringResource(R.string.pref_general_notice_checking_intervals),
            subTextPrefix = stringResource(R.string.pref_current_value_prefix),
            subText = "${minutes}分"
        ) {
            dialogVisible.value = true
        }

        if (dialogVisible.value) {
            val pickerValue = remember { mutableIntStateOf(minutes) }
            NumberPickerDialog(
                titleText = stringResource(R.string.pref_general_notice_checking_intervals),
                range = 15..120,
                current = pickerValue,
                positiveButton =
                    dialogButton(R.string.register) {
                        viewModel.checkingNoticesIntervals.value = pickerValue.intValue
                        dialogVisible.value = false
                    },
                negativeButton =
                    dialogButton(R.string.cancel) {
                        dialogVisible.value = false
                    },
                onDismissRequest = { dialogVisible.value = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    {
        PrefToggleButton(
            flow = viewModel.noticeSameComment,
            mainTextId = R.string.pref_general_notice_same_comment
        )
    },
    {
        PrefToggleButton(
            flow = viewModel.updateReadFlagOnNotification,
            mainTextId = R.string.pref_general_update_read_flag_on_notification
        )
    }
)

/**
 * アップデート通知セクション
 */
private fun MutableComposableList.updateNoticeSection(viewModel: GeneralViewModel) = add(
    { Section(R.string.pref_general_section_update_notification) },
    {
        PrefToggleButton(
            flow = viewModel.showingReleaseNotesAfterUpdateEnabled,
            mainTextId = R.string.pref_general_update_showing_release_notes
        )
    },
    {
        PrefButton(
            mainText = stringResource(R.string.pref_general_update_notice_targets),
            subTextPrefix = stringResource(R.string.pref_current_value_prefix),
            subText = ""
        ) {
            // TODO
        }
    },
    {
        PrefToggleButton(
            flow = viewModel.noticeUpdateOnceIgnored,
            mainTextId = R.string.pref_general_update_notice_once_ignored
        )
    }
)

/**
 * インテントセクション
 */
private fun MutableComposableList.intentSection(viewModel: GeneralViewModel) = add(
    { Section(R.string.pref_general_section_intent) },
    {
        PrefToggleButton(
            flow = viewModel.useIntentChooser,
            mainTextId = R.string.pref_general_intent_use_chooser
        )
    }
)

/**
 * ダイアログセクション
 */
private fun MutableComposableList.dialogSection(viewModel: GeneralViewModel) = add(
    { Section(R.string.pref_general_section_dialog) },
    {
        PrefToggleButton(
            flow = viewModel.closeDialogByTouchingOutside,
            mainTextId = R.string.pref_general_dialog_close_by_touching_outside
        )
    }
)

/**
 * バックアップセクション
 */
private fun MutableComposableList.backupSection(viewModel: GeneralViewModel) = add(
    { Section(R.string.pref_general_section_backup) },
    {
        PrefButton(mainTextId = R.string.pref_general_save_settings) {
            viewModel.launchAppDataExport()
        }
    },
    {
        PrefButton(mainTextId = R.string.pref_general_load_settings) {
            // TODO
        }
    }
)

/**
 * キャッシュセクション
 */
private fun MutableComposableList.cacheSection(viewModel: GeneralViewModel) = add(
    { Section(R.string.pref_general_section_cache) },
    {
        PrefButton(
            mainText = stringResource(R.string.pref_general_clear_image_cache_span),
            subTextPrefix = stringResource(R.string.pref_current_value_prefix),
            subText = "" // todo
        ) {
            // TODO
        }
    },
    {
        PrefButton(
            mainTextId = R.string.pref_general_clear_image_cache,
            mainTextColor = Color.Red
        ) {
            // TODO
        }
    }
)

// ------ //

@Preview
@Composable
private fun GeneralPagePreview() {
    Box(Modifier.background(CurrentTheme.background)) {
        BasicPreferencesPage(
            contents = generalPageContents(FakeGeneralViewModel())
        )
    }
}

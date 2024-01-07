package com.suihan74.satena2.scene.preferences.page.info.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.Dimension
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AdditionalLoadableLazyColumn
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.CustomDialogDefaults
import com.suihan74.satena2.compose.dialog.DialogButton
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.scene.preferences.page.info.FakeInformationViewModel
import com.suihan74.satena2.scene.preferences.page.info.InformationViewModel
import com.suihan74.satena2.scene.preferences.page.info.ReleaseNote
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import kotlinx.coroutines.launch

/**
 * リリースノート表示ダイアログ
 */
@Composable
fun ReleaseNotesDialog(
    viewModel: InformationViewModel,
    onDismissRequest: ()->Unit,
    properties: DialogProperties
) {
    val version = remember { mutableStateOf(2) }
    when (version.value) {
        1 -> ReleaseNotesDialogImpl(
            items = viewModel.releaseNotesV1.collectAsState().value,
            title = "更新履歴(v1)",
            neutralButton = dialogButton("v2") { version.value = 2 },
            onDismissRequest = onDismissRequest,
            properties = properties,
            launchedEffect = { viewModel.loadReleaseNotesV1() },
            onAppearLastItem = { viewModel.loadReleaseNotesV1() }
        )

        2 -> ReleaseNotesDialogImpl(
            items = viewModel.releaseNotes.collectAsState().value,
            title = "更新履歴",
            neutralButton = dialogButton("v1") { version.value = 1 },
            onDismissRequest = onDismissRequest,
            properties = properties,
            launchedEffect = { viewModel.loadReleaseNotes() },
            onAppearLastItem = { viewModel.loadReleaseNotes() }
        )
    }
}

@Composable
private fun ReleaseNotesDialogImpl(
    items: List<ReleaseNote>,
    title: String,
    neutralButton: DialogButton,
    onDismissRequest: ()->Unit,
    properties: DialogProperties,
    launchedEffect: suspend ()->Unit,
    onAppearLastItem: suspend ()->Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val state = rememberLazyListState()

    LaunchedEffect(title) {
        launchedEffect()
    }

    CustomDialog(
        titleText = title,
        positiveButton = dialogButton(R.string.close, onDismissRequest),
        neutralButton = neutralButton,
        onDismissRequest = onDismissRequest,
        colors = themedCustomDialogColors(),
        properties = properties
    ) {
        AdditionalLoadableLazyColumn(
            items = items,
            state = state,
            onAppearLastItem = {
                coroutineScope.launch {
                    onAppearLastItem()
                }
            },
            modifier = Modifier
                .verticalScrollbar(
                    state = state,
                    color = CurrentTheme.primary
                )
        ) {
            if (it.isSeparator) {
                ReleaseNoteSeparator()
            }
            else {
                ReleaseNoteItem(it)
            }
        }
    }
}

// ------ //

/**
 * v2用の履歴項目
 */
@Composable
private fun ReleaseNoteItem(item: ReleaseNote) {
    Column(
        Modifier
            .clickable {}
            .fillMaxWidth()
            .padding(
                vertical = 12.dp,
                horizontal = CustomDialogDefaults.DEFAULT_TITLE_HORIZONTAL_PADDING
            )
    ) {
        Row {
            SingleLineText(
                text = "[ version ${item.version} ]",
                fontSize = 16.sp,
                color = CurrentTheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (item.timestamp.isNotBlank()) {
                SingleLineText(
                    text = item.timestamp,
                    fontSize = 12.sp,
                    color = CurrentTheme.grayTextColor,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp)
                )
            }
        }
        Text(
            text = item.body,
            fontSize = 13.sp,
            color = CurrentTheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * マイナーバージョンごとに区切る分割線
 */
@Composable
private fun ReleaseNoteSeparator() {
    Divider(
        color = CurrentTheme.grayTextColor,
        thickness = 2.dp,
        modifier = Modifier.padding(vertical = 3.dp)
    )
}

// ------ //

@Preview
@Composable
private fun ReleaseNotesDialogPreview() {
    ReleaseNotesDialog(
        viewModel = FakeInformationViewModel(),
        onDismissRequest = {},
        properties = DialogProperties()
    )
}

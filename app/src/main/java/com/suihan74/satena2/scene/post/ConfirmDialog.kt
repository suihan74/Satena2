package com.suihan74.satena2.scene.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.CustomDialogDefaults
import com.suihan74.satena2.compose.dialog.DialogButton
import com.suihan74.satena2.scene.bookmarks.BookmarkItem
import com.suihan74.satena2.scene.bookmarks.DisplayBookmark
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors

/**
 * 投稿前の確認ダイアログ
 */
@Composable
fun ConfirmDialog(
    preview: DisplayBookmark,
    visible: MutableState<Boolean>,
    properties: DialogProperties = DialogProperties(),
    onPositive: ()->Unit = {}
) {
    CustomDialog(
        titleText = stringResource(R.string.confirm),
        onDismissRequest = { visible.value = false },
        positiveButton = DialogButton(stringResource(R.string.ok)) { onPositive(); visible.value = false },
        negativeButton = DialogButton(stringResource(R.string.cancel)) { visible.value = false },
        colors = themedCustomDialogColors(),
        properties = properties
    ) {
        Column {
            Text(
                text = stringResource(R.string.post_confirm_message),
                color = CurrentTheme.onBackground,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    horizontal = CustomDialogDefaults.DEFAULT_TITLE_HORIZONTAL_PADDING
                )
            )
            Spacer(Modifier.height(2.dp))
            BookmarkItem(
                item = preview,
                clickable = false
            )
        }
    }
}

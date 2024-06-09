package com.suihan74.satena2.scene.preferences.page.userLabel

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedTextFieldColors
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import kotlinx.coroutines.launch

/**
 * 新しいラベルを作成する・既存のラベル名を変更するダイアログ
 */
@Composable
fun UserLabelNameEditionDialog(
    label: Label,
    onRegistration: suspend (Label)->Boolean,
    onDismiss: () -> Unit,
    dialogProperties: DialogProperties
) {
    val textFieldValue = rememberMutableTextFieldValue(text = label.name)
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = focusKeyboardRequester()

    CustomDialog(
        titleText = stringResource(R.string.user_label_dialog_edit_label_name_title),
        positiveButton = dialogButton(R.string.register) {
            coroutineScope.launch {
                if (onRegistration(label.copy(name = textFieldValue.value.text))) {
                    onDismiss()
                }
            }
        },
        negativeButton = dialogButton(R.string.cancel) { onDismiss() },
        onDismissRequest = { onDismiss() },
        colors = themedCustomDialogColors(),
        properties = dialogProperties
    ) {
        TextField(
            value = textFieldValue.value,
            onValueChange = { textFieldValue.value = it },
            placeholder = { Text(
                stringResource(R.string.user_label_dialog_edit_label_name_placeholder)
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

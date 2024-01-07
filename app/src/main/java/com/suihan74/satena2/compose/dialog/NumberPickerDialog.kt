package com.suihan74.satena2.compose.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.chargemap.compose.numberpicker.NumberPicker

/**
 * `NumberPicker`表示用ダイアログ
 */
@Composable
fun NumberPickerDialog(
    range: Iterable<Int>,
    current: MutableState<Int>,
    onDismissRequest: ()->Unit,
    modifier: Modifier = Modifier,
    titleText: String? = null,
    positiveButton: DialogButton? = null,
    negativeButton: DialogButton? = null,
    neutralButton: DialogButton? = null,
    colors: CustomDialogColors = CustomDialogDefaults.colors(),
    properties: DialogProperties = DialogProperties(),
    widthRatio: Float = CustomDialogDefaults.DEFAULT_WIDTH_RATIO
) {
    CustomDialog(
        modifier,
        titleText,
        positiveButton,
        negativeButton,
        neutralButton,
        colors,
        onDismissRequest,
        properties,
        widthRatio
    ) {
        Surface(
            contentColor = colors.textColor,
            color = Color.Transparent,
            elevation = 0.dp
        ) {
            NumberPicker(
                value = current.value,
                onValueChange = { current.value = it },
                range = range,
                dividersColor = colors.positiveButtonTextColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

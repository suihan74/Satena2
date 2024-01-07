package com.suihan74.satena2.scene.preferences.page.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.model.theme.ThemeColors
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.utility.extension.rgbCode

/**
 * カラーピッカーダイアログ
 */
@Composable
fun ColorPickerDialog(
    colorPickerData: MutableState<Pair<Color, (Color) -> ThemeColors>?>,
    editItem: MutableState<ThemeColors>,
    viewModel: ThemeViewModel
) {
    if (colorPickerData.value == null) return
    val data = colorPickerData.value!!
    val selectedColor = remember { mutableStateOf(data.first) }
    CustomDialog(
        titleText = stringResource(R.string.pref_theme_color_picker_dialog_title),
        positiveButton = dialogButton(R.string.register) {
            editItem.value = data.second(selectedColor.value)
            colorPickerData.value = null
        },
        negativeButton = dialogButton(R.string.cancel) {
            colorPickerData.value = null
        },
        onDismissRequest = { colorPickerData.value = null },
        colors = themedCustomDialogColors(),
        properties = viewModel.dialogProperties()
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            ClassicColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp),
                color = HsvColor.from(color = data.first), showAlphaBar = false
            ) {
                selectedColor.value = it.toColor()
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(18.dp)
                        .background(selectedColor.value)
                ) {
                }
                Text(
                    text = selectedColor.value.rgbCode(),
                    fontSize = 14.sp,
                    color = CurrentTheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = "R: ${String.format("%3d", (255 * selectedColor.value.red).toInt())}",
                    fontSize = 14.sp,
                    color = CurrentTheme.onBackground,
                    modifier = Modifier.padding(start = 24.dp)
                )
                Text(
                    text = "G: ${String.format("%3d", (255 * selectedColor.value.green).toInt())}",
                    fontSize = 14.sp,
                    color = CurrentTheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Text(
                    text = "B: ${String.format("%3d", (255 * selectedColor.value.blue).toInt())}",
                    fontSize = 14.sp,
                    color = CurrentTheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

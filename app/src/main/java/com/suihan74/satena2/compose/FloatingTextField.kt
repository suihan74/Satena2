package com.suihan74.satena2.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue

data class FloatingTextFieldColors(
    val background : Color,
    val foreground : Color
) {
    companion object {
        val Default
            @Composable get() = FloatingTextFieldColors(
                background = MaterialTheme.colors.primary,
                foreground = MaterialTheme.colors.onPrimary
            )
    }
}

/**
 * テキストボックスを伴うFAB
 */
@Composable
fun FloatingTextField(
    isOpen: MutableState<Boolean>,
    textFieldValue: MutableState<TextFieldValue>,
    modifier: Modifier = Modifier,
    colors: FloatingTextFieldColors = FloatingTextFieldColors.Default,
    placeholderText: String = "",
    imageVector: ImageVector = Icons.Filled.Search,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Box(
        modifier = modifier.padding(8.dp)
    ) {
        if (isOpen.value) {
            Card(
                backgroundColor = colors.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                elevation = FloatingActionButtonDefaults.elevation()
                    .elevation(interactionSource).value
            ) {
                val focusRequester = focusKeyboardRequester()
                BasicTextField(
                    value = textFieldValue.value,
                    onValueChange = { textFieldValue.value = it },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 52.dp)
                        .focusRequester(focusRequester),
                ) {
                    val text = textFieldValue.value.text.ifEmpty { placeholderText }
                    val textColor =
                        if (textFieldValue.value.text.isEmpty()) colors.foreground.copy(alpha = .5f)
                        else colors.foreground

                    Row {
                        SingleLineText(
                            text = text,
                            color = textColor,
                            fontSize = 14.sp,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            backgroundColor = colors.background,
            contentColor = colors.foreground,
            modifier = modifier
                .size(40.dp)
                .align(Alignment.CenterEnd),
            onClick = { isOpen.value = !isOpen.value }
        ) {
            Icon(imageVector, contentDescription = "FAB icon")
        }
    }
}

@Preview
@Composable
private fun FloatingSearchAreaPreview() {
    val isOpen = remember { mutableStateOf(true) }
    val textFieldValue = rememberMutableTextFieldValue()
    FloatingTextField(isOpen, textFieldValue)
}

package com.suihan74.satena2.compose.dialog

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * スライダーダイアログ
 */
@Composable
fun SliderDialog(
    titleText: String,
    min: Float,
    max: Float,
    initialValue: Float,
    onDismissRequest: ()->Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    steps: Int = 0,
    neutralButton: DialogButton? = null,
    colors: SliderDialogColors = SliderDialogDefaults.colors(),
    properties: DialogProperties = DialogProperties(),
    widthRatio: Float = CustomDialogDefaults.DEFAULT_WIDTH_RATIO,
    onValueChanged: (Float)->Unit = {},
    onCompleted: (Float)->Boolean = { true }
) {
    val sliderValue = remember { mutableFloatStateOf(initialValue) }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(Unit) {
        interactionSource.interactions
            .onEach {
                when (it) {
                    is DragInteraction.Stop, is PressInteraction.Release -> {
                        onValueChanged(sliderValue.floatValue)
                    }
                    else -> {}
                }
            }
            .launchIn(this)
    }

    CustomDialog(
        titleText = titleText,
        negativeButton = DialogButton(stringResource(R.string.cancel)) { onDismissRequest() },
        positiveButton = DialogButton(stringResource(R.string.ok)) {
            onCompleted(sliderValue.floatValue)
            onDismissRequest()
        },
        neutralButton = neutralButton,
        modifier = modifier,
        properties = properties,
        widthRatio = widthRatio,
        colors = colors.customDialogColors,
        onDismissRequest = onDismissRequest
    ) {
        Column {
            description?.let {
                Text(
                    text = it,
                    color = colors.textColor,
                    fontSize = 14.sp
                )
            }
            Slider(
                value = sliderValue.floatValue,
                valueRange = min .. max,
                steps = steps,
                onValueChange = {
                    sliderValue.floatValue = it
                },
                interactionSource = interactionSource,
                colors = colors.sliderColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CustomDialogDefaults.DEFAULT_TITLE_HORIZONTAL_PADDING),
            )
        }
    }
}

// ------ //

data class SliderDialogColors(
    val backgroundColor : Color,
    val textColor : Color,
    val positiveButtonTextColor : Color,
    val negativeButtonTextColor : Color,
    val neutralButtonTextColor : Color,
    val thumbColor : Color,
    val trackColor : Color
) {
    val customDialogColors : CustomDialogColors by lazy {
        CustomDialogColors(
            backgroundColor = backgroundColor,
            textColor = textColor,
            positiveButtonTextColor = positiveButtonTextColor,
            negativeButtonTextColor = negativeButtonTextColor,
            neutralButtonTextColor = neutralButtonTextColor
        )
    }

    @Composable
    fun sliderColors() : SliderColors =
        SliderDefaults.colors(
            thumbColor = thumbColor,
            activeTrackColor = trackColor,
            inactiveTrackColor = trackColor.copy(alpha = SliderDefaults.InactiveTrackAlpha),
        )
}

// ------ //

object SliderDialogDefaults {
    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colors.background,
        textColor: Color = MaterialTheme.colors.onBackground,
        positiveButtonTextColor: Color = MaterialTheme.colors.primary,
        negativeButtonTextColor: Color = MaterialTheme.colors.primary,
        neutralButtonTextColor: Color = MaterialTheme.colors.primary,
        thumbColor: Color = MaterialTheme.colors.primary,
        trackColor: Color = thumbColor
    ) = remember(
        backgroundColor,
        textColor,
        positiveButtonTextColor,
        negativeButtonTextColor,
        neutralButtonTextColor,
        thumbColor,
        trackColor
    ) {
        SliderDialogColors(
            backgroundColor,
            textColor,
            positiveButtonTextColor,
            negativeButtonTextColor,
            neutralButtonTextColor,
            thumbColor,
            trackColor
        )
    }
}

// ------ //

@Preview
@Composable
private fun SliderDialogPreview() {
    Box(Modifier.fillMaxSize()) {
        SliderDialog(
            titleText = "Slider Dialog",
            min = 0f,
            max = 100f,
            initialValue = 50f,
            onDismissRequest = {},
            colors = SliderDialogDefaults.colors(
                thumbColor = CurrentTheme.primary
            )
        )
    }
}

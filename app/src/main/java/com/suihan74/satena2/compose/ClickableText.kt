package com.suihan74.satena2.compose

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.suihan74.satena2.utility.VibratorCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * [ClickableText]の素テキスト部分のクリックを後ろのビューに透過するための連携用データクラス
 */
data class ClickableTextState(
    val interactionSource: MutableInteractionSource,
    val state: MutableState<ClickState>
)

/**
 * [ClickableTextState]を作成
 */
@Composable
fun rememberClickableTextState(
    onLongClick: ()->Unit = {},
    onDoubleClick: ()->Unit = {},
    onClick: ()->Unit
) : ClickableTextState {
    val rememberState = remember {
        ClickableTextState(
            interactionSource = MutableInteractionSource(),
            state = mutableStateOf(ClickState.Empty)
        )
    }

    LaunchedEffect(rememberState) {
        snapshotFlow { rememberState.state.value }
            .collect {
                when (it) {
                    ClickState.Tap -> { onClick() }
                    ClickState.LongPress -> { onLongClick() }
                    ClickState.DoubleTap -> { onDoubleClick() }
                    else -> {}
                }
                rememberState.state.value = ClickState.Empty
            }
    }

    return rememberState
}

/**
 * [ClickableText]でどのクリックが検知されたか
 */
enum class ClickState {
    Empty,
    Tap,
    LongPress,
    DoubleTap
}

/**
 * [AnnotatedString]部分へのタップ・ロング・複数タップに対応した[BasicText]
 */
@Composable
fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    state: ClickableTextState = rememberClickableTextState {},
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onLongClick: (Int) -> Boolean = { false },
    onDoubleClick: (Int) -> Unit = {},
    onClick: (Int) -> Boolean
) {
    val context = LocalContext.current
    val longClickVibrationDuration = LocalLongClickVibrationDuration.current
    val coroutineScope = rememberCoroutineScope()
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressConsumed = remember { mutableStateOf<Boolean?>(null) }
    val pressIndicator = Modifier.pointerInput(state, onClick, onLongClick, onDoubleClick) {
        detectTapGestures(
            onLongPress = { pos ->
                pressConsumed.value = layoutResult.value?.let { layoutResult ->
                    onLongClick(layoutResult.getOffsetForPosition(pos))
                } ?: false
                if (pressConsumed.value != true) {
                    state.state.value = ClickState.LongPress
                }
                VibratorCompat.vibrateOneShot(context, longClickVibrationDuration)
            },
            onDoubleTap = { pos ->
                layoutResult.value?.let { layoutResult ->
                    onDoubleClick(layoutResult.getOffsetForPosition(pos))
                }
            },
            onTap = { offset ->
                pressConsumed.value = layoutResult.value?.let { layoutResult ->
                    onClick(layoutResult.getOffsetForPosition(offset)).also {
                        Log.i("ClickableText", "onClick ${if (it) "consumed" else "not consumed"}")
                    }
                } ?: false
                if (pressConsumed.value != true) {
                    state.state.value = ClickState.Tap
                }
            },
            onPress = { offset ->
                pressConsumed.value = null
                PressInteraction.Press(offset).let { press ->
                    coroutineScope.launch {
                        var canceled = false
                        state.interactionSource.emit(press)
                        if (tryAwaitRelease()) {
                            while (pressConsumed.value == null) { delay(1) }
                            if (pressConsumed.value != true) {
                                state.interactionSource.emit(PressInteraction.Release(press))
                            }
                            else { canceled = true }
                        }
                        else { canceled = true }

                        if (canceled) {
                            state.interactionSource.emit(PressInteraction.Cancel(press))
                        }
                        pressConsumed.value = null
                    }
                }
            }
        )
    }

    BasicText(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        }
    )
}

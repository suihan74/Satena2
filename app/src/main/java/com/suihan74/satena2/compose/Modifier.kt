package com.suihan74.satena2.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import com.suihan74.satena2.utility.VibratorCompat

/**
 * ロングタップ時の振動時間
 */
val LocalLongClickVibrationDuration = compositionLocalOf { 40L }

/**
 * ロングタップ時に振動するようにした[combinedClickable]
 */
@SuppressLint("ModifierFactoryUnreferencedReceiver")
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.localCombinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit
) : Modifier = composed {
    val context = LocalContext.current
    val longClickVibrationDuration = LocalLongClickVibrationDuration.current
    val mutableInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    Modifier.combinedClickable(
        enabled = enabled,
        onClick = onClick,
        onLongClick = {
            onLongClick?.let { action ->
                VibratorCompat.vibrateOneShot(context, longClickVibrationDuration)
                action()
            }
        },
        onDoubleClick = onDoubleClick,
        onClickLabel = onClickLabel,
        onLongClickLabel = onLongClickLabel,
        role = role,
        interactionSource = mutableInteractionSource,
        indication = indication ?: LocalIndication.current
    )
}

fun Modifier.combinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit
) : Modifier = localCombinedClickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    onLongClickLabel = onLongClickLabel,
    role = role,
    interactionSource = interactionSource,
    indication = indication,
    onClick = onClick,
    onLongClick = onLongClick,
    onDoubleClick = onDoubleClick
)

/**
 * ロングタップ時に振動するようにした[combinedClickable]
 *
 * ビュー内部の[ClickableText]とクリック状態を共有するために使用
 */
fun Modifier.combinedClickable(
    clickableTextState: ClickableTextState,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    role: Role? = null,
    indication: Indication? = null
) : Modifier = localCombinedClickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    onLongClickLabel = onLongClickLabel,
    role = role,
    interactionSource = clickableTextState.interactionSource,
    indication = indication,
    onClick = { clickableTextState.state.value = ClickState.Tap },
    onLongClick = { clickableTextState.state.value = ClickState.LongPress },
    onDoubleClick = { clickableTextState.state.value = ClickState.DoubleTap }
)

// ------ //

/**
 * クリックを奪う領域をつくる
 */
@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.clickGuard() : Modifier = composed {
    Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {}
}

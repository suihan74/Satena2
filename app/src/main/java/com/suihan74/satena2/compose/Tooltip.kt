@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.suihan74.satena2.compose

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

private val TooltipElevation = 16.dp
private val TooltipPadding = 12.dp
private const val InTransitionDuration = 64
private const val OutTransitionDuration = 240
private const val TooltipTimeout = 2_000L - OutTransitionDuration

@Composable
fun Tooltip(
    expanded: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    timeoutMillis: Long = TooltipTimeout,
    backgroundColor: Color = Color.Black,
    contentColor: Color = Color.White,
    backgroundAlpha: Float = .85f,
    offset: DpOffset = DpOffset.Zero,
    properties: PopupProperties = PopupProperties(focusable = false),
    content: @Composable ColumnScope.()->Unit = {}
) {
    val expandedStates = remember { MutableTransitionState(false) }.apply { targetState = expanded.value }

    if (expandedStates.currentState || expandedStates.targetState) {
        if (expandedStates.isIdle) {
            LaunchedEffect(timeoutMillis, expanded) {
                delay(timeoutMillis)
                expanded.value = false
            }
        }

        Popup(
            onDismissRequest = { expanded.value = false },
            popupPositionProvider = TooltipPositionProvider(offset, LocalDensity.current),
            properties = properties
        ) {
            Box(Modifier.padding(TooltipElevation)) {
                TooltipContent(
                    expandedStates,
                    backgroundColor,
                    contentColor,
                    backgroundAlpha,
                    modifier,
                    content
                )
            }
        }
    }
}

@Composable
private fun TooltipContent(
    expandedStates: MutableTransitionState<Boolean>,
    backgroundColor: Color,
    contentColor: Color,
    backgroundAlpha: Float,
    modifier: Modifier,
    content: @Composable ColumnScope.()->Unit
) {
    val transition = updateTransition(expandedStates, "Tooltip")
    val alpha by transition.animateFloat(
        label = "alpha",
        transitionSpec = {
            tween(durationMillis =
                if (false isTransitioningTo true) InTransitionDuration
                else OutTransitionDuration
            )
        }
    ) { if (it) 1f else 0f }

    Card(
        backgroundColor = backgroundColor.copy(alpha = backgroundAlpha),
        contentColor = contentColor,
        modifier = Modifier.alpha(alpha),
        elevation = TooltipElevation
    ) {
        val p = TooltipPadding
        Column(
            modifier = modifier
                .padding(
                    top = p * .5f,
                    bottom = p * .7f,
                    start = p,
                    end = p
                )
                .width(IntrinsicSize.Max),
            content = content
        )
    }
}

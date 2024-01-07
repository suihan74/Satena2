package com.suihan74.satena2.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val DEFAULT_LONG_PRESS_DURATION = 500L

/**
 * ロングタップできるようにした`Tab`
 */
@Composable
fun Tab(
    selected: Boolean,
    onClick: ()->Unit,
    onLongClick: ()->Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    longClickDuration: Long = DEFAULT_LONG_PRESS_DURATION,
    content: @Composable ColumnScope.()->Unit
) {
    LaunchedEffect(Unit) {
        val mutex = Mutex()
        var job: Job? = null
        interactionSource.interactions.collect {
            mutex.withLock {
                when (it) {
                    is PressInteraction.Press -> {
                        job?.cancel()
                        job = launch {
                            delay(longClickDuration)
                            onLongClick()
                            job = null
                        }
                    }

                    is PressInteraction.Release -> {
                        if (job != null) {
                            job?.cancel()
                            onClick()
                        }
                        job = null
                    }

                    else -> {
                        job?.cancel()
                        job = null
                    }
                }
            }
        }
    }

    androidx.compose.material.Tab(
        selected = selected,
        onClick = {},
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor,
        content = content
    )
}

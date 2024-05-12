package com.suihan74.satena2.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.DrawerDefaults
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.VibratorCompat

/**
 * 右から左に向かって開くことができる`ModalDrawer`
 *
 */
@Composable
fun OrientatedModalDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    drawerElevation: Dp = DrawerDefaults.Elevation,
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    scrimColor: Color = DrawerDefaults.scrimColor,
    isRtl: Boolean = false,
    width: Dp = 300.dp,
    content: @Composable () -> Unit
) {
    val contentDirection = LocalLayoutDirection.current
    val drawerDirection =
        if (isRtl) LayoutDirection.Rtl
        else LayoutDirection.Ltr
    val cursorInteractionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    LaunchedEffect(cursorInteractionSource) {
        cursorInteractionSource.interactions
            .collect {
                when (it) {
                    is PressInteraction.Press -> {
                        VibratorCompat.vibrateOneShot(context, duration = 5L)
                    }
                }
        }
    }

    Box(
        modifier
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides drawerDirection) {
            ModalDrawer(
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                        Column(
                            Modifier
                                .width(width)
                                .fillMaxHeight()
                        ) {
                            drawerContent()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                drawerState = drawerState,
                gesturesEnabled = gesturesEnabled,
                drawerShape = DrawerShape(width, isRtl),
                drawerElevation = drawerElevation,
                drawerBackgroundColor = drawerBackgroundColor,
                drawerContentColor = drawerContentColor,
                scrimColor = scrimColor,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                    Box(Modifier.fillMaxSize()) {
                        // 画面全体のドラッグに反応しないようにし、矢印部分だけに反応するようにする
                        Box(
                            Modifier
                                .background(Color.Transparent)
                                .fillMaxSize()
                                .draggable(
                                    interactionSource = null,//remember { MutableInteractionSource() },
                                    state = remember { DraggableState {} },
                                    orientation = Orientation.Horizontal
                                )
                        ) {
                            content()
                        }
                        // ドラッグ可能位置を表す矢印
                        Box(
                            Modifier
                                .size(
                                    width = 32.dp,
                                    height = 64.dp
                                )
                                .align(
                                    if (isRtl) Alignment.CenterEnd
                                    else Alignment.CenterStart
                                )
                                .let {
                                    if (gesturesEnabled) {
                                        it.clickable(
                                            interactionSource = cursorInteractionSource,
                                            indication = null
                                        ) {}
                                        .systemGestureExclusion()
                                    }
                                    else it
                                }
                        ) {
                            Icon(
                                painterResource(
                                    if (isRtl) R.drawable.ic_keyboard_arrow_left
                                    else R.drawable.ic_keyboard_arrow_right
                                ),
                                contentDescription = "drawer navigation cursor",
                                tint = CurrentTheme.primary.copy(alpha = .5f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------ //

/**
 * ドロワの横幅を変更するためのShape
 */
@Stable
class DrawerShape(
    private val width : Dp,
    private val isRtl: Boolean
) : Shape {
    private fun rectRtr(size: Size, density: Density) = Rect(
        left = size.width - width.value * density.density,
        right = size.width,
        top = 0f,
        bottom = size.height
    )

    private fun rectLtr(size: Size, density: Density) = Rect(
        left = 0f,
        right = width.value * density.density,
        top = 0f,
        bottom = size.height
    )

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        RectangleShape
        return Outline.Rectangle(
            if (isRtl) rectRtr(size, density) else rectLtr(size, density)
        )
    }

    override fun toString(): String = "DrawerShape"
}

// ------ //

@Preview
@Composable
private fun OrientatedModalDrawerPreview() {
    Box(Modifier.fillMaxSize()) {
        OrientatedModalDrawer(
            drawerContent = { Box(Modifier.fillMaxHeight()) {} },
            isRtl = true
        ) {
            Box(Modifier.fillMaxSize()) {
            }
        }
    }
}

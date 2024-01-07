@file:UseSerializers(ColorSerializer::class)

package com.suihan74.satena2.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.suihan74.satena2.model.theme.default.DefaultThemePresetLight
import com.suihan74.satena2.serializer.ColorSerializer
import com.suihan74.satena2.utility.extension.grayScale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.UseSerializers

private val darkColorPalette = darkColors(
    primary = Color(0x21, 0x96, 0xF3),
    onPrimary = Color(0xff, 0xff, 0xff),
    background = Color(0x38, 0x38, 0x39),
    onBackground = Color(0xfe, 0xfe, 0xfe),
    onSurface = Color(0x0, 0x0, 0x0)
)

private val lightColorPalette = lightColors(
    primary = Color(0x21, 0x96, 0xF3),
    onPrimary = Color(0xff, 0xff, 0xff),
    background = Color(0xee, 0xee, 0xee),
    onBackground = Color(0x0a, 0x0a, 0x0a),
    onSurface = Color(0x0, 0x0, 0x0)
)

val CurrentThemePreset = MutableStateFlow(DefaultThemePresetLight.colors)

val CurrentTheme
    @Composable get() = CurrentThemePreset.collectAsState().value

@Composable
fun Satena2Theme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors =
        if (darkTheme) darkColorPalette
        else lightColorPalette

    // タイトルバーの色
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(
        color = CurrentTheme.titleBarBackground,
        darkIcons = CurrentTheme.titleBarOnBackground.grayScale() < 0.5f
    )

    // 選択テキストのハイライトカラー
    val textSelectionColors = TextSelectionColors(
        handleColor = CurrentTheme.primary,
        backgroundColor = CurrentTheme.primary.copy(alpha = 0.4f)
    )

    // クリック時の背景色
    val rippleColor = CurrentTheme.ripple
    // clickable用
    val indicationColor = rememberRipple(color = CurrentTheme.ripple, bounded = true)
    // アイコンボタン用
    val buttonRippleColor = remember {
        object : RippleTheme {
            @Composable
            override fun defaultColor() : Color =
                RippleTheme.defaultRippleColor(
                    contentColor = rippleColor,
                    lightTheme = false
                )
            @Composable
            override fun rippleAlpha() : RippleAlpha =
                RippleAlpha(
                    draggedAlpha = .5f,
                    focusedAlpha = .5f,
                    hoveredAlpha = .5f,
                    pressedAlpha = .5f
                )
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
    ) {
        CompositionLocalProvider(
            LocalIndication provides indicationColor,
            LocalRippleTheme provides buttonRippleColor,
            LocalTextSelectionColors provides textSelectionColors,
            content = content
        )
    }
}

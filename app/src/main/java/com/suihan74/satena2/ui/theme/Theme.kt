@file:UseSerializers(ColorSerializer::class)

package com.suihan74.satena2.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.suihan74.satena2.model.theme.ThemePreset
import com.suihan74.satena2.model.theme.default.DefaultThemePresetLight
import com.suihan74.satena2.model.theme.default.TemporaryTheme
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

val CurrentThemePreset = MutableStateFlow(DefaultThemePresetLight)

/** 現在使用中のテーマ */
val LocalTheme = compositionLocalOf { CurrentThemePreset.value }

val CurrentTheme
    @Composable get() = LocalTheme.current.colors

/** 現在使用中のテーマ */
//val CurrentTheme
//    @Composable get() = CurrentThemePreset.collectAsState().value

@Composable
fun Satena2Theme(
    theme: ThemePreset,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val t =
        if (theme == TemporaryTheme) LocalTheme.current
        else theme

    val colors =
        if (darkTheme) darkColorPalette
        else lightColorPalette

    // タイトルバーの色
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(
        color = t.colors.titleBarBackground,
        darkIcons = t.colors.titleBarOnBackground.grayScale() < 0.5f
    )

    // 選択テキストのハイライトカラー
    val textSelectionColors = TextSelectionColors(
        handleColor = t.colors.primary,
        backgroundColor = t.colors.primary.copy(alpha = 0.4f)
    )

    // クリック時の背景色
    val rippleColor = t.colors.ripple
    // clickable用
    val indicationColor = rememberRipple(color = t.colors.ripple, bounded = true)
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
            LocalTheme provides t,
            content = content
        )
    }
}

@Composable
fun ComponentActivity.Satena2ThemeFullScreen(
    theme: ThemePreset,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    Satena2Theme(
        theme = theme,
        darkTheme = darkTheme
    ) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb(),
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = CurrentTheme.titleBarBackground.luminance() > .5f
        )
        content()
    }
}

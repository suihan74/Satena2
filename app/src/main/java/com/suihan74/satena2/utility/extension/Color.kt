@file:Suppress("unused")

package com.suihan74.satena2.utility.extension

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * `#RRGGBB`の文字列を取得する
 */
fun Color.rgbCode() : String = String.format("#%06X", toArgb().and(0xffffff))

/**
 * `#AARRGGBB`の文字列を取得する
 */
fun Color.argbCode() : String = String.format("#%08X", toArgb())

/**
 * グレースケール化
 */
fun Color.grayScale() : Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

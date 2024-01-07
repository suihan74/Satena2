package com.suihan74.satena2.utility.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 日時をシステムのタイムゾーンで表示する
 */
val LocalUseSystemTimeZone = compositionLocalOf { false }

@Composable
fun Instant.zonedString(pattern: String) : String =
    if (LocalUseSystemTimeZone.current) {
        DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.ofInstant(this, ZoneId.systemDefault()))
    }
    else {
        DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.ofInstant(this, ZoneId.of("Asia/Tokyo")))
    }

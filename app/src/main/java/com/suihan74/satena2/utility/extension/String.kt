package com.suihan74.satena2.utility.extension

/**
 * URI文字列からスキーム部分を除去する
 */
fun String.trimScheme() : String =
    "^[a-zA-Z-]+://(.+)$".toRegex(RegexOption.IGNORE_CASE).matchEntire(this)?.groupValues?.get(1) ?: this

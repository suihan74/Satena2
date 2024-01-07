package com.suihan74.satena2.utility.extension

import android.annotation.SuppressLint
import androidx.compose.ui.text.AnnotatedString

/**
 * [Regex]にマッチした部分にスタイルを適用する
 */
@SuppressLint("ComposableNaming")
fun AnnotatedString.Builder.appendRegex(
    regex: Regex,
    input: String,
    matchedAction: AnnotatedString.Builder.(MatchResult)->Unit
) {
    var matcher = regex.find(input, 0)
    if (matcher == null) {
        appendPlainText(input)
    }
    else {
        var last = input
        while (matcher != null) {
            appendPlainText(last.substring(0 until matcher.range.first))
            matchedAction(matcher)
            last = last.substring(matcher.range.last + 1)
            matcher = regex.find(last)
        }
        if (last.isNotEmpty()) { appendPlainText(last) }
    }
}

private fun AnnotatedString.Builder.appendPlainText(text: String) {
    pushStringAnnotation("plain", "")
    append(text)
    pop()
}

package com.suihan74.satena2.utility.extension

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import com.suihan74.satena2.R

/**
 * `Arrangement`に対応する文字列ID
 */
val Arrangement.Horizontal.textId : Int @StringRes get() =
    when (this) {
        Arrangement.Start -> R.string.arrangement_horizontal_start
        Arrangement.End -> R.string.arrangement_horizontal_end
        else -> throw IllegalStateException()
    }

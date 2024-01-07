package com.suihan74.satena2.utility.extension

import android.view.Gravity
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import com.suihan74.satena2.R

/**
 * `Alignment`に対応する文字列ID
 */
val Alignment.Horizontal.textId : Int @StringRes get() =
    when (this) {
        Alignment.Start -> R.string.alignment_start
        Alignment.End -> R.string.alignment_end
        Alignment.CenterHorizontally -> R.string.alignment_center
        else -> throw IllegalStateException()
    }

/**
 * `Alignment`に対応する文字列ID
 */
val Alignment.Vertical.textId : Int @StringRes get() =
    when (this) {
        Alignment.Top -> R.string.alignment_top
        Alignment.Bottom -> R.string.alignment_bottom
        Alignment.CenterVertically -> R.string.alignment_center
        else -> throw IllegalStateException()
    }

val Alignment.Horizontal.gravity : Int get() =
    when (this) {
        Alignment.Start -> Gravity.START
        Alignment.End -> Gravity.END
        Alignment.CenterHorizontally -> Gravity.CENTER_HORIZONTAL
        else -> throw IllegalStateException()
    }

val Alignment.Vertical.gravity : Int get() =
    when (this) {
        Alignment.Top -> Gravity.TOP
        Alignment.Bottom -> Gravity.BOTTOM
        Alignment.CenterVertically -> Gravity.CENTER_VERTICAL
        else -> throw IllegalStateException()
    }

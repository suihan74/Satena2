package com.suihan74.satena2.utility.extension

import androidx.constraintlayout.compose.Visibility

/**
 * 真値の場合のみ実行
 */
inline fun Boolean.onTrue(action: ()->Unit) : Boolean {
    if (this) {
        action()
    }
    return this
}

/**
 * 偽値の場合のみ実行
 */
inline fun Boolean.onFalse(action: ()->Unit) : Boolean {
    if (this) {
        action()
    }
    return this
}

/**
 * 真偽値を`Visibility`に変換
 */
fun Boolean.toVisibility(falseVisibility: Visibility = Visibility.Gone) : Visibility =
    if (this) Visibility.Visible
    else falseVisibility

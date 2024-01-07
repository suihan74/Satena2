package com.suihan74.satena2.utility.extension

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 必ずUIスレッドでトーストを処理するようにするための拡張関数
 */
suspend fun Context.showToast(
    text: String,
    duration: Int = Toast.LENGTH_SHORT
) = withContext(Dispatchers.Main) {
    Toast.makeText(this@showToast, text, duration).show()
}

/**
 * 必ずUIスレッドでトーストを処理するようにするための拡張関数
 */
suspend fun Context.showToast(
    @StringRes textId: Int,
    duration: Int = Toast.LENGTH_SHORT
) = withContext(Dispatchers.Main) {
    Toast.makeText(this@showToast, textId, duration).show()
}

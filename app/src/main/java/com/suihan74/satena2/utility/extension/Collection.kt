package com.suihan74.satena2.utility.extension

/**
 * コレクションがnullでも空ではないときのみ処理を実行
 */
@SinceKotlin("1.3")
inline fun <T> T?.ifNotNullOrEmpty(action: (T)->Unit): T? where T : Collection<*> {
    if (!isNullOrEmpty()) {
        action(this)
    }
    return this
}

/**
 * コレクションが空ではないときのみ処理を実行
 */
@SinceKotlin("1.3")
inline fun <T> T.ifNotEmpty(action: (T)->Unit): T where T : Collection<*> {
    if (isNotEmpty()) {
        action(this)
    }
    return this
}

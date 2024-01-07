@file:Suppress("unused")

package com.suihan74.satena2.utility.extension

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** 値が一致するときだけactionを実行する */
inline fun <T> T.on(expected: T, action: (T)->Unit) : T {
    if (this == expected) {
        action.invoke(this)
    }
    return this
}

/** 値が一致するときだけactionを実行する */
inline fun <T> T.on(
    crossinline comparator: (actual: T)->Boolean,
    crossinline action: (T)->Unit
) : T {
    if (comparator(this)) {
        action.invoke(this)
    }
    return this
}

// ------ //

/** 値が一致しないときだけactionを実行する */
inline fun <T> T.onNot(expected: T, action: (T)->Unit) : T {
    if (this != expected) {
        action.invoke(this)
    }
    return this
}

/** 値が一致しないときだけactionを実行する */
inline fun <T> T.onNot(
    crossinline comparator: (actual: T)->Boolean,
    crossinline action: (T)->Unit
) : T {
    if (!comparator(this)) {
        action.invoke(this)
    }
    return this
}

// ------ //

/** nullではないときだけactionを実行する */
inline fun <T> T.onNotNull(action: (T)->Unit) : T {
    if (this != null) {
        action.invoke(this)
    }
    return this
}

// ------ //

/** 与えられた型のときだけ実行するlet */
@Suppress("UNCHECKED_CAST")
@OptIn(kotlin.contracts.ExperimentalContracts::class)
inline fun <reified T, R> Any?.letAs(block: (T) -> R) : R? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return if (this is T) block(this as T)
    else null
}

/** 与えられた型のときだけ実行するalso */
@OptIn(kotlin.contracts.ExperimentalContracts::class)
inline fun <reified T> Any?.alsoAs(block: (T) -> Unit) : T? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return if (this is T) {
        block(this as T)
        this
    }
    else null
}

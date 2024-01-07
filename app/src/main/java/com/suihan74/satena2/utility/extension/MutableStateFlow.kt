package com.suihan74.satena2.utility.extension

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 実体がMutableのときだけ実行するalso
 */
@OptIn(kotlin.contracts.ExperimentalContracts::class)
inline fun <reified T> StateFlow<T>?.alsoAsMutable(
    crossinline block: (MutableStateFlow<T>) -> Unit
) : StateFlow<T>? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return if (this is MutableStateFlow<T>) {
        block(this)
        this
    }
    else null
}

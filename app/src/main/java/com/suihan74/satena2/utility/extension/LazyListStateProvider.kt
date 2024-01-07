package com.suihan74.satena2.utility.extension

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable

interface LazyListStateProvider {
    /**
     * リストのスクロール状態
     */
    @Composable
    fun lazyListState() : LazyListState
}

// ------ //

class LazyListStateProviderImpl : LazyListStateProvider {
    @Composable
    override fun lazyListState() : LazyListState {
        return _lazyListState ?: rememberLazyListState().also { _lazyListState = it }
    }
    private var _lazyListState : LazyListState? = null
}

package com.suihan74.satena2.scene.preferences.page

import androidx.compose.runtime.Composable

typealias ComposablePrefItem = Pair<Int, @Composable ()->Unit>
typealias MutableComposableList = MutableList<ComposablePrefItem>

inline fun buildComposableList(builderAction: MutableComposableList.()->Unit) = buildList(builderAction)

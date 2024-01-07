package com.suihan74.satena2.scene.preferences.page

import androidx.compose.runtime.Composable

typealias MutableComposableList = MutableList<@Composable ()->Unit>

inline fun buildComposableList(builderAction: MutableComposableList.()->Unit) = buildList<@Composable ()->Unit>(builderAction)

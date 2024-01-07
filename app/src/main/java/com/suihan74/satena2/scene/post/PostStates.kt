package com.suihan74.satena2.scene.post

import kotlinx.serialization.Serializable

/**
 * 連携の選択状態
 */
@Serializable
data class PostStates(
    val mastodon : Boolean = false,
    val misskey : Boolean = false,
    val twitter : Boolean = false,
    val facebook : Boolean = false,
    val sharing : Boolean = false,
    val private : Boolean = false
)

package com.suihan74.satena2.scene.bookmarks

import kotlinx.serialization.Serializable

/**
 * 「カスタム」タブの表示対象設定
 */
@Serializable
data class CustomTabSetting(
    /** 有効なユーザーラベルのID */
    val enableLabelIds: Set<Long> = emptySet(),

    /** ラベルがついていないユーザーを表示する */
    val areNoLabelsShown: Boolean = true,

    /** 無言ブクマを表示する */
    val areNoCommentsShown: Boolean = false,

    /** ミュート対象を表示する */
    val areIgnoresShown: Boolean = false,

    /** URLだけのブコメを表示する */
    val areUrlOnlyCommentsShown: Boolean = false
)

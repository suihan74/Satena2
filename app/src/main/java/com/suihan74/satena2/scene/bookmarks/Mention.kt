package com.suihan74.satena2.scene.bookmarks

import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.star.StarCount
import com.suihan74.hatena.model.star.StarsEntry

/**
 * 詳細画面での表示項目
 */
data class Mention(
    /**
     * ユーザー名
     */
    val user: String,

    /**
     * スター数
     */
    val stars: List<StarCount>,

    /**
     * ブコメ
     */
    val bookmark: Bookmark? = null,

    /**
     * 非表示ユーザーである
     */
    val ignoredUser : Boolean = false,
    /**
     * フィルタに引っかかった
     */
    val filtered : Boolean = false
)

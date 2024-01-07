package com.suihan74.satena2.scene.bookmarks

import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.bookmark.TweetsAndClicks
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.model.userLabel.UserAndLabels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 表示用のブクマ情報
 */
data class DisplayBookmark(
    /**
     * ブクマ
     */
    val bookmark : Bookmark,
    /**
     * ブクマに紐づいたツイートのURL・クリック数
     */
    val tweetsAndClicks : TweetsAndClicks? = null,
    /**
     * ブクマに対するブクマ数
     */
    val bookmarksCount : Int = 0,
    /**
     * ブクマにつけられたスター
     */
    val starsEntry : StateFlow<StarsEntry> = MutableStateFlow(StarsEntry(url = "", stars = emptyList())),
    /**
     * 非表示ユーザーである
     */
    val ignoredUser : Boolean = false,
    /**
     * フィルタに引っかかった
     */
    val filtered : Boolean = false,
    /**
     * このブクマが言及している他のブクマ
     */
    val mentions : List<DisplayBookmark> = emptyList(),
    /**
     * ユーザーラベル
     */
    val labels : Flow<UserAndLabels?> = MutableStateFlow(null)
)

package com.suihan74.satena2.scene.bookmarks

import com.suihan74.satena2.R
import com.suihan74.satena2.model.TextIdContainer

/**
 * ブコメ中のURLをクリックしてリンク先をブラウザで開く挙動を起こすために必要な行動
 */
enum class OpenCommentLinkTrigger(override val textId: Int) : TextIdContainer {
    /** 動作させない */
    Disabled(R.string.pref_bookmark_on_comment_link_click_trigger_disable),

    /** シングルクリックで動作 */
    SingleClick(R.string.pref_bookmark_on_comment_link_click_trigger_single_click),

    /** ダブルクリックで動作 */
    DoubleClick(R.string.pref_bookmark_on_comment_link_click_trigger_double_click),
}

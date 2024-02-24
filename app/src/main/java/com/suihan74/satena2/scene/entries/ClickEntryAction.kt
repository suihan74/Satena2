package com.suihan74.satena2.scene.entries

import androidx.annotation.StringRes
import com.suihan74.satena2.R
import com.suihan74.satena2.model.TextIdContainer

/**
 * エントリ項目をクリックしたときの処理
 */
enum class ClickEntryAction(
    val id: Int,
    @StringRes override val textId: Int
) : TextIdContainer {
    /** 何もしない */
    NOTHING(4, R.string.entry_action_nothing),

    /** ブックマークを見る */
    SHOW_COMMENTS(0, R.string.entry_action_show_comments),

    /** ページを開く */
    SHOW_PAGE(1, R.string.entry_action_show_page),

    /** ページを外部アプリで開く */
    SHOW_PAGE_IN_OTHER_APP(2, R.string.entry_action_show_page_in_browser),

    /** 共有 */
    SHARE(5, R.string.entry_action_share),

    /** メニューダイアログ */
    SHOW_MENU(3, R.string.entry_action_show_menu);
}

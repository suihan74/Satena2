package com.suihan74.satena2.scene.bookmarks

import androidx.annotation.StringRes
import com.suihan74.satena2.R
import com.suihan74.satena2.model.TextIdContainer

/**
 * ブクマ画面の各タブ
 */
enum class BookmarksTab(
    @StringRes override val textId: Int
) : TextIdContainer {
    DIGEST(R.string.bookmark_tab_digest),

    RECENT(R.string.bookmark_tab_recent),

    ALL(R.string.bookmark_tab_all),

    CUSTOM(R.string.bookmark_tab_custom)
}

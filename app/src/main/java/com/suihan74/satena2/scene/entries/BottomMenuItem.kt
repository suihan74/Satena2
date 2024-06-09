package com.suihan74.satena2.scene.entries

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.suihan74.satena2.R

/** ボトムバーに表示する項目 */
enum class BottomMenuItem(
    val id: Int,
    @DrawableRes val iconId: Int,
    @StringRes val textId: Int,
    val requireSignedIn: Boolean = false,
    val longClickable: Boolean = false
) {
    SCROLL_TO_TOP(0,
        R.drawable.ic_vertical_align_top,
        R.string.scroll_to_top
    ),

    SCROLL_TO_BOTTOM(1,
        R.drawable.ic_vertical_align_bottom,
        R.string.scroll_to_bottom
    ),

    NOTICES(2,
        R.drawable.ic_notifications,
        R.string.notices,
        requireSignedIn = true
    ),

    MY_BOOKMARKS(3,
        R.drawable.ic_mybookmarks,
        R.string.my_bookmarks,
        requireSignedIn = true
    ),

    INNER_BROWSER(4,
        R.drawable.ic_language,
        R.string.inner_browser,
        longClickable = true
    ),

    EXCLUDED_ENTRIES(5,
        R.drawable.ic_visibility_off,
        R.string.excluded_entries,
        longClickable = true
    ),

    SEARCH(6,
        R.drawable.ic_category_search,
        R.string.category_search
    ),

    PREFERENCES(7,
        R.drawable.ic_settings,
        R.string.preferences
    ),

    OPEN_OFFICIAL_TOP(8,
        R.drawable.ic_category_social,
        R.string.open_official_top_desc
    ),

    OPEN_OFFICIAL_HATENA(9,
        R.drawable.ic_category_social,
        R.string.open_official_hatena_desc
    ),

    OPEN_ANONYMOUS_DIARY(10,
        R.drawable.ic_category_social,
        R.string.open_anonymous_diary_desc
    ),

    HOME(11,
        R.drawable.ic_category_all,
        R.string.home_category_desc
    ),

    CATEGORIES(12,
        R.drawable.ic_category,
        R.string.categories
    )
}

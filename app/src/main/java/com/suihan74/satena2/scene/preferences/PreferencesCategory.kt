package com.suihan74.satena2.scene.preferences

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.suihan74.satena2.R
import com.suihan74.satena2.model.IconIdContainer
import com.suihan74.satena2.model.TextIdContainer

enum class PreferencesCategory(
    @StringRes override val textId: Int,
    @DrawableRes override val iconId: Int
) : TextIdContainer, IconIdContainer {
    Info(R.string.pref_information, R.drawable.ic_info),
    Accounts(R.string.pref_account, R.drawable.ic_person),
    General(R.string.pref_general, R.drawable.ic_settings),
    Theme(R.string.pref_theme, R.drawable.ic_palette),
    Entry(R.string.pref_entry, R.drawable.ic_view_list),
    Bookmark(R.string.pref_bookmark, R.drawable.ic_bookmarks),
    Browser(R.string.pref_browser, R.drawable.ic_language),
    FavoriteSites(R.string.pref_favorite_site, R.drawable.ic_star),
    NgWords(R.string.pref_ng_word, R.drawable.ic_visibility_off),
    NgUsers(R.string.pref_ng_users, R.drawable.ic_voice_over_off),
    Followings(R.string.pref_following, R.drawable.ic_people),
    UserLabels(R.string.pref_user_label, R.drawable.ic_tag)
}

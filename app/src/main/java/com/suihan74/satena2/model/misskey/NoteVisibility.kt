package com.suihan74.satena2.model.misskey

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.suihan74.misskey.entity.Visibility
import com.suihan74.satena2.R
import com.suihan74.satena2.model.IconIdContainer
import com.suihan74.satena2.model.TextIdContainer

/**
 * Misskey投稿の公開範囲
 */
enum class NoteVisibility(
    val value : Visibility,
    @StringRes override val textId: Int,
    @DrawableRes override val iconId: Int
) : TextIdContainer, IconIdContainer {

    Public(
        value = Visibility.Public,
        textId = R.string.misskey_status_visibility_public,
        iconId = R.drawable.ic_category_general
    ),

    Home(
        value = Visibility.Home,
        textId = R.string.misskey_status_visibility_home,
        iconId = R.drawable.ic_category_all
    ),

    Followers(
        value = Visibility.Followers,
        textId = R.string.misskey_status_visibility_followers,
        iconId = R.drawable.ic_lock
    ),

    Specified(
        value = Visibility.Specified,
        textId = R.string.misskey_status_visibility_specified,
        iconId = R.drawable.ic_email
    )
}

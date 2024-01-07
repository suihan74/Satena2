package com.suihan74.satena2.model.mastodon

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.suihan74.satena2.R
import com.suihan74.satena2.model.IconIdContainer
import com.suihan74.satena2.model.TextIdContainer
import com.sys1yagi.mastodon4j.api.entity.Status

/**
 * Mastodon投稿の公開範囲
 */
enum class TootVisibility(
    val value: Status.Visibility,
    @StringRes override val textId: Int,
    @DrawableRes override val iconId: Int
) : TextIdContainer, IconIdContainer {

    Public(
        Status.Visibility.Public,
        R.string.mastodon_status_visibility_public,
        R.drawable.ic_category_social
    ),

    Unlisted(
        Status.Visibility.Unlisted,
        R.string.mastodon_status_visibility_unlisted,
        R.drawable.ic_unlock
    ),

    Private(
        Status.Visibility.Private,
        R.string.mastodon_status_visibility_private,
        R.drawable.ic_lock
    ),

    Direct(
        Status.Visibility.Direct,
        R.string.mastodon_status_visibility_direct,
        R.drawable.ic_email
    ),
}

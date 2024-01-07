package com.suihan74.satena2.scene.post

import com.suihan74.hatena.model.entry.Entry
import kotlinx.serialization.Serializable

@Serializable
data class EditData(
    /** エントリ */
    val entry : Entry?,

    /** コメント */
    val comment : String,

    /** プライベート投稿する */
    val private : Boolean,

    /** Twitterに投稿する */
    val postTwitter : Boolean,

    /** Mastodonに投稿する */
    val postMastodon : Boolean,

    /** Facebookに投稿する */
    val postFacebook : Boolean,

    /** Misskeyに投稿する */
    val postMisskey : Boolean,

    /** 投稿後に共有する */
    val sharing : Boolean,

    /** Mastodonの警告文 */
    val mastodonSpoilerText : String
)

package com.suihan74.satena2.model.mastodon

import java.io.Serializable

/**
 * Mastodon: インスタンスとアクセストークンを保存するためのデータ
 */
data class MastodonAccessToken(
    /**
     * インスタンス
     */
    val instanceName: String = "",
    /**
     * アクセストークン
     */
    val accessToken: String = ""
) : Serializable

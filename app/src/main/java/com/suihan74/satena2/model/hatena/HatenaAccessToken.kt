package com.suihan74.satena2.model.hatena

import java.io.Serializable

/**
 * Hatena: RKを保存するためのデータ
 */
data class HatenaAccessToken(
    /**
     * APIコールに必要なRK
     */
    val rk: String = ""
) : Serializable

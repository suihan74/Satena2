package com.suihan74.satena2.serializer

import com.suihan74.satena2.model.mastodon.MastodonAccessToken
import com.suihan74.satena2.utility.CryptUtility
import com.suihan74.satena2.utility.ObjectSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 暗号/復号 を施す[MastodonAccessToken]用のシリアライザ
 */
object MastodonAccessTokenSerializer : AccessTokenSerializer(), KSerializer<MastodonAccessToken> {
    override val descriptor = PrimitiveSerialDescriptor("MastodonAccessToken", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MastodonAccessToken) {
        val objectSerializer = ObjectSerializer<MastodonAccessToken>()
        val serializedValue = objectSerializer.serialize(value)

        val encryptedValue = CryptUtility.encrypt(serializedValue, key)
        val encryptedSerializer = ObjectSerializer<CryptUtility.EncryptedData>()
        val serializedEncryptedValue = encryptedSerializer.serialize(encryptedValue)

        encoder.encodeString(serializedEncryptedValue)
    }

    override fun deserialize(decoder: Decoder): MastodonAccessToken {
        val encryptedSerializer = ObjectSerializer<CryptUtility.EncryptedData>()
        val serializedEncryptedValue = decoder.decodeString()
        val encryptedValue = encryptedSerializer.deserialize(serializedEncryptedValue)

        val value = CryptUtility.decrypt(encryptedValue, key)
        val objectSerializer = ObjectSerializer<MastodonAccessToken>()

        return objectSerializer.deserialize(value)
    }
}

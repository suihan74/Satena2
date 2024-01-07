package com.suihan74.satena2.serializer

import com.suihan74.satena2.model.hatena.HatenaAccessToken
import com.suihan74.satena2.utility.CryptUtility
import com.suihan74.satena2.utility.DigestUtility
import com.suihan74.satena2.utility.ObjectSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class AccessTokenSerializer {
    protected lateinit var key: String
        private set

    fun initialize(uuid: String, path: String) {
        key = DigestUtility.sha256("$uuid@$path")
    }
}

/**
 * 暗号/復号 を施す[HatenaAccessToken]用のシリアライザ
 */
object HatenaAccessTokenSerializer : AccessTokenSerializer(), KSerializer<HatenaAccessToken> {
    override val descriptor = PrimitiveSerialDescriptor("HatenaAccessToken", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: HatenaAccessToken) {
        val objectSerializer = ObjectSerializer<HatenaAccessToken>()
        val serializedValue = objectSerializer.serialize(value)

        val encryptedValue = CryptUtility.encrypt(serializedValue, key)
        val encryptedSerializer = ObjectSerializer<CryptUtility.EncryptedData>()
        val serializedEncryptedValue = encryptedSerializer.serialize(encryptedValue)

        encoder.encodeString(serializedEncryptedValue)
    }

    override fun deserialize(decoder: Decoder): HatenaAccessToken {
        val encryptedSerializer = ObjectSerializer<CryptUtility.EncryptedData>()
        val serializedEncryptedValue = decoder.decodeString()
        val encryptedValue = encryptedSerializer.deserialize(serializedEncryptedValue)

        val value = CryptUtility.decrypt(encryptedValue, key)
        val objectSerializer = ObjectSerializer<HatenaAccessToken>()

        return objectSerializer.deserialize(value)
    }
}

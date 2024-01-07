package com.suihan74.satena2.serializer

import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

class AppRegistrationSerializer : KSerializer<AppRegistration> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("AppRegistration") {
            element<Long>("id")
            element<String>("clientId")
            element<String>("clientSecret")
            element<String>("redirectUri")
            element<String>("instanceName")
        }

    override fun serialize(encoder: Encoder, value: AppRegistration) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.id)
            encodeStringElement(descriptor, 1, value.clientId)
            encodeStringElement(descriptor, 2, value.clientSecret)
            encodeStringElement(descriptor, 3, value.redirectUri)
            encodeStringElement(descriptor, 4, value.instanceName)
        }
    }

    override fun deserialize(decoder: Decoder): AppRegistration = decoder.decodeStructure(descriptor) {
        var id = 0L
        var clientId = ""
        var clientSecret = ""
        var redirectUri = ""
        var instanceName = ""
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> id = decodeLongElement(descriptor, 0)
                1 -> clientId = decodeStringElement(descriptor, 1)
                2 -> clientSecret = decodeStringElement(descriptor, 2)
                3 -> redirectUri = decodeStringElement(descriptor, 3)
                4 -> instanceName = decodeStringElement(descriptor, 4)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("unexpected index: $index")
            }
        }
        AppRegistration(id, clientId, clientSecret, redirectUri, instanceName)
    }
}

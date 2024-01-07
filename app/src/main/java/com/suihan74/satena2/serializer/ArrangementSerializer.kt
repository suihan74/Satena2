package com.suihan74.satena2.serializer

import androidx.compose.foundation.layout.Arrangement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * `Arrangement.Horizontal`用のシリアライザ
 */
class HorizontalArrangementSerializer : KSerializer<Arrangement.Horizontal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Arrangement.Horizontal", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Arrangement.Horizontal) {
        encoder.encodeInt(
            when (value) {
                Arrangement.Start -> 0
                Arrangement.End -> 1
                else -> throw IllegalArgumentException()
            }
        )
    }

    override fun deserialize(decoder: Decoder): Arrangement.Horizontal {
        return when (decoder.decodeInt()) {
            0 -> Arrangement.Start
            1 -> Arrangement.End
            else -> throw IllegalStateException()
        }
    }
}

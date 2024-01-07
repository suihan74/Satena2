package com.suihan74.satena2.serializer

import androidx.compose.ui.Alignment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * `Alignment.Horizontal`用のシリアライザ
 */
class HorizontalAlignmentSerializer : KSerializer<Alignment.Horizontal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Alignment.Horizontal", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Alignment.Horizontal) {
        encoder.encodeInt(
            when (value) {
                Alignment.Start -> 0
                Alignment.CenterHorizontally -> 1
                Alignment.End -> 2
                else -> throw IllegalArgumentException()
            }
        )
    }

    override fun deserialize(decoder: Decoder): Alignment.Horizontal {
        return when (decoder.decodeInt()) {
            0 -> Alignment.Start
            1 -> Alignment.CenterHorizontally
            2 -> Alignment.End
            else -> throw IllegalStateException()
        }
    }
}

/**
 * `Alignment.Vertical`用のシリアライザ
 */
class VerticalAlignmentSerializer : KSerializer<Alignment.Vertical> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Alignment.Vertical", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Alignment.Vertical) {
        encoder.encodeInt(
            when (value) {
                Alignment.Top -> 0
                Alignment.CenterVertically -> 1
                Alignment.Bottom -> 2
                else -> throw IllegalArgumentException()
            }
        )
    }

    override fun deserialize(decoder: Decoder): Alignment.Vertical {
        return when (decoder.decodeInt()) {
            0 -> Alignment.Top
            1 -> Alignment.CenterVertically
            2 -> Alignment.Bottom
            else -> throw IllegalStateException()
        }
    }
}

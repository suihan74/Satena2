package com.suihan74.satena2.model.theme

import androidx.room.TypeConverter
import com.suihan74.satena2.serializer.ColorSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * `ThemeColors`用のRoomの型コンバータ
 *
 * `Color`が値classのため型コンバータを直接使用できないため、プリセットをまとめてJson化して保存する
 */
class ThemeColorsConverter {
    private val jsonFormat = Json {
        serializersModule = SerializersModule {
            contextual(ColorSerializer())
        }
    }

    @TypeConverter
    fun fromJson(value: String?) : ThemeColors? = value?.let { jsonFormat.decodeFromString<ThemeColors>(it) }

    @TypeConverter
    fun toJson(preset: ThemeColors?) : String? = preset?.let { jsonFormat.encodeToString(it) }
}

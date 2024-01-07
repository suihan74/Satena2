package com.suihan74.satena2.model.room.converter

import androidx.room.TypeConverter
import com.suihan74.hatena.model.entry.Entry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * [Entry]用のコンバータ
 */
class EntryConverter {
    private val json = Json

    @TypeConverter
    fun fromJson(value: String?) : Entry? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun toJson(value: Entry?) : String? = value?.let { json.encodeToString(it) }
}

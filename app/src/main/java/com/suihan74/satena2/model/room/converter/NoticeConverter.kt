package com.suihan74.satena2.model.room.converter

import androidx.room.TypeConverter
import com.suihan74.hatena.model.account.Notice
import com.suihan74.satena2.model.NoticeVerb
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * [Notice]用のコンバータ
 */
class NoticeConverter {
    private val json = Json

    @TypeConverter
    fun fromJson(value: String?) : Notice? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun toJson(value: Notice?) : String? = value?.let { json.encodeToString(it) }
}

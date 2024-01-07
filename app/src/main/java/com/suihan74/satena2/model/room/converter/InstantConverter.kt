package com.suihan74.satena2.model.room.converter

import androidx.room.TypeConverter
import java.time.Instant

/**
 * [Instant]用のコンバータ
 */
class InstantConverter {
    @TypeConverter
    fun fromEpochSecond(value: Long?) : Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toEpochSecond(value: Instant?) : Long? = value?.toEpochMilli()
}

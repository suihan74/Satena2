package com.suihan74.satena2.model.ignoredEntry

import androidx.room.TypeConverter

class IgnoredEntryTypeConverter {
    @TypeConverter
    fun fromInt(value: Int?) = value?.let { IgnoredEntryType.fromOrdinal(it) }

    @TypeConverter
    fun toInt(value: IgnoredEntryType?) = value?.ordinal
}

// ------ //

class IgnoreTargetConverter {
    @TypeConverter
    fun fromInt(value: Int?) = value?.let { IgnoreTarget.fromId(it) }

    @TypeConverter
    fun toInt(value: IgnoreTarget?) = value?.id
}

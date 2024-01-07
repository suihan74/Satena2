package com.suihan74.satena2.model.notice

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.suihan74.hatena.model.account.Notice
import com.suihan74.satena2.model.NoticeVerb
import com.suihan74.satena2.model.room.converter.NoticeConverter
import java.time.Instant

@Entity(
    tableName = "notice",
)
@TypeConverters(
    NoticeConverter::class
)
data class NoticeRecord(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,

    val user: String,

    val created : Instant,

    val modified : Instant,

    val verb: NoticeVerb,

    val notice: Notice
)

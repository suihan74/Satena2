package com.suihan74.satena2.model.entries

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.model.room.converter.EntryConverter
import java.time.Instant

/**
 * 既読エントリ情報
 */
@Entity(
    tableName = "read_entry",
    indices = [
        Index(value = ["url"], name = "read_entry_url", unique = true)
    ]
)
@TypeConverters(
    EntryConverter::class
)
data class ReadEntry(
    /**
     * エントリID（ある場合のみ。無いときは0）
     */
    val eid: Long,
    /**
     * URL
     */
    val url: String,
    /**
     * 既読にした日時
     */
    val timestamp: Instant,
    /**
     * エントリオブジェクト
     */
    val entry: Entry,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)

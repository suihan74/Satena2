package com.suihan74.satena2.model.entries

import androidx.room.*

@Dao
interface ReadEntryDao {
    @Query("""
        SELECT * FROM read_entry
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun load(offset: Int = 0, limit: Int = 20) : List<ReadEntry>

    // --- //

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ReadEntry) : Long

    // --- //

    @Update
    suspend fun update(vararg entry: ReadEntry)

    @Update
    suspend fun update(entries: List<ReadEntry>)

    // --- //

    @Query("SELECT EXISTS (SELECT * FROM read_entry WHERE url = :url)")
    suspend fun exists(url: String) : Boolean

    @Query("SELECT EXISTS (SELECT * FROM read_entry WHERE eid = :eid)")
    suspend fun exists(eid: Long) : Boolean

    @Query("SELECT * FROM read_entry WHERE url = :url")
    suspend fun findReadEntry(url: String) : ReadEntry?

    @Query("SELECT * FROM read_entry WHERE eid = :eid")
    suspend fun findReadEntry(eid: Long) : ReadEntry?

    @Query("SELECT * FROM read_entry WHERE url in (:urls)")
    suspend fun getReadEntriesFromUrls(urls: List<String>) : List<ReadEntry>

    @Query("SELECT * FROM read_entry WHERE eid IN (:entryIds)")
    suspend fun getReadEntriesFromEntryIds(entryIds: List<Long>) : List<ReadEntry>

    // --- //

    @Query("DELETE FROM read_entry WHERE url = :url")
    suspend fun delete(url: String)

    @Query("DELETE FROM read_entry WHERE eid = :eid")
    suspend fun delete(eid: Long)

    @Delete
    suspend fun delete(entry: ReadEntry)
}

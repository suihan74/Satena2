package com.suihan74.satena2.model.ignoredEntry

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IgnoredEntryDao {
    @Query("SELECT * FROM ignored_entry")
    fun allFiltersFlow() : Flow<List<IgnoredEntry>>

    @Query("SELECT * FROM ignored_entry WHERE target & :targetInt > 0")
    fun filtersFlow(targetInt: Int) : Flow<List<IgnoredEntry>>

    fun entryFiltersFlow() : Flow<List<IgnoredEntry>> = filtersFlow(IgnoreTarget.ENTRY.id)

    fun bookmarkFiltersFlow() : Flow<List<IgnoredEntry>> = filtersFlow(IgnoreTarget.BOOKMARK.id)

    /**
     * 全ての非表示設定を取得する
     */
    @Query("SELECT * FROM ignored_entry ORDER BY id")
    suspend fun getAllEntries(): List<IgnoredEntry>

    /**
     * 適用対象で絞って非表示設定を取得する
     */
    @Query("""
        SELECT * FROM ignored_entry
        WHERE target & :targetInt > 0
        ORDER BY id
    """)
    suspend fun getEntriesByTarget(targetInt: Int) : List<IgnoredEntry>

    /**
     * 適用対象にブクマを含む非表示設定を取得する
     */
    suspend fun getEntriesForBookmarks() : List<IgnoredEntry> = getEntriesByTarget(IgnoreTarget.BOOKMARK.id)

    /**
     * 適用対象にエントリを含む非表示設定を取得する
     */
    suspend fun getEntriesForEntries() : List<IgnoredEntry> = getEntriesByTarget(IgnoreTarget.ENTRY.id)

    /**
     * 非表示URL or 非表示ワード で絞って非表示設定を取得する
     */
    @Query("""
        SELECT * FROM ignored_entry
        WHERE type = :typeInt
        ORDER BY id
    """)
    suspend fun getEntriesByType(typeInt: Int) : List<IgnoredEntry>

    /**
     * 非表示URL設定を取得する
     */
    suspend fun getNgUrlEntries() : List<IgnoredEntry> = getEntriesByType(IgnoredEntryType.URL.ordinal)

    /**
     * 非表示ワード設定を取得する
     */
    suspend fun getNgWordEntries() : List<IgnoredEntry> = getEntriesByType(IgnoredEntryType.TEXT.ordinal)

    /**
     * 条件に合致する非表示設定を0個or1個取得する
     */
    @Query("""
        SELECT * FROM ignored_entry 
        WHERE type = :typeInt AND `query` = :query
        LIMIT 1
    """)
    suspend fun find(typeInt: Int, query: String) : IgnoredEntry?

    /**
     * 条件に合致する非表示設定を0個or1個取得する
     */
    suspend fun find(type: IgnoredEntryType, query: String) = find(type.ordinal, query)

    // ------ //

    /**
     * 非表示設定を追加する
     *
     * @throws SQLiteConstraintException typeとqueryが重複した項目を挿入しようとした場合
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: IgnoredEntry) : Long

    /**
     * 非表示設定を更新する
     */
    @Update
    suspend fun update(entry: IgnoredEntry)

    // ------ //

    /**
     * 非表示設定を削除する
     */
    @Query("""
        DELETE FROM ignored_entry
        WHERE type = :typeInt AND `query` = :query
    """)
    suspend fun delete(typeInt: Int, query: String)

    /**
     * 非表示設定を削除する
     */
    suspend fun delete(type: IgnoredEntryType, query: String) = delete(type.ordinal, query)

    /**
     * 非表示設定を削除する
     */
    suspend fun delete(entry: IgnoredEntry) = delete(entry.type.ordinal, entry.query)

    /**
     * すべての非表示設定を削除する
     */
    @Query("""
        DELETE FROM ignored_entry
    """)
    suspend fun clear()
}

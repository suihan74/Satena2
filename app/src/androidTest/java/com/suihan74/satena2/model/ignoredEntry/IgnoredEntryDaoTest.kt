package com.suihan74.satena2.model.ignoredEntry

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.suihan74.satena2.model.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOError

@RunWith(AndroidJUnit4::class)
class IgnoredEntryDaoTest {
    private lateinit var dao : IgnoredEntryDao
    private lateinit var db : AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.ignoredEntryDao()
    }

    @After
    @Throws(IOError::class)
    fun closeDb() {
        db.close()
    }

    // ------ //

    /**
     * 非表示URL設定の追加
     */
    @Test
    fun insertNgUrl() = runBlocking {
        val entry = IgnoredEntry(
            type = IgnoredEntryType.URL,
            query = "test.com"
        )
        dao.insert(entry)
        val stored = dao.find(entry.type, entry.query)
        assertEquals(entry, stored)
    }

    /**
     * 非表示テキスト設定の追加
     */
    @Test
    fun insertNgText() = runBlocking {
        val entry = IgnoredEntry(
            type = IgnoredEntryType.TEXT,
            query = "test"
        )
        dao.insert(entry)
        val stored = dao.find(entry.type, entry.query)
        assertEquals(entry, stored)
    }

    /**
     * 重複した非表示URL設定を追加 -> エラー発生
     */
    @Test
    fun insertDuplicatedNgUrl() = runBlocking {
        val entry1 = IgnoredEntry(
            type = IgnoredEntryType.URL,
            query = "test.com"
        )
        val entry2 = IgnoredEntry(
            type = IgnoredEntryType.URL,
            query = "test.com"
        )
        dao.insert(entry1)
        val result = runCatching {
            dao.insert(entry2)
        }
        assert(result.exceptionOrNull() is SQLiteConstraintException)
    }

    /**
     * 重複した非表示テキストを追加 -> エラー発生
     */
    @Test
    fun insertDuplicatedNgText() = runBlocking {
        val entry1 = IgnoredEntry(
            type = IgnoredEntryType.TEXT,
            query = "test"
        )
        val entry2 = IgnoredEntry(
            type = IgnoredEntryType.TEXT,
            query = "test"
        )
        dao.insert(entry1)
        val result = runCatching {
            dao.insert(entry2)
        }
        assert(result.exceptionOrNull() is SQLiteConstraintException)
    }

    /**
     * queryが同一でもtypeが異なれば重複を許す
     */
    @Test
    fun insertDuplicatedNgQuery() = runBlocking {
        val textEntry = IgnoredEntry(
            type = IgnoredEntryType.TEXT,
            query = "test.com"
        )
        val urlEntry = IgnoredEntry(
            type = IgnoredEntryType.URL,
            query = "test.com"
        )
        dao.insert(textEntry)
        val result = runCatching {
            dao.insert(urlEntry)
        }
        assert(result.isSuccess)
    }

    /**
     * 非表示URLを削除
     */
    @Test
    fun deleteNgUrl() = runBlocking {
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.URL,
                query = "test.com"
            )
        )
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.URL,
                query = "test.com2"
            )
        )
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.URL,
                query = "test.com3"
            )
        )
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.TEXT,
                query = "test.com"
            )
        )
        dao.delete(IgnoredEntryType.URL, "test.com")
        val items = dao.getAllEntries()
        assertEquals(3, items.size)
        assert(!items.contains(IgnoredEntry(IgnoredEntryType.URL, "test.com")))
    }

    @Test
    fun deleteAll() = runBlocking {
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.URL,
                query = "test.com"
            )
        )
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.URL,
                query = "test.com2"
            )
        )
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.URL,
                query = "test.com3"
            )
        )
        dao.insert(
            IgnoredEntry(
                type = IgnoredEntryType.TEXT,
                query = "test.com"
            )
        )
        dao.clear()
        val items = dao.getAllEntries()
        assertEquals(0, items.size)
    }
}

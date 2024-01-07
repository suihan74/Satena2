package com.suihan74.satena2.scene.preferences.page.ngWords

import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 空クエリで非表示設定を追加しようとしたときの例外
 */
class EmptyNgQueryError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)

/**
 * 重複する非表示設定を追加しようとしたときの例外
 */
class NgWordDuplicationError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)

/**
 * 非表示設定の削除に失敗
 */
class NgWordDeletionError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)

// ------ //

/**
 * 非表示設定リポジトリ
 */
interface NgWordsRepository {
    /**
     * URLに対する非表示設定
     */
    val ngUrls: StateFlow<List<IgnoredEntry>>

    /**
     * 文字列に対する非表示設定
     */
    val ngWords: StateFlow<List<IgnoredEntry>>

    // ------ //

    /**
     * 各非表示設定リストを取得する
     */
    suspend fun initialize()

    // ------ //

    /**
     * 非表示設定を追加
     *
     * @throws EmptyNgQueryError 空クエリの設定を挿入しようとした
     * @throws NgWordDuplicationError 重複する設定を挿入しようとした
     */
    suspend fun insert(entry: IgnoredEntry)

    /**
     * 非表示設定を更新
     *
     * @throws EmptyNgQueryError 空クエリの設定を挿入しようとした
     * @throws NgWordDuplicationError 重複する設定を挿入しようとした
     */
    suspend fun update(entry: IgnoredEntry)

    /**
     * 非表示設定を削除
     */
    suspend fun delete(entry: IgnoredEntry)
}

// ------ //

class NgWordsRepositoryImpl(appDatabase: AppDatabase) : NgWordsRepository {
    private val dao = appDatabase.ignoredEntryDao()

    // ------ //

    /**
     * URLに対する非表示設定
     */
    override val ngUrls : StateFlow<List<IgnoredEntry>> = MutableStateFlow(emptyList())
    private val _ngUrls = ngUrls as MutableStateFlow

    /**
     * 文字列に対する非表示設定
     */
    override val ngWords : StateFlow<List<IgnoredEntry>> = MutableStateFlow(emptyList())
    private val _ngWords = ngWords as MutableStateFlow

    // ------ //

    override suspend fun initialize() {
        _ngUrls.value = dao.getNgUrlEntries()
        _ngWords.value = dao.getNgWordEntries()
    }

    // ------ //

    /**
     * 非表示設定を追加
     *
     * @throws EmptyNgQueryError 空クエリの設定を挿入しようとした
     * @throws NgWordDuplicationError 重複する設定を挿入しようとした
     */
    override suspend fun insert(entry: IgnoredEntry) {
        if (entry.query.isBlank()) throw EmptyNgQueryError()

        runCatching {
            dao.insert(entry)
        }.onFailure {
            throw NgWordDuplicationError(cause = it)
        }.onSuccess { id ->
            when (entry.type) {
                IgnoredEntryType.URL -> {
                    _ngUrls.value = _ngUrls.value.plus(entry.copy(id = id))
                }
                IgnoredEntryType.TEXT -> {
                    _ngWords.value = _ngWords.value.plus(entry.copy(id = id))
                }
            }
        }
    }

    /**
     * 非表示設定を更新
     *
     * @throws EmptyNgQueryError 空クエリの設定を挿入しようとした
     * @throws NgWordDuplicationError 重複する設定を挿入しようとした
     */
    override suspend fun update(entry: IgnoredEntry) {
        if (entry.query.isBlank()) throw EmptyNgQueryError()

        runCatching {
            dao.update(entry)
        }.onFailure {
            throw NgWordDuplicationError(cause = it)
        }.onSuccess {
            when (entry.type) {
                IgnoredEntryType.URL -> {
                    _ngUrls.value = _ngUrls.value.map {
                        if (it.id == entry.id) entry else it
                    }
                }
                IgnoredEntryType.TEXT -> {
                    _ngWords.value = _ngWords.value.map {
                        if (it.id == entry.id) entry else it
                    }
                }
            }
        }
    }

    /**
     * 非表示設定を削除
     */
    override suspend fun delete(entry: IgnoredEntry) {
        runCatching {
            dao.delete(entry)
        }.onFailure {
            throw NgWordDeletionError(cause = it)
        }.onSuccess {
            when (entry.type) {
                IgnoredEntryType.URL -> {
                    _ngUrls.value = _ngUrls.value.minus(entry)
                }
                IgnoredEntryType.TEXT -> {
                    _ngWords.value = _ngWords.value.minus(entry)
                }
            }
        }
    }
}

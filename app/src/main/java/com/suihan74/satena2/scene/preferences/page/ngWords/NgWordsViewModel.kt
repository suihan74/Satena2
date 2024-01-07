package com.suihan74.satena2.scene.preferences.page.ngWords

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.R
import com.suihan74.satena2.model.ignoredEntry.IgnoreTarget
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntryType
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionResult
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionResult.Companion.update
import com.suihan74.satena2.utility.extension.alsoAsMutable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

interface NgWordsUpdater {
    /**
     * 入力されたNGワードが（正規表現として）利用可能かチェックする
     */
    fun isNgRegexError(text: String, isRegex: Boolean) : Boolean {
        if (!isRegex) return false
        return runCatching { Pattern.compile(text) }.isFailure
    }

    /**
     * 挿入・更新時のエラーハンドラ
     */
    fun handleUpdateNgWordErrors(context: Context, e: Throwable) {
        when (e) {
            is NgWordDuplicationError -> {
                Toast.makeText(
                    context,
                    R.string.ng_word_setting_msg_insert_duplication_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is EmptyNgQueryError -> {
                Toast.makeText(
                    context,
                    R.string.ng_word_setting_msg_insert_empty_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is IllegalArgumentException -> {
                Toast.makeText(
                    context,
                    R.string.ng_word_setting_msg_insert_regex_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

interface NgWordsViewModel : IPreferencePageViewModel, NgWordsUpdater {
    /**
     * 現在表示中のタブ
     */
    val currentTab : StateFlow<Int>

    /**
     * 現在表示中のタブに表示するNG設定リスト
     */
    val currentTabItems : StateFlow<List<IgnoredEntry>>

    // ------ //

    /**
     * 表示中のタブを変更する
     */
    fun changeCurrentTab(index: Int) {
        currentTab.alsoAsMutable { it.value = index }
    }

    // ------ //

    /**
     * 非表示設定を追加する
     */
    suspend fun insert(entry: IgnoredEntry) : Boolean

    /**
     * 既存の非表示設定を更新する
     */
    suspend fun update(entry: IgnoredEntry) : Boolean

    /**
     * 非表示設定を削除する
     */
    suspend fun delete(entry: IgnoredEntry) : Boolean

    // ------ //

    /**
     * 非表示設定を追加する
     *
     * View側から値を渡すために使用
     */
    suspend fun insertNgSetting(args: NgWordEditionResult) : Boolean = insert(args.toIgnoredEntry())

    /**
     * 非表示設定を更新する
     *
     * View側から値を渡すために使用
     */
    suspend fun updateNgSetting(origin: IgnoredEntry, args: NgWordEditionResult) : Boolean = update(origin.update(args))
}

// ------ //

@HiltViewModel
class NgWordsViewModelImpl @Inject constructor(
    private val repository : NgWordsRepository,
    private val prefsRepository : PreferencesRepository
) :
    NgWordsViewModel,
    PreferencePageViewModel(prefsRepository.dataStore)
{
    override val currentTab = MutableStateFlow(IgnoredEntryType.URL.ordinal)
    override val currentTabItems = MutableStateFlow<List<IgnoredEntry>>(emptyList())

    private var collectionJob : Job? = null

    // ------ //

    init {
        viewModelScope.launch {
            repository.initialize()
            currentTab.collect {
                updateCurrentTabItems(it)
            }
        }
    }

    // ------ //

    override suspend fun insert(entry: IgnoredEntry) : Boolean {
        val result =
            runCatching {
                if (isNgRegexError(entry.query, entry.asRegex)) {
                    throw IllegalArgumentException()
                }
                repository.insert(entry)
            }.onFailure { handleUpdateNgWordErrors(context, it) }
        return result.isSuccess
    }

    override suspend fun update(entry: IgnoredEntry): Boolean {
        val result =
            runCatching {
                if (isNgRegexError(entry.query, entry.asRegex)) {
                    throw IllegalArgumentException()
                }
                repository.update(entry)
            }.onFailure { handleUpdateNgWordErrors(context, it) }
        return result.isSuccess
    }

    override suspend fun delete(entry: IgnoredEntry): Boolean {
        runCatching {
            repository.delete(entry)
        }.onFailure {
            Toast.makeText(
                context,
                "何かあって削除失敗",  // TODO
                Toast.LENGTH_SHORT
            ).show()
        }
        return true
    }

    // ------ //

    private suspend fun updateCurrentTabItems(tabIndex: Int) {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            when (tabIndex) {
                IgnoredEntryType.URL.ordinal -> {
                    repository.ngUrls.collect { currentTabItems.value = it }
                }
                IgnoredEntryType.TEXT.ordinal -> {
                    repository.ngWords.collect { currentTabItems.value = it }
                }
            }
        }
    }
}

// ------ //

class FakeNgWordsViewModel(coroutineScope: CoroutineScope) :
    NgWordsViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val currentTab = MutableStateFlow(0)
    override val currentTabItems = MutableStateFlow<List<IgnoredEntry>>(emptyList())

    // ------ //

    override suspend fun insert(entry: IgnoredEntry) : Boolean {
        when (entry.type) {
            IgnoredEntryType.TEXT -> {
                ngWords.value = ngWords.value.plus(entry)
            }
            IgnoredEntryType.URL -> {
                ngUrls.value = ngUrls.value.plus(entry)
            }
        }
        updateCurrentTabItems(currentTab.value)
        return true
    }

    override suspend fun update(entry: IgnoredEntry): Boolean {
        when (entry.type) {
            IgnoredEntryType.TEXT -> {
                ngWords.value = ngWords.value.map {
                    if (it.id == entry.id) entry else it
                }
            }
            IgnoredEntryType.URL -> {
                ngUrls.value = ngUrls.value.map {
                    if (it.id == entry.id) entry else it
                }
            }
        }
        updateCurrentTabItems(currentTab.value)
        return true
    }

    override suspend fun delete(entry: IgnoredEntry): Boolean {
        when (entry.type) {
            IgnoredEntryType.TEXT -> {
                ngWords.value = ngWords.value.minus(entry)
            }
            IgnoredEntryType.URL -> {
                ngUrls.value = ngUrls.value.minus(entry)
            }
        }
        updateCurrentTabItems(currentTab.value)
        return true
    }

    // ------ //

    private val ngUrls = MutableStateFlow(listOf(
        IgnoredEntry.createDummy(query = "dummy.com"),
        IgnoredEntry.createDummy(query = "test.co.jp"),
        IgnoredEntry.createDummy(query = "test.ne.jp")
    ))
    private val ngWords = MutableStateFlow(listOf(
        IgnoredEntry.createDummy(type = IgnoredEntryType.TEXT, query = "test1"),
        IgnoredEntry.createDummy(type = IgnoredEntryType.TEXT, query = "test2", target = IgnoreTarget.ENTRY),
        IgnoredEntry.createDummy(type = IgnoredEntryType.TEXT, query = "test3", target = IgnoreTarget.BOOKMARK),
        IgnoredEntry.createDummy(type = IgnoredEntryType.TEXT, query = "test4", target = IgnoreTarget.ALL)
    ))

    // ------ //

    init {
        currentTab.onEach { updateCurrentTabItems(it) }.launchIn(coroutineScope)
    }

    // ------ //

    private fun updateCurrentTabItems(tabIndex: Int) {
        currentTabItems.value =
            when (tabIndex) {
                IgnoredEntryType.URL.ordinal -> ngUrls.value
                IgnoredEntryType.TEXT.ordinal -> ngWords.value
                else -> throw NotImplementedError()
            }
    }
}


package com.suihan74.satena2.scene.bookmarks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.browser.BrowserActivityContract
import com.suihan74.satena2.scene.entries.ClickEntryAction
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.EntriesActivityContract
import com.suihan74.satena2.scene.entries.EntriesRepository
import com.suihan74.satena2.scene.entries.EntryActionHandler
import com.suihan74.satena2.scene.entries.EntryItemEvent
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsRepository
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsUpdater
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionResult
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.createIntentWithoutThisApplication
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 関連エントリリスト項目に対する操作を処理する[ViewModel]
 */
@HiltViewModel
class RelatedEntriesViewModelImpl @Inject constructor(
    prefsRepo : PreferencesRepository,
    private val entriesRepo : EntriesRepository,
    private val ngWordsRepo: NgWordsRepository
) :
    EntryActionHandler,
    NgWordsUpdater,
    ViewModel()
{
    private val prefsFlow = prefsRepo.dataStore.data

    private val clickAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val longClickAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val doubleClickAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val clickEdgeAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val longClickEdgeAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val doubleClickEdgeAction = MutableStateFlow(ClickEntryAction.NOTHING)

    // ------ //

    init {
        prefsFlow
            .onEach {
                clickAction.value = it.clickEntryAction
                longClickAction.value = it.longClickEntryAction
                doubleClickAction.value = it.doubleClickEntryAction

                clickEdgeAction.value = it.clickEntryEdgeAction
                longClickEdgeAction.value = it.longClickEntryEdgeAction
                doubleClickEdgeAction.value = it.doubleClickEntryEdgeAction
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
        lifecycleObserver = LifecycleObserver(activityResultRegistry!!)
        lifecycle?.addObserver(lifecycleObserver)
    }

    // ------ //

    /**
     * 項目の各種クリックイベント処理
     */
    override fun onEvent(
        entry: DisplayEntry,
        event: EntryItemEvent,
        onShowMenu: (DisplayEntry)->Unit,
        onShare: (DisplayEntry)->Unit
    ) {
        val action = when (event) {
            EntryItemEvent.Click -> clickAction.value
            EntryItemEvent.LongClick -> longClickAction.value
            EntryItemEvent.DoubleClick -> doubleClickAction.value
            EntryItemEvent.ClickEdge -> clickEdgeAction.value
            EntryItemEvent.LongClickEdge -> longClickEdgeAction.value
            EntryItemEvent.DoubleClickEdge -> doubleClickEdgeAction.value
        }
        launchClickAction(entry, action, onShowMenu, onShare)
    }

    private fun launchClickAction(
        entry: DisplayEntry,
        action: ClickEntryAction,
        onShowMenu: (DisplayEntry)->Unit,
        onShare: (DisplayEntry)->Unit
    ) {
        when (action) {
            ClickEntryAction.NOTHING -> {}
            ClickEntryAction.SHOW_COMMENTS -> launchBookmarksActivity(entry)
            ClickEntryAction.SHOW_PAGE -> launchBrowserActivity(entry)
            ClickEntryAction.SHOW_PAGE_IN_OTHER_APP -> openWithOtherApp(entry)
            ClickEntryAction.SHARE -> onShare(entry)
            ClickEntryAction.SHOW_MENU -> onShowMenu(entry)
        }
    }

    // ------ //


    /**
     * ブクマページを開く
     */
    override fun launchBookmarksActivity(entry: DisplayEntry) {
        readMarkEntry(entry)
        lifecycleObserver.launchBookmarksActivity(entry.entry)
    }

    /**
     * アプリ内ブラウザでページを開く
     */
    override fun launchBrowserActivity(entry: DisplayEntry) {
        readMarkEntry(entry)
        lifecycleObserver.launchBrowserActivity(entry.entry)
    }

    /**
     * アプリ外ブラウザでページを開く
     */
    override fun openWithOtherApp(entry: DisplayEntry) {
        lifecycleObserver.launchOuterBrowserActivity(entry.entry)
    }

    /**
     * 指定サイトのエントリ一覧を表示する
     */
    override fun navigateSiteCategory(url: String, navController: NavController) {
        lifecycleObserver.launchEntriesActivity(url)
    }

    /**
     * 既読マークをつける
     */
    private fun readMarkEntry(entry: DisplayEntry) {
        viewModelScope.launch {
            entriesRepo.readMark(entry.entry)
        }
    }

    /**
     * 既読マークを消す
     */
    override fun removeReadMark(entry: DisplayEntry) {
        viewModelScope.launch {
            entriesRepo.removeReadMark(entry.entry)
        }
    }

    /**
     * ブクマを削除する
     */
    override fun removeBookmark(entry: DisplayEntry) {
        viewModelScope.launch {
            runCatching {
                entriesRepo.removeBookmark(entry.entry)
            }.onSuccess {
                context.showToast(
                    textId = R.string.entry_msg_success_remove_bookmark,
                    duration = Toast.LENGTH_SHORT
                )
            }.onFailure {
                Log.e("EntriesViewModel", it.stackTraceToString())
                context.showToast(
                    textId = R.string.entry_msg_failure_remove_bookmark,
                    duration = Toast.LENGTH_SHORT
                )
            }
        }
    }

    /**
     * 「あとで読む」タグをつけてブクマする
     */
    override fun readLaterEntry(entry: DisplayEntry, isPrivate: Boolean) {
        viewModelScope.launch {
            entriesRepo.readLater(entry.entry, isPrivate)
        }
    }

    /**
     * 「読んだ」タグをつけてブクマする
     */
    override fun readEntry(entry: DisplayEntry, isPrivate: Boolean) {
        viewModelScope.launch {
            entriesRepo.read(entry.entry, isPrivate)
        }
    }

    // ------ //

    /**
     * コメントをクリックしたときの挙動
     */
    override fun onClickComment(entry: DisplayEntry, bookmark: BookmarkResult) {
        viewModelScope.launch {
            entriesRepo.readMark(entry.entry)
        }
        lifecycleObserver.launchBookmarksActivity(entry.entry, bookmark.user)
    }

    /**
     * コメントを長クリックしたときの挙動
     */
    override fun onLongClickComment(entry: DisplayEntry, bookmark: BookmarkResult) {
    }

    // ------ //

    /**
     * NG設定を追加
     */
    override suspend fun insertNgWord(args: NgWordEditionResult) : Boolean =
        runCatching {
            if (isNgRegexError(args.query, args.asRegex)) {
                throw IllegalArgumentException()
            }
            ngWordsRepo.insert(args.toIgnoredEntry())
        }.onSuccess {
            Toast.makeText(
                context,
                context.getString(R.string.ng_word_setting_msg_insert_success, args.query),
                Toast.LENGTH_SHORT
            ).show()
        }.onFailure { handleUpdateNgWordErrors(context, it) }.isSuccess

    // ------ //

    /** 他画面とデータをやりとりしながら遷移するためのやつ */
    private lateinit var lifecycleObserver : LifecycleObserver

    inner class LifecycleObserver(
        private val registry : ActivityResultRegistry
    ) : DefaultLifecycleObserver {
        /** [com.suihan74.satena2.scene.entries.EntriesActivity]のランチャs */
        private lateinit var entriesActivityLauncher : ActivityResultLauncher<String>

        /** [com.suihan74.satena2.scene.bookmarks.BookmarksActivity]のランチャ */
        private lateinit var bookmarksActivityLauncher : ActivityResultLauncher<Entry>

        /** [com.suihan74.satena2.scene.bookmarks.BookmarksActivity]のランチャ */
        private lateinit var bookmarksActivityLauncherWithUser : ActivityResultLauncher<Pair<Entry, String>>

        /** [com.suihan74.satena2.scene.browser.BrowserActivity]のランチャ */
        private lateinit var browserActivityLauncher : ActivityResultLauncher<String?>

        /** 外部ブラウザのランチャ */
        private lateinit var outerBrowserActivityLauncher : ActivityResultLauncher<String>

        override fun onCreate(owner: LifecycleOwner) {
            entriesActivityLauncher = registry.register(
                "EntriesActivityLauncher",
                owner,
                EntriesActivityContract.WithUrl()
            ) { /* do nothing */ }
            bookmarksActivityLauncher = registry.register(
                "BookmarksActivityLauncher",
                owner,
                BookmarksActivityContract.WithEntry()
            ) { resultEntry ->
                viewModelScope.launch {
                    entriesRepo.updateEntry(resultEntry)
                }
            }

            bookmarksActivityLauncherWithUser = registry.register(
                "BookmarksActivityLauncherWithUser",
                owner,
                BookmarksActivityContract.WithEntryAndUser()
            ) { resultEntry ->
                viewModelScope.launch {
                    entriesRepo.updateEntry(resultEntry)
                }
            }

            browserActivityLauncher = registry.register(
                "BrowserActivityLauncher",
                owner,
                BrowserActivityContract()
            ) { /* do nothing */ }

            outerBrowserActivityLauncher = registry.register(
                "OuterBrowserActivityLauncher",
                owner,
                object : ActivityResultContract<String, Unit>() {
                    override fun createIntent(context: Context, input: String) =
                        Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(input)
                        }.createIntentWithoutThisApplication(context)
                    override fun parseResult(resultCode: Int, intent: Intent?) { /* do nothing */ }
                }
            ) { /* do nothing */ }
        }

        fun launchEntriesActivity(url: String) { entriesActivityLauncher.launch(url) }
        fun launchBookmarksActivity(entry: Entry) { bookmarksActivityLauncher.launch(entry) }
        fun launchBookmarksActivity(entry: Entry, targetUser: String) { bookmarksActivityLauncherWithUser.launch(entry to targetUser) }
        fun launchBrowserActivity(entry: Entry) { browserActivityLauncher.launch(entry.url) }
        fun launchOuterBrowserActivity(entry: Entry) { outerBrowserActivityLauncher.launch(entry.url) }
    }
}

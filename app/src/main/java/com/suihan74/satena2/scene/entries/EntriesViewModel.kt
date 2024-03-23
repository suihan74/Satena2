package com.suihan74.satena2.scene.entries

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.suihan74.hatena.model.account.Account
import com.suihan74.hatena.model.account.Notice
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.entry.Issue
import com.suihan74.hatena.model.entry.MaintenanceEntry
import com.suihan74.hatena.model.entry.SearchType
import com.suihan74.satena2.R
import com.suihan74.satena2.model.NoticeVerb
import com.suihan74.satena2.scene.bookmarks.BookmarksActivityContract
import com.suihan74.satena2.scene.browser.BrowserActivityContract
import com.suihan74.satena2.scene.entries.bottomSheet.SearchSetting
import com.suihan74.satena2.scene.preferences.PreferencesActivityContract
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.accounts.SignInState
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAuthenticationActivity
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaFetchNgUsersException
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaSignInException
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsRepository
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsUpdater
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionResult
import com.suihan74.satena2.utility.DialogPropertiesProvider
import com.suihan74.satena2.utility.DialogPropertiesProviderImpl
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.currentArgument
import com.suihan74.satena2.utility.extension.createIntentWithoutThisApplication
import com.suihan74.satena2.utility.extension.showToast
import com.suihan74.satena2.utility.extension.trimScheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * EntriesActivityのUI部分から要求されるVMインターフェイス
 */
interface EntriesViewModel :
    NgWordsUpdater,
    EntryActionHandler,
    DialogPropertiesProvider
{
    /**
     * リスト更新の実行状態
     */
    fun loadingStateFlow(destination: Destination) : StateFlow<Boolean>

    /**
     * Hatenaのサインイン状態
     */
    val hatenaSignInState : StateFlow<SignInState>

    /**
     * Hatenaのアカウント情報
     */
   val hatenaAccount : StateFlow<Account?>

    /**
     * ボトムメニューを使用する
     */
    val useBottomMenu : Flow<Boolean>

    /**
     * ボトムメニューの配置
     */
    val bottomMenuArrangement : Flow<Arrangement.Horizontal>

    /**
     * ボトムメニューの項目
     */
    val bottomMenuItems : Flow<List<BottomMenuItem>>

    /**
     * カテゴリリストの表示形式
     */
    val categoryListType : Flow<EntryCategoryListType>

    /**
     * 最初に表示するカテゴリ
     */
    val initialState : StateFlow<EntryNavigationState>

    /**
     * 最初に表示するタブ
     */
    val initialTabs : Flow<Map<Category, Int>>

    /**
     * ドロワの配置
     */
    val drawerAlignment : Flow<Alignment.Horizontal>

    /**
     * 長押し時の振動時間
     */
    val longClickVibrationDuration : Flow<Long>

    /**
     * 日時をシステムのタイムゾーンで表示する
     */
    val useSystemTimeZone : Flow<Boolean>

    /**
     * 現在有効な検索設定
     */
    val searchSettingFlow : StateFlow<SearchSetting>

    /**
     * 現在有効なマイブクマ検索設定
     */
    val searchMyBookmarksSettingFlow : StateFlow<SearchSetting>

    /**
     * 既読エントリを記録する
     */
    val recordReadEntriesEnabled : StateFlow<Boolean>

    // ------ //

    /**
     * composeの外部からスクロール操作を伝播するためのフロー
     */
    val scrollingFlow : Flow<Scroll>

    // ------ //

    /**
     * カテゴリを使用して画面遷移
     */
    fun navigate(
        navController: NavController,
        category: Category,
        issue: Issue? = null,
        target: String? = null,
        clearStack: Boolean = false
    )

    /**
     * 最初の画面移動
     */
    fun initialNavigation(
        initialState: EntryNavigationState,
        navController: NavController
    )

    // ------ //

    /**
     * トップバーのタイトル
     */
    fun topBarTitle(category: Category, target: String?) : String

    /**
     * スワイプによるエントリリスト更新
     */
    fun swipeRefresh(destination: Destination)

    /**
     * 追加ロード
     */
    fun loadAdditional(destination: Destination)

    /**
     * エントリ検索
     */
    fun search(searchSetting: SearchSetting)

    /**
     * マイブクマ検索設定を変更し、再検索
     */
    fun searchMyBookmarks(searchSetting: SearchSetting)

    /**
     * ブラウザで検索する
     */
    fun searchInBrowser(query: String)

    /**
     * 設定画面に遷移
     */
    fun launchPreferencesActivity()

    /**
     * Hatenaのサインイン画面に遷移
     */
    fun launchHatenaAuthenticationActivity(context: Context)

    // ------ //

    /**
     * 通知を単クリックしたときの挙動
     */
    fun onClick(notice: Notice)

    /**
     * 通知を長クリックしたときの挙動
     */
    fun onLongClick(notice: Notice)

    // ------ //

    /**
     * ボトムメニュー項目をクリックしたときの挙動
     */
    suspend fun onClickBottomMenuItem(
        item: BottomMenuItem,
        navController: NavController,
        onOpenDrawer: suspend () -> Unit = {},
        onShowExcludedEntriesBottomSheet: suspend ()->Unit = {}
    )

    /**
     * ボトムメニュー項目を長クリックしたときの挙動
     */
    suspend fun onLongClickBottomMenuItem(
        item: BottomMenuItem,
        onShowBrowserBottomSheet: suspend ()->Unit = {}
    )

    // ------ //

    /**
     * エントリリスト
     */
    fun entriesFlow(destination: Destination) : EntriesListFlow

    /**
     * 除外されたエントリリスト
     */
    fun excludedEntriesFlow(destination: Destination) : EntriesListFlow

    /**
     * Issueリスト
     */
    fun issuesFlow(category: Category) : Flow<List<Issue>>

    /**
     * 通知リスト
     */
    val noticesFlow : StateFlow<List<Notice>>

    /**
     * メンテナンス情報リスト
     */
    val maintenanceEntriesFlow : StateFlow<List<MaintenanceEntry>>

    /**
     * 各画面に対応する一意のキーを取得する
     */
    fun getMapKey(destination: Destination) : String
}

// ------ //

/**
 * アプリ実働時用のVM
 */
@HiltViewModel
class EntriesViewModelImpl @Inject constructor(
    private val entriesRepo: EntriesRepository,
    private val hatenaRepo: HatenaAccountRepository,
    prefsRepo: PreferencesRepository,
    private val ngWordsRepository: NgWordsRepository
) : EntriesViewModel,
    ViewModel(),
    DialogPropertiesProvider by DialogPropertiesProviderImpl(prefsRepo.dataStore)
{
    private val prefsFlow = prefsRepo.dataStore.data

    /**
     * スワイプによるリスト更新の実行状態
     */
    override fun loadingStateFlow(destination: Destination) : StateFlow<Boolean> = entriesRepo.getLoadingState(destination)

    /**
     * Hatenaのサインイン状態
     */
    override val hatenaSignInState : StateFlow<SignInState> = hatenaRepo.state

    /**
     * Hatenaのユーザー
     */
    override val hatenaAccount : StateFlow<Account?> = hatenaRepo.account

    /**
     * ボトムメニューを使用する
     */
    override val useBottomMenu = prefsFlow.map { it.useEntryBottomMenu }

    /**
     * ボトムメニューの配置
     */
    override val bottomMenuArrangement = prefsFlow.map { it.entryBottomMenuArrangement }

    /**
     * ボトムメニューの項目
     */
    override val bottomMenuItems = prefsFlow.map { it.entryBottomMenuItems }

    /**
     * カテゴリリストの表示形式
     */
    override val categoryListType = prefsFlow.map { it.entryCategoryListType }

    /**
     * 最初に表示するカテゴリ
     */
    override val initialState = MutableStateFlow(EntryNavigationState.default)

    /**
     * 最初に表示するタブ
     */
    override val initialTabs = prefsFlow.map { it.entryInitialTabs }

    /**
     * ドロワの配置
     */
    override val drawerAlignment = prefsFlow.map { it.drawerAlignment }

    /**
     * 長押し時の振動時間
     */
    override val longClickVibrationDuration = prefsFlow.map { it.longClickVibrationDuration }

    /**
     * 日時をシステムのタイムゾーンで表示する
     */
    override val useSystemTimeZone = prefsFlow.map { it.useSystemTimeZone }

    /**
     * 現在有効な検索設定
     */
    override val searchSettingFlow = entriesRepo.searchSettingFlow

    /**
     * 現在有効なマイブクマ検索設定
     */
    override val searchMyBookmarksSettingFlow = entriesRepo.searchMyBookmarksSettingFlow

    /**
     * 既読エントリを記録する
     */
    override val recordReadEntriesEnabled = MutableStateFlow(true)

    // ------ //

    private val clickAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val longClickAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val doubleClickAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val clickEdgeAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val longClickEdgeAction = MutableStateFlow(ClickEntryAction.NOTHING)

    private val doubleClickEdgeAction = MutableStateFlow(ClickEntryAction.NOTHING)

    // ------ //

    /**
     * composeの外部からスクロール操作を伝播するためのフロー
     */
    override val scrollingFlow = MutableSharedFlow<Scroll>()

    // ------ //

    init {
        prefsFlow
            .onEach {
                initialState.value = it.entryInitialState

                clickAction.value = it.clickEntryAction
                longClickAction.value = it.longClickEntryAction
                doubleClickAction.value = it.doubleClickEntryAction

                clickEdgeAction.value = it.clickEntryEdgeAction
                longClickEdgeAction.value = it.longClickEntryEdgeAction
                doubleClickEdgeAction.value = it.doubleClickEntryEdgeAction

                recordReadEntriesEnabled.value = it.recordReadEntriesEnabled
            }
            .launchIn(viewModelScope)

        searchSettingFlow
            .onEach {
                repeat(2) {
                    swipeRefresh(
                        destination = Destination(
                            category = Category.Search,
                            tabIndex = it,
                        )
                    )
                }
            }
            .launchIn(viewModelScope)

        searchMyBookmarksSettingFlow
            .onEach {
                swipeRefresh(
                    destination = Destination(
                        category = Category.SearchMyBookmarks,
                        tabIndex = 0,
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?,
        intent: Intent
    ) {
        this.intent = intent
        lifecycleObserver = LifecycleObserver(activityResultRegistry!!)
        lifecycle?.addObserver(lifecycleObserver)
        // 非同期的に送出された例外を処理する
        lifecycle?.coroutineScope?.let { scope ->
            hatenaRepo.exceptionFlow
                .onEach {
                    when(it) {
                        is HatenaSignInException -> {
                            context.showToast(R.string.pref_account_hatena_msg_sign_in_failure)
                            Log.e("EntriesViewModel", it.stackTraceToString())
                        }

                        is HatenaFetchNgUsersException -> {
                            context.showToast(R.string.pref_fetch_ng_users_failure)
                            Log.e("EntriesViewModel", it.stackTraceToString())
                        }

                        else -> {
                            // 不明なエラー
                            Log.e("EntriesViewModel", it.stackTraceToString())
                        }
                    }
                }
                .launchIn(scope)

            entriesRepo.exceptionFlow
                .onEach {
                    when(it) {
                        is LoadingFailureException -> {
                            context.showToast(R.string.entry_msg_load_entries_failure)
                            Log.e("EntriesViewModel", it.stackTraceToString())
                        }

                        else -> {
                            // 不明なエラー
                            Log.e("EntriesViewModel", it.stackTraceToString())
                        }
                    }
                }
                .launchIn(scope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        entriesRepo.onCleared()
    }

    private var intent : Intent? = null

    // ------ //

    /**
     * トップバーのタイトル
     */
    override fun topBarTitle(category: Category, target: String?) : String {
        return when (category) {
            Category.Site -> target.orEmpty().trimScheme()
            Category.User -> context.getString(R.string.category_user_title, target.orEmpty())
            else -> context.getString(category.textId)
        }
    }

    /**
     * スワイプによるエントリリスト更新
     */
    override fun swipeRefresh(destination: Destination) {
        viewModelScope.launch {
            runCatching {
                entriesRepo.loadEntries(destination)
            }.onFailure {
                when (it) {
                    is IllegalStateException -> {}
                    else -> {
                        context.showToast(R.string.entry_msg_load_entries_failure)
                    }
                }
                Log.e("swipeRefresh", it.stackTraceToString())
            }
        }
    }

    /**
     * 追加ロード
     */
    override fun loadAdditional(destination: Destination) {
        viewModelScope.launch {
            runCatching {
                entriesRepo.additionalLoadEntries(destination)
            }.onFailure {
                context.showToast(R.string.entry_msg_load_entries_failure)
                Log.e("loadAdditional", it.stackTraceToString())
            }
        }
    }

    /**
     * エントリ検索設定を変更し、再検索
     */
    override fun search(searchSetting: SearchSetting) {
        viewModelScope.launch {
            runCatching {
                entriesRepo.updateSearchSetting(searchSetting)
            }
        }
    }

    /**
     * マイブクマ検索設定を変更し、再検索
     */
    override fun searchMyBookmarks(searchSetting: SearchSetting) {
        viewModelScope.launch {
            runCatching {
                entriesRepo.updateSearchMyBookmarksSetting(searchSetting)
            }
        }
    }

    /**
     * ブラウザで検索する
     */
    override fun searchInBrowser(query: String) {
        val url = "https://www.google.com/search?q=${Uri.encode(query)}"
        lifecycleObserver.launchBrowserActivity(url)
    }

    /**
     * 設定画面に遷移
     */
    override fun launchPreferencesActivity() {
        lifecycleObserver.launchPreferencesActivity()
    }

    /**
     * Hatenaのサインイン画面に遷移
     */
    override fun launchHatenaAuthenticationActivity(context: Context) {
        val intent = Intent(context, HatenaAuthenticationActivity::class.java)
        context.startActivity(intent)
    }

    // ------ //

    /**
     * 項目の各種クリックイベントを処理
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
     * 通知を単クリックしたときの挙動
     */
    override fun onClick(notice: Notice) {
        when (notice.verb) {
            NoticeVerb.STAR.str -> {
                lifecycleObserver.launchBookmarksActivity(notice)
            }

            else -> {
                lifecycleObserver.openWithOtherApp(notice.link)
            }
        }
    }

    /**
     * 通知を長クリックしたときの挙動
     */
    override fun onLongClick(notice: Notice) {

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
        lifecycleObserver.openWithOtherApp(entry.entry)
    }

    /**
     * 指定サイトのエントリ一覧を表示する
     */
    override fun navigateSiteCategory(url: String, navController: NavController) {
        navigate(
            navController = navController,
            category = Category.Site,
            target = Uri.encode(url)
        )
    }

    /**
     * 既読マークをつける
     */
    private fun readMarkEntry(entry: DisplayEntry) {
        if (recordReadEntriesEnabled.value) {
            viewModelScope.launch {
                entriesRepo.readMark(entry.entry)
            }
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
                context.showToast(R.string.entry_msg_success_remove_bookmark)
            }.onFailure {
                Log.e("EntriesViewModel", it.stackTraceToString())
                context.showToast(R.string.entry_msg_failure_remove_bookmark)
            }
        }
    }

    /**
     * 「あとで読む」タグをつけてブクマする
     */
    override fun readLaterEntry(entry: DisplayEntry, isPrivate: Boolean) {
        viewModelScope.launch {
            runCatching {
                entriesRepo.readLater(entry.entry, isPrivate)
            }.onSuccess {
                context.showToast(R.string.post_bookmark_success_msg)
            }.onFailure {
                context.showToast(R.string.post_bookmark_failure_msg)
            }
        }
    }

    /**
     * 「読んだ」タグをつけてブクマする
     */
    override fun readEntry(entry: DisplayEntry, isPrivate: Boolean) {
        viewModelScope.launch {
            runCatching {
                entriesRepo.read(entry.entry, isPrivate)
            }.onSuccess {
                context.showToast(R.string.post_bookmark_success_msg)
            }.onFailure {
                context.showToast(R.string.post_bookmark_failure_msg)
            }
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
    override suspend fun insertNgWord(args: NgWordEditionResult) : Boolean {
        val result = runCatching {
            if (isNgRegexError(args.query, args.asRegex)) {
                throw IllegalArgumentException()
            }
            ngWordsRepository.insert(args.toIgnoredEntry())
        }.onSuccess {
            context.showToast(context.getString(
                    R.string.ng_word_setting_msg_insert_success,
                    args.query
                ))
        }.onFailure {
            handleUpdateNgWordErrors(context, it)
        }
        return result.isSuccess
    }

    // ------ //

    /**
     * ボトムメニュー項目をクリックしたときの挙動
     */
    override suspend fun onClickBottomMenuItem(
        item: BottomMenuItem,
        navController: NavController,
        onOpenDrawer: suspend ()->Unit,
        onShowExcludedEntriesBottomSheet: suspend ()->Unit
    ) {
        when (item) {
            BottomMenuItem.SCROLL_TO_TOP -> {
                scrollingFlow.emit(Scroll.ToTop)
            }
            BottomMenuItem.SCROLL_TO_BOTTOM -> {
                scrollingFlow.emit(Scroll.ToBottom)
            }
            BottomMenuItem.EXCLUDED_ENTRIES -> {
                onShowExcludedEntriesBottomSheet()
            }
            BottomMenuItem.MY_BOOKMARKS -> {
                navigate(navController, Category.MyBookmarks)
            }
            BottomMenuItem.NOTICES -> {
                navigate(navController, Category.Notices)
            }
            BottomMenuItem.INNER_BROWSER -> {
                lifecycleObserver.launchBrowserActivity()
            }
            BottomMenuItem.HOME -> {
                navigate(navController, initialState.value.category, clearStack = true)
            }
            BottomMenuItem.SEARCH -> {
                navigate(navController, Category.Search)
            }
            BottomMenuItem.CATEGORIES -> {
                onOpenDrawer()
            }
            BottomMenuItem.PREFERENCES -> {
                launchPreferencesActivity()
            }

            BottomMenuItem.OPEN_OFFICIAL_TOP -> {
                lifecycleObserver.launchBrowserActivity("https://b.hatena.ne.jp/")
            }
            BottomMenuItem.OPEN_OFFICIAL_HATENA -> {
                lifecycleObserver.launchBrowserActivity("https://www.hatena.ne.jp/")
            }
            BottomMenuItem.OPEN_ANONYMOUS_DIARY -> {
                lifecycleObserver.launchBrowserActivity("https://anond.hatelabo.jp/")
            }
        }
    }

    /**
     * ボトムメニュー項目を長クリックしたときの挙動
     */
    override suspend fun onLongClickBottomMenuItem(
        item: BottomMenuItem,
        onShowBrowserBottomSheet: suspend ()->Unit
    ) {
        if (!item.longClickable) throw IllegalArgumentException()
        when (item) {
            BottomMenuItem.INNER_BROWSER -> {
                onShowBrowserBottomSheet()
            }
            else -> throw NotImplementedError()
        }
    }

    // ------ //

    /**
     * エントリリスト
     */
    override fun entriesFlow(destination: Destination) : EntriesListFlow {
        return entriesRepo.entriesFlow(viewModelScope, destination)
    }

    /**
     * 除外されたエントリリスト
     */
    override fun excludedEntriesFlow(destination: Destination) : EntriesListFlow {
        return entriesRepo.excludedEntriesFlow(viewModelScope, destination)
    }

    /**
     * Issueリスト
     */
    override fun issuesFlow(category: Category) : Flow<List<Issue>> {
        return entriesRepo.issuesFlow(viewModelScope, category)
    }

    /**
     * 通知リスト
     */
    override val noticesFlow = entriesRepo.noticesFlow

    /**
     * メンテナンス情報リスト
     */
    override val maintenanceEntriesFlow = entriesRepo.maintenanceEntriesFlow

    // ------ //

    /**
     * 各画面に対応する一意のキーを取得する
     */
    override fun getMapKey(destination: Destination) : String {
        return entriesRepo.makeMapKey(destination)
    }

    // ------ //

    /**
     * カテゴリ遷移
     */
    override fun navigate(
        navController: NavController,
        category: Category,
        issue: Issue?,
        target: String?,
        clearStack: Boolean
    ) {
        val currentCategory = navController.currentArgument<String>("category")
        val currentTarget = navController.currentArgument<String>("target")
        val currentIssue = navController.currentArgument<String>("issue")
        if (
            currentCategory == category.name
            && currentIssue.orEmpty() == issue?.code.orEmpty()
            && currentTarget.orEmpty() == target.orEmpty()
        ) return

        val dest = buildString {
            append("entries/", category.name)
            if (target != null || issue != null) {
                append("?target=", target.orEmpty(), "&issue=", issue?.code.orEmpty())
            }
        }

        navController.navigate(dest) {
            if (clearStack) { popUpTo(0) }
        }
    }

    /**
     * 最初の画面移動
     */
    override fun initialNavigation(
        initialState: EntryNavigationState,
        navController: NavController
    ) {
        val url = intent?.getStringExtra(EntriesActivityContract.EXTRA_URL)
        val user = intent?.getStringExtra(EntriesActivityContract.EXTRA_USER)
        val tag =  intent?.getStringExtra(EntriesActivityContract.EXTRA_TAG)
        val launchNotices = intent?.getBooleanExtra(EntriesActivityContract.EXTRA_LAUNCH_NOTICES, false)

        val (category, target, issue) = when {
            // 通知画面を開くように指定があった場合
            launchNotices == true -> Triple(Category.Notices, null, null)

            // 別画面からサイト指定で呼び出された場合
            url != null -> Triple(Category.Site, url, null)

            // 別画面からユーザー指定で呼び出された場合
            user != null -> Triple(Category.User, user, null)

            // 別画面からタグ指定で呼び出された場合
            tag != null -> Triple(Category.Search, tag, null)

            // 通常時。初期表示カテを表示
            else -> Triple(initialState.category, null, initialState.issue)
        }

        if (tag != null) {
            search(
                SearchSetting(
                    query = tag,
                    searchType = SearchType.TAG
                )
            )
        }

        navigate(
            navController = navController,
            category = category,
            target = target,
            issue = issue,
            clearStack = true
        )
    }

    // ------ //

    /** 他画面とデータをやりとりしながら遷移するためのやつ */
    private lateinit var lifecycleObserver : LifecycleObserver

    inner class LifecycleObserver(
        private val registry : ActivityResultRegistry
    ) : DefaultLifecycleObserver {
        /** [com.suihan74.satena2.scene.preferences.PreferencesActivity]のランチャ */
        private lateinit var preferencesActivityLauncher : ActivityResultLauncher<Unit>

        // ------ //

        /** [com.suihan74.satena2.scene.bookmarks.BookmarksActivity]のランチャ */
        private lateinit var bookmarksActivityLauncher : ActivityResultLauncher<Entry>

        /** [com.suihan74.satena2.scene.bookmarks.BookmarksActivity]のランチャ */
        private lateinit var bookmarksActivityLauncherWithUser : ActivityResultLauncher<Pair<Entry, String>>

        /** [com.suihan74.satena2.scene.bookmarks.BookmarksActivity]のランチャ */
        private lateinit var bookmarksActivityLauncherWithNotice : ActivityResultLauncher<Notice>

        // ------ //

        /** [com.suihan74.satena2.scene.browser.BrowserActivity]のランチャ */
        private lateinit var browserActivityLauncher : ActivityResultLauncher<String?>

        /** 外部アプリのランチャ */
        private lateinit var otherAppActivityLauncher : ActivityResultLauncher<String>

        override fun onCreate(owner: LifecycleOwner) {
            preferencesActivityLauncher = registry.register(
                "PreferencesActivityLauncher",
                owner,
                PreferencesActivityContract.NoArgs()
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

            bookmarksActivityLauncherWithNotice = registry.register(
                "BookmarksActivityLauncherWithNotice",
                owner,
                BookmarksActivityContract.WithNotice()
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

            otherAppActivityLauncher = registry.register(
                "otherAppActivityLauncher",
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

        fun launchPreferencesActivity() { preferencesActivityLauncher.launch() }
        fun launchBookmarksActivity(entry: Entry) { bookmarksActivityLauncher.launch(entry) }
        fun launchBookmarksActivity(entry: Entry, targetUser: String) { bookmarksActivityLauncherWithUser.launch(entry to targetUser) }
        fun launchBookmarksActivity(notice: Notice) { bookmarksActivityLauncherWithNotice.launch(notice) }
        fun launchBrowserActivity(entry: Entry) { browserActivityLauncher.launch(entry.url) }
        fun launchBrowserActivity(url: String) { browserActivityLauncher.launch(url) }
        fun launchBrowserActivity() { browserActivityLauncher.launch(null) }
        fun openWithOtherApp(entry: Entry) { otherAppActivityLauncher.launch(entry.url) }
        fun openWithOtherApp(url: String) { otherAppActivityLauncher.launch(url) }
    }
}

// ------ //

/**
 * Compose Preview用のVM
 */
class FakeEntriesViewModel(
    private val coroutineScope: CoroutineScope? = null,
    private val categoryToString : (Category)->String = { "category" },
) : EntriesViewModel {
    private val dummyLoadingStateFlow = MutableStateFlow(false)

    override fun loadingStateFlow(destination: Destination): StateFlow<Boolean> {
        return dummyLoadingStateFlow
    }

    override val hatenaSignInState : StateFlow<SignInState> = MutableStateFlow(SignInState.None)

    override val hatenaAccount: StateFlow<Account?> = MutableStateFlow(null)

    override val useBottomMenu: Flow<Boolean> = MutableStateFlow(true)

    override val bottomMenuArrangement = MutableStateFlow(Arrangement.Start)

    override val bottomMenuItems = MutableStateFlow(
        listOf(
            BottomMenuItem.SCROLL_TO_TOP,
            BottomMenuItem.INNER_BROWSER
        )
    )

    override val drawerAlignment = MutableStateFlow(Alignment.Start)

    override val longClickVibrationDuration = MutableStateFlow(40L)

    override val useSystemTimeZone = MutableStateFlow(false)

    override val searchSettingFlow = MutableStateFlow(SearchSetting())

    override val searchMyBookmarksSettingFlow = MutableStateFlow(SearchSetting())

    override val recordReadEntriesEnabled = MutableStateFlow(true)

    // ------ //

    /**
     * composeの外部からスクロール操作を伝播するためのフロー
     */
    override val scrollingFlow = MutableSharedFlow<Scroll>()

    // ------ //

    override val categoryListType = MutableStateFlow(EntryCategoryListType.LIST)

    override val initialState = MutableStateFlow(EntryNavigationState.default)

    override val initialTabs = MutableStateFlow(emptyMap<Category, Int>())

    // ------ //

    override fun navigate(
        navController: NavController,
        category: Category,
        issue: Issue?,
        target: String?,
        clearStack: Boolean
    ) {
    }

    override fun initialNavigation(
        initialState: EntryNavigationState,
        navController: NavController
    ) {
    }

    // ------ //

    override fun topBarTitle(category: Category, target: String?): String {
        return categoryToString(category)
    }

    override fun swipeRefresh(destination: Destination) {
        coroutineScope?.launch {
            dummyLoadingStateFlow.value = true
            delay(1000L)
            dummyLoadingStateFlow.value = false
        }
    }

    override fun loadAdditional(destination: Destination) {
        coroutineScope?.launch {
            dummyLoadingStateFlow.value = true
            delay(1000L)
            dummyLoadingStateFlow.value = false
        }
    }

    override fun search(searchSetting: SearchSetting) {
        searchSettingFlow.value = searchSetting
    }

    override fun searchMyBookmarks(searchSetting: SearchSetting) {
        searchMyBookmarksSettingFlow.value = searchSetting
    }

    override fun searchInBrowser(query: String) {
    }

    override fun launchPreferencesActivity() {}

    override fun launchHatenaAuthenticationActivity(context: Context) {}

    // ------ //

    override fun onEvent(
        entry: DisplayEntry,
        event: EntryItemEvent,
        onShowMenu: (DisplayEntry) -> Unit,
        onShare: (DisplayEntry) -> Unit
    ) {
    }

    // ------ //

    override fun onClick(notice: Notice) {
    }

    override fun onLongClick(notice: Notice) {
    }


    // ------ //

    override fun launchBookmarksActivity(entry: DisplayEntry) {
    }

    override fun launchBrowserActivity(entry: DisplayEntry) {
    }

    override fun openWithOtherApp(entry: DisplayEntry) {
    }

    override fun navigateSiteCategory(url: String, navController: NavController) {
    }

    override fun readLaterEntry(entry: DisplayEntry, isPrivate: Boolean) {
    }

    override fun readEntry(entry: DisplayEntry, isPrivate: Boolean) {
    }

    override fun removeReadMark(entry: DisplayEntry) {
    }

    override fun removeBookmark(entry: DisplayEntry) {
    }

    // ------ //

    override fun onClickComment(entry: DisplayEntry, bookmark: BookmarkResult) {
    }

    override fun onLongClickComment(entry: DisplayEntry, bookmark: BookmarkResult) {
    }

    // ------ //

    override suspend fun insertNgWord(args: NgWordEditionResult): Boolean = true

    // ------ //

    override suspend fun onClickBottomMenuItem(
        item: BottomMenuItem,
        navController: NavController,
        onOpenDrawer: suspend () -> Unit,
        onShowExcludedEntriesBottomSheet: suspend ()->Unit
    ) {
    }

    override suspend fun onLongClickBottomMenuItem(
        item: BottomMenuItem,
        onShowBrowserBottomSheet: suspend ()->Unit
    ) {
    }

    // ------ //

    override fun entriesFlow(destination: Destination) : EntriesListFlow = MutableStateFlow(emptyList())

    override fun excludedEntriesFlow(destination: Destination) : EntriesListFlow = MutableStateFlow(emptyList())

    override fun issuesFlow(category: Category): Flow<List<Issue>> = MutableStateFlow(emptyList())

    override val noticesFlow = MutableStateFlow(emptyList<Notice>())

    override val maintenanceEntriesFlow = MutableStateFlow<List<MaintenanceEntry>>(emptyList())

    // ------ //

    /**
     * 各画面に対応する一意のキーを取得する
     */
    override fun getMapKey(destination: Destination) : String {
        return ""
    }
}

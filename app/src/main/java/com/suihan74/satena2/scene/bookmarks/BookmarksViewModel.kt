package com.suihan74.satena2.scene.bookmarks

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Alignment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.model.account.Account
import com.suihan74.hatena.model.account.Notice
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.bookmark.Report
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.R
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.model.userLabel.UserAndLabels
import com.suihan74.satena2.scene.browser.BrowserActivityContract
import com.suihan74.satena2.scene.entries.EntriesActivityContract
import com.suihan74.satena2.scene.post.EditData
import com.suihan74.satena2.scene.post.PostBookmarkActivityContract
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelRepository
import com.suihan74.satena2.utility.DialogPropertiesProvider
import com.suihan74.satena2.utility.DialogPropertiesProviderImpl
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.getObjectExtra
import com.suihan74.satena2.utility.extension.showToast
import com.suihan74.satena2.utility.hatena.hatenaUserIconUrl
import com.suihan74.satena2.utility.hatena.toBookmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

interface BookmarksViewModel : DialogPropertiesProvider {
    /**
     * リスト更新の実行状態
     */
    val loadingFlow : StateFlow<Boolean>

    /**
     * 続きを読み込めるか否か
     */
    val additionalLoadableFlow : StateFlow<Boolean>

    /**
     * サインイン中のHatenaのアカウント
     */
    val hatenaAccountFlow : Flow<Account?>

    /**
     * ドロワの配置
     */
    val drawerAlignmentFlow : Flow<Alignment.Horizontal>

    /**
     * タブ長押しで初期タブを変更する
     */
    val changeInitialTabByLongClickFlow : Flow<Boolean>

    /**
     * 長押し時の振動時間
     */
    val longClickVibrationDuration : Flow<Long>

    /**
     * 日時をシステムのタイムゾーンで表示する
     */
    val useSystemTimeZone : Flow<Boolean>

    /**
     * 既読エントリを記録する
     */
    val recordReadEntriesEnabled : StateFlow<Boolean>

    // ------ //

    /**
     * 現在表示中ページのエントリ・ブクマ情報
     */
    val entityFlow : Flow<Entity>

    /**
     * 現在表示中ページのユーザーブクマ
     */
    val myBookmarkFlow : StateFlow<DisplayBookmark?>

    /**
     * ブコメ検索クエリ
     */
    val searchQueryFlow : StateFlow<String>

    /**
     * 「新着」ブクマリスト
     */
    val recentBookmarksFlow : StateFlow<List<DisplayBookmark>>

    /**
     * 「すべて」ブクマリスト
     */
    val allBookmarksFlow : StateFlow<List<DisplayBookmark>>

    /**
     * 「人気」ブクマリスト
     */
    val popularBookmarksFlow : StateFlow<List<DisplayBookmark>>

    /**
     * 「フォロー」ブクマリスト
     */
    val followingBookmarksFlow : StateFlow<List<DisplayBookmark>>

    /**
     * 「カスタム」ブクマリスト
     */
    val customBookmarksFlow : StateFlow<List<DisplayBookmark>>

    /**
     * 「カスタム」タブの表示対象
     */
    val customTabSettingFlow : StateFlow<CustomTabSetting>

    // ------ //

    /**
     * すべてのユーザーラベル
     */
    val allUserLabelsFlow : StateFlow<List<Label>>

    // ------ //

    /**
     * アクティビティへの紐づけが必要な処理の準備
     */
    fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    )

    /**
     * [BookmarksActivity]で利用。[Intent]からブクマ情報をロードする
     */
    suspend fun load(intent: Intent, navController: NavController)

    /**
     * [com.suihan74.satena2.scene.browser.BrowserActivity]で利用。URLからブクマ情報をロードする
     */
    fun load(url: String)

    /**
     * タブの初期化
     */
    @OptIn(ExperimentalFoundationApi::class)
    suspend fun initializeTab(pagerState: PagerState)

    /**
     * リストを更新
     */
    fun refresh(tab: BookmarksTab)

    /**
     * リストの続きを取得
     */
    fun loadAdditional(tab: BookmarksTab)

    /**
     * 指定ユーザーのブクマを取得
     */
    fun getUserBookmarkFlow(user: String) : Flow<DisplayBookmark?>

    /**
     * 指定ユーザーへのメンションを取得する
     */
    fun getMentionsTo(user: String) : Flow<List<DisplayBookmark>>

    /**
     * ブコメ検索クエリを登録する
     */
    fun setSearchQuery(query: String)

    /**
     * ブクマを通報する
     */
    suspend fun report(report: Report)

    /**
     * 「カスタム」タブの設定を更新する
     */
    fun updateCustomTabSetting(setting: CustomTabSetting)

    /**
     * 自分のブクマを削除する
     */
    suspend fun deleteMyBookmark() : Boolean

    // ------ //

    /**
     * タイトルバークリック時の処理
     */
    fun onClickTitleBar()

    /**
     * 投稿アクティビティを開く
     */
    fun openPostActivity()

    /**
     * アプリ内ブラウザで開く
     */
    fun openBrowser(url: String)

    /**
     * 上階を開く
     */
    fun upStair()

    /**
     * 下階を開く
     */
    fun downStair()

    /**
     * タブを長押し
     */
    fun onLongClickTab(tab: BookmarksTab)

    /**
     * ブコメ中のリンクをクリックした時の処理
     */
    fun onClickLink(url: String)

    // ------ //

    /**
     * ブクマ項目をクリックしたときの処理
     */
    fun onClick(bookmark: DisplayBookmark, navController: NavController)

    /**
     * ブクマ項目を長押ししたときの処理
     */
    fun onLongClick(bookmark: DisplayBookmark, onShowBookmarkItemMenu: (DisplayBookmark)->Unit)
    /**
     * ブクマの詳細を表示する
     */
    fun showBookmarkDetail(user: String, navController: NavController)

    /**
     * ユーザーを非表示にする/非表示を解除する
     */
    suspend fun toggleIgnore(user: String)

    // ------ //

    /**
     * 指定ユーザーにつけられたスターとそのスターをつけたユーザーのブクマを取得する
     */
    fun starsTo(item: DisplayBookmark) : StateFlow<List<Mention>>

    // ------ //

    /**
     * ユーザーにつけられたラベルを取得する
     */
    fun userLabelsFlow(user: String) : Flow<UserAndLabels?>

    /**
     * ユーザーラベルを更新する
     */
    fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>)

    /**
     * ユーザーラベルを作成する
     */
    suspend fun createUserLabel(label: Label) : Boolean

    // ------ //

    /**
     * 指定ユーザーが最近ブクマしたエントリ一覧画面に遷移する
     */
    fun launchEntriesActivityForUser(item: DisplayBookmark)

    /**
     * 指定タグで検索したエントリ一覧画面に遷移する
     */
    fun launchEntriesActivityForTag(tag: String)

    /**
     * ブクマに対するブクマ一覧を開く
     */
    fun launchBookmarksActivityToBookmark(item: DisplayBookmark)
}

// ------ //

@HiltViewModel
class BookmarksViewModelImpl @Inject constructor(
    private val repository: BookmarksRepository,
    private val userLabelRepo: UserLabelRepository,
    hatenaRepo: HatenaAccountRepository,
    private val prefsRepo: PreferencesRepository,
) :
    ViewModel(),
    BookmarksViewModel,
    DialogPropertiesProvider by DialogPropertiesProviderImpl(prefsRepo.dataStore)
{
    /**
     * リスト更新の実行状態
     */
    override val loadingFlow = repository.loadingFlow

    /**
     * 続きを読み込めるか否か
     */
    override val additionalLoadableFlow = repository.additionalLoadableFlow

    /**
     * サインイン中のHatenaのアカウント情報
     */
    override val hatenaAccountFlow = hatenaRepo.account

    /**
     * ドロワの配置
     */
    override val drawerAlignmentFlow = prefsRepo.dataStore.data.map { it.drawerAlignment }

    /**
     * タブ長押しで初期タブを変更する
     */
    override val changeInitialTabByLongClickFlow = prefsRepo.dataStore.data.map { it.bookmarkChangeInitialTabByLongClick }

    private val changeInitialTabFlow = MutableSharedFlow<BookmarksTab>()

    /**
     * 長押し時の振動時間
     */
    override val longClickVibrationDuration = prefsRepo.dataStore.data.map { it.longClickVibrationDuration }

    /**
     * 日時をシステムのタイムゾーンで表示する
     */
    override val useSystemTimeZone = prefsRepo.dataStore.data.map { it.useSystemTimeZone }

    /**
     * 既読エントリを記録する
     */
    override val recordReadEntriesEnabled = MutableStateFlow(true)

    // ------ //

    /**
     * 現在表示中ページのエントリ・ブクマ情報
     */
    override val entityFlow = repository.entityFlow

    /**
     * 現在表示中ページのユーザーブクマ
     */
    override val myBookmarkFlow = repository.myBookmarkFlow

    /**
     * ブコメ検索クエリ
     */
    override val searchQueryFlow = MutableStateFlow("")

    /**
     * 「新着」ブクマリスト
     */
    override val recentBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    /**
     * 「すべて」ブクマリスト
     */
    override val allBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    /**
     * 「人気」ブクマリスト
     */
    override val popularBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    /**
     * 「フォロー」ブクマリスト
     */
    override val followingBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    /**
     * 「カスタム」ブクマリスト
     */
    override val customBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    /**
     * 「カスタム」タブの表示対象
     */
    override val customTabSettingFlow = repository.customTabSettingFlow

    // ------ //

    /**
     * すべてのユーザーラベル
     */
    override val allUserLabelsFlow = userLabelRepo.allUserLabelsFlow.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    // ------ //

    private val tabInitialized = MutableStateFlow(false)

    /**
     * 編集途中のブコメ内容
     */
    private val editDataFlow = MutableStateFlow<EditData?>(null)

    // ------ //

    init {
        repository.initialize(viewModelScope)

        // 各表示用リストへの反映
        combine(repository.recentBookmarksFlow, searchQueryFlow) { list, q ->
            recentBookmarksFlow.value = queryFilterBookmarks(list, q)
        }.launchIn(viewModelScope)

        combine(repository.allBookmarksFlow, searchQueryFlow) { list, q ->
            allBookmarksFlow.value = queryFilterBookmarks(list, q)
        }.launchIn(viewModelScope)

        combine(repository.popularBookmarksFlow, searchQueryFlow) { list, q ->
            popularBookmarksFlow.value = queryFilterBookmarks(list, q)
        }.launchIn(viewModelScope)

        combine(repository.followingBookmarksFlow, searchQueryFlow) { list, q ->
            followingBookmarksFlow.value = queryFilterBookmarks(list, q)
        }.launchIn(viewModelScope)

        combine(repository.customBookmarksFlow, searchQueryFlow) { list, q ->
            customBookmarksFlow.value = queryFilterBookmarks(list, q)
        }.launchIn(viewModelScope)

        // タブ長押しで初期タブを変更
        combine(changeInitialTabByLongClickFlow, changeInitialTabFlow, ::Pair)
            .onEach { (enabled, tab) ->
                if (enabled) {
                    prefsRepo.dataStore.updateData {
                        if (it.bookmarkInitialTab != tab) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.bookmark_tab_changed_msg, context.getString(tab.textId)),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        it.copy(bookmarkInitialTab = tab)
                    }
                }
            }
            .launchIn(viewModelScope)

        // 設定反映
        prefsRepo.dataStore.data
            .onEach {
                recordReadEntriesEnabled.value = it.recordReadEntriesEnabled
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * アクティビティへの紐づけが必要な処理の準備
     */
    override fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
        lifecycleObserver = LifecycleObserver(activityResultRegistry!!)
        lifecycle?.addObserver(lifecycleObserver)
    }

    /**
     * [BookmarksActivity]で利用。[Intent]からブクマ情報をロードする
     */
    override suspend fun load(intent: Intent, navController: NavController) {
        val entry = intent.getObjectExtra<Entry>(BookmarksActivityContract.EXTRA_ENTRY)
        val eid = intent.getLongExtra(BookmarksActivityContract.EXTRA_ENTRY_ID, 0L)
        val url = intent.getStringExtra(BookmarksActivityContract.EXTRA_URL)
        val notice = intent.getObjectExtra<Notice>(BookmarksActivityContract.EXTRA_NOTICE)
        val targetUser = intent.getStringExtra(BookmarksActivityContract.EXTRA_TARGET_USER) ?: notice?.user

        repository.load(entry, eid, url, notice)
        targetUser?.let {
            showBookmarkDetail(it, navController)
        }
    }

    /**
     * [com.suihan74.satena2.scene.browser.BrowserActivity]で利用。URLからブクマ情報をロードする
     */
    override fun load(url: String) {
        viewModelScope.launch {
            repository.load(url = url)
        }
    }

    /**
     * タブの初期化
     */
    @OptIn(ExperimentalFoundationApi::class)
    override suspend fun initializeTab(pagerState: PagerState) {
        prefsRepo.dataStore.data.map { it.bookmarkInitialTab }
            .collect {
                if (!tabInitialized.value) {
                    tabInitialized.value = true
                    pagerState.scrollToPage(it.ordinal)
                }
            }
    }

    /**
     * リストを更新
     */
    override fun refresh(tab: BookmarksTab) {
        viewModelScope.launch {
            repository.refresh(entityFlow.value, tab)
        }
    }

    /**
     * リストの続きを取得
     */
    override fun loadAdditional(tab: BookmarksTab) {
        viewModelScope.launch {
            repository.loadAdditional(entityFlow.value, tab)
        }
    }

    /**
     * 指定ユーザーのブクマを取得
     *
     * todo: リポジトリ側でキャッシュするようにする
     */
    override fun getUserBookmarkFlow(user: String) : Flow<DisplayBookmark?> {
        return entityFlow.map { entity ->
            val myBookmark = entity.entry.bookmarkedData
            val bookmark = entity.bookmarksDigest.scoredBookmarks.firstOrNull { it.user == user }
                ?: entity.bookmarksDigest.favoriteBookmarks.firstOrNull { it.user == user }
                ?: entity.bookmarks.firstOrNull { it.user == user }
                ?: (if (myBookmark?.user == user) myBookmark else null)?.toBookmark()
                ?: entity.bookmarksEntry.bookmarks.firstOrNull { it.user == user }?.toBookmark(entity.entry.eid)
            bookmark?.let { repository.makeDisplayBookmark(bookmark = it, eid = entity.entry.eid) }
        }
    }

    /**
     * 指定ユーザーへのメンションを取得する
     *
     * todo: リポジトリ側でキャッシュするようにする
     */
    override fun getMentionsTo(user: String) : Flow<List<DisplayBookmark>> {
        val mentionText = "id:$user"
        return entityFlow.map { entity ->
            entity.bookmarksEntry.bookmarks
                .filter {
                    it.comment.contains(mentionText)
                }
                .map {
                    repository.makeDisplayBookmark(
                        bookmark = it.toBookmark(entity.entry.eid),
                        eid = entity.entry.eid
                    )
                }
        }
    }

    /**
     * ブコメ検索クエリを登録する
     */
    override fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    /**
     * 検索クエリが有効な場合にブクマリストをフィルタする
     */
    private fun queryFilterBookmarks(
        list: List<DisplayBookmark>,
        query: String
    ) : List<DisplayBookmark> {
        return if (query.isBlank()) list
        else list.filter { b ->
            b.bookmark.comment.contains(query) || b.bookmark.user.contains(query) || b.bookmark.tags.any { it.contains(query) }
        }
    }

    /**
     * ブクマを通報する
     */
    override suspend fun report(report: Report) {
        runCatching {
            repository.report(report)
        }.onSuccess {
            context.showToast(R.string.report_bookmark_success_msg)
        }.onFailure {
            context.showToast(R.string.report_bookmark_failure_msg)
            Log.e("ReportBookmark", it.stackTraceToString())
            throw it
        }
    }

    /**
     * 「カスタム」タブの設定を更新する
     */
    override fun updateCustomTabSetting(setting: CustomTabSetting) {
        viewModelScope.launch {
            prefsRepo.dataStore.updateData {
                it.copy(
                    bookmarkCustomTabSetting = setting
                )
            }
        }
    }

    /**
     * 自分のブクマを削除する
     */
    override suspend fun deleteMyBookmark() : Boolean {
        return withContext(viewModelScope.coroutineContext) {
            runCatching {
                repository.deleteMyBookmark()
            }.onSuccess {
                context.showToast(R.string.bookmark_deletion_succeeded_msg)
            }.onFailure {
                context.showToast(R.string.bookmark_deletion_failure_msg)
            }.isSuccess
        }
    }

    // ------ //

    /**
     * タイトルバークリック時の処理
     */
    override fun onClickTitleBar() {
        // todo
        openBrowser(entityFlow.value.entry.url)
    }

    /**
     * 投稿アクティビティを開く
     */
    override fun openPostActivity() {
        lifecycleObserver.launchPostBookmarksActivity()
    }

    /**
     * アプリ内ブラウザで開く
     */
    override fun openBrowser(url: String) {
        Regex("""id:entry:(\d+)""").matchEntire(url)?.let {
            viewModelScope.launch {
                runCatching {
                    val eid = it.groups[1]!!.value.toLong()
                    val actualUrl = repository.getPageUrl(eid)
                    lifecycleObserver.launchBrowserActivity(actualUrl)
                }.onFailure {
                    // todo
                    context.showToast("$url を開けませんでした")
                }
            }
        } ?: run {
            lifecycleObserver.launchBrowserActivity(url)
        }
    }

    /**
     * 上階を開く
     */
    override fun upStair() {
        lifecycleObserver.launchBookmarksActivity(entityFlow.value.upperStairUrl)
    }

    /**
     * 下階を開く
     */
    override fun downStair() {
        entityFlow.value.lowerStairUrl?.let {
            lifecycleObserver.launchBookmarksActivity(it)
        }
    }

    /**
     * タブを長押し
     */
    override fun onLongClickTab(tab: BookmarksTab) {
        viewModelScope.launch {
            changeInitialTabFlow.emit(tab)
        }
    }

    /**
     * ブコメ中のリンクをクリックした時の処理
     */
    override fun onClickLink(url: String) {
        openBrowser(url)
    }

    // ------ //

    /**
     * ブクマ項目をクリックしたときの処理
     */
    override fun onClick(bookmark: DisplayBookmark, navController: NavController) {
        showBookmarkDetail(bookmark.bookmark.user, navController)
    }

    /**
     * ブクマ項目を長押ししたときの処理
     */
    override fun onLongClick(bookmark: DisplayBookmark, onShowBookmarkItemMenu: (DisplayBookmark)->Unit) {
        onShowBookmarkItemMenu(bookmark)
    }

    // ------ //

    /**
     * 指定ユーザーが最近ブクマしたエントリ一覧画面に遷移する
     */
    override fun launchEntriesActivityForUser(item: DisplayBookmark) {
        lifecycleObserver.launchEntriesActivityWithUser(
            item.bookmark.user
        )
    }

    /**
     * 指定タグで検索したエントリ一覧画面に遷移する
     */
    override fun launchEntriesActivityForTag(tag: String) {
        lifecycleObserver.launchEntriesActivityWithTag(tag)
    }

    /**
     * ブクマに対するブクマ一覧を開く
     */
    override fun launchBookmarksActivityToBookmark(item: DisplayBookmark) {
        val entry = entityFlow.value.entry
        lifecycleObserver.launchBookmarksActivity(
            "${HatenaClient.baseUrlB}entry/${entry.eid}/comment/${item.bookmark.user}"
        )
    }

    /**
     * ブクマの詳細を表示する
     */
    override fun showBookmarkDetail(user: String, navController: NavController) {
        navController.navigate("detail/${user}")
    }

    /**
     * ユーザーを非表示にする/非表示を解除する
     */
    override suspend fun toggleIgnore(user: String) {
        if (repository.isIgnored(user)) {
            runCatching {
                repository.unIgnoreUser(user)
            }.onSuccess {
                context.showToast(
                    text = context.getString(R.string.unmute_user_succeeded_msg, user)
                )
            }.onFailure {
                context.showToast(R.string.unmute_user_failure_msg)
            }
        }
        else {
            runCatching {
                repository.ignoreUser(user)
            }.onSuccess {
                context.showToast(
                    text = context.getString(R.string.mute_user_succeeded_msg, user)
                )
            }.onFailure {
                context.showToast(R.string.mute_user_failure_msg)
            }
        }
    }

    // ------ //

    /**
     * 指定ユーザーにつけられたスターとそのスターをつけたユーザーのブクマを取得する
     */
    override fun starsTo(item: DisplayBookmark) : StateFlow<List<Mention>> {
        return repository.starsTo(
            entity = entityFlow.value,
            item = item,
            coroutineScope = viewModelScope
        )
    }

    // ------ //

    /**
     * ユーザーにつけられたラベルを取得する
     */
    override fun userLabelsFlow(user: String) : Flow<UserAndLabels?> {
        return userLabelRepo.userLabelsFlow(user)
    }

    /**
     * ユーザーラベルを更新する
     */
    override fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>) {
        viewModelScope.launch {
            runCatching {
                userLabelRepo.updateUserLabels(user, states)
            }.onSuccess {
                context.showToast(
                    context.getString(R.string.set_user_label_success, user)
                )
            }
        }
    }

    /**
     * ユーザーラベルを作成する
     */
    override suspend fun createUserLabel(label: Label) : Boolean {
        val result = runCatching {
            userLabelRepo.createLabel(label)
        }.onSuccess {
            context.showToast(
                context.getString(R.string.register_user_label_success, label.name)
            )
        }.onFailure {
            context.showToast(
                context.getString(R.string.register_user_label_failure, label.name)
            )
        }
        return result.isSuccess
    }

    // ------ //

    private lateinit var lifecycleObserver : LifecycleObserver

    inner class LifecycleObserver(private val registry : ActivityResultRegistry) : DefaultLifecycleObserver {
        private lateinit var entriesActivityWithUserLauncher : ActivityResultLauncher<String>

        private lateinit var entriesActivityWithTagLauncher : ActivityResultLauncher<String>

        private lateinit var bookmarksActivityLauncher : ActivityResultLauncher<String>

        private lateinit var postBookmarksActivityLauncher : ActivityResultLauncher<Pair<Entry, EditData?>>

        private lateinit var browserActivityLauncher : ActivityResultLauncher<String?>

        // ------ //

        override fun onCreate(owner: LifecycleOwner) {
            entriesActivityWithUserLauncher = registry.register(
                "EntriesActivityLauncherWithUser",
                owner,
                EntriesActivityContract.WithUser()
            ) { /* do nothing */ }

            entriesActivityWithTagLauncher = registry.register(
                "EntriesActivityLauncherWithTag",
                owner,
                EntriesActivityContract.WithTag()
            ) { /* do nothing */ }

            bookmarksActivityLauncher = registry.register(
                "BookmarksActivityLauncher",
                owner,
                BookmarksActivityContract.WithUrl()
            ) { /* TODO: 更新結果を伝播させる？ */ }

            postBookmarksActivityLauncher = registry.register(
                "PostBookmarksActivityLauncher",
                owner,
                PostBookmarkActivityContract()
            ) { (editData, result) ->
                editDataFlow.value = editData
                result?.let { r ->
                    viewModelScope.launch {
                        repository.updateMyBookmark(r)
                    }
                }
            }

            browserActivityLauncher = registry.register(
                "BrowserActivityLauncher",
                owner,
                BrowserActivityContract()
            ) { /* do nothing */ }
        }

        fun launchEntriesActivityWithUser(user: String) {
            entriesActivityWithUserLauncher.launch(user)
        }

        fun launchEntriesActivityWithTag(tag: String) {
            entriesActivityWithTagLauncher.launch(tag)
        }

        fun launchBookmarksActivity(url: String) {
            bookmarksActivityLauncher.launch(url)
        }

        fun launchPostBookmarksActivity() {
            postBookmarksActivityLauncher.launch(entityFlow.value.entry to editDataFlow.value)
        }

        fun launchBrowserActivity(url: String) {
            browserActivityLauncher.launch(url)
        }
    }
}

// ------ //

class FakeBookmarksViewModel : BookmarksViewModel {
    override val loadingFlow = MutableStateFlow(false)

    override val additionalLoadableFlow = MutableStateFlow(false)

    override val hatenaAccountFlow = MutableStateFlow(null)

    override val drawerAlignmentFlow = MutableStateFlow(Alignment.Start)

    override val changeInitialTabByLongClickFlow = MutableStateFlow(true)

    override val longClickVibrationDuration = MutableStateFlow(40L)

    override val useSystemTimeZone = MutableStateFlow(false)

    override val recordReadEntriesEnabled = MutableStateFlow(true)

    // ------ //

    override val entityFlow = MutableStateFlow(Entity.EMPTY)

    override val myBookmarkFlow = MutableStateFlow<DisplayBookmark?>(null)

    override val searchQueryFlow = MutableStateFlow("")

    override val recentBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    override val allBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    override val popularBookmarksFlow = MutableStateFlow(
        listOf(
            DisplayBookmark(
                bookmark = Bookmark(
                    _user = Bookmark.User(name = "suihan74", hatenaUserIconUrl("suihan74")),
                    comment = "test",
                    isPrivate = false,
                    link = "",
                    tags = emptyList(),
                    timestamp = Instant.now(),
                    starCount = emptyList()
                ),
            )
        )
    )

    override val followingBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    override val customBookmarksFlow = MutableStateFlow(emptyList<DisplayBookmark>())

    override val customTabSettingFlow = MutableStateFlow(CustomTabSetting())

    // ------ //

    override val allUserLabelsFlow = MutableStateFlow(emptyList<Label>())

    // ------ //

    /**
     * アクティビティへの紐づけが必要な処理の準備
     */
    override fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
    }

    /**
     * [BookmarksActivity]で利用。[Intent]からブクマ情報をロードする
     */
    override suspend fun load(intent: Intent, navController: NavController) {
    }

    /**
     * [com.suihan74.satena2.scene.browser.BrowserActivity]で利用。URLからブクマ情報をロードする
     */
    override fun load(url: String) {
    }

    @OptIn(ExperimentalFoundationApi::class)
    override suspend fun initializeTab(pagerState: PagerState) {
    }

    override fun refresh(tab: BookmarksTab) {
    }

    override fun loadAdditional(tab: BookmarksTab) {
    }

    /**
     * 指定ユーザーのブクマを取得
     */
    override fun getUserBookmarkFlow(user: String) : Flow<DisplayBookmark?> {
        return MutableStateFlow(null)
    }

    /**
     * 指定ユーザーへのメンションを取得する
     */
    override fun getMentionsTo(user: String) : Flow<List<DisplayBookmark>> {
        return MutableStateFlow(emptyList())
    }

    override fun setSearchQuery(query: String) {
    }

    // ------ //

    override fun onClickTitleBar() {
    }

    override fun openPostActivity() {
    }

    override fun openBrowser(url: String) {
    }

    override fun upStair() {
    }

    override fun downStair() {
    }

    override fun onLongClickTab(tab: BookmarksTab) {
    }

    override fun onClickLink(url: String) {
    }

    override fun onClick(bookmark: DisplayBookmark, navController: NavController) {
        navController.navigate("detail/${bookmark.bookmark.user}")
    }

    override fun onLongClick(bookmark: DisplayBookmark, onShowBookmarkItemMenu: (DisplayBookmark)->Unit) {
    }

    override fun showBookmarkDetail(user: String, navController: NavController) {
    }

    override suspend fun toggleIgnore(user: String) {
    }

    override suspend fun report(report: Report) {
    }

    override suspend fun deleteMyBookmark() : Boolean {
        return true
    }

    override fun updateCustomTabSetting(setting: CustomTabSetting) {
    }

    // ------ //

    override fun starsTo(item: DisplayBookmark) : StateFlow<List<Mention>> {
        return MutableStateFlow(emptyList())
    }

    // ------ //

    override fun userLabelsFlow(user: String) : StateFlow<UserAndLabels?> {
        return MutableStateFlow<UserAndLabels?>(null)
    }

    override fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>) {
    }

    override suspend fun createUserLabel(label: Label) : Boolean {
        return true
    }

    // ------ //

    override fun launchEntriesActivityForUser(item: DisplayBookmark) {
    }

    override fun launchEntriesActivityForTag(tag: String) {
    }

    override fun launchBookmarksActivityToBookmark(item: DisplayBookmark) {
    }
}

package com.suihan74.satena2.scene.entries

import android.util.Log
import androidx.datastore.core.DataStore
import com.suihan74.hatena.CertifiedHatenaClient
import com.suihan74.hatena.HatenaClientBase
import com.suihan74.hatena.model.account.Notice
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.EntriesType
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.entry.EntryItem
import com.suihan74.hatena.model.entry.Issue
import com.suihan74.hatena.model.entry.IssueEntry
import com.suihan74.hatena.model.entry.MaintenanceEntry
import com.suihan74.hatena.model.entry.MyHotEntry
import com.suihan74.hatena.model.entry.UserEntry
import com.suihan74.hatena.model.star.Star
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.entries.ReadEntry
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.scene.entries.bottomSheet.SearchSetting
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.utility.extension.onNot
import com.suihan74.satena2.utility.hatena.actualUrl
import com.suihan74.satena2.utility.hatena.hatenaUserIconUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

typealias EntriesListFlow = StateFlow<List<DisplayEntry>>

// ------ //

/**
 * ロード失敗時に送出する例外
 */
class LoadingFailureException(
    message: String? = null,
    cause: Throwable? = null
) : Throwable(message, cause)

// ------ //

interface EntriesRepository {
    /**
     * エントリ用のフィルタ設定
     */
    val filtersFlow : Flow<List<IgnoredEntry>>

    /**
     * 通知リスト
     */
    val noticesFlow : StateFlow<List<Notice>>

    /**
     * メンテナンス情報リスト
     */
    val maintenanceEntriesFlow : StateFlow<List<MaintenanceEntry>>

    /**
     * 現在有効な検索設定
     */
    val searchSettingFlow : StateFlow<SearchSetting>

    /**
     * 現在有効なマイブクマ検索設定
     */
    val searchMyBookmarksSettingFlow : StateFlow<SearchSetting>

    /**
     * マイブクマ用の検索クエリ
     */
    val myBookmarkSearchQuery : StateFlow<String>

    /**
     * ロード関係エラーの非同期的な送出
     */
    val exceptionFlow : Flow<Throwable>

    // ------ //

    /**
     * ViewModel#onCleared時にエントリのキャッシュをクリアする
     */
    fun onCleared()

    // ------ //

    fun entriesFlow(
        coroutineScope: CoroutineScope,
        destination: Destination
    ) : EntriesListFlow

    fun excludedEntriesFlow(
        coroutineScope: CoroutineScope,
        destination: Destination
    ) : EntriesListFlow

    suspend fun loadEntries(destination: Destination)

    suspend fun additionalLoadEntries(destination: Destination)

    /**
     * 検索設定を更新
     */
    suspend fun updateSearchSetting(searchSetting: SearchSetting)

    /**
     * マイブクマ検索設定を更新
     */
    suspend fun updateSearchMyBookmarksSetting(searchSetting: SearchSetting)

    // ------ //

    /**
     * 指定カテゴリのIssueリストを取得する
     */
    fun issuesFlow(coroutineScope: CoroutineScope, category: Category) : Flow<List<Issue>>

    // ------ //

    /**
     * 指定エントリを新たに与えられたものに差し替える
     */
    suspend fun updateEntry(entry: Entry)

    /**
     * 指定IDのエントリを取得する
     */
    suspend fun getEntry(eid: Long) : Entry

    /**
     * 指定エントリを「あとで読む」タグをつけてブクマする
     */
    suspend fun readLater(entry: Entry, isPrivate: Boolean)

    /**
     * 指定エントリを「読んだ」タグをつけてブクマする
     */
    suspend fun read(entry: Entry, isPrivate: Boolean)

    /**
     * 指定エントリに既読マークをつける
     */
    suspend fun readMark(entry: Entry)

    /**
     * 指定エントリの既読マークを消す
     */
    suspend fun removeReadMark(entry: Entry)

    /**
     * ブクマを削除する
     */
    suspend fun removeBookmark(entry: Entry)

    // ------ //

    /**
     * カテゴリ・タブに対応するエントリキャッシュの参照キーを取得する
     */
    fun makeMapKey(
        category: Category,
        tabIndex: Int,
        target: String?
    ) : String = "${category.name}__${target.orEmpty()}__${tabIndex}"

    /**
     * カテゴリ・Issue・タブに対応するエントリキャッシュの参照キーを取得する
     */
    fun makeMapKey(destination: Destination) : String =
        destination.run {
            issue?.let {
                "${category.name}__${issue.code}__${target.orEmpty()}__${tabIndex}"
            } ?: makeMapKey(category, tabIndex, target)
        }

    // ------ //

    /**
     * 各画面ごとのロード中かどうかの状態を取得する
     */
    fun getLoadingState(destination: Destination) : StateFlow<Boolean>
}

// ------ //

class EntriesRepositoryImpl @Inject constructor(
    appDatabase: AppDatabase,
    private val hatenaRepo: HatenaAccountRepository,
    dataStore: DataStore<Preferences>
) : EntriesRepository {
    private val ignoredEntryDao = appDatabase.ignoredEntryDao()

    private val readEntryDao = appDatabase.readEntryDao()

    private val noticeDao = appDatabase.noticeDao()

    /**
     * エントリリストのキャッシュ
     */
    private val rawEntriesMap = HashMap<String, MutableStateFlow<List<Entry>>>()
    private val allEntriesMap = HashMap<String, Flow<List<DisplayEntry>>>()
    private val validEntriesMap = HashMap<String, EntriesListFlow>()
    private val excludedEntriesMap = HashMap<String, Flow<List<DisplayEntry>>>()

    /**
     * MyBookmarksカテゴリでは非表示エントリを表示する
     */
    private val ignoredEntriesVisibilityInMyBookmarks = dataStore.data.map { it.ignoredEntriesVisibilityInMyBookmarks }

    /**
     * 通知リスト
     */
    override val noticesFlow = MutableStateFlow<List<Notice>>(emptyList())

    /**
     * メンテナンス情報リスト
     */
    override val maintenanceEntriesFlow = MutableStateFlow<List<MaintenanceEntry>>(emptyList())

    /**
     * サブカテゴリのキャッシュ
     */
    private val issuesMap = HashMap<Category, MutableStateFlow<List<Issue>>>()

    /**
     * 全フィルタ
     */
    override val filtersFlow = ignoredEntryDao.entryFiltersFlow()

    /**
     * 既読エントリ情報
     */
    private val readEntriesFlow = MutableStateFlow<List<ReadEntry>>(emptyList())

    /**
     * 現在有効な検索設定
     */
    override val searchSettingFlow = MutableStateFlow(SearchSetting())

    /**
     * 現在有効なマイブクマ検索設定
     */
    override val searchMyBookmarksSettingFlow = MutableStateFlow(SearchSetting())

    /**
     * マイブクマ用の検索クエリ
     */
    override val myBookmarkSearchQuery = MutableStateFlow("")

    /**
     * ロード関係エラーの非同期的な送出
     */
    override val exceptionFlow = MutableSharedFlow<Throwable>()

    // ------ //

    private suspend fun <T> withClient(action: suspend (client: HatenaClientBase)->T) : T =
        hatenaRepo.withClient(action)

    private suspend fun <T> withSignedClient(action: suspend (client: CertifiedHatenaClient)->T) : T? =
        hatenaRepo.withSignedClient(action)

    // ------ //

    /**
     * ViewModel#onCleared時にエントリのキャッシュをクリアする
     */
    @OptIn(InternalCoroutinesApi::class)
    override fun onCleared() {
        synchronized(allEntriesMap) {
            validEntriesMap.clear()
            excludedEntriesMap.clear()
            allEntriesMap.clear()
            rawEntriesMap.clear()
        }
    }

    // ------ //

    @OptIn(InternalCoroutinesApi::class)
    override fun entriesFlow(
        coroutineScope: CoroutineScope,
        destination: Destination
    ) : EntriesListFlow {
        val key = makeMapKey(destination)
        return synchronized(allEntriesMap) {
            validEntriesMap.getOrPut(key) {
                val allEntriesFlow = pickEntriesFlow(coroutineScope, destination)

               excludedEntriesMap[key] =
                    allEntriesFlow.map { items ->
                        items.filter { it.filterState == FilterState.EXCLUSION }
                    }

                allEntriesFlow
                    .map { items -> items.filter { it.filterState == FilterState.VALID } }
                    .stateIn(
                        scope = coroutineScope,
                        started = SharingStarted.Eagerly,
                        initialValue = emptyList()
                    )
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun rawEntriesFlow(destination: Destination) : MutableStateFlow<List<Entry>> {
        return synchronized(rawEntriesMap) {
            rawEntriesMap
                .getOrPut(makeMapKey(destination)) {
                    MutableStateFlow(emptyList())
                }
        }
    }

    private fun pickEntriesFlow(
        coroutineScope: CoroutineScope,
        destination: Destination
    ) : Flow<List<DisplayEntry>> {
        return rawEntriesFlow(destination).let { rawFlow ->
            // 必要であれば初回ロード
            if (rawFlow.value.isEmpty()) {
                coroutineScope.launch {
                    runCatching {
                        rawFlow.value = getEntries(destination)
                    }.onFailure {
                        exceptionFlow.emit(
                            LoadingFailureException(cause = it)
                        )
                    }
                }
            }

            combine(
                rawFlow,
                filtersFlow,
                readEntriesFlow,
                ignoredEntriesVisibilityInMyBookmarks
            ) { entries, filters, readEntries, ignoredVisibilityInMyBookmarks ->
                val myBookmarksState = destination.category == Category.MyBookmarks && ignoredVisibilityInMyBookmarks
                toDisplayEntries(entries, filters, readEntries, myBookmarksState)
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun excludedEntriesFlow(
        coroutineScope: CoroutineScope,
        destination: Destination
    ) : EntriesListFlow {
        val key = makeMapKey(destination)
        return synchronized(allEntriesMap) {
            excludedEntriesMap.getOrPut(key) {
                pickEntriesFlow(coroutineScope, destination).map { items ->
                    items.filter { it.filterState == FilterState.EXCLUSION }
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
        }
    }

    // ------ //

    override suspend fun loadEntries(destination: Destination) {
        when (destination.category) {
            Category.Notices -> {
                val state = getLoadingState(destination)
                if (state.value) {
                    return
                }
                state.value = true

                runCatching {
                    noticesFlow.value = getNotices()
                }.onFailure {
                    state.value = false
                    throw it
                }.onSuccess {
                    state.value = false
                }
            }

            Category.Maintenance -> {
                val state = getLoadingState(destination)
                if (state.value) {
                    return
                }
                state.value = true

                runCatching {
                    maintenanceEntriesFlow.value = getMaintenanceInformation()
                }.onFailure {
                    state.value = false
                    throw it
                }.onSuccess {
                    state.value = false
                }
            }

            else -> {
                val flow = rawEntriesFlow(destination)
                flow.value = getEntries(destination)
            }
        }
    }

    /**
     * エントリ追加ロード
     */
    override suspend fun additionalLoadEntries(destination: Destination) {
        when (destination.category) {
            Category.Notices -> additionalLoadNotices()
            else -> additionalLoadEntriesImpl(destination)
        }
    }

    /**
     * 通常カテゴリでのエントリ追加ロード
     */
    private suspend fun additionalLoadEntriesImpl(destination: Destination) {
        val flow = rawEntriesFlow(destination)
        val prevItems = flow.value
        val items = getEntries(
            destination = destination,
            offset = prevItems.lastIndex
        )
        flow.value = prevItems
            .map { prev ->
                if (prev.eid == 0L) {
                    items.firstOrNull { prev.url == it.url } ?: prev
                }
                else {
                    items.firstOrNull { prev.eid == it.eid } ?: prev
                }
            }
            .plus(
                items.filter { item ->
                    if (item.eid == 0L) prevItems.none { it.url == item.url }
                    else prevItems.none { it.eid == item.eid }
                }
            )
    }

    /**
     * 通知を追加ロード
     */
    private suspend fun additionalLoadNotices() = withSignedClient { client ->
        val loaded = noticesFlow.value
        val stored = noticeDao.getRecords(user = client.accountName, offset = loaded.size)
        noticesFlow.value = loaded.plus(stored.map { it.notice })
    }

    /**
     * 検索設定を更新
     */
    override suspend fun updateSearchSetting(searchSetting: SearchSetting) {
        // 「人気」「新着」2タブ分リピート
        repeat(2) {
            val flow = rawEntriesFlow(
                Destination(category = Category.Search, tabIndex = it)
            )
            flow.value = emptyList()
        }
        searchSettingFlow.value = searchSetting
    }

    /**
     * マイブクマ検索設定を更新
     */
    override suspend fun updateSearchMyBookmarksSetting(searchSetting: SearchSetting) {
        val flow = rawEntriesFlow(
            Destination(category = Category.SearchMyBookmarks, tabIndex = 0)
        )
        flow.value = emptyList()
        searchMyBookmarksSettingFlow.value = searchSetting
    }

    /**
     * 該当エントリを新たに与えられたものに差し替える
     */
    override suspend fun updateEntry(entry: Entry) = withContext(Dispatchers.Default) {
        for (rawEntriesFlow in rawEntriesMap.values) {
            val prev = rawEntriesFlow.value
            prev.indexOfFirst {
                when (it.eid) {
                    0L -> it.url == entry.url
                    else -> it.eid == entry.eid
                }
            }.onNot(comparator = { it < 0 }) { idx ->
                rawEntriesFlow.value = buildList {
                    if (idx > 0) addAll(prev.subList(0, idx))
                    add(entry)
                    if (idx + 1 < prev.size) addAll(prev.subList(idx + 1, prev.size))
                }
            }
        }
    }

    /**
     * 指定IDのエントリを取得する
     */
    override suspend fun getEntry(eid: Long) : Entry = withClient { client ->
        return@withClient client.entry.getEntry(eid = eid)
    }

    // ------ //

    /**
     * 指定エントリを「あとで読む」タグをつけてブクマする
     */
    override suspend fun readLater(entry: Entry, isPrivate: Boolean) {
        withSignedClient { client ->
            client.bookmark.postBookmark(
                url = entry.actualUrl(),
                comment = "",
                readLater = true,
                private = isPrivate
            )
        }
    }

    /**
     * 指定エントリを「読んだ」タグをつけてブクマする
     */
    override suspend fun read(entry: Entry, isPrivate: Boolean) {
        withSignedClient { client ->
            client.bookmark.postBookmark(
                url = entry.actualUrl(),
                comment = "[読んだ]",
                readLater = false,
                private = isPrivate
            )
        }
    }

    /**
     * 指定エントリに既読マークをつける
     */
    override suspend fun readMark(entry: Entry) {
        val existed =
            (if (entry.eid > 0L) readEntryDao.findReadEntry(entry.eid) else null)
                ?: readEntryDao.findReadEntry(entry.actualUrl())

        existed?.let { e ->
            e.copy(
                eid = entry.eid,
                url = entry.actualUrl(),
                timestamp = Instant.now(),
                entry = entry
            ).also { item ->
                readEntryDao.update(item)
                readEntriesFlow.value = readEntriesFlow.value.map {
                    if (it.id == e.id) item
                    else it
                }
            }
        } ?: run {
            ReadEntry(
                eid = entry.eid,
                url = entry.actualUrl(),
                timestamp = Instant.now(),
                entry = entry
            ).also { item ->
                val id = readEntryDao.insert(item)
                readEntriesFlow.value = readEntriesFlow.value.plus(item.copy(id = id))
            }
        }
    }

    /**
     * 指定エントリの既読マークを消す
     */
    override suspend fun removeReadMark(entry: Entry) {
        val existed =
            (if (entry.eid > 0L) readEntryDao.findReadEntry(entry.eid) else null)
                ?: readEntryDao.findReadEntry(entry.actualUrl())
                ?: return
        readEntryDao.delete(existed)
        readEntriesFlow.value = readEntriesFlow.value.filter { it.id != existed.id }
    }

    /**
     * ブクマを削除する
     */
    @OptIn(InternalCoroutinesApi::class)
    override suspend fun removeBookmark(entry: Entry) {
        withSignedClient { it.bookmark.deleteBookmark(url = entry.actualUrl()) }
            ?: throw IllegalStateException("not signed in")
        // キャッシュからも削除
        synchronized(rawEntriesMap) {
            for ((key, flow) in rawEntriesMap) {
                when {
                    key.contains(Category.MyBookmarks.name) -> {
                        flow.value = flow.value.filterNot { it.eid == entry.eid }
                    }

                    else -> {
                        flow.value = flow.value.mapNotNull {
                            if (it.bookmarkedData != null && it.eid == entry.eid) {
                                when (it) {
                                    is EntryItem -> it.copy(bookmarkedData = null)
                                    is IssueEntry -> it.copy(bookmarkedData = null)
                                    is MyHotEntry -> it.copy(bookmarkedData = null)
                                    is UserEntry -> if (it.userName == entry.bookmarkedData?.user) null else it
                                }
                            }
                            else it
                        }
                    }
                }
            }
        }
    }

    /**
     * 既読エントリ情報を呼び出し
     *
     * todo
     */
    private suspend fun getReadEntries(entries: List<Entry>) {
        val ids = ArrayList<Long>(entries.size)
        val urls = ArrayList<String>(entries.size)
        for (entry in entries) {
            if (entry.eid == 0L) urls.add(entry.actualUrl())
            else ids.add(entry.eid)
        }
        val fromIds = readEntryDao.getReadEntriesFromEntryIds(ids)
        val fromUrls = readEntryDao.getReadEntriesFromUrls(urls)
        readEntriesFlow.value = buildList {
            addAll(readEntriesFlow.value)
            addAll(fromIds)
            addAll(fromUrls)
        }
    }

    // ------ //

    /**
     * 指定カテゴリのIssueリストを取得する
     */
    @OptIn(InternalCoroutinesApi::class)
    override fun issuesFlow(coroutineScope: CoroutineScope, category: Category) : Flow<List<Issue>> {
        return synchronized(issuesMap) {
            issuesMap
                .getOrPut(category) { MutableStateFlow(emptyList()) }
                .also { flow ->
                    if (!category.hasIssues) return@also
                    if (flow.value.isNotEmpty()) return@also
                    val hatenaCategory = category.hatenaCategory ?: return@also
                    coroutineScope.launch(Dispatchers.IO) {
                        withClient { client ->
                            runCatching {
                                flow.value = client.entry.getIssues(hatenaCategory).issues
                            }.onFailure {
                                Log.e("issuesFlow", it.stackTraceToString())
                            }
                        }
                    }
                }
        }
    }

    // ------ //

    /**
     * APIから得られたエントリ情報をフィルタリングした上で表示用のエンティティに変換する
     */
    private suspend fun toDisplayEntries(
        rawItems: List<Entry>,
        filters: List<IgnoredEntry>,
        readEntries: List<ReadEntry>,
        ignoreFilter: Boolean = false
    ) : List<DisplayEntry> = withContext(Dispatchers.IO) {
        val displayEntries =
            rawItems.map { entry ->
                val filterState =
                    when {
                        ignoreFilter -> FilterState.VALID
                        filters.any { it.match(entry) } -> FilterState.EXCLUSION
                        else -> FilterState.VALID
                    }

                val readState =
                    readEntries.firstOrNull {
                        if (entry.eid > 0L) it.eid == entry.eid
                        else it.url == entry.actualUrl()
                    }

                val starsEntries =
                    bookmarkStarsMutex.withLock {
                        bookmarkStars.filterKeys { it.startsWith("${entry.eid}_") }
                    }

                DisplayEntry(
                    entry = entry,
                    read = readState,
                    filterState = filterState,
                    starsEntries = starsEntries
                )
            }
        return@withContext displayEntries
    }

    // ------ //

    /**
     * カテゴリにあわせて各種エントリリストを取得する
     */
    private suspend fun getEntries(
        destination: Destination,
        offset: Int? = null
    ) : List<Entry> = withContext(Dispatchers.IO) {
        val state = getLoadingState(destination)
        if (state.value) {
            throw IllegalStateException("loading has already started.")
        }
        state.value = true

        val entries =
            runCatching {
                when (destination.category) {
                    Category.MyBookmarks -> getMyBookmarksEntries(destination.tabIndex, offset)

                    Category.MyHotEntries -> getMyHotEntries()

                    Category.Stars -> getStarEntries(destination.tabIndex)

                    Category.Site -> getSiteEntries(destination.target, destination.tabIndex)

                    Category.Followings -> getFollowingsEntries(offset)

                    Category.Memorial15th -> getMemorialEntries(destination.tabIndex)

                    Category.FavoriteSites -> getFavoriteSitesEntries(destination.tabIndex, offset)

                    Category.Search -> searchEntries(destination.tabIndex, offset)

                    Category.SearchMyBookmarks -> searchMyBookmarks(destination.tabIndex, offset)

                    Category.History -> getHistories(offset)

                    Category.User -> getUserBookmarksEntries(destination.target, offset)

                    else -> {
                        if (destination.category.hatenaCategory != null) {
                            if (destination.issue == null) {
                                getHatenaEntries(destination.category, destination.tabIndex, offset)
                            }
                            else {
                                getHatenaIssueEntries(destination.issue, destination.tabIndex, offset)
                            }
                        }
                        else throw IllegalArgumentException()
                    }
                }
            }.onFailure {
                state.value = false
                throw it
            }.onSuccess {
                state.value = false
            }.getOrThrow()

        return@withContext entries
            .distinctBy { it.url }
            .also {
                launch { loadBookmarkStars(entries) }
                launch { getReadEntries(entries) }
            }
    }

    /**
     * カテゴリを指定してエントリ一覧を取得する
     */
    private suspend fun getHatenaEntries(
        category: Category,
        tabIndex: Int,
        offset: Int? = null
    ) : List<Entry> = withClient { client ->
        val entriesType = EntriesType.fromOrdinal(tabIndex)
        client.entry.getEntries(
            entriesType = entriesType,
            category = category.hatenaCategory!!,
            offset = offset
        )
    }

    /**
     * サブカテゴリを指定してエントリ一覧を取得する
     */
    private suspend fun getHatenaIssueEntries(
        issue: Issue,
        tabIndex: Int,
        offset: Int? = null,
    ) : List<Entry> = withClient { client ->
        val entriesType = EntriesType.fromOrdinal(tabIndex)
        val response = client.entry.getIssueEntries(
            entriesType = entriesType,
            issue = issue,
            offset = offset
        )
        return@withClient response.entries
    }

    /**
     * 自分がブクマしたエントリ一覧を取得する
     */
    private suspend fun getMyBookmarksEntries(
        tabIndex: Int,
        offset: Int? = null
    ) : List<Entry> = withSignedClient { client ->
        when (tabIndex) {
            0 -> client.entry.getBookmarkedEntries(offset = offset)

            1 -> {
                client.entry.getBookmarkedEntries(
                    user = client.accountName,
                    offset = offset,
                    tag = "あとで読む"
                )
            }

            else -> throw IllegalArgumentException()
        }
    } ?: emptyList()

    /**
     * 自分がブクマしたエントリから検索する
     */
    private suspend fun searchMyBookmarks(
        tabIndex: Int,
        offset: Int? = null
    ) : List<Entry> = withSignedClient { client ->
        val setting = searchMyBookmarksSettingFlow.value
        client.entry.searchBookmarkedEntries(
            searchType = setting.searchType,
            query = setting.query,
            offset = offset
        )
    } ?: emptyList()

    /**
     * マイホットエントリ一覧を取得する
     */
    private suspend fun getMyHotEntries() : List<Entry> = withSignedClient { client ->
        client.entry.getMyHotEntries()
            .groupBy { it.eid }
            .map { p ->
                p.value[0].copy(
                    myHotEntryComments = buildList {
                        p.value.map { entry ->
                            entry.myHotEntryComments?.let {
                                addAll(it)
                            }
                        }
                    }
                )
            }
    } ?: emptyList()

    /**
     * フォローしているユーザーがブクマをつけたエントリ一覧を取得する
     */
    private suspend fun getFollowingsEntries(offset: Int?) : List<Entry> = withSignedClient { client ->
        client.entry.getFollowingEntries(offset = offset)
    } ?: emptyList()

    // TODO
    private suspend fun getStarEntries(tabIndex: Int) : List<Entry> = withSignedClient { client ->
        val starsEntries = when (tabIndex) {
            0 -> client.star.getMyRecentStars()
            1 -> client.star.getRecentStarsReport().entries
            else -> throw IllegalArgumentException()
        }
        convertStarsToEntries(starsEntries)
    } ?: emptyList()

    private suspend fun convertStarsToEntries(
        starsEntries: List<StarsEntry>
    ) : List<Entry> = withClient { client ->
        coroutineScope {
            val urlRegex = Regex("""https?://b\.hatena\.ne\.jp/(.+)/(\d+)#bookmark-(\d+)""")
            val data = starsEntries
                .mapNotNull {
                    val match = urlRegex.matchEntire(it.url) ?: return@mapNotNull null
                    val user = match.groups[1]?.value ?: return@mapNotNull null
                    val timestamp = match.groups[2]?.value ?: return@mapNotNull null
                    val eid = match.groups[3]?.value ?: return@mapNotNull null
                    val starsCount = it.allStars
                    BookmarkCommentUrl(
                        it.url,
                        user,
                        timestamp,
                        eid.toLong(),
                        starsCount
                    )
                }
                /*
                .groupBy { it.eid.toString() + "_" + it.user }
                .map {
                    val starsCount = it.value
                        .flatMap { e -> e.starsCount }
                        .groupBy { s -> s.color }
                        .map { s -> Star("", "", s.value.size, s.key) }
                    it.value.first().copy(starsCount = starsCount)
                }*/

            // TODO: ブクマ自体のページを取得する
            val tasks = data
                .map { comment ->
                    async {
                        runCatching {
                            client.entry.getEntry(eid = comment.eid)
                        }.getOrNull()
                    }
                }
            tasks.awaitAll()

            return@coroutineScope tasks.mapIndexedNotNull { index, deferred ->
                deferred.await()?.let { entry ->
                    val comment = data[index]
                    EntryItem(
                        title = entry.title,
                        url = entry.url,
                        eid = entry.eid,
                        description = entry.description,
                        count = entry.count,
                        createdAt = entry.createdAt,
                        _entryUrl = entry._entryUrl,
                        bookmarkedData = entry.bookmarkedData,
                        bookmarksOfFollowings = listOf(
                            BookmarkResult(
                                user = comment.user,
                                userIconUrl = hatenaUserIconUrl(comment.user),
                                comment = "",
                                commentRaw = "",
                                tags = emptyList(),
                                timestamp = Instant.now(),
                                _timestamp = comment.timestamp,
                                permalink = comment.url,
                                starsCount = comment.starsCount
                            )
                        )
                    )
                }
            }
        }
    }

    /**
     * 指定サイトのエントリ一覧を取得する
     */
    private suspend fun getSiteEntries(
        rootUrl: String?,
        tabIndex: Int,
        page: Int = 1
    ) : List<Entry> = withClient { client ->
        if (rootUrl == null) throw IllegalArgumentException("rootUrl is null")
        client.entry.getSiteEntries(
            url = rootUrl,
            entriesType = EntriesType.fromOrdinal(tabIndex),
            page = page
        )
    }

    /**
     * お気に入りサイトのエントリ一覧を取得する
     */
    private suspend fun getFavoriteSitesEntries(
        tabIndex: Int,
        offset: Int?
    ) : List<Entry> = withClient { client ->
        // todo
        emptyList()
    }

    /**
     * 15周年タイムカプセルエントリ一覧を取得する
     */
    private suspend fun getMemorialEntries(tabIndex: Int) : List<Entry> = withClient { client ->
        client.entry.getHistoricalEntries(2005 + tabIndex)
    }

    /**
     * 通知を取得する
     */
    private suspend fun getNotices() : List<Notice> = withSignedClient { client ->
        client.user.getNotices().notices.let { fetched ->
            for (n in fetched) {
                noticeDao.insert(n)
            }
        }
        noticeDao.getRecords(user = client.accountName)
            .map { it.notice }
    } ?: emptyList()

    /**
     * 既読エントリを取得する
     */
    private suspend fun getHistories(offset: Int?) : List<Entry> {
        val readEntries = readEntryDao.load(offset = offset ?: 0)
        return readEntries.map { it.entry }
    }

    /**
     * メンテナンス情報一覧を取得する
     */
    private suspend fun getMaintenanceInformation() : List<MaintenanceEntry> = withClient { client ->
        client.entry.getMaintenanceEntries()
    }

    /**
     * エントリを検索する
     */
    private suspend fun searchEntries(tabIndex: Int, offset: Int?) : List<Entry> = withClient { client ->
        searchSettingFlow.value.let {
            if (it.query.isBlank()) {
                emptyList()
            }
            else {
                client.entry.searchEntries(
                    searchType = it.searchType,
                    query = it.query,
                    sortType = EntriesType.fromOrdinal(tabIndex),
                    users = it.bookmarksCount,
                    dateBegin = it.dateBegin,
                    dateEnd = it.dateEnd,
                    safe = it.safe,
                    offset = offset
                )
            }
        }
    }

    /**
     * 指定ユーザーがブクマしたエントリを取得する
     */
    private suspend fun getUserBookmarksEntries(user: String?, offset: Int?) : List<Entry> = withClient { client ->
        user?.let {
            client.entry.getBookmarkedEntries(
                user = it,
                offset = offset
            )
        } ?: emptyList()
    }

    // ------ //

    private val bookmarkStarsMutex = Mutex()

    private val bookmarkStars = HashMap<String, MutableStateFlow<StarsEntry?>>()

    /**
     * ブクマのスター情報を取得する
     */
    private suspend fun loadBookmarkStars(entries: List<Entry>) {
        val loaderMap = HashMap<String, BookmarkResult>()
        val urls = bookmarkStarsMutex.withLock {
            entries
                .filter { it.bookmarkedData?.comment?.isNotBlank() == true }
                .map { entry ->
                    entry.bookmarkedData!!.let { b ->
                        bookmarkStars.getOrPut("${b.eid}_${b.user}") { MutableStateFlow(null) }
                        loaderMap[b.permalink] = b
                        b.permalink
                    }
                }
        }
        withClient { client ->
            runCatching {
                val starsEntries = client.star.getStarsEntries(urls)
                bookmarkStarsMutex.withLock {
                    for (entry in starsEntries) {
                        val key = loaderMap[entry.url]
                            ?.let { "${it.eid}_${it.user}" }
                            ?: continue
                        bookmarkStars[key]?.emit(entry)
                    }
                }
            }
        }
    }

    // ------ //

    private val loadingStateMap = HashMap<String, MutableStateFlow<Boolean>>()

    /**
     * 各画面ごとのロード中かどうかの状態を取得する
     */
    override fun getLoadingState(destination: Destination) : MutableStateFlow<Boolean> =
        kotlin.synchronized(loadingStateMap) {
            val key = makeMapKey(destination)
            loadingStateMap.getOrPut(key) { MutableStateFlow(false) }
        }

    // ------ //

    /**
     * ブコメ単体で取得するために必要な情報
     *
     * スター -> ブコメの変換用一時データ
     */
    private data class BookmarkCommentUrl (
        val url : String,
        val user : String,
        val timestamp : String,
        val eid : Long,
        val starsCount: List<Star>
    )
}

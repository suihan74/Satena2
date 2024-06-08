package com.suihan74.satena2.scene.bookmarks

import android.util.Log
import androidx.datastore.core.DataStore
import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.model.account.Notice
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.bookmark.BookmarksDigest
import com.suihan74.hatena.model.bookmark.BookmarksEntry
import com.suihan74.hatena.model.bookmark.BookmarksResponse
import com.suihan74.hatena.model.bookmark.Report
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.entry.RelatedEntriesResponse
import com.suihan74.hatena.model.star.StarCount
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelRepository
import com.suihan74.satena2.utility.hatena.actualUrl
import com.suihan74.satena2.utility.hatena.copy
import com.suihan74.satena2.utility.hatena.modifySpecificUrl
import com.suihan74.satena2.utility.hatena.toBookmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface BookmarksRepository {
    /**
     * ローディング状況
     */
    val loadingFlow : StateFlow<Boolean>

    /**
     * 現在表示中ページのエントリ・ブクマ情報
     */
    val entityFlow : StateFlow<Entity>

    /**
     * 現在表示中ページの自分のブクマ
     */
    val myBookmarkFlow : StateFlow<DisplayBookmark?>

    /**
     * 続きの読み込みができるか否か
     */
    val additionalLoadableFlow : StateFlow<Boolean>

    // ------ //

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
     * 「カスタム」タブの表示対象設定
     */
    val customTabSettingFlow : StateFlow<CustomTabSetting>

    // ------ //

    fun initialize(coroutineScope: CoroutineScope)

    /**
     * 与えられたエントリorURLor通知のエントリ・ブクマ情報をロードする
     */
    suspend fun load(entry: Entry? = null, eid: Long = 0L, url: String? = null, notice: Notice? = null)

    /**
     * リストの最新を取得する
     */
    suspend fun refresh(entity: Entity, tab: BookmarksTab)

    /**
     * リストの続きを取得する
     */
    suspend fun loadAdditional(entity: Entity, tab: BookmarksTab)

    /**
     * ブクマ結果をリストに反映する
     */
    suspend fun updateMyBookmark(bookmarkResult: BookmarkResult?)

    /**
     * ユーザーを非表示にする
     */
    suspend fun ignoreUser(user: String)

    /**
     * ユーザー非表示を解除する
     */
    suspend fun unIgnoreUser(user: String)

    /**
     * ユーザーが非表示か否か
     */
    fun isIgnored(user: String) : Boolean

    /**
     * ブクマを通報する
     */
    suspend fun report(report: Report)

    /**
     * 自身のブクマを削除する
     */
    suspend fun deleteMyBookmark()

    // ------ //

    /**
     * 指定の[Bookmark]から表示用の[DisplayBookmark]を作成する
     */
    suspend fun makeDisplayBookmark(bookmark: Bookmark, eid: Long) : DisplayBookmark

    // ------ //

    /**
     * 指定ユーザーにつけられたスターとそのスターをつけたユーザーのブクマを取得する
     */
    fun starsTo(
        entity: Entity,
        item: DisplayBookmark,
        coroutineScope: CoroutineScope
    ) : StateFlow<List<Mention>>

    // ------ //

    /**
     * エントリIDからページのURLを取得する
     */
    suspend fun getPageUrl(eid: Long) : String
}

// ------ //

// TODO: hatenaClientのサインイン状態変更中にブクマ等読み込みが起こらないようにする
class BookmarksRepositoryImpl @Inject constructor(
    appDatabase: AppDatabase,
    private val dataStore: DataStore<Preferences>,
    private val hatenaRepo: HatenaAccountRepository,
    private val userLabelRepo: UserLabelRepository
) : BookmarksRepository {
    /**
     * URLに対応するエントリ・ブクマ情報のキャッシュ
     */
    private val entityCaches = HashMap<String, Entity>()

    // ------ //

    private val ignoredEntryDao = appDatabase.ignoredEntryDao()

    /**
     * サインイン状態を伴わないHatenaクライアント
     */
    private val plainHatenaClient = HatenaClient

    // ------ //

    /**
     * ローディング状況
     */
    override val loadingFlow = MutableStateFlow(false)

    /**
     * 現在表示中ページのエントリ・ブクマ情報
     */
    override val entityFlow = MutableStateFlow(Entity.EMPTY)

    /**
     * 現在表示中ページの自分のブクマ
     */
    override val myBookmarkFlow = MutableStateFlow<DisplayBookmark?>(null)

    /**
     * 続きの読み込みができるか否か
     */
    override val additionalLoadableFlow = MutableStateFlow(false)

    /**
     * フィルタ
     */
    private val filtersFlow = ignoredEntryDao.bookmarkFiltersFlow()
    private val filters = ArrayList<IgnoredEntry>()
    private val filtersMutex = Mutex()

    /**
     * アカウント側に設定されている非表示ユーザーリスト
     */
    private val ignoredUsersFlow = MutableStateFlow(emptySet<String>())

    // ------ //

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
     * 「カスタム」タブの表示対象設定
     */
    override val customTabSettingFlow = MutableStateFlow(CustomTabSetting())

    // ------ //

    /**
     * 各ブコメに付与されたスター情報
     */
    private val starsMap = HashMap<String, MutableStateFlow<StarsEntry>>()

    // ------ //

    override fun initialize(coroutineScope: CoroutineScope) {
        dataStore.data
            .onEach {
                filtersMutex.withLock {
                    customTabSettingFlow.value = it.bookmarkCustomTabSetting
                }
            }
            .launchIn(coroutineScope)

        hatenaRepo.ngUsers
            .onEach {
                filtersMutex.withLock {
                    ignoredUsersFlow.value = it.toSet()
                }
            }
            .launchIn(coroutineScope)

        filtersFlow
            .onEach {
                filtersMutex.withLock {
                    filters.clear()
                    filters.addAll(it)
                }
            }
            .launchIn(coroutineScope)

        combine(
            entityFlow,
            filtersFlow,
            ignoredUsersFlow,
            hatenaRepo.loadingNgUsers,
            customTabSettingFlow
        ) { entity, _, _, loadingNgUsers, _ ->
            if (loadingNgUsers) return@combine  // NGユーザー読み込み完了前にブクマリストを表示しないようにする
            val entry = entity.entry
            val bookmarks = entity.bookmarks
            val scoredBookmarks = entity.bookmarksDigest.scoredBookmarks
            val favoriteBookmarks = entity.bookmarksDigest.favoriteBookmarks
            recentBookmarksFlow.value = toDisplayList(entry, bookmarks, BookmarksTab.RECENT)
            allBookmarksFlow.value = toDisplayList(entry, bookmarks, BookmarksTab.ALL)
            popularBookmarksFlow.value = toDisplayList(entry, scoredBookmarks, BookmarksTab.DIGEST)
            followingBookmarksFlow.value = toDisplayList(entry, favoriteBookmarks, BookmarksTab.DIGEST)
            customBookmarksFlow.value = toDisplayList(entry, bookmarks, BookmarksTab.CUSTOM)
        }
        .launchIn(coroutineScope)

        combine(entityFlow, hatenaRepo.account) { entity, account ->
            myBookmarkFlow.value = account?.name?.let { user -> extractMyBookmark(entity, user) }
        }
        .launchIn(coroutineScope)

        entityFlow
            .onEach {
                additionalLoadableFlow.value = it.recentCursor != null
            }
            .launchIn(coroutineScope)
    }

    /**
     * エンティティからユーザーのブクマを抽出する
     */
    private suspend fun extractMyBookmark(entity: Entity, user: String) : DisplayBookmark? {
        with(entity) {
            entry.bookmarkedData?.let {
                return it.toBookmark().toDisplayBookmark(entity.entry.eid).also { d ->
                    loadStarsToBookmarks(entry, listOf(d.bookmark))
                }
            }
            allBookmarksFlow.value.firstOrNull { it.bookmark.user == user }?.let {
                return it
            }
            popularBookmarksFlow.value.firstOrNull { it.bookmark.user == user }?.let {
                return it
            }
            bookmarksEntry.bookmarks.firstOrNull { it.user == user }?.let {
                return it.toBookmark(entity.bookmarksEntry).toDisplayBookmark(entity.entry.eid).also { d ->
                    loadStarsToBookmarks(entry, listOf(d.bookmark))
                }
            }
        }
        return null
    }

    // ------ //

    /**
     * 与えられたエントリorURLor通知のエントリ・ブクマ情報をロードする
     */
    override suspend fun load(entry: Entry?, eid: Long, url: String?, notice: Notice?) {
        loadingFlow.value = true
        runCatching {
            val entryImpl = when {
                entry != null ->
                    entry
                /*
                    hatenaRepo.withClient {
                        if (entry.eid == 0L) {
                            it.entry.getEntry(modifySpecificUrl(entry.url) ?: entry.url)
                        }
                        else {
                            it.entry.getEntry(entry.eid)
                        }
                    }
                 */

                eid > 0L ->
                    hatenaRepo.withClient {
                        it.entry.getEntry(eid)
                    }

                url != null ->
                    hatenaRepo.withClient {
                        it.entry.getEntry(modifySpecificUrl(url) ?: url)
                    }

                notice != null ->
                    hatenaRepo.withClient {
                        it.entry.getEntry(notice.eid)
                    }

                else -> throw IllegalArgumentException("both entry and url are empty.")
            }
            val requestUrl = entryImpl.actualUrl()

            val tasks = coroutineScope {
                val client = hatenaRepo.getSignedClient() ?: hatenaRepo.getClient()
                listOf(
                    async {
                        runCatching {
                            plainHatenaClient.bookmark.getBookmarksEntry(requestUrl)
                        }.getOrElse {
                            BookmarksEntry(
                                id = entryImpl.eid,
                                title = entryImpl.title,
                                count = 0,
                                url = requestUrl,
                                entryUrl = entryImpl.entryUrl,
                                requestedUrl = requestUrl,
                                screenshot = entryImpl.imageUrl,
                                bookmarks = emptyList()
                            )
                        }
                    },
                    async {
                        runCatching {
                            client.bookmark.getBookmarksDigest(requestUrl)
                        }.getOrElse {
                            BookmarksDigest(
                                referredBlogEntries = emptyList(),
                                scoredBookmarks = emptyList(),
                                favoriteBookmarks = emptyList()
                            )
                        }.also { d ->
                            loadStarsToBookmarks(
                                entryImpl,
                                sequenceOf(
                                    d.scoredBookmarks,
                                    d.favoriteBookmarks
                                ).flatten().distinct().toList()
                            )
                        }
                    },
                    async {
                        runCatching {
                            plainHatenaClient.bookmark.getRecentBookmarks(requestUrl, null).also {
                                loadStarsToBookmarks(entryImpl, it.bookmarks)
                            }
                        }.getOrElse {
                            BookmarksResponse(
                                cursor = null,
                                bookmarks = emptyList()
                            )
                        }
                    },
                    async {
                        runCatching {
                            plainHatenaClient.star.getStarsEntry(requestUrl)
                        }.getOrElse {
                            StarsEntry(
                                url = requestUrl,
                                stars = emptyList()
                            )
                        }
                    },
                    async {
                        runCatching {
                            client.entry.getRelatedEntries(requestUrl)
                        }.getOrElse {
                            RelatedEntriesResponse(
                                entries = emptyList(),
                                metaEntry = null,
                                referredBlogEntries = emptyList(),
                                referredEntries = emptyList(),
                                topics = emptyList(),
                                prEntries = emptyList()
                            )
                        }
                    }
                )
            }
            tasks.awaitAll()

            val bookmarksEntry = tasks[0].await() as BookmarksEntry
            val bookmarksDigest = tasks[1].await() as BookmarksDigest
            val recentBookmarksResponse = tasks[2].await() as BookmarksResponse
            val entryStarsEntry = tasks[3].await() as StarsEntry
            val relatedEntriesResponse = tasks[4].await() as RelatedEntriesResponse

            val entity =
                Entity(
                    requestUrl = requestUrl,
                    entry = entryImpl,
                    bookmarksEntry = updateBookmarksEntry(bookmarksEntry, recentBookmarksResponse.bookmarks),
                    bookmarksDigest = bookmarksDigest,
                    bookmarks = recentBookmarksResponse.bookmarks,
                    recentCursor = recentBookmarksResponse.cursor,
                    entryStars = entryStarsEntry,
                    starsMap = emptyMap(),
                    relatedEntriesResponse = relatedEntriesResponse
                )

            entityCaches[requestUrl] = entity
            entityFlow.value = entity
            Log.i("bookmark","initial loaded")
        }.onFailure {
            loadingFlow.value = false
            throw it
        }
        loadingFlow.value = false
    }

    // ------ //

    /**
     * リストの最新を取得する
     */
    override suspend fun refresh(entity: Entity, tab: BookmarksTab) {
        loadingFlow.value = true
        runCatching {
            val client = plainHatenaClient
            val requestUrl = entity.requestUrl
            val newEntity = when (tab) {
                BookmarksTab.DIGEST -> {
                    val digest = client.bookmark.getBookmarksDigest(requestUrl)
                    val updatedEntity = entity.copy(bookmarksDigest = digest)
                    val bookmarks = digest.scoredBookmarks.plus(digest.favoriteBookmarks)
                    updateBookmarksEntry(updatedEntity, bookmarks)
                }

                else -> {
                    val entry = client.entry.getEntry(requestUrl)
                    val recentBookmarks = client.bookmark.getRecentBookmarks(requestUrl)
                    val updatedEntity = entity.copy(
                        entry = entry,
                        bookmarks = updateRecentBookmarks(entity, recentBookmarks.bookmarks)
                    )
                    updateBookmarksEntry(updatedEntity, recentBookmarks.bookmarks)
                }
            }
            updateEntity(newEntity, requestUrl)
        }
        loadingFlow.value = false
    }

    override suspend fun loadAdditional(entity: Entity, tab: BookmarksTab) {
        if (tab == BookmarksTab.DIGEST) return
        if (!additionalLoadableFlow.value) return
        loadingFlow.value = true
        runCatching {
            val client = plainHatenaClient
            val requestUrl = entity.requestUrl
            val response = client.bookmark.getRecentBookmarks(
                url = requestUrl,
                cursor = entity.recentCursor
            )
            val newEntity = updateBookmarksEntry(entity, response.bookmarks).copy(
                bookmarks = entity.bookmarks.plus(response.bookmarks),
                recentCursor = response.cursor
            )
            updateEntity(newEntity, requestUrl)
        }
        loadingFlow.value = false
    }

    // ------ //

    private fun updateEntity(entity: Entity, url: String? = null) {
        val requestUrl = url ?: entity.requestUrl
        entityCaches[requestUrl] = entity
        if (entityFlow.value.requestUrl == requestUrl) {
            entityFlow.value = entity
        }
    }

    private suspend fun updateBookmarksEntry(entity: Entity, bookmarks: List<Bookmark>) : Entity {
        if (bookmarks.isEmpty()) return entity
        loadStarsToBookmarks(entity.entry, bookmarks)
        return entity.copy(
            bookmarksEntry = updateBookmarksEntry(entity.bookmarksEntry, bookmarks)
        )
    }

    private fun updateBookmarksEntry(bookmarksEntry: BookmarksEntry, bookmarks: List<Bookmark>) : BookmarksEntry {
        if (bookmarks.isEmpty()) return bookmarksEntry
        val newBookmarks = buildList {
            bookmarks.forEach { item ->
                if (bookmarksEntry.bookmarks.none { it.user == item.user }) {
                    add(
                        BookmarksEntry.Bookmark(
                            user = item.user,
                            comment = item.comment,
                            tags = item.tags,
                            timestamp = item.timestamp
                        )
                    )
                }
            }
            bookmarksEntry.bookmarks.forEach { orig ->
                val item = bookmarks.firstOrNull { it.user == orig.user }
                if (item == null) add(orig)
                else add(
                    orig.copy(
                        comment = item.comment,
                        tags = item.tags,
                        timestamp = item.timestamp
                    )
                )
            }
        }

        return bookmarksEntry.copy(bookmarks = newBookmarks)
    }

    private fun updateRecentBookmarks(entity: Entity, bookmarks: List<Bookmark>) : List<Bookmark> {
        return buildList {
            addAll(bookmarks)
            entity.bookmarks.forEach { b ->
                if (bookmarks.none { it.user == b.user }) {
                    add(b)
                }
            }
        }.sortedByDescending { it.timestamp }
    }

    // ------ //

    /**
     * ブクマ結果をリストに反映する
     */
    override suspend fun updateMyBookmark(bookmarkResult: BookmarkResult?) = coroutineScope {
        val prevEntity = entityFlow.value
        val user = hatenaRepo.account.value?.name ?: return@coroutineScope
        val bookmark = bookmarkResult?.toBookmark()
        val tasks = listOf(
            async {
                prevEntity.bookmarksDigest.copy(
                    scoredBookmarks = prevEntity.bookmarksDigest.scoredBookmarks.mapNotNull {
                        if (it.user == user) bookmark
                        else it
                    }
                )
            },
            async {
                prevEntity.bookmarks.mapNotNull {
                    if (it.user == user) bookmark
                    else it
                }
            },
            async {
                prevEntity.bookmarksEntry.copy(
                    bookmarks = prevEntity.bookmarksEntry.bookmarks.mapNotNull {
                        if (it.user == user) {
                            bookmark?.let { b ->
                                BookmarksEntry.Bookmark(
                                    user = user, comment = b.comment, tags = b.tags, timestamp = b.timestamp
                                )
                            }
                        }
                        else it
                    }
                )
            }
        )
        tasks.awaitAll()

        val entry = prevEntity.entry.copy(bookmarkResult)

        @Suppress("UNCHECKED_CAST")
        val entity = prevEntity.copy(
            entry = entry,
            bookmarksEntry = tasks[2].await() as BookmarksEntry,
            bookmarksDigest = tasks[0].await() as BookmarksDigest,
            bookmarks = tasks[1].await() as List<Bookmark>
        )
        updateEntity(entity)
    }

    // ------ //

    private val starsMapMutex = Mutex()

    /**
     * ブコメのスターエントリを取得する
     */
    private suspend fun loadStarsToBookmarks(
        entry: Entry,
        bookmarks: List<Bookmark>
    ) {
        val eid = entry.eid
        val formatter = DateTimeFormatter.ofPattern("uuuuMMdd")
        val zoneOffset = ZoneOffset.ofHours(9)
        val bookmarksEntry = entityFlow.value.bookmarksEntry
        val allBookmarks = allBookmarksFlow.value

        val urls = starsMapMutex.withLock {
            val mentions = buildList {
                for (b in bookmarks) {
                    if (b.comment.isBlank()) {
                        continue
                    }
                    val idsCalled = idCallRegex.findAll(b.comment)
                    for (m in idsCalled) {
                        val id = m.groupValues[2]
                        if (id == b.user) {
                            continue
                        }
                        val target = allBookmarks.firstOrNull { it.bookmark.user == id }?.let {
                            val date = formatter.format(it.bookmark.timestamp.atOffset(zoneOffset))
                            "${HatenaClient.baseUrlB}${it.bookmark.user}/$date#bookmark-$eid"
                        }
                            ?: bookmarksEntry.bookmarks.firstOrNull { it.user == id }?.let {
                                val date = formatter.format(it.timestamp.atOffset(zoneOffset))
                                "${HatenaClient.baseUrlB}${it.user}/$date#bookmark-$eid"
                            }
                        target?.let {
                            val flow = starsMap.getOrPut(it) {
                                MutableStateFlow(StarsEntry(url = "", stars = emptyList()))
                            }
                            add(it to flow)
                        }
                    }
                }
            }

            bookmarks
                .filter { it.comment.isNotBlank() }
                .map {
                    val date = formatter.format(it.timestamp.atOffset(zoneOffset))
                    val url = "${HatenaClient.baseUrlB}${it.user}/$date#bookmark-$eid"
                    val flow = starsMap.getOrPut(url) { MutableStateFlow(StarsEntry(url = "", stars = emptyList())) }
                    url to flow
                }
                .plus(mentions)
                .distinct()
        }

        val starEntries = hatenaRepo.withClient { client ->
            client.star.getStarsEntries(urls.map { it.first })
        }
        for (starEntry in starEntries) {
            val item = urls.firstOrNull { it.first == starEntry.url } ?: continue
            item.second.value = starEntry
        }
    }

    // ------ //

    private suspend fun toDisplayList(
        entry: Entry,
        bookmarks: List<Bookmark>,
        tab: BookmarksTab
    ) : List<DisplayBookmark> {
        val eid = entry.eid

        val accountName = hatenaRepo.account.value?.name
        val ignoredUsers = ignoredUsersFlow.value

        return when (tab) {
            BookmarksTab.DIGEST -> {
                // todo: フィルタするかどうかを選べるようにする
                filtersMutex.withLock {
                    bookmarks
                        .filter { b ->
                            val isMyBookmark = b.user == accountName
                            val notNgUser = !ignoredUsers.contains(b.user)
                            val notBlankComment = b.comment.isNotBlank()
                            val notMuted = filters.none { it.match(b) }
                            isMyBookmark || notNgUser && notBlankComment && notMuted
                        }
                        .map { b -> b.toDisplayBookmark(eid) }
                }
            }

            BookmarksTab.RECENT -> {
                filtersMutex.withLock {
                    bookmarks
                        .filter { b ->
                            val isMyBookmark = b.user == accountName
                            val notNgUser = !ignoredUsers.contains(b.user)
                            val notBlankComment = b.comment.isNotBlank()
                            val notMuted = filters.none { it.match(b) }
                            isMyBookmark || notNgUser && notBlankComment && notMuted
                        }
                        .map { b -> b.toDisplayBookmark(eid) }
                }
            }

            BookmarksTab.CUSTOM -> {
                filtersMutex.withLock {
                    val s = customTabSettingFlow.value
                    val enabledLabels = userLabelRepo.getLabeledUsers(s.enableLabelIds.toList())
                    val enabledUsers = enabledLabels.flatMap { it.users }.map { it.name }.toSet()

                    bookmarks
                        .filter { b ->
                            if (b.user == accountName) return@filter true
                            if (!s.areIgnoresShown && ignoredUsers.contains(b.user)) return@filter false
                            if (!s.areNoCommentsShown && b.comment.isBlank()) return@filter false
                            if (!s.areIgnoresShown && filters.any { it.match(b) }) return@filter false

                            val labeled = userLabelRepo.isLabeledUser(b.user)
                            if (labeled) enabledUsers.contains(b.user)
                            else return@filter s.areNoLabelsShown
                        }
                        .map { b -> b.toDisplayBookmark(eid) }
                }
            }

            else -> {
                filtersMutex.withLock {
                    bookmarks
                        .map { b -> b.toDisplayBookmark(eid) }
                        .let { list ->
                            myBookmarkFlow.value?.let { my ->
                                if (list.any { it.bookmark.user == my.bookmark.user }) list
                                else list.plus(my)
                            } ?: list
                        }
                }
            }
        }
    }

    private val idCallRegex = Regex("""(^|[^a-zA-Z0-9:])id:(?!entry:)([a-zA-Z0-9_\-]+)""")
    private val urlRegex = Regex("""id:entry:\d+|https?://([\w-]+\.)+[\w-]+(/[a-zA-Z0-9_\-+./!?%&=|^~#@*;:,<>()\[\]{}]*)?""")

    private suspend fun Bookmark.toDisplayBookmark(eid: Long, recursivable: Boolean = true) : DisplayBookmark {
        val ignoredUsers = ignoredUsersFlow.value
        val formatter = DateTimeFormatter.ofPattern("uuuuMMdd")
        val zoneOffset = ZoneOffset.ofHours(9)
        val date = formatter.format(timestamp.atOffset(zoneOffset))
        val url = "${HatenaClient.baseUrlB}${user}/$date#bookmark-$eid"
        val starsEntry =
            starsMapMutex.withLock {
                starsMap.getOrPut(url) { MutableStateFlow(StarsEntry(url = "", stars = emptyList())) }
            }

        val mentions =
            if (recursivable) {
                buildList {
                    val bookmarksEntry = entityFlow.value.bookmarksEntry
                    val allBookmarks = allBookmarksFlow.value
                    val idsCalled = idCallRegex.findAll(this@toDisplayBookmark.comment)
                    for (m in idsCalled) {
                        val id = m.groupValues[2]
                        if (id == this@toDisplayBookmark.user) {
                            continue
                        }
                        val b = allBookmarks.firstOrNull { it.bookmark.user == id }
                            ?: bookmarksEntry.bookmarks.firstOrNull { it.user == id }
                                ?.toBookmark(bookmarksEntry)
                                ?.toDisplayBookmark(eid, recursivable = false)
                        if (b != null) {
                            add(b)
                        }
                    }
                }
            }
            else emptyList()

        val urls = urlRegex.findAll(comment)
            .map {
                DisplayBookmark.Link(
                    url = it.value,
                    range = it.range
                )
            }
            .toList()

        val labels = userLabelRepo.userLabelsFlow(this.user)

        return DisplayBookmark(
            bookmark = this,
            tweetsAndClicks = null,
            bookmarksCount = 0,
            ignoredUser = ignoredUsers.contains(this.user),
            filtered = filters.any { it.match(this) },
            mentions = mentions,
            urls = urls,
            labels = labels,
            starsEntry = starsEntry,
            isMyBookmark = hatenaRepo.account.value?.let { it.name == this.user } ?: false
        )
    }

    override suspend fun makeDisplayBookmark(bookmark: Bookmark, eid: Long) : DisplayBookmark = bookmark.toDisplayBookmark(eid)

    /**
     * ユーザーを非表示にする
     */
    override suspend fun ignoreUser(user: String) {
        hatenaRepo.insertNgUser(user)
    }

    /**
     * ユーザー非表示を解除する
     */
    override suspend fun unIgnoreUser(user: String) {
        hatenaRepo.removeNgUser(user)
    }

    /**
     * ユーザーが非表示か否か
     */
    override fun isIgnored(user: String) : Boolean {
        return ignoredUsersFlow.value.contains(user)
    }

    /**
     * ブクマを通報する
     */
    override suspend fun report(report: Report) {
        hatenaRepo.withSignedClient { client ->
            client.bookmark.report(report)
        }
    }

    /**
     * 自身のブクマを削除する
     */
    override suspend fun deleteMyBookmark() {
        hatenaRepo.withSignedClient { client ->
            client.bookmark.deleteBookmark(entityFlow.value.requestUrl)
            myBookmarkFlow.value = null
            updateMyBookmark(null)
            // todo: starsMapからも除去する
        }
    }

    // ------ //

    /** スターをつけた人のブクマ情報のキャッシュ */
    private val starsToMap = HashMap<String, StateFlow<List<Mention>>>()

    /**
     * 指定ユーザーにつけられたスターとそのスターをつけたユーザーのブクマを取得する
     */
    override fun starsTo(
        entity: Entity,
        item: DisplayBookmark,
        coroutineScope: CoroutineScope
    ) : StateFlow<List<Mention>> {
        val entry = entity.entry
        val targetUser = item.bookmark.user

        val eid = entry.eid
        val formatter = DateTimeFormatter.ofPattern("uuuuMMdd")
        val zoneOffset = ZoneOffset.ofHours(9)

        val date = formatter.format(item.bookmark.timestamp.atOffset(zoneOffset))
        val url = "${HatenaClient.baseUrlB}${targetUser}/$date#bookmark-$eid"

        val flow = starsToMap[url]
        if (flow != null) return flow

        return MutableStateFlow(emptyList<Mention>()).also { mentionFlow ->
            starsToMap[url] = mentionFlow
            coroutineScope.launch(Dispatchers.Default) {
                val starsFlow = starsMapMutex.withLock {
                    starsMap.getOrPut(url) {
                        MutableStateFlow(StarsEntry(url = url, stars = emptyList())).also { f ->
                            launch(Dispatchers.IO) {
                                f.value = hatenaRepo.withClient { client -> client.star.getStarsEntry(url) }
                            }
                        }
                    }
                }

                starsFlow
                    .onEach {
                        (item.starsEntry as? MutableStateFlow<StarsEntry>)?.value = it
                    }
                    .map { starsEntry ->
                        starsEntry.allStars
                            .groupBy { s -> s.user }
                            .map { g ->
                                val user = g.key
                                val stars = g.value.map { s -> StarCount(color = s.color, count = s.count) }
                                val bookmark =
                                    entity.bookmarks.firstOrNull { b -> b.user == user }
                                        ?: entity.bookmarksEntry.bookmarks.firstOrNull { b -> b.user == user }
                                            ?.toBookmark(eid)
                                Mention(
                                    user = user,
                                    stars = stars,
                                    bookmark = bookmark,
                                    ignoredUser = isIgnored(user),
                                    filtered = false // todo
                                )
                            }
                            .asReversed()
                    }
                    .onEach { mentionFlow.value = it }
                    .flowOn(Dispatchers.Default)
                    .launchIn(this)
            }
        }
    }

    // ------ //

    /**
     * エントリIDからエントリのURLを取得する
     */
    override suspend fun getPageUrl(eid: Long) : String {
        return plainHatenaClient.entry.getUrl(eid)
    }
}

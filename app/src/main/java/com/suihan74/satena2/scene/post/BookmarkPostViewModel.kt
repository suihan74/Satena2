package com.suihan74.satena2.scene.post

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import com.suihan74.hatena.CertifiedHatenaClient
import com.suihan74.hatena.model.account.Tag
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.R
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.mastodon.TootVisibility
import com.suihan74.satena2.model.misskey.NoteVisibility
import com.suihan74.satena2.scene.bookmarks.DisplayBookmark
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.mastodon.MastodonAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.misskey.MisskeyAccountRepository
import com.suihan74.satena2.utility.DialogPropertiesProvider
import com.suihan74.satena2.utility.DialogPropertiesProviderImpl
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.exception.AlreadyExistsException
import com.suihan74.satena2.utility.hatena.actualUrl
import com.suihan74.satena2.utility.extension.getObjectExtra
import com.suihan74.satena2.utility.extension.showToast
import com.sys1yagi.mastodon4j.api.method.Statuses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.math.ceil

interface BookmarkPostViewModel : DialogPropertiesProvider {
    /**
     * Mastodon連携状態
     */
    val isAuthMastodon: Flow<Boolean>

    /**
     * Misskey連携状態
     */
    val isAuthMisskey: Flow<Boolean>

    /**
     * Twitter認証状態
     */
    val isAuthTwitter: Flow<Boolean>

    /**
     * Facebook認証状態
     */
    val isAuthFacebook: Flow<Boolean>

    /**
     * Mastodonに投稿する
     */
    val postMastodon: MutableStateFlow<Boolean>

    /**
     * Misskeyに投稿する
     */
    val postMisskey: MutableStateFlow<Boolean>

    /**
     * Twitterに投稿する
     */
    val postTwitter: MutableStateFlow<Boolean>

    /**
     * Facebookに投稿する
     */
    val postFacebook: MutableStateFlow<Boolean>

    /**
     * 投稿後に共有する
     */
    val sharing: MutableStateFlow<Boolean>

    /**
     * 非公開投稿する
     */
    val isPrivate: MutableStateFlow<Boolean>

    /**
     * 投稿前に確認する
     */
    val confirmBeforePosting: Flow<Boolean>

    /**
     * コメント
     */
    val comment: MutableStateFlow<String>

    /**
     * ユーザーが使用したことがあるタグリスト
     */
    val tags: StateFlow<List<Tag>>

    // ------ //

    /**
     * Mastodon: 投稿の公開範囲
     */
    val mastodonPostVisibility: StateFlow<TootVisibility>

    /**
     * Mastodon: 警告文
     */
    val mastodonSpoilerText: MutableStateFlow<String>

    // ------ //

    /**
     * Misskey: 投稿の公開範囲
     */
    val misskeyPostVisibility: StateFlow<NoteVisibility>

    /**
     * Misskey: 警告文
     */
    val misskeySpoilerText: MutableStateFlow<String>

    // ------ //

    /**
     * 縦位置
     */
    val verticalAlignment: StateFlow<Alignment>

    /**
     * コンテンツの外側をクリックしてダイアログを閉じる
     */
    val dismissOnClickOutside: StateFlow<Boolean>

    /**
     * 処理中かどうかのフラグ
     */
    val loadingFlow: StateFlow<Boolean>

    /**
     * 外部アプリから開かれたかどうか
     */
    val calledFromOtherAppsFlow: StateFlow<Boolean>

    /**
     * 投稿対象のエントリ
     */
    val entryFlow: StateFlow<Entry?>

    /**
     * 投稿結果
     */
    val bookmarkResultFlow: Flow<BookmarkResult?>

    // ------ //

    /**
     * 初期化。対象エントリ情報の設定
     */
    fun initialize(intent: Intent)

    /**
     * ブクマを投稿する
     */
    suspend fun postBookmark(context: Context)

    /**
     * Mastodonに投稿
     */
    suspend fun postMastodon(bookmark: BookmarkResult)

    /**
     * Misskeyに投稿
     */
    suspend fun postMisskey(bookmark: BookmarkResult)

    /**
     * 文字列からコメント長をはてブのルールにあわせて計算する
     *
     * 外部から分かる限りサーバ側での判定に極力近づけているが完璧である保証はない
     * タグは判定外で、その他部分はバイト数から判定している模様
     */
    fun calcCommentLength(commentRaw: String): Int =
        ceil(commentRaw.replace(tagRegex, "").sumOf { c ->
            val code = c.code
            when (code / 0xff) {
                0 -> 1
                1 -> if (code <= 0xc3bf) 1 else 3
                else -> 3
            }.toLong()
        } / 3f).toInt()

    /**
     * コメント長が投稿可能範囲内か確認する
     */
    fun checkCommentLength(commentRaw: String): Boolean =
        calcCommentLength(commentRaw) <= MAX_COMMENT_LENGTH

    /**
     * タグ数が投稿可能範囲内か確認する
     */
    fun checkTagsCount(commentRaw: String): Boolean {
        val matches = tagRegex.findAll(commentRaw)
        return matches.count() <= MAX_TAGS_COUNT
    }

    /**
     * 現在の編集内容を取得する
     */
    fun editData() = EditData(
        entry = entryFlow.value,
        comment = comment.value,
        private = isPrivate.value,
        postTwitter = postTwitter.value,
        postMastodon = postMastodon.value,
        postFacebook = postFacebook.value,
        postMisskey = postMisskey.value,
        sharing = sharing.value,
        mastodonSpoilerText = mastodonSpoilerText.value
    )

    /**
     * プレビュー用のダミーデータを作成する
     */
    fun createPreview(): DisplayBookmark?

    // ------ //

    /**
     * 与えられたコメントにタグを挿入して返す
     */
    fun insertTag(text: String, tag: String): Pair<Int, String>

    // ------ //

    /**
     * Mastodon: 公開範囲を変更
     */
    fun setMastodonPostVisibility(value: TootVisibility)

    // ------ //

    /**
     * Misskey: 公開範囲を変更
     */
    fun setMisskeyPostVisibility(value: NoteVisibility)

    // ------ //

    companion object {
        /** 最大コメント文字数 */
        const val MAX_COMMENT_LENGTH = 100

        /** 同時使用可能な最大タグ個数 */
        const val MAX_TAGS_COUNT = 10

        /** (単数の)タグを表現する正規表現 */
        @JvmStatic
        val tagRegex = Regex("""\[[^%/:\[\]]+]""")
    }
}

// ------ //

@HiltViewModel
class BookmarkPostViewModelImpl @Inject constructor(
    private val hatena: HatenaAccountRepository,
    private val mastodon: MastodonAccountRepository,
    private val misskey: MisskeyAccountRepository,
    private val dataStore: DataStore<Preferences>
) :
    BookmarkPostViewModel,
    ViewModel(),
    DialogPropertiesProvider by DialogPropertiesProviderImpl(dataStore) {
    private val clientFlow = hatena.client

    override val isAuthMastodon = mastodon.account.map { it != null }

    override val isAuthMisskey = misskey.account.map { it != null }

    override val isAuthTwitter = hatena.account.map { it?.isOAuthTwitter ?: false }

    override val isAuthFacebook = hatena.account.map { it?.isOAuthFaceBook ?: false }

    override val postMastodon = MutableStateFlow(false)

    override val postMisskey = MutableStateFlow(false)

    override val postTwitter = MutableStateFlow(false)

    override val postFacebook = MutableStateFlow(false)

    override val sharing = MutableStateFlow(false)

    override val isPrivate = MutableStateFlow(false)

    /**
     * 投稿前に確認する
     */
    override val confirmBeforePosting = dataStore.data.map { it.postBookmarkConfirmation }

    override val comment = MutableStateFlow("")

    override val tags = MutableStateFlow(emptyList<Tag>())

    private val tagRegex = Regex("""\[([^%/:\[\]]+)]""")
    private val tagsAreaRegex = Regex("""^(\[[^%/:\[\]]+])+""")

    // ------ //

    /**
     * Mastodon: 投稿の公開範囲
     */
    override val mastodonPostVisibility = mastodon.postVisibility

    /**
     * Mastodon: 警告文
     */
    override val mastodonSpoilerText = MutableStateFlow("")

    // ------ //

    /**
     * Misskey: 投稿の公開範囲
     */
    override val misskeyPostVisibility = misskey.postVisibility

    /**
     * Misskey: 警告文
     */
    override val misskeySpoilerText = MutableStateFlow("")

    // ------ //

    override val verticalAlignment = MutableStateFlow(Alignment.Center)

    override val dismissOnClickOutside = MutableStateFlow(false)

    // ------ //

    /**
     * 処理中かどうかのフラグ
     */
    override val loadingFlow = MutableStateFlow(false)

    /**
     * 外部アプリから開かれたかどうか
     */
    override val calledFromOtherAppsFlow = MutableStateFlow(false)

    /**
     * 投稿対象のエントリ
     */
    override val entryFlow = MutableStateFlow<Entry?>(null)

    /**
     * 投稿結果
     */
    override val bookmarkResultFlow = MutableSharedFlow<BookmarkResult?>()

    // ------ //

    init {
        // 設定を反映
        dataStore.data
            .onEach {
                dismissOnClickOutside.value = it.dismissDialogOnClickOutside
                verticalAlignment.value =
                    when (it.postBookmarkDialogVerticalAlignment) {
                        Alignment.CenterVertically -> Alignment.Center
                        Alignment.Top -> Alignment.TopCenter
                        Alignment.Bottom -> Alignment.BottomCenter
                        else -> Alignment.Center
                    }
            }
            .map {
                if (it.postBookmarkSaveStates) it.postBookmarkLastStates
                else it.postBookmarkDefaultStates
            }
            .onEach {
                postMastodon.value = it.mastodon
                postTwitter.value = it.twitter
                postFacebook.value = it.facebook
                sharing.value = it.sharing
                isPrivate.value = it.private
            }
            .launchIn(viewModelScope)

        // 各種スイッチ選択状態を設定に反映
        combine(
            postMastodon,
            postTwitter,
            postFacebook,
            postMisskey
        ) { mastodon, twitter, facebook, misskey ->
            PostStates(
                mastodon = mastodon,
                misskey = misskey,
                twitter = twitter,
                facebook = facebook,
                sharing = sharing.value,
                private = isPrivate.value
            )
        }
            .onEach { states ->
                dataStore.updateData { prefs -> prefs.copy(postBookmarkLastStates = states) }
            }
            .launchIn(viewModelScope)

        combine(sharing, isPrivate) { sharing, private ->
            PostStates(
                mastodon = postMastodon.value,
                misskey = postMisskey.value,
                twitter = postTwitter.value,
                facebook = postFacebook.value,
                sharing = sharing,
                private = private
            )
        }.onEach { states ->
            dataStore.updateData { prefs -> prefs.copy(postBookmarkLastStates = states) }
        }.launchIn(viewModelScope)

        // サインイン後にタグ取得
        clientFlow
            .onEach { client ->
                tags.value =
                    if (client is CertifiedHatenaClient) {
                        runCatching {
                            client.user.getUserTags()
                        }.getOrDefault(emptyList())
                    }
                    else emptyList()
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * 初期化。対象エントリ情報の設定
     */
    override fun initialize(intent: Intent) {
        val editData = intent.getObjectExtra<EditData>(PostBookmarkActivityContract.EXTRA_EDIT_DATA)
        if (editData == null) {
            initializeForFirstLaunch(intent)
        }
        else {
            initializeForContinue(editData)
        }
    }

    private fun initializeForContinue(editData: EditData) {
        editData.let {
            entryFlow.value = it.entry
            comment.value = it.comment
            isPrivate.value = it.private
            postTwitter.value = it.postTwitter
            postFacebook.value = it.postFacebook
            postMastodon.value = it.postMastodon
            postMisskey.value = it.postMisskey
            sharing.value = it.sharing
            mastodonSpoilerText.value = it.mastodonSpoilerText
        }
    }

    private fun initializeForFirstLaunch(intent: Intent) {
        viewModelScope.launch {
            loadingFlow.value = true
            runCatching {
                entryFlow.value =
                    intent.getObjectExtra<Entry>(PostBookmarkActivityContract.EXTRA_ENTRY)
                        ?: loadEntryFromUrl(intent.getStringExtra(Intent.EXTRA_TEXT)!!)
                comment.value = entryFlow.value?.bookmarkedData?.commentRaw.orEmpty()
            }.onFailure {
                context.showToast(R.string.bookmark_entry_loading_failure_msg)
            }
            loadingFlow.value = false
        }
    }

    private suspend fun loadEntryFromUrl(url: String): Entry {
        val client = clientFlow.value
        calledFromOtherAppsFlow.value = true
        return client.entry.getEntry(url)
    }

    /**
     * ブクマを投稿する
     */
    override suspend fun postBookmark(context: Context) {
        loadingFlow.value = true
        runCatching {
            val comment = comment.value.also {
                require(checkCommentLength(it)) { context.getString(R.string.post_too_long_comment_msg) }
                require(checkTagsCount(it)) { context.getString(R.string.post_too_many_tags_msg) }
            }
            val url = entryFlow.value!!.actualUrl()
            val client = clientFlow.value as CertifiedHatenaClient
            client.bookmark.postBookmark(
                url = url,
                comment = comment,
                postTwitter = postTwitter.value,
                postFacebook = postFacebook.value,
                private = isPrivate.value
            )
        }.onFailure { e ->
            val msg = when (e) {
                is IllegalArgumentException -> e.message.orEmpty()
                is ClassCastException -> context.getString(R.string.post_sign_in_failure_msg)
                is NullPointerException -> context.getString(R.string.post_fetch_entry_failure_msg)
                else -> context.getString(R.string.post_bookmark_failure_msg)
            }
            context.showToast(msg)
            loadingFlow.value = false
        }.onSuccess {
            coroutineScope {
                val tasks = listOf(
                    async { postMastodon(it) },
                    async { postMisskey(it) }
                )
                tasks.awaitAll()
            }
            bookmarkResultFlow.emit(it)
            context.showToast(R.string.post_bookmark_success_msg)
            // 投稿後に共有
            if (sharing.value) {
                shareAfterPosting(context, it, entryFlow.value!!)
            }
            loadingFlow.value = false
        }
    }

    /**
     * Mastodonに投稿
     */
    override suspend fun postMastodon(bookmark: BookmarkResult) {
        if (isPrivate.value || !postMastodon.value) return
        runCatching {
            val entry = entryFlow.value!!
            val visibility = mastodon.postVisibility.value.value
            val spoilerText = mastodonSpoilerText.value
            val status = makeSharingText(bookmark.comment, entry)
            mastodon.withSignedClient { client ->
                Statuses(client).postStatus(
                    status = status,
                    inReplyToId = null,
                    mediaIds = null,
                    sensitive = false,
                    visibility = visibility,
                    spoilerText = spoilerText.ifBlank { null }
                ).execute()
            }
        }.onFailure {
            Log.e("mastodon", it.stackTraceToString())
            context.showToast(R.string.post_mastodon_failure_msg)
        }
    }

    /**
     * Misskeyに投稿
     */
    override suspend fun postMisskey(bookmark: BookmarkResult) {
        if (isPrivate.value || !postMisskey.value) return
        runCatching {
            val entry = entryFlow.value!!
            val visibility = misskey.postVisibility.value.value
            val spoilerText = mastodonSpoilerText.value
            val status = makeSharingText(bookmark.comment, entry)
            misskey.withSignedClient { client ->
                client.notes.create(
                    text = status,
                    visibility = visibility,
                    cw = spoilerText.ifEmpty { null }
                )
            }
        }.onFailure {
            Log.e("misskey", it.stackTraceToString())
            context.showToast(R.string.post_misskey_failure_msg)
        }
    }

    /**
     * 投稿完了後に共有機能に連携
     */
    private fun shareAfterPosting(context: Context, bookmark: BookmarkResult, entry: Entry) {
        val intent = Intent(Intent.ACTION_SEND).also {
            it.putExtra(Intent.EXTRA_TEXT, makeSharingText(bookmark.comment, entry))
            it.type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "ブクマを共有"))
    }

    private fun makeSharingText(comment: String, entry: Entry) : String {
        return buildString {
            val replyRegex = Regex("""@([a-zA-Z0-9_]+)""")
            if (comment.isNotBlank()) {
                // コメントに"@hoge"が含まれている場合リプライにならないようにする
                append(
                    replyRegex.replace(comment) { r -> "@\u202A" + r.groupValues[1] },
                    " / "
                )
            }
            append(
                "\"",
                replyRegex.replace(entry.title) { r -> "@\u202A" + r.groupValues[1] },
                "\" ",
                entry.actualUrl()
            )
        }
    }

    // ------ //

    /**
     * 与えられたコメントにタグを挿入して返す
     */
    override fun insertTag(text: String, tag: String): Pair<Int, String> {
        val tagsAreaMatchResult = tagsAreaRegex.find(text)
        return tagsAreaMatchResult?.value?.let { tagsArea ->
            runCatching {
                val byteSize = tag.toByteArray().size
                if (byteSize >= 32) throw IllegalArgumentException()

                val tags = tagRegex.findAll(tagsArea)
                if (tags.count() >= 10) throw IndexOutOfBoundsException()
                if (tags.any { it.groups[1]?.value == tag }) throw AlreadyExistsException()
                val resultText = buildString {
                    append(
                        tagsArea,
                        "[", tag, "]",
                        text.substring(tagsArea.length)
                    )
                }
                tagsArea.length to resultText
            }.onFailure {
                viewModelScope.launch {
                    val res = when (it) {
                        is IllegalArgumentException -> R.string.post_too_long_tag_msg
                        is IndexOutOfBoundsException -> R.string.post_too_many_tags_msg
                        is AlreadyExistsException -> R.string.post_already_exists_tags_msg
                        else -> R.string.error
                    }
                    context.showToast(res)
                }
            }.getOrThrow()
        } ?: (0 to "[$tag]$text")
    }

    /**
     * rawCommentからコメントとタグを分離して返す
     */
    private fun separateCommentAndTags() : Pair<String, List<String>> {
        val rawComment = comment.value
        return tagsAreaRegex.find(rawComment)?.value?.let { tagsArea ->
            val comment = rawComment.substring(tagsArea.length)
            val tags = tagRegex.findAll(tagsArea)
                .map { it.groups[1]?.value.orEmpty() }
                .toList()
            comment to tags
        } ?: (rawComment to emptyList())
    }

    // ------ //

    /**
     * Mastodon: 公開範囲を変更
     */
    override fun setMastodonPostVisibility(value: TootVisibility) {
        viewModelScope.launch {
            mastodon.updatePostVisibility(value)
        }
    }

    // ------ //

    /**
     * Misskey: 公開範囲を変更
     */
    override fun setMisskeyPostVisibility(value: NoteVisibility) {
        viewModelScope.launch {
            misskey.updatePostVisibility(value)
        }
    }

    // ------ //

    /**
     * プレビュー用のダミーデータを作成する
     */
    override fun createPreview(): DisplayBookmark? {
        val account = hatena.account.value ?: return null
        val isPrivate = isPrivate.value
        val client = clientFlow.value
        val (comment, tags) = separateCommentAndTags()
        return DisplayBookmark(
            bookmark = Bookmark(
                _user = Bookmark.User(
                    name = account.name,
                    profileImageUrl = client.user.getUserIconUrl(account.name)
                ),
                comment = comment,
                isPrivate = isPrivate,
                link = "",
                tags = tags,
                timestamp = Instant.now(),
                starCount = emptyList()
            )
        )
    }
}

// ------ //

class FakeBookmarkPostViewModel(
    testEntry: Entry? = null,
    loading: Boolean = false
) : BookmarkPostViewModel {
    override val isAuthMastodon = MutableStateFlow(true)

    override val isAuthMisskey = MutableStateFlow(true)

    override val isAuthTwitter = MutableStateFlow(true)

    override val isAuthFacebook = MutableStateFlow(true)

    override val postMastodon = MutableStateFlow(false)

    override val postMisskey = MutableStateFlow(false)

    override val postTwitter = MutableStateFlow(false)

    override val postFacebook = MutableStateFlow(false)

    override val sharing = MutableStateFlow(false)

    override val isPrivate = MutableStateFlow(false)

    override val confirmBeforePosting = MutableStateFlow(false)

    override val comment = MutableStateFlow("")

    override val tags = MutableStateFlow(emptyList<Tag>())

    // ------ //

    override val mastodonPostVisibility = MutableStateFlow(TootVisibility.Public)

    override val mastodonSpoilerText = MutableStateFlow("")

    // ------ //

    override val misskeyPostVisibility = MutableStateFlow(NoteVisibility.Public)

    override val misskeySpoilerText = MutableStateFlow("")

    // ------ //

    override val verticalAlignment = MutableStateFlow(Alignment.Center)

    override val dismissOnClickOutside = MutableStateFlow(false)

    // ------ //

    override val loadingFlow = MutableStateFlow(loading)

    override val calledFromOtherAppsFlow = MutableStateFlow(testEntry != null)

    override val entryFlow = MutableStateFlow(testEntry)

    override val bookmarkResultFlow = MutableSharedFlow<BookmarkResult?>()

    // ------ //

    override fun initialize(intent: Intent) {
    }

    override suspend fun postBookmark(context: Context) {
    }

    override suspend fun postMastodon(bookmark: BookmarkResult) {
    }

    override suspend fun postMisskey(bookmark: BookmarkResult) {
    }

    // ------ //

    override fun insertTag(text: String, tag: String): Pair<Int, String> {
        return 0 to ""
    }

    // ------ //

    override fun setMastodonPostVisibility(value: TootVisibility) {
        mastodonPostVisibility.value = value
    }

    override fun setMisskeyPostVisibility(value: NoteVisibility) {
        misskeyPostVisibility.value = value
    }

    // ------ //

    override fun createPreview(): DisplayBookmark? {
        return null
    }
}

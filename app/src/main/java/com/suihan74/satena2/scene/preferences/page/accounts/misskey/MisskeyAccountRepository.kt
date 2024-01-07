package com.suihan74.satena2.scene.preferences.page.accounts.misskey

import android.content.Context
import androidx.datastore.core.DataStore
import com.suihan74.misskey.AuthorizedMisskeyClient
import com.suihan74.misskey.Misskey
import com.suihan74.misskey.api.auth.AppCredential
import com.suihan74.misskey.api.auth.GenerateSessionResponse
import com.suihan74.satena2.Application
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.mastodon.MastodonAccessToken
import com.suihan74.satena2.model.misskey.NoteVisibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias MisskeyAccount = com.suihan74.misskey.entity.Account

interface MisskeyAccountRepository {
    /**
     * アカウント情報
     */
    val account : StateFlow<MisskeyAccount?>

    /**
     * 接続先インスタンス
     */
    val instance : StateFlow<String>

    /**
     * ノートの公開範囲
     */
    val postVisibility: StateFlow<NoteVisibility>

    // ------ //

    /**
     * サインインを待機してから[AuthorizedMisskeyClient]を取得し、処理を実施する
     */
    suspend fun <T> withSignedClient(action: suspend (AuthorizedMisskeyClient)->T) : T?

    // ------ //

    /**
     * 全アカウントをリロード
     */
    suspend fun reload()

    // ------ //

    /**
     * 認証画面のURL・認証用トークンを生成する
     */
    suspend fun authSession(instance: String) : Pair<AppCredential, GenerateSessionResponse>

    /**
     * ユーザーが認証画面後、アクセストークンを取得する
     */
    suspend fun continueAuthorization(
        context: Context,
        instance: String,
        appCredential: AppCredential,
        session: GenerateSessionResponse
    )

    /**
     * ノートの公開範囲の設定を更新する
     */
    suspend fun updatePostVisibility(value: NoteVisibility)

    /**
     * アカウント連携を終了する
     */
    suspend fun signOut()
}

// ------ //

class MisskeyAccountRepositoryImpl(
    application: Application,
    private val dataStore: DataStore<Preferences>
) : MisskeyAccountRepository {
    /**
     * 認証済クライアント
     */
    private val client = MutableStateFlow<AuthorizedMisskeyClient?>(null)

    /**
     * アカウント情報
     */
    override val account = MutableStateFlow<MisskeyAccount?>(null)

    /**
     * 接続先インスタンス
     */
    override val instance = MutableStateFlow("")

    /**
     * アクセストークン
     */
    private val accessToken = MutableStateFlow<MastodonAccessToken?>(null)

    /**
     * ノートの公開範囲
     */
    override val postVisibility = MutableStateFlow(NoteVisibility.Public)

    // ------ //

    private val accountMutex = Mutex()

    // ------ //

    /**
     * サインインを待機してから[AuthorizedMisskeyClient]を取得し、処理を実施する
     */
    override suspend fun <T> withSignedClient(action: suspend (AuthorizedMisskeyClient)->T) : T? {
        val client = accountMutex.withLock { client.value } ?: return null
        return action(client)
    }

    // ------ //

    /**
     * 全アカウントをリロード
     */
    override suspend fun reload() {
        onUpdateAccessToken(accessToken.value)
    }

    // ------ //

    private suspend fun onUpdateAccessToken(accessToken: MastodonAccessToken?) {
        accountMutex.withLock {
            this.accessToken.value = accessToken
            this.instance.value = accessToken?.instanceName.orEmpty()

            if (accessToken == null) {
                client.value = null
                account.value = null
            }
            else {
                val client = Misskey.Client(
                    instance = accessToken.instanceName,
                    tokenDigest = accessToken.accessToken
                )
                this.client.value = client
                runCatching {
                    account.value = client.account.i()
                }.onFailure {
                    account.value = null
                }
            }
        }
    }

    private fun onUpdatePostVisibility(value: NoteVisibility) {
        if (postVisibility.value == value) return
        postVisibility.value = value
    }

    // ------ //

    /**
     * 認証画面のURLを生成する
     */
    override suspend fun authSession(instance: String) : Pair<AppCredential, GenerateSessionResponse> {
        val appCredential = Misskey.auth.createApp(
            instance = instance,
            name = "Satena",
            description = "はてなブックマークの非公式アプリ",
            permissions = listOf("write:notes", "read:account"),
            callbackUrl = "satena-misskey://$instance/callback"
        )
        return appCredential to Misskey.auth.generateSession(appCredential)
    }

    /**
     * ユーザーが認証画面後、アクセストークンを取得し保存する
     */
    override suspend fun continueAuthorization(
        context: Context,
        instance: String,
        appCredential: AppCredential,
        session: GenerateSessionResponse
    ) {
        val accessToken = Misskey.auth.getAccessToken(appCredential, session)

        dataStore.updateData {
            it.copy(
                misskeyAccessToken = MastodonAccessToken(instanceName = instance, accessToken = accessToken)
            )
        }
    }

    /**
     * ノートの公開範囲の設定を更新する
     */
    override suspend fun updatePostVisibility(value: NoteVisibility) {
        dataStore.updateData {
            it.copy(misskeyPostVisibility = value)
        }
    }

    /**
     * アカウント連携を終了する
     */
    override suspend fun signOut() {
        dataStore.updateData {
            it.copy(misskeyAccessToken = null)
        }
    }

    // ------ //

    init {
        dataStore.data
            .onEach {
                onUpdateAccessToken(it.misskeyAccessToken)
                onUpdatePostVisibility(it.misskeyPostVisibility)
            }
            .flowOn(Dispatchers.IO)
            .launchIn(application.coroutineScope)
    }
}


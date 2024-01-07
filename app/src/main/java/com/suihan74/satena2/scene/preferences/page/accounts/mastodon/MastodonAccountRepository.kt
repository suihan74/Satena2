package com.suihan74.satena2.scene.preferences.page.accounts.mastodon

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.gson.Gson
import com.suihan74.satena2.Application
import com.suihan74.satena2.R
import com.suihan74.satena2.model.mastodon.MastodonAccessToken
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.mastodon.TootVisibility
import com.suihan74.satena2.serializer.AppRegistrationSerializer
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration
import com.sys1yagi.mastodon4j.api.method.Apps
import com.sys1yagi.mastodon4j.extension.fromJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import okhttp3.OkHttpClient

typealias MastodonAccount = com.sys1yagi.mastodon4j.api.entity.Account

interface MastodonAccountRepository {
    /**
     * 連携状態
     */
    val signedIn: StateFlow<Boolean>

    /**
     * アカウント情報
     */
    val account: StateFlow<MastodonAccount?>

    /**
     * 接続先インスタンス情報
     */
    val instance: StateFlow<String>

    /**
     * トゥートの公開範囲
     */
    val postVisibility: StateFlow<TootVisibility>

    // ------ //

    /**
     * 全アカウントをリロード
     */
    suspend fun reload()

    // ------ //

    /**
     * 認証を待機してから[MastodonClient]を取得し、処理を実施する
     */
    suspend fun <T> withSignedClient(action: suspend (MastodonClient) -> T): T?

    /**
     * 認証解除
     */
    suspend fun signOut()

    /**
     * OAuth認証画面のURLを生成する
     */
    suspend fun oAuthUrl(context: Context, instance: String): String

    /**
     * OAuth認証画面から戻ってきたときのトークン処理
     */
    suspend fun continueAuthorization(context: Context, instance: String, code: String)

    /**
     * トゥートの公開範囲の設定を更新する
     */
    suspend fun updatePostVisibility(value: TootVisibility)
}

// ------ //

class MastodonAccountRepositoryImpl(
    application: Application,
    private val dataStore: DataStore<Preferences>
) : MastodonAccountRepository {
    private val accessToken = MutableStateFlow<MastodonAccessToken?>(null)

    val client = MutableStateFlow<MastodonClient?>(null)

    private val accountMutex = Mutex()

    /**
     * 連携状態
     */
    override val signedIn = MutableStateFlow(false)

    /**
     * アカウント情報
     */
    override val account = MutableStateFlow<MastodonAccount?>(null)

    /**
     * 接続先インスタンス情報
     */
    override val instance = MutableStateFlow("")

    /**
     * Mastodon: トゥートの公開範囲
     */
    override val postVisibility = MutableStateFlow(TootVisibility.Public)

    // ------ //

    /**
     * 全アカウントをリロード
     */
    override suspend fun reload() {
        onUpdateAccessToken(accessToken.value)
    }

    // ------ //

    /**
     * 投稿の公開範囲設定が更新されたらリポジトリに反映する
     */
    private fun onUpdatePostVisibility(value: TootVisibility) {
        if (postVisibility.value == value) return
        postVisibility.value = value
    }

    /**
     * アクセストークンが更新されたらリポジトリに反映する
     */
    private suspend fun onUpdateAccessToken(token: MastodonAccessToken?) {
        accountMutex.withLock {
            if (accessToken.value == token) return
            if (token == null) {
                client.value = null
                accessToken.value = null
                account.value = null
                instance.value = ""
                signedIn.value = false
            }
            else {
                client.value = MastodonClient.Builder(
                    token.instanceName,
                    OkHttpClient.Builder(),
                    Gson()
                ).accessToken(token.accessToken).build()
                accessToken.value = token
                account.value = getAccount(token)
                instance.value = token.instanceName
                signedIn.value = true
            }
        }
    }

    // ------ //

    /**
     * 認証を待機してから[MastodonClient]を取得し、処理を実施する
     */
    override suspend fun <T> withSignedClient(action: suspend (MastodonClient)->T) : T? {
        val client = accountMutex.withLock { client.value } ?: return null
        return withContext(Dispatchers.IO) { action(client) }
    }

    // ------ //

    override suspend fun signOut() {
        dataStore.updateData {
            it.copy(mastodonAccessToken = null)
        }
    }

    /**
     * Mastodon: 取得済みの[AppRegistration]を読み込む
     */
    private suspend fun readMastodonAppRegistration(
        context: Context,
        apps: Apps,
        instance: String
    ) : AppRegistration = withContext(Dispatchers.IO) {
        runCatching {
            val jsonFormat = Json {
                serializersModule = SerializersModule {
                    contextual(AppRegistrationSerializer())
                }
            }
            val filename = "mstdn_app_reg_$instance"
            context.openFileInput(filename).bufferedReader().useLines { lines ->
                val json = lines.fold("") { some, text -> "$some\n$text" }
                jsonFormat.decodeFromString<AppRegistration>(json)
            }
        }.getOrElse {
            apps.createApp(
                "Satena for Android",
                "satena-mastodon://$instance/callback",
                Scope(Scope.Name.ALL),
                context.getString(R.string.developer_website)
            ).execute().also {
                writeAppRegistration(context, instance, it)
            }
        }
    }

    /**
     * Mastodon: [AppRegistration]情報を保存する
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun writeAppRegistration(
        context: Context,
        instance: String,
        appRegistration: AppRegistration
    ) = withContext(Dispatchers.IO) {
        val jsonFormat = Json {
            serializersModule = SerializersModule {
                contextual(AppRegistrationSerializer())
            }
        }
        val filename = "mstdn_app_reg_$instance"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            val json = jsonFormat.encodeToString(appRegistration)
            it.write(json.toByteArray())
        }
    }

    /**
     * Mastodon: OAuth認証画面のURLを生成する
     */
    override suspend fun oAuthUrl(context: Context, instance: String) : String {
        val client = MastodonClient.Builder(
            instance,
            OkHttpClient.Builder(),
            Gson()
        ).build()
        val apps = Apps(client)
        val appRegistration = readMastodonAppRegistration(context, apps, instance)
        return apps.getOAuthUrl(
            clientId = appRegistration.clientId,
            scope = Scope(Scope.Name.ALL),
            redirectUri = "satena-mastodon://$instance/callback"
        )
    }

    /**
     * Mastodon: OAuth認証画面から戻ってきたときのトークン処理
     */
    override suspend fun continueAuthorization(
        context: Context,
        instance: String,
        code: String
    ) : Unit = withContext(Dispatchers.Default) {
        val client =
            MastodonClient.Builder(
                instance,
                OkHttpClient.Builder(),
                Gson()
            ).build()
        val apps = Apps(client)
        val appRegistration = readMastodonAppRegistration(context, apps, instance)
        val accessToken =
            appRegistration.let {
                apps.getAccessToken(
                    it.clientId,
                    it.clientSecret,
                    it.redirectUri,
                    code,
                    "authorization_code"
                ).execute()
            }
        dataStore.updateData {
            it.copy(
                mastodonAccessToken = MastodonAccessToken(
                    instanceName = instance,
                    accessToken = accessToken.accessToken
                )
            )
        }
    }

    /**
     * Mastodon: アカウント情報を明示的に取得
     */
    private suspend fun getAccount(
        accountData: MastodonAccessToken?
    ) : MastodonAccount? = withContext(Dispatchers.IO) {
        accountData?.let {
            runCatching {
                val client = MastodonClient.Builder(
                    accountData.instanceName,
                    OkHttpClient.Builder(),
                    Gson()
                ).accessToken(accountData.accessToken).build()
                client.get("accounts/verify_credentials")
                    .fromJson(Gson(), MastodonAccount::class.java)
            }.getOrNull()
        }
    }

    /**
     * Mastodon: トゥートの公開範囲の設定を更新する
     */
    override suspend fun updatePostVisibility(value: TootVisibility) {
        dataStore.updateData {
            it.copy(mastodonPostVisibility = value)
        }
    }

    // ------ //

    init {
        dataStore.data
            .onEach {
                onUpdatePostVisibility(it.mastodonPostVisibility)
                onUpdateAccessToken(it.mastodonAccessToken)
            }
            .flowOn(Dispatchers.IO)
            .launchIn(application.coroutineScope)
    }
}

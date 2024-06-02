package com.suihan74.satena2.scene.preferences.page.accounts.hatena

import androidx.datastore.core.DataStore
import com.suihan74.hatena.CertifiedHatenaClient
import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.HatenaClientBase
import com.suihan74.hatena.model.account.Account
import com.suihan74.satena2.Application
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.hatena.HatenaAccessToken
import com.suihan74.satena2.scene.preferences.page.accounts.SignInState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class HatenaSignInException(
    override val cause: Throwable? = null,
    override val message: String? = null
) : Throwable()

class HatenaFetchNgUsersException(
    override val cause: Throwable? = null,
    override val message: String? = null
) : Throwable()

// ------ //

interface HatenaAccountRepository {

    /**
     * サインイン状態
     */
    val state: StateFlow<SignInState>

    /**
     * アカウント情報
     */
    val account: StateFlow<Account?>

    /**
     * APIコール用クライアント
     */
    val client: StateFlow<HatenaClientBase>

    // ------ //

    /**
     * 非表示ユーザー名リスト
     */
    val ngUsers: StateFlow<List<String>>

    /**
     * 非表示ユーザー名リストのロード実行状態
     *
     * 比較的時間がかかる処理のため読み込み状態を参照できるようにする
     */
    val loadingNgUsers: StateFlow<Boolean>

    // ------ //

    /**
     * 非同期的な処理失敗を通知する[Flow]
     */
    val exceptionFlow : Flow<Throwable>

    // ------ //

    /**
     * サインインを待機してから[HatenaClient]を取得
     */
    suspend fun getClient() : HatenaClientBase

    /**
     * サインインを待機してから[CertifiedHatenaClient]を取得
     */
    suspend fun getSignedClient() : CertifiedHatenaClient?

    /**
     * サインインを待機してから[HatenaClient]を取得し、処理を実施する
     */
    suspend fun <T> withClient(action: suspend (HatenaClientBase)->T) : T

    /**
     * サインインを待機してから[CertifiedHatenaClient]を取得し、処理を実施する
     */
    suspend fun <T> withSignedClient(action: suspend (CertifiedHatenaClient)->T) : T?

    /**
     * アカウントをリロード
     */
    suspend fun reload()

    /**
     * 認証キーを保存する(空文字でサインアウト)
     */
    suspend fun saveRk(rk: String?)

    /**
     * NGユーザーを再取得する
     */
    suspend fun reloadNgUsers()

    /**
     * アカウントを(明示的に)取得する
     */
    suspend fun getAccount(): Account?

    /**
     * NGユーザー名リストを明示的に取得する
     */
    suspend fun getNgUsers(): List<String>

    /**
     * NGユーザーを追加する
     */
    suspend fun insertNgUser(username: String)

    /**
     * NGユーザーを解除する
     */
    suspend fun removeNgUser(username: String)
}

// ------ //

class HatenaAccountRepositoryImpl(
    application: Application,
    private val dataStore: DataStore<Preferences>
) : HatenaAccountRepository {
    /**
     * キー情報
     */
    private val hatenaRK = MutableStateFlow("")

    /**
     * サインイン状態
     */
    override val state = MutableStateFlow(SignInState.None)

    /**
     * アカウント情報
     */
    override val account = MutableStateFlow<Account?>(null)

    /**
     * APIコール用クライアント
     */
    override val client = MutableStateFlow<HatenaClientBase>(HatenaClient)

    // ------ //

    /**
     * NGユーザー名リスト
     */
    override val ngUsers = MutableStateFlow<List<String>>(emptyList())

    /**
     * 非表示ユーザー名リストのロード実行状態
     *
     * 比較的時間がかかる処理のため読み込み状態を参照できるようにする
     */
    override val loadingNgUsers = MutableStateFlow(false)

    // ------ //

    /**
     * アカウント操作用[Mutex]
     */
    private val accountMutex = Mutex()

    /**
     * NGユーザー操作用[Mutex]
     */
    private val ngUsersMutex = Mutex()

    // ------ //

    /**
     * 非同期的な処理失敗を通知する[Flow]
     */
    override val exceptionFlow = MutableSharedFlow<Throwable>()

    // ------ //

    /**
     * RKが更新されたときのイベントハンドラ
     */
    private suspend fun onUpdateRK(rk: String) {
        withContext(Dispatchers.IO) {
            accountMutex.withLock {
                if (rk.isNotBlank() && hatenaRK.value == rk) return@withContext
                runCatching {
                    state.value = SignInState.Signing
                    hatenaRK.value = rk
                    client.value =
                        if (rk.isBlank()) HatenaClient
                        else HatenaClient.signIn(rk)
                    state.value =
                        if (rk.isBlank()) SignInState.None
                        else SignInState.SignedIn
                }.onFailure {
                    hatenaRK.value = ""
                    client.value = HatenaClient
                    state.value = SignInState.None
                    account.value = null
                    exceptionFlow.emit(HatenaSignInException(cause = it))
                    return@withContext
                }
            }

            if (hatenaRK.value.isNotBlank()) {
                launch {
                    runCatching {
                        account.value = getAccount()
                    }.onFailure {
                        hatenaRK.value = ""
                        client.value = HatenaClient
                        state.value = SignInState.None
                        account.value = null
                        exceptionFlow.emit(HatenaSignInException(cause = it))
                    }
                }

                launch {
                    ngUsersMutex.withLock {
                        runCatching {
                            ngUsers.value = getNgUsers()
                        }.onFailure {
                            ngUsers.value = emptyList()
                            exceptionFlow.emit(HatenaFetchNgUsersException(cause = it))
                        }
                    }
                }
            }
        }
    }

    /**
     * NGユーザーを再取得する
     */
    override suspend fun reloadNgUsers() {
        ngUsersMutex.withLock {
            runCatching {
                ngUsers.value = getNgUsers()
            }.onFailure {
                exceptionFlow.emit(HatenaFetchNgUsersException(cause = it))
            }
        }
    }

    // ------ //

    /**
     * サインインを待機してから[HatenaClient]を取得
     */
    override suspend fun getClient() : HatenaClientBase {
        return accountMutex.withLock { client.value }
    }

    /**
     * サインインを待機してから[CertifiedHatenaClient]を取得
     */
    override suspend fun getSignedClient() : CertifiedHatenaClient? {
        return accountMutex.withLock { client.value } as? CertifiedHatenaClient
    }

    /**
     * サインインを待機してから[HatenaClient]を取得し、処理を実施する
     */
    override suspend fun <T> withClient(action: suspend (HatenaClientBase)->T) : T {
        return withContext(Dispatchers.IO) {
            val client = accountMutex.withLock { client.value }
            action(client)
        }
    }

    /**
     * サインインを待機してから[CertifiedHatenaClient]を取得し、処理を実施する
     */
    override suspend fun <T> withSignedClient(action: suspend (CertifiedHatenaClient)->T) : T? {
        return withContext(Dispatchers.IO) {
            val signedClient = accountMutex.withLock { client.value } as? CertifiedHatenaClient ?: return@withContext null
            action(signedClient)
        }
    }

    /**
     * サインイン状態によって処理をスイッチする
     */
    private suspend inline fun <T> HatenaClientBase.act(
        crossinline withSign: suspend (client: CertifiedHatenaClient)->T,
        crossinline withoutSign: suspend (client: HatenaClientBase)->T
    ) : T = accountMutex.withLock {
        when (val c = this) {
            is CertifiedHatenaClient -> withSign(c)
            else -> withoutSign(c)
        }
    }

    // ------ //

    /**
     * アカウントをリロード
     */
    override suspend fun reload() {
        onUpdateRK(hatenaRK.value)
    }

    // ------ //

    /**
     * 認証キーを保存(空文字でサインアウト)
     */
    override suspend fun saveRk(rk: String?) {
        dataStore.updateData { p ->
            p.copy(
                hatenaRK = rk?.let { HatenaAccessToken(rk = it) }
            )
        }
    }

    /**
     * アカウントを(明示的に)取得する
     */
    override suspend fun getAccount() : Account? = client.value.act(
        withSign = { it.user.getAccount() },
        withoutSign = { null }
    )

    /**
     * NGユーザー名リストを明示的に取得する
     *
     * @throws HatenaException 通信失敗
     */
    override suspend fun getNgUsers() : List<String> = client.value.act(
        withSign = { client ->
            loadingNgUsers.value = true
            val result = runCatching {
                val response = client.user.getIgnoredUsersAll()
                response.users.reversed()  // 設定画面で最新が上の方になるようにするため反転
            }
            loadingNgUsers.value = false
            result.getOrThrow()
        },
        withoutSign = {
            loadingNgUsers.value = false
            emptyList()
        }
    )

    /**
     * NGユーザーを追加する
     *
     * @throws HatenaException 通信失敗
     */
    override suspend fun insertNgUser(username: String) {
        ngUsersMutex.withLock {
            if (ngUsers.value.contains(username)) return
            withSignedClient { it.user.ignoreUser(username) }
            ngUsers.value = ngUsers.value.plus(username)
        }
    }

    /**
     * NGユーザーを解除する
     *
     * @throws HatenaException 通信失敗
     */
    override suspend fun removeNgUser(username: String) {
        ngUsersMutex.withLock {
            withSignedClient { it.user.unIgnoreUser(username) }
            ngUsers.value = ngUsers.value.minus(username)
        }
    }

    // ------ //

    init {
        dataStore.data
            .onEach {
                onUpdateRK(it.hatenaRK?.rk.orEmpty())
            }
            .flowOn(Dispatchers.IO)
            .launchIn(application.coroutineScope)
    }
}

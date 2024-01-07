package com.suihan74.satena2.scene.preferences.page.accounts

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import com.suihan74.hatena.model.account.Account
import com.suihan74.satena2.R
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.mastodon.TootVisibility
import com.suihan74.satena2.model.misskey.NoteVisibility
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAuthenticationActivity
import com.suihan74.satena2.scene.preferences.page.accounts.mastodon.MastodonAccount
import com.suihan74.satena2.scene.preferences.page.accounts.mastodon.MastodonAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.mastodon.MastodonAuthenticationActivity
import com.suihan74.satena2.scene.preferences.page.accounts.misskey.MisskeyAccount
import com.suihan74.satena2.scene.preferences.page.accounts.misskey.MisskeyAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.misskey.MisskeyAuthenticationActivity
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AccountViewModel : IPreferencePageViewModel {
    /**
     * Hatena: サインイン状態
     */
    val signedInHatena : StateFlow<Boolean>
    /**
     * Hatena: アカウント情報
     */
    val hatenaAccount : StateFlow<Account?>

    // ------ //

    /**
     * Mastodon: サインイン状態
     */
    val signedInMastodon : StateFlow<Boolean>

    /**
     * Mastodon: アカウント情報
     */
    val mastodonAccount : StateFlow<MastodonAccount?>

    /**
     * Mastodon: 接続先インスタンス
     */
    val mastodonInstance : StateFlow<String>

    /**
     * Mastodon: トゥートの公開範囲
     */
    val mastodonPostVisibility : StateFlow<TootVisibility>

    // ------ //

    /**
     * Misskey: サインイン状態
     */
    val signedInMisskey : Flow<Boolean>

    /**
     * Misskey: アカウント情報
     */
    val misskeyAccount : StateFlow<MisskeyAccount?>

    /**
     * Misskey: 接続先インスタンス
     */
    val misskeyInstance : StateFlow<String>

    /**
     * Misskey: ノートの公開範囲
     */
    val misskeyPostVisibility : StateFlow<NoteVisibility>

    // ------ //

    /**
     * 全アカウントをリロード
     */
    fun reload()

    // ------ //

    /**
     * HatenaにサインインするためのActivityに遷移
     */
    fun launchHatenaAuthorizationActivity(context: Context)

    /**
     * Hatenaからサインアウト
     */
    fun signOutHatena()

    // ------ //

    /**
     * Mastodonと連携するためのActivityに遷移
     */
    fun launchMastodonAuthorizationActivity(context: Context)

    /**
     * Mastodonアカウントの連携解除
     */
    fun signOutMastodon()

    /**
     * Mastodon: トゥートの公開範囲の設定を更新する
     */
    fun updateMastodonPostVisibility(value: TootVisibility)

    // ------ //

    /**
     * Misskeyと連携するためのActivityに遷移
     */
    fun launchMisskeyAuthorizationActivity(context: Context)

    /**
     * Misskeyアカウントの連携解除
     */
    fun signOutMisskey()

    /**
     * Misskey: ノートの公開範囲の設定を更新する
     */
    fun updateMisskeyPostVisibility(value: NoteVisibility)
}

// ------ //

@HiltViewModel
class AccountViewModelImpl @Inject constructor(
    private val hatena: HatenaAccountRepository,
    private val mastodon: MastodonAccountRepository,
    private val misskey: MisskeyAccountRepository,
    dataStore: DataStore<Preferences>
) : AccountViewModel, PreferencePageViewModel(dataStore) {
    /**
     * Hatena: サインイン状態
     */
    override val signedInHatena = hatena.signedIn
    /**
     * Hatena: アカウント情報
     */
    override val hatenaAccount = hatena.account

    // ------ //

    /**
     * Mastodon: サインイン状態
     */
    override val signedInMastodon = mastodon.signedIn

    /**
     * Mastodon: アカウント情報
     */
    override val mastodonAccount = mastodon.account

    /**
     * Mastodon: 接続先インスタンス
     */
    override val mastodonInstance = mastodon.instance

    /**
     * Mastodon: トゥートの公開範囲
     */
    override val mastodonPostVisibility = mastodon.postVisibility

    // ------ //

    /**
     * Misskey: サインイン状態
     */
    override val signedInMisskey = misskey.account.map { it != null }

    /**
     * Misskey: アカウント情報
     */
    override val misskeyAccount = misskey.account

    /**
     * Misskey: 接続先インスタンス
     */
    override val misskeyInstance = misskey.instance

    /**
     * Misskey: ノートの公開範囲
     */
    override val misskeyPostVisibility = misskey.postVisibility

    // ------ //

    /**
     * 全アカウントをリロード
     */
    override fun reload() {
        viewModelScope.launch {
            runCatching {
                val tasks = listOf(
                    async { hatena.reload() },
                    async { mastodon.reload() },
                    async { misskey.reload() }
                )
                tasks.awaitAll()
            }.onSuccess {
                context.showToast(R.string.pref_account_msg_reload_succeeded)
            }.onFailure {
                context.showToast(R.string.pref_account_msg_reload_failure)
            }
        }
    }

    // ------ //

    /**
     * HatenaにサインインするためのActivityに遷移
     */
    override fun launchHatenaAuthorizationActivity(context: Context) {
        val intent = Intent(context, HatenaAuthenticationActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Hatenaからサインアウト
     */
    override fun signOutHatena() {
        viewModelScope.launch {
            hatena.saveRk("")
            context.showToast(R.string.pref_account_hatena_msg_sign_out)
        }
    }

    // ------ //

    /**
     * Mastodonと連携するためのActivityに遷移
     */
    override fun launchMastodonAuthorizationActivity(context: Context) {
        val intent = Intent(context, MastodonAuthenticationActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Mastodonアカウントの連携解除
     */
    override fun signOutMastodon() {
        viewModelScope.launch {
            mastodon.signOut()
            context.showToast(R.string.pref_account_mastodon_msg_sign_out_mastodon)
        }
    }

    /**
     * Mastodon: トゥートの公開範囲の設定を更新する
     */
    override fun updateMastodonPostVisibility(value: TootVisibility) {
        viewModelScope.launch {
            mastodon.updatePostVisibility(value)
        }
    }

    // ------ //

    /**
     * Misskeyと連携するためのActivityに遷移
     */
    override fun launchMisskeyAuthorizationActivity(context: Context) {
        val intent = Intent(context, MisskeyAuthenticationActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Misskeyアカウントの連携解除
     */
    override fun signOutMisskey() {
        viewModelScope.launch {
            misskey.signOut()
            context.showToast(R.string.pref_account_misskey_msg_sign_out)
        }
    }

    /**
     * Misskey: ノートの公開範囲の設定を更新する
     */
    override fun updateMisskeyPostVisibility(value: NoteVisibility) {
        viewModelScope.launch {
            misskey.updatePostVisibility(value)
        }
    }
}

// ------ //

/**
 * Preview用のダミーViewModel
 */
class FakeAccountViewModel(
    signedInHatena: Boolean = true,
    signedInMastodon: Boolean = false,
    signedInMisskey: Boolean = false
) :
    AccountViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val signedInHatena = MutableStateFlow(signedInHatena)
    override val hatenaAccount = MutableStateFlow(null)

    // ------ //

    override val signedInMastodon = MutableStateFlow(signedInMastodon)
    override val mastodonAccount = MutableStateFlow<MastodonAccount?>(null)
    override val mastodonInstance = MutableStateFlow("test.com")
    override val mastodonPostVisibility = MutableStateFlow(TootVisibility.Public)

    // ------ //

    override val signedInMisskey = MutableStateFlow(signedInMisskey)
    override val misskeyAccount = MutableStateFlow<MisskeyAccount?>(null)
    override val misskeyInstance = MutableStateFlow("test.com")
    override val misskeyPostVisibility = MutableStateFlow(NoteVisibility.Public)

    // ------ //

    override fun reload() {
    }

    // ------ //

    override fun launchHatenaAuthorizationActivity(context: Context) {
        signedInHatena.value = true
    }

    override fun signOutHatena() {
        signedInHatena.value = false
    }

    // ------- //

    override fun launchMastodonAuthorizationActivity(context: Context) {
        signedInMastodon.value = true
    }

    override fun signOutMastodon() {
        signedInMastodon.value = false
    }

    override fun updateMastodonPostVisibility(value: TootVisibility) {
        mastodonPostVisibility.value = value
    }

    // ------ //

    override fun launchMisskeyAuthorizationActivity(context: Context) {
    }

    override fun signOutMisskey() {
    }

    override fun updateMisskeyPostVisibility(value: NoteVisibility) {
    }
}

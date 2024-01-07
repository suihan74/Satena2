package com.suihan74.satena2.scene.preferences.page.accounts.misskey

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.viewModelScope
import com.suihan74.misskey.api.auth.AppCredential
import com.suihan74.misskey.api.auth.GenerateSessionResponse
import com.suihan74.satena2.R
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MisskeyAuthenticationViewModel {
    /**
     * 完了したか否か
     */
    val completedFlow : SharedFlow<Boolean>

    /**
     * 処理中か否か
     */
    val processingFlow: StateFlow<Boolean>

    // ------ //

    /**
     * 連携を開始する
     */
    fun startAuthorization(context: Context, instance: String)

    /**
     * 認証画面から復帰時にcredentialを取得
     */
    fun onResume(intent: Intent)
}

// ------ //

@HiltViewModel
class MisskeyAuthenticationViewModelImpl @Inject constructor(
    private val repository: MisskeyAccountRepository
) : MisskeyAuthenticationViewModel, ViewModel() {

    /**
     * 完了したか否か
     */
    override val completedFlow = MutableSharedFlow<Boolean>()

    /**
     * 処理中か否か
     */
    override val processingFlow = MutableStateFlow(false)

    private var appCredential: AppCredential? = null
    private var session: GenerateSessionResponse? = null

    override fun startAuthorization(context: Context, instance: String) {
        processingFlow.value = true

        viewModelScope.launch {
            val (appCredential, session) = repository.authSession(instance)
            this@MisskeyAuthenticationViewModelImpl.appCredential = appCredential
            this@MisskeyAuthenticationViewModelImpl.session = session

            withContext(Dispatchers.Main) {
                val intent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setUrlBarHidingEnabled(true)
                    .build()
                intent.launchUrl(context, Uri.parse(session.url))
            }
        }
    }

    override fun onResume(intent: Intent) {
        val uri = intent.data
        if (Intent.ACTION_VIEW != intent.action || uri?.scheme != "satena-misskey") {
            processingFlow.value = false
            return
        }

        viewModelScope.launch {
            runCatching {
                val instance = uri.host!!
                repository.continueAuthorization(context, instance, appCredential!!, session!!)
            }.onSuccess {
                Log.i("misskey", "authorization has been completed")
                appCredential = null
                session = null
                context.showToast(R.string.pref_account_misskey_msg_auth_succeeded)
                completedFlow.emit(true)
            }.onFailure {
                Log.e("misskey", "authorization failure")
                Log.e("misskey", it.stackTraceToString())
                context.showToast(R.string.pref_account_misskey_msg_auth_failure)
            }
        }
    }
}

// ------ //

class FakeMisskeyAuthenticationViewModel : MisskeyAuthenticationViewModel {
    override val completedFlow = MutableSharedFlow<Boolean>()

    override val processingFlow = MutableStateFlow(false)

    override fun startAuthorization(context: Context, instance: String) {
        processingFlow.value = true
    }

    override fun onResume(intent: Intent) {
        processingFlow.value = false
    }
}

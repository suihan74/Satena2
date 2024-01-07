package com.suihan74.satena2.scene.preferences.page.accounts.mastodon

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IMastodonAuthenticationViewModel {
    val completed : SharedFlow<Boolean>

    // ------ //

    suspend fun startAuthorization(context: Context, instance: String)

    fun onResume(intent: Intent)
}

// ------ //

@HiltViewModel
class MastodonAuthenticationViewModel @Inject constructor(
    private val repository: MastodonAccountRepository
) : IMastodonAuthenticationViewModel, ViewModel() {
    override val completed = MutableSharedFlow<Boolean>()

    override suspend fun startAuthorization(context: Context, instance: String) {
        val oauthUrl = repository.oAuthUrl(context, instance)
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()
        intent.launchUrl(context, Uri.parse(oauthUrl))
    }

    override fun onResume(intent: Intent) {
        if (Intent.ACTION_VIEW != intent.action) return

        val uri = intent.data
        if (uri?.scheme != "satena-mastodon") return

        viewModelScope.launch {
            runCatching {
                val instance = uri.host!!
                val authCode = uri.getQueryParameter("code") ?: throw Exception("invalid code")
                repository.continueAuthorization(context, instance, authCode)
            }.onSuccess {
                Log.i("mastodon", "authorization has been completed")
                context.showToast("mastodon連携成功")
                completed.emit(true)
            }.onFailure {
                Log.e("mastodon", "authorization failure")
                Log.e("mastodon", it.stackTraceToString())
                context.showToast("mastodon連携失敗")
            }
        }
    }
}

// ------ //

class FakeMastodonAuthenticationViewModel : IMastodonAuthenticationViewModel {
    override val completed = MutableSharedFlow<Boolean>()

    override suspend fun startAuthorization(context: Context, instance: String) {
    }

    override fun onResume(intent: Intent) {
    }
}

package com.suihan74.satena2.scene.preferences.page.accounts.hatena

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.lifecycle.viewModelScope
import com.google.accompanist.web.AccompanistWebViewClient
import com.suihan74.satena2.R
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import javax.inject.Inject

interface HatenaAuthenticationViewModel {
    val signInPageUrl get() = "https://www.hatena.ne.jp/login"

    // ------ //

    /** 読み込み中状態 */
    val loading : StateFlow<Boolean>

    val webViewClient : AccompanistWebViewClient

    /** WebViewの設定 */
    fun onCreated(webView: WebView)

    /** 読み込み失敗時の挙動 */
    fun onError()

    /** サインイン完了時の挙動 */
    fun onFinish()
}

// ------ //

@HiltViewModel
class HatenaAuthenticationViewModelImpl @Inject constructor(
    private val repository: HatenaAccountRepository
) :
    HatenaAuthenticationViewModel,
    ViewModel()
{
    override val loading : StateFlow<Boolean> = MutableStateFlow(false)
    private val _loading = loading as MutableStateFlow

    private val cookieManager = CookieManager.getInstance()
    private var finished = MutableStateFlow(false)

    private val rk: String?
        get() {
            val cookies = cookieManager.getCookie("https://www.hatena.ne.jp")
            if (cookies.isNullOrBlank()) return null
            val regex = Regex("""rk=(.+);""")
            val matches = regex.find(cookies)
            return matches?.groupValues?.getOrNull(1)
        }

    // ------ //

    private var onErrorListener : (()->Unit)? = null

    private var onFinishListener : (()->Unit)? = null

    fun setOnErrorListener(listener: (() -> Unit)?) {
        onErrorListener = listener
    }

    fun setOnFinishListener(listener: (()->Unit)?) {
        onFinishListener = listener
    }

    // ------ //

    override val webViewClient = AuthWebViewClient()

    /** WebViewの設定 */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreated(webView: WebView) {
        cookieManager.let {
            it.acceptCookie()
            it.removeAllCookies {}
            it.setAcceptThirdPartyCookies(webView, true)
        }
        webView.settings.let {
            it.javaScriptEnabled = true
            it.loadWithOverviewMode = true
            it.useWideViewPort = true
        }
        webView.setInitialScale(1)
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }

    /** 読み込み失敗時の挙動 */
    override fun onError() {
        onErrorListener?.invoke()
    }

    /** サインイン完了時の挙動 */
    override fun onFinish() {
        viewModelScope.launch {
            runCatching {
                repository.saveRk(rk!!)
                repository.account
                    .onEach { account ->
                        account?.let {
                            context.showToast(
                                context.getString(R.string.pref_account_hatena_msg_sign_in_succeeded, it.name)
                            )
                            onFinishListener?.invoke()
                        }
                    }
                    .launchIn(viewModelScope)
            }.onFailure { onError() }
        }
    }

    // ------ //

    inner class AuthWebViewClient : AccompanistWebViewClient() {
        private val emptyResourceRequest: WebResourceResponse =
            WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            error?.let {
                Log.e("auth", "code:${it.errorCode}, description:${it.description}")
            }
            // TODO: 以下の条件なしだとプロキシ利用などでリソース読み込み拒否した場合でも終了してしまうので、あとでなんとかする
            if (request?.url?.toString() == signInPageUrl) {
                onError()
            }
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            synchronized(this) {
                if (finished.value) return emptyResourceRequest
                if (request == null) return emptyResourceRequest
                if (request.url.toString() == signInPageUrl && request.method == "POST") {
                    _loading.value = true
                }
                if (!finished.value && !rk.isNullOrBlank()) {
                    finished.value = true
                    onFinish()
                    return emptyResourceRequest
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            if (url == signInPageUrl) {
                _loading.value = false
            }
        }
    }
}

// ------ //

class FakeHatenaAuthenticationViewModel(
    private val coroutineScope: CoroutineScope,
    initialLoading: Boolean = true
) : HatenaAuthenticationViewModel {
    override val loading: StateFlow<Boolean> = MutableStateFlow(initialLoading)
    private val _loading = loading as MutableStateFlow

    override val webViewClient = AccompanistWebViewClient()

    override fun onCreated(webView: WebView) {
        coroutineScope.launch {
            delay(3_000)
            _loading.value = false
        }
    }
    override fun onError() {}
    override fun onFinish() {}
}

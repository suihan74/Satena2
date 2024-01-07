package com.suihan74.satena2.scene.browser

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebBackForwardList
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.google.accompanist.web.AccompanistWebViewClient
import com.suihan74.satena2.utility.extension.trimScheme
import com.suihan74.satena2.utility.extension.withSafety
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

class WebViewClient(
    private val urlFlow : MutableSharedFlow<String>,
    private val refreshState : MutableStateFlow<Boolean>,
    private val resourceUrls: MutableStateFlow<ArrayList<ResourceUrl>>,
    private val backForwardList: MutableStateFlow<WebBackForwardList?>,
    private val blockTargets: StateFlow<List<String>>,
    private val coroutineScope: CoroutineScope
) : AccompanistWebViewClient() {
    private val emptyResourceRequest: WebResourceResponse =
        WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view.copyBackForwardList().let {
            backForwardList.value = it
        }
        coroutineScope.launch {
            urlFlow.emit(url.orEmpty())
        }
        synchronized(resourceUrls) {
            resourceUrls.value = ArrayList()
        }
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        refreshState.value = false
    }

    // ------ //

    /**
     * URLブロック対象のリソースを読み込まないようにする
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        val urlWithoutScheme = url.trimScheme()
        val blocked = blockTargets.value.any { urlWithoutScheme.startsWith(it) }

        // ブロックされたリソースを記録
        // 並行して呼び出される可能性があるので雑にロックする
        synchronized(resourceUrls) {
            resourceUrls.value = resourceUrls.value.also { list ->
                list.add(ResourceUrl(url = url, blocked = blocked))
            }
        }

        return if (blocked) emptyResourceRequest
        else super.shouldInterceptRequest(view, request)
    }

    // ------ //

    /**
     * ページに遷移するか否かを決定する
     */
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return false
        return when (val scheme = uri.scheme) {
            "https", "http" ->
                super.shouldOverrideUrlLoading(view, request)

            "intent", "android-app" -> {
                handleIntentScheme(view, scheme, uri)
                true
            }

            else -> {
                handleOtherSchemes(view, uri)
                true
            }
        }
    }

    /**
     * intentスキームのURIを処理する
     */
    private fun handleIntentScheme(view: WebView?, scheme: String, uri: Uri) {
        runCatching {
            val activity = view?.context as Activity
            val intentScheme =
                if (scheme == "intent") Intent.URI_INTENT_SCHEME
                else Intent.URI_ANDROID_APP_SCHEME
            val intent = Intent.parseUri(uri.toString(), intentScheme).withSafety()
            activity.startActivity(intent)
        }.onFailure { e ->
            Log.e("WebViewClient", Log.getStackTraceString(e))
        }
    }

    /**
     * http,httpsでもなく、intentでもないアドレスを処理する
     */
    private fun handleOtherSchemes(view: WebView?, uri: Uri) {
        runCatching {
            val activity = view?.context as Activity
            val intent = Intent(Intent.ACTION_DEFAULT, uri).withSafety()
            activity.startActivity(intent)
        }.onFailure { e ->
            Log.e("WebViewClient", Log.getStackTraceString(e))
        }
    }
}

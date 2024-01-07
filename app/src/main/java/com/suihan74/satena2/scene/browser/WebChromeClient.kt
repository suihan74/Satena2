package com.suihan74.satena2.scene.browser

import android.graphics.Bitmap
import android.webkit.WebBackForwardList
import android.webkit.WebView
import com.google.accompanist.web.AccompanistWebChromeClient
import com.suihan74.satena2.utility.bound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WebChromeClient(
    private val backForwardList: MutableStateFlow<WebBackForwardList?>,
    private val coroutineScope: CoroutineScope
) : AccompanistWebChromeClient() {
    val progress = MutableStateFlow(0f)

    /**
     * URLとfaviconURLのペア
     */
    val faviconFlow: Flow<Pair<String, Bitmap>> = MutableSharedFlow()
    private val _faviconFlow = faviconFlow as MutableSharedFlow<Pair<String, Bitmap>>

    /**
     * URLとページタイトルのペア
     */
    val titleFlow: Flow<Pair<String, String>> = MutableSharedFlow()
    private val _titleFlow = titleFlow as MutableSharedFlow<Pair<String, String>>

    // ------ //

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progress.value = bound(min = 0f, max = 1f, value = newProgress / 100f)
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        val url = view.url
        if (url != null && icon != null) {
            coroutineScope.launch {
                _faviconFlow.emit(url to icon)
            }
        }
        view.copyBackForwardList()?.let {
            backForwardList.value = it
        }
    }

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)
        val url = view.url
        if (url != null && title != null) {
            coroutineScope.launch {
                _titleFlow.emit(url to title)
            }
        }
        view.copyBackForwardList().let {
            backForwardList.value = it
        }
    }
}

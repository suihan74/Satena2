package com.suihan74.satena2.scene.browser

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebBackForwardList
import android.webkit.WebHistoryItem
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.compose.ui.Alignment
import androidx.datastore.core.DataStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.suihan74.satena2.model.browser.History
import com.suihan74.satena2.model.browser.WebViewTheme
import com.suihan74.satena2.model.dataStore.BrowserPreferences
import com.suihan74.satena2.model.theme.ThemePreset
import com.suihan74.satena2.scene.bookmarks.BookmarksActivityContract
import com.suihan74.satena2.scene.browser.BrowserViewModel.Companion.SYSTEM_URL_ABOUT_BLANK
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.createIntentWithoutThisApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

interface BrowserViewModel {
    /**
     * テーマ
     */
    val theme : StateFlow<ThemePreset>

    /**
     * 表示中ページURL
     */
    val currentUrl : SharedFlow<String>

    /**
     * 戻る/進むリスト
     */
    val backForwardList : StateFlow<WebBackForwardList?>

    /**
     * 履歴
     */
    val histories : StateFlow<List<History>>

    /**
     * 読み込み拒否されたリソース
     */
    val blockTargets : StateFlow<List<String>>

    /**
     * 表示中ページのリソースリスト
     */
    val resourceUrls : StateFlow<List<ResourceUrl>>

    // ------ //

    val webViewClient : WebViewClient

    val webChromeClient : WebChromeClient

    val swipeRefreshingFlow : StateFlow<Boolean>

    /**
     * 戻るボタンが長押しされた
     */
    val backForwardLongPressed : MutableSharedFlow<Boolean>

    // ------ //

    /**
     * ドロワの配置
     */
    val drawerAlignment : Flow<Alignment.Horizontal>

    /**
     * アドレスバーの配置
     */
    val addressBarAlignment : Flow<Alignment.Vertical>

    /**
     * リソースブロックの有効状態
     */
    val urlBlockingEnabled : Flow<Boolean>

    /**
     * JavaScriptの有効状態
     */
    val javascriptEnabled : Flow<Boolean>

    // ------ //

    /**
     * 最初に表示するURLを取得する
     */
    fun initialUrl(intent: Intent) : String

    /**
     * WebView作成完了時に設定を行う
     */
    fun onCreated(webView: WebView, coroutineScope: CoroutineScope)

    /**
     * 終了時処理
     */
    fun onDispose()

    // ------ //

    /**
     * 指定URLに遷移する
     */
    fun loadUrl(url: String)

    /**
     * 与えられた文字列をウェブ検索する
     */
    fun search(text: String)

    /**
     * 検索または指定URLに遷移
     */
    fun enterAddressBarText(text: String)

    /**
     * ページを再読み込み
     */
    fun refresh()

    /**
     * 戻る/進む
     */
    fun goBackOrForward(item: WebHistoryItem)

    /**
     * 戻る/進む
     *
     * @throws IndexOutOfBoundsException 指定ステップが戻る/進むリストの範囲外
     */

    fun goBackOrForward(steps: Int)

    /**
     * 表示中ページのweb魚拓を開く
     */
    fun openPageArchive()

    /**
     * URLブロックの有効状態を切り替える
     */
    suspend fun toggleUrlBlockingEnabled()

    /**
     * JavaScriptの有効状態を切り替える
     */
    suspend fun toggleJavaScriptEnabled()

    // ------ //

    /**
     * 現在表示中ページに対するブクマ画面を開く
     */
    fun launchBookmarkActivity()

    /**
     * 外部アプリで開く
     */
    fun openWithOtherApp()

    // ------ //

    /**
     * faviconキャッシュのフルパス
     */
    val faviconPath: String

    /**
     * 履歴の続きを取得する
     */
    fun additionalLoadHistories()

    // ------ //

    /**
     * 読み込み拒否するリソースを追加する
     */
    fun insertBlockedResource(url: String)

    // ------ //

    companion object {
        /** ブランクページのURL */
        const val SYSTEM_URL_ABOUT_BLANK = "about:blank"
    }
}

// ------ //

@HiltViewModel
class BrowserViewModelImpl @Inject constructor(
    prefsRepo: PreferencesRepository,
    private val browserDataStore: DataStore<BrowserPreferences>,
    private val repo: HistoryRepository
) : ViewModel(), BrowserViewModel {
    /**
     * テーマ
     */
    override val theme = prefsRepo.theme

    override val currentUrl = MutableStateFlow("")

    override val backForwardList = MutableStateFlow<WebBackForwardList?>(null)

    override val resourceUrls = MutableStateFlow(ArrayList<ResourceUrl>())

    override val swipeRefreshingFlow = MutableStateFlow(false)

    override val histories = repo.histories

    override val blockTargets = MutableStateFlow(emptyList<String>())

    // ------ //

    override val webViewClient = WebViewClient(
        urlFlow = currentUrl,
        refreshState = swipeRefreshingFlow,
        resourceUrls = resourceUrls,
        backForwardList = backForwardList,
        blockTargets = blockTargets,
        coroutineScope = viewModelScope
    )

    override val webChromeClient = WebChromeClient(backForwardList, viewModelScope)

    // ------ //

    /**
     * 戻るボタンが長押しされた
     */
    override val backForwardLongPressed = MutableSharedFlow<Boolean>()

    /**
     * ページ遷移トリガ用[Flow]
     */
    private val currentUrlOrigin = MutableStateFlow("")

    /**
     * ページ再読み込みトリガ用[Flow]
     */
    private val reloadTrigger = MutableSharedFlow<Boolean>()

    private val historyMoveSteps = MutableSharedFlow<Int>()

    override val urlBlockingEnabled = browserDataStore.data.map { it.urlBlockingEnabled }

    override val javascriptEnabled = browserDataStore.data.map { it.javascriptEnabled }

    // ------ //

    /**
     * ドロワの配置
     */
    override val drawerAlignment = prefsRepo.dataStore.data.map { it.drawerAlignment }

    /**
     * アドレスバーの配置
     */
    override val addressBarAlignment = browserDataStore.data.map { it.addressBarAlignment }

    // ------ //

    /**
     * ウェブサイトのテーマ
     */
    private val webViewTheme = browserDataStore.data.map { it.webViewTheme }

    private val isForceDarkStrategySupported : Boolean
        get() = WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)

    private val isForceDarkSupported : Boolean
        get() = WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)

    // ------ //

    init {
        combine(webChromeClient.titleFlow, webChromeClient.faviconFlow) { (tUrl, title), (fUrl, bitmap) ->
            if (fUrl != tUrl) return@combine
            repo.saveFavicon(context, fUrl, bitmap)
            repo.insertOrUpdateHistory(context, tUrl, title)
        }.launchIn(viewModelScope)

        browserDataStore.data
            .onEach { blockTargets.value = it.blockedResources }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            repo.loadHistories()
        }
    }

    // ------ //

    fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
        lifecycleObserver = LifecycleObserver(activityResultRegistry!!)
        lifecycle?.addObserver(lifecycleObserver)
    }

    // ------ //

    /**
     * 最初に表示するURLを取得する
     */
    override fun initialUrl(intent: Intent) : String {
        return intent.getStringExtra(BrowserActivityContract.EXTRA_URL)
            ?: runBlocking { browserDataStore.data.map { it.startPageUrl }.first() }
    }

    /**
     * WebView作成完了時に設定を行う
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreated(webView: WebView, coroutineScope: CoroutineScope) {
        webView.clearHistory()

        webView.settings.apply {
            // DOMストレージ有効
            domStorageEnabled = true
            // ページサイズの調整
            useWideViewPort = true
            loadWithOverviewMode = true
            // ズーム
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false // ズームボタン非表示
            webView.setInitialScale(100)
            // todo
            userAgentString = null
//            userAgentString =  "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0"
        }
        // サードパーティCookie有効
        CookieManager.getInstance().acceptThirdPartyCookies(webView)

        // セキュリティ保護を利用可能な全てのバージョンでデフォルトで保護を行う
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(webView.settings, true)
        }

        // コンテンツのダウンロード処理に割り込む
        // PDFを開こうとした場合、処理を外部アプリに投げる
        webView.setDownloadListener { url, _/*userAgent*/, _/*contentDisposition*/, mimeType, _/*size*/ ->
            val context = webView.context
            if (mimeType == "application/pdf") {
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW).also {
                        it.setDataAndType(Uri.parse(url), mimeType)
                    }
                    context.startActivity(intent)
                }.onFailure {
                    // 開けるアプリが無かったらストアを開く
                    val storeIntent = Intent(Intent.ACTION_VIEW).also {
                        it.data = Uri.parse("market://search?q=pdf")
                    }
                    runCatching {
                        context.startActivity(storeIntent)
                    }
                }
            }
        }

        // テーマの設定
        webViewTheme
            .onEach {
                val theme = when (it) {
                    WebViewTheme.AUTO -> it
                    else -> it
                }
                setWebViewTheme(webView, theme)
            }
            .launchIn(coroutineScope)

        // JSのON/OFF
        var javascriptInitialized = false
        javascriptEnabled
            .onEach {
                webView.settings.javaScriptEnabled = it
                if (javascriptInitialized) {
                    webView.reload()
                }
                else {
                    javascriptInitialized = true
                }
            }
            .launchIn(coroutineScope)

        // ページ遷移をWebViewまで伝搬
        currentUrlOrigin
            .onEach { webView.loadUrl(it) }
            .launchIn(coroutineScope)

        // ページ再読み込み
        reloadTrigger
            .onEach { webView.reload() }
            .launchIn(coroutineScope)

        // 戻る/進むヒストリの移動
        historyMoveSteps
            .onEach { steps ->
                if (webView.canGoBackOrForward(steps)) {
                    webView.goBackOrForward(steps)
                }
            }
            .launchIn(coroutineScope)
    }

    private fun setWebViewTheme(webView: WebView, theme: WebViewTheme) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)
        ) {
            setWebViewThemeTiramisuAndAbove(webView, theme)
        }
        else {
            setWebViewThemeUnderTiramisu(webView, theme)
        }
    }

    @SuppressLint("RequiresFeature")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setWebViewThemeTiramisuAndAbove(webView: WebView, theme: WebViewTheme) {
        if (
            theme == WebViewTheme.DARK
            && isForceDarkStrategySupported
            && isForceDarkSupported
        ) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
        }
        else if (
            theme == WebViewTheme.FORCE_DARK
            && isForceDarkSupported
        ) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
        }
        else if (isForceDarkSupported) {
            // todo
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, false)
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("RequiresFeature")
    private fun setWebViewThemeUnderTiramisu(webView: WebView, theme: WebViewTheme) {
        if (
            theme == WebViewTheme.DARK
            && isForceDarkStrategySupported
            && isForceDarkSupported
        ) {
            WebSettingsCompat.setForceDark(
                webView.settings,
                WebSettingsCompat.FORCE_DARK_ON
            )
            WebSettingsCompat.setForceDarkStrategy(
                webView.settings,
                WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY
            )
        }
        else if (
            theme == WebViewTheme.FORCE_DARK
            && isForceDarkSupported
        ) {
            WebSettingsCompat.setForceDark(
                webView.settings,
                WebSettingsCompat.FORCE_DARK_ON
            )
            if (isForceDarkStrategySupported) {
                WebSettingsCompat.setForceDarkStrategy(
                    webView.settings,
                    WebSettingsCompat.DARK_STRATEGY_USER_AGENT_DARKENING_ONLY
                )
            }
        }
        else if (isForceDarkSupported) {
            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_OFF)
        }
    }

    /**
     * 終了時処理
     */
    override fun onDispose() {
        loadUrl(SYSTEM_URL_ABOUT_BLANK)
    }

    // ------ //

    /**
     * 指定URLに遷移する
     */
    override fun loadUrl(url: String) {
        require(URLUtil.isNetworkUrl(url) || url == SYSTEM_URL_ABOUT_BLANK)
        if (url == currentUrlOrigin.value) {
            refresh()
        }
        else {
            currentUrlOrigin.value = url
        }
    }

    /**
     * 与えられた文字列をウェブ検索する
     */
    override fun search(text: String) {
        // todo
        loadUrl("https://www.google.com/search?q=${Uri.encode(text)}")
    }

    /**
     * 検索または指定URLに遷移
     */
    override fun enterAddressBarText(text: String) {
        if (URLUtil.isNetworkUrl(text)) {
            loadUrl(text)
        }
        else {
            search(text)
        }
    }

    /**
     * ページを再読み込み
     */
    override fun refresh() {
        swipeRefreshingFlow.value = true
        viewModelScope.launch {
            currentUrl.emit(currentUrlOrigin.value)
            reloadTrigger.emit(true)
        }
    }

    /**
     * 戻る/進む
     *
     * @throws IllegalArgumentException 指定した履歴が戻る/進むリストにない
     */
    override fun goBackOrForward(item: WebHistoryItem) {
        val items = backForwardList.value ?: return
        val targetIdx =
            (0 until items.size).firstOrNull { items.getItemAtIndex(it) == item } ?: throw IllegalArgumentException()
        val currentIdx = items.currentIndex
        val steps = targetIdx - currentIdx
        viewModelScope.launch {
            historyMoveSteps.emit(steps)
        }
    }

    /**
     * 戻る/進む
     *
     * @throws IndexOutOfBoundsException 指定ステップが戻る/進むリストの範囲外
     */
    override fun goBackOrForward(steps: Int) {
        val items = backForwardList.value ?: return
        val currentIdx = items.currentIndex
        val toIdx = currentIdx + steps
        if (toIdx < 0 || toIdx >= items.size) throw IndexOutOfBoundsException()
        viewModelScope.launch {
            historyMoveSteps.emit(steps)
        }
    }

    /**
     * 表示中ページのweb魚拓を開く
     */
    override fun openPageArchive() {
        loadUrl("https://megalodon.jp/?url=${Uri.encode(currentUrl.value)}")
    }

    /**
     * URLブロックの有効状態を切り替える
     */
    override suspend fun toggleUrlBlockingEnabled() {
        browserDataStore.updateData { prefs ->
            prefs.copy(urlBlockingEnabled = !prefs.urlBlockingEnabled)
        }
    }

    /**
     * JavaScriptの有効状態を切り替える
     */
    override suspend fun toggleJavaScriptEnabled() {
        browserDataStore.updateData { prefs ->
            prefs.copy(javascriptEnabled = !prefs.javascriptEnabled)
        }
    }

    // ------ //

    /**
     * 現在表示中ページに対するブクマ画面を開く
     */
    override fun launchBookmarkActivity() {
        lifecycleObserver.launchBookmarksActivity(currentUrl.value)
    }

    /**
     * 外部アプリで開く
     */
    override fun openWithOtherApp() {
        lifecycleObserver.openWithOtherApp(currentUrl.value)
    }

    // ------ //

    /**
     * faviconキャッシュのフルパス
     */
    override val faviconPath: String by lazy { "${context.filesDir.absoluteFile}/${HistoryRepositoryImpl.FAVICON_CACHE_DIR}" }

    /**
     * 履歴の続きを取得する
     */
    override fun additionalLoadHistories() {
        viewModelScope.launch {
            repo.additionalLoadHistories()
        }
    }

    // ------ //

    /**
     * 読み込み拒否するリソースを追加する
     */
    override fun insertBlockedResource(url: String) {
        viewModelScope.launch {
            if (blockTargets.value.contains(url)) return@launch
            browserDataStore.updateData {
                it.copy(
                    blockedResources = it.blockedResources.plus(url)
                )
            }
        }
    }

    // ------ //

    private lateinit var lifecycleObserver: LifecycleObserver

    inner class LifecycleObserver(
        private val registry : ActivityResultRegistry
    ) : DefaultLifecycleObserver {

        /** [com.suihan74.satena2.scene.bookmarks.BookmarksActivity]のランチャ */
        private lateinit var bookmarksActivityLauncher : ActivityResultLauncher<String>

        /** 外部アプリのランチャ */
        private lateinit var otherAppActivityLauncher : ActivityResultLauncher<String>

        override fun onCreate(owner: LifecycleOwner) {
            bookmarksActivityLauncher = registry.register(
                "BookmarksActivityLauncherWithUrl",
                owner,
                BookmarksActivityContract.WithUrl()
            ) {
                /* do nothing */
            }

            otherAppActivityLauncher = registry.register(
                "otherAppActivityLauncher",
                owner,
                object : ActivityResultContract<String, Unit>() {
                    override fun createIntent(context: Context, input: String) =
                        Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(input)
                        }.createIntentWithoutThisApplication(context)
                    override fun parseResult(resultCode: Int, intent: Intent?) { /* do nothing */ }
                }
            ) { /* do nothing */ }
        }

        fun launchBookmarksActivity(url: String) {
            bookmarksActivityLauncher.launch(url)
        }

        fun openWithOtherApp(url: String) {
            otherAppActivityLauncher.launch(url)
        }
    }
}

// ------ //

class FakeBrowserViewModel(
    private val coroutineScope: CoroutineScope
) : BrowserViewModel {
    override val theme = MutableStateFlow(ThemePreset())

    override val currentUrl = MutableSharedFlow<String>()

    override val backForwardList = MutableStateFlow<WebBackForwardList?>(null)

    override val histories = MutableStateFlow<List<History>>(emptyList())

    override val resourceUrls = MutableStateFlow(ArrayList<ResourceUrl>())

    override val swipeRefreshingFlow = MutableStateFlow(false)

    override val blockTargets = MutableStateFlow(emptyList<String>())

    override val webViewClient = WebViewClient(
        urlFlow = currentUrl,
        refreshState = swipeRefreshingFlow,
        resourceUrls = resourceUrls,
        backForwardList = backForwardList,
        blockTargets = blockTargets,
        coroutineScope = coroutineScope
    )//.also {
//        it.state = WebViewState(WebContent.Url(url = "https://localhost"))
//    }

    override val webChromeClient = WebChromeClient(backForwardList, coroutineScope)

    override val backForwardLongPressed = MutableSharedFlow<Boolean>()

    // ------ //

    override val drawerAlignment = MutableStateFlow(Alignment.Start)

    override val addressBarAlignment = MutableStateFlow(Alignment.Bottom)

    override val urlBlockingEnabled = MutableStateFlow(true)

    override val javascriptEnabled = MutableStateFlow(true)

    // ------ //

    override fun initialUrl(intent: Intent) : String {
        return SYSTEM_URL_ABOUT_BLANK
    }

    override fun onCreated(webView: WebView, coroutineScope: CoroutineScope) {
        webView.webViewClient = webViewClient
        webView.webChromeClient = webChromeClient
    }

    /**
     * 終了時処理
     */
    override fun onDispose() {
        loadUrl(SYSTEM_URL_ABOUT_BLANK)
    }

    // ------ //

    override fun loadUrl(url: String) {
    }

    override fun search(text: String) {
    }

    override fun enterAddressBarText(text: String) {
    }

    override fun refresh() {
    }

    override fun goBackOrForward(item: WebHistoryItem) {
    }

    override fun goBackOrForward(steps: Int) {
    }


    override fun openPageArchive() {
    }

    override suspend fun toggleUrlBlockingEnabled() {
        urlBlockingEnabled.value = !urlBlockingEnabled.value
    }

    override suspend fun toggleJavaScriptEnabled() {
        javascriptEnabled.value = !javascriptEnabled.value
    }

    // ------ //

    override val faviconPath: String = ""

    override fun launchBookmarkActivity() {
    }

    override fun openWithOtherApp() {
    }

    // ------ //

    override fun additionalLoadHistories() {
    }

    // ------ //

    override fun insertBlockedResource(url: String) {
    }
}

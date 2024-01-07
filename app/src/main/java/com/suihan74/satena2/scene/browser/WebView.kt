@file:Suppress("unused")

/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.suihan74.satena2.scene.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.OverScroller
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.suihan74.satena2.scene.browser.LoadingState.Finished
import com.suihan74.satena2.scene.browser.LoadingState.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 */
@Composable
fun NestedScrollableWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() }
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }

    LaunchedEffect(webView, navigator) {
        with(navigator) { webView?.handleNavigationEvents() }
    }

    val currentOnDispose by rememberUpdatedState(onDispose)

    webView?.let { it ->
        DisposableEffect(it) {
            onDispose { currentOnDispose(it) }
        }
    }

    // Set the state of the client and chrome client
    // This is done internally to ensure they always are the same instance as the
    // parent Web composable
    client.state = state
    client.navigator = navigator
    chromeClient.state = state

    val runningInPreview = LocalInspectionMode.current

    AndroidView(
        factory = { context ->
            NestedScrollWebView(context).apply {
                onCreated(this)

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webChromeClient = chromeClient
                webViewClient = client
            }.also { webView = it }
        },
        modifier = modifier
    ) { view ->
        // AndroidViews are not supported by preview, bail early
        if (runningInPreview) return@AndroidView

        when (val content = state.content) {
            is WebContent.Url -> {
                val url = content.url

                if (url.isNotEmpty() && url != view.url) {
                    view.loadUrl(url, content.additionalHttpHeaders.toMutableMap())
                }
            }
            is WebContent.Data -> {
                view.loadDataWithBaseURL(content.baseUrl, content.data, null, "utf-8", null)
            }
        }

        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }
}

/**
 * AccompanistWebViewClient
 *
 * A parent class implementation of WebViewClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overriden if further custom behaviour is required.
 */
open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: WebViewState
        internal set
    open lateinit var navigator: WebViewNavigator
        internal set

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        state.loadingState = Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageTitle = null
        state.pageIcon = null
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = Finished
        navigator.canGoBack = view?.canGoBack() ?: false
        navigator.canGoForward = view?.canGoForward() ?: false
    }

    override fun doUpdateVisitedHistory(
        view: WebView?,
        url: String?,
        isReload: Boolean
    ) {
        super.doUpdateVisitedHistory(view, url, isReload)
        // WebView will often update the current url itself.
        // This happens in situations like redirects and navigating through
        // history. We capture this change and update our state holder url.
        // On older APIs (28 and lower), this method is called when loading
        // html data. We don't want to update the state in this case as that will
        // overwrite the html being loaded.
        if (url != null &&
            !url.startsWith("data:text/html") &&
            state.content.getCurrentUrl() != url
        ) {
            state.content = state.content.withUrl(url)
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (error != null) {
            state.errorsForCurrentRequest.add(WebViewError(request, error))
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        // Override all url loads to make the single source of truth
        // of the URL the state holder Url
        request?.let {
            state.content = state.content.withUrl(it.url.toString())
        }
        return true
    }
}

/**
 * AccompanistWebChromeClient
 *
 * A parent class implementation of WebChromeClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overriden if further custom behaviour is required.
 */
open class AccompanistWebChromeClient : WebChromeClient() {
    open lateinit var state: WebViewState
        internal set

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        state.pageTitle = title
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        state.pageIcon = icon
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is Finished) return
        state.loadingState = Loading(newProgress / 100.0f)
    }
}

sealed class WebContent {
    data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) : WebContent()

    data class Data(val data: String, val baseUrl: String? = null) : WebContent()

    fun getCurrentUrl(): String? {
        return when (this) {
            is Url -> url
            is Data -> baseUrl
        }
    }
}

internal fun WebContent.withUrl(url: String) = when (this) {
    is WebContent.Url -> copy(url = url)
    else -> WebContent.Url(url)
}

/**
 * Sealed class for constraining possible loading states.
 * See [Loading] and [Finished].
 */
sealed class LoadingState {
    /**
     * Describes a WebView that has not yet loaded for the first time.
     */
    object Initializing : LoadingState()

    /**
     * Describes a webview between `onPageStarted` and `onPageFinished` events, contains a
     * [progress] property which is updated by the webview.
     */
    data class Loading(val progress: Float) : LoadingState()

    /**
     * Describes a webview that has finished loading content.
     */
    object Finished : LoadingState()
}

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
@Stable
class WebViewState(webContent: WebContent) {
    /**
     *  The content being loaded by the WebView
     */
    var content: WebContent by mutableStateOf(webContent)

    /**
     * Whether the WebView is currently [LoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the webview is currently loading data in its main frame
     */
    val isLoading: Boolean
        get() = loadingState !is Finished

    /**
     * The title received from the loaded content of the current page
     */
    var pageTitle: String? by mutableStateOf(null)
        internal set

    /**
     * the favicon received from the loaded content of the current page
     */
    var pageIcon: Bitmap? by mutableStateOf(null)
        internal set

    /**
     * A list for errors captured in the last load. Reset when a new page is loaded.
     * Errors could be from any resource (iframe, image, etc.), not just for the main page.
     * For more fine grained control use the OnError callback of the WebView.
     */
    val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()
}

/**
 * Allows control over the navigation of a WebView from outside the composable. E.g. for performing
 * a back navigation in response to the user clicking the "up" button in a TopAppBar.
 *
 * @see [rememberWebViewNavigator]
 */
@Stable
class WebViewNavigator(private val coroutineScope: CoroutineScope) {

    private enum class NavigationEvent { BACK, FORWARD, RELOAD, STOP_LOADING }

    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow()

    // Use Dispatchers.Main to ensure that the webview methods are called on UI thread
    internal suspend fun WebView.handleNavigationEvents(): Nothing = withContext(Dispatchers.Main) {
        navigationEvents.collect { event ->
            when (event) {
                NavigationEvent.BACK -> goBack()
                NavigationEvent.FORWARD -> goForward()
                NavigationEvent.RELOAD -> reload()
                NavigationEvent.STOP_LOADING -> stopLoading()
            }
        }
    }

    /**
     * True when the web view is able to navigate backwards, false otherwise.
     */
    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    /**
     * True when the web view is able to navigate forwards, false otherwise.
     */
    var canGoForward: Boolean by mutableStateOf(false)
        internal set

    /**
     * Navigates the webview back to the previous page.
     */
    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.BACK) }
    }

    /**
     * Navigates the webview forward after going back from a page.
     */
    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.FORWARD) }
    }

    /**
     * Reloads the current page in the webview.
     */
    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.RELOAD) }
    }

    /**
     * Stops the current page load (if one is loading).
     */
    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.STOP_LOADING) }
    }
}

/**
 * Creates and remembers a [WebViewNavigator] using the default [CoroutineScope] or a provided
 * override.
 */
@Composable
fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): WebViewNavigator = remember(coroutineScope) { WebViewNavigator(coroutineScope) }

/**
 * A wrapper class to hold errors from the WebView.
 */
@Immutable
data class WebViewError(
    /**
     * The request the error came from.
     */
    val request: WebResourceRequest?,
    /**
     * The error that was reported.
     */
    val error: WebResourceError
)

/**
 * Creates a WebView state that is remembered across Compositions.
 *
 * @param url The url to load in the WebView
 * @param additionalHttpHeaders Optional, additional HTTP headers that are passed to [WebView.loadUrl].
 *                              Note that these headers are used for all subsequent requests of the WebView.
 */
@Composable
fun rememberWebViewState(url: String, additionalHttpHeaders: Map<String, String> = emptyMap()): WebViewState =
// Rather than using .apply {} here we will recreate the state, this prevents
    // a recomposition loop when the webview updates the url itself.
    remember(url, additionalHttpHeaders) {
        WebViewState(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders
            )
        )
    }

/**
 * Creates a WebView state that is remembered across Compositions.
 *
 * @param data The uri to load in the WebView
 */
@Composable
fun rememberWebViewStateWithHTMLData(data: String, baseUrl: String? = null): WebViewState =
    remember(data, baseUrl) {
        WebViewState(WebContent.Data(data, baseUrl))
    }

// ------ //

class NestedScrollWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.webViewStyle
) : WebView(context, attrs, defStyle), NestedScrollingChild3, Runnable {

    private val scroller: OverScroller = OverScroller(context)
    private val touchSlop: Int
    private val minimumFlingVelocity: Float
    private val maximumFlingVelocity: Float
    private var velocityTracker: VelocityTracker? = null

    private var lastMotionX: Int = 0
    private var lastMotionY: Int = 0
    private val consumed: IntArray = IntArray(2)

    private var downMotionX: Int = 0
    private var downMotionY: Int = 0

    private var lastScrollY: Int = 0

    private var dragOrientation: DragOrientation? = null

    private var isUpEventDispatched: Boolean = false
    private var isCancelEventDispatched: Boolean = false

    private val childHelper = NestedScrollingChildHelper(this)
    private val scrollConsumed: IntArray = IntArray(2)

    enum class DragOrientation {
        HORIZONTAL, VERTICAL
    }

    init {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        minimumFlingVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        maximumFlingVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
        isNestedScrollingEnabled = true
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                }
                lastMotionX = x
                lastMotionY = y

                downMotionX = x
                downMotionY = y

                initOrResetVelocityTracker()

                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
                startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_TOUCH)

                isUpEventDispatched = false
                isCancelEventDispatched = false
                super.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaX = lastMotionX - x
                var deltaY = lastMotionY - y

                when {
                    dragOrientation == null && abs(deltaY) >= touchSlop && abs(deltaY) >= abs(deltaX) -> {
                        dragOrientation = DragOrientation.VERTICAL
                        parent?.requestDisallowInterceptTouchEvent(true)
                        if (deltaY > 0) {
                            deltaY -= touchSlop
                        } else {
                            deltaY += touchSlop
                        }
                    }
                    dragOrientation == null && abs(deltaX) >= touchSlop && abs(deltaX) >= abs(deltaY) -> {
                        dragOrientation = DragOrientation.HORIZONTAL
                        parent?.requestDisallowInterceptTouchEvent(true)
                        if (deltaX > 0) {
                            deltaX -= touchSlop
                        } else {
                            deltaX += touchSlop
                        }
                    }
                }

                when (dragOrientation) {
                    DragOrientation.VERTICAL -> {
                        lastMotionY = y

                        scrollConsumed[1] = 0
                        dispatchNestedPreScroll(deltaX, deltaY, scrollConsumed, null, ViewCompat.TYPE_TOUCH)
                        deltaY -= scrollConsumed[1]

                        consumed[1] = 0
                        doScroll(deltaY, consumed)
                        val unconsumedY = deltaY - consumed[1]

                        scrollConsumed[1] = 0
                        dispatchNestedScroll(
                            0, consumed[1],
                            0, unconsumedY,
                            null,
                            ViewCompat.TYPE_TOUCH,
                            scrollConsumed
                        )

                        dispatchCancelEventToSuperIfNeed(event)
                    }
                    DragOrientation.HORIZONTAL -> {
                        lastMotionX = x
                        scrollConsumed[0] = 0
                        dispatchNestedPreScroll(deltaX, deltaY, scrollConsumed, null, ViewCompat.TYPE_TOUCH)
                        deltaX -= scrollConsumed[0]

                        consumed[0] = 0
                        doScrollX(deltaX, consumed)
                        val unconsumedX = deltaX - consumed[0]

                        dispatchNestedScroll(
                            consumed[0], 0,
                            unconsumedX, 0,
                            null,
                            ViewCompat.TYPE_TOUCH,
                            scrollConsumed
                        )

                        dispatchCancelEventToSuperIfNeed(event)
//                        dispatchMoveEventToSuper(event)
                    }
                    else -> Unit
                }
            }
            MotionEvent.ACTION_UP -> {
                when (dragOrientation) {
                    DragOrientation.VERTICAL -> {
                        velocityTracker?.computeCurrentVelocity(1000, maximumFlingVelocity)
                        val yVelocity = velocityTracker?.yVelocity ?: 0f
                        if (abs(yVelocity) >= minimumFlingVelocity) {
                            if (!dispatchNestedPreFling(0f, yVelocity)) {
                                dispatchNestedFling(0f, yVelocity, true)
                                doFling(-yVelocity.toInt())
                            }
                            dispatchCancelEventToSuperIfNeed(event)
                        } else {
                            dispatchUpEventToSuperIfNeed(event)
                        }
                    }
                    DragOrientation.HORIZONTAL -> {
                        dispatchUpEventToSuperIfNeed(event)
                    }
                    null -> {
                        dispatchUpEventToSuperIfNeed(event)
                    }
                }
                endDrag()
            }
            MotionEvent.ACTION_CANCEL -> {
                dispatchCancelEventToSuperIfNeed(event)
                endDrag()
            }
        }

        initVelocityTrackerIfNotExists()
        velocityTracker?.addMovement(event)

        return true
    }

    private fun dispatchMoveEventToSuper(event: MotionEvent) {
        val superEvent = MotionEvent.obtain(event)
        val offsetY = (downMotionY - event.y)
        val offsetX = (downMotionX - event.x)
        superEvent.offsetLocation(offsetX, offsetY)
        super.onTouchEvent(superEvent)
        superEvent.recycle()
    }

    private fun dispatchUpEventToSuperIfNeed(event: MotionEvent) {
        if (!isUpEventDispatched && !isCancelEventDispatched) {
            isUpEventDispatched = true
            val superEvent = MotionEvent.obtain(event)
            val offsetX = (downMotionX - event.x)
            val offsetY = (downMotionY - event.y)
            superEvent.offsetLocation(offsetX, offsetY)
//            if (dragOrientation == DragOrientation.HORIZONTAL) {
//                superEvent.offsetLocation(0f, offsetY)
//            } else {
//                superEvent.offsetLocation(offsetX, offsetY)
//            }
            super.onTouchEvent(superEvent)
            superEvent.recycle()
        }
    }

    private fun dispatchCancelEventToSuperIfNeed(event: MotionEvent) {
        if (!isCancelEventDispatched) {
            isCancelEventDispatched = true
            val superEvent = MotionEvent.obtain(event)
            val offsetX = (downMotionX - event.x)
            val offsetY = (downMotionY - event.y)
            superEvent.offsetLocation(offsetX, offsetY)
            superEvent.action = MotionEvent.ACTION_CANCEL
            super.onTouchEvent(superEvent)
            superEvent.recycle()
        }
    }

    private fun endDrag() {
        dragOrientation = null
        recycleVelocityTracker()
        stopNestedScroll(ViewCompat.TYPE_TOUCH)
    }

    private fun initVelocityTrackerIfNotExists() {
        velocityTracker ?: kotlin.run {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun initOrResetVelocityTracker() {
        velocityTracker?.clear() ?: kotlin.run {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.apply {
            clear()
            velocityTracker = null
        }
    }

    private fun getVerticalScrollOffset(): Int {
        return computeVerticalScrollOffset()
    }

    private fun getVerticalScrollRange(): Int {
        return computeVerticalScrollRange() - computeVerticalScrollExtent()
    }

    private fun getHorizontalScrollOffset(): Int {
        return computeHorizontalScrollOffset()
    }

    private fun getHorizontalScrollRange(): Int {
        return computeHorizontalScrollRange() - computeHorizontalScrollExtent()
    }

    private fun doScrollX(deltaX: Int, consumed: IntArray) {
        val scrollRange = getHorizontalScrollRange()
        val scrollOffset = getHorizontalScrollOffset()
        var consumedX = deltaX
        val newScrollOffset = scrollOffset + deltaX
        if (deltaX >= 0) {
            if (newScrollOffset > scrollRange) {
                consumedX = scrollRange - scrollOffset
            }
        } else {
            if (newScrollOffset < 0) {
                consumedX = -scrollOffset
            }
        }
        scrollBy(consumedX, 0)
        consumed[0] = consumedX
    }

    private fun doScroll(deltaY: Int, consumed: IntArray) {
        val scrollRange = getVerticalScrollRange()
        val scrollOffset = getVerticalScrollOffset()
        var consumedY = deltaY
        val newScrollOffset = scrollOffset + deltaY
        if (deltaY >= 0) {
            if (newScrollOffset > scrollRange) {
                consumedY = scrollRange - scrollOffset
            }
        } else {
            if (newScrollOffset < 0) {
                consumedY = -scrollOffset
            }
        }
        scrollBy(0, consumedY)
        consumed[1] = consumedY
    }

    private fun doFling(yVelocity: Int) {
        lastScrollY = scrollY
        scroller.fling(
            0, lastScrollY,
            0, yVelocity,
            0, 0,
            Int.MIN_VALUE, Int.MAX_VALUE
        )
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
        ViewCompat.postOnAnimation(this, this)
    }

    override fun run() {
        if (scroller.computeScrollOffset()) {
            val currentY = scroller.currY
            var deltaY = currentY - lastScrollY
            lastScrollY = currentY

            scrollConsumed[1] = 0
            dispatchNestedPreScroll(0, deltaY, scrollConsumed, null, ViewCompat.TYPE_NON_TOUCH)
            deltaY -= scrollConsumed[1]

            consumed[1] = 0
            doScroll(deltaY, consumed)
            var unconsumedY = deltaY - consumed[1]

            scrollConsumed[1] = 0
            dispatchNestedScroll(0, consumed[1], 0, unconsumedY, null, ViewCompat.TYPE_NON_TOUCH, scrollConsumed)
            unconsumedY -= scrollConsumed[1]

            if (unconsumedY != 0) {
                scroller.abortAnimation()
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
            } else {
                ViewCompat.postOnAnimation(this, this)
            }
        }
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return childHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        childHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return childHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

}

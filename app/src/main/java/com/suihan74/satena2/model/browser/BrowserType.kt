package com.suihan74.satena2.model.browser

import com.suihan74.satena2.R
import com.suihan74.satena2.model.TextIdContainer

/**
 * 使用するアプリ内ブラウザ
 */
enum class BrowserType(
    override val textId: Int
) : TextIdContainer {
    /**
     * WebView
     */
    WEB_VIEW(R.string.browser_type_webview),

    /**
     * CustomTabsIntent
     */
    CUSTOM_TABS_INTENT(R.string.browser_type_custom_tabs)
}

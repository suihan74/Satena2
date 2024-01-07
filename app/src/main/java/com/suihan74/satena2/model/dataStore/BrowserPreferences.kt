package com.suihan74.satena2.model.dataStore

import android.content.Context
import androidx.compose.ui.Alignment
import androidx.datastore.dataStore
import com.suihan74.satena2.model.browser.BrowserType
import com.suihan74.satena2.model.browser.WebViewTheme
import com.suihan74.satena2.serializer.VerticalAlignmentSerializer
import kotlinx.serialization.Serializable

val Context.browserPreferencesDataStore by dataStore(
    fileName = "browser_preferences",
    serializer = jsonDataStoreSerializer(defaultValue = { BrowserPreferences() })
)

// ------ //

/** 現行の設定バージョン */
private const val LATEST_VERSION: Int = 0

/**
 * 設定データ
 */
@PreferenceVersion(LATEST_VERSION)
@Serializable
data class BrowserPreferences(
    /**
     * 使用するアプリ内ブラウザ
     */
    val browserType: BrowserType = BrowserType.WEB_VIEW,
    /**
     * スタートページ
     */
    val startPageUrl: String = "https://www.hatena.ne.jp/",
    /**
     * JavaScript有効状態
     */
    val javascriptEnabled: Boolean = true,
    /**
     * URLブロック有効状態
     */
    val urlBlockingEnabled: Boolean = true,
    /**
     * アドレスバーの配置
     */
    @Serializable(with = VerticalAlignmentSerializer::class)
    val addressBarAlignment: Alignment.Vertical = Alignment.Bottom,
    /**
     * ウェブサイトのテーマ
     */
    val webViewTheme: WebViewTheme = WebViewTheme.AUTO,
    /**
     * ブロック対象のURL
     */
    val blockedResources: List<String> = emptyList()
)

package com.suihan74.satena2.scene.preferences.page.browser

import androidx.compose.ui.Alignment
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.model.browser.BrowserType
import com.suihan74.satena2.model.browser.WebViewTheme
import com.suihan74.satena2.model.dataStore.BrowserPreferences
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface BrowserViewModel : IPreferencePageViewModel {
    /**
     * 使用するアプリ内ブラウザ
     */
    val browserType : MutableStateFlow<BrowserType>
    /**
     * スタートページ
     */
    val startPageUrl : MutableStateFlow<String>
    /**
     * JavaScript有効状態
     */
    val javascriptEnabled : MutableStateFlow<Boolean>
    /**
     * URLブロック有効状態
     */
    val urlBlockingEnabled : MutableStateFlow<Boolean>
    /**
     * アドレスバーの配置
     */
    val addressBarAlignment : MutableStateFlow<Alignment.Vertical>
    /**
     * ウェブサイトのテーマ
     */
    val webViewTheme : MutableStateFlow<WebViewTheme>

    // ------ //

    /**
     * ブラウザで現在表示しているページのURL
     *
     * 設定画面をブラウザで表示している場合だけ使用
     */
    val currentUrl : MutableStateFlow<String?>
}

// ------ //

@HiltViewModel
class BrowserViewModelImpl @Inject constructor(
    private val dataStore: DataStore<BrowserPreferences>,
    prefsDataStore: DataStore<Preferences>
) :
    BrowserViewModel,
    PreferencePageViewModel(prefsDataStore)
{
    /**
     * 使用するアプリ内ブラウザ
     */
    override val browserType : MutableStateFlow<BrowserType> = prefsStateFlow(BrowserType.WEB_VIEW)
    /**
     * スタートページ
     */
    override val startPageUrl : MutableStateFlow<String> = prefsStateFlow("")
    /**
     * JavaScript有効状態
     */
    override val javascriptEnabled : MutableStateFlow<Boolean> = prefsStateFlow(true)
    /**
     * URLブロック有効状態
     */
    override val urlBlockingEnabled : MutableStateFlow<Boolean> = prefsStateFlow(true)
    /**
     * アドレスバーの配置
     */
    override val addressBarAlignment : MutableStateFlow<Alignment.Vertical> = prefsStateFlow(Alignment.Bottom)
    /**
     * ウェブサイトのテーマ
     */
    override val webViewTheme : MutableStateFlow<WebViewTheme> = prefsStateFlow(WebViewTheme.AUTO)

    // ------ //

    /**
     * ブラウザで現在表示しているページのURL
     *
     * 設定画面をブラウザで表示している場合だけ使用
     */
    override val currentUrl : MutableStateFlow<String?> = MutableStateFlow(null)

    // ------ //

    init {
        dataStore.data
            .onEach {
                browserType.value = it.browserType
                startPageUrl.value = it.startPageUrl
                javascriptEnabled.value = it.javascriptEnabled
                urlBlockingEnabled.value = it.urlBlockingEnabled
                addressBarAlignment.value = it.addressBarAlignment
                webViewTheme.value = it.webViewTheme
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * 値変更に連動してデータストアを更新する`MutableStateFlow`の`EntryViewModel`用のインスタンスを生成する
     */
    private fun <T> prefsStateFlow(initialValue: T) =
        MutableStateFlow(initialValue).also {
            it.onEach {
                dataStore.updateData { prefs ->
                    prefs.copy(
                        browserType = browserType.value,
                        startPageUrl = startPageUrl.value,
                        javascriptEnabled = javascriptEnabled.value,
                        urlBlockingEnabled = urlBlockingEnabled.value,
                        addressBarAlignment = addressBarAlignment.value,
                        webViewTheme = webViewTheme.value
                    )
                }
            }.launchIn(viewModelScope)
        }
}

// ------ //

class FakeBrowserViewModel :
    BrowserViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val browserType = MutableStateFlow(BrowserType.WEB_VIEW)
    override val startPageUrl = MutableStateFlow("")
    override val javascriptEnabled = MutableStateFlow(true)
    override val urlBlockingEnabled = MutableStateFlow(true)
    override val addressBarAlignment = MutableStateFlow(Alignment.Bottom)
    override val webViewTheme = MutableStateFlow(WebViewTheme.AUTO)

    // ------ //

    override val currentUrl : MutableStateFlow<String?> = MutableStateFlow(null)
}

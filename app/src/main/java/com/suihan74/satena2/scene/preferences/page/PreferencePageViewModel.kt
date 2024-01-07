package com.suihan74.satena2.scene.preferences.page

import androidx.datastore.core.DataStore
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.utility.DialogPropertiesProvider
import com.suihan74.satena2.utility.DialogPropertiesProviderImpl
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.LazyListStateProvider
import com.suihan74.satena2.utility.extension.LazyListStateProviderImpl

/**
 * 設定ページ用ViewModelの共通部分インタフェース
 */
interface IPreferencePageViewModel : DialogPropertiesProvider, LazyListStateProvider

// ------ //

/**
 * 設定ページ用ViewModelベースクラス(Android用の実装)
 */
abstract class PreferencePageViewModel(
    dataStore: DataStore<Preferences>
) :
    IPreferencePageViewModel,
    LazyListStateProvider by LazyListStateProviderImpl(),
    DialogPropertiesProvider by DialogPropertiesProviderImpl(dataStore),
    ViewModel()

// ------ //

class FakePreferencesPageViewModelImpl :
    IPreferencePageViewModel,
    LazyListStateProvider by LazyListStateProviderImpl()

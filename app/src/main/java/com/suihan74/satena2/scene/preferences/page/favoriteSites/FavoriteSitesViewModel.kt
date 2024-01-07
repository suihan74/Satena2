package com.suihan74.satena2.scene.preferences.page.favoriteSites

import androidx.datastore.core.DataStore
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

interface FavoriteSitesViewModel : IPreferencePageViewModel {

}

// ------ //

@HiltViewModel
class FavoriteSitesViewModelImpl @Inject constructor(
    dataStore: DataStore<Preferences>
) :
    FavoriteSitesViewModel,
    PreferencePageViewModel(dataStore)
{
}

// ------ //

/**
 * Preview用のダミーViewModel
 */
class FakeFavoriteSitesViewModel :
    FavoriteSitesViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
}

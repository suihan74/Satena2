package com.suihan74.satena2.scene.preferences.page.ngUsers

import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.R
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

interface NgUsersViewModel : IPreferencePageViewModel {
    val loading : StateFlow<Boolean>

    val ngUsers : StateFlow<List<String>>

    val searchQuery : StateFlow<String>

    fun iconUrl(username: String) = "https://cdn.profile-image.st-hatena.com/users/$username/profile.png"

    // ------ //

    /**
     * 抽出用クエリを設定する
     */
    fun setSearchQuery(query: String)

    /**
     * NGユーザー項目を削除する
     */
    suspend fun removeNgUser(username: String)

    /**
     * リストを再読み込みする
     */
    fun reload()
}

// ------ //

@HiltViewModel
class NgUsersViewModelImpl @Inject constructor(
    private val repository: HatenaAccountRepository,
    dataStore: DataStore<Preferences>
) : NgUsersViewModel, PreferencePageViewModel(dataStore) {

    override val loading = repository.loadingNgUsers

    override val ngUsers = MutableStateFlow<List<String>>(emptyList())

    private val allNgUsers = repository.ngUsers

    override val searchQuery = MutableStateFlow("")

    // ------ //

    init {
        allNgUsers
            .combine(searchQuery, ::Pair)
            .onEach { (rawList, query) ->
                if (query.isBlank()) {
                    ngUsers.value = rawList
                }
                else {
                    ngUsers.value = rawList.filter { it.lowercase().contains(query) }
                }
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * 抽出用クエリを設定する
     */
    override fun setSearchQuery(query: String) {
        searchQuery.value = query.lowercase()
    }

    /**
     * NGユーザー項目を削除する
     */
    override suspend fun removeNgUser(username: String) {
        runCatching {
            repository.removeNgUser(username)
        }.onSuccess {
            context.showToast("id:${username}をNG解除しました", Toast.LENGTH_SHORT)
        }.onFailure {
            context.showToast("ユーザーNG解除に失敗しました", Toast.LENGTH_SHORT)
        }
    }

    /**
     * リストを再読み込みする
     */
    override fun reload() {
        viewModelScope.launch {
            runCatching {
                repository.reloadNgUsers()
            }.onFailure {
                context.showToast(R.string.pref_fetch_ng_users_failure)
            }
        }
    }
}

// ------ //

class FakeNgUsersViewModel :
    NgUsersViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val loading = MutableStateFlow(false)

    override val ngUsers = MutableStateFlow(
        listOf(
            "test0", "hello1", "world2", "suihan", "nanashi"
        )
    )

    override val searchQuery = MutableStateFlow("")

    // ------ //

    override fun setSearchQuery(query: String) {}

    override suspend fun removeNgUser(username: String) {}

    override fun reload() {}
}

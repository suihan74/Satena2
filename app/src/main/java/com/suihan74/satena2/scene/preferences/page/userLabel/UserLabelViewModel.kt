package com.suihan74.satena2.scene.preferences.page.userLabel

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.R
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.model.userLabel.LabelAndUsers
import com.suihan74.satena2.model.userLabel.UserAndLabels
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface UserLabelsViewModel : IPreferencePageViewModel {
    /**
     * すべてのラベル
     */
    val labelsFlow : Flow<List<Label>>

    /**
     * 指定ラベルの所属ユーザーリスト
     */
    fun labeledUsersFlow(label: Label) : Flow<LabelAndUsers?>

    /**
     * 指定ユーザーが所属しているラベルリスト
     */
    fun userAndLabelsFlow(user: String) : Flow<UserAndLabels?>

    // ------ //

    /**
     * ラベルを追加
     */
    suspend fun createLabel(label: Label) : Boolean

    /**
     * ラベルを削除
     */
    suspend fun deleteLabel(label: Label) : Boolean

    // ------ //

    /**
     * ユーザーにつけられたラベルを更新する
     */
    fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>)
}

// ------ //

@HiltViewModel
class UserLabelsViewModelImpl @Inject constructor(
    private val repository: UserLabelRepository,
    dataStore: DataStore<Preferences>
) : UserLabelsViewModel, PreferencePageViewModel(dataStore)
{
    /**
     * すべてのラベル
     */
    override val labelsFlow = repository.allUserLabelsFlow

    /**
     * 指定ラベルの所属ユーザーリスト
     */
    override fun labeledUsersFlow(label: Label) : Flow<LabelAndUsers?> {
        return repository.labeledUsersFlow(label)
    }

    /**
     * 指定ユーザーが所属しているラベルリスト
     */
    override fun userAndLabelsFlow(user: String) : Flow<UserAndLabels?> {
        return repository.userLabelsFlow(user)
    }

    // ------ //

    /**
     * ラベルを追加or更新
     */
    override suspend fun createLabel(label: Label) : Boolean {
        val result = runCatching {
            repository.createLabel(label)
        }.onSuccess {
            context.showToast(
                context.getString(R.string.register_user_label_success, label.name)
            )
        }.onFailure {
            context.showToast(
                context.getString(R.string.register_user_label_failure, label.name)
            )
        }
        return result.isSuccess
    }

    /**
     * ラベルを削除
     */
    override suspend fun deleteLabel(label: Label) : Boolean {
        val result = runCatching {
            repository.deleteLabel(label)
        }.onSuccess {
            context.showToast(
                context.getString(R.string.delete_user_label_success, label.name)
            )
        }.onFailure {
            // todo
            Log.e("userLabel", it.stackTraceToString())
        }
        return result.isSuccess
    }

    // ------ //

    /**
     * ユーザーにつけられたラベルを更新する
     */
    override fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>) {
        viewModelScope.launch {
            runCatching {
                repository.updateUserLabels(user, states)
            }.onSuccess {
                context.showToast(
                    context.getString(R.string.set_user_label_success, user)
                )
            }.onFailure {
                // todo
                Log.e("userLabel", it.stackTraceToString())
            }
        }
    }
}

// ------ //

class FakeUserLabelsViewModel
    : UserLabelsViewModel,
      IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val labelsFlow = MutableStateFlow<List<Label>>(emptyList())

    override fun labeledUsersFlow(label: Label) : Flow<LabelAndUsers?> {
        return MutableStateFlow(null)
    }

    override fun userAndLabelsFlow(user: String) : Flow<UserAndLabels?> {
        return MutableStateFlow(null)
    }

    // ------ //

    override suspend fun createLabel(label: Label) : Boolean {
        return true
    }

    override suspend fun deleteLabel(label: Label) : Boolean {
        return true
    }

    // ------ //

    override fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>) {
    }
}

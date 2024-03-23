package com.suihan74.satena2.scene.preferences.page.theme

import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.R
import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.theme.InvalidThemePresetNameError
import com.suihan74.satena2.model.theme.ThemePreset
import com.suihan74.satena2.model.theme.ThemePresetDuplicationError
import com.suihan74.satena2.model.theme.default.DefaultThemePresetLight
import com.suihan74.satena2.model.theme.default.TemporaryTheme
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ThemeViewModel : IPreferencePageViewModel {
    /**
     * すべてのプリセット
     */
    val allPresetsFlow : Flow<List<ThemePreset>>

    /**
     * 現在アプリに適用されているテーマ
     */
    val currentThemeFlow : Flow<ThemePreset>

    // ------ //

    /**
     * 現在テーマを渡されたプリセットの内容に変更する
     */
    fun updateCurrentTheme(preset: ThemePreset)

    /**
     * プリセットを更新
     */
    suspend fun update(preset: ThemePreset) : Boolean

    /**
     * 渡されたプリセットのコピーを作成
     */
    suspend fun copy(preset: ThemePreset) : Boolean

    /**
     * プリセットを削除
     */
    fun delete(preset: ThemePreset)

    /**
     * プリセット名が使用できるか確認
     */
    suspend fun checkInsertable(preset: ThemePreset) : Boolean
}

// ------ //

@HiltViewModel
class ThemeViewModelImpl @Inject constructor(
    appDatabase : AppDatabase,
    dataStore: DataStore<Preferences>
) : ThemeViewModel, PreferencePageViewModel(dataStore) {
    private val dao = appDatabase.themeDao()

    /**
     * すべてのプリセット
     */
    override val allPresetsFlow = dao.allPresetsFlow()

    /**
     * 現在アプリに適用されているテーマ
     */
    override val currentThemeFlow: StateFlow<ThemePreset> =
        dao.currentThemeFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = TemporaryTheme
        )

    // ------ //

    /**
     * 現在テーマを渡されたプリセットの内容に変更する
     */
    override fun updateCurrentTheme(preset: ThemePreset) {
        viewModelScope.launch {
            dao.updateCurrentTheme(preset)
        }
    }

    /**
     * プリセットを更新
     *
     * システムデフォルトの場合更新しないで別名で保存する
     */
    override suspend fun update(preset: ThemePreset) : Boolean {
        return if (preset.isSystemDefault) {
            copy(preset)
        }
        else {
            runCatching {
                dao.update(preset)
            }.onSuccess {
                context.showToast(R.string.pref_theme_msg_saving_preset_succeeded)
            }.onFailure { e ->
                withContext(Dispatchers.Main) {
                    when (e) {
                        is InvalidThemePresetNameError ->
                            context.showToast(R.string.pref_theme_msg_empty_preset_title)

                        is ThemePresetDuplicationError ->
                            context.showToast(R.string.pref_theme_msg_already_existed_preset_title)
                    }
                }
            }.isSuccess
        }
    }

    /**
     * 渡されたプリセットのコピーを作成
     */
    override suspend fun copy(preset: ThemePreset) : Boolean {
        val name = let {
            var name =
                if (preset.isCurrentTheme) "${context.getString(R.string.pref_theme_current_theme)}_copy"
                else "${preset.name}_copy"
            var i = 0
            while (dao.exists(name) != null) {
                name = "${preset.name}_copy${i++}"
            }
            name
        }

        return runCatching {
            dao.insert(
                preset.copy(
                    id = 0L,
                    name = name,
                    isSystemDefault = false
                )
            )
        }.onSuccess {
            context.showToast(R.string.pref_theme_msg_copying_preset_succeeded)
        }.onFailure { e ->
            withContext(Dispatchers.Main) {
                when (e) {
                    is InvalidThemePresetNameError ->
                        context.showToast(R.string.pref_theme_msg_empty_preset_title)

                    is ThemePresetDuplicationError ->
                        context.showToast(R.string.pref_theme_msg_already_existed_preset_title)
                }
            }
        }.isSuccess
    }

    /**
     * プリセットを削除
     */
    override fun delete(preset: ThemePreset) {
        if (preset.isCurrentTheme || preset.isSystemDefault) return
        viewModelScope.launch {
            dao.delete(preset)
        }
    }

    /**
     * プリセット名が使用できるか確認
     */
    override suspend fun checkInsertable(preset: ThemePreset) : Boolean {
        return runCatching {
            if (preset.name.isBlank()) {
                throw InvalidThemePresetNameError()
            }
            dao.exists(preset.name)?.let { existId ->
                if (existId != preset.id) {
                    throw ThemePresetDuplicationError()
                }
            }
        }.onFailure { e ->
            withContext(Dispatchers.Main) {
                when (e) {
                    is InvalidThemePresetNameError ->
                        context.showToast(R.string.pref_theme_msg_empty_preset_title)

                    is ThemePresetDuplicationError ->
                        context.showToast(R.string.pref_theme_msg_already_existed_preset_title)
                }
            }
        }.isSuccess
    }
}

// ------ //

class FakeThemeViewModel :
    ThemeViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val allPresetsFlow = MutableStateFlow(
        buildList {
            repeat(20) {
                add(
                    ThemePreset(
                        name = "Item$it",
                        isSystemDefault = it < 3,
                        colors = DefaultThemePresetLight.colors
                    )
                )
            }
        }
    )

    override val currentThemeFlow: Flow<ThemePreset> = MutableStateFlow(
        ThemePreset(
            name = "現在の設定",
            isSystemDefault = false,
            colors = DefaultThemePresetLight.colors
        )
    )

    // ------ //

    override fun updateCurrentTheme(preset: ThemePreset) {
    }

    override suspend fun update(preset: ThemePreset) : Boolean = true

    override suspend fun copy(preset: ThemePreset) : Boolean = true

    override fun delete(preset: ThemePreset) {
    }

    /**
     * プリセット名が使用できるか確認
     */
    override suspend fun checkInsertable(preset: ThemePreset) : Boolean = true
}

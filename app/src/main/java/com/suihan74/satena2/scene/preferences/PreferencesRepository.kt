package com.suihan74.satena2.scene.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.core.DataStore
import com.suihan74.satena2.Application
import com.suihan74.satena2.model.dataStore.Preferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

interface PreferencesRepository {
    /**
     * 設定データストア
     */
    val dataStore : DataStore<Preferences>

    // ------ //

    /**
     * 値更新に連動して設定用データストアを更新する`MutableStateFlow`を生成する
     */
    fun <T> prefsStateFlow(
        key: String,
        initialValue: T,
        updater: (Preferences)->Preferences
    ) : MutableStateFlow<T>

    // ------ //

    /**
     * 設定を反映したダイアログプロパティ
     */
    @Composable
    fun dialogProperties() : DialogProperties
}

// ------ //

class PreferencesRepositoryImpl @Inject constructor(
    private val application : Application,
    override val dataStore : DataStore<Preferences>
) : PreferencesRepository {

    private var updaterJobs : HashMap<String, Job?> = HashMap()

    /**
     * 値更新に連動して設定用データストアを更新する`MutableStateFlow`を生成する
     */
    override fun <T> prefsStateFlow(
        key: String,
        initialValue: T,
        updater: (Preferences)->Preferences
    ) = MutableStateFlow(initialValue).apply {
        onEach {
            runCatching {
                updaterJobs[key]?.cancel()
            }
            runCatching {
                updaterJobs[key] = application.coroutineScope.launch {
                    delay(1_000)
                    dataStore.updateData { prefs -> updater(prefs) }
                    updaterJobs.remove(key)
                }
            }
        }.launchIn(application.coroutineScope)
    }

    // ------ //

    /**
     * ダイアログの外側をタップしたら閉じる
     */
    private val dismissDialogOnClickOutside = dataStore.data.map { it.dismissDialogOnClickOutside }

    /**
     * 設定を反映したダイアログプロパティ
     */
    @Composable
    override fun dialogProperties() : DialogProperties {
        return DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = dismissDialogOnClickOutside.collectAsState(initial = false).value,
            usePlatformDefaultWidth = false
        )
    }
}

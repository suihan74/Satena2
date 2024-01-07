package com.suihan74.satena2.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.datastore.core.DataStore
import com.suihan74.satena2.model.dataStore.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * アプリ設定を反映した[DialogProperties]を[ViewModel]などから供給するためのインターフェイス
 */
interface DialogPropertiesProvider {
    /**
     * ダイアログ設定
     */
    @Composable
    fun dialogProperties(
        dismissOnBackPress: Boolean? = null,
        dismissOnClickOutside: Boolean? = null,
        securePolicy: SecureFlagPolicy? = null,
        usePlatformDefaultWidth: Boolean? = null,
        decorFitsSystemWindows: Boolean? = null
    ) : DialogProperties {
        return DialogProperties(
            dismissOnBackPress = dismissOnBackPress ?: true,
            dismissOnClickOutside = dismissOnClickOutside ?: true,
            securePolicy = securePolicy ?: SecureFlagPolicy.Inherit,
            usePlatformDefaultWidth = usePlatformDefaultWidth ?: true,
            decorFitsSystemWindows = decorFitsSystemWindows ?: true
        )
    }
}

// ------ //

class DialogPropertiesProviderImpl(
    private val dataStore: DataStore<Preferences>
) : DialogPropertiesProvider {
    private var _dismissDialogOnClickOutside : Flow<Boolean>? = null
    private val dismissDialogOnClickOutside : Flow<Boolean>
        get() =
            _dismissDialogOnClickOutside
                ?: dataStore.data
                    .map { it.dismissDialogOnClickOutside }
                    .also {
                        _dismissDialogOnClickOutside = it
                    }

    @Composable
    override fun dialogProperties(
        dismissOnBackPress: Boolean?,
        dismissOnClickOutside: Boolean?,
        securePolicy: SecureFlagPolicy?,
        usePlatformDefaultWidth: Boolean?,
        decorFitsSystemWindows: Boolean?
    ): DialogProperties {
        return DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside =
                dismissOnClickOutside ?: dismissDialogOnClickOutside.collectAsState(initial = false).value,
            usePlatformDefaultWidth = false
        )
    }
}

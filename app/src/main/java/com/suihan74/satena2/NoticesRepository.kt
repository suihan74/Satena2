package com.suihan74.satena2

import androidx.datastore.core.DataStore
import com.suihan74.satena2.model.dataStore.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface NoticesRepository {
    /**
     * バックグラウンドでの通知確認を行うか
     */
    val notificationEnabled: StateFlow<Boolean>

    /**
     * バックグラウンドでの通知確認のインターバル（分）
     */
    val intervals: StateFlow<Int>

    /** 同じコメントに対する通知で複数回鳴動する */
    val noticeSameComment : StateFlow<Boolean>

    /** 通知鳴動時に既読フラグを更新する */
    val updateReadFlagOnNotification : StateFlow<Boolean>
}

// ------ //

class NoticesRepositoryImpl(
    application: Application,
    dataStore: DataStore<Preferences>
) : NoticesRepository {
    /**
     * バックグラウンドでの通知確認を行うか
     */
    override val notificationEnabled = MutableStateFlow(false)

    /**
     * バックグラウンドでの通知確認のインターバル（分）
     */
    override val intervals = MutableStateFlow(15)

    /** 同じコメントに対する通知で複数回鳴動する */
    override val noticeSameComment = MutableStateFlow(true)

    /** 通知鳴動時に既読フラグを更新する */
    override val updateReadFlagOnNotification = MutableStateFlow(true)

    // ------ //

    init {
        dataStore.data
            .onEach {
                notificationEnabled.value = it.backgroundCheckingNoticesEnabled
                intervals.value = it.checkingNoticesIntervals
                noticeSameComment.value = it.noticeSameComment
                updateReadFlagOnNotification.value = it.updateReadFlagOnNotification
            }
            .flowOn(Dispatchers.IO)
            .launchIn(application.coroutineScope)
    }
}

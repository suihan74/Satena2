package com.suihan74.satena2.scene.preferences.page.general

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.preferences.PreferencesCategory
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import com.suihan74.satena2.utility.extension.showToast
import com.suihan74.satena2.utility.migration.AppDataMigrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface GeneralViewModel : IPreferencePageViewModel {
    /** 日時をシステムのタイムゾーンで表示する */
    val useSystemTimeZone : MutableStateFlow<Boolean>

    /** ドロワの配置 */
    val drawerAlignment : MutableStateFlow<Alignment.Horizontal>

    /** 長押し時の振動時間 */
    val longClickVibrationDuration : MutableStateFlow<Long>

    //

    /** 常駐して通知を確認する */
    val backgroundCheckingNoticesEnabled : MutableStateFlow<Boolean>

    /** 通知権限要求ランチャを実行するためのフラグを伝搬する */
    val launchRequestNotificationPermission : MutableStateFlow<Boolean>

    /** 常駐して通知を確認する間隔（分） */
    val checkingNoticesIntervals : MutableStateFlow<Int>

    /** 同じコメントに対する通知で複数回鳴動する */
    val noticeSameComment : MutableStateFlow<Boolean>

    /** 通知鳴動時に既読フラグを更新する */
    val updateReadFlagOnNotification : MutableStateFlow<Boolean>

    /** アップデート後にリリースノートを表示する */
    val showingReleaseNotesAfterUpdateEnabled : MutableStateFlow<Boolean>

    /** 一度無視したアップデート通知を再度通知する */
    val noticeUpdateOnceIgnored : MutableStateFlow<Boolean>

    /** 外部アプリを開かう際に毎回アプリを選択する */
    val useIntentChooser : MutableStateFlow<Boolean>

    /** ダイアログの外側をタップしたら閉じる */
    val closeDialogByTouchingOutside : MutableStateFlow<Boolean>

    // ------ //

    /**
     * [Activity]作成時にインテントランチャの初期化を行う
     */
    fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    )

    /**
     * Android通知送信の権限をリクエストする
     */
    fun requestNotificationPermission(requestPermission: ActivityResultLauncher<String>)

    /**
     * アプリデータをエクスポートする処理を開始
     */
    fun launchAppDataExport()
}

// ------ //

@HiltViewModel
class GeneralViewModelImpl @Inject constructor(
    private val repository : PreferencesRepository
) :
    GeneralViewModel,
    PreferencePageViewModel(repository.dataStore)
{
    override val useSystemTimeZone: MutableStateFlow<Boolean> = prefsStateFlow(false)
    override val drawerAlignment: MutableStateFlow<Alignment.Horizontal> = prefsStateFlow(Alignment.Start)
    override val longClickVibrationDuration : MutableStateFlow<Long> = prefsStateFlow(40L)
    override val backgroundCheckingNoticesEnabled : MutableStateFlow<Boolean> = prefsStateFlow(true)
    override val launchRequestNotificationPermission = MutableStateFlow(false)
    override val checkingNoticesIntervals : MutableStateFlow<Int> = prefsStateFlow(15)
    override val noticeSameComment : MutableStateFlow<Boolean> = prefsStateFlow(true)
    override val updateReadFlagOnNotification : MutableStateFlow<Boolean> = prefsStateFlow(true)
    override val showingReleaseNotesAfterUpdateEnabled : MutableStateFlow<Boolean> = prefsStateFlow(true)
    override val noticeUpdateOnceIgnored : MutableStateFlow<Boolean> = prefsStateFlow(true)
    override val useIntentChooser : MutableStateFlow<Boolean> = prefsStateFlow(true)
    override val closeDialogByTouchingOutside : MutableStateFlow<Boolean> = prefsStateFlow(true)

    // 権限リクエストから戻ってきたときにこの画面に戻ってくるためのリクエストコード
    private val permissionRequestCode = PreferencesCategory.General.ordinal

    // ------ //

    init {
        // データストアの値を反映
        repository.dataStore.data
            .onEach {
                useSystemTimeZone.value = it.useSystemTimeZone
                drawerAlignment.value = it.drawerAlignment
                longClickVibrationDuration.value = it.longClickVibrationDuration
                backgroundCheckingNoticesEnabled.value = it.backgroundCheckingNoticesEnabled
                checkingNoticesIntervals.value = it.checkingNoticesIntervals
                noticeSameComment.value = it.noticeSameComment
                updateReadFlagOnNotification.value = it.updateReadFlagOnNotification
                showingReleaseNotesAfterUpdateEnabled.value = it.showingReleaseNotesAfterUpdateEnabled
                noticeUpdateOnceIgnored.value = it.noticeUpdateOnceIgnored
                useIntentChooser.value = it.useIntentChooser
                closeDialogByTouchingOutside.value = it.dismissDialogOnClickOutside
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * 値変更に連動してデータストアを更新する[MutableStateFlow]の[GeneralViewModel]用のインスタンスを生成する
     */
    private fun <T> prefsStateFlow(initialValue: T) =
        repository.prefsStateFlow("General", initialValue) { prefs ->
            prefs.copy(
                useSystemTimeZone = useSystemTimeZone.value,
                drawerAlignment = drawerAlignment.value,
                longClickVibrationDuration = longClickVibrationDuration.value,
                backgroundCheckingNoticesEnabled = backgroundCheckingNoticesEnabled.value,
                checkingNoticesIntervals = checkingNoticesIntervals.value,
                noticeSameComment = noticeSameComment.value,
                updateReadFlagOnNotification = updateReadFlagOnNotification.value,
                showingReleaseNotesAfterUpdateEnabled = showingReleaseNotesAfterUpdateEnabled.value,
                noticeUpdateOnceIgnored = noticeUpdateOnceIgnored.value,
                useIntentChooser = useIntentChooser.value,
                dismissDialogOnClickOutside = closeDialogByTouchingOutside.value
            )
        }

    // ------ //

    /**
     * [Activity]作成時にインテントランチャの初期化を行う
     */
    override fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
        lifecycleObserver = LifecycleObserver(activityResultRegistry!!)
        lifecycle?.addObserver(lifecycleObserver)
    }

    /**
     * Android通知送信の権限をリクエストする
     */
    override fun requestNotificationPermission(requestPermission: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        viewModelScope.launch {
            context.showToast(R.string.request_notification_permission_msg, Toast.LENGTH_SHORT)
        }
        requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * アプリデータをエクスポートする処理を開始
     */
    override fun launchAppDataExport() {
        lifecycleObserver.launchFileCreatorLauncher()
    }

    private fun startAppDataExport(destUri: Uri) {
        viewModelScope.launch {
            runCatching {
                AppDataMigrator.Export()
                    .write(context, destUri)
                context.showToast(
                    context.getString(R.string.pref_general_export_appdata_succeeded)
                )
            }.onFailure {
                context.showToast(R.string.pref_general_export_appdata_failed)
            }
        }
    }

    // ------ //

    private lateinit var lifecycleObserver : LifecycleObserver

    inner class LifecycleObserver(
        private val registry : ActivityResultRegistry
    ) : DefaultLifecycleObserver {
        /** アプリデータ出力先ファイル選択ランチャ */
        private lateinit var fileCreatorLauncher : ActivityResultLauncher<String>

        override fun onCreate(owner: LifecycleOwner) {
            fileCreatorLauncher = registry.register(
                "filePickerLauncher",
                owner,
                ActivityResultContracts.CreateDocument("application/satena2-settings"),
            ) { result ->
                if (result == null) {
                    viewModelScope.launch {
                        context.showToast(R.string.pref_general_export_appdata_canceled)
                    }
                    return@register
                }
                else {
                    startAppDataExport(result)
                }
            }
        }

        fun launchFileCreatorLauncher() {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd-hh-mm-ss")
            val defaultFilename = "${formatter.format(now)}.satena2-settings"
            fileCreatorLauncher.launch(defaultFilename)
        }
    }
}

// ------ //

/**
 * Preview用のダミーViewModel
 */
class FakeGeneralViewModel :
    GeneralViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val useSystemTimeZone = MutableStateFlow(false)
    override val drawerAlignment = MutableStateFlow(Alignment.Start)
    override val longClickVibrationDuration = MutableStateFlow(40L)
    override val backgroundCheckingNoticesEnabled = MutableStateFlow(true)
    override val launchRequestNotificationPermission = MutableStateFlow(false)
    override val checkingNoticesIntervals = MutableStateFlow(15)
    override val noticeSameComment = MutableStateFlow(true)
    override val updateReadFlagOnNotification = MutableStateFlow(true)
    override val showingReleaseNotesAfterUpdateEnabled = MutableStateFlow(true)
    override val noticeUpdateOnceIgnored = MutableStateFlow(true)
    override val useIntentChooser = MutableStateFlow(true)
    override val closeDialogByTouchingOutside = MutableStateFlow(true)

    // ------ //

    override fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
    }

    override fun requestNotificationPermission(requestPermission: ActivityResultLauncher<String>) {
    }

    override fun launchAppDataExport() {
    }
}

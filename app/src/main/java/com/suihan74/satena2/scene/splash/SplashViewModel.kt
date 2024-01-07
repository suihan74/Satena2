package com.suihan74.satena2.scene.splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.entries.EntriesActivity
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.utility.ViewModel
import com.suihan74.satena2.utility.extension.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

interface SplashViewModel {
    /**
     * アプリのバージョン文字列
     */
    val versionName: String

    // ------ //

    /**
     * アクティビティ起動時に実行。完了後に画面遷移
     */
    suspend fun onStart(context: Context)
}

// ------ //

@HiltViewModel
class SplashViewModelImpl @Inject constructor(
    private val hatenaRepo: HatenaAccountRepository,
    private val prefsRepository : PreferencesRepository
) : SplashViewModel, ViewModel() {
    /**
     * アプリのバージョン文字列
     */
    override val versionName by lazy { application.versionName }

    // ------ //

    /**
     * アクティビティ起動時の初期化処理
     */
    fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
        lifecycleObserver = LifecycleObserver(activityResultRegistry!!)
        lifecycle?.addObserver(lifecycleObserver)
    }

    /**
     * アクティビティ起動時に実行。完了後に画面遷移
     */
    override suspend fun onStart(context: Context) {
        val overSDK33 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val notificationPermissionNotGranted =
            !overSDK33 ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

        if (notificationPermissionNotGranted) {
            val ds = runBlocking { prefsRepository.dataStore.data.first() }
            if (ds.backgroundCheckingNoticesEnabled) {
                lifecycleObserver.launchRequestNotificationPermission()
            }
            else {
                lifecycleObserver.launchEntriesActivity()
            }
        }
        else {
            lifecycleObserver.launchEntriesActivity()
        }
    }

    // ------ //

    /** 他画面とデータをやりとりしながら遷移するためのやつ */
    private lateinit var lifecycleObserver : LifecycleObserver

    inner class LifecycleObserver(
        private val registry : ActivityResultRegistry
    ) : DefaultLifecycleObserver {
        /** 通知権限リクエスト画面のランチャ */
        private lateinit var requestNotificationPermissionLauncher : ActivityResultLauncher<String>

        /** [EntriesActivity]のランチャ */
        private lateinit var entriesActivityLauncher : ActivityResultLauncher<Unit>

        override fun onCreate(owner: LifecycleOwner) {
            requestNotificationPermissionLauncher = registry.register(
                "RequestNotificationPermissionLauncher",
                owner,
                ActivityResultContracts.RequestPermission(),
            ) { result ->
                viewModelScope.launch {
                    if (result) {
                        context.showToast(R.string.request_notification_permission_granted_msg, Toast.LENGTH_SHORT)
                    }
                    else {
                        context.showToast(R.string.request_notification_permission_denied_msg, Toast.LENGTH_SHORT)
                    }
                    launchEntriesActivity()
                }
            }

            entriesActivityLauncher = registry.register(
                "EntriesActivityLauncher",
                owner,
                object : ActivityResultContract<Unit, Unit>() {
                    override fun createIntent(context: Context, input: Unit) =
                        Intent(context, EntriesActivity::class.java)
                    override fun parseResult(resultCode: Int, intent: Intent?) { /* do nothing */ }
                }
            ) {}
        }

        fun launchRequestNotificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        suspend fun launchEntriesActivity() {
            hatenaRepo.withClient {
                entriesActivityLauncher.launch(Unit)
            }
        }
    }
}

// ------ //

class FakeSplashViewModel() : SplashViewModel {
    override val versionName: String = "x.x.x"

    override suspend fun onStart(context: Context) {
    }
}

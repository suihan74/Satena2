package com.suihan74.satena2.scene.preferences.page.info

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import com.suihan74.satena2.R
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

interface InformationViewModel : IPreferencePageViewModel {
    /**
     * アプリのバージョン
     */
    val appVersionName : String

    val releaseNotes : StateFlow<List<ReleaseNote>>

    val releaseNotesV1 : StateFlow<List<ReleaseNote>>

    // ------ //

    suspend fun loadReleaseNotes()

    suspend fun loadReleaseNotesV1()

    // ------ //

    /**
     * ストアを開く
     */
    fun launchPlayStore(context: Context)

    /**
     * 開発者はてブページを開く
     */
    fun launchDeveloperHatenaPage(context: Context)

    /**
     * 開発者Webサイトを開く
     */
    fun launchDeveloperWebsite(context: Context)

    /**
     * 開発者Twitterを開く
     */
    fun launchDeveloperTwitter(context: Context)

    /**
     * 開発者にメールする
     */
    fun sendMailToDeveloper(context: Context)

    /**
     * はてな利用規約を開く
     */
    fun launchHatenaTerms(context: Context)

    /**
     * プライバシーポリシーを開く
     */
    fun launchPrivacyPolicy(context: Context)

    /**
     * ライセンス一覧画面を開く
     */
    fun launchLicenseActivity(context: Context)
}

// ------ //

@HiltViewModel
class InformationViewModelImpl @Inject constructor(
    private val releaseNotesRepository: ReleaseNotesRepository,
    dataStore: DataStore<Preferences>
) : InformationViewModel, PreferencePageViewModel(dataStore) {
    override val appVersionName : String
        get() = application.versionName

    override val releaseNotes = releaseNotesRepository.releaseNotes
    override val releaseNotesV1 = releaseNotesRepository.releaseNotesV1

    private val releaseNotesLoadingMutex = Mutex()

    // ------ //

    override suspend fun loadReleaseNotes() {
        releaseNotesLoadingMutex.withLock {
            runCatching {
                releaseNotesRepository.loadReleaseNotes()
            }
        }
    }

    override suspend fun loadReleaseNotesV1() {
        releaseNotesLoadingMutex.withLock {
            runCatching {
                releaseNotesRepository.loadReleaseNotesV1()
            }
        }
    }

    // ------ //

    override fun launchPlayStore(context: Context) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(context.getString(R.string.play_store))
                `package` = "com.android.vending"
            }
            context.startActivity(intent)
        }.onFailure {
            /* TODO: エラーメッセージ */
        }
    }

    override fun launchDeveloperHatenaPage(context: Context) {
        openUrl(context, R.string.developer_hatena)
    }

    override fun launchDeveloperWebsite(context: Context) {
        openUrl(context, R.string.developer_website)
    }

    override fun launchDeveloperTwitter(context: Context) {
        openUrl(context, R.string.developer_twitter)
    }

    override fun sendMailToDeveloper(context: Context) {
        runCatching {
            val address = context.getString(R.string.developer_email)
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).also {
                it.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            }
            context.startActivity(intent)
        }.onFailure {
            /* TODO: エラーメッセージ */
        }
    }

    override fun launchHatenaTerms(context: Context) {
        openUrl(context, R.string.hatena_rule)
    }

    override fun launchPrivacyPolicy(context: Context) {
        openUrl(context, R.string.privacy_policy)
    }

    override fun launchLicenseActivity(context: Context) {
        runCatching {
        }.onFailure {
            /* TODO: エラーメッセージ */
        }
    }

    // ------ //

    /**
     * URLを開く
     */
    private fun openUrl(context: Context, @StringRes urlTextId: Int) {
        runCatching {
            val url = context.getString(urlTextId)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }.onFailure {
            /* TODO: エラーメッセージ */
        }
    }
}

// ------ //

/**
 * Preview用のダミーViewModel
 */
class FakeInformationViewModel(
    override val appVersionName : String = "2.X.X - compose preview",
    override val releaseNotes : StateFlow<List<ReleaseNote>> = MutableStateFlow(emptyList()),
    override val releaseNotesV1 : StateFlow<List<ReleaseNote>> = MutableStateFlow(emptyList())
) :
    InformationViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override suspend fun loadReleaseNotes() {}
    override suspend fun loadReleaseNotesV1() {}

    override fun launchPlayStore(context: Context) {}

    override fun launchDeveloperHatenaPage(context: Context) {}

    override fun launchDeveloperWebsite(context: Context) {}

    override fun launchDeveloperTwitter(context: Context) {}

    override fun sendMailToDeveloper(context: Context) {}

    override fun launchHatenaTerms(context: Context) {}

    override fun launchPrivacyPolicy(context: Context) {}

    override fun launchLicenseActivity(context: Context) {}
}

package com.suihan74.satena2.scene.preferences.page.info

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.PrefItem
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.scene.preferences.page.BasicPreferencesPage
import com.suihan74.satena2.scene.preferences.page.MutableComposableList
import com.suihan74.satena2.scene.preferences.page.buildComposableList
import com.suihan74.satena2.scene.preferences.page.info.dialog.ReleaseNotesDialog
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.copyrightYearStr
import com.suihan74.satena2.utility.extension.add
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 「情報」ページのコンテンツ
 */
@Composable
fun informationPageContents(viewModel: InformationViewModel) = buildComposableList {
    val context = LocalContext.current
    appInfoSection(context, viewModel)
    developerSection(context, viewModel)
    otherInfoSection(context, viewModel)
}

private fun MutableComposableList.appInfoSection(
    context: Context,
    viewModel: InformationViewModel
) = this.run {
    add(
        0 to { Section(R.string.pref_information_section_app_info) },
        R.string.pref_information_section_app_info to{ AppInformation(viewModel) },
        R.string.pref_information_play_store to { PrefButton(R.string.pref_information_play_store) { viewModel.launchPlayStore(context) } },
        R.string.pref_information_release_notes to {
            val dialogVisible = remember { mutableStateOf(false) }
            PrefButton(R.string.pref_information_release_notes) { dialogVisible.value = true }

            if (dialogVisible.value) {
                ReleaseNotesDialog(
                    viewModel = viewModel,
                    onDismissRequest = { dialogVisible.value = false },
                    properties = viewModel.dialogProperties()
                )
            }
        },
    )
}

private fun MutableComposableList.developerSection(
    context: Context,
    viewModel: InformationViewModel
) = this.run {
    add(
        0 to { Section(R.string.pref_information_section_developer) },
        R.string.developer to { PrefButton(R.string.developer) { viewModel.launchDeveloperHatenaPage(context) } },
        R.string.pref_information_website to { PrefButton(R.string.pref_information_website) { viewModel.launchDeveloperWebsite(context) } },
        R.string.pref_information_twitter to { PrefButton(R.string.pref_information_twitter) { viewModel.launchDeveloperTwitter(context) } },
        R.string.pref_information_mail to { PrefButton(R.string.pref_information_mail) { viewModel.sendMailToDeveloper(context) } },
    )
}

private fun MutableComposableList.otherInfoSection(
    context: Context,
    viewModel: InformationViewModel
) = this.run {
    add(
        0 to { Section(R.string.pref_information_section_other_info) },
        R.string.pref_information_hatena_terms to { PrefButton(R.string.pref_information_hatena_terms) { viewModel.launchHatenaTerms(context) } },
        R.string.pref_information_privacy_policy to { PrefButton(R.string.pref_information_privacy_policy) { viewModel.launchPrivacyPolicy(context) } },
        R.string.pref_information_license to { PrefButton(R.string.pref_information_license) { viewModel.launchLicenseActivity(context) } },
    )
}

// ------ //

@Preview
@Composable
private fun InformationPagePreview() {
    val releaseNotes = MutableStateFlow(
        listOf(
            ReleaseNote(version = "v2.0.1", body = "・Hello", timestamp = "2022-12-31"),
            ReleaseNote(version = "v2.0.0", body = "・World", timestamp = "2022-08-01")
        )
    )
    val releaseNotesV1 = MutableStateFlow(
        listOf(
            ReleaseNote(version = "v1.0.1", body = "・Hello", timestamp = ""),
            ReleaseNote(version = "v1.0.0", body = "・World", timestamp = "")
        )
    )

    BasicPreferencesPage(
        contents = informationPageContents(
            viewModel = FakeInformationViewModel(
                releaseNotes = releaseNotes,
                releaseNotesV1 = releaseNotesV1
            )
        )
    )
}

// ------ //

/**
 * アプリ情報タイル
 */
@Composable
private fun AppInformation(viewModel: InformationViewModel) {
    PrefItem(
        verticalPadding = 8.dp,
    ) {
        Row {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "app icon",
                modifier = Modifier.size(64.dp)
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 20.sp,
                    color = CurrentTheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "version: ${viewModel.appVersionName}",
                    fontSize = 16.sp,
                    color = CurrentTheme.onBackground
                )
                Text(
                    text = stringResource(
                        id = R.string.copyright,
                        copyrightYearStr()
                    ),
                    fontSize = 12.sp,
                    color = CurrentTheme.grayTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

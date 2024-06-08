package com.suihan74.satena2.scene.preferences.page.accounts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.model.mastodon.TootVisibility
import com.suihan74.satena2.model.misskey.NoteVisibility
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.PrefItem
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.scene.preferences.page.BasicPreferencesPage
import com.suihan74.satena2.scene.preferences.page.ComposablePrefItem
import com.suihan74.satena2.scene.preferences.page.MutableComposableList
import com.suihan74.satena2.scene.preferences.page.buildComposableList
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.utility.extension.add

/**
 * 「アカウント」ページ
 */
@Composable
fun AccountsPage(
    state: LazyListState = LazyListState(),
    contents: List<ComposablePrefItem>,
    onReload: ()->Unit
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            items(contents) {
                it.second.invoke()
            }
            emptyFooter()
        }

        // リロードボタン
        FloatingActionButton(
            backgroundColor = CurrentTheme.primary,
            contentColor = CurrentTheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp),
            onClick = onReload
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "reload"
            )
        }
    }
}

/**
 * 「アカウント」ページのコンテンツ
 *
 * TODO: サインイン済みの場合の`AccountItem`に渡す情報
 */
@Composable
fun accountPageContents(viewModel: AccountViewModel) = buildComposableList {
    hatenaSection(viewModel)
    mastodonSection(viewModel)
    misskeySection(viewModel)
}

/**
 * はてなセクション
 */
@Composable
private fun MutableComposableList.hatenaSection(viewModel: AccountViewModel) = add(
    0 to { Section(R.string.pref_account_section_hatena) },
    R.string.pref_account_section_hatena to {
        val context = LocalContext.current
        val state by viewModel.signedInHatena.collectAsState()
        when (state) {
            SignInState.None -> {
                PrefButton(R.string.sign_in) {
                    viewModel.launchHatenaAuthorizationActivity(context)
                }
            }

            SignInState.SignedIn -> {
                val account = viewModel.hatenaAccount.collectAsState()
                val userName = account.value?.name.orEmpty()
                val iconUrl = account.value?.name?.let {
                    "https://cdn1.www.st-hatena.com/users/$it/profile.gif"
                }
                AccountItem(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(iconUrl)
                            .error(R.drawable.ic_file)
                            .build()
                    ),
                    text = userName,
                    onClick = { viewModel.launchHatenaAuthorizationActivity(context) },
                    onClear = { viewModel.signOutHatena() }
                )
            }

            SignInState.Signing -> {
                Column(Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        color = CurrentTheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
)

/**
 * マストドンセクション
 */
@Composable
private fun MutableComposableList.mastodonSection(viewModel: AccountViewModel) = run {
    val context = LocalContext.current

    add(0 to { Section(R.string.pref_account_section_mastodon) })
    if (viewModel.signedInMastodon.collectAsState().value) {
        add(
            R.string.pref_account_section_mastodon to {
                val account = viewModel.mastodonAccount.collectAsState().value!!
                val instance = viewModel.mastodonInstance.collectAsState().value
                AccountItem(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(account.avatar)
                            .error(R.drawable.ic_file)
                            .build()
                    ),
                    text = "${account.userName}@$instance",
                    onClick = { viewModel.launchMastodonAuthorizationActivity(context) },
                    onClear = { viewModel.signOutMastodon() }
                )
            },
            R.string.pref_account_mastodon_visibility to {
                val menuVisible = remember { mutableStateOf(false) }
                val current by viewModel.mastodonPostVisibility.collectAsState()
                PrefButton(
                    mainTextId = R.string.pref_account_mastodon_visibility,
                    subTextPrefixId = R.string.pref_current_value_prefix,
                    subTextId = current.textId
                ) {
                    menuVisible.value = true
                }

                if (menuVisible.value) {
                    MenuDialog(
                        colors = themedCustomDialogColors(),
                        titleText = stringResource(R.string.pref_account_mastodon_visibility),
                        menuItems = buildList {
                            addAll(
                                TootVisibility.entries.map {
                                    menuDialogItem(textId = it.textId) {
                                        viewModel.updateMastodonPostVisibility(it)
                                        true
                                    }
                                }
                            )
                        },
                        negativeButton = dialogButton(R.string.cancel) { menuVisible.value = false },
                        onDismissRequest = { menuVisible.value = false },
                        properties = viewModel.dialogProperties()
                    )
                }
            }
        )
    }
    else {
        add(
            R.string.pref_account_section_mastodon to { PrefButton(R.string.sign_in) { viewModel.launchMastodonAuthorizationActivity(context) } }
        )
    }
}

/**
 * Misskeyセクション
 */
@Composable
private fun MutableComposableList.misskeySection(viewModel: AccountViewModel) = run {
    val context = LocalContext.current

    add(0 to { Section(R.string.pref_account_section_misskey) })
    if (viewModel.signedInMisskey.collectAsState(initial = false).value) {
        add(
            R.string.pref_account_section_misskey to {
                val instance by viewModel.misskeyInstance.collectAsState()
                val account by viewModel.misskeyAccount.collectAsState()
                AccountItem(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(account?.avatarUrl)
                            .error(R.drawable.ic_file)
                            .build()
                    ),
                    text = account?.let { "${it.username}@${instance}" } ?: "アカウント情報取得失敗@${instance}",
                    onClick = { viewModel.launchMisskeyAuthorizationActivity(context) },
                    onClear = { viewModel.signOutMisskey() }
                )
            },
            R.string.pref_account_misskey_visibility to {
                val menuVisible = remember { mutableStateOf(false) }
                val current by viewModel.misskeyPostVisibility.collectAsState()
                PrefButton(
                    mainTextId = R.string.pref_account_misskey_visibility,
                    subTextPrefixId = R.string.pref_current_value_prefix,
                    subTextId = current.textId
                ) {
                    menuVisible.value = true
                }

                if (menuVisible.value) {
                    MenuDialog(
                        colors = themedCustomDialogColors(),
                        titleText = stringResource(R.string.pref_account_misskey_visibility),
                        menuItems = buildList {
                            addAll(
                                NoteVisibility.entries.map {
                                    menuDialogItem(textId = it.textId) {
                                        viewModel.updateMisskeyPostVisibility(it)
                                        true
                                    }
                                }
                            )
                        },
                        negativeButton = dialogButton(R.string.cancel) { menuVisible.value = false },
                        onDismissRequest = { menuVisible.value = false },
                        properties = viewModel.dialogProperties()
                    )
                }
            }
        )
    }
    else {
        add(
            R.string.pref_account_section_misskey to { PrefButton(R.string.sign_in) { viewModel.launchMisskeyAuthorizationActivity(context) } }
        )
    }
}

// ------ //

@Preview
@Composable
private fun AccountPagePreview() {
    Box(
        Modifier.background(CurrentTheme.background)
    ) {
        BasicPreferencesPage(
            contents = accountPageContents(FakeAccountViewModel(signedInHatena = false))
        )
    }
}

// ------ //

@Composable
private fun AccountItem(
    painter: Painter,
    text: String,
    onClear: ()->Unit = {},
    onClick: ()->Unit = {},
) {
    PrefItem(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painter,
                contentDescription = "account icon",
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = text,
                fontSize = 16.sp,
                color = CurrentTheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            IconButton(onClick = onClear) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "remove account button",
                    tint = CurrentTheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun AccountItemPreview() {
    Box(
        Modifier.background(CurrentTheme.background)
    ) {
        AccountItem(
            painter = painterResource(id = R.mipmap.ic_launcher),
            text = "suihan74"
        )
    }
}

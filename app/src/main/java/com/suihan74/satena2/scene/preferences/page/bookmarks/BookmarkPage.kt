package com.suihan74.satena2.scene.preferences.page.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.scene.bookmarks.BookmarksTab
import com.suihan74.satena2.scene.bookmarks.OpenCommentLinkTrigger
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.PrefToggleButton
import com.suihan74.satena2.scene.preferences.Section
import com.suihan74.satena2.scene.preferences.page.MutableComposableList
import com.suihan74.satena2.scene.preferences.page.buildComposableList
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.utility.extension.add
import com.suihan74.satena2.utility.extension.textId

/**
 * 「ブックマーク」ページのコンテンツ
 */
@Composable
fun bookmarkPageContents(viewModel: BookmarkViewModel) = buildComposableList {
    postSection(viewModel)
    tabsSection(viewModel)
    titleBarSection()
    behaviorSection()
    muteSection()
    linkSection(viewModel)
    digestSection()
}

private fun MutableComposableList.postSection(viewModel: BookmarkViewModel) = add(
    0 to { Section(R.string.pref_bookmark_section_post) },
    R.string.pref_post_bookmark_confirmation to{
        PrefToggleButton(
            flow = viewModel.postConfirmation,
            mainTextId = R.string.pref_post_bookmark_confirmation
        )
    },
    R.string.pref_post_bookmark_dialog_vertical_alignment to {
        val dialogVisible = remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_post_bookmark_dialog_vertical_alignment,
            subTextId = viewModel.postDialogVerticalAlignment.value.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible.value = true
        }

        if (dialogVisible.value) {
            val items = listOf(
                Alignment.Top,
                Alignment.CenterVertically,
                Alignment.Bottom
            )
            MenuDialog(
                titleText = stringResource(R.string.pref_post_bookmark_dialog_vertical_alignment),
                menuItems = items.map { item ->
                    menuDialogItem(item.textId) {
                        viewModel.postDialogVerticalAlignment.value = item
                        true
                    }
                },
                negativeButton = dialogButton(R.string.cancel) { dialogVisible.value = false },
                onDismissRequest = { dialogVisible.value = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    R.string.pref_post_bookmark_save_states to {
        PrefToggleButton(
            flow = viewModel.postSaveStates,
            mainTextId = R.string.pref_post_bookmark_save_states
        )
    }
)

private fun MutableComposableList.tabsSection(viewModel: BookmarkViewModel) = add(
    0 to { Section(R.string.pref_bookmark_section_tabs) },
    R.string.pref_bookmark_initial_tab to {
        val dialogVisible = remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_bookmark_initial_tab,
            subTextId = viewModel.initialTab.collectAsState().value.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible.value = true
        }

        if (dialogVisible.value) {
            MenuDialog(
                titleText = stringResource(R.string.pref_bookmark_initial_tab),
                menuItems = BookmarksTab.entries.map { tab ->
                    menuDialogItem(tab.textId) {
                        viewModel.initialTab.value = tab
                        true
                    }
                },
                negativeButton = dialogButton(R.string.cancel) { dialogVisible.value = false },
                onDismissRequest = { dialogVisible.value = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    },
    R.string.pref_bookmark_change_initial_tab_by_long_click to {
        PrefToggleButton(
            flow = viewModel.changeInitialTabByLongClick,
            mainTextId = R.string.pref_bookmark_change_initial_tab_by_long_click
        )
    }
)

private fun MutableComposableList.titleBarSection() = add(
    0 to { Section(R.string.pref_bookmark_section_title_bar) }
)

private fun MutableComposableList.behaviorSection() = add(
    0 to { Section(R.string.pref_bookmark_section_behavior) }
)

private fun MutableComposableList.muteSection() = add(
    0 to { Section(R.string.pref_bookmark_section_mute) }
)

private fun MutableComposableList.linkSection(viewModel: BookmarkViewModel) = add(
    0 to { Section(R.string.pref_bookmark_section_link) },
    R.string.pref_bookmark_open_in_browser_on_click_url to {
        val dialogVisible = remember { mutableStateOf(false) }
        PrefButton(
            mainTextId = R.string.pref_bookmark_open_in_browser_on_click_url,
            subTextId = viewModel.openCommentLinkTrigger.collectAsState().value.textId,
            subTextPrefixId = R.string.pref_current_value_prefix
        ) {
            dialogVisible.value = true
        }

        if (dialogVisible.value) {
            MenuDialog(
                titleText = stringResource(R.string.pref_bookmark_open_in_browser_on_click_url),
                menuItems = OpenCommentLinkTrigger.entries.map { item ->
                    menuDialogItem(item.textId) {
                        viewModel.openCommentLinkTrigger.value = item
                        true
                    }
                },
                negativeButton = dialogButton(R.string.cancel) { dialogVisible.value = false },
                onDismissRequest = { dialogVisible.value = false },
                colors = themedCustomDialogColors(),
                properties = viewModel.dialogProperties()
            )
        }
    }
)

private fun MutableComposableList.digestSection() = add(
    0 to { Section(R.string.pref_bookmark_section_digest) }
)

package com.suihan74.satena2.scene.bookmarks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuToggleItem
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun CustomTabSettingContent(
    initialSetting: CustomTabSetting,
    allLabels: List<Label>,
    onRegister: (CustomTabSetting)->Unit
) {
    var areNoLabelsShown by remember { mutableStateOf(initialSetting.areNoLabelsShown) }
    var areNoCommentsShown by remember { mutableStateOf(initialSetting.areNoCommentsShown) }
    var areIgnoresShown by remember { mutableStateOf(initialSetting.areIgnoresShown) }
    var areUrlOnlyCommentsShown by remember { mutableStateOf(initialSetting.areUrlOnlyCommentsShown) }
    var labels by remember {
        mutableStateOf(
            allLabels.map { label ->
                label to initialSetting.enableLabelIds.contains(label.id)
            }
        )
    }

    Box {
        Column(
            Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.bookmark_custom_tab_setting_title),
                color = CurrentTheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                Modifier.verticalScrollbar(
                    state = rememberLazyListState(),
                    color = CurrentTheme.primary
                )
            ) {
                item {
                    BottomSheetMenuToggleItem(
                        text = stringResource(id = R.string.bookmark_custom_tab_setting_no_labels_users_shown),
                        value = areNoLabelsShown
                    ) {
                        areNoLabelsShown = !areNoLabelsShown
                    }
                }
                item {
                    BottomSheetMenuToggleItem(
                        text = stringResource(id = R.string.bookmark_custom_tab_setting_no_comments_shown),
                        value = areNoCommentsShown
                    ) {
                        areNoCommentsShown = !areNoCommentsShown
                    }
                }
                item {
                    BottomSheetMenuToggleItem(
                        text = stringResource(id = R.string.bookmark_custom_tab_setting_url_only_comments_shown),
                        value = areUrlOnlyCommentsShown
                    ) {
                        areUrlOnlyCommentsShown = !areUrlOnlyCommentsShown
                    }
                }
                item {
                    BottomSheetMenuToggleItem(
                        text = stringResource(id = R.string.bookmark_custom_tab_setting_ignored_users_shown),
                        value = areIgnoresShown
                    ) {
                        areIgnoresShown = !areIgnoresShown
                    }
                }

                items(labels) { item ->
                    BottomSheetMenuToggleItem(
                        text = item.first.name,
                        value = item.second
                    ) {
                        labels = labels.map {
                            if (it == item) item.first to !item.second
                            else it
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            FloatingActionButton(
                backgroundColor = CurrentTheme.primary,
                contentColor = CurrentTheme.onPrimary,
                onClick = {
                    onRegister(
                        CustomTabSetting(
                            enableLabelIds =
                                labels
                                    .filter { it.second }
                                    .map { it.first.id }
                                    .toSet(),
                            areNoLabelsShown = areNoLabelsShown,
                            areNoCommentsShown = areNoCommentsShown,
                            areIgnoresShown = areIgnoresShown,
                            areUrlOnlyCommentsShown = areUrlOnlyCommentsShown
                        )
                    )
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = "save",
                    colorFilter = ColorFilter.tint(CurrentTheme.onPrimary)
                )
            }
        }
    }
}

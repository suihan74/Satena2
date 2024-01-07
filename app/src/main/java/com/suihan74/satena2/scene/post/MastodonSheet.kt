package com.suihan74.satena2.scene.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.MultiToggleButton
import com.suihan74.satena2.compose.Tooltip
import com.suihan74.satena2.model.mastodon.TootVisibility
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedMultiToggleButtonColors
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun MastodonSheet(viewModel: BookmarkPostViewModel) {
    val tootVisibility by viewModel.mastodonPostVisibility.collectAsState()
    val selectedIndex = remember {
        mutableIntStateOf(TootVisibility.values().indexOf(tootVisibility))
    }
    val tooltipVisibilities = remember {
        TootVisibility.values().map { mutableStateOf(false) }
    }
    val spoilerTextFieldValue = rememberMutableTextFieldValue(
        text = viewModel.mastodonSpoilerText.collectAsState().value
    )
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.mastodonSpoilerText
            .onEach {
                spoilerTextFieldValue.value = spoilerTextFieldValue.value.copy(text = it)
            }
            .launchIn(this)

        snapshotFlow { spoilerTextFieldValue.value.text }
            .onEach {
                viewModel.mastodonSpoilerText.value = it
            }
            .launchIn(this)
    }

    Column(
        Modifier
            .background(
                color = CurrentTheme.background,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            .fillMaxWidth()
            .height(154.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.mastodon_setting_title),
            color = CurrentTheme.drawerOnBackground,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.mastodon_setting_post_visibility),
            color = CurrentTheme.drawerOnBackground,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        MultiToggleButton(
            contents = TootVisibility.values().mapIndexed { idx, v ->
                {
                    val tooltipVisible = tooltipVisibilities[idx]
                    val label = stringResource(id = v.textId)
                    Box {
                        Icon(
                            painterResource(v.iconId),
                            contentDescription = label,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(20.dp)
                        )
                        Tooltip(expanded = tooltipVisible) {
                            Text(label)
                        }
                    }
                }
            },
            selectedIndex = selectedIndex,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp, horizontal = 6.dp),
            colors = themedMultiToggleButtonColors(),
            onLongClick = {
                tooltipVisibilities[it].value = true
            },
            onToggleChange = {
                viewModel.setMastodonPostVisibility(TootVisibility.values()[it])
            }
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = spoilerTextFieldValue.value,
            onValueChange = { spoilerTextFieldValue.value = it },
            maxLines = 1,
            singleLine = true,
            label = { Text(stringResource(R.string.mastodon_setting_spoiler_text_label)) },
            placeholder = { Text(stringResource(R.string.mastodon_setting_spoiler_text_placeholder)) },
            colors = TextFieldDefaults.textFieldColors(
                textColor = CurrentTheme.drawerOnBackground,
                placeholderColor = CurrentTheme.grayTextColor,
                cursorColor = CurrentTheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                trailingIconColor = CurrentTheme.primary,
                unfocusedLabelColor = CurrentTheme.grayTextColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .focusRequester(focusRequester)
        )
    }
}

// ------ //

@Preview
@Composable
private fun MastodonSheetPreview() {
    val viewModel = FakeBookmarkPostViewModel()
    Column(
        Modifier
            .fillMaxWidth()
            .background(CurrentTheme.drawerBackground)
    ) {
        MastodonSheet(viewModel)
    }
}

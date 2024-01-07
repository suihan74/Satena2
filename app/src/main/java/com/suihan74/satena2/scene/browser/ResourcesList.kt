package com.suihan74.satena2.scene.browser

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.CombinedIconButton
import com.suihan74.satena2.compose.verticalScrollbar
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.trimScheme
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import kotlinx.coroutines.launch

/**
 * URLテキストフィールドの初期選択範囲を作成する
 *
 * deleteキーいっぱつで選択範囲を一括で消せるようにするための処置
 *
 * TwitterやNoteの場合は"twitter.com/userName"より後の部分にする
 */
private fun makeUrlInitialSelection(query: String) : TextRange {
    val domains = listOf(
        "twitter.com/", "note.com/"
    )
    val separatorIdx =
        domains.firstOrNull { query.startsWith(it) }
            ?.let { query.indexOf('/', startIndex = it.length) }
            ?: query.indexOf('/')

    return if (separatorIdx < 0) TextRange(0, query.length)
    else TextRange(separatorIdx + if (separatorIdx == query.lastIndex) 0 else 1, query.length)
}

@Composable
fun ResourcesList(
    items: List<ResourceUrl>,
    onInsertBlockedResource: (String)->Unit
) {
    val lazyListState = rememberLazyListState()

    val textFieldValue = rememberMutableTextFieldValue()
    val focusRequester = remember { FocusRequester() }
    var keepSelection by remember { mutableStateOf(false) }
    if (keepSelection) {
        SideEffect {
            keepSelection = false
        }
    }

    Column(
        Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.browser_resources_title),
            fontSize = 18.sp,
            color = CurrentTheme.drawerOnBackground,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = textFieldValue.value,
                onValueChange = {
                    if (keepSelection) {
                        keepSelection = false
                        val selection = makeUrlInitialSelection(textFieldValue.value.text)
                        textFieldValue.value = textFieldValue.value.copy(
                            selection = selection
                        )
                    }
                    else {
                        textFieldValue.value = it
                    }
                },
                maxLines = 1,
                singleLine = true,
                label = { Text(stringResource(R.string.browser_block_resource_label)) },
                placeholder = { Text(stringResource(R.string.browser_block_resource_placeholder)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = CurrentTheme.primary,
                    placeholderColor = CurrentTheme.grayTextColor,
                    cursorColor = CurrentTheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    trailingIconColor = CurrentTheme.primary,
                    focusedLabelColor = CurrentTheme.grayTextColor,
                    unfocusedLabelColor = CurrentTheme.grayTextColor,
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            val selection = makeUrlInitialSelection(textFieldValue.value.text)
                            textFieldValue.value = textFieldValue.value.copy(
                                selection = selection
                            )
                            keepSelection = true
                        }
                    }
            )
            CombinedIconButton(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .align(Alignment.CenterVertically),
                onClick = {
                    onInsertBlockedResource(Uri.decode(textFieldValue.value.text))
                    textFieldValue.value = textFieldValue.value.copy(text = "")
                },
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "add blocking url",
                    tint = CurrentTheme.primary
                )
            }
        }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .verticalScrollbar(
                    state = lazyListState,
                    color = CurrentTheme.primary
                )
        ) {
            items(items) { item ->
                ResourcesListItem(
                    text = Uri.decode(item.url),
                    blocked = item.blocked,
                    onClick = {
                        val text = Uri.decode(item.url).trimScheme()
                        textFieldValue.value = textFieldValue.value.copy(text = text)
                        focusRequester.requestFocus()
                    }
                )
            }
        }
    }
}

// ------ //

@Composable
private fun ResourcesListItem(
    text: String,
    blocked: Boolean,
    onClick: suspend ()->Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Text(
        text = text.trimScheme(),
        fontSize = 14.sp,
        color =
            if (blocked) CurrentTheme.drawerOnBackground.copy(alpha = .5f)
            else CurrentTheme.drawerOnBackground,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clickable { coroutineScope.launch { onClick() } }
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp)
    )
}

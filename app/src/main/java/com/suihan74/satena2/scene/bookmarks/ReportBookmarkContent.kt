package com.suihan74.satena2.scene.bookmarks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.hatena.model.bookmark.Report
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * ブクマを通報するダイアログの内容
 */
@Composable
fun ReportBookmarkContent(
    entry: Entry,
    item: DisplayBookmark?,
    onReport: (Report)->Unit = {}
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf(Report.Reason.SPAM) }

    var textFieldValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardOptions = remember { KeyboardOptions.Default }

    if (item == null) {
        Box(Modifier.fillMaxHeight())
        return
    }

    Box {
        Column(
            Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.report_bookmark_title),
                color = CurrentTheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(6.dp))
            BookmarkItem(item = item, clickable = false)
            Text(
                text = stringResource(R.string.report_bookmark_reason_desc),
                color = CurrentTheme.onBackground,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            // 通報理由
            Text(
                text = selectedReason.name,
                color = CurrentTheme.onBackground,
                modifier = Modifier
                    .clickable { dropdownExpanded = true }
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            )
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                for (r in Report.Reason.entries) {
                    DropdownMenuItem(
                        onClick = {
                            selectedReason = r
                            dropdownExpanded = false
                        }
                    ) {
                        Text(
                            text = r.name,
                            color = CurrentTheme.onBackground
                        )
                    }
                }
            }
            // コメント
            Text(
                text = stringResource(R.string.report_bookmark_comment_desc),
                color = CurrentTheme.onBackground,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                placeholder = { Text(stringResource(id = R.string.report_bookmark_comment_placeholder)) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = CurrentTheme.onBackground,
                    placeholderColor = CurrentTheme.grayTextColor,
                    cursorColor = CurrentTheme.onBackground,
                    focusedIndicatorColor = CurrentTheme.primary,
                ),
                isError = false,
                keyboardOptions = keyboardOptions,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .height(200.dp)
            )
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
                    onReport(
                        Report(
                            entry = entry,
                            bookmark = item.bookmark,
                            reason = selectedReason,
                            comment = textFieldValue
                        )
                    )
                }
            ) {
                Image(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "report",
                    colorFilter = ColorFilter.tint(CurrentTheme.onPrimary)
                )
            }
        }
    }
}

package com.suihan74.satena2.scene.preferences.page.userLabel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuItem
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCheckboxColors

/**
 * ユーザーラベル設定ダイアログのコンテンツ
 */
@Composable
fun UserLabelDialog(
    labels: List<Label>,
    checkedLabels: List<Label>,
    dialogProperties: DialogProperties,
    onCreateLabel: suspend (Label)->Boolean,
    onUpdate: (List<Pair<Label,Boolean>>)->Unit
) {
    val pairs = remember(labels, checkedLabels) {
        labels.map { label ->
            label to mutableStateOf(checkedLabels.any { it == label })
        }
    }
    var editorDialogTarget by remember { mutableStateOf<Label?>(null) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = stringResource(R.string.bookmark_menu_manage_user_labels),
                fontSize = 18.sp,
                color = CurrentTheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            LazyColumn {
                items(pairs) { item ->
                    UserLabelDialogItem(
                        label = item.first,
                        checked = item.second
                    )
                }
                item {
                    BottomSheetMenuItem(
                        icon = rememberVectorPainter(image = Icons.Default.Add),
                        text = stringResource(R.string.user_label_dialog_add_new_label),
                        onClick = {
                            editorDialogTarget = Label(name = "")
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(vertical = 16.dp, horizontal = 16.dp),
            backgroundColor = CurrentTheme.primary,
            contentColor = CurrentTheme.onPrimary,
            onClick = {
                val states = pairs.map { it.first to it.second.value }
                onUpdate(states)
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "register",
                colorFilter = ColorFilter.tint(CurrentTheme.onPrimary)
            )
        }
    }

    editorDialogTarget?.let { label ->
        UserLabelNameEditionDialog(
            label = label,
            onRegistration = onCreateLabel,
            onDismiss = { editorDialogTarget = null },
            dialogProperties = dialogProperties
        )
    }
}

// ------ //

@Composable
private fun UserLabelDialogItem(
    label: Label,
    checked: MutableState<Boolean>
) {
    Row(
        Modifier
            .clickable {
                checked.value = !checked.value
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked.value,
            onCheckedChange = { checked.value = it },
            colors = themedCheckboxColors(),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        SingleLineText(
            text = label.name,
            color = CurrentTheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        )
    }
}

// ------ //

@Preview
@Composable
private fun UserLabelDialogItemPreview() {
    Box(Modifier.background(CurrentTheme.background)) {
        UserLabelDialogItem(
            label = Label(name = "ユーザーラベル"),
            checked = remember { mutableStateOf(false) }
        )
    }
}

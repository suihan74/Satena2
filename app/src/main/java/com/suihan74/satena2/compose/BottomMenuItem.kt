package com.suihan74.satena2.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.suihan74.satena2.scene.entries.BottomMenu
import com.suihan74.satena2.scene.entries.Category
import com.suihan74.satena2.scene.entries.FakeEntriesViewModel
import com.suihan74.satena2.ui.theme.CurrentTheme

@Composable
fun BottomMenuItemButton(
    @DrawableRes iconId: Int,
    @StringRes textId: Int,
    tint: Color = CurrentTheme.bottomBarOnBackground,
    tooltipVisible: MutableState<Boolean> = remember { mutableStateOf(false) },
    onClick: ()->Unit = {},
    onLongClick: ((MutableState<Boolean>)->Unit)? = null
) {
    val label = stringResource(id = textId)
    val onLongClickImpl = onLongClick ?: { tooltipVisible.value = true }
    Box {
        CombinedIconButton(
            modifier = Modifier.padding(horizontal = 12.dp),
            onClick = onClick,
            onLongClick = { onLongClickImpl(tooltipVisible) }
        ) {
            Icon(
                painterResource(id = iconId),
                contentDescription = label,
                tint = tint
            )
        }
        Tooltip(expanded = tooltipVisible) {
            Text(label)
        }
    }
}

@Preview
@Composable
private fun BottomMenuPreview() {
    val viewModel = FakeEntriesViewModel()
    val bottomMenuItems by viewModel.bottomMenuItems.collectAsState(initial = emptyList())
    val arrangement by viewModel.bottomMenuArrangement.collectAsState(Arrangement.Start)
    val density = LocalDensity.current
    BottomMenu(
        items = bottomMenuItems,
        itemsArrangement = arrangement,
        category = Category.All,
        signedIn = false,
        contentPaddingValues = PaddingValues(start = 0.dp, end = 88.dp),
        issues = emptyList()
    )
}

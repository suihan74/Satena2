package com.suihan74.satena2.scene.preferences.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.scene.preferences.PrefButton
import com.suihan74.satena2.scene.preferences.PrefToggleButton
import com.suihan74.satena2.scene.preferences.Section
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 通常の設定ページ用の共通UI
 */
@Composable
fun BasicPreferencesPage(
    state: LazyListState = LazyListState(),
    contents: List<@Composable ()->Unit>
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize()
    ) {
        items(contents) { composable ->
            composable.invoke()
        }
        emptyFooter()
    }
}

// ------ //

@Preview
@Composable
private fun BasicPreferencesPagePreview() {
    val contents = listOf<@Composable ()->Unit>(
        { Section(title = "セクション1") },
        { PrefButton(mainText = "ボタン") },
        { PrefToggleButton(mainText = "トグルボタン", flow = MutableStateFlow(true)) },
        { Section(title = "セクション2") },
        { PrefButton(mainText = "ボタン2") },
        { PrefToggleButton(mainText = "トグルボタン2", flow = MutableStateFlow(true)) },
    )

    BasicPreferencesPage(contents = contents)
}

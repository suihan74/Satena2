package com.suihan74.satena2.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * `LazyColumn`の下端に適当なスペースを挿入するやつ
 */
fun LazyListScope.emptyFooter(height: Dp = 80.dp) {
    this.item {
        Spacer(Modifier.height(height))
    }
}

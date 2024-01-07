package com.suihan74.satena2.scene.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * セクション
 */
@Composable
fun Section(@StringRes textId: Int) = Section(stringResource(textId))

/**
 * セクション
 */
@Composable
fun Section(title: String) {
    val sectionHorizontalPadding = 12.dp
    Row(
        Modifier
            .padding(
                top = 12.dp,
                start = sectionHorizontalPadding,
                end = sectionHorizontalPadding
            )
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            color = CurrentTheme.primary,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(end = sectionHorizontalPadding)
        )
        Box(
            Modifier
                .background(CurrentTheme.primary)
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@Preview
@Composable
private fun SectionPreview() {
    Section("セクション")
}

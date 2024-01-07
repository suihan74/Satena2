package com.suihan74.satena2.scene.browser.drawer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.bookmarks.BookmarksViewModel
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * ブラウザのドロワでブクマ画面を表示する前にブクマ情報を取得するか確認するための画面
 */
@Composable
fun BookmarksConfirmationContent(
    currentUrl: String,
    bookmarksViewModel: BookmarksViewModel,
    onConfirmed: ()->Unit
) {
    val url = remember { Uri.decode(currentUrl) }
    Box(
        Modifier
            .fillMaxSize()
            .background(CurrentTheme.drawerBackground)
            .padding(8.dp)
    ) {
        Text(
            text = stringResource(R.string.confirm),
            color = CurrentTheme.drawerOnBackground,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Text(
                text = url,
                color = CurrentTheme.drawerOnBackground,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            Button(
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary
                ),
                onClick = { onConfirmed() }
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

package com.suihan74.satena2.scene.preferences

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * Hatenaユーザーリスト項目
 */
@Composable
fun HatenaUserItem(
    username: String,
    iconUrl: String,
    onClick: ()->Unit = {},
    onLongClick: ()->Unit = {}
) {
    val context = LocalContext.current
    Row(
        Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .fillMaxWidth()
            .padding(
                vertical = PrefItemDefaults.listItemVerticalPadding,
                horizontal = PrefItemDefaults.listItemHorizontalPadding
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(iconUrl)
                .error(R.drawable.ic_file)
                .build(),
            contentDescription = "user icon",
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterVertically)
        )
        SingleLineText(
            text = username,
            fontSize = 14.sp,
            color = CurrentTheme.onBackground,
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

// ------ //

@Preview
@Composable
private fun HatenaUserItemPreview() {
    HatenaUserItem(username = "suihan74", iconUrl = "") {

    }
}

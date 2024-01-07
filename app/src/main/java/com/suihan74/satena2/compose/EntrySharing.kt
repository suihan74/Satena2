package com.suihan74.satena2.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import com.suihan74.hatena.model.bookmark.Bookmark
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.createIntentWithDefaultBrowser
import com.suihan74.satena2.utility.extension.showToast
import com.suihan74.satena2.utility.hatena.actualUrl
import kotlinx.coroutines.launch

/**
 * エントリを共有するダイアログのコンテンツ
 */
@Composable
fun EntrySharingContent(entry: Entry) {
    val rawActualUrl = remember(entry) { entry.actualUrl() }
    SharingContent(
        title = stringResource(R.string.entry_sharing_title),
        rawUrl = rawActualUrl,
        rawEntryUrl = entry.entryUrl,
        text = entry.title
    )
}

// ------ //

/**
 * ブクマを共有するダイアログのコンテンツ
 */
@Composable
fun BookmarkSharingContent(bookmark: Bookmark) {
    SharingContent(
        title = stringResource(R.string.bookmark_sharing_title),
        rawUrl = bookmark.link,
        rawEntryUrl = null,
        text = "${bookmark.user} : ${bookmark.comment}"
    )
}

// ------ //

/**
 * エントリを共有するダイアログのコンテンツ
 */
@Composable
fun SharingContent(title: String, rawUrl:String, rawEntryUrl: String?, text: String) {
    Column(
        Modifier.padding(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = CurrentTheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        SharingUrlMenu(
            rawUrl = rawUrl,
            rawEntryUrl = rawEntryUrl
        )
        Spacer(
            Modifier
                .padding(vertical = 8.dp)
                .background(CurrentTheme.grayTextColor)
                .fillMaxWidth()
                .height(1.dp)
        )
        SharingTextMenu(
            text = text,
            rawUrl = rawUrl
        )
    }
}

// ------ //

/**
 * URLの共有メニュー
 */
@Composable
private fun SharingUrlMenu(rawUrl: String, rawEntryUrl: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val labelTextUnit = 14.sp
    val url = remember(rawUrl) { Uri.decode(rawUrl) }
    val entryUrl = remember(rawEntryUrl) { rawEntryUrl?.let { Uri.decode(it) } }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(R.drawable.ic_link),
            contentDescription = "sharing url icon",
            colorFilter = ColorFilter.tint(CurrentTheme.onBackground),
            modifier = Modifier.size(15.dp)
        )
        MarqueeText(
            text = url,
            fontSize = labelTextUnit,
            color = CurrentTheme.onBackground,
            gradientColor = CurrentTheme.background,
            modifier = Modifier
                .weight(1f)
                .padding(start = 3.dp)
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.horizontalScroll(scrollState)
    ) {
        MenuItem(
            iconId = R.drawable.ic_content_copy,
            textId = R.string.copy_to_clipboard,
        ) {
            coroutineScope.launch {
                copyTextToClipboard(context, url)
            }
        }
        Spacer(Modifier.width(18.dp))
        MenuItem(
            iconId = R.drawable.ic_open_in_new,
            textId = R.string.open_with_other_apps,
        ) {
            openInOtherApp(context, url)
        }
        Spacer(Modifier.width(18.dp))
        /*
        MenuItem(
            iconId = R.drawable.ic_category_general,
            textId = R.string.open_with_web_browser,
        ) {
            openInBrowser(context, url)
        }
        Spacer(Modifier.width(18.dp))
        */
        MenuItem(
            iconId = R.drawable.ic_share,
            textId = R.string.share,
        ) {
            share(context, url)
        }
        if (entryUrl != null) {
            Spacer(Modifier.width(18.dp))
            Spacer(
                Modifier
                    .width(2.dp)
                    .height(72.dp)
                    .background(CurrentTheme.grayTextColor)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(18.dp))
            MenuItem(
                iconId = R.drawable.ic_content_copy,
                textId = R.string.copy_entry_url_to_clipboard,
            ) {
                coroutineScope.launch {
                    copyTextToClipboard(context, entryUrl)
                }
            }
            Spacer(Modifier.width(18.dp))
            MenuItem(
                iconId = R.drawable.ic_open_in_new,
                textId = R.string.open_entry_url_with_other_apps,
            ) {
                openInOtherApp(context, entryUrl)
            }
            Spacer(Modifier.width(18.dp))
            /*
            MenuItem(
                iconId = R.drawable.ic_category_general,
                textId = R.string.open_entry_url_with_web_browser,
            ) {
                openInBrowser(context, url)
            }
            Spacer(Modifier.width(18.dp))
             */
            MenuItem(
                iconId = R.drawable.ic_share,
                textId = R.string.share_entry_url,
            ) {
                share(context, entryUrl)
            }
        }
    }
}

// ------ //

/**
 * テキストの共有メニュー
 */
@Composable
private fun SharingTextMenu(text: String, rawUrl: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val labelTextUnit = 14.sp
    val body = remember(text, rawUrl) {
        if (rawUrl.isNullOrBlank()) text
        else "$text ${Uri.decode(rawUrl)}"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(R.drawable.ic_title),
            contentDescription = "sharing text icon",
            colorFilter = ColorFilter.tint(CurrentTheme.onBackground),
            modifier = Modifier.size(15.dp)
        )
        MarqueeText(
            text = body,
            fontSize = labelTextUnit,
            color = CurrentTheme.onBackground,
            gradientColor = CurrentTheme.background,
            modifier = Modifier
                .weight(1f)
                .padding(start = 3.dp)
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.horizontalScroll(scrollState)
    ) {
        MenuItem(
            iconId = R.drawable.ic_content_copy,
            textId = R.string.copy_to_clipboard,
        ) {
            coroutineScope.launch {
                copyTextToClipboard(context, body)
            }
        }
        Spacer(Modifier.width(18.dp))
        MenuItem(
            iconId = R.drawable.ic_share,
            textId = R.string.share,
        ) {
            share(context, body)
        }
    }
}

// ------ //

/**
 * 共有メニュー項目
 */
@Composable
private fun MenuItem(
    @DrawableRes iconId: Int,
    @StringRes textId: Int,
    onClick: ()->Unit
) {
    val text = stringResource(textId)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        FloatingActionButton(
            backgroundColor = CurrentTheme.primary,
            contentColor = CurrentTheme.onPrimary,
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = text
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 10.5.sp,
            color = CurrentTheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

// ------- //

/**
 * クリップボードにコピー
 */
private suspend fun copyTextToClipboard(context: Context, text: CharSequence) {
    runCatching {
        val cm = context.getSystemService<ClipboardManager>()!!
        cm.setPrimaryClip(ClipData.newPlainText("", text))
        context.showToast(R.string.msg_copy_to_clipboard)
    }.onFailure {
        context.showToast(R.string.msg_copy_to_clipboard_failure)
    }
}

/**
 * 他アプリで開く
 */
private fun openInOtherApp(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

/**
 * ブラウザで開く
 */
private fun openInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).createIntentWithDefaultBrowser(context)
    context.startActivity(intent)
}

/**
 * 共有
 */
private fun share(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).also {
        it.putExtra(Intent.EXTRA_TEXT, text)
        it.type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, text))
}

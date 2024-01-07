package com.suihan74.satena2.scene.preferences.page.favoriteSites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AnimatedListItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.model.browser.FaviconInfo
import com.suihan74.satena2.model.favoriteSite.FavoriteSite
import com.suihan74.satena2.model.favoriteSite.FavoriteSiteAndFavicon
import com.suihan74.satena2.scene.preferences.PrefItemDefaults
import com.suihan74.satena2.ui.theme.CurrentTheme
import java.time.Instant

/**
 * お気に入りサイトページ
 */
@Composable
fun FavoriteSitesPage(viewModel: FavoriteSitesViewModel) {
    Box(
        Modifier.fillMaxSize()
    ) {
        // コンテンツ
        LazyColumn(
            state = viewModel.lazyListState(),
            modifier = Modifier.fillMaxSize()
        ) {
            val fakeItems = buildList {
                add(
                    FavoriteSiteAndFavicon(
                        site = FavoriteSite(
                            url = "test.com/hoge",
                            title = "test",
                            isEnabled = true,
                            faviconInfoId = 0L
                        ),
                        faviconInfo = FaviconInfo(
                            site = "test.com",
                            filename = "dummy",
                            lastUpdated = Instant.MIN
                        )
                    )
                )
            }

            items(fakeItems) {
                AnimatedListItem {
                    FavoriteSiteItem(item = it) {
                        // TODO
                    }
                }
                Divider(
                    color = CurrentTheme.listItemDivider,
                    thickness = 1.dp
                )
            }
            emptyFooter()
        }

        // 項目追加ボタン
        FloatingActionButton(
            backgroundColor = CurrentTheme.primary,
            contentColor = CurrentTheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp),
            onClick = {
                // TODO
            }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "add an item"
            )
        }
    }
}

@Preview
@Composable
private fun FavoriteSitesPagePreview() {
    FavoriteSitesPage(FakeFavoriteSitesViewModel())
}

// ------ //

@Composable
fun FavoriteSiteItem(
    item: FavoriteSiteAndFavicon,
    onClick: ()->Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = onClick,
                onClick = onClick
            )
            .padding(
                vertical = PrefItemDefaults.listItemVerticalPadding,
                horizontal = PrefItemDefaults.listItemHorizontalPadding
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.faviconInfo?.filename)
                .error(R.drawable.ic_file)
                .build(),
            contentDescription = "favicon",
            colorFilter = ColorFilter.tint(CurrentTheme.onBackground),
            modifier = Modifier
                .padding(top = 3.dp, end = 6.dp)
                .size(16.dp)
        )
        Column(Modifier.fillMaxWidth()) {
            SingleLineText(
                text = item.site.title,
                fontSize = 16.sp,
                color = CurrentTheme.onBackground
            )
            SingleLineText(
                text = item.site.url,
                fontSize = 13.sp,
                color = CurrentTheme.grayTextColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview
@Composable
private fun FavoriteSiteItemPreview() {
    FavoriteSiteItem(
        FavoriteSiteAndFavicon(
            site = FavoriteSite(
                url = "test.com/hoge",
                title = "test",
                isEnabled = true,
                faviconInfoId = 0L
            ),
            faviconInfo = FaviconInfo(
                site = "test.com",
                filename = "dummy",
                lastUpdated = Instant.MIN
            )
        )
    )
}

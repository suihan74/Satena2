package com.suihan74.satena2.scene.entries

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.compose.SingleLineText
import com.suihan74.satena2.ui.theme.CurrentTheme

/**
 * EntriesActivityのドロワコンテンツルート
 */
@Composable
fun DrawerContent(
    viewModel: EntriesViewModel,
    onClickItem: ((Category) -> Unit) = {}
) {
    val hatenaAccount by viewModel.hatenaAccount.collectAsState()
    val categories = remember(hatenaAccount) {
        if (hatenaAccount != null) Category.valuesWithSignedIn()
        else Category.valuesWithoutSignedIn()
    }
    val listType by viewModel.categoryListType.collectAsState(initial = EntryCategoryListType.LIST)
    val modifier = Modifier
        .fillMaxSize()
        .background(CurrentTheme.drawerBackground)

    when (listType) {
        EntryCategoryListType.LIST -> {
            VerticalCategoriesList(modifier, categories, onClickItem)
        }

        EntryCategoryListType.GRID -> {
            CategoriesGrid(modifier, categories, onClickItem)
        }
    }
}

// ------ //

/**
 * 縦方向リスト型のカテゴリ一覧
 */
@Preview
@Composable
private fun VerticalCategoriesList(
    modifier: Modifier = Modifier,
    categories: Array<Category> = Category.valuesWithoutSignedIn(),
    onClickItem: (Category) -> Unit = {}
) {
    LazyColumn(modifier) {
        items(categories) { category ->
            VerticalCategoryItem(category, onClickItem)
        }
    }
}

/**
 * カテゴリリスト項目
 */
@Preview
@Composable
private fun VerticalCategoryItem(
    category: Category = Category.All,
    onClickItem: (Category) -> Unit = {}
) {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(
        Modifier
            .background(CurrentTheme.drawerBackground)
            .fillMaxWidth()
            .clickable { onClickItem(category) }
            .padding(bottom = navigationBarHeight)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Image(
                painter = painterResource(category.iconId),
                contentDescription = "category icon",
                colorFilter = ColorFilter.tint(CurrentTheme.drawerOnBackground),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp)
            )
            SingleLineText(
                text = stringResource(category.textId),
                fontSize = 20.sp,
                color = CurrentTheme.drawerOnBackground,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

// ------ //

/**
 * グリッド型のカテゴリ一覧
 */
@Preview
@Composable
private fun CategoriesGrid(
    modifier: Modifier = Modifier,
    categories: Array<Category> = Category.valuesWithoutSignedIn(),
    onClickItem: (Category) -> Unit = {}
) {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .background(CurrentTheme.drawerBackground)
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(64.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = navigationBarHeight)
        ) {
            items(categories) { category ->
                GridCategoryItem(category, onClickItem)
            }
        }
    }
}

/**
 * カテゴリグリッド項目
 */
@Preview
@Composable
private fun GridCategoryItem(
    category: Category = Category.All,
    onClickItem: (Category) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(CurrentTheme.drawerBackground)
            .size(width = 64.dp, height = 96.dp)
            .clickable { onClickItem(category) }
    ) {
        Image(
            painter = painterResource(category.iconId),
            contentDescription = "category icon",
            colorFilter = ColorFilter.tint(CurrentTheme.drawerOnBackground),
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 12.dp)
                .size(32.dp)
        )
        Text(
            text = stringResource(category.textId),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = CurrentTheme.drawerOnBackground,
            modifier = Modifier
        )
    }
}

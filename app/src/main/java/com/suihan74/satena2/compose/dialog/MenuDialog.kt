package com.suihan74.satena2.compose.dialog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.Dimension
import com.suihan74.satena2.R
import kotlinx.coroutines.launch

/**
 * 汎用メニューダイアログ
 */
@Composable
fun MenuDialog(
    menuItems: List<MenuDialogItem>,
    onDismissRequest: ()->Unit,
    modifier: Modifier = Modifier,
    titleText: String? = null,
    positiveButton: DialogButton? = null,
    negativeButton: DialogButton? = null,
    neutralButton: DialogButton? = null,
    colors: CustomDialogColors = CustomDialogDefaults.colors(),
    properties: DialogProperties = DialogProperties(),
    widthRatio: Float = CustomDialogDefaults.DEFAULT_WIDTH_RATIO
) {
    CustomDialog(
        modifier = modifier,
        titleText = titleText,
        positiveButton = positiveButton,
        negativeButton = negativeButton,
        neutralButton = neutralButton,
        colors = colors,
        onDismissRequest = onDismissRequest,
        properties = properties,
        widthRatio = widthRatio,
        contentHeight = Dimension.preferredWrapContent,
    ) {
        MenuDialogContent(menuItems, colors, onDismissRequest)
    }
}

/**
 * 汎用メニューダイアログ
 */
@Composable
fun MenuDialog(
    menuItems: List<MenuDialogItem>,
    onDismissRequest: ()->Unit,
    modifier: Modifier = Modifier,
    title: @Composable ()->Unit,
    positiveButton: DialogButton? = null,
    negativeButton: DialogButton? = null,
    neutralButton: DialogButton? = null,
    colors: CustomDialogColors = CustomDialogDefaults.colors(),
    properties: DialogProperties = DialogProperties(),
    widthRatio: Float = CustomDialogDefaults.DEFAULT_WIDTH_RATIO
) {
    CustomDialog(
        modifier = modifier,
        title = title,
        positiveButton = positiveButton,
        negativeButton = negativeButton,
        neutralButton = neutralButton,
        colors = colors,
        onDismissRequest = onDismissRequest,
        properties = properties,
        widthRatio = widthRatio,
        contentHeight = Dimension.preferredWrapContent,
    ) {
        MenuDialogContent(menuItems, colors, onDismissRequest)
    }
}

@Composable
private fun MenuDialogContent(
    menuItems: List<MenuDialogItem>,
    colors: CustomDialogColors,
    onDismissRequest: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxWidth()
    ) {
        items(menuItems) { item ->
            item.composable(colors, onDismissRequest)
        }
    }
}

// ------ //

/**
 * `MenuDialog`の`menuItems`引数に渡す表示項目データクラス
 */
data class MenuDialogItem(
    val composable : @Composable (colors: CustomDialogColors, onDismissRequest: () -> Unit)->Unit
)

/**
 * `MenuDialog`の`menuItems`引数に渡す表示項目データ（ラベルを文字列で指定）
 */
fun menuDialogItem(
    text: String,
    icon: Painter? = null,
    action: suspend ()->Boolean = { true }
) = MenuDialogItem { colors, onDismissRequest ->
    val coroutineScope = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                coroutineScope.launch {
                    val result = action()
                    if (result) {
                        onDismissRequest()
                    }
                }
            }
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min)
            .padding(
                vertical = 16.dp,
                horizontal = CustomDialogDefaults.DEFAULT_TITLE_HORIZONTAL_PADDING
            )
    ) {
        icon?.let {
            Image(
                it,
                contentDescription = "menu item icon",
                colorFilter = ColorFilter.tint(colors.textColor),
                modifier = Modifier
                    .size(with(LocalDensity.current) { 16.sp.toDp() })
            )
            Spacer(Modifier.width(8.dp))
        }

        Text(
            text = text,
            color = colors.textColor,
            fontSize = 16.sp
        )
    }
}

/**
 * `MenuDialog`の`menuItems`引数に渡す表示項目データ（ラベルを文字列リソースで指定）
 */
@Composable
fun menuDialogItem(
    @StringRes textId: Int,
    @DrawableRes iconId: Int? = null,
    action: suspend ()->Boolean = { true }
) = menuDialogItem(
    text = stringResource(textId),
    icon = iconId?.let { painterResource(it) },
    action = action
)

// ------ //

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun MenuDialogPreview() {
    MenuDialog(
        titleText = "メニューダイアログ",
        menuItems = listOf(
            menuDialogItem(text = "test1", icon = painterResource(id = R.drawable.ic_person)) { true },
            menuDialogItem(text = "test2") { true },
            menuDialogItem(text = "テスト3") { true }
        ),
        positiveButton = DialogButton("OK"),
        negativeButton = DialogButton("CANCEL"),
        neutralButton = DialogButton("NEUTRAL"),
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

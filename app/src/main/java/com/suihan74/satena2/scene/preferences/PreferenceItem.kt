package com.suihan74.satena2.scene.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.ui.theme.CurrentTheme
import kotlinx.coroutines.flow.MutableStateFlow

object PrefItemDefaults {
    /** 設定項目の上下余白 */
    val verticalPadding = 16.dp

    /** 設定項目の左右余白 */
    val horizontalPadding = 24.dp

    /** リスト項目の上下余白 */
    val listItemVerticalPadding = 12.dp

    /** リスト項目の左右余白 */
    val listItemHorizontalPadding = 16.dp
}

// ------ //

/**
 * 設定項目として表示するコンテンツのベースUI
 */
@Composable
fun PrefItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    verticalPadding: Dp = PrefItemDefaults.verticalPadding,
    horizontalPadding: Dp = PrefItemDefaults.horizontalPadding,
    content: @Composable ColumnScope.()->Unit
) {
    PrefItem(
        modifier = modifier,
        onClick = onClick,
        topPadding = verticalPadding,
        bottomPadding = verticalPadding,
        startPadding = horizontalPadding,
        endPadding = horizontalPadding,
        content = content
    )
}

/**
 * 設定項目として表示するコンテンツのベースUI
 */
@Composable
fun PrefItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    topPadding: Dp = PrefItemDefaults.verticalPadding,
    bottomPadding: Dp = PrefItemDefaults.horizontalPadding,
    startPadding: Dp = PrefItemDefaults.verticalPadding,
    endPadding: Dp = PrefItemDefaults.horizontalPadding,
    content: @Composable ColumnScope.()->Unit
) {
    val m = (onClick?.let { modifier.clickable(onClick = onClick) } ?: modifier)
        .fillMaxWidth()
        .padding(
            top = topPadding,
            bottom = bottomPadding,
            start = startPadding,
            end = endPadding
        )

    Column(
        modifier = m,
        content = content
    )
}

// ------ //

/**
 * 単純なボタン
 */
@Composable
fun PrefButton(
    @StringRes mainTextId: Int,
    @StringRes subTextId: Int? = null,
    @StringRes subTextPrefixId: Int? = null,
    mainTextColor: Color = CurrentTheme.onBackground,
    subTextColor: Color = CurrentTheme.onBackground,
    subTextPrefixColor: Color = CurrentTheme.grayTextColor,
    onClick: (()->Unit)? = null
) {
    PrefButton(
        mainText = stringResource(mainTextId),
        subText = subTextId?.let { stringResource(it) } ?: "",
        subTextPrefix = subTextPrefixId?.let { stringResource(it) } ?: "",
        mainTextColor = mainTextColor,
        subTextColor = subTextColor,
        subTextPrefixColor = subTextPrefixColor,
        onClick = onClick
    )
}

/**
 * 単純なボタン
 */
@Composable
fun PrefButton(
    mainText: String,
    subText: String = "",
    subTextPrefix: String = "",
    mainTextColor: Color = CurrentTheme.onBackground,
    subTextColor: Color = CurrentTheme.onBackground,
    subTextPrefixColor: Color = CurrentTheme.grayTextColor,
    onClick: (()->Unit)? = null
) {
    PrefItem(onClick = onClick) {
        Text(
            text = mainText,
            color = mainTextColor,
            fontSize = 16.sp
        )

        if (subText.isNotEmpty() || subTextPrefix.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = subTextPrefix,
                    color = subTextPrefixColor,
                    fontSize = 12.sp,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = subText,
                    color = subTextColor,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PrefButtonPreview_two_lines() {
    PrefButton(
        mainText = "メインテキスト",
        subText = "サブテキスト",
        subTextPrefix = "",
    )
}

@Preview
@Composable
private fun PrefButtonPreview_single_line() {
    PrefButton(mainText = "メインテキスト")
}

// ------ //

/**
 * トグルボタン
 */
@Composable
fun PrefToggleButton(
    flow: MutableStateFlow<Boolean>,
    @StringRes mainTextId: Int,
    onValueChanged: (Boolean)->Unit = {}
) {
    PrefToggleButton(flow, stringResource(mainTextId), onValueChanged)
}

/**
 * トグルボタン
 */
@Composable
fun PrefToggleButton(
    flow: MutableStateFlow<Boolean>,
    mainText: String,
    onValueChanged: (Boolean) -> Unit = {}
) {
    val isOn = flow.collectAsState().value
    PrefToggleButton(
        isOn = isOn,
        mainText = mainText,
        onValueChanged = {
            flow.value = it
            onValueChanged(it)
        }
    )
}

@Composable
fun PrefToggleButton(
    state: MutableState<Boolean>,
    mainText: String,
    onValueChanged: (Boolean) -> Unit = {}
) {
    PrefToggleButton(
        isOn = state.value,
        mainText = mainText,
        onValueChanged = {
            state.value = it
            onValueChanged(it)
        }
    )
}

@Composable
fun PrefToggleButton(
    isOn: Boolean,
    mainText: String,
    onValueChanged: (Boolean)->Unit
) {
    PrefButton(
        mainText = mainText,
        subTextPrefix = stringResource(R.string.pref_current_value_prefix),
        subText = if (isOn) "ON" else "OFF",
        subTextColor = if (isOn) CurrentTheme.primary else CurrentTheme.onBackground,
        onClick = { onValueChanged(!isOn) }
    )
}

@Preview
@Composable
private fun PrefToggleButtonPreview() {
    PrefToggleButton(
        flow = MutableStateFlow(true),
        mainText = "トグルボタン"
    )
}

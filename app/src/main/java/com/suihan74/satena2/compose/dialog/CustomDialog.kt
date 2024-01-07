package com.suihan74.satena2.compose.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.launch

/**
 * 汎用ダイアログ
 */
@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    title: @Composable ()->Unit,
    buttons: @Composable RowScope.()->Unit,
    backgroundColor: Color = MaterialTheme.colors.background,
    onDismissRequest: ()->Unit,
    properties: DialogProperties = DialogProperties(),
    widthRatio: Float = CustomDialogDefaults.DEFAULT_WIDTH_RATIO,
    contentHeight: Dimension = Dimension.preferredWrapContent,
    content: @Composable ()->Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = backgroundColor,
            modifier = modifier
                .fillMaxWidth(widthRatio)
        ) {
            ConstraintLayout(Modifier.fillMaxWidth()) {
                val (titleArea, contentsArea, buttonsArea) = createRefs()

                // タイトルバー
                Box(
                    Modifier.constrainAs(titleArea) {
                        linkTo(
                            top = parent.top,
                            bottom = contentsArea.top,
                            start = parent.start,
                            end = parent.end
                        )
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                ) {
                    title()
                }

                // コンテンツ
                Box(
                    Modifier.constrainAs(contentsArea) {
                        linkTo(
                            top = titleArea.bottom,
                            bottom = buttonsArea.top,
                            start = parent.start,
                            end = parent.end
                        )
                        width = Dimension.fillToConstraints
                        height = contentHeight
                    }
                ) {
                    content()
                }

                // ボタンエリア
                Row(
                    modifier = Modifier
                        .constrainAs(buttonsArea) {
                            linkTo(
                                top = contentsArea.bottom,
                                bottom = parent.bottom,
                                start = parent.start,
                                end = parent.end
                            )
                            width = Dimension.fillToConstraints
                            height = Dimension.wrapContent
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    buttons()
                }
            }
        }
    }
}

// ------ //

/**
 * 汎用ダイアログ
 */
@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    title: @Composable ()->Unit,
    positiveButton: DialogButton? = null,
    negativeButton: DialogButton? = null,
    neutralButton: DialogButton? = null,
    colors: CustomDialogColors = CustomDialogDefaults.colors(),
    onDismissRequest: ()->Unit,
    properties: DialogProperties = DialogProperties(),
    widthRatio: Float = CustomDialogDefaults.DEFAULT_WIDTH_RATIO,
    contentHeight: Dimension = Dimension.preferredWrapContent,
    content: @Composable ()->Unit
) {
    CustomDialog(
        backgroundColor = colors.backgroundColor,
        title = title,
        contentHeight = contentHeight,
        content = content,
        buttons = {
            val coroutineScope = rememberCoroutineScope()
            if (neutralButton != null) {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                    elevation = null,
                    onClick = { coroutineScope.launch { neutralButton.action() } }
                ) {
                    Text(
                        text = neutralButton.text,
                        color = colors.neutralButtonTextColor
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                if (negativeButton != null) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        elevation = null,
                        onClick = { coroutineScope.launch { negativeButton.action() } }
                    ) {
                        Text(
                            text = negativeButton.text,
                            color = colors.negativeButtonTextColor
                        )
                    }
                }
                if (positiveButton != null) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        elevation = null,
                        onClick = { coroutineScope.launch { positiveButton.action() } }
                    ) {
                        Text(
                            text = positiveButton.text,
                            color = colors.positiveButtonTextColor
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        widthRatio = widthRatio,
        properties = properties,
        modifier = modifier
    )
}

// ------ //

/**
 * 汎用ダイアログ
 */
@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    titleText: String? = null,
    positiveButton: DialogButton? = null,
    negativeButton: DialogButton? = null,
    neutralButton: DialogButton? = null,
    colors: CustomDialogColors = CustomDialogDefaults.colors(),
    onDismissRequest: ()->Unit,
    properties: DialogProperties = DialogProperties(),
    widthRatio: Float = CustomDialogDefaults.DEFAULT_WIDTH_RATIO,
    contentHeight: Dimension = Dimension.preferredWrapContent,
    content: @Composable ()->Unit
) {
    CustomDialog(
        title = {
            if (titleText != null) {
                Text(
                    text = titleText,
                    fontSize = 20.sp,
                    color = colors.textColor,
                    modifier = Modifier
                        .padding(
                            vertical = CustomDialogDefaults.DEFAULT_TITLE_VERTICAL_PADDING,
                            horizontal = CustomDialogDefaults.DEFAULT_TITLE_HORIZONTAL_PADDING
                        )
                )
            }
        },
        positiveButton = positiveButton,
        negativeButton = negativeButton,
        neutralButton = neutralButton,
        contentHeight = contentHeight,
        content = content,
        colors = colors,
        onDismissRequest = onDismissRequest,
        widthRatio = widthRatio,
        properties = properties,
        modifier = modifier
    )
}

// ------ //

/**
 * 簡易`CustomDialog`ボタンに渡す表示項目データクラス
 */
data class DialogButton(
    val text : String,
    val action : suspend ()->Unit = {}
)

fun dialogButton(
    text: String,
    action: suspend ()->Unit = {}
) = DialogButton(text = text, action = action)

@Composable
fun dialogButton(
    @StringRes textId: Int,
    action: suspend ()->Unit = {}
) = DialogButton(text = stringResource(textId), action = action)

// ------ //

@Preview
@Composable
private fun CustomDialogPreview() {
    Box(Modifier.fillMaxSize()) {
        CustomDialog(
            titleText = "タイトル",
            onDismissRequest = {},
            positiveButton = dialogButton("OK"),
            negativeButton = dialogButton("キャンセル"),
        ) {
            Text(text = "コンテンツ")
        }
    }
}

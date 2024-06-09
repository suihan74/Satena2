package com.suihan74.satena2.scene.preferences.page.userLabel

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.AnimatedListItem
import com.suihan74.satena2.compose.combinedClickable
import com.suihan74.satena2.compose.dialog.DialogButton
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.compose.emptyFooter
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.model.userLabel.LabelAndUsers
import com.suihan74.satena2.model.userLabel.UserAndLabels
import com.suihan74.satena2.scene.preferences.HatenaUserItem
import com.suihan74.satena2.scene.preferences.PrefItemDefaults
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.utility.hatena.hatenaUserIconUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ユーザーラベルページ
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserLabelsPage(
    viewModel: UserLabelsViewModel,
    pagerState: PagerState
) {
    val navController = rememberNavController()
    val labels by viewModel.labelsFlow.collectAsState(initial = emptyList())
    val targetLabel = remember { mutableStateOf<Label?>(null) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect {
                navController.navigate("labels") { popUpTo(0) }
                targetLabel.value = null
            }
    }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "empty",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable("empty") {
            Box(Modifier.fillMaxSize()) {}
        }

        composable("labels") {
            val lazyListState = viewModel.lazyListState()
            LabelsContents(
                labels = labels,
                lazyListState = lazyListState,
                dialogProperties = viewModel.dialogProperties(),
                onClick = {
                    targetLabel.value = it
                    navController.navigate("users")
                },
                onRegistration = { label -> viewModel.createLabel(label) },
                onDelete = { label -> viewModel.deleteLabel(label) }
            )
        }

        composable(
            "users",
            enterTransition = {
                slideInHorizontally(animationSpec = tween(300)) { it }
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween(300)) { it }
            }
        ) {
            targetLabel.value?.let { l ->
                val label by viewModel.labeledUsersFlow(l).collectAsState(initial = null)
                UsersContents(
                    label = label,
                    allLabels = labels,
                    dialogProperties = viewModel.dialogProperties(),
                    userAndLabelsGetter = { user -> viewModel.userAndLabelsFlow(user) },
                    onBack = {
                        navController.popBackStack()
                    },
                    onCrateLabel = {
                        viewModel.createLabel(it)
                    },
                    onUpdateLabels = { user, states ->
                        viewModel.updateUserLabels(user, states)
                    }
                )
            }
        }
    }
}

// ------ //

/**
 * ラベルリスト
 */
@Composable
private fun LabelsContents(
    labels: List<Label>,
    lazyListState: LazyListState,
    dialogProperties: DialogProperties,
    onClick: (Label)->Unit,
    onRegistration: suspend (Label)->Boolean,
    onDelete: suspend (Label)->Boolean
) {
    var menuDialogTarget by remember { mutableStateOf<Label?>(null) }
    var editorDialogTarget by remember { mutableStateOf<Label?>(null) }

    Box(
        Modifier.fillMaxSize()
    ) {
        // コンテンツ
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(labels) { item ->
                AnimatedListItem {
                    LabelItem(
                        label = item,
                        onEdit = { /* todo */ },
                        onClick = onClick,
                        onLongClick = { menuDialogTarget = it }
                    )
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
                editorDialogTarget = Label(name = "")
            }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "add an item"
            )
        }
    }

    if (menuDialogTarget != null) {
        MenuDialog(
            titleText = menuDialogTarget!!.name,
            menuItems = listOf(
                menuDialogItem(R.string.edit) {
                    editorDialogTarget = menuDialogTarget
                    true
                },
                menuDialogItem(R.string.delete) {
                    onDelete(menuDialogTarget!!)
                }
            ),
            negativeButton = DialogButton(text = stringResource(R.string.close)),
            onDismissRequest = { menuDialogTarget = null },
            properties = dialogProperties
        )
    }

    editorDialogTarget?.let { label ->
        UserLabelNameEditionDialog(
            label = label,
            onRegistration = { onRegistration(it) },
            onDismiss = { editorDialogTarget = null },
            dialogProperties = dialogProperties
        )
    }
}

// ------ //

/**
 * 指定ラベルがついたユーザーリスト
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UsersContents(
    label: LabelAndUsers?,
    allLabels: List<Label>,
    dialogProperties: DialogProperties,
    userAndLabelsGetter: (String)-> Flow<UserAndLabels?>,
    onBack: ()->Unit,
    onCrateLabel: suspend (Label)->Boolean,
    onUpdateLabels: (String, List<Pair<Label, Boolean>>)->Unit
) {
    if (label == null) {
        Box(Modifier.fillMaxSize())
        return
    }

    val coroutineScope = rememberCoroutineScope()
    var userLabelsSheetTarget by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        modifier = Modifier.fillMaxSize(),
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetBackgroundColor = CurrentTheme.background,
        sheetContent = {
            val checkedLabels by remember(userLabelsSheetTarget) {
                userLabelsSheetTarget?.let { userAndLabelsGetter(it) } ?: MutableStateFlow(null)
            }.collectAsState(initial = null)

            UserLabelDialog(
                labels = allLabels,
                checkedLabels = checkedLabels?.labels.orEmpty(),
                dialogProperties = dialogProperties,
                onCreateLabel = onCrateLabel,
                onUpdate = {
                    coroutineScope.launch {
                        onUpdateLabels(userLabelsSheetTarget!!, it)
                        bottomSheetState.hide()
                    }
                }
            )
        }
    ) {
        var menuTargetUser by remember { mutableStateOf<String?>(null) }
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                // タイトル
                Column(
                    Modifier
                        .background(CurrentTheme.bottomBarBackground)
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = label.userLabel.name,
                        color = CurrentTheme.bottomBarOnBackground,
                        fontSize = 18.sp
                    )
                    Text(
                        text = label.users.size.let { "$it user${if (it == 1) "" else "s"}" },
                        color = CurrentTheme.bottomBarOnBackground,
                        fontSize = 13.sp
                    )
                }

                // コンテンツ
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(label.users) { item ->
                        AnimatedListItem {
                            HatenaUserItem(
                                item.name,
                                hatenaUserIconUrl(item.name),
                                onClick = { menuTargetUser = item.name },
                                onLongClick = { menuTargetUser = item.name }
                            )
                        }
                        Divider(
                            color = CurrentTheme.listItemDivider,
                            thickness = 1.dp
                        )
                    }
                    emptyFooter()
                }
            }

            Row(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp)
            ) {
                // 戻るボタン
                FloatingActionButton(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Bottom),
                    onClick = onBack
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back a screen"
                    )
                }
                // 項目追加ボタン
                FloatingActionButton(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(48.dp),
                    onClick = {
                    }
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "add an item"
                    )
                }
            }
        }

        menuTargetUser?.let { user ->
            ItemMenuDialog(
                user = user,
                dialogProperties = dialogProperties,
                onDismiss = { menuTargetUser = null },
                onEditLabels = {
                    coroutineScope.launch {
                        userLabelsSheetTarget = user
                        bottomSheetState.show()
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun UsersContentsPreview() {
    UsersContents(
        label = LabelAndUsers().apply {
            userLabel = Label(name = "Test Label")
            users = listOf()
        },
        allLabels = emptyList(),
        dialogProperties = remember { DialogProperties() },
        onBack = {},
        onCrateLabel = { true },
        onUpdateLabels = { _, _ -> },
        userAndLabelsGetter = { MutableStateFlow(null) }
    )
}

// ------ //

/**
 * ユーザー項目に対する操作メニュー
 */
@Composable
private fun ItemMenuDialog(
    user: String,
    dialogProperties: DialogProperties,
    onDismiss: ()->Unit,
    onEditLabels: (String)->Unit
) {
    if (user.isBlank()) return
    val iconUrl = hatenaUserIconUrl(user)

    MenuDialog(
        title = { HatenaUserItem(user, iconUrl) },
        positiveButton = dialogButton(R.string.close) { onDismiss() },
        menuItems = listOf(
            menuDialogItem(R.string.pref_user_menu_dialog_recent_bookmarks) { /* TODO */ true },
            menuDialogItem(R.string.pref_user_menu_dialog_set_labels) {
                onEditLabels(user)
                true
            }
        ),
        onDismissRequest = { onDismiss() },
        colors = themedCustomDialogColors(),
        properties = dialogProperties
    )
}

@Preview
@Composable
private fun ItemMenuDialogPreview() {
    Box(
        Modifier.fillMaxSize()
    ) {
        ItemMenuDialog(
            user = "suihan74",
            dialogProperties = remember { DialogProperties() },
            onEditLabels = {},
            onDismiss = {}
        )
    }
}

// ------ //

/**
 * プリセット名リスト項目
 */
@Composable
private fun LabelItem(
    label: Label,
    onEdit: (Label)->Unit,
    onClick: (Label)->Unit,
    onLongClick: (Label)->Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(label) },
                onLongClick = { onLongClick(label) }
            )
            .padding(
                vertical = PrefItemDefaults.listItemVerticalPadding + 4.dp,
                horizontal = PrefItemDefaults.listItemHorizontalPadding
            )
    ) {
        Text(
            text = label.name,
            color = CurrentTheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

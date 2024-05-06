package com.suihan74.satena2.scene.bookmarks

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BookmarkSharingContent
import com.suihan74.satena2.compose.EntrySharingContent
import com.suihan74.satena2.compose.LocalLongClickVibrationDuration
import com.suihan74.satena2.compose.OrientatedModalDrawer
import com.suihan74.satena2.compose.VerticalGradientEdge
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.DialogButton
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntryType
import com.suihan74.satena2.scene.entries.Category
import com.suihan74.satena2.scene.entries.DisplayEntry
import com.suihan74.satena2.scene.entries.EntryActionHandler
import com.suihan74.satena2.scene.entries.FakeEntryActionHandler
import com.suihan74.satena2.scene.entries.FilterState
import com.suihan74.satena2.scene.entries.bottomSheet.EntryItemMenuContent
import com.suihan74.satena2.scene.preferences.page.ngWords.FakeNgWordsViewModel
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsViewModel
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsViewModelImpl
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionDialog
import com.suihan74.satena2.scene.preferences.page.theme.ThemeViewModelImpl
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelDialog
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2Theme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.utility.argument
import com.suihan74.satena2.utility.extension.LocalUseSystemTimeZone
import com.suihan74.satena2.utility.extension.onTrue
import com.suihan74.satena2.utility.extension.putObjectExtra
import com.suihan74.satena2.utility.extension.trimScheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// ------ //

private enum class BottomSheetContent {
    /** ブランク */
    Empty,
    /** エントリを共有 */
    ShareEntry,
    /** エントリに対するメニュー */
    EntryMenu,
    /** ブクマに対するメニュー */
    BookmarkMenu,
    /** ブクマを共有 */
    ShareBookmark,
    /** ユーザーラベルの設定 */
    UserLabel,
    /** ブクマに含まれるリンク一覧 */
    Urls,
    /** ブクマに含まれるタグ一覧 */
    Tags,
    /** ブクマを通報 */
    Report,
    /** 「カスタム」タブの表示対象 */
    CustomTabSetting
}

// ------ //

/**
 * ブクマ一覧画面
 */
@AndroidEntryPoint
class BookmarksActivity : ComponentActivity() {
    private val viewModel by viewModels<BookmarksViewModelImpl>()

    private val themeViewModel by viewModels<ThemeViewModelImpl>()

    private val relatedEntriesViewModel by viewModels<RelatedEntriesViewModelImpl>()

    private val ngWordsViewModel by viewModels<NgWordsViewModelImpl>()

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreateActivity(activityResultRegistry, lifecycle)
        relatedEntriesViewModel.onCreateActivity(activityResultRegistry, lifecycle)

        setContent {
            val theme by themeViewModel.currentThemeFlow.collectAsState()
            val longClickVibrationDuration by viewModel.longClickVibrationDuration.collectAsState(40L)
            val useSystemTimeZone by viewModel.useSystemTimeZone.collectAsState(false)

            Satena2Theme(theme) {
                CompositionLocalProvider(
                    LocalLongClickVibrationDuration provides longClickVibrationDuration,
                    LocalUseSystemTimeZone provides useSystemTimeZone
                ) {
                    BookmarksScene(
                        viewModel = viewModel,
                        entryActionHandler = relatedEntriesViewModel,
                        ngWordsViewModel = ngWordsViewModel,
                        intent = intent,
                        savedInstanceState = savedInstanceState
                    )
                }
            }
        }
    }

    override fun finish() {
        this.setResult(
            RESULT_OK,
            Intent().apply {
                putObjectExtra(
                    BookmarksActivityContract.RESULT_ENTRY,
                    viewModel.entityFlow.value.entry
                )
            }
        )
        super.finish()
    }
}

// ------ //

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookmarksScene(
    viewModel: BookmarksViewModel,
    entryActionHandler: EntryActionHandler,
    ngWordsViewModel: NgWordsViewModel,
    intent: Intent,
    savedInstanceState: Bundle?
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerAlignment by viewModel.drawerAlignmentFlow.collectAsState(initial = Alignment.Start)

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    var bottomSheetContent by remember { mutableStateOf(BottomSheetContent.Empty) }

    val account by viewModel.hatenaAccountFlow.collectAsState(initial = null)
    val entity by viewModel.entityFlow.collectAsState(initial = Entity.EMPTY)

    var bottomMenuTarget by remember { mutableStateOf<DisplayBookmark?>(null) }
    val ignoreUserDialogTarget = remember { mutableStateOf<DisplayBookmark?>(null) }
    val shareBookmarkTarget = remember { mutableStateOf<DisplayBookmark?>(null) }

    var entryMenuTarget by remember { mutableStateOf<DisplayEntry?>(null) }

    // エントリの既読マークを使用するかのフラグ
    val entryReadMarkVisible by viewModel.recordReadEntriesEnabled.collectAsState()

    // ブクマのNGワード設定編集対象
    var ngWordTarget by remember { mutableStateOf<DisplayBookmark?>(null) }

    val initialSetting by viewModel.customTabSettingFlow.collectAsState()
    val labels by viewModel.allUserLabelsFlow.collectAsState()

    val onShowEntryMenu: (DisplayEntry)->Unit = {
        entryMenuTarget = it
        bottomSheetContent = BottomSheetContent.EntryMenu
        coroutineScope.launch {
            bottomSheetState.hide()
            bottomSheetState.show()
        }
    }

    val onShareEntry: (DisplayEntry)->Unit = {
        entryMenuTarget = it
        bottomSheetContent = BottomSheetContent.ShareEntry
        coroutineScope.launch {
            bottomSheetState.hide()
            bottomSheetState.show()
        }
    }

    // 初期ロード
    LaunchedEffect(savedInstanceState) {
        if (savedInstanceState == null) {
            viewModel.load(intent, navController)
        }
    }

    ModalBottomSheetLayout(
        modifier = Modifier.fillMaxSize(),
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetBackgroundColor = CurrentTheme.background,
        sheetContent = {
            when (bottomSheetContent) {
                // 何も設定されていない状態
                BottomSheetContent.Empty -> { Box(Modifier.fillMaxHeight()) }

                // エントリメニュー
                BottomSheetContent.EntryMenu -> {
                    var ngWordEditionDialogVisible by remember { mutableStateOf(false) }
                    EntryItemMenuContent(
                        item = entryMenuTarget,
                        sheetState = bottomSheetState,
                        category = Category.All,
                        account = account,
                        readMarkVisible = entryReadMarkVisible,
                        onLaunchBookmarksActivity = { entryActionHandler.launchBookmarksActivity(it) },
                        onLaunchBrowserActivity = { entryActionHandler.launchBrowserActivity(it) },
                        onLaunchOuterBrowser = { entryActionHandler.openWithOtherApp(it) },
                        onShare = onShareEntry,
                        onNavigateSiteCategory = { entryActionHandler.navigateSiteCategory(it.entry.rootUrl, navController) },
                        onFavorite = { /* todo */ },
                        onUnFavorite = { /* todo */ },
                        onCreateNgWord = { ngWordEditionDialogVisible = true },
                        onRead = { entry, isPrivate -> entryActionHandler.readEntry(entry, isPrivate) },
                        onReadLater = { entry, isPrivate -> entryActionHandler.readLaterEntry(entry, isPrivate) },
                        onDeleteReadMark = { entryActionHandler.removeReadMark(it) },
                        onDeleteBookmark = { entryActionHandler.removeBookmark(it) }
                    )

                    ngWordEditionDialogVisible.onTrue {
                        NgWordEditionDialog(
                            initialText = entryMenuTarget!!.entry.title,
                            initialUrl = entryMenuTarget!!.entry.url.trimScheme(),
                            isError = { text, asRegex -> ngWordsViewModel.isNgRegexError(text, asRegex) },
                            properties = viewModel.dialogProperties(),
                            onDismiss = { ngWordEditionDialogVisible = false }
                        ) {
                            ngWordsViewModel.insert(it.toIgnoredEntry()).onTrue {
                                bottomSheetState.hide()
                            }
                        }
                    }
                }

                // エントリ共有メニュー
                BottomSheetContent.ShareEntry -> {
                    EntrySharingContent(entry = entryMenuTarget!!.entry)
                }

                // ブクマメニュー
                BottomSheetContent.BookmarkMenu -> {
                    BookmarkItemMenuContent(
                        item = bottomMenuTarget,
                        coroutineScope = coroutineScope,
                        onShowRecentBookmarks = {
                            viewModel.launchEntriesActivityForUser(it)
                            bottomSheetState.hide()
                        },
                        onShowBookmarksToItem = {
                            viewModel.launchBookmarksActivityToBookmark(it)
                            bottomSheetState.hide()
                        },
                        onShowUserLabelDialog = {
                            bottomSheetState.hide()
                            bottomSheetContent = BottomSheetContent.UserLabel
                            bottomSheetState.show()
                        },
                        onSelectUrlsMenu = {
                            bottomSheetState.hide()
                            bottomSheetContent = BottomSheetContent.Urls
                            bottomSheetState.show()
                        },
                        onSelectTagsMenu = {
                            bottomSheetState.hide()
                            bottomSheetContent = BottomSheetContent.Tags
                            bottomSheetState.show()
                        },
                        onSelectNgWordsMenu = {
                            ngWordTarget = it
                            bottomSheetState.hide()
                        },
                        onFollow = {
                            // todo
                            bottomSheetState.hide()
                        },
                        onIgnore = {
                            ignoreUserDialogTarget.value = it
                            bottomSheetState.hide()
                        },
                        onShare = {
                            bottomSheetState.hide()
                            bottomSheetContent = BottomSheetContent.ShareBookmark
                            shareBookmarkTarget.value = it
                            bottomSheetState.show()
                        },
                        onReport = {
                            bottomSheetState.hide()
                            bottomSheetContent = BottomSheetContent.Report
                            bottomSheetState.show()
                        }
                    )
                }

                // ブクマ共有メニュー
                BottomSheetContent.ShareBookmark -> {
                    Column(Modifier.height(300.dp)) {
                        shareBookmarkTarget.value?.let {
                            BookmarkSharingContent(bookmark = it.bookmark)
                        }
                    }
                }

                // ユーザーラベル
                BottomSheetContent.UserLabel -> {
                    val user = bottomMenuTarget?.bookmark?.user
                    if (user == null) {
                        Box(Modifier.height(1.dp))
                    }
                    else {
                        val userLabels by viewModel.allUserLabelsFlow.collectAsState()
                        val userAndLabels by viewModel.userLabelsFlow(user).collectAsState(initial = null)
                        UserLabelDialog(
                            labels = userLabels,
                            checkedLabels = userAndLabels?.labels.orEmpty(),
                            onUpdate = {
                                coroutineScope.launch {
                                    viewModel.updateUserLabels(user, it)
                                    bottomSheetContent = BottomSheetContent.Empty
                                    bottomSheetState.hide()
                                }
                            }
                        )
                    }
                }

                // コメントに含まれるリンクリスト
                BottomSheetContent.Urls -> {
                    if (bottomMenuTarget?.urls.isNullOrEmpty()) {
                        Box(Modifier.height(1.dp))
                    }
                    else {
                        BookmarkUrlsMenuContent(
                            item = bottomMenuTarget,
                            onSelectUrl = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    viewModel.openBrowser(it)
                                }
                            }
                        )
                    }
                }

                // タグリスト
                BottomSheetContent.Tags -> {
                    if (bottomMenuTarget?.bookmark?.tags.isNullOrEmpty()) {
                        Box(Modifier.height(1.dp))
                    }
                    else {
                        BookmarkTagsMenuContent(
                            item = bottomMenuTarget,
                            onSelectTag = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    viewModel.launchEntriesActivityForTag(it)
                                }
                            }
                        )
                    }
                }

                // ブクマを報告
                BottomSheetContent.Report -> {
                    if (bottomMenuTarget?.bookmark == null) {
                        Box(Modifier.height(1.dp))
                    }
                    else {
                        ReportBookmarkContent(
                            entry = entity.entry,
                            item = bottomMenuTarget,
                            onReport = {
                                coroutineScope.launch {
                                    runCatching {
                                        viewModel.report(it)
                                    }.onSuccess {
                                        bottomSheetState.hide()
                                    }
                                }
                            }
                        )
                    }
                }

                // 「カスタム」タブの表示対象を設定
                BottomSheetContent.CustomTabSetting -> {
                    CustomTabSettingContent(
                        initialSetting = initialSetting,
                        allLabels = labels
                    ) {
                        viewModel.updateCustomTabSetting(it)
                        coroutineScope.launch {
                            bottomSheetState.hide()
                        }
                    }
                }
            }
        }
    ) {
        OrientatedModalDrawer(
            modifier = Modifier
                .fillMaxSize()
                .background(CurrentTheme.background),
            isRtl = drawerAlignment == Alignment.End,
            width = 300.dp,
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    entryActionHandler = entryActionHandler,
                    entity = entity,
                    launchBrowser = { viewModel.openBrowser(it) },
                    downStair = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.downStair()
                    },
                    upStair = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.upStair()
                    },
                    onShowEntryMenu = onShowEntryMenu,
                    onShareEntryMenu = onShareEntry,
                    onShowTag = { viewModel.launchEntriesActivityForTag(it) }
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "bookmarks",
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(300)) { it }
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(300)) { it }
                },
            ) {
                composable(
                    route = "bookmarks",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { fadeOut(animationSpec = tween(600)) }
                ) {
                    BookmarksMainContent(
                        viewModel = viewModel,
                        entity = entity,
                        navController = navController,
                        onClickTopBar = { viewModel.onClickTitleBar() },
                        onLongClickTopBar = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                entryMenuTarget = DisplayEntry(
                                    entry = entity.entry,
                                    read = null,
                                    filterState = FilterState.VALID,
                                )
                                bottomSheetContent = BottomSheetContent.EntryMenu
                                bottomSheetState.show()
                            }
                        },
                        onShowBookmarkItemMenu = {
                            coroutineScope.launch {
                                bottomSheetContent = BottomSheetContent.BookmarkMenu
                                bottomMenuTarget = it
                                bottomSheetState.show()
                            }
                        },
                        onOpenBottomSetting = {
                            coroutineScope.launch {
                                bottomSheetContent = BottomSheetContent.CustomTabSetting
                                bottomSheetState.show()
                            }
                        }
                    )
                    // ドロワ開閉よりもNavHostの戻るボタン処理が優先されないように内側で使用
                    BackHandler(drawerState.isOpen) {
                        coroutineScope.launch { drawerState.close() }
                    }
                }
                composable(
                    "detail/{user}",
                    arguments = listOf(navArgument("user") { type = NavType.StringType })
                ) { backStackEntry ->
                    val user = backStackEntry.argument<String>("user") ?: return@composable
                    val item by viewModel.getUserBookmarkFlow(user)
                        .collectAsState(initial = null)
                    BookmarkDetailContent(
                        viewModel = viewModel,
                        navController = navController,
                        item = item,
                        onShowBookmarkItemMenu = {
                            coroutineScope.launch {
                                bottomSheetContent = BottomSheetContent.BookmarkMenu
                                bottomMenuTarget = it
                                bottomSheetState.show()
                            }
                        }
                    )
                    // ドロワ開閉よりもNavHostの戻るボタン処理が優先されないように内側で使用
                    BackHandler(drawerState.isOpen) {
                        coroutineScope.launch { drawerState.close() }
                    }
                }
            }
        }
    }

    if (bottomSheetState.isVisible) {
        if (bottomSheetState.currentValue == ModalBottomSheetValue.HalfExpanded) {
            Box(Modifier.fillMaxSize()) {
                VerticalGradientEdge(
                    topColor = Color.Transparent,
                    bottomColor = CurrentTheme.background,
                    height = 48.dp,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        BackHandler(true) {
            coroutineScope.launch {
                bottomSheetState.hide()
            }
        }
    }

    // NGワード追加
    ngWordTarget?.let { target ->
        NgWordEditionDialog(
            initialText = target.bookmark.comment,
            initialUrl = target.bookmark.link,
            initialTabIndex = IgnoredEntryType.TEXT.ordinal,
            isError = { text, asRegex -> ngWordsViewModel.isNgRegexError(text, asRegex) },
            properties = viewModel.dialogProperties(),
            onDismiss = { ngWordTarget = null }
        ) {
            ngWordsViewModel.insert(it.toIgnoredEntry())

        }
    }

    // ユーザーの非表示ON/OFF
    ignoreUserDialogTarget.value?.let { target ->
        CustomDialog(
            title = {
                Text(
                    stringResource(
                        if (target.ignoredUser) R.string.unmute_user_dialog_title
                        else R.string.mute_user_dialog_title
                    )
                )
            },
            negativeButton = DialogButton(stringResource(R.string.cancel)) {
                ignoreUserDialogTarget.value = null
            },
            positiveButton = DialogButton(stringResource(R.string.ok)) {
                viewModel.toggleIgnore(target.bookmark.user)
                ignoreUserDialogTarget.value = null
            },
            colors = themedCustomDialogColors(),
            onDismissRequest = { ignoreUserDialogTarget.value = null },
            properties = viewModel.dialogProperties()
        ) {
            Column {
                BookmarkItem(item = target, clickable = false)
                Text(
                    stringResource(
                        if (target.ignoredUser) R.string.unmute_user_dialog_confirm_msg
                        else R.string.mute_user_dialog_confirm_msg,
                        target.bookmark.user
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun BookmarksScenePreview() {
    val coroutineScope = rememberCoroutineScope()
    BookmarksScene(
        viewModel = FakeBookmarksViewModel(),
        entryActionHandler = FakeEntryActionHandler(),
        ngWordsViewModel = FakeNgWordsViewModel(coroutineScope),
        intent = Intent(),
        savedInstanceState = null
    )
}

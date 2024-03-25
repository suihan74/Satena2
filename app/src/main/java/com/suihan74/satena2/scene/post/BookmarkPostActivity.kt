package com.suihan74.satena2.scene.post

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.entry.EntryItem
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.CombinedIconButton
import com.suihan74.satena2.compose.HorizontalScrollableIndicator
import com.suihan74.satena2.compose.MarqueeText
import com.suihan74.satena2.compose.Tooltip
import com.suihan74.satena2.scene.preferences.page.theme.ThemeViewModelImpl
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.Satena2Theme
import com.suihan74.satena2.utility.extension.putObjectExtra
import com.suihan74.satena2.utility.extension.showToast
import com.suihan74.satena2.utility.extension.toVisibility
import com.suihan74.satena2.utility.focusKeyboardRequester
import com.suihan74.satena2.utility.rememberMutableTextFieldValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant

/** コンテンツの標準的な高さ */
private val DEFAULT_CONTENT_HEIGHT = 154.dp

/** タグリストコンテンツの標準的な高さ */
private val TAGS_LIST_CONTENT_HEIGHT = 300.dp

/** 遷移アニメーションの所要時間 */
private const val TRANSITION_DELAY = 200

@AndroidEntryPoint
class BookmarkPostActivity : ComponentActivity() {
    private val viewModel by viewModels<BookmarkPostViewModelImpl>()

    private val themeViewModel by viewModels<ThemeViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        bindVerticalAlignment(viewModel)
        if (savedInstanceState == null) {
            viewModel.initialize(intent)
            onBackPressedDispatcher.addCallback(this) {
                setCancelResult()
                finish()
            }
        }

        // 投稿完了でアクティビティ終了
        viewModel.bookmarkResultFlow
            .onEach {
                if (it == null) return@onEach
                setSuccessResult(it)
                finish()
            }
            .launchIn(lifecycleScope)

        setContent {
            val theme by themeViewModel.currentThemeFlow.collectAsState()
            Satena2Theme(theme) {
//                BookmarkPostContent(viewModel)
                BookmarkPostPage(viewModel) {
                    setCancelResult()
                    finish()
                }
/*                Box(
                    Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    BookmarkPostContent(viewModel) { finish() }
                }
 */
            }
        }
    }

    // ------ //

    /** ウィンドウ縦位置を設定する */
    private fun bindVerticalAlignment(viewModel: BookmarkPostViewModel) {
/*        viewModel.verticalAlignment
            .onEach { valign ->
                window.attributes.gravity = valign.gravity
            }
            .launchIn(lifecycleScope)
*/
    }

    // ------ //

    /** Activity外側タップ検出時に処理を挟む */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isOutOfBounds(event)) {
            if (viewModel.loadingFlow.value) return false
            if (!viewModel.dismissOnClickOutside.value) return false
            if (event?.action == MotionEvent.ACTION_DOWN) {
                setCancelResult()
            }
        }
        return super.onTouchEvent(event)
    }

    /** Activityの外側をタップしたかを判別する */
    private fun isOutOfBounds(event: MotionEvent?): Boolean {
        if (event == null) return false
        val x = event.x.toInt()
        val y = event.y.toInt()
        val dialogBounds = Rect()
        window.decorView.getHitRect(dialogBounds)
        return !dialogBounds.contains(x, y)
    }

    // ------ //

    private fun setCancelResult() {
        val intent = Intent().also {
            it.putObjectExtra(PostBookmarkActivityContract.RESULT_EDIT_DATA, viewModel.editData())
            it.putObjectExtra<BookmarkResult?>(PostBookmarkActivityContract.RESULT_BOOKMARK, null)
        }
        setResult(RESULT_CANCELED, intent)
    }

    private fun setSuccessResult(result: BookmarkResult) {
        val intent = Intent().also {
            it.putObjectExtra(PostBookmarkActivityContract.RESULT_BOOKMARK, result)
            it.putObjectExtra<EditData?>(PostBookmarkActivityContract.RESULT_EDIT_DATA, null)
        }
        setResult(RESULT_OK, intent)
    }
}

// ------ //

@Composable
private fun BookmarkPostPage(
    viewModel: BookmarkPostViewModel,
    onClickOutSide: ()->Unit = {}
) {
    val contentAlignment by viewModel.verticalAlignment.collectAsState()
    Box(
        contentAlignment = contentAlignment,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .clickable(
                onClick = onClickOutSide,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        BookmarkPostContent(viewModel)
    }
}

@Composable
private fun BookmarkPostContent(
    viewModel: BookmarkPostViewModel,
    mainContentNavController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val entry by viewModel.entryFlow.collectAsState()
    val comment by viewModel.comment.collectAsState()
    val textFieldValue = rememberMutableTextFieldValue(text = comment)

    // 状態
    val loading by viewModel.loadingFlow.collectAsState()
    val calledFromOtherApps by viewModel.calledFromOtherAppsFlow.collectAsState()

    // 投稿前に確認
    val confirmDialogVisible = remember { mutableStateOf(false) }
    val confirmBeforePositing by viewModel.confirmBeforePosting.collectAsState(initial = false)

    // サブ画面の遷移アニメーション
    val transitionAnim = tween<IntOffset>(TRANSITION_DELAY)
    val enterTransition = slideIn(animationSpec = transitionAnim) { IntOffset(it.width, 0) }
    val exitTransition = slideOut(animationSpec = transitionAnim) { IntOffset(it.width, 0) }

    val servicesListState = rememberLazyListState()
    // 各サービス
    val isAuthMastodon by viewModel.isAuthMastodon.collectAsState(initial = false)
    val isAuthTwitter by viewModel.isAuthTwitter.collectAsState(initial = false)
    val isAuthFacebook by viewModel.isAuthFacebook.collectAsState(initial = false)
    val isAuthMisskey by viewModel.isAuthMisskey.collectAsState(initial = false)
    val isPrivate by viewModel.isPrivate.collectAsState()

    // タグリスト
    val tagsScrollState = rememberScrollState()
    val tags by viewModel.tags.collectAsState()
    val tagTextFieldValue = rememberMutableTextFieldValue(text = "")

    // メイン部分のナビゲーション
    val currentBackStackEntry = mainContentNavController.currentBackStackEntryAsState()
    mainContentNavController.setLifecycleOwner(LocalLifecycleOwner.current)
    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher?.let {
        mainContentNavController.setOnBackPressedDispatcher(it)
    }

    LaunchedEffect(Unit) {
        viewModel.comment
            .onEach {
                textFieldValue.value = textFieldValue.value.copy(text = it)
            }
            .launchIn(this)

        snapshotFlow { textFieldValue.value.text }
            .onEach {
                viewModel.comment.value = it
            }
            .launchIn(this)
    }

    ConstraintLayout(
        Modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp,
//                horizontal = 8.dp
            )  // ボタンシャドウが途切れないようにするため
            .clickable(
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )  // コンテンツ部分のクリックを透過しないようにする
    ) {
        val (main, sendButton, tagsArea, guard, progressBar) = createRefs()

        Column(
            Modifier
                .background(CurrentTheme.background, RoundedCornerShape(8.dp))
                .constrainAs(main) {
                    linkTo(
                        top = parent.top,
                        bottom = tagsArea.top,
                        start = parent.start,
                        end = parent.end,
                        topMargin = 8.dp,
                        bottomMargin = 8.dp,
                        startMargin = 25.dp,
                        endMargin = 25.dp
                    )
                    width = Dimension.fillToConstraints
                }
        ) {
            if (calledFromOtherApps) {
                EntryInfo(entry)
            }
            NavHost(
                navController = mainContentNavController,
                startDestination = "default",
                modifier = Modifier.fillMaxWidth()
            ) {
                composable("default") {
                    val focusRequester = focusKeyboardRequester()
                    TextField(
                        value = textFieldValue.value,
                        onValueChange = { textFieldValue.value = it },
                        placeholder = { Text(stringResource(R.string.post_placeholder_comment_text_field)) },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = CurrentTheme.onBackground,
                            placeholderColor = CurrentTheme.grayTextColor,
                            cursorColor = CurrentTheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            trailingIconColor = CurrentTheme.primary
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DEFAULT_CONTENT_HEIGHT)
                            .focusRequester(focusRequester)
                    )
                }

                composable("mastodon",
                    enterTransition = { enterTransition },
                    exitTransition = { exitTransition }
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(DEFAULT_CONTENT_HEIGHT)
                    ) {
                        MastodonSheet(viewModel)
                    }
                }

                composable("misskey",
                    enterTransition = { enterTransition },
                    exitTransition = { exitTransition }
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(DEFAULT_CONTENT_HEIGHT)
                    ) {
                        MisskeySheet(viewModel)
                    }
                }

                composable("tags",
                    enterTransition = { scaleIn(animationSpec = tween(TRANSITION_DELAY)) },
                    exitTransition = { scaleOut(animationSpec = tween(TRANSITION_DELAY)) }
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(TAGS_LIST_CONTENT_HEIGHT)
                    ) {
                        TagsSheet(
                            tags = tags,
                            textFieldValue = textFieldValue,
                            tagTextFieldValue = tagTextFieldValue
                        )
                    }
                }
            }
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(CurrentTheme.primary)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                        top = 6.dp,
                        bottom = 6.dp,
                        end = 60.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .weight(1f)
                ) {
                    LazyRow(
                        state = servicesListState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAuthMisskey && (currentBackStackEntry.routeEquals("misskey", "default"))) {
                            item {
                                IconToggleButton(
                                    iconId = R.drawable.ic_misskey_logo,
                                    contentDescription = "misskey",
                                    tooltipText = stringResource(R.string.post_tooltip_misskey),
                                    enabled = !isPrivate,
                                    flow = viewModel.postMisskey,
                                    onLongClick = {
                                        mainContentNavController.navigateOnce("misskey", currentBackStackEntry)
                                    }
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                        if (isAuthMastodon && (currentBackStackEntry.routeEquals("mastodon", "default"))) {
                            item {
                                IconToggleButton(
                                    iconId = R.drawable.ic_mstdn_logo,
                                    contentDescription = "mastodon",
                                    tooltipText = stringResource(R.string.post_tooltip_mastodon),
                                    enabled = !isPrivate,
                                    flow = viewModel.postMastodon,
                                    onLongClick = {
                                        mainContentNavController.navigateOnce("mastodon", currentBackStackEntry)
                                    }
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                        if (currentBackStackEntry.routeEquals("default")) {
                            if (isAuthTwitter) {
                                item {
                                    IconToggleButton(
                                        iconId = R.drawable.ic_twitter_logo,
                                        contentDescription = "twitter",
                                        tooltipText = stringResource(R.string.post_tooltip_twitter),
                                        enabled = !isPrivate,
                                        flow = viewModel.postTwitter
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                            }
                            if (isAuthFacebook) {
                                item {
                                    IconToggleButton(
                                        iconId = R.drawable.ic_facebook,
                                        contentDescription = "facebook",
                                        tooltipText = stringResource(R.string.post_tooltip_facebook),
                                        enabled = !isPrivate,
                                        flow = viewModel.postFacebook
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                            }
                            item {
                                IconToggleButton(
                                    iconId = R.drawable.ic_share,
                                    contentDescription = "share",
                                    tooltipText = stringResource(R.string.post_tooltip_share),
                                    flow = viewModel.sharing
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            item {
                                IconToggleButton(
                                    iconId = R.drawable.ic_lock,
                                    contentDescription = "private",
                                    tooltipText = stringResource(R.string.post_tooltip_private),
                                    flow = viewModel.isPrivate
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                        if (currentBackStackEntry.routeEquals("tags", "default")) {
                            item {
                                ShareIconButton(
                                    iconId = R.drawable.ic_tag,
                                    contentDescription = "tags",
                                    tooltipText = stringResource(R.string.post_tooltip_tags),
                                    color = CurrentTheme.grayTextColor
                                ) {
                                    coroutineScope.launch {
                                        mainContentNavController.navigateOnce("tags", currentBackStackEntry)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalScrollableIndicator(
                        lazyListState = servicesListState,
                        gradientColor = CurrentTheme.background
                    )
                }
                Spacer(Modifier.width(6.dp))
                if (currentBackStackEntry.routeEquals("default")) {
                    Text(
                        text = viewModel.calcCommentLength(textFieldValue.value.text).toString(),
                        color = CurrentTheme.onBackground,
                        fontSize = 13.sp
                    )
                }
                else if (currentBackStackEntry.routeEquals("tags")) {
                    CombinedIconButton(
                        onClick = {
                            insertTag(
                                viewModel = viewModel,
                                textFieldValue = textFieldValue,
                                tag = tagTextFieldValue.value.text,
                                onSuccess = {
                                    tagTextFieldValue.value = tagTextFieldValue.value.copy(text = "")
                                    coroutineScope.launch {
                                        context.showToast(R.string.post_insert_tag_succeeded)
                                    }
                                },
                                onBlank = {
                                    coroutineScope.launch {
                                        context.showToast(R.string.post_insert_tag_failure)
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "add tags continuously",
                            tint = CurrentTheme.onBackground
                        )
                    }
                }
            }
        }
        // タグリスト
        Row(
            Modifier
                .horizontalScroll(tagsScrollState)
                .constrainAs(tagsArea) {
                    linkTo(
                        top = main.bottom,
                        bottom = parent.bottom,
                        start = parent.start,
                        end = parent.end,
                        topMargin = 6.dp
                    )
                    width = Dimension.fillToConstraints
                    visibility = loading
                        .not()
                        .toVisibility()
                }
        ) {
            Spacer(Modifier.width(23.dp))
            for (tag in tags) {
                Box(
                    Modifier
                        .padding(horizontal = 2.dp)
                        .background(
                            color = CurrentTheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            insertTag(
                                viewModel = viewModel,
                                textFieldValue = textFieldValue,
                                tag = tag.text
                            )
                        }
                ) {
                    Text(
                        text = tag.text,
                        fontSize = 13.sp,
                        color = CurrentTheme.onPrimary,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
            Spacer(Modifier.width(23.dp))
        }

        val interactionSource = remember { MutableInteractionSource() }
        Box(
            Modifier
                .background(
                    color = CurrentTheme.tapGuard,
                    shape = RoundedCornerShape(8.dp)
                )
                .hoverable(interactionSource)
                .constrainAs(guard) {
                    linkTo(
                        top = main.top,
                        bottom = main.bottom,
                        start = main.start,
                        end = main.end
                    )
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                    visibility = loading.toVisibility()
                }
        )
        AnimatedVisibility(
            visible = (!loading && currentBackStackEntry.value != null),
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
//                .padding(start = 24.dp)
                .constrainAs(sendButton) {
                    bottom.linkTo(main.bottom, margin = (-4).dp)
                    end.linkTo(parent.end, margin = 8.dp)
                }
        ) {
            if (currentBackStackEntry.routeEquals("default")) {
                // 投稿ボタン
                FloatingActionButton(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    onClick = {
                        if (confirmBeforePositing) {
                            confirmDialogVisible.value = true
                        }
                        else {
                            coroutineScope.launch {
                                viewModel.postBookmark(context)
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.post_button_text),
                        fontSize = 20.sp,
                    )
                }
            }
            else {
                // サブ画面から戻るボタン
                FloatingActionButton(
                    backgroundColor = CurrentTheme.primary,
                    contentColor = CurrentTheme.onPrimary,
                    onClick = {
                        if (currentBackStackEntry.routeEquals("tags")) {
                            val tag = tagTextFieldValue.value.text
                            insertTag(
                                viewModel = viewModel,
                                textFieldValue = textFieldValue,
                                tag = tag,
                                onBlank = { mainContentNavController.popBackStack() }
                            ) {
                                tagTextFieldValue.value = tagTextFieldValue.value.copy(text = "")
                                mainContentNavController.popBackStack()
                            }
                        }
                        else {
                            mainContentNavController.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            }
        }

        CircularProgressIndicator(
            color = CurrentTheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier
                .constrainAs(progressBar) {
                    linkTo(
                        top = parent.top, bottom = parent.bottom,
                        start = parent.start, end = parent.end
                    )
                    visibility = loading.toVisibility()
                }
        )
    }

    if (confirmDialogVisible.value) {
        val preview = remember(confirmDialogVisible.value) { viewModel.createPreview() }
        if (preview != null) {
            ConfirmDialog(
                preview = preview,
                visible = confirmDialogVisible,
                properties = viewModel.dialogProperties()
            ) {
                coroutineScope.launch {
                    viewModel.postBookmark(context)
                }
            }
        }
    }

    // 処理中は戻るボタンの処理を奪う
    BackHandler(loading) {
        // do nothing on loading
    }
}

@Composable
private fun EntryInfo(entry: Entry?) {
    if (entry == null) return
    Column(
        Modifier.padding(vertical = 3.dp, horizontal = 16.dp)
    ) {
        MarqueeText(
            text = entry.title,
            color = CurrentTheme.onBackground,
            gradientColor = CurrentTheme.background
        )
        MarqueeText(
            text = Uri.decode(entry.url),
            fontSize = 12.sp,
            color = CurrentTheme.grayTextColor,
            gradientColor = CurrentTheme.background
        )
    }
}

// ------ //

@Composable
private fun ShareIconButton(
    @DrawableRes iconId: Int,
    contentDescription: String,
    color: Color,
    tooltipText: String? = null,
    enabled: Boolean = true,
    onLongClick: (()->Unit)? = null,
    onClick: ()->Unit = {}
) {
    val modifier = Modifier
        .size(32.dp)
        .padding(4.dp)

    if (tooltipText != null) {
        val tooltipVisible = remember { mutableStateOf(false) }
        Box {
            CombinedIconButton(
                modifier = modifier,
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick ?: { tooltipVisible.value = true }
            ) {
                Icon(
                    painterResource(id = iconId),
                    contentDescription = contentDescription,
                    tint = color
                )
            }
            Tooltip(expanded = tooltipVisible) {
                Text(text = tooltipText)
            }
        }
    }
    else {
        CombinedIconButton(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick
        ) {
            Icon(
                painterResource(id = iconId),
                contentDescription = contentDescription,
                tint = color
            )
        }
    }
}

@Composable
private fun IconToggleButton(
    @DrawableRes iconId: Int,
    contentDescription: String,
    flow: MutableStateFlow<Boolean>,
    tooltipText: String? = null,
    enabled: Boolean = true,
    onLongClick: (()->Unit)? = null
) {
    val state by flow.collectAsState()
    val color =
        (if (state) CurrentTheme.primary else CurrentTheme.grayTextColor).let {
            if (enabled) it else it.copy(alpha = 0.38f)
        }
    ShareIconButton(
        iconId = iconId,
        contentDescription = contentDescription,
        color = color,
        tooltipText = tooltipText,
        enabled = enabled,
        onClick = { flow.value = !flow.value },
        onLongClick = onLongClick
    )
}

// ------ //

private fun insertTag(
    viewModel: BookmarkPostViewModel,
    textFieldValue: MutableState<TextFieldValue>,
    tag: String,
    onBlank: ()->Unit = {},
    onSuccess: ()->Unit = {}
) {
    if (tag.isBlank()) {
        onBlank()
        return
    }
    runCatching {
        val text = textFieldValue.value.text
        val selection = textFieldValue.value.selection
        viewModel.insertTag(text, tag).let { (idx, after) ->
            val delta = after.length - text.length
            val start = selection.start.let { if (it >= idx) it + delta else it }
            val end = selection.end.let { if (it >= idx) it + delta else it }
            textFieldValue.value =
                textFieldValue.value.copy(
                    text = after,
                    selection = TextRange(start, end)
                )
        }
    }.onSuccess {
        onSuccess.invoke()
    }
}

// ------ //

private fun State<NavBackStackEntry?>.routeEquals(vararg args: String?) : Boolean {
    for (route in args) {
        if (value?.destination?.route == route) return true
    }
    return false
}

private fun NavController.navigateOnce(route: String, currentBackStackEntry: State<NavBackStackEntry?>) {
    if (!currentBackStackEntry.routeEquals(route)) {
        this.navigate(route)
    }
}

// ------ //

@Preview
@Composable
private fun BookmarkPostContentPreview() {
    val viewModel = FakeBookmarkPostViewModel()
    Box(Modifier.fillMaxSize()) {
        BookmarkPostPage(viewModel = viewModel)
    }
}

@Preview
@Composable
private fun BookmarkPostContentFromOtherAppsPreview() {
    val viewModel = FakeBookmarkPostViewModel(
        testEntry = EntryItem(
            title = "test",
            url = "https://localhost",
            eid = 0L,
            description = "",
            count = 1,
            createdAt = Instant.now()
        )
    )
    Box(Modifier.fillMaxSize()) {
        BookmarkPostPage(viewModel = viewModel)
    }
}

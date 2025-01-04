package com.suihan74.satena2.scene.preferences.page.bookmarks

import androidx.compose.ui.Alignment
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.scene.bookmarks.BookmarksTab
import com.suihan74.satena2.scene.bookmarks.OpenCommentLinkTrigger
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface BookmarkViewModel : IPreferencePageViewModel {
    /** 最初に表示するタブ */
    val initialTab : MutableStateFlow<BookmarksTab>

    /** タブ長押しで初期タブを変更する */
    val changeInitialTabByLongClick : MutableStateFlow<Boolean>

    // ------ //
    // リンク

    /** ブコメ中のリンクをクリック時にブラウザで開く */
    val openCommentLinkTrigger : MutableStateFlow<OpenCommentLinkTrigger>

    // ------ //
    // 投稿

    /** ブクマする前に確認する */
    val postConfirmation : MutableStateFlow<Boolean>

    /** ダイアログの表示位置 */
    val postDialogVerticalAlignment : MutableStateFlow<Alignment.Vertical>

    /** 連携の選択状態を保存し引き継ぐ */
    val postSaveStates : MutableStateFlow<Boolean>
}

// ------ //

@HiltViewModel
class BookmarkViewModelImpl @Inject constructor(
    private val repository : PreferencesRepository
) : BookmarkViewModel, PreferencePageViewModel(repository.dataStore)
{
    /** 最初に表示するタブ */
    override val initialTab : MutableStateFlow<BookmarksTab> = prefsStateFlow(BookmarksTab.DIGEST)

    /** タブ長押しで初期タブを変更する */
    override val changeInitialTabByLongClick : MutableStateFlow<Boolean> = prefsStateFlow(true)

    // ------ //

    /** ブコメ中のリンクをクリック時にブラウザで開く */
    override val openCommentLinkTrigger : MutableStateFlow<OpenCommentLinkTrigger> = prefsStateFlow(OpenCommentLinkTrigger.Disabled)

    // ------ //

    /** ブクマする前に確認する */
    override val postConfirmation: MutableStateFlow<Boolean> = prefsStateFlow(true)

    /** ダイアログの表示位置 */
    override val postDialogVerticalAlignment : MutableStateFlow<Alignment.Vertical> = prefsStateFlow(Alignment.CenterVertically)

    /** 連携の選択状態を保存し引き継ぐ */
    override val postSaveStates : MutableStateFlow<Boolean> = prefsStateFlow(true)

    // ------ //

    init {
        repository.dataStore.data
            .onEach {
                initialTab.value = it.bookmarkInitialTab
                changeInitialTabByLongClick.value = it.bookmarkChangeInitialTabByLongClick
                openCommentLinkTrigger.value = it.bookmarkOpenCommentLinkTrigger
                postConfirmation.value = it.postBookmarkConfirmation
                postDialogVerticalAlignment.value = it.postBookmarkDialogVerticalAlignment
                postSaveStates.value = it.postBookmarkSaveStates
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * 値変更に連動してデータストアを更新する`MutableStateFlow`の`EntryViewModel`用のインスタンスを生成する
     */
    private fun <T> prefsStateFlow(initialValue: T) =
        repository.prefsStateFlow("Bookmark", initialValue) { prefs ->
            prefs.copy(
                bookmarkInitialTab = initialTab.value,
                bookmarkChangeInitialTabByLongClick = changeInitialTabByLongClick.value,
                bookmarkOpenCommentLinkTrigger = openCommentLinkTrigger.value,
                postBookmarkConfirmation = postConfirmation.value,
                postBookmarkDialogVerticalAlignment = postDialogVerticalAlignment.value,
                postBookmarkSaveStates = postSaveStates.value
            )
        }
}

// ------ //

class FakeBookmarkViewModel :
    BookmarkViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val initialTab = MutableStateFlow(BookmarksTab.DIGEST)
    override val changeInitialTabByLongClick = MutableStateFlow(true)
    override val openCommentLinkTrigger = MutableStateFlow(OpenCommentLinkTrigger.Disabled)
    override val postConfirmation = MutableStateFlow(true)
    override val postDialogVerticalAlignment = MutableStateFlow(Alignment.CenterVertically)
    override val postSaveStates = MutableStateFlow(true)
}

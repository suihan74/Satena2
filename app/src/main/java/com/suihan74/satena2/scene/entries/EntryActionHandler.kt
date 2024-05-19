package com.suihan74.satena2.scene.entries

import androidx.navigation.NavController
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.satena2.scene.preferences.page.ngWords.dialog.NgWordEditionResult

enum class EntryItemEvent {
    /** 単クリック */
    Click,
    /** 長押し */
    LongClick,
    /** ダブルクリック */
    DoubleClick,
    /** 右端をクリック */
    ClickEdge,
    /** 右端を長押し */
    LongClickEdge,
    /** 右端をダブルクリック */
    DoubleClickEdge,
    /** コメントをクリック */
    ClickComment,
    /** コメントを長押し */
    LongClickComment,
    /** コメントをダブルクリック */
    DoubleClickComment,
}

// ------ //

/**
 * エントリリスト項目に対する操作を処理
 */
interface EntryActionHandler {
    /**
     * 項目の各種クリックイベント処理
     */
    fun onEvent(
        entry: DisplayEntry,
        event: EntryItemEvent,
        onShowMenu: (DisplayEntry)->Unit,
        onShare: (DisplayEntry)->Unit
    )

    // ------ //

    /**
     * ブクマページを開く
     */
    fun launchBookmarksActivity(entry: DisplayEntry)

    /**
     * ブクマページを開き指定ユーザーのブクマを表示する
     */
    fun launchBookmarksActivity(entry: DisplayEntry, user: String)

    /**
     * ブクマ編集画面を開く
     */
    fun launchPostBookmarkActivity(entry: DisplayEntry)

    /**
     * アプリ内ブラウザでページを開く
     */
    fun launchBrowserActivity(entry: DisplayEntry)

    /**
     * 外部アプリでページを開く
     */
    fun openWithOtherApp(entry: DisplayEntry)

    /**
     * 指定サイトのエントリ一覧を表示する
     */
    fun navigateSiteCategory(url: String, navController: NavController)

    /**
     * エントリを「あとで読む」タグをつけてブクマする
     */
    fun readLaterEntry(entry: DisplayEntry, isPrivate: Boolean)

    /**
     * エントリを「読んだ」タグをつけてブクマする
     */
    fun readEntry(entry: DisplayEntry, isPrivate: Boolean)

    /**
     * 既読マークを消す
     */
    fun removeReadMark(entry: DisplayEntry)

    /**
     * ブクマを削除する
     */
    fun removeBookmark(entry: DisplayEntry)

    /**
     * NG設定を追加
     */
    suspend fun insertNgWord(args: NgWordEditionResult) : Boolean

    // ------ //

    /**
     * コメントをクリックしたときの挙動
     */
    fun onClickComment(entry: DisplayEntry, bookmark: BookmarkResult)

    /**
     * コメントを長クリックしたときの挙動
     */
    fun onLongClickComment(entry: DisplayEntry, bookmark: BookmarkResult)
}

// ------ //

/**
 * Compose Preview用のVM
 */
class FakeEntryActionHandler : EntryActionHandler {
    override fun onEvent(
        entry: DisplayEntry,
        event: EntryItemEvent,
        onShowMenu: (DisplayEntry)->Unit,
        onShare: (DisplayEntry)->Unit
    ) {
    }

    // ------ //

    override fun launchBookmarksActivity(entry: DisplayEntry) {
    }

    override fun launchBookmarksActivity(entry: DisplayEntry, user: String) {
    }

    override fun launchPostBookmarkActivity(entry: DisplayEntry) {
    }

    override fun launchBrowserActivity(entry: DisplayEntry) {
    }

    override fun openWithOtherApp(entry: DisplayEntry) {
    }

    override fun navigateSiteCategory(url: String, navController: NavController) {
    }

    override fun readLaterEntry(entry: DisplayEntry, isPrivate: Boolean) {
    }

    override fun readEntry(entry: DisplayEntry, isPrivate: Boolean) {
    }

    override fun removeReadMark(entry: DisplayEntry) {
    }

    override fun removeBookmark(entry: DisplayEntry) {
    }

    // ------ //

    override fun onClickComment(entry: DisplayEntry, bookmark: BookmarkResult) {
    }

    override fun onLongClickComment(entry: DisplayEntry, bookmark: BookmarkResult) {
    }

    // ------ //

    override suspend fun insertNgWord(args: NgWordEditionResult) : Boolean = true
}

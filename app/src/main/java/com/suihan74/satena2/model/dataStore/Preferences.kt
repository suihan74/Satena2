package com.suihan74.satena2.model.dataStore

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.datastore.dataStore
import com.suihan74.satena2.model.hatena.HatenaAccessToken
import com.suihan74.satena2.model.mastodon.MastodonAccessToken
import com.suihan74.satena2.model.mastodon.TootVisibility
import com.suihan74.satena2.model.misskey.NoteVisibility
import com.suihan74.satena2.scene.bookmarks.BookmarksTab
import com.suihan74.satena2.scene.entries.BottomMenuItem
import com.suihan74.satena2.scene.entries.Category
import com.suihan74.satena2.scene.entries.ClickEntryAction
import com.suihan74.satena2.scene.entries.EntryCategoryListType
import com.suihan74.satena2.scene.entries.EntryNavigationState
import com.suihan74.satena2.scene.post.PostStates
import com.suihan74.satena2.scene.post.TagsListOrder
import com.suihan74.satena2.serializer.HatenaAccessTokenSerializer
import com.suihan74.satena2.serializer.HorizontalAlignmentSerializer
import com.suihan74.satena2.serializer.HorizontalArrangementSerializer
import com.suihan74.satena2.serializer.MastodonAccessTokenSerializer
import com.suihan74.satena2.serializer.VerticalAlignmentSerializer
import kotlinx.serialization.Serializable

val Context.preferencesDataStore by dataStore(
    fileName = "preferences",
    serializer = jsonDataStoreSerializer(defaultValue = { Preferences() })
)

// ------ //

/** 現行の設定バージョン */
private const val LATEST_VERSION : Int = 0

/**
 * 設定データ
 */
@PreferenceVersion(LATEST_VERSION)
@Serializable
data class Preferences(
    /** 設定データのバージョン */
    val version : Int = LATEST_VERSION,

    /** はてなのログイン用クッキー */
    @Serializable(with = HatenaAccessTokenSerializer::class)
    val hatenaRK : HatenaAccessToken? = null,

    // ------ //

    /** Mastodonのアクセストークン */
    @Serializable(with = MastodonAccessTokenSerializer::class)
    val mastodonAccessToken : MastodonAccessToken? = null,

    /** Mastodon投稿時の公開範囲 */
    val mastodonPostVisibility : TootVisibility = TootVisibility.Public,

    // ------ //

    /** Misskeyのアクセストークン */
    @Serializable(with = MastodonAccessTokenSerializer::class)
    val misskeyAccessToken : MastodonAccessToken? = null,

    /** Misskey投稿時の公開範囲 */
    val misskeyPostVisibility : NoteVisibility = NoteVisibility.Public,

    // ------ //

    /** 常駐して通知を確認する */
    val backgroundCheckingNoticesEnabled : Boolean = true,

    /** 常駐して通知を確認する間隔（分） */
    val checkingNoticesIntervals : Int = 15,

    /** 同じコメントに対する通知で複数回鳴動する */
    val noticeSameComment : Boolean = true,

    /** 通知鳴動時に既読フラグを更新する */
    val updateReadFlagOnNotification : Boolean = true,

    /** アップデート後にリリースノートを表示する */
    val showingReleaseNotesAfterUpdateEnabled : Boolean = true,

    /** 一度無視したアップデートを再度通知する */
    val noticeUpdateOnceIgnored : Boolean = false,

    /** 外部アプリを開かう際に毎回アプリを選択する */
    val useIntentChooser : Boolean = true,

    /** ダイアログの外側をタップしたら閉じる */
    val dismissDialogOnClickOutside : Boolean = true,

    /** ドロワの表示位置（左or右） */
    @Serializable(with = HorizontalAlignmentSerializer::class)
    val drawerAlignment : Alignment.Horizontal = Alignment.Start,

    /** 長押しでメニューダイアログが表示される場合などの振動の長さ(ミリ秒) */
    val longClickVibrationDuration : Long = 40L,

    /** 日時をシステムのタイムゾーンで表示する */
    val useSystemTimeZone : Boolean = false,

    // ------ //

    /** エントリ: ボトムメニューを使用する */
    val useEntryBottomMenu : Boolean = true,

    /** エントリ: スクロールでボトムメニューを隠す */
    val dismissEntryBottomMenuOnScroll : Boolean = false,

    /** エントリ: ボトムメニューを左右どちらに寄せて並べるか */
    @Serializable(with = HorizontalArrangementSerializer::class)
    val entryBottomMenuArrangement : Arrangement.Horizontal = Arrangement.Start,

    /** エントリ: ボトムメニュー項目 */
    val entryBottomMenuItems : List<BottomMenuItem> = emptyList(),

    /** エントリ: カテゴリリストの表示形式 */
    val entryCategoryListType : EntryCategoryListType = EntryCategoryListType.GRID,

    /** エントリ: 最初に表示するカテゴリ */
    val entryInitialState : EntryNavigationState = EntryNavigationState.default,

    /** エントリ: 各カテゴリで最初に表示するタブ */
    val entryInitialTabs : Map<Category, Int> = emptyMap(),

    /** エントリ: 単クリック時の処理 */
    val clickEntryAction : ClickEntryAction = ClickEntryAction.SHOW_COMMENTS,

    /** エントリ: ロングクリック時の処理 */
    val longClickEntryAction : ClickEntryAction = ClickEntryAction.SHOW_MENU,

    /** エントリ: ダブルクリック時の処理 */
    val doubleClickEntryAction : ClickEntryAction = ClickEntryAction.SHOW_PAGE,

    /** エントリ: 右端を単クリック時の処理 */
    val clickEntryEdgeAction : ClickEntryAction = clickEntryAction,

    /** エントリ: 右端をロングクリック時の処理 */
    val longClickEntryEdgeAction : ClickEntryAction = longClickEntryAction,

    /** エントリ: 右端をダブルクリック時の処理 */
    val doubleClickEntryEdgeAction : ClickEntryAction = doubleClickEntryAction,

    /** エントリ: スクロールでツールバーを隠す */
    val entryHidingToolbarByScroll : Boolean = true,

    // ------ //

    /** ブクマ: 最初に表示するタブ */
    val bookmarkInitialTab : BookmarksTab = BookmarksTab.DIGEST,

    /** ブクマ: タブ長押しで初期タブを変更する */
    val bookmarkChangeInitialTabByLongClick : Boolean = true,

    // ------ //

    /** 投稿: ブクマする前に確認する */
    val postBookmarkConfirmation : Boolean = true,

    /** 投稿: ダイアログの表示位置 */
    @Serializable(with = VerticalAlignmentSerializer::class)
    val postBookmarkDialogVerticalAlignment : Alignment.Vertical = Alignment.CenterVertically,

    /** 投稿: 前回の投稿設定を引き継ぐ */
    val postBookmarkSaveStates : Boolean = true,

    /** 投稿: タグリストの並び順 */
    val postBookmarkTagsListOrder : TagsListOrder = TagsListOrder.INDEX,

    /** 投稿: タグ入力ダイアログをデフォルトで最大展開する */
    val postBookmarkExpandTagsDialogByDefault : Boolean = false,

    /** 投稿: 前回の連携選択状態 */
    val postBookmarkLastStates : PostStates = PostStates(),

    /** 投稿: デフォルトの連携選択状態 */
    val postBookmarkDefaultStates : PostStates = PostStates()
)

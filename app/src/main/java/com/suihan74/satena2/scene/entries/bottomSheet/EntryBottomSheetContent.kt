package com.suihan74.satena2.scene.entries.bottomSheet

/**
 * ボトムシートの表示内容
 */
enum class EntryBottomSheetContent {
    /** 何も選択されていない状態。表示物としては使用しない */
    Empty,
    /** 共有メニュー */
    Share,
    /** サブカテゴリリスト */
    Issues,
    /** エントリメニュー */
    ItemMenu,
    /** エントリコメントのメニュー */
    CommentMenu,
    /** 検索設定 */
    SearchSetting,
    /** マイブクマ検索設定 */
    SearchMyBookmarksSetting,
    /** ブラウザショートカット */
    Browser,
    /** 除外されたエントリリスト */
    ExcludedEntries
}

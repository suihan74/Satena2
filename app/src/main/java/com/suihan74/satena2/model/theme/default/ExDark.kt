package com.suihan74.satena2.model.theme.default

import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.model.theme.ThemeColors
import com.suihan74.satena2.model.theme.ThemePreset

/**
 * "ExDark"テーマ
 */
val DefaultThemePresetExDark = ThemePreset(
    name = "ExDark",
    isSystemDefault = true,
    colors = ThemeColors(
        primary = Color(0x22, 0x78, 0xbd),
        onPrimary = Color(0x00, 0x00, 0x00),
        background = Color(0x00, 0x00, 0x00),
        onBackground = Color(0xd3, 0xd3, 0xd3),
        /** クリック時の色 */
        ripple = Color(0x94, 0x94, 0x94),

        // theme for Satena
        /** タップ防止用ビューの塗りつぶし色 */
        tapGuard = Color(0, 0, 0, 0xa0),
        /** タップ防止用ビュー上の文字色 */
        onTapGuard = Color(0xd3, 0xd3, 0xd3),

        /** タイトルバー背景色を`primary`にする */
        isTitleBarPrimary = false,

        /** ボトムバーの背景色 */
        bottomBarBackground = Color(0x00, 0x00, 0x00),
        /** ボトムバーの文字色 */
        bottomBarOnBackground = Color(0xd3, 0xd3, 0xd3),

        /** ドロワの背景色 */
        drawerBackground = Color(0x00, 0x00, 0x00),
        /** ドロワの文字色 */
        drawerOnBackground = Color(0xd3, 0xd3, 0xd3),

        /** タブの背景色 */
        tabBackground = Color(0x00, 0x00, 0x00),
        /** 選択されたタブの文字色 */
        tabSelectedColor = Color(0x22, 0x78, 0xbd),
        /** 選択されていないタブの文字色 */
        tabUnSelectedColor = Color(0x9f, 0x9f, 0x9f),

        /** リストの分割線の色 */
        listItemDivider = Color(0x9b, 0x9b, 0x9b),

        /** デフォルトではグレーで表示される部分のテキスト色 */
        grayTextColor = Color(0x9b, 0x9b, 0x9b),

        /** `EntryItem`のブクマ数文字色 */
        entryUsers  = Color(0xf9, 0x4e, 0x5b),
        /** `EntryItem`のコメント部分の背景色 */
        entryCommentBackground = Color(0x00, 0x00, 0x00),
        /** `EntryItem`のコメント部分の文字色 */
        entryCommentOnBackground = Color(0xd3, 0xd3, 0xd3)
    )
)

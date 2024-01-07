package com.suihan74.satena2.model.theme.default

import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.model.theme.ThemeColors
import com.suihan74.satena2.model.theme.ThemePreset

/**
 * "Light"テーマ
 */
val DefaultThemePresetLight = ThemePreset(
    name = "Light",
    isSystemDefault = true,
    colors = ThemeColors(
        primary = Color(0x21, 0x96, 0xF3),
        onPrimary = Color(0xff, 0xff, 0xff),
        background = Color(0xee, 0xee, 0xee),
        onBackground = Color(0x22, 0x22, 0x22),
        /** クリック時の色 */
        ripple = Color(0x5e, 0x5e, 0x5e),

        // theme for Satena
        /** タップ防止用ビューの塗りつぶし色 */
        tapGuard = Color(0, 0, 0, 0xa0),
        /** タップ防止用ビュー上の文字色 */
        onTapGuard = Color(0xdf, 0xdf, 0xdf),
        /** ボトムバーの背景色 */
        bottomBarBackground = Color(0xf0, 0xf0, 0xf0),
        /** ボトムバーの文字色 */
        bottomBarOnBackground = Color(0x22, 0x22, 0x22),
        /** ドロワの背景色 */
        drawerBackground = Color(0xee, 0xee, 0xee),
        /** ドロワの文字色 */
        drawerOnBackground = Color(0x22, 0x22, 0x22),
        /** タブの背景色 */
        tabBackground = Color(0xf0, 0xf0, 0xf0),
        /** 選択されたタブの文字色 */
        tabSelectedColor = Color(0x21, 0x96, 0xf3),
        /** 選択されていないタブの文字色 */
        tabUnSelectedColor = Color(0x9f, 0x9f, 0x9f),
        /** リストの分割線の色 */
        listItemDivider = Color(0x88, 0x88, 0x88),

        /** デフォルトではグレーで表示される部分のテキスト色 */
        grayTextColor = Color(0x88, 0x88, 0x88),

        /** `EntryItem`のブクマ数文字色 */
        entryUsers  = Color(0xf9, 0x4e, 0x5b),
        /** `EntryItem`のコメント部分の背景色 */
        entryCommentBackground = Color(0xdd, 0xdd, 0xdd),
        /** `EntryItem`のコメント部分の文字色 */
        entryCommentOnBackground = Color(0x22, 0x22, 0x22)
    )
)

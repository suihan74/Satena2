package com.suihan74.satena2.model.theme.default

import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.model.theme.ThemeColors
import com.suihan74.satena2.model.theme.ThemePreset

/**
 * "Dark"テーマ
 */
val DefaultThemePresetDark = ThemePreset(
    name = "Dark",
    isSystemDefault = true,
    colors = ThemeColors(
        primary = Color(0x1c, 0x79, 0xc4),
        onPrimary = Color(0xff, 0xff, 0xff),
        background = Color(0x38, 0x38, 0x39),
        onBackground = Color(0xdf, 0xdf, 0xdf),
        /** クリック時の色 */
        ripple = Color(0xc0, 0xc0, 0xc0),

        // theme for Satena
        /** タップ防止用ビューの塗りつぶし色 */
        tapGuard = Color(0, 0, 0, 0xa0),
        /** タップ防止用ビュー上の文字色 */
        onTapGuard = Color(0xdf, 0xdf, 0xdf),
        /** ボトムバーの背景色 */
        bottomBarBackground = Color(0x22, 0x22, 0x22),
        /** ボトムバーの文字色 */
        bottomBarOnBackground = Color(0xdf, 0xdf, 0xdf),
        /** ドロワの背景色 */
        drawerBackground = Color(0x22, 0x22, 0x22),
        /** ドロワの文字色 */
        drawerOnBackground = Color(0xdf, 0xdf, 0xdf),
        /** タブの背景色 */
        tabBackground = Color(0x49, 0x49, 0x47),
        /** 選択されたタブの文字色 */
        tabSelectedColor = Color(0x1c, 0x79, 0xc4),
        /** 選択されていないタブの文字色 */
        tabUnSelectedColor = Color(0x9f, 0x9f, 0x9f),
        /** リストの分割線の色 */
        listItemDivider = Color.Gray,

        /** デフォルトではグレーで表示される部分のテキスト色 */
        grayTextColor = Color(0x76, 0x76, 0x76),

        /** `EntryItem`のブクマ数文字色 */
        entryUsers  = Color(0xf9, 0x4e, 0x5b),
        /** `EntryItem`のコメント部分の背景色 */
        entryCommentBackground = Color(0x22, 0x22, 0x22),
        /** `EntryItem`のコメント部分の文字色 */
        entryCommentOnBackground = Color(0xdf, 0xdf, 0xdf)
    )
)

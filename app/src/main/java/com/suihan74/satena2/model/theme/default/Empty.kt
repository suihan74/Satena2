package com.suihan74.satena2.model.theme.default

import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.model.theme.ThemeColors
import com.suihan74.satena2.model.theme.ThemePreset

/**
 * テーマが適用されるまでの間に表示されるテンポラリテーマ
 */
val TemporaryTheme = ThemePreset(
    name = "Empty",
    isSystemDefault = true,
    colors = ThemeColors(
        primary = Color.Black,
        onPrimary = Color.Black,
        background = Color.Black,
        onBackground = Color.Black,
        /** クリック時の色 */
        ripple = Color.Black,

        // theme for Satena
        /** タップ防止用ビューの塗りつぶし色 */
        tapGuard = Color.Black,
        /** タップ防止用ビュー上の文字色 */
        onTapGuard = Color.Black,
        /** ボトムバーの背景色 */
        bottomBarBackground = Color.Black,
        /** ボトムバーの文字色 */
        bottomBarOnBackground = Color.Black,
        /** ドロワの背景色 */
        drawerBackground = Color.Black,
        /** ドロワの文字色 */
        drawerOnBackground = Color.Black,
        /** タブの背景色 */
        tabBackground = Color.Black,
        /** 選択されたタブの文字色 */
        tabSelectedColor = Color.Black,
        /** 選択されていないタブの文字色 */
        tabUnSelectedColor = Color.Black,
        /** リストの分割線の色 */
        listItemDivider = Color.Black,

        /** デフォルトではグレーで表示される部分のテキスト色 */
        grayTextColor = Color.Black,

        /** `EntryItem`のブクマ数文字色 */
        entryUsers  = Color.Black,
        /** `EntryItem`のコメント部分の背景色 */
        entryCommentBackground = Color.Black,
        /** `EntryItem`のコメント部分の文字色 */
        entryCommentOnBackground = Color.Black
    )
)

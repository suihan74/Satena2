@file:UseSerializers(ColorSerializer::class)

package com.suihan74.satena2.model.theme

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.suihan74.satena2.serializer.ColorSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * テーマプリセット
 */
@Entity(
    tableName = "theme_preset"
)
@TypeConverters(
    ThemeColorsConverter::class
)
data class ThemePreset(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L,

    /**
     * アプリ側から提供されるデフォルトのプリセットである
     */
    val isSystemDefault : Boolean = false,

    /**
     * プリセット名
     *
     * 空白不可
     */
    val name : String = "",

    /**
     * 色情報
     */
    val colors : ThemeColors = ThemeColors(),
) {
    companion object {
        /**
         * 現在アプリに適用されているテーマとして扱うID
         */
        const val CURRENT_THEME_ID : Long = 1L
    }

    @Ignore
    val isCurrentTheme = id == CURRENT_THEME_ID
}

// ------ //

/**
 * テーマプリセットの各種色情報
 */
@Serializable
data class ThemeColors(
    val primary : Color = Color.Black,
    val onPrimary : Color = Color.Black,
    val background : Color = Color.Black,
    val onBackground : Color = Color.Black,
    /** クリック時の色 */
    val ripple : Color = Color.Black,

    // theme for Satena
    /** タップ防止用ビューの塗りつぶし色 */
    val tapGuard : Color = Color.Black,
    /** タップ防止用ビュー上の文字色 */
    val onTapGuard : Color = Color.Black,

    /** タイトルバー背景色を`primary`にする */
    val isTitleBarPrimary : Boolean = true,

    /** ボトムバーの背景色 */
    val bottomBarBackground : Color = Color.Black,
    /** ボトムバーの文字色 */
    val bottomBarOnBackground : Color = Color.Black,

    /** ドロワの背景色 */
    val drawerBackground : Color = Color.Black,
    /** ドロワの文字色 */
    val drawerOnBackground : Color = Color.Black,

    /** タブの背景色 */
    val tabBackground : Color = Color.Black,
    /** 選択されたタブの文字色 */
    val tabSelectedColor : Color = Color.Black,
    /** 選択されていないタブの文字色 */
    val tabUnSelectedColor : Color = Color.Black,

    /** リストの分割線の色 */
    val listItemDivider : Color = Color.Black,

    /** デフォルトではグレーで表示される部分のテキスト色 */
    val grayTextColor : Color = Color.Black,

    /** `EntryItem`のブクマ数文字色 */
    val entryUsers : Color = Color.Black,
    /** `EntryItem`のコメント部分の背景色 */
    val entryCommentBackground : Color = Color.Black,
    /** `EntryItem`のコメント部分の文字色 */
    val entryCommentOnBackground : Color = Color.Black,
) {
    /**
     * タイトルバーの背景色
     */
    val titleBarBackground : Color by lazy {
        if (isTitleBarPrimary) primary
        else background
    }

    /**
     * タイトルバーの文字色
     */
    val titleBarOnBackground : Color by lazy {
        if (isTitleBarPrimary) onPrimary
        else onBackground
    }
}

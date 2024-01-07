package com.suihan74.satena2.model.browser

import android.os.Build
import androidx.annotation.StringRes
import com.suihan74.satena2.R
import com.suihan74.satena2.model.TextIdContainer

enum class WebViewTheme(
    val id: Int,
    @StringRes override val textId: Int
) : TextIdContainer {
    /**
     * Satenaのテーマにあわせる
     *
     * LIGHT -> WebViewTheme.NORMAL
     * DARK -> WebViewTheme.DARK
     */
    AUTO(0,
        R.string.browser_webview_theme_auto
    ),

    /** とくに設定しない(メディアクエリなどを渡さずにサイトに任せる) */
    NORMAL(1,
        R.string.browser_webview_theme_normal
    ),

    /** 用意のあるサイトではダークテーマを使用する */
    DARK(2,
        R.string.browser_webview_theme_dark
    ),

    /** 強制的にダークテーマを使用する */
    FORCE_DARK(3,
        R.string.browser_webview_theme_force_dark
    )
    ;

    companion object {
        /**
         * Android13以上では強制ダークテーマは機能しない
         */
        fun values(sdkInt: Int) : Array<WebViewTheme> {
            return if (sdkInt < Build.VERSION_CODES.TIRAMISU) values()
            else arrayOf(AUTO, NORMAL, DARK)
        }
    }
}

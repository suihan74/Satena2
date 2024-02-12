package com.suihan74.satena2.scene.entries

import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContract

/**
 * [EntriesActivity]の呼び出し・戻り値のやりとり
 */
interface EntriesActivityContract {
    /**
     * ユーザー名を引数に呼び出す場合
     */
    class WithUser : ActivityResultContract<String, Unit>() {
        override fun createIntent(context: Context, input: String) : Intent =
            Intent(context, EntriesActivity::class.java).apply {
                putExtra(EXTRA_USER, input)
            }
        override fun parseResult(resultCode: Int, intent: Intent?) {}
    }

    /**
     * URLを引数に呼び出す場合
     */
    class WithUrl : ActivityResultContract<String, Unit>() {
        override fun createIntent(context: Context, input: String) : Intent {
            require(URLUtil.isNetworkUrl(input))
            return Intent(context, EntriesActivity::class.java).apply {
                putExtra(EXTRA_URL, input)
            }
        }
        override fun parseResult(resultCode: Int, intent: Intent?) {}
    }

    /**
     * タグを引数に呼び出す場合
     */
    class WithTag : ActivityResultContract<String, Unit>() {
        override fun createIntent(context: Context, input: String) : Intent =
            Intent(context, EntriesActivity::class.java).apply {
                putExtra(EXTRA_TAG, input)
            }
        override fun parseResult(resultCode: Int, intent: Intent?) {}
    }

    // ------ //

    companion object {
        // Argument keys

        /**
         * ユーザー名
         */
        const val EXTRA_USER = "EntriesActivity.EXTRA_USER"

        /**
         * URL
         */
        const val EXTRA_URL = "EntriesActivity.EXTRA_URL"

        /**
         * タグ
         */
        const val EXTRA_TAG = "EntriesActivity.EXTRA_TAG"

        /**
         * 通知画面を開く
         */
        const val EXTRA_LAUNCH_NOTICES = "EntriesActivity.EXTRA_LAUNCH_NOTICES"
    }
}

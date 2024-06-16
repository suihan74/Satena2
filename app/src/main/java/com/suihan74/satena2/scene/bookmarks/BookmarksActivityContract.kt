package com.suihan74.satena2.scene.bookmarks

import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContract
import com.suihan74.hatena.model.account.Notice
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.utility.extension.getObjectExtra
import com.suihan74.satena2.utility.extension.putObjectExtra

/**
 * [BookmarksActivity]の呼び出し・戻り値のやりとり
 */
interface BookmarksActivityContract {
    interface BookmarksActivityContractImpl {
        fun parseResultImpl(intent: Intent?) : Entry? = intent?.getObjectExtra<Entry>(RESULT_ENTRY)
    }

    // ------ //

    /**
     * エントリを引数に呼び出す場合
     */
    class WithEntry : ActivityResultContract<Entry, Entry?>(), BookmarksActivityContractImpl {
        override fun createIntent(context: Context, input: Entry) : Intent =
            Intent(context, BookmarksActivity::class.java).apply {
                putObjectExtra(EXTRA_ENTRY, input)
            }
        override fun parseResult(resultCode: Int, intent: Intent?) : Entry? = parseResultImpl(intent)
    }

    /**
     * URLを引数に呼び出す場合
     */
    class WithUrl : ActivityResultContract<String, Entry?>(), BookmarksActivityContractImpl {
        override fun createIntent(context: Context, input: String) : Intent {
            require(URLUtil.isNetworkUrl(input))
            return Intent(context, BookmarksActivity::class.java).apply {
                putExtra(EXTRA_URL, input)
            }
        }
        override fun parseResult(resultCode: Int, intent: Intent?) : Entry? = parseResultImpl(intent)
    }

    /**
     * エントリとユーザー名を引数に呼び出す場合
     */
    class WithEntryAndUser : ActivityResultContract<Pair<Entry, String>, Entry?>(), BookmarksActivityContractImpl {
        override fun createIntent(context: Context, input: Pair<Entry, String>) : Intent =
            Intent(context, BookmarksActivity::class.java).apply {
                putObjectExtra(EXTRA_ENTRY, input.first)
                putExtra(EXTRA_TARGET_USER, input.second)
            }
        override fun parseResult(resultCode: Int, intent: Intent?) : Entry? = parseResultImpl(intent)
    }

    class WithNotice : ActivityResultContract<Notice, Entry?>(), BookmarksActivityContractImpl {
        override fun createIntent(context: Context, input: Notice) : Intent =
            Intent(context, BookmarksActivity::class.java).apply {
                putObjectExtra(EXTRA_NOTICE, input)
            }
        override fun parseResult(resultCode: Int, intent: Intent?) : Entry? = parseResultImpl(intent)
    }

    // ------ //

    companion object {
        // Argument keys

        /**
         * ブクマ画面を開くエントリ
         */
        const val EXTRA_ENTRY = "BookmarksActivity.EXTRA_ENTRY"

        /**
         * ブクマ画面を開くURL
         */
        const val EXTRA_URL = "BookmarksActivity.EXTRA_URL"

        /**
         * ブクマ画面を開くエントリID
         */
        const val EXTRA_ENTRY_ID = "BookmarksActivity.EXTRA_ENTRY_ID"

        /**
         * ブクマ画面を開いた直後に詳細を開くユーザー名
         */
        const val EXTRA_TARGET_USER = "BookmarksActivity.EXTRA_TARGET_USER"

        /**
         * ブクマ画面を開く通知エントリ
         */
        const val EXTRA_NOTICE = "BookmarksActivity.EXTRA_NOTICE"

        // ------ //
        // Result keys

        /**
         * キャンセル時: 編集データを返す
         */
        const val RESULT_ENTRY = "BookmarksActivity.RESULT_ENTRY"
    }
}

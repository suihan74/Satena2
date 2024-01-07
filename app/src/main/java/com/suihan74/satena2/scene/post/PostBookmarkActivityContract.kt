package com.suihan74.satena2.scene.post

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.Entry
import com.suihan74.satena2.utility.extension.getObjectExtra
import com.suihan74.satena2.utility.extension.putObjectExtra

/**
 * `BookmarkPostActivity`の呼び出し・戻り値のやりとり
 */
class PostBookmarkActivityContract
    : ActivityResultContract<Pair<Entry, EditData?>, Pair<EditData?, BookmarkResult?>>()
{
    override fun createIntent(context: Context, input: Pair<Entry, EditData?>) =
        Intent(context, BookmarkPostActivity::class.java).apply {
            putObjectExtra(EXTRA_ENTRY, input.first)
            input.second?.let {
                putObjectExtra(EXTRA_EDIT_DATA, it)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?) : Pair<EditData?, BookmarkResult?> = Pair(
        intent?.getObjectExtra<EditData>(RESULT_EDIT_DATA),
        intent?.getObjectExtra<BookmarkResult>(RESULT_BOOKMARK)
    )

    // ------ //

    companion object {
        // Argument keys

        /**
         * ブクマ画面を開くエントリ
         */
        const val EXTRA_ENTRY = "BookmarkPostActivity.EXTRA_ENTRY"

        /**
         * 編集データの初期値
         */
        const val EXTRA_EDIT_DATA = "BookmarkPostActivity.EDIT_DATA"

        // ------ //
        // Result keys

        /**
         * キャンセル時: 編集データを返す
         */
        const val RESULT_EDIT_DATA = "BookmarkPostActivity.EDIT_DATA"

        /**
         * 成功時: 投稿完了したブクマ情報(BookmarkResult)を返す
         */
        const val RESULT_BOOKMARK = "BookmarkPostActivity.RESULT_BOOKMARK"
    }
}

package com.suihan74.satena2.scene.browser

import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContract

/**
 * `BrowserActivity`の呼び出し
 */
class BrowserActivityContract : ActivityResultContract<String?, Unit>() {
    override fun createIntent(context: Context, input: String?) : Intent {
        require(input == null || URLUtil.isNetworkUrl(input))
        return Intent(context, BrowserActivity::class.java).apply {
            input?.let {
                putExtra(EXTRA_URL, it)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        // do nothing
    }

    // ------ //

    companion object {
        /**
         * 最初に開くURL
         */
        const val EXTRA_URL = "BrowserActivity.EXTRA_URL"
    }
}

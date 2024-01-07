package com.suihan74.satena2.scene.preferences

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * `PreferencesActivity`の呼び出し
 */
interface PreferencesActivityContract {
    class NoArgs : ActivityResultContract<Unit, Unit>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, PreferencesActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?) {
            // do nothing
        }
    }

    class WithPage : ActivityResultContract<PreferencesCategory, Unit>() {
        override fun createIntent(context: Context, input: PreferencesCategory): Intent {
            return Intent(context, PreferencesActivity::class.java).apply {
                putExtra(EXTRA_PAGE, input)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?) {
            // do nothing
        }
    }

    // ------ //

    companion object {
        /**
         * 最初に開くページ
         */
        const val EXTRA_PAGE = "PreferencesActivity.EXTRA_PAGE"
    }
}

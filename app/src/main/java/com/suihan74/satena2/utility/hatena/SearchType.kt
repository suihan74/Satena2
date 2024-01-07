package com.suihan74.satena2.utility.hatena

import com.suihan74.hatena.model.entry.SearchType
import com.suihan74.satena2.R

/**
 * [SearchType] 検索対象のラベル文字列ID
 */
val SearchType.textId : Int
    get() = when (this) {
        SearchType.TEXT -> R.string.search_type_text
        SearchType.TAG -> R.string.search_type_tag
    }

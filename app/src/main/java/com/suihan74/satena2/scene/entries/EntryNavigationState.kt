package com.suihan74.satena2.scene.entries

import com.suihan74.hatena.model.entry.Issue
import kotlinx.serialization.Serializable

/**
 * 「最初に表示するタブ」設定用のデータクラス
 */
@Serializable
data class EntryNavigationState(
    /** カテゴリ */
    val category : Category,

    /** サブカテゴリ */
    val issue : Issue? = null
) {
    companion object {
        val default = EntryNavigationState(category = Category.All)
    }
}

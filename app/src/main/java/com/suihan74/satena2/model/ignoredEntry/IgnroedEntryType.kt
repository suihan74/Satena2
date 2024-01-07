package com.suihan74.satena2.model.ignoredEntry

/**
 * 非表示設定を判別する対象（URL or TEXT）
 */
enum class IgnoredEntryType {
    URL,
    TEXT;

    // ------ //

    companion object {
        fun fromOrdinal(idx: Int) = values().getOrElse(idx) { TEXT }
    }
}

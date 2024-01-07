package com.suihan74.satena2.model.ignoredEntry

/**
 * 非表示設定の適用範囲
 */
enum class IgnoreTarget(val id: Int) {
    NONE(0b00),
    ENTRY(0b01),
    BOOKMARK(0b10),
    ALL(0b11);

    // ------ //

    companion object {
        fun fromId(i: Int) = values().first { it.id == i }
    }

    // ------ //

    infix fun or(other: IgnoreTarget) = fromId(id or other.id)
    infix fun contains(other: IgnoreTarget) : Boolean = 0 != (id and other.id)
}

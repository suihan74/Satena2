package com.suihan74.satena2.model

enum class NoticeVerb(
    val code : Int,
    val str : String,
) {
    OTHERS(0b0000_0001, ""),

    ADD_FAVORITE(0b0000_0010, "add_favorite"),
    BOOKMARK(0b0000_0100, "bookmark"),
    STAR(0b0000_1000, "star"),
    FIRST_BOOKMARK(0b0001_0000, "first_bookmark"),
    ;

    companion object {
        val all : Int = entries.sumOf { it.code }
        fun fromInt(int: Int) : List<NoticeVerb> = entries.filter { it.code and int > 0 }
        fun fromStr(str: String) : NoticeVerb = entries.firstOrNull { it.str == str } ?: OTHERS
        fun isOthers(verb: String) = verb.isBlank() || entries.all { it.str != verb }
    }
}

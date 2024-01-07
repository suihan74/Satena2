package com.suihan74.satena2.model

/**
 * テキストIDをもつモデル
 */
interface TextIdContainer {
    /** R.string.* */
    val textId : Int
}

/**
 * アイコンとして扱うドロワブルIDをもつモデル
 */
interface IconIdContainer {
    /** R.drawable.* */
    val iconId : Int
}

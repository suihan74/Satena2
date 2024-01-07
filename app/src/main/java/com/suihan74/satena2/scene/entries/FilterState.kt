package com.suihan74.satena2.scene.entries

enum class FilterState {
    /** 未処理: 未だ判定されていない */
    UNHANDLED,

    /** 有効: 表示対象と判定された */
    VALID,

    /** 無効: フィルタ対象と判定された */
    EXCLUSION,
}

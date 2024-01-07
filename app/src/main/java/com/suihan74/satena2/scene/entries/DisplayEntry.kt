package com.suihan74.satena2.scene.entries

import com.suihan74.hatena.model.entry.Entry
import com.suihan74.hatena.model.star.StarsEntry
import com.suihan74.satena2.model.entries.ReadEntry
import kotlinx.coroutines.flow.StateFlow

/**
 * 表示時に必要な情報を加えたエントリ情報
 */
data class DisplayEntry(
    /**
     * エントリ情報
     */
    val entry : Entry,

    /**
     * 既読状態
     */
    val read : ReadEntry?,

    /**
     * フィルタ結果
     */
    val filterState : FilterState,

    /**
     * エントリへのブクマについたスター情報
     *
     * key => ユーザーID
     */
    val starsEntries : Map<String, StateFlow<StarsEntry?>> = HashMap()
)

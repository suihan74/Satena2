package com.suihan74.satena2.scene.entries

import com.suihan74.hatena.model.entry.Issue

/**
 * 表示対象を決定する情報
 */
data class Destination(
    /**
     * カテゴリ
     */
    val category: Category,
    /**
     * タブ位置
     */
    val tabIndex: Int,
    /**
     * サブカテゴリ
     */
    val issue: Issue? = null,
    /**
     * 特定カテゴリでの追加のターゲット情報
     *
     * e.g. ユーザーブクマカテゴリでの対象ユーザー
     */
    val target: String? = null
)

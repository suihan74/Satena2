package com.suihan74.satena2.model.dataStore

/**
 * プログラム中で扱う設定バージョンを示すためのアノテーション
 *
 * PreferenceVersion := 2, Preferences::version := 1 などの関係の場合、設定データを最新バージョンに更新する必要がある
 */
annotation class PreferenceVersion(
    val version : Int
)

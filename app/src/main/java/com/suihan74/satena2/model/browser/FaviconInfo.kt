package com.suihan74.satena2.model.browser

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * faviconキャッシュ情報
 */
@Entity(
    tableName = "browser_favicon_info",
    indices = [
        Index(value = ["site"], name = "favicon_info_site", unique = true)
    ]
)
data class FaviconInfo(
    /**
     * 対象サイト
     *
     * URLから`Uri#estimatedHierarchy`を使用して次のように変換し格納
     * "https://www.hoge.com/foo/bar" -> "www.hoge.com/foo"
     */
    val site : String,

    /**
     * faviconキャッシュファイル名
     *
     * 変換前ビットマップのハッシュ値
     */
    val filename : String,

    /**
     * 最終更新日時
     */
    val lastUpdated : Instant,

    @PrimaryKey(autoGenerate = true)
    val id : Long = 0
)

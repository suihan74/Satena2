package com.suihan74.satena2.utility.hatena

import android.net.Uri
import com.suihan74.hatena.model.entry.Entry

/**
 * PR用のURLからエントリの実際のURLを取得する
 */
fun Entry.actualUrl() : String {
    if (!this.isPr) return this.url
    val uri = Uri.parse(this.url)
    return uri.getQueryParameter("url")?.let { Uri.decode(it) } ?: this.url
}

package com.suihan74.satena2.utility.hatena

import android.net.Uri
import com.suihan74.hatena.HatenaClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

/**
 * ブックマーク情報が正常に取得できるURLに修正する
 */
suspend fun modifySpecificUrl(url: String?) : String? {
    if (url == null) return null

    val result = runCatching {
        when (val modifiedTemp = modifySpecificUrlWithoutConnection(url)) {
            "about:blank" -> modifiedTemp
            url -> runCatching { modifySpecificUrlForEntry(url) }.getOrNull()
            else -> modifiedTemp
        }
    }

    return removeUtmParameters(result.getOrDefault(url))
}

/**
 * URLからUTMパラメータを除去して返す
 */
private fun removeUtmParameters(url: String?) : String? {
    if (url == null || !url.contains("?")) return url

    val uri = Uri.parse(url)
    val queries = uri.queryParameterNames
        .mapNotNull { key ->
            if (key.startsWith("utm_")) null
            else "$key=${uri.getQueryParameter(key)}"
        }
        .joinToString(separator = "&")

    return buildString {
        append(uri.scheme, "://", uri.host, uri.encodedPath)
        if (queries.isNotEmpty()) {
            append("?", queries)
        }
    }
}


/**
 * 幾つかの頻出するサイトに対して
 * ブックマーク情報が正常に取得できるURLに修正する
 * (OGP検証など通信を必要とする補正は行わない)
 */
private fun modifySpecificUrlWithoutConnection(url: String) : String = when {
    url == "about:blank" -> url

    url.startsWith("https://m.youtube.com/") ->
        Regex("""https://m\.youtube\.com/(.*)""").replace(url) { m ->
            "https://www.youtube.com/${m.groupValues.last()}"
        }

    url.startsWith("https://mobile.twitter.com/") ->
        Regex("""https://mobile\.twitter\.com/(.*)""").replace(url) { m ->
            "https://twitter.com/${m.groupValues.last()}"
        }

    url.startsWith("https://mobile.facebook.com/") ->
        Regex("""https://mobile\.facebook\.com/(.*)""").replace(url) { m ->
            "https://www.facebook.com/${m.groupValues.last()}"
        }

    else -> url
}

/**
 * ブックマーク情報が正常に取得できるURLに修正する
 * (eidから元URLを取得する・OGPタグやTwitterカードなどのmetaタグを参照する)
 */
private suspend fun modifySpecificUrlWithConnection(url: String) : String = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()

        // ヘッダ取得だけで解決する場合
        val requestUrl =
            Request.Builder()
                .head()
                .url(url)
                .build()
                .let { req ->
                    client.newCall(req).execute().use { response ->
                        if (!response.isSuccessful) return@use url
                        val realUrl = response.request.url.let { uri ->
                            buildString { append(uri.scheme, "://", uri.host, uri.encodedPath) }
                        }

                        if (response.header("Content-Type")?.contains("text/html") == true) url
                        else return@withContext realUrl
                    }
                }

        val request = Request.Builder()
            .get()
            .url(requestUrl)
            .build()

        val modified = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use requestUrl

            val realUrl = response.request.url.let { uri ->
                buildString { append(uri.scheme, "://", uri.host, uri.encodedPath) }
            }
            if (response.header("Content-Type")?.contains("text/html") != true) {
                return@use realUrl
            }

            val root = Jsoup.parse(response.body!!.use { it.string() })
            val entryRegex = Regex("""https?://b\.hatena\.ne\.jp/entry/\d+/?$""")
            if (realUrl.startsWith(HatenaClient.baseUrlB+"/entry")) {
                if (entryRegex.matches(realUrl)) {
                    // "https://b.hatena.ne.jp/entry/{eid}"は通常の法則に則ったURLのブコメページにリダイレクトされる
                    // modifySpecificUrls()では、さらにそのブコメページのブコメ先エントリURLに変換する
                    // 例) [in] /entry/18625960 ==> /entry/s/www.google.com ==> https://www.google.com [out]
                    val htmlTag = root.getElementsByTag("html").first()!!
                    // コメントページのURLの場合
                    when (htmlTag.attr("data-page-subtype")) {
                        "comment" -> htmlTag.attr("data-stable-request-url")
                        else -> htmlTag.attr("data-entry-url")
                    }
                }
                else realUrl
            }
            else {
                root.head()
                    .allElements
                    .firstOrNull { elem ->
                        elem.tagName() == "meta" && (elem.attr("property") == "og:url" || elem.attr("name") == "twitter:url")
                    }
                    ?.attr("content")
                    ?: realUrl
            }
        } ?: url

        val modifiedUri = Uri.parse(modified)
        val urlUri = Uri.parse(url)

        if (modifiedUri.scheme != urlUri.scheme && url.removePrefix(urlUri.scheme ?: "") == modified.removePrefix(modifiedUri.scheme ?: "")) url
        else modified
    }
    catch (e: Throwable) {
        url
    }
}

/**
 * URLが既にエントリ登録済みかどうかを確認し、そうであるならそのURLを、
 * 未登録なら妥当なURLを推定して補正する
 */
private suspend fun modifySpecificUrlForEntry(srcUrl: String) : String = withContext(Dispatchers.IO) {
    val modifiedUrl = modifySpecificUrlWithConnection(srcUrl)
    val bookmarkCounts = HatenaClient.bookmark.getBookmarksCount(listOf(srcUrl, modifiedUrl))
    bookmarkCounts.maxByOrNull { it.value }?.key ?: srcUrl
}

// ------ //

/**
 * 与えられたURLに対応するエントリのrootUrlと思われるアドレスを取得する
 *
 * (大体の場合https://domain/)
 */
suspend fun getEntryRootUrl(srcUrl: String) : String {
    val modifiedUrl = modifySpecificUrl(srcUrl) ?: srcUrl

    val twitterRegex = Regex("""^(https://twitter\.com/[a-zA-Z0-9_]+/?)""")
    val twMatch = twitterRegex.find(modifiedUrl)

    val rootUrl =
        when {
            twMatch != null -> twMatch.groupValues[0]

            else -> runCatching {
                val uri = Uri.parse(modifiedUrl)
                uri.scheme + "://" + uri.authority + "/"
            }.getOrDefault(modifiedUrl)
        }

    return rootUrl
}

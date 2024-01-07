package com.suihan74.satena2.utility.extension

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.suihan74.satena2.scene.entries.EntriesActivity
import com.suihan74.satena2.utility.PackageManagerCompat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> Intent.putObjectExtra(key: String?, value: T) {
    this.putExtra(key, Json.encodeToString(value))
}

inline fun <reified T> Intent.putObjectExtra(key: String?, value: T, serializer: KSerializer<T>) {
    this.putExtra(key, Json.encodeToString(serializer, value))
}

inline fun <reified T> Intent.getObjectExtra(key: String?) : T? {
    return this.getStringExtra(key)?.let { Json.decodeFromString(it) }
}


// ------ //

/**
 * URLを開くために"共有先リストからこのアプリを除いた"Intentを作成する
 */
fun Intent.createIntentWithoutThisApplication(
    context: Context,
    title: CharSequence = "Choose a browser",
) : Intent {
    val packageManager = context.packageManager
    val dummyIntent = Intent(this.action, Uri.parse("https://dummy"))
    val useChooser = true // todo

    if (!useChooser) {
        val defaultApp =
            PackageManagerCompat.queryIntentActivities(
                packageManager,
                this,
                PackageManager.MATCH_DEFAULT_ONLY
            ).firstOrNull()

        if (defaultApp != null && defaultApp.activityInfo.packageName != context.packageName) {
            return Intent(this).apply {
                setPackage(defaultApp.activityInfo.packageName)
            }
        }

        val defaultBrowser =
            PackageManagerCompat.queryIntentActivities(
                packageManager,
                dummyIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            ).firstOrNull()

        if (defaultBrowser != null && defaultBrowser.activityInfo.packageName != context.packageName) {
            return Intent(this).apply {
                setPackage(defaultBrowser.activityInfo.packageName)
            }
        }
    }

    val intents =
        PackageManagerCompat.queryIntentActivities(
            packageManager,
            dummyIntent,
            PackageManager.MATCH_ALL
        ).filterNot {
            it.activityInfo.packageName == context.packageName
        }
        .map {
            Intent(this).apply {
                setPackage(it.activityInfo.packageName)
            }
        }

    return when (intents.size) {
        0 -> this
        1 -> intents.first()
        else -> Intent.createChooser(this, title).apply {
            putExtra(Intent.EXTRA_ALTERNATE_INTENTS, intents.toTypedArray())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(
                    ComponentName(context, EntriesActivity::class.java),
                ))
            }
        }
    }
}

/**
 * デフォルトアプリでURLを開くIntentを作成する
 */
fun Intent.createIntentWithDefaultBrowser(context: Context) : Intent? {
    val packageManager = context.packageManager
    val dummyIntent = Intent(this.action, Uri.parse("https://dummy"))
    val resolveInfo = packageManager.resolveActivity(dummyIntent, PackageManager.MATCH_DEFAULT_ONLY)
        ?: return null

    return Intent(this).apply {
        setPackage(resolveInfo.activityInfo.packageName)
    }
}

/**
 * "intent://"スキームなどで外部からのアクティビティ遷移指定を処理する際のセキュリティ対策を行う
 *
 * 外部に公開していないアクティビティを開けないようにする
 */
fun Intent.withSafety() : Intent = this.also {
    it.addCategory(Intent.CATEGORY_BROWSABLE)
    it.component = null
    it.selector?.let { selector ->
        selector.addCategory(Intent.CATEGORY_BROWSABLE)
        selector.component = null
    }
}

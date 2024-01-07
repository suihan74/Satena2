package com.suihan74.satena2.model.dataStore

import androidx.datastore.core.Serializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * 設定データをJsonに変換してDataStoreにぶち込む
 */
inline fun <reified T> jsonDataStoreSerializer(
    crossinline defaultValue: () -> T,
    stringFormat: StringFormat = Json
) = object : Serializer<T> {
    override val defaultValue: T get() = defaultValue()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: T, output: OutputStream) {
        val json = stringFormat.encodeToString(t)
        val bytes = json.encodeToByteArray()
        output.write(bytes)
    }

    override suspend fun readFrom(input: InputStream): T {
        val bytes = input.readBytes()
        val json = bytes.decodeToString()
        return stringFormat.decodeFromString(json)
    }
}

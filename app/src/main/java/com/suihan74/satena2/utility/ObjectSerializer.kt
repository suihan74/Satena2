package com.suihan74.satena2.utility

import android.util.Base64
import java.io.*

/**
 * オブジェクト -> 文字列
 */
class ObjectSerializer<T : Serializable>(private val klass: Class<T>) {
    companion object {
        inline operator fun <reified T : Serializable> invoke() = ObjectSerializer(T::class.java)
    }

    fun serialize(obj: T) : String =
        runCatching {
            val ostream = ByteArrayOutputStream()
            val oos = ObjectOutputStream(ostream)
            oos.use {
                oos.writeObject(obj)
                oos.flush()
                Base64.encodeToString(ostream.toByteArray(), Base64.NO_WRAP)
            }
        }.onFailure {
            throw ClassCastException("failed to serialize from ${klass.name}}")
        }.getOrThrow()

    fun deserialize(str: String) : T =
        runCatching {
            ObjectInputStream(ByteArrayInputStream(Base64.decode(str, Base64.NO_WRAP))).use { ois ->
                val obj = ois.readObject()
                klass.cast(obj) ?: throw Exception()
            }
        }.onFailure {
            throw ClassCastException("failed to deserialize to ${klass.name}}")
        }.getOrThrow()
}

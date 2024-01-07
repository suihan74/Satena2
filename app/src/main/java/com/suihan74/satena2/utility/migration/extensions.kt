package com.suihan74.satena2.utility.migration

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.and

internal fun Int.toByteArray(): ByteArray {
    var value = this
    val result = ByteArray(Int.SIZE_BYTES)
    for (i in 0 until Int.SIZE_BYTES) {
        result[i] = (value and 0xFF).toByte()
        value = value shr Byte.SIZE_BITS
    }
    return result
}

/**
 * @throws ArrayIndexOutOfBoundsException
 */
internal fun ByteArray.toInt(): Int {
    var result = 0
    if (Int.SIZE_BYTES < this.size) {
        throw ArrayIndexOutOfBoundsException("ByteArray overflows when treated as Int")
    }
    for (i in 0 until Int.SIZE_BYTES) {
        val value = (this[i] and  0xFF.toByte()).toInt().let {
            if (it < 0) 256 + it else it
        }
        result = result or (value shl (i * Byte.SIZE_BITS))
    }
    return result
}

// ------ //

/**
 * @throws java.io.IOException
 */
internal fun OutputStream.writeInt(value: Int) {
    this.write(value.toByteArray())
}

/**
 * @throws java.io.IOException
 */
internal fun OutputStream.writeString(value: String) {
    value.toByteArray().let {
        writeInt(it.size)
        write(it)
    }
}

// ------ //

/**
 * @throws java.io.IOException
 * @throws IndexOutOfBoundsException
 */
internal fun InputStream.readInt(): Int {
    val bytes = ByteArray(Int.SIZE_BYTES)
    this.read(bytes)
    return bytes.toInt()
}

/**
 * @throws java.io.IOException
 * @throws IndexOutOfBoundsException
 */
internal fun InputStream.readString(): String {
    val size = readInt()
    val bytes = ByteArray(size)
    this.read(bytes)
    return bytes.toString(Charsets.UTF_8)
}

/**
 * @exception  IOException
 * @exception  IndexOutOfBoundsException
 */
internal fun InputStream.readByteArray(size: Int): ByteArray {
    return ByteArray(size).also {
        read(it, 0, size)
    }
}


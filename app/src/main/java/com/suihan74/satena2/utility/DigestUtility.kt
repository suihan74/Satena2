package com.suihan74.satena2.utility

import java.math.BigInteger
import java.security.MessageDigest

object DigestUtility {
    fun md5Bytes(src: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("MD5")
        md.update(src)
        return md.digest()
    }

    fun md5(src: String) = md5(src.toByteArray())

    fun md5(srcBytes: ByteArray): String {
        val md5bytes = md5Bytes(srcBytes)
        val bigInt = BigInteger(1, md5bytes)
        return bigInt.toString(16)
    }

    // ------ //

    fun sha256(src: String): String = sha256(src.toByteArray())

    fun sha256Bytes(src: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(src)

    fun sha256(srcBytes: ByteArray): String {
        val digest = sha256Bytes(srcBytes)
        return BigInteger(1, digest).toString(16)
    }
}

package com.suihan74.satena2.utility.migration

import java.io.InputStream
import java.io.OutputStream

/**
 * [AppDataMigrator]で扱うデータ
 */
data class MigrationData(
    val type: DataType,
    val key: String,
    val version: Int,
    val filename: String,
    val size: Int,
    val data: ByteArray
) {
    enum class DataType {
        PREFERENCE,
        DATABASE,
        FILE
    }

    // ------ //

    override fun hashCode() = (key.hashCode() + version + filename.hashCode() + size) * 31 + (data.sum())
    override fun equals(other: Any?): Boolean = super.equals(other)

    // ------ //

    fun toByteArray(): ByteArray =
        ArrayList<Byte>().apply {
            addAll(key.toByteArray().toList())
            addAll(version.toByteArray().toList())
            addAll(filename.toByteArray().toList())
            addAll(size.toByteArray().toList())
            addAll(data.toList())
        }.toByteArray()

    /**
     * @throws java.io.IOException
     */
    fun write(stream: OutputStream) = stream.run {
        writeInt(type.ordinal)
        writeString(key)
        writeInt(version)
        writeString(filename)
        writeInt(size)
        write(data)
    }

    // ------ //

    companion object {
        fun read(stream: InputStream): MigrationData = stream.run {
            val type = DataType.entries[readInt()]
            val key = readString()
            val version = readInt()
            val filename = readString()
            val dataSize = readInt()
            val data = readByteArray(dataSize)

            MigrationData(
                type = type,
                key = key,
                version = version,
                filename = filename,
                size = dataSize,
                data = data
            )
        }
    }
}

package com.suihan74.satena2.utility.migration

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.dataStoreFile
import com.suihan74.satena2.hilt.ApplicationModule.appDatabase
import com.suihan74.satena2.utility.DigestUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * アプリデータのインポート/エクスポートをするユーティリティ
 */
class AppDataMigrator {
    companion object {
        private val SIGNATURE = "SAT2SET".toByteArray()
        private val VERSION = byteArrayOf(2)

        private val SIGNATURE_SIZE = SIGNATURE.size
        private const val HASH_SIZE = 16
    }

    // ------ //

    class MigrationFailureException(
        message: String? = null,
        cause: Throwable? = null
    ) : Throwable(message, cause)

    private class BackupFailureException(
        message: String? = null,
        cause: Throwable? = null
    ) : Throwable(message, cause)


    // ------ //

    class Export {
        private val items = ArrayList<MigrationData>()

        /**
         * [targetUri]に対して書き込み実行
         *
         * @throws MigrationFailureException
         *          cause NullPointerException
         *          cause java.io.IOException
         */
        suspend fun write(context: Context, targetUri: Uri) = withContext(Dispatchers.IO) {
            runCatching {
                val headerHash = DigestUtility.md5Bytes(VERSION.plus(items.size.toByteArray()))
                val bodyHash = DigestUtility.md5Bytes(
                    items.flatMap { data ->
                        DigestUtility.md5Bytes(data.toByteArray()).toList()
                    }.toByteArray()
                )

                context.fileOutputStream(targetUri) { stream ->
                    stream.write(SIGNATURE)
                    stream.write(headerHash)
                    stream.write(bodyHash)
                    stream.write(VERSION)
                    stream.writeInt(items.size)
                    items.forEach { it.write(stream) }
                }
            }.onFailure {
                throw MigrationFailureException(cause = it)
            }
        }

        // ------ //

        /**
         * @throws NullPointerException
         * @throws java.io.IOException
         */
        private fun Context.fileOutputStream(uri: Uri, action: (stream: BufferedOutputStream)->Unit) {
            this.contentResolver.run {
                openFileDescriptor(uri, "w")!!.use { d ->
                    FileOutputStream(d.fileDescriptor).buffered().use { stream ->
                        action(stream)
                    }
                }
            }
        }
    }

    // ------ //

    class Import {
        /**
         * [targetUri]からアプリデータ読み込み
         *
         * @throws MigrationFailureException
         *          cause NullPointerException
         *          cause java.io.IOException
         */
        suspend fun read(context: Context, targetUri: Uri) = withContext(Dispatchers.IO) {
            runCatching {
                context.appDatabase.close()

                val path = targetUri.path!!
                context.fileInputStream(targetUri) { stream ->
                    // シグネチャチェック
                    val signature = stream.readByteArray(SIGNATURE_SIZE)
                    check(signature.contentEquals(SIGNATURE)) {
                        "this file is not an appData for Satena2 : $path"
                    }
                    // ハッシュ値
                    val headerHash = stream.readByteArray(HASH_SIZE)
                    val bodyHash = stream.readByteArray(HASH_SIZE)
                    // バージョンチェック
                    val version = stream.readByteArray(1)
                    check(version.contentEquals(VERSION)) {
                        "cannot to read an old appData file: $path"
                    }
                    // 項目数
                    val itemsCount = stream.readInt()
                    // ヘッダのハッシュ値チェック
                    val actualHeaderHash = DigestUtility.md5Bytes(version.plus(itemsCount.toByteArray()))
                    check(actualHeaderHash.contentEquals(headerHash)) {
                        "this file is falsified: $path"
                    }
                    // 項目読み取り
                    val items = ArrayList<MigrationData>(itemsCount)
                    for (i in 0 until itemsCount) {
                        items.add(MigrationData.read(stream))
                    }
                    // データ部分のハッシュ値チェック
                    val actualBodyHash = DigestUtility.md5Bytes(
                        items.flatMap { DigestUtility.md5Bytes(it.toByteArray()).toList() }
                            .toByteArray()
                    )
                    check(actualBodyHash.contentEquals(bodyHash)) {
                        "this file is falsified: $path"
                    }
                    // 現状をバックアップ
                    backup(context)
                    // 読み込んだデータを処理
                    for (item in items) {
                        runCatching {
                            apply(context, item)
                        }.onFailure { e ->
                            Log.e("migration", Log.getStackTraceString(e))
                            throw e
                        }
                    }
                    deleteBackupFiles(context)
                }
            }.onFailure { e ->
                when (e) {
                    is BackupFailureException -> {
                        deleteBackupFiles(context)
                    }
                    else -> {
                        restoreBackupFiles(context)
                    }
                }
                throw MigrationFailureException(cause = e)
            }
        }

        private fun apply(context: Context, data: MigrationData) {
            when (data.type) {
                MigrationData.DataType.PREFERENCE -> applyPreferences(context, data)
                MigrationData.DataType.DATABASE -> applyDatabase(context, data)
                MigrationData.DataType.FILE -> applyFile(context, data)
            }
        }

        private fun applyPreferences(context: Context, data: MigrationData) {
            val file = context.dataStoreFile(data.filename)
            import(file, data)
        }

        private fun applyDatabase(context: Context, data: MigrationData) {
            val file = context.getDatabasePath(data.filename)
            import(file, data)
        }

        /**
         * @throws java.io.IOException
         */
        private fun applyFile(context: Context, data: MigrationData) {
            val file = File(context.filesDir.absolutePath + "/" + data.filename)
            import(file, data)
        }

        /**
         * @throws java.io.IOException
         */
        private fun import(destFile: File, data: MigrationData) {
            if (destFile.parentFile?.exists() != true) {
                destFile.parentFile?.mkdirs()
            }
            destFile.outputStream().buffered().use {
                it.write(data.data)
            }
        }

        // ------ //

        /**
         * 処理前に現在の状態をバックアップする
         */
        private fun backup(context: Context) {
            runCatching {
                val destDir = File(context.filesDir, "backup/")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                backupPreferences(context, destDir)
                backupDatabase(context, destDir)
                // todo
            }.onFailure { e ->
                throw BackupFailureException(cause = e)
            }
        }

        private fun backupPreferences(context: Context, destDir: File) {
            val dest = File(destDir, "datastore/")
            context.dataStoreFile("temp").parentFile?.copyRecursively(dest)
        }

        private fun backupDatabase(context: Context, destDir: File) {
            val dest = File(destDir, "database/")
            context.getDatabasePath("temp").parentFile?.copyRecursively(dest)
        }

        // ------ //

        /**
         * バックアップから復元する
         */
        private fun restoreBackupFiles(context: Context) {
            runCatching {
                val srcDir = File(context.filesDir, "backup/")
                restorePreferencesBackup(context, srcDir)
                restoreDatabaseBackup(context, srcDir)
            }
            // todo
        }

        /**
         * バックアップから設定ファイルを復元する
         */
        private fun restorePreferencesBackup(context: Context, srcDir: File) {
            val src = File(srcDir, "datastore/")
            context.dataStoreFile("temp").parentFile?.let { dest ->
                src.copyRecursively(target = dest, overwrite = true)
            }
        }

        /**
         * バックアップからデータベースファイルを復元する
         */
        private fun restoreDatabaseBackup(context: Context, srcDir: File) {
            val src = File(srcDir, "database/")
            context.getDatabasePath("temp").parentFile?.let { dest ->
                src.copyRecursively(target = dest, overwrite = true)
            }
        }

        /**
         * バックアップを削除する
         */
        private fun deleteBackupFiles(context: Context) {
            runCatching {
                val dir = File(context.filesDir, "backup/")
                if (dir.exists() && dir.isDirectory) {
                    dir.deleteRecursively()
                }
            }.onFailure { e ->
                Log.e("migration_finalize", Log.getStackTraceString(e))
            }
        }

        // ------ //

        /**
         * @throws NullPointerException
         * @throws java.io.IOException
         */
        private fun Context.fileInputStream(uri: Uri, action: (stream: BufferedInputStream)->Unit) {
            this.contentResolver.run {
                openFileDescriptor(uri, "r")!!.use { d ->
                    FileInputStream(d.fileDescriptor).buffered().use { stream ->
                        action(stream)
                    }
                }
            }
        }
    }
}

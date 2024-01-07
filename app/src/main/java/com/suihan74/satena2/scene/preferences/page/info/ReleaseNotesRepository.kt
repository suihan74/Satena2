package com.suihan74.satena2.scene.preferences.page.info

import android.content.Context
import androidx.annotation.RawRes
import com.suihan74.satena2.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.Closeable
import javax.inject.Inject

interface ReleaseNotesRepository {
    /**
     * 現バージョンのリリースノート
     */
    val releaseNotes : StateFlow<List<ReleaseNote>>

    /**
     * 旧バージョン用のリリースノート
     */
    val releaseNotesV1 : StateFlow<List<ReleaseNote>>

    // ------ //

    suspend fun loadReleaseNotes()

    suspend fun loadReleaseNotesV1()
}

// ------ //

class ReleaseNotesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : ReleaseNotesRepository {
    override val releaseNotes = MutableStateFlow<List<ReleaseNote>>(emptyList())

    override val releaseNotesV1 = MutableStateFlow<List<ReleaseNote>>(emptyList())

    // ------ //

    private val releaseNotesReader = releaseNoteReader(context, R.raw.release_notes)

    private val releaseNotesV1Reader = releaseNoteV1Reader(context, R.raw.release_notes_v1)

    // ------ //

    override suspend fun loadReleaseNotes() {
        val newItems = releaseNotesReader.loadNextItems(10)
        if (newItems.isEmpty()) return
        releaseNotes.value = releaseNotes.value.plus(newItems)
    }

    override suspend fun loadReleaseNotesV1() {
        val newItems = releaseNotesV1Reader.loadNextItems(10)
        if (newItems.isEmpty()) return
        releaseNotesV1.value = releaseNotesV1.value.plus(newItems)
    }
}

// ------ //

/**
 * (v2.0系)更新履歴の各項目
 */
data class ReleaseNote(
    val version : String,
    val body : String,
    val timestamp: String
) {
    /**
     * 分割線を表すインスタンスか確認
     */
    val isSeparator : Boolean by lazy { this == ReleaseNoteSeparator }
}

/**
 * 分割線用のインスタンス
 */
private val ReleaseNoteSeparator = ReleaseNote("---", "", "")

// ------ //

private class ReleaseNoteV1Reader(
    private val bufferedReader : BufferedReader,
    private var nextLine : String?,
    private var linesCount : Int,
    private var lastLoadedVersion : String?
) : Closeable {
    private val mutex = Mutex()

    override fun close() = runBlocking {
        mutex.withLock {
            bufferedReader.close()
        }
    }

    private suspend fun readLine() : String? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val result = nextLine
            nextLine = runCatching { bufferedReader.readLine() }.getOrNull()
            if (nextLine != null) linesCount++
            return@withContext result
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadNextItems(
        num: Int,
        lastVersionName: String? = null
    ) : List<ReleaseNote> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!bufferedReader.ready()) return@withContext emptyList()
        }
        if (lastVersionName != null && lastVersionName == lastLoadedVersion) return@withContext emptyList()

        val newItems = buildList {
            val titleRegex = Regex("""^\s*\[\s*version\s*([0-9.]+)\s*]\s*${'$'}""")
            val separatorRegex = Regex("""^----*$""")

            repeat(num) {
                if (nextLine == null) return@repeat
                var insertSeparator = false

                val version = buildString {
                    while (true) {
                        val line = readLine() ?: return@buildList
                        val matchResult = titleRegex.find(line)
                        if (matchResult != null) {
                            val code = matchResult.groupValues[1]
                            // 差分表示時は前回起動時のバージョンまで表示したら処理終了する
                            lastLoadedVersion = code
                            if (lastVersionName == code) {
                                return@buildList
                            }

                            append(code)
                            break
                        }
                    }
                }

                val body = buildString {
                    do {
                        val line = readLine() ?: break
                        if (separatorRegex.matches(line)) {
                            insertSeparator = true
                            break
                        }
                        else {
                            if (line.isNotBlank()) {
                                append(line)
                                append("\n")
                            }
                        }
                    } while (nextLine?.matches(titleRegex) != true)
                    // 文末の改行を削除する
                    this.deleteAt(lastIndex)
                }

                add(ReleaseNote(version, body, ""))

                if (insertSeparator) {
                    add(ReleaseNoteSeparator)
                }
            }
        }
        return@withContext newItems
    }
}

private fun releaseNoteV1Reader(
    context: Context,
    @RawRes rawResId: Int,
) : ReleaseNoteV1Reader {
    val bufferedReader = context.resources.openRawResource(rawResId).bufferedReader()
    return ReleaseNoteV1Reader(
        bufferedReader = bufferedReader,
        nextLine = runCatching { bufferedReader.readLine() }.getOrNull(),
        linesCount = 0,
        lastLoadedVersion = null
    )
}

// ------ //

private class ReleaseNoteReader(
    private val bufferedReader : BufferedReader,
    private var nextLine : String?,
    private var linesCount : Int,
    private var lastLoadedVersion : String?
) : Closeable {
    private val mutex = Mutex()

    override fun close() = runBlocking {
        mutex.withLock {
            bufferedReader.close()
        }
    }

    private suspend fun readLine() : String? {
        mutex.withLock {
            val result = nextLine
            nextLine = runCatching { bufferedReader.readLine() }.getOrNull()
            if (nextLine != null) linesCount++
            return result
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadNextItems(
        num: Int,
        lastVersionName: String? = ""
    ) : List<ReleaseNote> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!bufferedReader.ready()) return@withContext emptyList()
        }
        if (lastVersionName != null && lastVersionName == lastLoadedVersion) return@withContext emptyList()

        val newItems = buildList {
            val titleRegex = Regex("""^\s*\[\s*version\s*([0-9.]+)\s*]\s*(\d\d\d\d-\d\d-\d\d)\s*$""")
            val separatorRegex = Regex("""^----*$""")

            repeat(num) {
                if (nextLine == null) return@repeat
                var insertSeparator = false

                var version = ""
                var timestamp = ""

                while (true) {
                    val line = readLine() ?: break
                    val matchResult = titleRegex.find(line)
                    if (matchResult != null) {
                        val code = matchResult.groupValues[1]
                        val timestampStr = matchResult.groupValues[2]
                        // 差分表示時は前回起動時のバージョンまで表示したら処理終了する
                        lastLoadedVersion = code
                        if (lastVersionName == code) {
                            break
                        }

                        version = code
                        timestamp = timestampStr
                        break
                    }
                }

                val body = buildString {
                    do {
                        val line = readLine() ?: break
                        if (separatorRegex.matches(line)) {
                            insertSeparator = true
                            break
                        }
                        else {
                            if (line.isNotBlank()) {
                                append(line)
                                append("\n")
                            }
                        }
                    } while (nextLine?.matches(titleRegex) != true)
                    // 文末の改行を削除する
                    this.deleteAt(lastIndex)
                }

                add(ReleaseNote(version, body, timestamp))

                if (insertSeparator) {
                    add(ReleaseNote("---", "", ""))
                }
            }
        }
        return@withContext newItems
    }
}

private fun releaseNoteReader(
    context: Context,
    @RawRes rawResId: Int,
) : ReleaseNoteReader {
    val bufferedReader = context.resources.openRawResource(rawResId).bufferedReader()
    return ReleaseNoteReader(
        bufferedReader = bufferedReader,
        nextLine = runCatching { bufferedReader.readLine() }.getOrNull(),
        linesCount = 0,
        lastLoadedVersion = null
    )
}

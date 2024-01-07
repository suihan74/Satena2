package com.suihan74.satena2.model.notice

import androidx.room.*
import com.suihan74.hatena.model.account.Notice
import com.suihan74.satena2.model.NoticeVerb
import java.time.Instant

@Dao
interface NoticeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: NoticeRecord) : Long

    @Update
    suspend fun update(record: NoticeRecord)

    @Delete
    suspend fun delete(record: NoticeRecord)

    @Query("""
        SELECT * FROM notice
        WHERE user = :user AND created = :created AND verb = :verb
        LIMIT 1
    """)
    suspend fun find(user: String, created: Instant, verb: NoticeVerb) : NoticeRecord?

    @Query("""
        SELECT * FROM notice
        WHERE user = :user
        ORDER BY modified DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecords(user: String, offset: Int = 0, limit: Int = 20) : List<NoticeRecord>

    // ------ //

    /**
     * 通知を挿入or更新する
     */
    @Transaction
    suspend fun insert(notice: Notice) : Long {
        return find(notice)?.let {
            val record = it.copy(
                modified = notice.modified,
                notice = notice
            )
            update(record)
            record.id
        } ?: run {
            val record = NoticeRecord(
                user = notice.user,
                created = notice.created,
                modified = notice.modified,
                verb = NoticeVerb.fromStr(notice.verb),
                notice = notice
            )
            insert(record)
        }
    }

    /**
     * 通知が記録済みか確認し、既存ならそれを返す
     */
    suspend fun find(notice: Notice) : NoticeRecord? {
        return find(
            user = notice.user,
            created = notice.created,
            verb = NoticeVerb.fromStr(notice.verb)
        )
    }

}

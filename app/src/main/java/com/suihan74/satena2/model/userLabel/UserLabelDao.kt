package com.suihan74.satena2.model.userLabel

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLabelDao {
    // === get all === //

    @Query("SELECT * FROM user_label ORDER BY id ASC")
    fun allLabelsFlow(): Flow<List<Label>>

    @Query("SELECT * FROM user_label_user ORDER BY id ASC")
    fun allUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM user_label_relation")
    suspend fun getAllRelations(): List<UserLabelRelation>

    // === find === //

    @Query("""
        SELECT * FROM user_label
        WHERE name = :name
        LIMIT 1
    """)
    suspend fun findLabel(name: String) : Label?

    @Query("""
        SELECT * FROM user_label
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun findLabel(id: Long) : Label?

    @Query("""
        SELECT * FROM user_label_user
        WHERE name = :name
        LIMIT 1
    """)
    suspend fun findUser(name: String) : User?

    @Query("""
        SELECT * FROM user_label_user
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun findUser(id: Long) : User?

    @Query("""
        SELECT * FROM user_label_relation
        WHERE label_id = :labelId AND user_id = :userId
        limit 1
    """)
    suspend fun findRelation(labelId: Long, userId: Long) : UserLabelRelation?

    @Query("""
        SELECT * FROM user_label_relation
        WHERE label_id = :labelId
    """)
    suspend fun findRelations(labelId: Long) : List<UserLabelRelation>

    // === insert === //

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLabel(label: Label): Long

    suspend fun insertLabel(name: String): Long = insertLabel(Label(name = name))

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    suspend fun insertUser(name: String): Long = insertUser(User(name = name))

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRelation(relation: UserLabelRelation): Long

    suspend fun insertRelation(label: Label, user: User): Long =
        insertRelation(UserLabelRelation(label = label, user = user))

    // === find or insert === //

    /**
     * ラベルを取得 or 新しく登録して返す
     */
    @Transaction
    suspend fun createLabel(name: String): Label =
        findLabel(name) ?: let {
            val id = insertLabel(name)
            findLabel(id)!!
        }

    /**
     * ユーザーを取得 or 新しく登録して返す
     */
    suspend fun createUser(name: String): User =
        findUser(name) ?: let {
            val id = insertUser(name)
            findUser(id)!!
        }

    // === update === //

    @Update
    suspend fun updateLabel(label: Label)

    // === delete === //

    @Delete
    suspend fun deleteLabel(vararg labels: Label)

    @Delete
    suspend fun deleteUser(vararg users: User)

    @Delete
    suspend fun deleteRelation(vararg relation: UserLabelRelation)

    @Delete
    suspend fun deleteRelations(relations: List<UserLabelRelation>)

    // === get relations === //

    @Transaction
    @Query("""
        SELECT * FROM user_label
        WHERE name = :labelName
        LIMIT 1
    """)
    fun getLabelAndUsers(labelName: String): Flow<LabelAndUsers?>

    @Transaction
    @Query("""
        SELECT * FROM user_label
        WHERE id = :labelId
        LIMIT 1
    """)
    fun getLabelAndUsers(labelId: Long): Flow<LabelAndUsers?>

    @Transaction
    @Query("""
        SELECT * FROM user_label
        WHERE id IN (:labelIds)
    """)
    suspend fun getLabelAndUsers(labelIds: List<Long>): List<LabelAndUsers>

    @Transaction
    @Query("""
        SELECT * FROM user_label_user
        WHERE name = :userName
        LIMIT 1
    """)
    fun getUserAndLabels(userName: String): Flow<UserAndLabels?>

    /**
     * ユーザーにひとつでもラベルがついているか確認する
     */
    @Query("""
        SELECT EXISTS(
            SELECT * FROM user_label_relation
            INNER JOIN user_label_user ON user_id = user_label_user.id
            WHERE user_label_user.name = :userName
        )
    """)
    suspend fun isLabeledUser(userName: String) : Boolean
}

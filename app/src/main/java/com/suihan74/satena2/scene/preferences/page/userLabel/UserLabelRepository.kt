package com.suihan74.satena2.scene.preferences.page.userLabel

import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.model.userLabel.LabelAndUsers
import com.suihan74.satena2.model.userLabel.UserAndLabels
import kotlinx.coroutines.flow.Flow

/**
 * ユーザーラベル操作レポジトリ
 */
interface UserLabelRepository {
    /**
     * すべてのユーザーラベルリスト
     */
    val allUserLabelsFlow : Flow<List<Label>>

    // ------ //

    /**
     * 指定ラベルの所属ユーザーリスト
     */
    fun labeledUsersFlow(label: Label) : Flow<LabelAndUsers?>

    /**
     * ユーザーについているラベルを取得する
     */
    fun userLabelsFlow(user: String) : Flow<UserAndLabels?>

    // ------ //

    /**
     * ラベルを追加
     */
    suspend fun createLabel(label: Label) : Label

    /**
     * ラベルを削除
     */
    suspend fun deleteLabel(label: Label)

    // ------ //

    /**
     * ユーザーラベルを更新する
     */
    suspend fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>)
}

// ------ //

class UserLabelRepositoryImpl(
    appDatabase: AppDatabase,
) : UserLabelRepository {
    private val dao = appDatabase.userLabelDao()

    // ------ //

    /**
     * すべてのユーザーラベルリスト
     */
    override val allUserLabelsFlow by lazy { dao.allLabelsFlow() }

    // ------ //

    /**
     * 指定ラベルの所属ユーザーリスト
     */
    override fun labeledUsersFlow(label: Label) : Flow<LabelAndUsers?> {
        return dao.getLabelAndUsers(label.id)
    }

    /**
     * ユーザーについているラベルを取得する
     */
    override fun userLabelsFlow(user: String) : Flow<UserAndLabels?> {
        return dao.getUserAndLabels(user)
    }

    // ------ //

    /**
     * ラベルを追加
     */
    override suspend fun createLabel(label: Label) : Label {
        val result = runCatching {
            if (label.id == 0L) {
                val id = dao.insertLabel(label.name)
                dao.findLabel(id)!!
            }
            else {
                dao.updateLabel(label)
                dao.findLabel(label.id)!!
            }
        }
        return result.getOrThrow()
    }

    /**
     * ラベルを削除
     */
    override suspend fun deleteLabel(label: Label) {
        runCatching {
            // 該当ラベルを含むリレーションを削除する
            val relations = dao.findRelations(label.id)
            dao.deleteRelations(relations)
            dao.deleteLabel(label)
        }
    }

    // ------ //

    /**
     * ユーザーラベルを更新する
     */
    override suspend fun updateUserLabels(user: String, states: List<Pair<Label, Boolean>>) {
        val userObj = dao.createUser(name = user)
        for (state in states) {
            if (state.second) {
                val relation = dao.findRelation(state.first.id, userObj.id)
                if (relation == null) {
                    dao.insertRelation(state.first, userObj)
                }
            }
            else {
                val relation = dao.findRelation(state.first.id, userObj.id)
                if (relation != null) {
                    dao.deleteRelation(relation)
                }
            }
        }
    }
}

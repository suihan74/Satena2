package com.suihan74.satena2.model.userLabel

import androidx.room.*

/**
 * ユーザーラベル
 */
@Entity(
    tableName = "user_label",
    indices = [
        Index(value = ["name"], name = "label_name", unique = true)
    ]
)
data class Label(
    val name: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)

// ------ //

/**
 * ひとつ以上ラベルがつけられたユーザー
 */
@Entity(
    tableName = "user_label_user",
    indices = [
        Index(value = ["name"], name = "user_name", unique = true)
    ]
)
data class User(
    val name: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)

// ------ //

/**
 * ユーザーがタグ付けされていることを示すリレーション
 */
@Entity(
    tableName = "user_label_relation",
    primaryKeys = ["label_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = Label::class,
            parentColumns = ["id"],
            childColumns = ["label_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "label_id"], name = "relation_label_id_user_id", unique = true)
    ]
)
class UserLabelRelation {
    @ColumnInfo(name = "label_id")
    val labelId: Long

    @ColumnInfo(name = "user_id")
    val userId: Long

    constructor(labelId: Long, userId: Long) {
        this.labelId = labelId
        this.userId = userId
    }

    constructor(label: Label, user: User) {
        this.labelId = label.id
        this.userId = user.id
    }
}

// ------ //

/**
 * ラベルとそのラベルがついたユーザーリスト
 */
class LabelAndUsers {
    @Embedded
    lateinit var userLabel: Label

    @Relation(
        entity = User::class,
        parentColumn = "id",  // Label#id
        entityColumn = "id",  // User#id
        associateBy = Junction(
            value = UserLabelRelation::class,
            parentColumn = "label_id",
            entityColumn = "user_id"
        )
    )
    lateinit var users: List<User>
}

/**
 * ユーザーとそのユーザーについたラベルリスト
 */

class UserAndLabels {
    @Embedded
    lateinit var user: User

    @Relation(
        entity = Label::class,
        parentColumn = "id",  // User#id
        entityColumn = "id",  // Label#id
        associateBy = Junction(
            value = UserLabelRelation::class,
            parentColumn = "user_id",
            entityColumn = "label_id"
        )
    )
    lateinit var labels: List<Label>
}

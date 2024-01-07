package com.suihan74.satena2.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.suihan74.satena2.model.browser.BrowserHistoryDao
import com.suihan74.satena2.model.browser.FaviconInfo
import com.suihan74.satena2.model.browser.HistoryLog
import com.suihan74.satena2.model.browser.HistoryPage
import com.suihan74.satena2.model.entries.ReadEntry
import com.suihan74.satena2.model.entries.ReadEntryDao
import com.suihan74.satena2.model.favoriteSite.FavoriteSite
import com.suihan74.satena2.model.favoriteSite.FavoriteSiteDao
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntryDao
import com.suihan74.satena2.model.notice.NoticeDao
import com.suihan74.satena2.model.notice.NoticeRecord
import com.suihan74.satena2.model.room.converter.InstantConverter
import com.suihan74.satena2.model.theme.ThemeDao
import com.suihan74.satena2.model.theme.ThemePreset
import com.suihan74.satena2.model.userLabel.Label
import com.suihan74.satena2.model.userLabel.User
import com.suihan74.satena2.model.userLabel.UserLabelDao
import com.suihan74.satena2.model.userLabel.UserLabelRelation

@Database(
    entities = [
        FaviconInfo::class,
        FavoriteSite::class,
        HistoryLog::class,
        HistoryPage::class,
        IgnoredEntry::class,
        Label::class,
        NoticeRecord::class,
        ReadEntry::class,
        ThemePreset::class,
        User::class,
        UserLabelRelation::class
    ],
    version = 4
)
@TypeConverters(
    InstantConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ignoredEntryDao(): IgnoredEntryDao
    abstract fun favoriteSiteDao(): FavoriteSiteDao
    abstract fun themeDao(): ThemeDao
    abstract fun readEntryDao(): ReadEntryDao
    abstract fun noticeDao(): NoticeDao
    abstract fun userLabelDao(): UserLabelDao
    abstract fun browserHistoryDao(): BrowserHistoryDao
}

// ------ //

/**
 * [AppDatabase]のマイグレーションを設定する
 */
fun RoomDatabase.Builder<AppDatabase>.migrate(): RoomDatabase.Builder<AppDatabase> {
    return this.apply {
        addMigrations(
            Migration1to2(),
            Migration2to3(),
            Migration3to4()
        ).fallbackToDestructiveMigration()
    }
}

// ------ //

class Migration1to2 : Migration(1, 2) {
    /**
     * ユーザーラベル関連テーブル追加
     */
    override fun migrate(db: SupportSQLiteDatabase) {
        // ラベルテーブル
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_label` (`name` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `label_name` ON `user_label` (`name`)")

        // ユーザーテーブル
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_label_user` (`name` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `user_name` ON `user_label_user` (`name`)")

        // ユーザーラベルリレーションテーブル
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_label_relation` (`label_id` INTEGER NOT NULL, `user_id` INTEGER NOT NULL, PRIMARY KEY(`label_id`, `user_id`), FOREIGN KEY(`label_id`) REFERENCES `user_label`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`user_id`) REFERENCES `user_label_user`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `relation_label_id_user_id` ON `user_label_relation` (`user_id`, `label_id`)")
    }
}

class Migration2to3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM `read_entry`")
        db.execSQL("ALTER TABLE `read_entry` ADD COLUMN `entry` TEXT NOT NULL")
    }
}

class Migration3to4 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `browser_history_items` (`visitedAt` INTEGER NOT NULL, `pageId` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `browser_history_pages` (`url` TEXT NOT NULL, `title` TEXT NOT NULL, `lastVisited` INTEGER NOT NULL, `visitTimes` INTEGER NOT NULL, `faviconInfoId` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
    }
}

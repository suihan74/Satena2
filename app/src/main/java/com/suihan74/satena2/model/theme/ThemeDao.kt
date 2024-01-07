package com.suihan74.satena2.model.theme

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 使用できない名前のプリセットを作成しようとした
 */
class InvalidThemePresetNameError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)

/**
 * 重複したプリセットを追加しようとした
 */
class ThemePresetDuplicationError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)

// ------ //

@Dao
interface ThemeDao {
    /**
     * プリセットの一覧を取得
     */
    @Query("SELECT * FROM theme_preset WHERE id != ${ThemePreset.CURRENT_THEME_ID} ORDER BY isSystemDefault DESC, id ASC")
    fun allPresetsFlow() : Flow<List<ThemePreset>>

    /**
     * 名前が一致するプリセットを取得
     */
    @Query("SELECT * FROM theme_preset WHERE id != ${ThemePreset.CURRENT_THEME_ID} AND name = :name LIMIT 1")
    suspend fun preset(name: String) : ThemePreset?

    /**
     * 現在有効なアプリテーマを取得
     */
    @Query("SELECT * FROM theme_preset WHERE id = ${ThemePreset.CURRENT_THEME_ID}")
    fun currentThemeFlow() : Flow<ThemePreset>

    @Query("SELECT id FROM theme_preset WHERE id != ${ThemePreset.CURRENT_THEME_ID} AND name = :name LIMIT 1")
    suspend fun exists(name: String) : Long?

    @Query("SELECT id FROM theme_preset WHERE id = :id LIMIT 1")
    suspend fun exists(id: Long) : Long?

    // ------ //

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun __insert(preset: ThemePreset) : Long

    /**
     * プリセットを追加
     *
     * @throws InvalidThemePresetNameError プリセット名が空白
     * @throws ThemePresetDuplicationError プリセット名が重複
     */
    @Transaction
    suspend fun insert(preset: ThemePreset) : Long = withNameCheck(preset) { __insert(it) }

    // ------ //

    /**
     * プリセットを更新
     *
     * @throws InvalidThemePresetNameError プリセット名が空白
     * @throws ThemePresetDuplicationError プリセット名が他のプリセットと重複
     */
    @Update
    suspend fun update(preset: ThemePreset)

    /**
     * 現在有効なアプリテーマを更新
     */
    @Transaction
    suspend fun updateCurrentTheme(preset: ThemePreset) {
        update(
            preset.copy(
                id = ThemePreset.CURRENT_THEME_ID,
                name = "",
                isSystemDefault = false
            )
        )
    }

    // ------ //

    @Delete
    suspend fun delete(preset: ThemePreset)
}

/**
 * プリセット名を検証後にクエリ実行
 *
 * @throws InvalidThemePresetNameError プリセット名が空白
 * @throws ThemePresetDuplicationError プリセット名が重複
 */
private suspend inline fun <T> ThemeDao.withNameCheck(
    preset: ThemePreset,
    crossinline onSuccess: suspend (ThemePreset)->T
) : T {
    if (preset.name.isBlank()) {
        throw InvalidThemePresetNameError()
    }
    exists(preset.name)?.let { existId ->
        if (existId != preset.id) {
            throw ThemePresetDuplicationError()
        }
    }
    return onSuccess(preset)
}

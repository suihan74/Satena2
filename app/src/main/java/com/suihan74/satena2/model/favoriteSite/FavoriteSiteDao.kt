package com.suihan74.satena2.model.favoriteSite

import androidx.room.*
import com.suihan74.satena2.model.browser.FaviconInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSiteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteSite: FavoriteSite) : Long

    // --- //

    @Update
    suspend fun update(vararg favoriteSites: FavoriteSite)

    @Update
    suspend fun update(favoriteSites: List<FavoriteSite>)

    // --- //

    @Transaction
    @Query("""SELECT * FROM favorite_site""")
    fun allFavoriteSitesFlow() : Flow<List<FavoriteSiteAndFavicon>>

    // --- //

    @Query("SELECT EXISTS (SELECT * FROM favorite_site WHERE url = :url)")
    suspend fun exists(url: String) : Boolean

    @Transaction
    @Query("SELECT * FROM favorite_site WHERE url = :url")
    suspend fun findFavoriteSite(url: String) : FavoriteSiteAndFavicon?

    @Query("SELECT * FROM favorite_site WHERE faviconInfoId = 0")
    suspend fun findItemsFaviconInfoNotSet() : List<FavoriteSite>

    // --- //

    @Delete
    suspend fun delete(favoriteSite: FavoriteSite)

    @Query("DELETE FROM favorite_site WHERE url = :url")
    suspend fun delete(url: String)

    // ------ //

    @Query("SELECT * FROM browser_favicon_info WHERE site = :site")
    suspend fun findFaviconInfo(site: String) : FaviconInfo?
}

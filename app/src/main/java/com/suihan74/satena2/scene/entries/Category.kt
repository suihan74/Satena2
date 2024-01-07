package com.suihan74.satena2.scene.entries

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.suihan74.satena2.R
import com.suihan74.satena2.model.IconIdContainer
import com.suihan74.satena2.model.TextIdContainer

typealias HatenaCategory = com.suihan74.hatena.model.entry.Category

/**
 * エントリリストのカテゴリ
 */
@Suppress("unused")
enum class Category(
    @StringRes override val textId : Int,
    @DrawableRes override val iconId : Int,
    val hatenaCategory : HatenaCategory? = null,
    val requireSignedIn : Boolean = false,
    val singleTab : Boolean = false,
    val hasIssues : Boolean = false,
    val displayInList : Boolean = true,
    val willBeHome : Boolean = true,
    val canHideReadEntries : Boolean = true
) : TextIdContainer, IconIdContainer {
    All(
        R.string.category_all,
        R.drawable.ic_category_all,
        hatenaCategory = HatenaCategory.ALL
    ),

    General(
        R.string.category_general,
        R.drawable.ic_category_general,
        hatenaCategory = HatenaCategory.GENERAL
    ),

    Social(
        R.string.category_social,
        R.drawable.ic_category_social,
        hatenaCategory = HatenaCategory.SOCIAL,
        hasIssues = true
    ),

    Economics(
        R.string.category_economics,
        R.drawable.ic_category_economics,
        hatenaCategory = HatenaCategory.ECONOMICS,
        hasIssues = true
    ),

    Life(
        R.string.category_life,
        R.drawable.ic_category_life,
        hatenaCategory = HatenaCategory.LIFE,
        hasIssues = true
    ),

    Knowledge(
        R.string.category_knowledge,
        R.drawable.ic_category_knowledge,
        hatenaCategory = HatenaCategory.KNOWLEDGE,
        hasIssues = true
    ),

    It(
        R.string.category_it,
        R.drawable.ic_category_it,
        hatenaCategory = HatenaCategory.IT,
        hasIssues = true
    ),

    Entertainment(
        R.string.category_entertainment,
        R.drawable.ic_category_entertainment,
        hatenaCategory = HatenaCategory.ENTERTAINMENT,
        hasIssues = true
    ),

    Game(
        R.string.category_game,
        R.drawable.ic_category_game,
        hatenaCategory = HatenaCategory.GAME,
        hasIssues = true
    ),

    Fun(
        R.string.category_fun,
        R.drawable.ic_category_fun,
        hatenaCategory = HatenaCategory.FUN,
        hasIssues = true
    ),

    MyHotEntries(
        R.string.category_myhotentries,
        R.drawable.ic_category_myhotentries,
        requireSignedIn = true,
        singleTab = true
    ),

    MyBookmarks(
        R.string.category_mybookmarks,
        R.drawable.ic_mybookmarks,
        requireSignedIn = true
    ),

    Followings(
        R.string.category_followings,
        R.drawable.ic_category_followings,
        requireSignedIn = true,
        singleTab = true
    ),

    FavoriteSites(
        R.string.category_favorite_sites,
        R.drawable.ic_category_favorite_sites
    ),

    Search(
        R.string.category_search,
        R.drawable.ic_category_search,
        singleTab = false
    ),

    Stars(
        R.string.category_mystars,
        R.drawable.ic_star,
        requireSignedIn = true
    ),

    Memorial15th(
        R.string.category_memorial15,
        R.drawable.ic_category_memorial,
        requireSignedIn = false,
        willBeHome = false
    ),

    Maintenance(
        R.string.category_maintenance,
        R.drawable.ic_category_maintenance,
        requireSignedIn = false,
        singleTab = true,
        canHideReadEntries = false
    ),

    History(
        R.string.category_history,
        R.drawable.ic_category_history,
        requireSignedIn = false,
        singleTab = true,
        canHideReadEntries = false
    ),

    Site(
        R.string.category_site,
        R.drawable.ic_category_site,
        displayInList = false,
        willBeHome = false
    ),

    User(
        R.string.category_user,
        0,
        singleTab = true,
        displayInList = false,
        willBeHome = false
    ),

    Notices(
        R.string.category_notices,
        R.drawable.ic_notifications,
        requireSignedIn = true,
        singleTab = true,
        displayInList = true,
        willBeHome = false,
        canHideReadEntries = false
    ),

    SearchMyBookmarks(
        R.string.category_search_mybookmarks,
        R.drawable.ic_category_search,
        requireSignedIn = true,
        singleTab = true,
        displayInList = false,
        willBeHome = false,
        canHideReadEntries = false
    )

    ;

    companion object {
        fun valuesWithSignedIn() = values().filter { it.displayInList }.toTypedArray()
        fun valuesWithoutSignedIn() = values().filterNot { !it.displayInList || it.requireSignedIn }.toTypedArray()
    }

    val code: String? by lazy { hatenaCategory?.code }

    /**
     * カテゴリに対応するタブ名を取得する
     */
    fun tabs(context: Context) : List<String> = when {
        singleTab -> emptyList()

        MyBookmarks == this -> listOf(context.getString(R.string.my_bookmarks), context.getString(R.string.read_later))

        Stars == this -> listOf(context.getString(R.string.recent_stars), context.getString(R.string.stars_report))

        Memorial15th == this -> (2005..2020).map { it.toString() }

        else -> listOf(context.getString(R.string.hot), context.getString(R.string.recent))
    }
}

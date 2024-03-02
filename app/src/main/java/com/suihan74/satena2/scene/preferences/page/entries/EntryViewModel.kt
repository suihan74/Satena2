package com.suihan74.satena2.scene.preferences.page.entries

import androidx.compose.foundation.layout.Arrangement
import androidx.lifecycle.viewModelScope
import com.suihan74.satena2.scene.entries.BottomMenuItem
import com.suihan74.satena2.scene.entries.Category
import com.suihan74.satena2.scene.entries.ClickEntryAction
import com.suihan74.satena2.scene.entries.EntryCategoryListType
import com.suihan74.satena2.scene.entries.EntryNavigationState
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.page.FakePreferencesPageViewModelImpl
import com.suihan74.satena2.scene.preferences.page.IPreferencePageViewModel
import com.suihan74.satena2.scene.preferences.page.PreferencePageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface EntryViewModel : IPreferencePageViewModel {
    /**
     * ボトムメニューを使用する
     */
    val useBottomMenu : MutableStateFlow<Boolean>

    /**
     * ボトムメニューの配置
     */
    val bottomMenuArrangement : MutableStateFlow<Arrangement.Horizontal>

    /**
     * ボトムメニューの項目リスト
     */
    val bottomMenuItems : MutableStateFlow<List<BottomMenuItem>>

    /**
     * カテゴリリストの表示形式
     */
    val categoryListType : MutableStateFlow<EntryCategoryListType>

    /**
     * 最初に表示するカテゴリ
     */
    val initialState : MutableStateFlow<EntryNavigationState>

    /**
     * 最初に表示するタブ
     */
    val initialTabs : MutableStateFlow<Map<Category, Int>>

    /**
     * MyBookmarksカテゴリで非表示エントリを表示する
     */
    val ignoredEntriesVisibilityInMyBookmarks : MutableStateFlow<Boolean>

    /**
     * 既読エントリを記録する
     */
    val recordReadEntriesEnabled : MutableStateFlow<Boolean>

    // ------ //

    /**
     * エントリ項目を単クリックしたときの処理
     */
    val clickEntryAction : MutableStateFlow<ClickEntryAction>

    /**
     * エントリ項目をロングクリックしたときの処理
     */
    val longClickEntryAction : MutableStateFlow<ClickEntryAction>

    /**
     * エントリ項目をダブルクリックしたときの処理
     */
    val doubleClickEntryAction : MutableStateFlow<ClickEntryAction>

    /**
     * エントリ項目右端を単クリックしたときの処理
     */
    val clickEntryEdgeAction : MutableStateFlow<ClickEntryAction>

    /**
     * エントリ項目右端をロングクリックしたときの処理
     */
    val longClickEntryEdgeAction : MutableStateFlow<ClickEntryAction>

    /**
     * エントリ項目右端をダブルクリックしたときの処理
     */
    val doubleClickEntryEdgeAction : MutableStateFlow<ClickEntryAction>

    /**
     * ボトムメニュー項目リストを編集
     */
    fun setBottomMenuItems(index: Int, item: BottomMenuItem?)
}

// ------ //

@HiltViewModel
class EntryViewModelImpl @Inject constructor(
    private val repository : PreferencesRepository
) : EntryViewModel, PreferencePageViewModel(repository.dataStore)
{
    /**
     * ボトムメニューを使用する
     */
    override val useBottomMenu : MutableStateFlow<Boolean> = prefsStateFlow(true)

    /**
     * ボトムメニューの配置
     */
    override val bottomMenuArrangement : MutableStateFlow<Arrangement.Horizontal> = prefsStateFlow(Arrangement.Start)

    /**
     * ボトムメニューの項目リスト
     */
    override val bottomMenuItems : MutableStateFlow<List<BottomMenuItem>> = prefsStateFlow(emptyList())

    /**
     * カテゴリリストの表示形式
     */
    override val categoryListType : MutableStateFlow<EntryCategoryListType> = prefsStateFlow(
        EntryCategoryListType.LIST)

    /**
     * 最初に表示するカテゴリ
     */

    override val initialState : MutableStateFlow<EntryNavigationState> = prefsStateFlow(EntryNavigationState.default)

    /**
     * 最初に表示するタブ
     */
    override val initialTabs : MutableStateFlow<Map<Category, Int>> = prefsStateFlow(emptyMap())

    /**
     * MyBookmarksカテゴリで非表示エントリを表示する
     */
    override val ignoredEntriesVisibilityInMyBookmarks : MutableStateFlow<Boolean> = prefsStateFlow(true)

    /**
     * 既読エントリを記録する
     */
    override val recordReadEntriesEnabled : MutableStateFlow<Boolean> = prefsStateFlow(true)

    // ------ //

    /**
     * エントリ項目を単クリックしたときの処理
     */
    override val clickEntryAction : MutableStateFlow<ClickEntryAction> = prefsStateFlow(ClickEntryAction.SHOW_COMMENTS)

    /**
     * エントリ項目をロングクリックしたときの処理
     */
    override val longClickEntryAction : MutableStateFlow<ClickEntryAction> = prefsStateFlow(ClickEntryAction.SHOW_MENU)

    /**
     * エントリ項目をダブルクリックしたときの処理
     */
    override val doubleClickEntryAction : MutableStateFlow<ClickEntryAction> = prefsStateFlow(ClickEntryAction.SHOW_PAGE)

    /**
     * エントリ項目を単クリックしたときの処理
     */
    override val clickEntryEdgeAction : MutableStateFlow<ClickEntryAction> = prefsStateFlow(ClickEntryAction.SHOW_COMMENTS)

    /**
     * エントリ項目をロングクリックしたときの処理
     */
    override val longClickEntryEdgeAction : MutableStateFlow<ClickEntryAction> = prefsStateFlow(ClickEntryAction.SHOW_MENU)

    /**
     * エントリ項目をダブルクリックしたときの処理
     */
    override val doubleClickEntryEdgeAction : MutableStateFlow<ClickEntryAction> = prefsStateFlow(ClickEntryAction.SHOW_PAGE)

    // ------ //

    init {
        repository.dataStore.data
            .onEach {
                useBottomMenu.value = it.useEntryBottomMenu
                bottomMenuArrangement.value = it.entryBottomMenuArrangement
                bottomMenuItems.value = it.entryBottomMenuItems
                categoryListType.value = it.entryCategoryListType
                initialState.value = it.entryInitialState
                initialTabs.value = it.entryInitialTabs
                ignoredEntriesVisibilityInMyBookmarks.value = it.ignoredEntriesVisibilityInMyBookmarks
                recordReadEntriesEnabled.value = it.recordReadEntriesEnabled

                clickEntryAction.value = it.clickEntryAction
                longClickEntryAction.value = it.longClickEntryAction
                doubleClickEntryAction.value = it.doubleClickEntryAction
                clickEntryEdgeAction.value = it.clickEntryEdgeAction
                longClickEntryEdgeAction.value = it.longClickEntryEdgeAction
                doubleClickEntryEdgeAction.value = it.doubleClickEntryEdgeAction
            }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * 値変更に連動してデータストアを更新する`MutableStateFlow`の`EntryViewModel`用のインスタンスを生成する
     */
    private fun <T> prefsStateFlow(initialValue: T) =
        repository.prefsStateFlow("Entry", initialValue) { prefs ->
            prefs.copy(
                useEntryBottomMenu = useBottomMenu.value,
                entryBottomMenuArrangement = bottomMenuArrangement.value,
                entryBottomMenuItems = bottomMenuItems.value,
                entryCategoryListType = categoryListType.value,
                entryInitialState = initialState.value,
                entryInitialTabs = initialTabs.value,
                ignoredEntriesVisibilityInMyBookmarks = ignoredEntriesVisibilityInMyBookmarks.value,
                recordReadEntriesEnabled = recordReadEntriesEnabled.value,
                clickEntryAction = clickEntryAction.value,
                longClickEntryAction = longClickEntryAction.value,
                doubleClickEntryAction = doubleClickEntryAction.value,
                clickEntryEdgeAction = clickEntryEdgeAction.value,
                longClickEntryEdgeAction = longClickEntryEdgeAction.value,
                doubleClickEntryEdgeAction = doubleClickEntryEdgeAction.value
            )
        }

    // ------ //

    /**
     * ボトムメニュー項目リストを編集
     */
    override fun setBottomMenuItems(index: Int, item: BottomMenuItem?) {
        bottomMenuItems.value = item?.let { modifyBottomMenuItems(index, it) } ?: removeBottomMenuItems(index)
    }

    /**
     * 指定位置のボトムメニューを追加・変更
     */
    private fun modifyBottomMenuItems(index: Int, item: BottomMenuItem) : List<BottomMenuItem> {
        return bottomMenuItems.value.let { prevItems ->
            if (index < 0) {
                // 追加
                buildList {
                    add(item)
                    addAll(prevItems.filterNot { it.id == item.id })
                }
            }
            else {
                // 変更
                prevItems.mapIndexedNotNull { idx, value ->
                    if (idx != index) {
                        if (value.id == item.id) null
                        else value
                    }
                    else item
                }
            }
        }
    }

    /**
     * 指定位置のボトムメニューを削除
     */
    private fun removeBottomMenuItems(index: Int) : List<BottomMenuItem> {
        return buildList {
            bottomMenuItems.value.let { items ->
                if (index > 0) {
                    addAll(items.subList(0, index))
                }
                if (index < items.lastIndex) {
                    addAll(items.subList(index + 1, items.lastIndex + 1))
                }
            }
        }
    }
}

// ------ //

class FakeEntryViewModel :
    EntryViewModel,
    IPreferencePageViewModel by FakePreferencesPageViewModelImpl()
{
    override val useBottomMenu = MutableStateFlow(true)
    override val bottomMenuArrangement = MutableStateFlow(Arrangement.Start)
    override val bottomMenuItems = MutableStateFlow<List<BottomMenuItem>>(emptyList())
    override val categoryListType = MutableStateFlow(EntryCategoryListType.LIST)
    override val initialState = MutableStateFlow(EntryNavigationState.default)
    override val initialTabs = MutableStateFlow<Map<Category, Int>>(emptyMap())
    override val ignoredEntriesVisibilityInMyBookmarks = MutableStateFlow(true)
    override val recordReadEntriesEnabled = MutableStateFlow(true)
    override val clickEntryAction = MutableStateFlow(ClickEntryAction.SHOW_COMMENTS)
    override val longClickEntryAction = MutableStateFlow(ClickEntryAction.SHOW_MENU)
    override val doubleClickEntryAction = MutableStateFlow(ClickEntryAction.SHOW_PAGE)
    override val clickEntryEdgeAction = MutableStateFlow(ClickEntryAction.SHOW_COMMENTS)
    override val longClickEntryEdgeAction = MutableStateFlow(ClickEntryAction.SHOW_MENU)
    override val doubleClickEntryEdgeAction = MutableStateFlow(ClickEntryAction.SHOW_PAGE)

    // ------ //

    override fun setBottomMenuItems(index: Int, item: BottomMenuItem?) {
    }
}

package com.suihan74.satena2.scene.entries

import com.suihan74.satena2.R
import com.suihan74.satena2.model.TextIdContainer

enum class EntryCategoryListType(
    override val textId: Int
) : TextIdContainer {
    LIST(R.string.entry_category_list_type_list),
    GRID(R.string.entry_category_list_type_grid)
}

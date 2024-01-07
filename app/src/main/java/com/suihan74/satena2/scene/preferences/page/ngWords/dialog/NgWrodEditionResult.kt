package com.suihan74.satena2.scene.preferences.page.ngWords.dialog

import com.suihan74.satena2.model.ignoredEntry.IgnoreTarget
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntry
import com.suihan74.satena2.model.ignoredEntry.IgnoredEntryType

/**
 * `NgWordEditionDialog`の操作結果
 */
data class NgWordEditionResult(
    val tabIndex : Int,
    val text : String,
    val url : String,
    val targetEntry : Boolean,
    val targetBookmark : Boolean,
    val asRegex : Boolean
) {
    val type by lazy { IgnoredEntryType.fromOrdinal(tabIndex) }

    val query = when (type) {
        IgnoredEntryType.TEXT -> text
        IgnoredEntryType.URL -> url
    }

    val target = when (type) {
        IgnoredEntryType.TEXT -> toIgnoreTarget(targetEntry, targetBookmark)
        IgnoredEntryType.URL -> IgnoreTarget.ALL
    }

    // ------ //

    fun toIgnoredEntry() : IgnoredEntry {
        return IgnoredEntry.createDummy(type, query, target, asRegex)
    }

    companion object {
        private fun toIgnoreTarget(targetEntry: Boolean, targetBookmark: Boolean) : IgnoreTarget =
            (if (targetEntry) IgnoreTarget.ENTRY else IgnoreTarget.NONE) or
                    (if (targetBookmark) IgnoreTarget.BOOKMARK else IgnoreTarget.NONE)

        /**
         * @throws IllegalArgumentException
         */
        fun IgnoredEntry.update(args: NgWordEditionResult): IgnoredEntry {
            if (args.tabIndex != this.type.ordinal) throw IllegalArgumentException()
            return when (this.type) {
                IgnoredEntryType.TEXT -> {
                    this.copy(
                        query = args.text,
                        target = toIgnoreTarget(args.targetEntry, args.targetBookmark),
                        asRegex = args.asRegex
                    )
                }
                IgnoredEntryType.URL -> {
                    this.copy(
                        query = args.url,
                    )
                }
            }
        }
    }
}

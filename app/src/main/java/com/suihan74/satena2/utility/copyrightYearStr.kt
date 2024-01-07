package com.suihan74.satena2.utility

import java.time.LocalDate

/**
 * コピーライトに表示する年文字列
 */
fun copyrightYearStr() : String {
    val firstYear = 2019
    val y = LocalDate.now().year
    return if (y > firstYear) "$firstYear-$y"
    else firstYear.toString()
}

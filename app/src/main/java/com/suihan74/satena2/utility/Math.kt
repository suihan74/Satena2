package com.suihan74.satena2.utility

import java.lang.Float.max
import java.lang.Float.min

/**
 * 与えられた値を最小～最大値間におさめる
 */
fun bound(
    min: Float,
    max: Float,
    value: Float,
) : Float = max(min, min(max, value))

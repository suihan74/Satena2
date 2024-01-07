package com.suihan74.satena2.utility.hatena

import androidx.compose.ui.graphics.Color
import com.suihan74.hatena.model.star.StarColor

val StarColor.color : Color
    get() = when (this) {
        StarColor.YELLOW -> Color(0xff, 0xbb, 0x00)
        StarColor.RED -> Color(0xff, 0x04, 0x51)
        StarColor.GREEN -> Color(0x00, 0xd2, 0x00)
        StarColor.BLUE -> Color(0x00, 0xd0, 0xef)
        StarColor.PURPLE -> Color(0x77, 0x77, 0xff)
    }

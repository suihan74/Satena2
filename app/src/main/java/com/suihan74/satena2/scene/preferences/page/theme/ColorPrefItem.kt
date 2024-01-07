package com.suihan74.satena2.scene.preferences.page.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suihan74.satena2.R
import com.suihan74.satena2.scene.preferences.PrefItemDefaults
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.utility.extension.rgbCode

/**
 * 色情報用の設定項目
 */
@Composable
fun ColorPrefItem(
    mainText: String,
    color: Color,
    mainTextColor: Color = CurrentTheme.onBackground,
    subTextColor: Color = CurrentTheme.onBackground,
    subTextPrefixColor: Color = CurrentTheme.grayTextColor,
    onClick : ()->Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                vertical = PrefItemDefaults.verticalPadding,
                horizontal = PrefItemDefaults.horizontalPadding
            )
    ) {
        Box(
            modifier = Modifier
                .background(color = color)
                .size(24.dp)
        )

        Column(
            Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = mainText,
                color = mainTextColor,
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.pref_current_value_prefix),
                    color = subTextPrefixColor,
                    fontSize = 12.sp,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = color.rgbCode(),
                    color = subTextColor,
                    fontSize = 13.sp,
                    modifier = Modifier.alignByBaseline().padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ColorPrefItemPreview() {
    ColorPrefItem("Test", Color(0xff, 0xaa, 0x11))
}

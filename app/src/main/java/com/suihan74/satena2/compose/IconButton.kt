package com.suihan74.satena2.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ロングクリック・ダブルクリックに対応し、タップ時エフェクトサイズを調整できるようにした`IconButton`
 */
@Composable
fun CombinedIconButton(
    onClick: ()->Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onLongClick: (()->Unit)? = null,
    onDoubleClick: (()->Unit)? = null,
    rippleRadius: Dp = 24.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = rippleRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha =
            if (enabled) LocalContentAlpha.current
            else ContentAlpha.disabled
        CompositionLocalProvider(
            LocalContentAlpha provides contentAlpha, content = content
        )
    }
}

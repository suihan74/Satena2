package com.suihan74.satena2.compose.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val Send: ImageVector
    get() {
        if (_send != null) {
            return _send!!
        }
        _send =
            ImageVector.Builder(
                name = "send",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(3f, 20f)
                        verticalLineTo(14f)
                        lineToRelative(8f, -2f)
                        lineTo(3f, 10f)
                        verticalLineTo(4f)
                        lineToRelative(19f, 8f)
                        lineTo(3f, 20f)
                        close()
                    }
                }
                .build()
        return _send!!
    }

private var _send: ImageVector? = null
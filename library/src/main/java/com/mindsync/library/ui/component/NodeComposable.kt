package com.mindsync.library.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindsync.library.MindMapManager

@Composable
fun NodeComposable(
    modifier: Modifier = Modifier,
    mindMapManager: MindMapManager,
    text: String = "MindMap",
    centerColor: Color = Color.Blue.copy(alpha = 0.5f),
    outerColor: Color = Color.Red.copy(alpha = 0.5f),
) {
    val textMeasurer = rememberTextMeasurer()

    val style = TextStyle(
        fontSize = 24.sp,
        color = Color.Black,
//        background = Color.Red.copy(alpha = 0.2f)
    )

    val textLayoutResult = remember(text) {
        textMeasurer.measure(text, style)
    }
    Canvas(
        modifier = modifier
            .fillMaxSize(),
    ) {
        // Step 2: Calculate the width and height of the canvas
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    centerColor, centerColor, centerColor, outerColor,
                ),
            ),
            center = Offset(x = canvasWidth / 2, y = canvasHeight / 2),
        )
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            style = style,
            topLeft = Offset(
                x = center.x - textLayoutResult.size.width / 2,
                y = center.y - textLayoutResult.size.height / 2,
            )
        )
    }
}

@Preview
@Composable
private fun NodeComposePreview() {
    MaterialTheme {
        val context = LocalContext.current
        NodeComposable(
            modifier = Modifier.size(100.dp),
            mindMapManager = MindMapManager(context),
        )
    }
}
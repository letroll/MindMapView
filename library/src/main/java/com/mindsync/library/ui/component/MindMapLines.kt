package com.mindsync.library.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.mindsync.library.MindMapManager
import com.mindsync.library.data.CircleNodeData
import com.mindsync.library.data.NodeData
import com.mindsync.library.data.RectangleNodeData
import com.mindsync.library.data.Tree

@Composable
fun MindMapLines(
    mindMapManager: MindMapManager,
    modifier: Modifier = Modifier,
) {
    val tree = remember { mindMapManager.getTree() }
    val rootNode = remember { tree.getRootNode() }
    val movingState = remember { mindMapManager.getMovingState() }
    val selectedNode = remember { mindMapManager.getSelectedNode() }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    Canvas(modifier = modifier) {
        if (rootNode.children.isNotEmpty()) {
            traverseLine(
                drawScope = this,
                node = rootNode,
                tree = tree,
                movingState = movingState,
                selectedNode = selectedNode,
                depth = 0,
                isSystemInDarkTheme = isSystemInDarkTheme
            )
        }
    }
}

@Preview
@Composable
private fun MindMapLinesPreview() {
   MaterialTheme{
      val context = LocalContext.current
      MindMapLines(
          mindMapManager = MindMapManager(context),
          modifier = Modifier,
      )
   }
}

private fun traverseLine(
    drawScope: DrawScope,
    node: NodeData<*>,
    tree: Tree<*>,
    movingState: Boolean,
    selectedNode: NodeData<*>?,
    depth: Int,
    isSystemInDarkTheme: Boolean,
) {
    for (toNodeId in node.children) {
        val toNode = tree.getNode(toNodeId)
        drawLine(
            drawScope = drawScope,
            fromNode = node,
            toNode = toNode,
            movingState = movingState,
            selectedNode = selectedNode,
            isSystemInDarkTheme = isSystemInDarkTheme
        )
        traverseLine(
            drawScope = drawScope,
            node = toNode,
            tree = tree,
            movingState = movingState,
            selectedNode = selectedNode,
            depth = depth + 1,
            isSystemInDarkTheme = isSystemInDarkTheme
        )
    }
}

private fun drawLine(
    drawScope: DrawScope,
    fromNode: NodeData<*>,
    toNode: NodeData<*>,
    movingState: Boolean,
    selectedNode: NodeData<*>?,
    isSystemInDarkTheme: Boolean,
) {
    val linePaint = if (isSystemInDarkTheme) {
        // Dark mode line paint
        Paint().apply {
            color = Color.White
            strokeWidth = 2.toPx(drawScope)
            style = PaintingStyle.Stroke
        }
    } else {
        // Light mode line paint
        Paint().apply {
            color = Color.Black
            strokeWidth = 2.toPx(drawScope)
            style = PaintingStyle.Stroke
        }
    }

    val path = createPath(drawScope, fromNode, toNode)

//    val colorStops = arrayOf(
//        0.0f to Color.Yellow,
//        0.2f to Color.Red,
//        1f to Color.Blue
//    )
//    val brush = Brush.horizontalGradient(colorStops = colorStops)

    if (!movingState || selectedNode?.id != toNode.id) {
        drawScope.drawPath(
            color = Color.Black,
            path= path,
//            brush = linePaint,
        )
    }
}

private fun createPath(
    drawScope: DrawScope,
    fromNode: NodeData<*>,
    toNode: NodeData<*>,
): Path {
    val startX = getNodeEdgeX(drawScope, fromNode, true)
    val startY = fromNode.path.centerY.dpVal.toPx(drawScope)
    val endX = getNodeEdgeX(drawScope, toNode, false)
    val endY = toNode.path.centerY.dpVal.toPx(drawScope)
    val midX = (startX + endX) / 2
    return Path().apply {
        reset()
        moveTo(startX, startY)
        cubicTo(midX, startY, midX, endY, endX, endY)
    }
}

private fun getNodeEdgeX(
    drawScope: DrawScope,
    node: NodeData<*>,
    isStart: Boolean,
): Float {
    val nodeCenterX = node.path.centerX.dpVal.toPx(drawScope)
    val widthOffset = when (node) {
        is CircleNodeData -> node.path.radius.dpVal.toPx(drawScope)
        is RectangleNodeData -> node.path.width.dpVal.toPx(drawScope) / 2
        else -> 0f // Handle other node types if needed
    }
    return if (isStart) nodeCenterX + widthOffset else nodeCenterX - widthOffset
}

// Extension functions for converting values to pixels in DrawScope
private fun Float.toPx(drawScope: DrawScope): Float =
    with(drawScope) { this@toPx * density }

private fun Int.toPx(drawScope: DrawScope): Float =
    with(drawScope) { this@toPx * density }
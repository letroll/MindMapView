package com.mindsync.library.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindsync.library.MindMapManager
import com.mindsync.library.animator.MindMapAnimator
import com.mindsync.library.animator.TreeChangeAnimation
import com.mindsync.library.command.AddNodeCommand
import com.mindsync.library.data.NodeData
import com.mindsync.library.data.RectangleNodeData
import com.mindsync.library.model.Node
import com.mindsync.library.model.OperationType
import com.mindsync.library.model.OperationType.Add
import com.mindsync.library.model.OperationType.None
import com.mindsync.library.util.NodeGenerator

@Composable
fun MindMapComposable(
    modifier: Modifier = Modifier,
    mindMapManager: MindMapManager,
    mindMapAnimator: MindMapAnimator,
    operationType : OperationType = None,
    description :String = "",
//    content: @Composable () -> Unit,
    selectedNode: Node?,
    onNodeSelect: (node: NodeData<*>?) -> Unit,
//    dialogToShow: (EditDescriptionDialog)->Unit,
) {
    // set up all transformation states
    var scale by remember { mutableFloatStateOf(1f) }
//    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
//        rotation += rotationChange
        offset += offsetChange
    }
    when (operationType) {
        Add -> {
            selectedNode?.let { node ->
                val addNode = NodeGenerator.makeNode(RectangleNodeData::class, description, node.id)
                val addNodeCommand = AddNodeCommand(mindMapManager, addNode)
                addNodeCommand.execute()
                animateTreeChange(
                    mindMapManager,
                    mindMapAnimator
                )
//                requestLayout()
            }
        }
//        Edit -> TODO()
//        FitScreen -> TODO()
//        None -> TODO()
//        Remove -> TODO()
        else -> {

        }
    }
    Layout(
        content = {
            mindMapManager.getTree().nodes.forEach { pair ->
                NodeComposable(
                    modifier = Modifier
                        .wrapContentSize()
                        .size(80.dp)
                        .clickable {
                            onNodeSelect(pair.value)
                        },
                    mindMapManager = mindMapManager,
                    text = pair.value.description,
                )
            }
        },
        modifier = Modifier
            .then(modifier)
            .clickable {
                onNodeSelect(null)
            }
// apply other transformations like rotation and zoom
            // on the pizza slice emoji
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
//                rotationZ = rotation,
                translationX = offset.x,
                translationY = offset.y
            )
            // add transformable to listen to multitouch transformation events
            // after offset
            .transformable(state = state),
    ) { measurables, constraints ->
        val parentWidth = constraints.maxWidth
        val parentHeight = constraints.maxHeight


        val placeables: List<Placeable> = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        // calculate our layout width and height
        val itemsTotalWidth = placeables.sumOf { placeable -> placeable.width }
        val ourLayoutTotalWidth = if (itemsTotalWidth > constraints.maxWidth) constraints.maxWidth else itemsTotalWidth
        val ourLayoutTotalHeight = placeables.sumOf { placeable -> placeable.height }

        // place child items
//        layout(width = ourLayoutTotalWidth, height = ourLayoutTotalHeight) {
        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
//            var y = 0
//            var x = 0

            placeables.first().apply {
                /*
                pour en Y
                0 = centre
                 */
                place(x = parentWidth / 2 - width / 2, y = 0)
            }

//            placeables.forEach { placeable ->
//                val itemHorizontalEndCoordinate = x + placeable.width
//                if (itemHorizontalEndCoordinate > constraints.maxWidth) {
//                    x = 0
//                }
//
//                placeable.place(x = x, y = y)
//
//                y += placeable.height
//                x += placeable.width
//            }
        }
    }
}

private fun animateTreeChange(
    mindMapManager: MindMapManager,
    mindMapAnimator: MindMapAnimator
) {
    mindMapAnimator.setAnimationStrategy(
        TreeChangeAnimation(mindMapManager) { updateNodeAndLine() }
    )
    mindMapAnimator.executeAnimation()
}


private fun updateNodeAndLine() {
//    nodeView.update()
//    lineView.update()
}

/*
Canvas(
modifier = modifier.fillMaxWidth(),
) {
    // Step 2: Calculate the width and height of the canvas
    val canvasWidth = size.width
    val canvasHeight = size.height
    // Step 3: Draw a rectangle on the canvas
    DrawBackground(canvasWidth, canvasHeight)
}
*/

@Preview
@Composable
private fun MindMapPreview() {
    MaterialTheme {
        val context = LocalContext.current
        MindMapComposable(
            modifier = Modifier.size(500.dp),
            mindMapManager = MindMapManager(context),
            mindMapAnimator = MindMapAnimator(),
            onNodeSelect = { },
            selectedNode = null,
        )
    }
}

private fun DrawScope.DrawBackground(canvasWidth: Float, canvasHeight: Float) {
    drawRect(
        // Parameter 1: color
        color = Color.Red.copy(alpha = 0.5f),

        // Parameter 2: topLeft
        // Defines the top-left corner of the rectangle
        // Offset(0f, 0f) means the top-left corner of the canvas
        topLeft = Offset(0f, 0f),

        // Parameter 3: size
        // Defines the size (width and height) of the rectangle
        // Size(canvasWidth, canvasHeight) means the rectangle's size will match the canvas size
        size = Size(canvasWidth, canvasHeight),

        // Parameter 4: style
        // Defines the style of the rectangle's outline
        // Stroke(width = 2.dp.toPx()) means the outline will have a width of 2 pixels
        style = Fill
    )
}
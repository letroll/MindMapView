package com.mindsync.library.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mindsync.library.MindMapManager
import com.mindsync.library.MindMapView
import com.mindsync.library.NodeClickListener
import com.mindsync.library.data.CircleNodeData
import com.mindsync.library.data.NodeData
import com.mindsync.library.data.RectangleNodeData
import com.mindsync.library.data.Tree
import com.mindsync.library.model.CircleNode
import com.mindsync.library.model.CirclePath
import com.mindsync.library.model.Node
import com.mindsync.library.model.OperationType.Add
import com.mindsync.library.model.OperationType.Edit
import com.mindsync.library.model.OperationType.FitScreen
import com.mindsync.library.model.OperationType.None
import com.mindsync.library.model.OperationType.Remove
import com.mindsync.library.model.RectangleNode
import com.mindsync.library.model.RectanglePath
import com.mindsync.library.ui.dialog.EditDescriptionDialog
import com.mindsync.library.util.Dp

@Composable
fun ComposableMindMapView(
    modifier: Modifier = Modifier,
    selectedNode: Node?,
    onNodeSlect:(Node?)->Unit,
    dialogToShow: (EditDescriptionDialog)->Unit,
) {

    lateinit var manager: MindMapManager
    var canRemove by remember { mutableStateOf(true) }
    var description by remember { mutableStateOf("") }
    var operationType by remember { mutableStateOf(None) }

    Column(
        modifier = modifier,
    ) {
        AndroidView(
            modifier = Modifier
                .weight(1f),
            factory = { context ->
                val tree = Tree<Node>(context)
                MindMapView(context).apply {
//                        setBackgroundColor(Color.Blue.toArgb())
                    clipToOutline = true
                    setTree(tree)
                    initialize()
                    manager = getMindMapManager()
                    setNodeClickListener(object : NodeClickListener {
                        override fun onClickListener(node: NodeData<*>?) {
                            val newSelectedNode = createNode(node)
                            onNodeSlect(newSelectedNode)

                            canRemove = when (newSelectedNode) {
                                is CircleNode -> false
                                is RectangleNode -> true
                                else -> false
                            }
                        }
                    })
                }
            },
            update = { view ->
                // View's been inflated or state read in this block has been updated
                // Add logic here if necessary
                when(operationType){
                    Add -> {
                        view.addNode(description)
                        operationType = None
                    }
                    Edit -> {
                        view.editNodeText(description)
                        operationType = None
                    }
                    FitScreen -> {
                        view.fitScreen()
                    }
                    Remove -> {
                        view.removeNode()
                    }

                    None -> {}
                }
            }
        )
        if (selectedNode != null) {
            MapViewBar(
                modifier = Modifier,
//                        .background(Color.Green),
                canRemove = canRemove,
                onAction = { action,newDescription ->
                    when (action) {
                        Add -> {
                            description = newDescription
                            operationType = action
                        }
                        Edit -> {
                            description = newDescription
                            operationType = action
                        }
                        FitScreen -> {
                            operationType = action
                        }
                        Remove -> {
                            operationType = action
                        }

                        None -> {}
                    }
                },
                selectedNode = selectedNode,
                dialogToShow = dialogToShow,
            )
        }
    }
}

private fun createNode(node: NodeData<*>?): Node? {
    return when (node) {
        is CircleNodeData -> CircleNode(
            node.id,
            node.parentId,
            CirclePath(
                Dp(node.path.centerX.dpVal),
                Dp(node.path.centerY.dpVal),
                Dp(node.path.radius.dpVal)
            ),
            node.description,
            node.children
        )

        is RectangleNodeData -> RectangleNode(
            node.id,
            node.parentId,
            RectanglePath(
                Dp(node.path.centerX.dpVal),
                Dp(node.path.centerY.dpVal),
                Dp(node.path.width.dpVal),
                Dp(node.path.height.dpVal)
            ),
            node.description,
            node.children
        )

        else -> null
    }
}

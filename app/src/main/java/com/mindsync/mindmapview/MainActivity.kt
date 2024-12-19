package com.mindsync.mindmapview

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mindsync.library.MindMapManager
import com.mindsync.library.MindMapView
import com.mindsync.library.NodeClickListener
import com.mindsync.library.data.CircleNodeData
import com.mindsync.library.data.NodeData
import com.mindsync.library.data.RectangleNodeData
import com.mindsync.library.data.Tree
import com.mindsync.mindmapview.model.CircleNode
import com.mindsync.mindmapview.model.CirclePath
import com.mindsync.mindmapview.model.Node
import com.mindsync.mindmapview.model.RectangleNode
import com.mindsync.mindmapview.model.RectanglePath

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MindMapViewModel>()
    private lateinit var mindMapView: MindMapView
    private lateinit var imgbtnMindMapAdd: ImageButton
    private lateinit var imgbtnMindMapEdit: ImageButton
    private lateinit var imgbtnMindMapRemove: ImageButton
    private lateinit var imgbtnMindMapFit: ImageButton
    private lateinit var manager: MindMapManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        setClickEvent()
    }

    private fun setBinding() {
//        android:visibility="@{vm.selectedNode != null ? View.VISIBLE : View.GONE}"
//        app:constraint_referenced_ids="imgbtn_mind_map_add,imgbtn_mind_map_remove,imgbtn_mind_map_edit,view_mind_map_side_bar"
    }

    private fun init() {
        val tree = Tree<Node>(this)
        mindMapView = findViewById(R.id.mind_map_view)
        imgbtnMindMapAdd = findViewById(R.id.imgbtn_mind_map_add)
        imgbtnMindMapEdit = findViewById(R.id.imgbtn_mind_map_edit)
        imgbtnMindMapFit = findViewById(R.id.imgbtn_mind_map_fit)
        imgbtnMindMapRemove = findViewById(R.id.imgbtn_mind_map_remove)

        mindMapView.setTree(tree)
        mindMapView.initialize()
        manager = mindMapView.getMindMapManager()
    }

    private fun showDialog(
        operationType: String,
        selectedNode: Node,
    ) {
        val description = if (operationType == "add") "" else selectedNode.description
        val editDescriptionDialog = EditDescriptionDialog()
        editDescriptionDialog.setDescription(description)
        editDescriptionDialog.setSubmitListener { description ->
            when (operationType) {
                "add" -> {
                    mindMapView.addNode(description)
                }

                "update" -> {
                    mindMapView.editNodeText(description)
                }

                else -> return@setSubmitListener
            }
        }
        editDescriptionDialog.show(
            this.supportFragmentManager,
            "EditDescriptionDialog",
        )
    }

    private fun setClickEvent() {
        imgbtnMindMapAdd.setOnClickListener {
            viewModel.selectedNode.value?.let { selectNode ->
                showDialog("add", selectNode)
            }
        }
        imgbtnMindMapEdit.setOnClickListener {
            viewModel.selectedNode.value?.let { selectNode ->
                showDialog("update", selectNode)
            }
        }
        imgbtnMindMapRemove.setOnClickListener {
            viewModel.selectedNode.value?.let { selectNode ->
                mindMapView.removeNode()
            }
        }

        imgbtnMindMapFit.setOnClickListener {
            mindMapView.fitScreen()
        }

        mindMapView.setNodeClickListener(object : NodeClickListener {
            override fun onClickListener(node: NodeData<*>?) {
                val selectedNode = createNode(node)
                viewModel.setSelectedNode(selectedNode)

                imgbtnMindMapRemove.isEnabled = when (selectedNode) {
                    is CircleNode -> false
                    is RectangleNode -> true
                    else -> false
                }
            }
        })
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
}
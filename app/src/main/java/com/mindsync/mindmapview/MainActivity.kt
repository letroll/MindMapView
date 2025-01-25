package com.mindsync.mindmapview

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.mindsync.library.MindMapManager
import com.mindsync.library.MindMapViewModel
import com.mindsync.library.animator.MindMapAnimator
import com.mindsync.library.data.NodeData
import com.mindsync.library.data.Tree
import com.mindsync.library.data.Tree.Companion.DEFAULT_ROOT_TEXT
import com.mindsync.library.ui.component.ComposableMindMapView
import com.mindsync.library.ui.primaryContainerLight
import com.mindsync.library.ui.primaryLight
import com.mindsync.library.util.NodeGenerator

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MindMapViewModel>()
    private lateinit var mindMapManager : MindMapManager
    private val mindMapAnimator = MindMapAnimator()
    private val rootNode = NodeGenerator.makeRootNode()
    private val rootNodeMap = Pair(DEFAULT_ROOT_TEXT,rootNode)
    private val tree = Tree<NodeData<*>>(
            mapOf(rootNodeMap)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                scrim = primaryLight.toArgb(),
                darkScrim = primaryContainerLight.toArgb()
            )
        )
        mindMapManager = MindMapManager(this)
        tree.setRootNode(rootNode)
        mindMapManager.setTree(tree)
        setContent {
            val state = viewModel.uiState.collectAsState()
            MaterialTheme {
                Scaffold(
                    modifier = Modifier,
                    content = { innerPadding ->
                        ComposableMindMapView(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                            mindMapManager = mindMapManager,
                            mindMapAnimator = mindMapAnimator,
                            selectedNode = state.value.selectedNode,
                            onNodeSlect = { newSelectedNode ->
                                viewModel.setSelectedNode(newSelectedNode)
                            },
                            dialogToShow = { dialog ->
                                dialog.show(
                                    this.supportFragmentManager,
                                    "EditDescriptionDialog",
                                )
                            }
                        )
                    }
                )
            }
        }
    }

}
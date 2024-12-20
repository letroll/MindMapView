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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mindsync.library.MindMapViewModel
import com.mindsync.library.ui.ComposableMindMapView

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MindMapViewModel>()

    val primaryLight = Color(0xFF805610)
    val primaryContainerLight = Color(0xFFFFDDB3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                scrim = primaryLight.toArgb(),
                darkScrim = primaryContainerLight.toArgb()
            )
        )
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
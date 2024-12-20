package com.mindsync.library

import androidx.lifecycle.ViewModel
import com.mindsync.library.model.MindmapUiState
import com.mindsync.library.model.Node
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MindMapViewModel: ViewModel() {

    private val _uiState: MutableStateFlow<MindmapUiState> = MutableStateFlow(MindmapUiState())
    val uiState: StateFlow<MindmapUiState> = _uiState

    fun setSelectedNode(selectNode: Node?) {
        _uiState.update {
            it.copy(
                selectedNode = selectNode
            )
        }
    }
}

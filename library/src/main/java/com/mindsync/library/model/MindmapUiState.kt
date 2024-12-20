package com.mindsync.library.model

import com.mindsync.library.model.OperationType.None

data class MindmapUiState(
    val selectedNode: Node?=null,
    val currentOperation: OperationType = None,
    val description:String="",
)
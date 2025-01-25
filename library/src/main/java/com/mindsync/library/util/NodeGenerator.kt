package com.mindsync.library.util

import com.mindsync.library.data.CircleNodeData
import com.mindsync.library.data.NodeData
import com.mindsync.library.data.RectangleNodeData
import com.mindsync.library.data.Tree.Companion.DEFAULT_ROOT_TEXT
import com.mindsync.library.data.Tree.Companion.ROOT_ID
import com.mindsync.library.model.CirclePath
import com.mindsync.library.model.RectanglePath
import com.mindsync.library.util.IdGenerator.makeRandomNodeId
import kotlin.reflect.KClass

object NodeGenerator {

    fun makeRootNode(
        description: String=DEFAULT_ROOT_TEXT,
    ): CircleNodeData = CircleNodeData(
        id = ROOT_ID,
        parentId = "",
        path = CirclePath(Dp(0f), Dp(0f), Dp(50f)),
        description = description,
        children = listOf()
    )

    fun <N : NodeData<*>> makeNode(
        nodeClass: KClass<N>,
        description: String,
        parentId: String,
        isRoot: Boolean = false
    ): N {
        val id = if (isRoot) ROOT_ID else makeRandomNodeId()

        return when (nodeClass) {
            CircleNodeData::class -> CircleNodeData(
                id = id,
                parentId = parentId,
                path = CirclePath(Dp(0f), Dp(0f), Dp(50f)),  // 예시로 CirclePath 설정
                description = description,
                children = listOf()
            ) as N

            RectangleNodeData::class -> RectangleNodeData(
                id = id,
                parentId = parentId,
                path = RectanglePath(Dp(0f), Dp(0f), Dp(100f), Dp(50f)),  // 예시로 RectanglePath 설정
                description = description,
                children = listOf()
            ) as N

            else -> throw IllegalArgumentException("Unsupported Node type")
        }
    }
}

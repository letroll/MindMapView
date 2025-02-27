package com.mindsync.library.data

import com.mindsync.library.model.CirclePath
import com.mindsync.library.model.NodePath
import com.mindsync.library.model.RectanglePath
import com.mindsync.library.util.Dp

sealed class NodeData<T>(
    open val id: String,
    open val parentId: String?,
    open val path: NodePath,
    open val description: String,
    open val children: List<String>,
    open var alpha: Float,
    open var isAnimating: Boolean,
    open var isDrawingLine: Boolean,
    open var strokeWidth: Float,
) : java.io.Serializable {
    abstract fun adjustPosition(horizontalSpacing: Dp, totalHeight: Dp): NodeData<T>
}

data class CircleNodeData(
    override val id: String,
    override val parentId: String?,
    override val path: CirclePath = CirclePath(Dp(0f), Dp(0f), Dp(0f)),
    override val description: String,
    override val children: List<String>,
    override var alpha: Float = 0f,
    override var isAnimating: Boolean = false,
    override var isDrawingLine: Boolean = false,
    override var strokeWidth: Float = 1f,
) : NodeData<CircleNodeData>(id, parentId, path, description, children, alpha, isAnimating, isDrawingLine, strokeWidth) {
    override fun adjustPosition(horizontalSpacing: Dp, totalHeight: Dp): NodeData<CircleNodeData> {
        return this.copy(path = path.adjustPath(horizontalSpacing, totalHeight))
    }
}

data class RectangleNodeData(
    override val id: String,
    override val parentId: String,
    override val path: RectanglePath = RectanglePath(Dp(0f), Dp(0f), Dp(0f), Dp(0f)),
    override val description: String,
    override val children: List<String>,
    override var alpha: Float = 0f,
    override var isAnimating: Boolean = false,
    override var isDrawingLine: Boolean = false,
    override var strokeWidth: Float = 1f,
) : NodeData<RectangleNodeData>(id, parentId, path, description, children, alpha, isAnimating, isDrawingLine, strokeWidth) {
    override fun adjustPosition(horizontalSpacing: Dp, totalHeight: Dp): NodeData<RectangleNodeData> {
        return this.copy(path = path.adjustPath(horizontalSpacing, totalHeight))
    }
}

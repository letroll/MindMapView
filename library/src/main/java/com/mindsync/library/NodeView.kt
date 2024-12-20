package com.mindsync.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.mindsync.library.animator.MindMapAnimator
import com.mindsync.library.animator.TreeChangeAnimation
import com.mindsync.library.data.CircleNodeData
import com.mindsync.library.data.NodeData
import com.mindsync.library.data.RectangleNodeData
import com.mindsync.library.layout.MindMapRightLayoutManager
import com.mindsync.library.model.DrawInfo
import com.mindsync.library.node.NodeDrawerFactory
import com.mindsync.library.util.Dp
import com.mindsync.library.util.Px
import com.mindsync.library.util.toDp
import com.mindsync.library.util.toPx

class NodeView @JvmOverloads constructor(
    private val mindMapManager: MindMapManager,
    private val lineView: LineView,
    private val typeface: Typeface,
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {
    private val drawInfo = DrawInfo(context)
    private var attachedNode: NodeData<*>? = null
    private val rightLayoutManager = MindMapRightLayoutManager()
    private val mindMapAnimator = MindMapAnimator()
    var listener: NodeClickListener? = null
    var moveListener: NodeMoveListener? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAttachedNode(canvas)
        drawTree(canvas)
        mindMapManager.getSelectedNode()?.let { selectedNode ->
            makeStrokeNode(canvas, selectedNode)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mindMapManager.setNotMoving()
                findTouchNode(event.x, event.y)
                return mindMapManager.getSelectedNode() != null
            }

            MotionEvent.ACTION_MOVE -> {
                if (mindMapManager.getSelectedNode() is CircleNodeData) {
                    mindMapManager.setNotMoving()
                } else {
                    mindMapManager.setMoving()
                }
                moveNode(
                    event.x,
                    event.y,
                )
                findIncludedNode(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mindMapManager.getMovingState()) {
                    mindMapManager.setNotMoving()
                    updateTreeIfNodeAttached(event)
                }
                resetStateAndRefreshTree()
            }
        }
        return false
    }

    private fun updateTreeIfNodeAttached(event: MotionEvent) {
        mindMapManager.getSelectedNode()?.let { selectedNode ->
            findIncludedNode(event.x, event.y)
            attachNode(selectedNode)
        }
    }

    private fun resetStateAndRefreshTree() {
        attachedNode = null
        mindMapAnimator.setAnimationStrategy(
            TreeChangeAnimation(mindMapManager) {
                lineView.update()
                invalidate()
            }
        )
        mindMapAnimator.executeAnimation()

    }

    private fun attachNode(selectedNode: NodeData<*>) {
        attachedNode?.let { attachedNode ->
            mindMapManager.getTree().doPreorderTraversal { node ->
                if (node.id == selectedNode.id) {
                    mindMapManager.getTree().removeNode(node.id)
                }
            }
            mindMapManager.getTree().doPreorderTraversal { node ->
                if (node.id == attachedNode.id) {
                    mindMapManager.getTree().attachNode(selectedNode.id, attachedNode.id)

                }
            }
            this.moveListener?.onMoveListener(mindMapManager.getTree(), selectedNode, attachedNode)
        }
    }

    private fun findIncludedNode(
        dx: Float,
        dy: Float,
    ) {
        var attachedNode: NodeData<*>? = null
        if (mindMapManager.getSelectedNode() is CircleNodeData) return
        mindMapManager.getTree().doPreorderTraversal { node ->
            mindMapManager.getSelectedNode()?.let {
                if (isInsideNode(node, dx, dy) && mindMapManager.getSelectedNode()?.id != node.id) {
                    attachedNode = node
                }
            }
        }

        attachedNode?.let { node ->
            this.attachedNode = node
        } ?: run {
            this.attachedNode = null
        }
    }

    private fun moveNode(
        dx: Float,
        dy: Float,
    ) {
        mindMapManager.getSelectedNode()?.let { selectedNode ->
            if (selectedNode is CircleNodeData) return
            traverseMovedNode(mindMapManager.getTree().getRootNode(), selectedNode, dx, dy)
            mindMapManager.update(mindMapManager.getTree())
            rightLayoutManager.arrangeNode(
                mindMapManager.getTree(),
                selectedNode as RectangleNodeData
            )
        }
        lineView.update()
        invalidate()
    }

    private fun traverseMovedNode(
        node: NodeData<*>,
        target: NodeData<*>,
        dx: Float,
        dy: Float,
    ) {
        if (node.id == target.id) {
            val centerX = Dp(Px(dx).toDp(context))
            val centerY = Dp(Px(dy).toDp(context))
            mindMapManager.getTree()
                .updateNode(target.id, target.description, target.children, centerX, centerY)
        }
        node.children.forEach { nodeId ->
            traverseMovedNode(mindMapManager.getTree().getNode(nodeId), target, dx, dy)
        }
    }

    fun update() {
        invalidate()
    }

    private fun drawTree(canvas: Canvas) {
        mindMapManager.getTree().doPreorderTraversal { node, depth ->
            mindMapManager.getSelectedNode()?.let { selectedNode ->
                if (selectedNode.id == node.id) {
                    mindMapManager.setSelectedNode(node)
                }
            }
            drawNode(canvas, node, depth)
        }
    }

    private fun findTouchNode(
        x: Float,
        y: Float,
    ) {
        var rangeResultNode: NodeData<*>? = null
        mindMapManager.getTree().doPreorderTraversal { node ->
            if (isInsideNode(node, x, y)) {
                rangeResultNode = node
            }
        }
        rangeResultNode?.let {
            mindMapManager.setSelectedNode(it)
            this.listener?.onClickListener(it)
        } ?: run {
            mindMapManager.setSelectedNode(null)
            this.listener?.onClickListener(null)
        }
        invalidate()
    }

    private fun drawAttachedNode(canvas: Canvas) {
        attachedNode?.let { attachedNode ->
            val height = when (attachedNode) {
                is RectangleNodeData -> attachedNode.path.height
                is CircleNodeData -> attachedNode.path.radius + Dp(ATTACH_CIRCLE_NODE_RANGE_VALUE)
            }
            val width = when (attachedNode) {
                is RectangleNodeData -> attachedNode.path.width
                is CircleNodeData -> attachedNode.path.radius + Dp(ATTACH_CIRCLE_NODE_RANGE_VALUE)
            }
            val radius = maxOf(height.toPx(context), width.toPx(context))
            canvas.drawCircle(
                attachedNode.path.centerX.toPx(context),
                attachedNode.path.centerY.toPx(context),
                radius,
                drawInfo.boundaryPaint,
            )
            invalidate()
        }
    }

    private fun makeStrokeNode(
        canvas: Canvas,
        node: NodeData<*>,
    ) {
        val nodeDrawerFactory = NodeDrawerFactory(node, context)
        val nodeDrawer = nodeDrawerFactory.createStrokeNode()
        nodeDrawer.drawNode(canvas)
    }

    private fun isInsideNode(
        node: NodeData<*>,
        x: Float,
        y: Float,
    ): Boolean {
        when (node) {
            is CircleNodeData -> {
                if (x in (node.path.centerX - node.path.radius).toPx(context)..(node.path.centerX + node.path.radius).toPx(
                        context
                    ) && y in (node.path.centerY - node.path.radius).toPx(context)..(node.path.centerY + node.path.radius).toPx(
                        context
                    )
                ) {
                    return true
                }
            }

            is RectangleNodeData -> {
                if (x in node.path.leftX().toPx(context)..node.path.rightX()
                        .toPx(context) && y in node.path.topY().toPx(context)..node.path.bottomY()
                        .toPx(context)
                ) {
                    return true
                }
            }
        }
        return false
    }

    private fun drawNode(
        canvas: Canvas,
        node: NodeData<*>,
        depth: Int,
    ) {
        val nodeDrawerFactory = NodeDrawerFactory(node, context, depth)
        val nodeDrawer = nodeDrawerFactory.createNodeDrawer()
        val lines = node.description.split("\n")
        nodeDrawer.drawNode(canvas)
        nodeDrawer.drawText(canvas, lines, typeface)
    }


    companion object {
        private const val ATTACH_CIRCLE_NODE_RANGE_VALUE = 15f
    }
}

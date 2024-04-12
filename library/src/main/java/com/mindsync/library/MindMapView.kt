package com.mindsync.mindmap

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import boostcamp.and07.mindsync.ui.view.model.LayoutMode
import com.mindsync.mindmap.animator.FitAnimation
import com.mindsync.mindmap.animator.MindMapAnimator
import com.mindsync.mindmap.animator.TreeChangeAnimation
import com.mindsync.mindmap.command.AddNodeCommand
import com.mindsync.mindmap.command.RemoveNodeCommand
import com.mindsync.mindmap.command.UpdateNodeCommand
import com.mindsync.mindmap.data.Tree
import com.mindsync.mindmap.util.Dp
import com.mindsync.mindmap.util.toPx


class MindMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var scaleFactor = DEFAULT_ZOOM
    private val focusPoint = PointF()
    private val scaleGestureDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                focusPoint.set(detector.focusX, detector.focusY)
                return true
            }
        },
    )
    private val touchStartPoint = PointF()
    private val preFocusPoint = PointF()
    private val currentPoint = PointF()
    private var mode = LayoutMode.DRAG
    private lateinit var nodeView: NodeView
    private lateinit var lineView: LineView
    private val mindMapManager = MindMapManager(context)
    private var winWidth = 0
    private var winHeight = 0
    private val mindMapAnimator = MindMapAnimator()

    init {
        initialize()
    }

    private fun initialize() {
        lineView = LineView(mindMapManager, context, attrs = null).apply {
            update()
        }
        nodeView = NodeView(mindMapManager, lineView, context, attrs = null).apply {
            update()
        }
        addView(nodeView)
        addView(lineView)
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {

        var width = 0
        var height = 0
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            width = maxOf(width, child.measuredWidth)
            height = maxOf(height, child.measuredHeight)
        }
        if (MeasureSpec.getSize(widthMeasureSpec) > 0 && MeasureSpec.getSize(heightMeasureSpec) > 0) {
            winWidth = MeasureSpec.getSize(widthMeasureSpec)
            winHeight = MeasureSpec.getSize(heightMeasureSpec)
        }

        setMeasuredDimension(
            maxOf(
                width,
                mindMapManager.measureWidth(mindMapManager.getTree().getRootNode()).toPx(context)
                    .toInt() + mindMapManager.getTree().getRootNode().path.centerX.toPx(context)
                    .toInt()
            ), maxOf(
                height,
                mindMapManager.measureHeight(mindMapManager.getTree().getRootNode()).toPx(context)
                    .toInt() + mindMapManager.getTree().getRootNode().path.centerY.toPx(context)
                    .toInt()
            )
        )
    }

    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int,
    ) {
        val childCount = childCount
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            child.layout(
                child.left,
                child.top,
                child.right + mindMapManager.getTree().getRootNode().path.centerX.toPx(context)
                    .toInt() + mindMapManager.getTree().getRootNode().path.radius.toPx(context)
                    .toInt() * 2,
                child.bottom + mindMapManager.getTree().getRootNode().path.centerY.toPx(context)
                    .toInt() + mindMapManager.getTree().getRootNode().path.radius.toPx(context)
                    .toInt() * 2,
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        winWidth = w
        winHeight = h
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (mindMapManager.getSelectedNode() == null) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                updateInitialTouchCoordinates(event)
                mode = LayoutMode.DRAG
            }

            MotionEvent.ACTION_MOVE -> {
                focusPoint.set(preFocusPoint)
                if (mode == LayoutMode.DRAG && mindMapManager.getMovingState().not()) {
                    updateTranslation(event)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = LayoutMode.ZOOM
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = LayoutMode.NONE
            }
        }
        applyScaleAndTranslation()
        return true
    }

    private fun updateInitialTouchCoordinates(event: MotionEvent) {
        focusPoint.set(preFocusPoint)
        touchStartPoint.set(event.x - currentPoint.x, event.y - currentPoint.y)
    }

    private fun updateTranslation(event: MotionEvent) {
        val cx = minOf(0f, event.x - touchStartPoint.x)
        val cy = minOf(0f, event.y - touchStartPoint.y)
        currentPoint.set(cx, cy)
    }

    private fun applyScaleAndTranslation() {
        for (index in 0 until childCount) {
            with(getChildAt(index)) {
                scaleX = scaleFactor
                scaleY = scaleFactor
                pivotX = focusPoint.x
                pivotY = focusPoint.y
                translationX = currentPoint.x
                translationY = currentPoint.y
            }
        }
    }

    fun addNode(description: String) {
        mindMapManager.getSelectedNode()?.let { node ->
            val addNodeCommand = AddNodeCommand(mindMapManager, description, node.id)
            addNodeCommand.execute()
        }
        mindMapAnimator.setAnimationStrategy(
            TreeChangeAnimation(mindMapManager, { updateNodeAndLine() })
        )
        mindMapAnimator.executeAnimation()
        requestLayout()
    }

    fun fitScreen() {
        adjustScaleToFitNodes()
    }

    private fun adjustScaleToFitNodes() {
        preFocusPoint.set(focusPoint)
        focusPoint.set(0f, 0f)

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val widthScale = 1f * (mindMapManager.measureWidth(mindMapManager.getTree().getRootNode())
            .toPx(context) + Dp(50f).toPx(context)) / screenWidth
        val heightScale = 1f * (maxOf(
            mindMapManager.measureHeight(mindMapManager.getTree().getRootNode())
                .toPx(context) * 1.4f, Dp(500f).toPx(context)
        )) / screenHeight

        val scale = maxOf(widthScale, heightScale)
        val startScaleFactor = scaleFactor
        val endScaleFactor = DEFAULT_ZOOM / scale
        mindMapAnimator.setAnimationStrategy(FitAnimation(
            startScaleFactor,
            endScaleFactor,
            currentPoint.x,
            currentPoint.y
        ) { scale, x, y ->
            scaleFactor = scale
            currentPoint.set(x, y)
            applyScaleAndTranslation()
        })
        mindMapAnimator.executeAnimation()
    }

    fun removeNode() {
        mindMapManager.getSelectedNode()?.let { node ->
            val removeNodeCommand = RemoveNodeCommand(mindMapManager, node)
            removeNodeCommand.execute()
            mindMapAnimator.setAnimationStrategy(
                TreeChangeAnimation(mindMapManager) { updateNodeAndLine() }
            )
            mindMapAnimator.executeAnimation()
            requestLayout()
        }
    }

    fun editNodeText(description: String) {
        mindMapManager.getSelectedNode()?.let { node ->
            val updateNodeCommand = UpdateNodeCommand(mindMapManager, node, description)
            updateNodeCommand.execute()
            mindMapAnimator.setAnimationStrategy(
                TreeChangeAnimation(mindMapManager) { updateNodeAndLine() }
            )
            mindMapAnimator.executeAnimation()
            updateNodeAndLine()
        }
    }

    fun updateTree(tree: Tree) {
        mindMapManager.update(tree)
        mindMapAnimator.setAnimationStrategy(
            TreeChangeAnimation(mindMapManager) { updateNodeAndLine() }
        )
        mindMapAnimator.executeAnimation()
    }

    fun getTree(): Tree {
        return mindMapManager.getTree()
    }

    private fun updateNodeAndLine() {
        nodeView.update()
        lineView.update()
    }

    companion object {
        private const val DEFAULT_ZOOM = 1f
    }
}
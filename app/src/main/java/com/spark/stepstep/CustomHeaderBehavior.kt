package com.spark.stepstep

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CustomHeaderBehavior(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, attrs) {

    private var originalHeight = 0
    private var minHeight = 0
    private var currentHeight = 0
    private var maxOverScroll = 200f // 最大过度滚动距离

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is RecyclerView
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        parent.onLayoutChild(child, layoutDirection)

        if (originalHeight == 0) {
            originalHeight = child.height
            minHeight = (originalHeight * 2 / 3) // 缩小到原来的2/3
            currentHeight = originalHeight
        }

        return true
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (dy > 0) {
            // 向上滑动
            if (currentHeight > minHeight) {
                val newHeight = max(minHeight, currentHeight - dy)
                val delta = currentHeight - newHeight
                currentHeight = newHeight

                updateChildHeight(child, currentHeight)
                consumed[1] = delta
            }
        } else if (dy < 0) {
            // 向下滑动
            val recyclerView = target as? RecyclerView
            val canScrollUp = recyclerView?.canScrollVertically(-1) ?: false

            if (!canScrollUp && currentHeight < originalHeight) {
                val newHeight = min(originalHeight, currentHeight - dy)
                val delta = currentHeight - newHeight
                currentHeight = newHeight

                updateChildHeight(child, currentHeight)
                consumed[1] = delta
            }
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout, child, target,
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed
        )

        // 处理过度滚动（下拉超过原始高度）
        if (dyUnconsumed < 0 && currentHeight >= originalHeight) {
            val overScroll = min(abs(dyUnconsumed.toFloat()), maxOverScroll)
            val scale = 1f + (overScroll / maxOverScroll) * 0.1f // 最多放大10%

            child.scaleX = scale
            child.scaleY = scale
            child.pivotY = 0f
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)

        // 回弹动画
        child.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start()
    }

    private fun updateChildHeight(child: View, height: Int) {
        val params = child.layoutParams
        params.height = height
        child.layoutParams = params
    }
}
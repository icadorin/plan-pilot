package com.israel.planpilot

import android.graphics.Canvas
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

class GridDividerDecoration(
    color: Int,
    private val size: Float,
    private val isVertical: Boolean = true
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()

    init {
        paint.color = color
        paint.strokeWidth = size
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (isVertical) {
            drawVerticalLines(c, parent)
        } else {
            drawHorizontalLines(c, parent)
        }
    }

    private fun drawVerticalLines(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + size

            c.drawLine(left.toFloat(), top.toFloat(), right.toFloat(), bottom, paint)
        }
    }

    private fun drawHorizontalLines(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val left = child.right + params.rightMargin
            val right = left + size

            c.drawLine(left.toFloat(), top.toFloat(), right, bottom.toFloat(), paint)
        }
    }
}


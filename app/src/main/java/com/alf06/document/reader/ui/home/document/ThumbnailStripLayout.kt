package com.alf06.document.reader.ui.home.document

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val STRIP_PERCENT_PORTRAIT = 0.135f
private const val STRIP_PERCENT_LANDSCAPE = 0.13f

/**
 * Places the page thumbnail strip below [content] (scrolling horizontally) in portrait, or on its
 * right (scrolling vertically) in landscape. [header] is the view the strip sits under in landscape.
 */
fun ConstraintLayout.layoutThumbnailStrip(
    header: View,
    content: View,
    strip: RecyclerView,
    landscape: Boolean,
) {
    val id = strip.id
    ConstraintSet().apply {
        clone(this@layoutThumbnailStrip)
        clear(id)
        clear(content.id, ConstraintSet.BOTTOM)
        clear(content.id, ConstraintSet.END)
        constrainWidth(id, 0)
        constrainHeight(id, 0)
        connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        if (landscape) {
            constrainPercentWidth(id, STRIP_PERCENT_LANDSCAPE)
            connect(id, ConstraintSet.TOP, header.id, ConstraintSet.BOTTOM)
            connect(content.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(content.id, ConstraintSet.END, id, ConstraintSet.START)
        } else {
            constrainPercentHeight(id, STRIP_PERCENT_PORTRAIT)
            connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(content.id, ConstraintSet.BOTTOM, id, ConstraintSet.TOP)
            connect(content.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }
    }.applyTo(this)

    val padding = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp)
    if (landscape) {
        val top = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp)
        strip.setPadding(padding, top, padding, 0)
    } else {
        strip.setPadding(0, padding, 0, padding)
    }
    val orientation = if (landscape) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
    val manager = strip.layoutManager as? LinearLayoutManager
    if (manager?.orientation == orientation) return
    strip.layoutManager = LinearLayoutManager(strip.context, orientation, false)
    strip.recycledViewPool.clear()
}

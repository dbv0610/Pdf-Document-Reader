package com.wxiwei.office.pg.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.View
import com.wxiwei.office.common.shape.GroupShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.animate.Animation
import com.wxiwei.office.pg.animate.AnimationManager
import com.wxiwei.office.pg.animate.EmphanceAnimation
import com.wxiwei.office.pg.animate.FadeAnimation
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.pg.animate.ShapeAnimation
import com.wxiwei.office.pg.control.Presentation
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.system.beans.CalloutView.CalloutView

class SlideShowView(var presentation: Presentation?, var slide: PGSlide?) {
    private var paint: Paint? = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.SANS_SERIF
        textSize = 24f
    }
    private var bgRect = Rect()
    private var slideshowStep = 0
    private var animationMgr: AnimationManager? = null
    private var shapeVisible: MutableMap<Int, MutableMap<Int, IAnimation>>? = null
    private var animShapeArea: Rect? = null
    private var pageAnimation: IAnimation? = null
    private var animDuration = Animation.Duration

    private fun removeAnimation() {
        if (shapeVisible == null) {
            shapeVisible = HashMap()
        } else {
            shapeVisible!!.clear()
            slideshowStep = 0
        }
        animationMgr?.stopAnimation()
        presentation?.getEditor()?.clearAnimation()
        slide?.let {
            for (i in 0 until it.getShapeCount()) {
                it.getShape(i)?.let { shape -> removeShapeAnimation(shape) }
            }
        }
    }

    private fun removeShapeAnimation(shape: IShape) {
        if (shape is GroupShape) shape.getShapes().forEach { removeShapeAnimation(it) }
        else shape.getAnimation()?.let {
            shape.setAnimation(null)
            it.dispose()
        }
    }

    fun initSlideShow(slide: PGSlide?, showAnimation: Boolean) {
        removeAnimation()
        this.slide = slide
        if (slide == null) return
        slide.getSlideShowAnimation()?.forEach { shapeAnim ->
            val map = shapeVisible!!.getOrPut(shapeAnim.getShapeID()) { HashMap() }
            for (para in shapeAnim.getParagraphBegin()..shapeAnim.getParagraphEnd()) {
                if (!map.containsKey(para)) {
                    val animation = FadeAnimation(shapeAnim, animDuration)
                    for (p in shapeAnim.getParagraphBegin()..shapeAnim.getParagraphEnd()) map[p] = animation
                    setShapeAnimation(shapeAnim.getShapeID(), animation)
                    break
                }
            }
        }
        if (animationMgr == null) animationMgr = presentation!!.getControl()!!.getSysKit().getAnimationManager()
        if (slide.hasTransition()) {
            if (pageAnimation == null) pageAnimation = FadeAnimation(ShapeAnimation(ShapeAnimation.Slide, ShapeAnimation.SA_ENTR), animDuration)
            else pageAnimation!!.setDuration(animDuration)
            animationMgr!!.setAnimation(pageAnimation)
            if (showAnimation) animationMgr!!.beginAnimation(1000 / pageAnimation!!.getFPS()) else animationMgr!!.stopAnimation()
        }
    }

    private fun setShapeAnimation(shapeID: Int, animation: IAnimation) {
        val current = slide ?: return
        for (i in 0 until current.getShapeCount()) {
            val shape = current.getShape(i) ?: continue
            if ((shape.getShapeID() == shapeID || shape.getGroupShapeID() == shapeID) && shape.getAnimation() == null) setShapeAnimation(shape, animation)
        }
    }

    private fun setShapeAnimation(shape: IShape, animation: IAnimation) {
        if (shape is GroupShape) shape.getShapes().forEach { setShapeAnimation(it, animation) } else shape.setAnimation(animation)
    }

    fun endSlideShow() = removeAnimation()
    fun isExitSlideShow(): Boolean = slide == null
    fun gotopreviousSlide(): Boolean = slide?.getSlideShowAnimation()?.let { slideshowStep <= 0 } ?: true
    fun gotoNextSlide(): Boolean = slide?.getSlideShowAnimation()?.let { slideshowStep >= it.size } ?: true

    fun previousActionSlideShow() {
        val old = slideshowStep - 1
        initSlideShow(slide, false)
        while (slideshowStep < old) {
            slideshowStep++
            updateShapeAnimation(slideshowStep, false)
        }
    }
    fun nextActionSlideShow() {
        slideshowStep++
        updateShapeAnimation(slideshowStep, true)
    }

    fun gotoLastAction() {
        while (!gotoNextSlide()) {
            slideshowStep++
            updateShapeAnimation(slideshowStep, false)
        }
    }

    private fun updateShapeAnimation(step: Int, showAnimation: Boolean) {
        val current = slide ?: return
        val list = current.getSlideShowAnimation() ?: return
        val shapeAnim = list[step - 1]
        updateShapeArea(shapeAnim.getShapeID(), presentation!!.getZoom())
        val animation = if (shapeAnim.getAnimationType() != ShapeAnimation.SA_EMPH) FadeAnimation(shapeAnim, animDuration) else EmphanceAnimation(shapeAnim, animDuration)
        shapeVisible!!.getOrPut(shapeAnim.getShapeID()) { HashMap() }[shapeAnim.getParagraphBegin()] = animation
        updateShapeAnimation(shapeAnim.getShapeID(), animation, showAnimation)
    }

    private fun updateShapeAnimation(shapeID: Int, animation: IAnimation, showAnimation: Boolean) {
        val current = slide ?: return
        animationMgr!!.setAnimation(animation)
        for (i in 0 until current.getShapeCount()) {
            val shape = current.getShape(i) ?: continue
            if (shape.getShapeID() == shapeID || shape.getGroupShapeID() == shapeID) setShapeAnimation(shape, animation)
        }
        if (showAnimation) animationMgr!!.beginAnimation(1000 / animation.getFPS()) else animationMgr!!.stopAnimation()
    }

    private fun updateShapeArea(shapeID: Int, zoom: Float) {
        val current = slide ?: return
        for (i in 0 until current.getShapeCount()) {
            val shape = current.getShape(i) ?: continue
            val rect = shape.takeIf { it.getShapeID() == shapeID }?.getBounds() ?: continue
            val left = Math.round(rect.x * zoom)
            val top = Math.round(rect.y * zoom)
            val width = Math.round(rect.width * zoom)
            val height = Math.round(rect.height * zoom)
            if (animShapeArea == null) animShapeArea = Rect(left, top, left + width, top + height) else animShapeArea!!.set(left, top, left + width, top + height)
            return
        }
        animShapeArea = null
    }

    fun changeSlide(slide: PGSlide?) { this.slide = slide }
    fun getDrawingRect(): Rect = bgRect

    fun drawSlide(canvas: Canvas, zoomValue: Float, callouts: CalloutView?) {
        var zoom = zoomValue
        if (pageAnimation != null && pageAnimation!!.getAnimationStatus() != Animation.AnimStatus_End) {
            zoom *= pageAnimation!!.getCurrentAnimationInfor()!!.getProgress()
            if (zoom <= 0.001f) return
        }
        val d: Dimension = presentation!!.getPageSize()!!
        val w = (d.width * zoom).toInt()
        val h = (d.height * zoom).toInt()
        val x = (presentation!!.getmWidth() - w) / 2
        val y = (presentation!!.getmHeight() - h) / 2
        canvas.save()
        canvas.translate(x.toFloat(), y.toFloat())
        canvas.clipRect(0, 0, w, h)
        bgRect.set(0, 0, w, h)
        SlideDrawKit.instance().drawSlide(canvas, presentation!!.getPGModel()!!, presentation!!.getEditor(), slide!!, zoom, shapeVisible)
        canvas.restore()
        if (callouts != null) {
            if (pageAnimation != null && pageAnimation!!.getAnimationStatus() != Animation.AnimStatus_End) callouts.visibility = View.INVISIBLE
            else {
                callouts.setZoom(zoom)
                callouts.layout(x, y, x + w, y + h)
                callouts.visibility = View.VISIBLE
            }
        }
    }

    fun drawSlideForToPicture(canvas: Canvas, zoomValue: Float, originBitmapW: Int, originBitmapH: Int) {
        var zoom = zoomValue
        val rect = canvas.clipBounds
        if (rect.width() != originBitmapW || rect.height() != originBitmapH) zoom *= minOf(rect.width().toFloat() / originBitmapW, rect.height().toFloat() / originBitmapH)
        SlideDrawKit.instance().drawSlide(canvas, presentation!!.getPGModel()!!, presentation!!.getEditor(), slide!!, zoom, shapeVisible)
    }

    fun animationStoped(): Boolean = animationMgr?.hasStoped() ?: true
    fun setAnimationDuration(duration: Int) { animDuration = duration }
    fun getSlideshowToImage(slide: PGSlide?, step: Int): Bitmap? {
        this.slide = slide
        initSlideShow(slide, false)
        while (slideshowStep < step - 1) {
            slideshowStep++
            updateShapeAnimation(slideshowStep, false)
        }
        val image = SlideDrawKit.instance().slideToImage(presentation!!.getPGModel()!!, presentation!!.getEditor(), slide, shapeVisible)
        removeAnimation()
        return image
    }

    fun dispose() {
        paint = null
        presentation = null
        slide = null
        animationMgr?.dispose()
        animationMgr = null
        shapeVisible?.clear()
        shapeVisible = null
    }
}

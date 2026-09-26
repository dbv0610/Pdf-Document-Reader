package com.wxiwei.office.pg.animate

import android.graphics.Rect

interface IAnimation {
    class AnimationInformation(postion: Rect?, alpha: Int, angle: Int) {
        companion object { const val ROTATION: Int = 720 }

        private var alpha = alpha
        private var angle = angle
        private var postion: Rect? = postion?.let { Rect(it) }
        private var progress = 0f

        fun setAnimationInformation(postion: Rect?, alpha: Int, angle: Int) {
            if (postion != null) this.postion = Rect(postion)
            this.alpha = alpha
            this.angle = angle
            progress = 0f
        }
        fun setProgress(progress: Float) { this.progress = progress }
        fun getProgress(): Float = progress
        fun setPostion(postion: Rect?) { if (postion != null) this.postion = Rect(postion) }
        fun getPostion(): Rect? = postion
        fun setAlpha(alpha: Int) { this.alpha = alpha }
        fun getAlpha(): Int = alpha
        fun setAngle(angle: Int) { this.angle = angle }
        fun getAngle(): Int = angle
        fun dispose() { postion = null }
    }

    fun getAnimationStatus(): Byte
    fun start()
    fun stop()
    fun animation(frameIndex: Int)
    fun getCurrentAnimationInfor(): AnimationInformation?
    fun getShapeAnimation(): ShapeAnimation?
    fun getDuration(): Int
    fun setDuration(duration: Int)
    fun getFPS(): Int
    fun dispose()
}

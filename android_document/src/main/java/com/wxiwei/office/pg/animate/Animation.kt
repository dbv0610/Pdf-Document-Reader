package com.wxiwei.office.pg.animate

open class Animation(shapeAnim: ShapeAnimation?, duration: Int = Duration, fps: Int = FPS) : IAnimation {
    companion object {
        const val AnimStatus_NotStarted: Byte = 0
        const val AnimStatus_Animating: Byte = 1
        const val AnimStatus_End: Byte = 2
        const val FadeIn: Byte = 0
        const val FadeOut: Byte = 1
        const val Duration: Int = 1200
        private const val FPS: Int = 15
    }
    protected var shapeAnim: ShapeAnimation? = shapeAnim
    protected var duration: Float = duration.toFloat()
    protected var fps: Int = fps
    protected var delay: Int = 1000 / fps
    protected var status: Byte = AnimStatus_NotStarted
    protected var begin: IAnimation.AnimationInformation? = null
    protected var end: IAnimation.AnimationInformation? = null
    protected var current: IAnimation.AnimationInformation? = null
    override fun start() { status = AnimStatus_Animating }
    override fun stop() { status = AnimStatus_End }
    override fun getCurrentAnimationInfor(): IAnimation.AnimationInformation? = current
    override fun getShapeAnimation(): ShapeAnimation? = shapeAnim
    override fun setDuration(duration: Int) { this.duration = duration.toFloat() }
    override fun getDuration(): Int = duration.toInt()
    override fun getFPS(): Int = fps
    override fun animation(frameIndex: Int) {}
    override fun getAnimationStatus(): Byte = status
    override fun dispose() {
        shapeAnim = null
        begin?.dispose(); begin = null
        end?.dispose(); end = null
        current?.dispose(); current = null
    }
}

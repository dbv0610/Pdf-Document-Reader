package com.wxiwei.office.pg.animate

class EmphanceAnimation : Animation {
    private companion object { const val ROTATION = 360 }
    constructor(shapeAnim: ShapeAnimation?) : super(shapeAnim) { current = IAnimation.AnimationInformation(null, 0, 0) }
    constructor(shapeAnim: ShapeAnimation?, duration: Int) : super(shapeAnim, duration) { current = IAnimation.AnimationInformation(null, 0, 0) }
    constructor(shapeAnim: ShapeAnimation?, duration: Int, fps: Int) : super(shapeAnim, duration, fps) { current = IAnimation.AnimationInformation(null, 0, 0) }
    override fun animation(frameIndex: Int) { if (shapeAnim?.getAnimationType() == ShapeAnimation.SA_EMPH) emphance(frameIndex * delay) }
    override fun start() { super.start(); current?.setAlpha(255); current?.setAngle(0); current?.setProgress(0f) }
    override fun stop() { super.stop(); current?.setAngle(0); current?.setAlpha(255); current?.setProgress(1f) }
    private fun emphance(time: Int) {
        current?.let {
            if (time < duration) { val progress = time / duration; it.setProgress(progress); it.setAngle((ROTATION * progress).toInt()) }
            else { status = AnimStatus_End; it.setProgress(1f); it.setAngle(0) }
        }
    }
}

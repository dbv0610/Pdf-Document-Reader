package com.wxiwei.office.pg.animate

class FadeAnimation : Animation {
    constructor(shapeAnim: ShapeAnimation?) : super(shapeAnim) { initAnimationKeyPoint() }
    constructor(shapeAnim: ShapeAnimation?, duration: Int) : super(shapeAnim, duration) { initAnimationKeyPoint() }
    constructor(shapeAnim: ShapeAnimation?, duration: Int, fps: Int) : super(shapeAnim, duration, fps) { initAnimationKeyPoint() }
    private fun initAnimationKeyPoint() {
        val enter = shapeAnim?.getAnimationType() == ShapeAnimation.SA_ENTR
        begin = IAnimation.AnimationInformation(null, if (shapeAnim == null || enter) 0 else 255, 0)
        end = IAnimation.AnimationInformation(null, if (shapeAnim == null || enter) 255 else 0, 0)
        current = IAnimation.AnimationInformation(null, if (shapeAnim == null || enter) 0 else 255, 0)
    }
    override fun animation(frameIndex: Int) {
        if (shapeAnim != null && current != null) when (shapeAnim?.getAnimationType()) {
            ShapeAnimation.SA_ENTR, ShapeAnimation.SA_EMPH -> fadeIn(frameIndex * delay)
            ShapeAnimation.SA_EXIT -> fadeOut(frameIndex * delay)
        }
    }
    override fun start() { super.start(); current?.setProgress(0f) }
    override fun stop() {
        super.stop(); current?.setAngle(0); current?.setProgress(1f)
        when (shapeAnim?.getAnimationType()) { ShapeAnimation.SA_ENTR -> current?.setAlpha(255); ShapeAnimation.SA_EXIT -> current?.setAlpha(0) }
    }
    private fun fadeIn(time: Int) { current?.let { if (time < duration) { val p = time / duration; it.setProgress(p); it.setAlpha((255 * p).toInt()) } else { status = AnimStatus_End; it.setProgress(1f); it.setAlpha(255) } } }
    private fun fadeOut(time: Int) { current?.let { if (time < duration) { val p = time / duration; it.setProgress(p); it.setAlpha((255 * (1 - p)).toInt()) } else { status = AnimStatus_End; it.setProgress(1f); it.setAlpha(0) } } }
}

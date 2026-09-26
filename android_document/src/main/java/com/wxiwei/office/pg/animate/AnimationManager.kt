package com.wxiwei.office.pg.animate

import com.wxiwei.office.system.*

import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.ITimerListener
import com.wxiwei.office.system.beans.ATimer

class AnimationManager(private var control: IControl?) : ITimerListener {
    private var animation: IAnimation? = null
    private var timer: ATimer? = null
    private var actionIndex = 0
    fun setAnimation(animation: IAnimation?) { if (this.animation != null && timer?.isRunning == true) { timer?.stop(); this.animation?.stop() }; this.animation = animation }
    fun beginAnimation(delay: Int) {
        if (timer == null) timer = ATimer(delay, this, control)
        animation?.let { actionIndex = 0; it.start(); timer?.start(); control?.officeToPicture?.setModeType(IOfficeToPicture.VIEW_CHANGING) }
    }
    fun restartAnimationTimer() { timer?.restart() }
    fun killAnimationTimer() { timer?.stop() }
    fun stopAnimation() {
        animation?.let { timer?.stop(); it.stop(); control?.officeToPicture?.setModeType(IOfficeToPicture.VIEW_CHANGE_END); control?.actionEvent(EventConstant.PG_REPAINT_ID, null) }
    }
    override fun actionPerformed() {
        if (animation != null && animation?.getAnimationStatus() != Animation.AnimStatus_End) { animation?.animation(++actionIndex); control?.actionEvent(EventConstant.PG_REPAINT_ID, null); timer?.restart() }
        else { timer?.stop(); control?.officeToPicture?.setModeType(IOfficeToPicture.VIEW_CHANGE_END); control?.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null) }
    }
    fun hasStoped(): Boolean = animation?.getAnimationStatus() == Animation.AnimStatus_End || animation == null
    fun dispose() { control = null; animation = null; timer?.dispose(); timer = null }
}

package com.wxiwei.office.common.borders

import com.wxiwei.office.common.bg.BackgroundAndFill

class Line : Border() {
    fun dispose() {
        if (this.backgroundAndFill != null) {
            this.backgroundAndFill = null
        }
    }

    /**
     * @return Returns the bgFill.
     */
    /**
     * @param bgFill The bgFill to set.
     */
    // background
    var backgroundAndFill: BackgroundAndFill? = null

    /**
     * this patheffect only affects drawing with the paint's style is set
     * to STROKE or STROKE_AND_FILL. It is ignored if the drawing is done with
     * style == FILL. app ignores dash type and only as dash
     * 
     * solid 		Solid
     * dot 			Dot
     * dash 			Dash
     * lgDash 		Large Dash
     * dashDot 		Dash Dot
     * lgDashDot 	Large Dash Dot
     * lgDashDotDot 	Large Dash Dot Dot
     * sysDash 		System Dash
     * sysDot 		System Dot
     * sysDashDot 	System Dash Dot
     * sysDashDotDot System Dash Dot Dot
     */
    var isDash: Boolean = false

    init {
        lineWidth = 1.toShort().toInt()
    }
}

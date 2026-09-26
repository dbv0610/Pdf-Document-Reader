package com.wxiwei.office.system.beans

import android.content.Context
import android.content.res.Configuration
import android.widget.LinearLayout

open class ADialogFrame(context: Context, private var dialog: ADialog?) : LinearLayout(context) {
    init {
        orientation = VERTICAL
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dialog?.onConfigurationChanged(newConfig)
    }

    fun dispose() {
        dialog = null
    }
}

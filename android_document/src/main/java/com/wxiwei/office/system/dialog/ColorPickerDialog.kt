package com.wxiwei.office.system.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.CalloutView.CalloutManager

class ColorPickerDialog(context: Context, private val control: IControl) : Dialog(context) {
    private val mContext: Context = context
    private val calloutMgr: CalloutManager = control.getSysKit().getCalloutManager()

    init {
        mInitialColor = calloutMgr.getColor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        @JvmField
        var mInitialColor: Int = 0
    }
}

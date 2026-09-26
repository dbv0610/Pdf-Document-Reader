package com.wxiwei.office.officereader.beans

import android.content.Context
import android.util.AttributeSet
import com.wxiwei.office.system.IControl

class CalloutToolsbar : AToolsbar {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, control: IControl) : super(context, control) { init() }

    private fun init() {
        // Buttons intentionally remain disabled, matching the original implementation.
    }
}

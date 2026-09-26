package com.wxiwei.office.system.dialog

import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import com.wxiwei.office.res.ResConstant
import com.wxiwei.office.system.IDialogAction
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.ADialog
import java.util.Vector

class QuestionDialog(control: IControl, context: Context, action: IDialogAction?, model: Vector<Any>?, dialogID: Int, titleResID: Int, message: String?) : ADialog(control, context, action, model, dialogID, titleResID) {
    private var textView: TextView? = null

    init {
        init(context, message)
    }

    fun init(context: Context, message: String?) {
        val width = context.resources.displayMetrics.widthPixels - MARGIN * 4
        textView = TextView(context)
        textView?.setPadding(5, 2, 5, 2)
        textView?.gravity = Gravity.TOP
        if (message != null) textView?.text = message
        val params = LayoutParams(width, LayoutParams.WRAP_CONTENT)
        params.leftMargin = GAP
        params.rightMargin = GAP
        params.topMargin = GAP * 2
        params.bottomMargin = GAP * 2
        params.gravity = Gravity.CENTER
        dialogFrame!!.addView(textView, params)
        val buttons = LinearLayout(context)
        buttons.gravity = Gravity.CENTER
        buttons.orientation = LinearLayout.HORIZONTAL
        val buttonParams = LayoutParams(width / 2, LayoutParams.WRAP_CONTENT)
        ok = Button(context)
        ok?.text = ResConstant.BUTTON_OK
        ok?.setOnClickListener(this)
        buttons.addView(ok, buttonParams)
        cancel = Button(context)
        cancel?.text = ResConstant.BUTTON_CANCEL
        cancel?.setOnClickListener(this)
        buttons.addView(cancel, buttonParams)
        dialogFrame!!.addView(buttons)
    }

    override fun onClick(v: View?) {
        if (v === ok) action?.doAction(dialogID, model)
        dismiss()
    }

    override fun doLayout() {
        var width = context.resources.displayMetrics.widthPixels
        width -= if (control!!.getSysKit().isVertical(context)) MARGIN * 4 else MARGIN * 12
        val params = LayoutParams(width, LayoutParams.WRAP_CONTENT)
        params.leftMargin = GAP
        params.rightMargin = GAP
        params.topMargin = GAP * 2
        params.bottomMargin = GAP * 2
        textView?.layoutParams = params
        val buttonParams = LayoutParams(width / 2, LayoutParams.WRAP_CONTENT)
        ok?.layoutParams = buttonParams
        cancel?.layoutParams = buttonParams
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        doLayout()
    }

    override fun dispose() {
        super.dispose()
        textView = null
    }
}

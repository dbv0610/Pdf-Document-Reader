package com.wxiwei.office.system.beans

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout.LayoutParams
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.system.IDialogAction
import com.wxiwei.office.system.IControl
import java.util.Vector

open class ADialog(
    @JvmField protected var control: IControl?,
    context: Context,
    @JvmField protected var action: IDialogAction?,
    @JvmField protected var model: Vector<Any>?,
    @JvmField protected var dialogID: Int,
    title: String?
) : Dialog(context), View.OnClickListener {
    @JvmField protected val GAP: Int = MainConstant.GAP
    @JvmField protected val MARGIN: Int = 30
    @JvmField protected var dialogFrame: ADialogFrame? = null
    @JvmField protected var ok: Button? = null
    @JvmField protected var cancel: Button? = null

    init {
        dialogFrame = ADialogFrame(context, this)
        setTitle(title)
    }

    constructor(
        control: IControl?,
        context: Context,
        action: IDialogAction?,
        model: Vector<Any>?,
        dialogID: Int,
        titleResID: Int
    ) : this(control, context, action, model, dialogID, context.resources.getString(titleResID)) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(dialogFrame!!)
        dialogFrame!!.post { doLayout() }
    }

    open fun onConfigurationChanged(newConfig: Configuration) {
    }

    override fun onClick(v: View?) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun dismiss() {
        super.dismiss()
        dispose()
    }

    open fun doLayout() {
    }

    protected fun setSize(w: Int, h: Int) {
        dialogFrame?.layoutParams = LayoutParams(w, h)
    }

    open fun dispose() {
        control = null
        model?.clear()
        model = null
        action = null
        dialogFrame?.dispose()
        dialogFrame = null
        ok = null
        cancel = null
    }
}

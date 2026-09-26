package com.wxiwei.office.pg.dialog

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import com.wxiwei.office.R
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import com.wxiwei.office.system.beans.ADialog
import java.util.Vector

class NotesDialog(control: IControl, context: Context, action: IDialogAction?, model: Vector<Any>?, dialogID: Int) :
    ADialog(control, context, action, model, dialogID, R.string.toolbar_note) {
    private var scrollView: ScrollView? = null
    private var notes: EditText? = null

    init { init(context) }

    fun init(context: Context) {
        notes = EditText(context).also {
            it.setBackgroundColor(Color.WHITE)
            it.textSize = 18f
            it.setPadding(5, 2, 5, 2)
            it.gravity = Gravity.TOP
        }
        if (model != null) {
            // TODO(coroutine): apply the dialog model on the UI thread; use Dispatchers.Main.
            val dialogModel = model
            dialogFrame!!.post { notes?.setText(dialogModel!![0] as String) }
        }
        scrollView = ScrollView(context).also {
            it.isFillViewport = true
            it.isHorizontalFadingEdgeEnabled = false
            it.setFadingEdgeLength(0)
            it.addView(notes)
            dialogFrame!!.addView(it)
        }
        ok = Button(context).also {
            it.setText(R.string.ok)
            it.setOnClickListener(this)
            dialogFrame!!.addView(it)
        }
    }

    override fun doLayout() {
        var width = context.resources.displayMetrics.widthPixels
        var height = context.resources.displayMetrics.heightPixels
        height -= window!!.decorView.height - dialogFrame!!.height
        width -= GAP * 10
        height -= GAP * 10
        val params = LayoutParams(width - GAP * 2, height - ok!!.height)
        params.leftMargin = GAP; params.rightMargin = GAP; params.bottomMargin = GAP
        scrollView?.layoutParams = params
    }

    override fun onConfigurationChanged(newConfig: Configuration) { doLayout() }
    override fun onClick(v: View?) { dismiss() }
    override fun dispose() { super.dispose(); scrollView = null; notes = null }
}

package com.alf06.document.reader.ui.dialog

import android.content.Context
import android.view.View
import com.alf06.document.reader.databinding.DialogProcessBinding
import com.ui.baselib.base.BaseDialog

class DialogProcess(context: Context) :
    BaseDialog<DialogProcessBinding>(context, DialogProcessBinding::inflate, cancelAble = false) {

    override fun DialogProcessBinding.initView() = Unit

    override fun show() {
        binding.txtProgress.visibility = View.GONE
        super.show()
    }

    /** Shows "[current]/[total]" under the spinner, e.g. pages written so far. */
    fun setProgress(current: Int, total: Int) {
        binding.txtProgress.text = "$current/$total"
        binding.txtProgress.visibility = View.VISIBLE
    }

    /** Shows or hides the dialog; no-op when already in that state so a re-emitted state doesn't flicker it. */
    fun setShowing(showing: Boolean) {
        if (showing == isShowing) return
        if (showing) show() else dismiss()
    }
}

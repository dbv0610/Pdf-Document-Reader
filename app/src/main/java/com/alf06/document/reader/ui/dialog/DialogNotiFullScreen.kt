package com.alf06.document.reader.ui.dialog

import android.content.Context
import androidx.core.graphics.toColorInt
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.DialogNotiFullBinding
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click
import com.ui.baselib.utils.textStyle

class DialogNotiFullScreen(context: Context, val onAllow: () -> Unit = {}) :
    BaseDialog<DialogNotiFullBinding>(
        context,
        DialogNotiFullBinding::inflate,
        cancelAble = false
    ) {
    override fun DialogNotiFullBinding.initView() {
        allowContent.text = textStyle {
            lineHeightPx(18)
            normal(text = context.getString(R.string.allow), textColor = "#80828D".toColorInt())
            bold(
                text = " ${context.getString(R.string.app_name)} ",
                textColor = "#FFFFFF".toColorInt()
            )
            normal(
                text = context.getString(R.string.to_send_you_notifications),
                textColor = "#80828D".toColorInt()
            )
        }
        btnAllow.click {
            dismiss()
            onAllow()
        }
    }
}
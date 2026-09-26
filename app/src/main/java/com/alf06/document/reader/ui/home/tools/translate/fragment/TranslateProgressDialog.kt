package com.alf06.document.reader.ui.home.tools.translate.fragment

import android.content.Context
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.DialogTranslateProgressBinding
import com.alf06.document.reader.databinding.ItemTranslateStepBinding
import com.alf06.document.reader.ui.home.tools.translate.TranslateStep
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

class TranslateProgressDialog(
    context: Context,
    private val onCancel: () -> Unit,
) : BaseDialog<DialogTranslateProgressBinding>(
    context,
    DialogTranslateProgressBinding::inflate,
    cancelAble = false
) {
    private val steps by lazy {
        listOf(binding.stepRead, binding.stepDownload, binding.stepTranslate, binding.stepWrite)
    }

    override fun DialogTranslateProgressBinding.initView() {
        stepRead.tvLabel.setText(R.string.translate_step_read)
        stepDownload.tvLabel.setText(R.string.translate_step_download)
        stepTranslate.tvLabel.setText(R.string.translate_step_translate)
        stepWrite.tvLabel.setText(R.string.translate_step_write)
        btnCancel.click { onCancel() }
        setOnKeyListener { _, keyCode, event ->
            if (keyCode != KeyEvent.KEYCODE_BACK) return@setOnKeyListener false
            if (event.action == KeyEvent.ACTION_UP) onCancel()
            true
        }
    }

    override fun show() {
        binding.progressIndicator.setProgress(0, animate = false)
        super.show()
    }

    fun render(step: TranslateStep) {
        val current = step.index
        steps.forEachIndexed { index, row ->
            row.setState(
                done = index < current,
                running = index == current,
                detail = if (index == current) detailOf(step) else null
            )
        }
        val percent = overallPercent(step)
        binding.tvPercent.text = context.getString(R.string.translate_percent, percent)
        binding.progressIndicator.setProgress(percent)
    }

    private fun ItemTranslateStepBinding.setState(
        done: Boolean,
        running: Boolean,
        detail: String?
    ) {
        pbRunning.isVisible = running
        imgState.isVisible = !running
        imgState.setImageResource(if (done) R.drawable.ic_app_circle_check else R.drawable.ic_app_circle_uncheck)
        tvLabel.setTextColor(
            ContextCompat.getColor(
                context,
                if (running || done) R.color.text_primary else R.color.text_secondary
            )
        )
        tvLabel.typeface =
            ResourcesCompat.getFont(context, if (running) R.font.rb_500 else R.font.rb_400)
        tvDetail.text = detail
        tvDetail.isVisible = detail != null
    }

    private fun detailOf(step: TranslateStep): String? = when (step) {
        is TranslateStep.Reading -> "${step.done + 1}/${step.total}"
        is TranslateStep.Downloading -> step.percent?.let { "$it%" }
        is TranslateStep.Translating -> "${fraction(step.done, step.total)}%"
        TranslateStep.Writing -> null
    }

    private val TranslateStep.index: Int
        get() = when (this) {
            is TranslateStep.Reading -> 0
            is TranslateStep.Downloading -> 1
            is TranslateStep.Translating -> 2
            TranslateStep.Writing -> 3
        }

    private fun overallPercent(step: TranslateStep): Int = when (step) {
        is TranslateStep.Reading -> fraction(step.done, step.total) * READ_SHARE / 100
        is TranslateStep.Downloading -> READ_SHARE + (step.percent ?: 0) * DOWNLOAD_SHARE / 100
        is TranslateStep.Translating ->
            READ_SHARE + DOWNLOAD_SHARE + fraction(step.done, step.total) * TRANSLATE_SHARE / 100

        TranslateStep.Writing -> READ_SHARE + DOWNLOAD_SHARE + TRANSLATE_SHARE
    }

    private fun fraction(done: Int, total: Int) = done * 100 / total.coerceAtLeast(1)

    private companion object {
        const val READ_SHARE = 25
        const val DOWNLOAD_SHARE = 25
        const val TRANSLATE_SHARE = 45
    }
}

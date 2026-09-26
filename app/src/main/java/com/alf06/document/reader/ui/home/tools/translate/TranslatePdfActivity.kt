package com.alf06.document.reader.ui.home.tools.translate

import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivityTranslatePdfBinding
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.translate.fragment.TranslateLanguageFragment
import com.alf06.document.reader.ui.home.tools.translate.fragment.TranslateResultFragment
import com.alf06.document.reader.ui.home.tools.translate.fragment.TranslateSelectPagesFragment
import com.alf06.document.reader.ui.home.tools.translate.fragment.TranslateSelectPdfFragment
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.base.NavGraph
import com.ui.baselib.extensions.newInstanceWithArgs

class TranslatePdfActivity : BaseActivity<ActivityTranslatePdfBinding>(ActivityTranslatePdfBinding::inflate) {
    override val fullStatus: Boolean
        get() = true

    override fun backPressed() {
        if (!goBackFragment()) {
            finish()
        }
    }

    override val fragmentContainerId: Int
        get() = R.id.fragmentContainerTranslatePdf
    override val navGraph: NavGraph
        get() = NavGraph().apply {
            addDestination(TranslateNavigator.SelectFile.route, isStartDestination = true) { newInstanceWithArgs<TranslateSelectPdfFragment>() }
            addDestination(TranslateNavigator.SelectPages.route) { newInstanceWithArgs<TranslateSelectPagesFragment>() }
            addDestination(TranslateNavigator.Language.route) { newInstanceWithArgs<TranslateLanguageFragment>() }
            addDestination(TranslateNavigator.Result.route) { newInstanceWithArgs<TranslateResultFragment>() }
            addDestination(TranslateNavigator.Success.route) { args -> SuccessFragment().apply { arguments = args } }
        }

    override fun initialize() {
    }

    override fun ActivityTranslatePdfBinding.setData() {
    }

    override fun ActivityTranslatePdfBinding.onClick() {
    }
}

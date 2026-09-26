package com.alf06.document.reader.ui.home.tools.split

import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivitySplitPdfBinding
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.split.fragment.SelectPagesFragment
import com.alf06.document.reader.ui.home.tools.split.fragment.SelectPdfFragment
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.base.NavGraph
import com.ui.baselib.extensions.newInstanceWithArgs

class SplitPdfActivity : BaseActivity<ActivitySplitPdfBinding>(ActivitySplitPdfBinding::inflate) {
    override val fullStatus: Boolean
        get() = true

    override fun backPressed() {
        if (!goBackFragment()) {
            finish()
        }
    }

    override val fragmentContainerId: Int
        get() = R.id.fragmentContainerSplitPdf
    override val navGraph: NavGraph
        get() = NavGraph().apply {
            addDestination(SplitNavigator.SelectFile.route, isStartDestination = true) { newInstanceWithArgs<SelectPdfFragment>() }
            addDestination(SplitNavigator.SelectPages.route) { newInstanceWithArgs<SelectPagesFragment>() }
            addDestination(SplitNavigator.Success.route) { args -> SuccessFragment().apply { arguments = args } }
        }

    override fun initialize() {
    }

    override fun ActivitySplitPdfBinding.setData() {
    }

    override fun ActivitySplitPdfBinding.onClick() {
    }
}

package com.alf06.document.reader.ui.home.tools.merge

import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivityMergePdfBinding
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.merge.fragment.MergeFilesFragment
import com.alf06.document.reader.ui.home.tools.merge.fragment.SelectPdfsFragment
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.base.NavGraph
import com.ui.baselib.extensions.newInstanceWithArgs

class MergePdfActivity : BaseActivity<ActivityMergePdfBinding>(ActivityMergePdfBinding::inflate) {
    override val fullStatus: Boolean
        get() = true

    override fun backPressed() {
        if (!goBackFragment()) {
            finish()
        }
    }

    override val fragmentContainerId: Int
        get() = R.id.fragmentContainerMergePdf
    override val navGraph: NavGraph
        get() = NavGraph().apply {
            addDestination(MergeNavigator.SelectFiles.route, isStartDestination = true) { newInstanceWithArgs<SelectPdfsFragment>() }
            addDestination(MergeNavigator.MergeFiles.route) { newInstanceWithArgs<MergeFilesFragment>() }
            addDestination(MergeNavigator.Success.route) { args -> SuccessFragment().apply { arguments = args } }
        }

    override fun initialize() {
    }

    override fun ActivityMergePdfBinding.setData() {
    }

    override fun ActivityMergePdfBinding.onClick() {
    }
}

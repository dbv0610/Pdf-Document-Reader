package com.alf06.document.reader.ui.home.tools.zip

import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivityCreateZipBinding
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.zip.fragment.SelectZipFilesFragment
import com.alf06.document.reader.ui.home.tools.zip.fragment.ZipFilesFragment
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.base.NavGraph
import com.ui.baselib.extensions.newInstanceWithArgs

class CreateZipActivity : BaseActivity<ActivityCreateZipBinding>(ActivityCreateZipBinding::inflate) {
    override val fullStatus: Boolean
        get() = true

    override fun backPressed() {
        if (!goBackFragment()) {
            finish()
        }
    }

    override val fragmentContainerId: Int
        get() = R.id.fragmentContainerCreateZip
    override val navGraph: NavGraph
        get() = NavGraph().apply {
            addDestination(ZipNavigator.SelectFiles.route, isStartDestination = true) { newInstanceWithArgs<SelectZipFilesFragment>() }
            addDestination(ZipNavigator.ZipFiles.route) { newInstanceWithArgs<ZipFilesFragment>() }
            addDestination(ZipNavigator.Success.route) { args -> SuccessFragment().apply { arguments = args } }
        }

    override fun initialize() {
    }

    override fun ActivityCreateZipBinding.setData() {
    }

    override fun ActivityCreateZipBinding.onClick() {
    }
}

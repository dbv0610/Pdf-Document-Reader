package com.alf06.document.reader.ui.home.tools.image_to_pdf

import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivityImageToPdfBinding
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.image_to_pdf.fragment.ConvertFragment
import com.alf06.document.reader.ui.home.tools.image_to_pdf.fragment.SelectImageFragment
import com.alf06.document.reader.ui.home.tools.image_to_pdf.fragment.ViewImageFragment
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.base.NavGraph
import com.ui.baselib.extensions.newInstanceWithArgs

class ImageToPdfActivity : BaseActivity<ActivityImageToPdfBinding>(ActivityImageToPdfBinding::inflate){
    override val fullStatus: Boolean
        get() = true
    override fun backPressed() {
        if(!goBackFragment()){
            finish()
        }
    }
    override val fragmentContainerId: Int
        get() = R.id.fragmentContainerImagePDF
    override val navGraph: NavGraph
        get() = NavGraph().apply {
            addDestination(ImagePdfNavigator.SelectImage.route, isStartDestination = true) { newInstanceWithArgs<SelectImageFragment>() }
            addDestination(ImagePdfNavigator.Convert.route) { newInstanceWithArgs<ConvertFragment>() }
            addDestination(ImagePdfNavigator.Success.route) { args -> SuccessFragment().apply { arguments = args } }
            addDestination(ImagePdfNavigator.ViewImage.route) { args -> ViewImageFragment().apply { arguments = args } }
        }

    override fun initialize() {
    }

    override fun ActivityImageToPdfBinding.setData() {
    }

    override fun ActivityImageToPdfBinding.onClick() {
    }
}

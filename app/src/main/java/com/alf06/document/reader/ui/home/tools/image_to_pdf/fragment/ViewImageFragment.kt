package com.alf06.document.reader.ui.home.tools.image_to_pdf.fragment

import androidx.viewpager2.widget.MarginPageTransformer
import com.alf06.document.reader.databinding.FragmentViewImageBinding
import com.alf06.document.reader.ui.adapter.ViewImageAdapter
import com.alf06.document.reader.ui.home.tools.image_to_pdf.SelectImageViewModel
import com.intuit.sdp.R
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ViewImageFragment :
    BaseFragment<FragmentViewImageBinding>(FragmentViewImageBinding::inflate) {
    private val viewModel: SelectImageViewModel by activityViewModel()
    private val adapter = ViewImageAdapter()
    private var startImageId: Long? = null

    override fun FragmentViewImageBinding.initView() {
        startImageId = arguments?.getLong(ARG_IMAGE_ID)
        vpImages.adapter = adapter
        vpImages.offscreenPageLimit = 1
        vpImages.setPageTransformer(
            MarginPageTransformer(resources.getDimensionPixelSize(R.dimen._4sdp))
        )

        collectFlow(viewModel.images) { images ->
            adapter.submitList(images) {
                val id = startImageId ?: return@submitList
                val index = images.indexOfFirst { it.id == id }
                if (index >= 0) {
                    vpImages.setCurrentItem(index, false)
                    startImageId = null
                }
            }
        }
    }

    override fun FragmentViewImageBinding.onClick() {
        icClose.click { navHost.goBackFragment() }
    }

    companion object {
        const val ARG_IMAGE_ID = "image_id"
    }
}

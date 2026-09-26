package com.alf06.document.reader.ui.home.tools.image_to_pdf.fragment

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentSelectImageBinding
import com.alf06.document.reader.model.GalleryImage
import com.alf06.document.reader.ui.adapter.GalleryImageAdapter
import com.alf06.document.reader.ui.adapter.ImageFolderAdapter
import com.alf06.document.reader.ui.adapter.SelectedImageAdapter
import com.alf06.document.reader.ui.home.tools.image_to_pdf.ImagePdfNavigator
import com.alf06.document.reader.ui.home.tools.image_to_pdf.SelectImageViewModel
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SelectImageFragment :
    BaseFragment<FragmentSelectImageBinding>(FragmentSelectImageBinding::inflate) {
    private val viewModel: SelectImageViewModel by activityViewModel()

    private val imageAdapter = GalleryImageAdapter(
        onToggle = { viewModel.toggle(it) },
        onOpenItem = { image ->
            navigateTo(ImagePdfNavigator.ViewImage.route, ViewImageFragment.ARG_IMAGE_ID to image.id)
        }
    )
    private val selectedAdapter = SelectedImageAdapter(onRemove = { viewModel.remove(it) })
    private val folderAdapter = ImageFolderAdapter(onClick = { folder ->
        viewModel.selectFolder(folder)
        showFolders(false)
    })

    override fun onBackPressed(): Boolean {
        handleBack()
        return true
    }

    override fun FragmentSelectImageBinding.initView() {
        rcvImages.adapter = imageAdapter
        rcvSelected.adapter = selectedAdapter
        rcvFolders.adapter = folderAdapter

        collectFlow(combine(viewModel.images, viewModel.isLoaded) { images, loaded -> images to loaded }) { (images, loaded) ->
            imageAdapter.submitList(images)
            val empty = loaded && images.isEmpty()
            layoutEmpty.isVisible = empty
            rcvImages.isVisible = !empty
            btnImport.isVisible = !empty
            icSelectAll.isVisible = !empty
        }
        collectFlow(viewModel.selected) { selected -> renderSelection(selected) }
        collectFlow(viewModel.folders) { folders -> folderAdapter.submitList(folders) }
        collectFlow(viewModel.currentFolder) { folder ->
            tvFolder.text = when {
                folder?.id == null -> getString(R.string.all_images)
                folder.name.isEmpty() -> getString(R.string.internal_storage)
                else -> folder.name
            }
        }
    }

    override fun FragmentSelectImageBinding.onClick() {
        icBack.click { handleBack() }
        btnFolder.click { showFolders(!rcvFolders.isVisible) }
        viewFolderDim.click { showFolders(false) }
        icSelectAll.click { viewModel.toggleSelectAll() }
        btnImport.click {
            if (viewModel.selected.value.isNotEmpty()) navigateTo(ImagePdfNavigator.Convert.route)
        }
    }

    private fun renderSelection(selected: List<GalleryImage>) = with(binding) {
        imageAdapter.setSelectedIds(selected.mapTo(HashSet()) { it.id })
        val wasEmpty = selectedAdapter.itemCount == 0
        selectedAdapter.submitList(selected) {
            if (!wasEmpty && selected.isNotEmpty()) rcvSelected.scrollToPosition(selected.lastIndex)
        }
        layoutSelected.isVisible = selected.isNotEmpty()
        btnImport.isEnabled = selected.isNotEmpty()
        btnImport.text = getString(R.string.import_count, selected.size)
    }

    private fun showFolders(show: Boolean) = with(binding) {
        if (show) {
            rcvFolders.updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintMaxHeight = (root.height * 0.6f).toInt()
            }
        }
        rcvFolders.isVisible = show
        viewFolderDim.isVisible = show
        icFolderArrow.animate().rotation(if (show) 180f else 0f).setDuration(150).start()
    }

    private fun handleBack() {
        if (binding.rcvFolders.isVisible) showFolders(false) else activity?.finish()
    }
}

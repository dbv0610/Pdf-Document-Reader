package com.alf06.document.reader.ui.dialog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.BottomSheetFileOptionsBinding
import com.alf06.document.reader.databinding.ItemFileOptionBinding
import com.alf06.document.reader.model.RecentUi
import com.ui.baselib.base.BaseBottomSheet
import com.ui.baselib.extensions.click
import java.io.File

class FileOptionsBottomSheet(
    activity: FragmentActivity,
    private val item: RecentUi,
    private val onRename: () -> Unit,
    private val onFavorite: () -> Unit,
    private val onShare: () -> Unit,
    private val onDelete: () -> Unit,
    private val onThumbnail: (() -> Unit)? = null,
    private val onPageByPage: (() -> Unit)? = null,
    private val onSlideShow: (() -> Unit)? = null,
    private val onSlideList: (() -> Unit)? = null,
    private val onExportPdf: (() -> Unit)? = null,
    private val onExportImages: (() -> Unit)? = null,
) : BaseBottomSheet<BottomSheetFileOptionsBinding>(activity, BottomSheetFileOptionsBinding::inflate) {

    override fun BottomSheetFileOptionsBinding.onBind() {
        tvFileName.text = File(item.document.path).name
        onThumbnail?.let { btnThumbnail.setup(R.drawable.ic_page_thumnail, R.string.thumbnail, it) }
        onPageByPage?.let { btnPageByPage.setup(R.drawable.ic_page_bypage, R.string.page_by_page, it) }
        onSlideShow?.let { btnSlideShow.setup(R.drawable.ic_slide_show, R.string.slide_show, it) }
        onSlideList?.let { btnSlideList.setup(R.drawable.ic_page_bypage, R.string.slide_list, it) }
        onExportPdf?.let { btnExportPdf.setup(R.drawable.ic_app_download, R.string.export_pdf, it) }
        onExportImages?.let { btnExportImages.setup(R.drawable.ic_app_image, R.string.export_images, it) }
        btnRename.setup(R.drawable.ic_app_edit, R.string.file_action_rename, onRename)
        btnFavorite.setup(R.drawable.ic_app_fav, R.string.file_action_favorite, onFavorite)
        if (item.isFavorite) btnFavorite.imgIcon.imageTintList = null
        btnShare.setup(R.drawable.ic_app_share, R.string.file_action_share, onShare)
        btnDelete.setup(R.drawable.ic_app_trashcan, R.string.file_action_delete, onDelete)
    }

    private fun ItemFileOptionBinding.setup(
        @DrawableRes icon: Int,
        @StringRes label: Int,
        action: () -> Unit,
    ) {
        root.isVisible = true
        imgIcon.setImageResource(icon)
        tvLabel.setText(label)
        root.click {
            dismissSafe()
            action()
        }
    }
}

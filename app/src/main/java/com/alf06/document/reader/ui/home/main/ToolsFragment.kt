package com.alf06.document.reader.ui.home.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentToolsBinding
import com.alf06.document.reader.databinding.ItemToolsOptionBinding
import com.alf06.document.reader.ui.home.tools.image_to_pdf.ImageToPdfActivity
import com.alf06.document.reader.ui.home.tools.merge.MergePdfActivity
import com.alf06.document.reader.ui.home.tools.split.SplitPdfActivity
import com.alf06.document.reader.ui.home.tools.translate.TranslatePdfActivity
import com.alf06.document.reader.ui.home.tools.zip.CreateZipActivity
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.widget.layout.UiConstraintLayout
import com.alf06.document.reader.ui.home.setting.SettingActivity

class ToolsFragment : BaseFragment<FragmentToolsBinding>(FragmentToolsBinding::inflate) {
    override fun FragmentToolsBinding.initView() {
    }

    override fun FragmentToolsBinding.onClick() {
        icSetting.click { launchActivity<SettingActivity>() }
        itemTranslatePdf.setUpItemView(R.string.translate_pdf, R.drawable.img_tools_option_1).click {
            launchActivity<TranslatePdfActivity>()
        }
        itemScanToPdf.setUpItemView(R.string.scan_pdf, R.drawable.img_tools_option_2).click {
        }
        itemImagePdf.setUpItemView(R.string.image_to_pdf, R.drawable.img_tools_option_3).click {
            launchActivity<ImageToPdfActivity>()
        }
        itemSplitPdf.setUpItemView(R.string.split_pdf, R.drawable.img_tools_option_4).click {
            launchActivity<SplitPdfActivity>()
        }
        itemMergePdf.setUpItemView(R.string.merge_pdf, R.drawable.img_tools_option_5).click {
            launchActivity<MergePdfActivity>()
        }
        itemCreateZip.setUpItemView(R.string.create_zip_file, R.drawable.img_tools_option_6).click {
            launchActivity<CreateZipActivity>()
        }
    }

    fun ItemToolsOptionBinding.setUpItemView(
        @StringRes title: Int,
        @DrawableRes icon: Int
    ): UiConstraintLayout {
        tvTitle.setText(title)
        imgContent.setImageResource(icon)
        return root
    }
}

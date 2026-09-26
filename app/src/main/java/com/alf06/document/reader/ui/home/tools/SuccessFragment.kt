package com.alf06.document.reader.ui.home.tools

import android.text.format.Formatter
import com.alf06.document.reader.R
import com.alf06.document.reader.base.openFileWith
import com.alf06.document.reader.base.shareFile
import com.alf06.document.reader.databinding.FragmentSuccessBinding
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.ui.home.document.openDocument
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class SuccessFragment : BaseFragment<FragmentSuccessBinding>(FragmentSuccessBinding::inflate) {
    private val file: File by lazy { File(arguments?.getString(ARG_PATH).orEmpty()) }
    private val isZip: Boolean by lazy { file.extension.equals("zip", ignoreCase = true) }

    override fun FragmentSuccessBinding.initView() {
        tvTitle.setText(arguments?.getInt(ARG_TITLE)?.takeIf { it != 0 } ?: R.string.converted_successfully)
        tvFileName.text = file.name
        if (isZip) {
            imgFile.setImageResource(R.drawable.ic_app_zip_1)
            btnOpenPdf.setText(R.string.open)
        }
        tvFileInfo.text = getString(
            R.string.success_file_info,
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified())),
            Formatter.formatShortFileSize(appContext(), file.length())
        )
    }

    override fun FragmentSuccessBinding.onClick() {
        icClose.click {navHost.popToRoot() }
        btnShare.click {
            if (!requireContext().shareFile(file.path)) toast(R.string.file_share_failed)
        }
        btnOpenPdf.click {
            if (isZip) {
                if (!requireContext().openFileWith(file.path)) toast(R.string.file_open_with_failed)
                return@click
            }
            val document = RecentDocument(
                path = file.path,
                lastModified = file.lastModified(),
                size = file.length(),
                type = DocumentType.Pdf
            )
            requireContext().openDocument(document)
          navHost.popToRoot()
        }
    }

    override fun onBackPressed(): Boolean {
        navHost.popToRoot()
        return true
    }

    companion object {
        const val ARG_PATH = "path"

        const val ARG_TITLE = "title"
    }
}

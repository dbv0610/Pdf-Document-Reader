package com.alf06.document.reader.ui.adapter

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemDocumentViewBinding
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.utils.formatDateByMillis
import com.ui.baselib.base.DiffAdapter
import java.io.File

class DocumentAdapter(
    private val onItemClick: (RecentUi) -> Unit,
    private val onFavoriteClick: (RecentUi) -> Unit,
    private val onMoreClick: (RecentUi) -> Unit,
) : DiffAdapter<RecentUi, ItemDocumentViewBinding>(
    areItemsTheSame = { old, new -> old.document.mediaId == new.document.mediaId }
) {

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemDocumentViewBinding = ItemDocumentViewBinding.inflate(inflater, parent, false)

    override fun ItemDocumentViewBinding.bind(item: RecentUi, position: Int) {
        val document = item.document
        val context = root.context
        imgSource.setImageResource(document.type.iconRes())
        tvFileName.text = File(document.path).name
        tvFileInfo.text = context.getString(
            R.string.document_info,
            formatDateByMillis(document.lastModified),
            Formatter.formatShortFileSize(context, document.size)
        )
        imgFavorite.setImageResource(
            if (item.isFavorite) R.drawable.ic_app_fav else R.drawable.ic_app_fa_no
        )
        root.setOnClickListener { onItemClick(item) }
        imgFavorite.setOnClickListener { onFavoriteClick(item) }
        imgTools.setOnClickListener { onMoreClick(item) }
    }
}

@DrawableRes
internal fun DocumentType.iconRes(): Int = when (this) {
    DocumentType.Pdf -> R.drawable.ic_app_pdf
    DocumentType.Excel -> R.drawable.ic_app_xls
    DocumentType.Ppt -> R.drawable.ic_app_ppt
    DocumentType.Txt -> R.drawable.ic_app_txt
    DocumentType.Doc, DocumentType.Image -> R.drawable.ic_app_word
}

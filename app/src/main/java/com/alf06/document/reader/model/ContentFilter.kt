package com.alf06.document.reader.model

import androidx.annotation.StringRes
import com.alf06.document.reader.R

enum class SortByData {
    SortByName, SortBySize, SortByDate, None
}

enum class SortOrder {
    Increase, Decrease
}

enum class SortDateType {
    NewToOld, OldToNew, NoSelect
}

enum class SortSizeType {
    BigToSmall, SmallToBig, NoSelect
}

/** Tabs of the home file menu: All file, PDF, Word, Excel, PPT, TXT. */
enum class FileTypeFilter(@StringRes val title: Int, val documentType: DocumentType?) {
    All(R.string.all_file, null),
    Pdf(R.string.tab_pdf, DocumentType.Pdf),
    Word(R.string.tab_word, DocumentType.Doc),
    Excel(R.string.tab_excel, DocumentType.Excel),
    Ppt(R.string.tab_ppt, DocumentType.Ppt),
    Txt(R.string.tab_txt, DocumentType.Txt);

    fun accepts(type: DocumentType): Boolean =
        documentType == null || documentType == type
}

/** Tabs of the Storage screen. */
enum class StorageTab {
    Recent, Favorites
}

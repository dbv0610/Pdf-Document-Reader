package com.alf06.document.reader.di

import com.alf06.document.reader.ui.home.document.office.ReadDocumentViewModel
import com.alf06.document.reader.ui.home.document.pdf.ReadPdfViewModel
import com.alf06.document.reader.ui.home.scanner.ScanSessionViewModel
import com.alf06.document.reader.ui.home.tools.image_to_pdf.SelectImageViewModel
import com.alf06.document.reader.ui.home.tools.merge.MergePdfViewModel
import com.alf06.document.reader.ui.home.tools.split.SplitPdfViewModel
import com.alf06.document.reader.ui.home.tools.translate.TranslatePdfViewModel
import com.alf06.document.reader.ui.home.tools.zip.CreateZipViewModel
import com.alf06.document.reader.viewmodel.DocumentViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::DocumentViewModel)
    viewModelOf(::ReadPdfViewModel)
    viewModelOf(::ReadDocumentViewModel)
    viewModelOf(::SelectImageViewModel)
    viewModelOf(::SplitPdfViewModel)
    viewModelOf(::MergePdfViewModel)
    viewModelOf(::CreateZipViewModel)
    viewModelOf(::TranslatePdfViewModel)
    viewModelOf(::ScanSessionViewModel)
}

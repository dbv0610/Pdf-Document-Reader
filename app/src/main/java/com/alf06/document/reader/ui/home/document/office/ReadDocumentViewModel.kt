package com.alf06.document.reader.ui.home.document.office

import androidx.lifecycle.ViewModel
import com.alf06.document.reader.model.PageViewType
import com.alf06.document.reader.utils.AppUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class ReadDocumentViewModel : ViewModel() {
    private val _pageViewState = MutableStateFlow(PageViewType.PageByPage)
    val pageViewState = _pageViewState.asStateFlow()

    private var defaultPageStateApplied = false

    fun setPageState(state: PageViewType) {
        defaultPageStateApplied = true
        _pageViewState.value = state
    }

    fun applyDefaultPageState(state: PageViewType) {
        if (defaultPageStateApplied) return
        defaultPageStateApplied = true
        _pageViewState.value = state
    }

    companion object {
        private const val TAG = "ReadDocumentViewModel"

        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 20

        fun pdfFile(name: String) = File(AppUtils.documentPath, "$name.pdf")
    }
}

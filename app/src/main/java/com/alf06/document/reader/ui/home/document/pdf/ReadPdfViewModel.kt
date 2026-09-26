package com.alf06.document.reader.ui.home.document.pdf

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alf06.document.reader.model.ContentWithPage
import com.alf06.document.reader.model.DocumentPage
import com.alf06.document.reader.model.PageViewType
import com.alf06.document.reader.model.RecentDocument
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ReadPdfViewModel : ViewModel() {
    var document: RecentDocument? = null
        private set
    val pdfPath: String? get() = document?.path

    var page: Int = 1

    private val _pageViewState = MutableStateFlow(PageViewType.PageByPage)
    val pageViewState = _pageViewState.asStateFlow()

    private val _pages = MutableStateFlow<List<DocumentPage>>(emptyList())
    val pages = _pages.asStateFlow()

    private val _searchResult = MutableSharedFlow<Result<List<ContentWithPage>>>()
    val searchResult = _searchResult.asSharedFlow()

    private var thumbnailLoader: PdfThumbnailLoader? = null
    private var renderJob: Job? = null
    private var searchJob: Job? = null

    fun setDocument(value: RecentDocument) {
        document = value
    }

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

    fun renderThumbnails(context: Context, file: File, password: String?, thumbnailWidth: Int = 200) {
        if (renderJob != null) return
        val loader = PdfThumbnailLoader(context, file, password, thumbnailWidth)
        thumbnailLoader = loader
        renderJob = viewModelScope.launch {
            _pages.value = List(loader.pageCount()) { DocumentPage(it) }
        }
    }

    suspend fun thumbnail(index: Int): Bitmap? = thumbnailLoader?.thumbnail(index)

    /** Call after the file was saved, so thumbnails are rendered from the new content. */
    suspend fun invalidateThumbnails() {
        thumbnailLoader?.invalidate()
    }

    fun search(query: String, password: String?) {
        val path = pdfPath ?: return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { searchText(File(path), query, password) }
            }
            if (result.exceptionOrNull() is CancellationException) return@launch
            _searchResult.emit(result)
        }
    }

    private fun searchText(file: File, query: String, password: String?): List<ContentWithPage> {
        val result = mutableListOf<ContentWithPage>()

        return result
    }

    override fun onCleared() {
        renderJob?.cancel()
        thumbnailLoader?.close()
        super.onCleared()
    }
}

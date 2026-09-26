package com.alf06.document.reader.ui.home.tools.split

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alf06.document.reader.model.RecentDocument
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfPasswordException
import com.reader.pdfviewer.pdfium.PdfiumCore
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfTools
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class SplitPdfViewModel(context: Context, private val pdfTools: PdfTools) : ViewModel() {
    private companion object {
        const val TAG = "SplitPdfViewModel"
    }

    private val appContext = context.applicationContext
    private val pdfium by lazy { PdfiumCore(appContext) }

    private val pdfDispatcher = Dispatchers.IO.limitedParallelism(1)
    @Volatile
    private var document: PdfDocument? = null

    private val thumbnails = object : LruCache<Int, Bitmap>(
        minOf(Runtime.getRuntime().maxMemory() / 8, 32L * 1024 * 1024).toInt()
    ) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.allocationByteCount
    }

    private var password: String? = null

    private val _source = MutableStateFlow<RecentDocument?>(null)
    val source: StateFlow<RecentDocument?> = _source.asStateFlow()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private val _selectedPages = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPages: StateFlow<Set<Int>> = _selectedPages.asStateFlow()

    private val _openState = MutableStateFlow<OpenState>(OpenState.Idle)
    val openState: StateFlow<OpenState> = _openState.asStateFlow()

    private val _splitState = MutableStateFlow<SplitState>(SplitState.Idle)
    val splitState: StateFlow<SplitState> = _splitState.asStateFlow()

    fun open(file: RecentDocument, password: String? = null) {
        if (_openState.value == OpenState.Opening) return

        if (file.path == _source.value?.path && document != null) {
            _openState.value = OpenState.Opened
            return
        }
        _openState.value = OpenState.Opening
        viewModelScope.launch {
            val result = withContext(pdfDispatcher) {
                closeDocument()
                runCatching {
                    val fd = ParcelFileDescriptor.open(File(file.path), ParcelFileDescriptor.MODE_READ_ONLY)
                    val doc = try {
                        pdfium.newDocument(fd, password)
                    } catch (e: Throwable) {
                        fd.close()
                        throw e
                    }
                    document = doc
                    pdfium.getPageCount(doc)
                }
            }
            result.onSuccess { count ->
                this@SplitPdfViewModel.password = password
                _source.value = file
                _pageCount.value = count
                _selectedPages.value = emptySet()
                _openState.value = if (count > 0) OpenState.Opened else OpenState.Failed
            }.onFailure { error ->
                Log.e(TAG, "Open ${file.path} failed", error)
                _openState.value = if (error.isPasswordError()) {
                    OpenState.PasswordRequired(file, incorrect = password != null)
                } else {
                    OpenState.Failed
                }
            }
        }
    }

    fun consumeOpenResult() {
        if (_openState.value != OpenState.Opening) _openState.value = OpenState.Idle
    }

    suspend fun thumbnail(index: Int, width: Int): Bitmap? = withContext(pdfDispatcher) {
        thumbnails.get(index)?.let { return@withContext it }
        val doc = document ?: return@withContext null
        try {
            val size = pdfium.getPageSize(doc, index) ?: return@withContext null
            val height = (width * size.height / size.width.toFloat()).roundToInt().coerceAtLeast(1)
            val bitmap = createBitmap(width.coerceAtLeast(1), height)
            bitmap.eraseColor(Color.WHITE)
            if (!pdfium.renderPageBitmapOnce(doc, bitmap, index)) return@withContext null
            thumbnails.put(index, bitmap)
            bitmap
        } catch (e: Exception) {
            Log.w(TAG, "Thumbnail $index failed", e)
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    fun togglePage(index: Int) {
        _selectedPages.update { if (index in it) it - index else it + index }
    }

    fun split(output: File) {
        val file = _source.value ?: return
        val pages = _selectedPages.value.sorted().toIntArray()
        if (pages.isEmpty() || _splitState.value == SplitState.Running) return
        _splitState.value = SplitState.Running
        viewModelScope.launch {
            val result = runCatching { pdfTools.split(PdfSource(File(file.path), password), pages, output) }
            if (result.exceptionOrNull() is CancellationException) return@launch
            result.onFailure { Log.e(TAG, "Split failed", it) }
            if (result.isSuccess) {
                MediaScannerConnection.scanFile(appContext, arrayOf(output.path), arrayOf("application/pdf"), null)
            }
            _splitState.value = if (result.isSuccess) SplitState.Done(output.path) else SplitState.Failed
        }
    }

    fun consumeSplitResult() {
        _splitState.value = SplitState.Idle
    }

    private fun closeDocument() {
        document?.let(pdfium::closeDocument)
        document = null
        thumbnails.evictAll()
    }

    private fun Throwable.isPasswordError(): Boolean =
        generateSequence(this) { it.cause }.take(8).any { it is PdfPasswordException }

    override fun onCleared() {
        CoroutineScope(pdfDispatcher + NonCancellable).launch { closeDocument() }
        super.onCleared()
    }
}

sealed interface OpenState {
    data object Idle : OpenState
    data object Opening : OpenState
    data object Opened : OpenState
    data class PasswordRequired(val file: RecentDocument, val incorrect: Boolean) : OpenState
    data object Failed : OpenState
}

sealed interface SplitState {
    data object Idle : SplitState
    data object Running : SplitState
    data class Done(val path: String) : SplitState
    data object Failed : SplitState
}

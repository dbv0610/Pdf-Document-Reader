package com.alf06.document.reader.ui.home.tools.translate

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
import com.alf06.document.reader.ui.home.tools.split.OpenState
import com.google.mlkit.nl.translate.TranslateLanguage
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfPasswordException
import com.reader.pdfviewer.pdfium.PdfiumCore
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfTools
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

class TranslatePdfViewModel(context: Context, pdfTools: PdfTools) : ViewModel() {
    private companion object {
        const val TAG = "TranslatePdfViewModel"
        const val CACHE_DIR = "translate"
    }

    private val appContext = context.applicationContext
    private val pdfium by lazy { PdfiumCore(appContext) }
    private val translator = PdfTranslator(appContext, pdfTools)

    private val cacheDir get() = File(appContext.cacheDir, CACHE_DIR)

    private val pdfDispatcher = Dispatchers.IO.limitedParallelism(1)
    @Volatile
    private var document: PdfDocument? = null

    private val thumbnails = object : LruCache<Int, Bitmap>(
        minOf(Runtime.getRuntime().maxMemory() / 8, 32L * 1024 * 1024).toInt()
    ) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.allocationByteCount
    }

    private var password: String? = null

    val languages: List<Pair<String, String>> by lazy {
        val locale = Locale.getDefault()
        TranslateLanguage.getAllLanguages()
            .map { tag -> tag to Locale.forLanguageTag(tag).getDisplayName(locale).replaceFirstChar { it.titlecase(locale) } }
            .sortedBy { it.second }
    }

    private val _source = MutableStateFlow<RecentDocument?>(null)
    val source: StateFlow<RecentDocument?> = _source.asStateFlow()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private val _selectedPages = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPages: StateFlow<Set<Int>> = _selectedPages.asStateFlow()

    private val _targetLanguage = MutableStateFlow<String?>(null)
    val targetLanguage: StateFlow<String?> = _targetLanguage.asStateFlow()

    private val _openState = MutableStateFlow<OpenState>(OpenState.Idle)
    val openState: StateFlow<OpenState> = _openState.asStateFlow()

    private val _translateState = MutableStateFlow<TranslateState>(TranslateState.Idle)
    val translateState: StateFlow<TranslateState> = _translateState.asStateFlow()

    private val _translatedFile = MutableStateFlow<File?>(null)
    val translatedFile: StateFlow<File?> = _translatedFile.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private var translateJob: Job? = null

    fun open(file: RecentDocument, password: String? = null) {
        if (_openState.value == OpenState.Opening) return

        if (file.path == _source.value?.path && document != null) {
            _selectedPages.value = emptySet()
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
                this@TranslatePdfViewModel.password = password
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

    fun setTargetLanguage(tag: String) {
        _targetLanguage.value = tag
    }

    fun languageName(tag: String): String = languages.firstOrNull { it.first == tag }?.second ?: tag

    fun translate() {
        val file = _source.value ?: return
        val language = _targetLanguage.value ?: return
        val pages = _selectedPages.value.sorted()
        if (pages.isEmpty() || _translateState.value is TranslateState.Running) return
        _translateState.value = TranslateState.Running(TranslateStep.Reading(0, pages.size))
        translateJob = viewModelScope.launch {
            val output = withContext(Dispatchers.IO) {
                cacheDir.deleteRecursively()
                cacheDir.mkdirs()
                File(cacheDir, "${File(file.path).nameWithoutExtension}_$language.pdf")
            }
            val result = runCatching {
                translator.translate(PdfSource(File(file.path), password), pages, language, output) { step ->
                    _translateState.value = TranslateState.Running(step)
                }
            }
            if (result.exceptionOrNull() is CancellationException) return@launch
            result.onSuccess {
                _translatedFile.value = output
                _translateState.value = TranslateState.Done
            }.onFailure { error ->
                Log.e(TAG, "Translate failed", error)
                _translateState.value = TranslateState.Failed((error as? TranslateException)?.error ?: TranslateError.Failed)
            }
        }
    }

    fun cancelTranslate() {
        translateJob?.cancel()
        translateJob = null
        if (_translateState.value is TranslateState.Running) _translateState.value = TranslateState.Idle
    }

    fun consumeTranslateResult() {
        if (_translateState.value !is TranslateState.Running) _translateState.value = TranslateState.Idle
    }

    fun save(output: File) {
        val translated = _translatedFile.value ?: return
        if (_saveState.value == SaveState.Running) return
        _saveState.value = SaveState.Running
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val temp = File(output.parentFile, "${output.name}.tmp")
                    try {
                        translated.copyTo(temp, overwrite = true)
                        if (!temp.renameTo(output)) error("Cannot move ${temp.name} to ${output.name}")
                    } finally {
                        temp.delete()
                    }
                }
            }
            if (result.exceptionOrNull() is CancellationException) return@launch
            result.onFailure { Log.e(TAG, "Save failed", it) }
            if (result.isSuccess) {
                MediaScannerConnection.scanFile(appContext, arrayOf(output.path), arrayOf("application/pdf"), null)
            }
            _saveState.value = if (result.isSuccess) SaveState.Done(output.path) else SaveState.Failed
        }
    }

    fun consumeSaveResult() {
        _saveState.value = SaveState.Idle
    }

    private fun closeDocument() {
        document?.let(pdfium::closeDocument)
        document = null
        thumbnails.evictAll()
    }

    private fun Throwable.isPasswordError(): Boolean =
        generateSequence(this) { it.cause }.take(8).any { it is PdfPasswordException }

    override fun onCleared() {
        CoroutineScope(pdfDispatcher + NonCancellable).launch {
            closeDocument()
            cacheDir.deleteRecursively()
        }
        super.onCleared()
    }
}

sealed interface TranslateState {
    data object Idle : TranslateState
    data class Running(val step: TranslateStep) : TranslateState
    data object Done : TranslateState
    data class Failed(val error: TranslateError) : TranslateState
}

sealed interface SaveState {
    data object Idle : SaveState
    data object Running : SaveState
    data class Done(val path: String) : SaveState
    data object Failed : SaveState
}

package com.alf06.document.reader.ui.home.tools.merge

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alf06.document.reader.model.RecentDocument
import com.reader.pdfviewer.pdfium.PdfPasswordException
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfSourceException
import com.reader.pdfviewer.tools.PdfTools
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MergePdfViewModel(context: Context, private val pdfTools: PdfTools) : ViewModel() {
    private companion object {
        const val TAG = "MergePdfViewModel"
    }

    private val appContext = context.applicationContext

    private val passwords = mutableMapOf<String, String>()

    private val _files = MutableStateFlow<List<RecentDocument>>(emptyList())
    val files: StateFlow<List<RecentDocument>> = _files.asStateFlow()

    private val _mergeState = MutableStateFlow<MergeState>(MergeState.Idle)
    val mergeState: StateFlow<MergeState> = _mergeState.asStateFlow()

    fun toggle(document: RecentDocument) {
        _files.update { files ->
            if (files.any { it.path == document.path }) files.filterNot { it.path == document.path }
            else files + document
        }
    }

    fun remove(document: RecentDocument) {
        _files.update { files -> files.filterNot { it.path == document.path } }
    }

    fun reorder(files: List<RecentDocument>) {
        _files.value = files
    }

    fun setPassword(path: String, password: String) {
        passwords[path] = password
    }

    fun merge(output: File) {
        val files = _files.value
        if (files.size < 2 || _mergeState.value == MergeState.Running) return
        _mergeState.value = MergeState.Running
        viewModelScope.launch {
            val sources = files.map { PdfSource(File(it.path), passwords[it.path]) }
            val result = runCatching { pdfTools.merge(sources, output) }
            val error = result.exceptionOrNull()
            if (error is CancellationException) return@launch
            if (error != null) Log.e(TAG, "Merge failed", error)
            _mergeState.value = when {
                error == null -> {
                    MediaScannerConnection.scanFile(appContext, arrayOf(output.path), arrayOf("application/pdf"), null)
                    MergeState.Done(output.path)
                }
                error is PdfSourceException && error.isPasswordError() -> {
                    val path = error.source.file.path
                    MergeState.PasswordRequired(path, output, incorrect = path in passwords)
                }
                else -> MergeState.Failed
            }
        }
    }

    fun consumeMergeResult() {
        if (_mergeState.value != MergeState.Running) _mergeState.value = MergeState.Idle
    }

    private fun Throwable.isPasswordError(): Boolean =
        generateSequence(this) { it.cause }.take(8).any { it is PdfPasswordException }
}

sealed interface MergeState {
    data object Idle : MergeState
    data object Running : MergeState
    data class Done(val path: String) : MergeState

    data class PasswordRequired(val path: String, val output: File, val incorrect: Boolean) : MergeState
    data object Failed : MergeState
}

package com.alf06.document.reader.ui.home.tools.zip

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alf06.document.reader.model.RecentDocument
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CreateZipViewModel(context: Context) : ViewModel() {
    private companion object {
        const val TAG = "CreateZipViewModel"
    }

    private val appContext = context.applicationContext

    private val _files = MutableStateFlow<List<RecentDocument>>(emptyList())
    val files: StateFlow<List<RecentDocument>> = _files.asStateFlow()

    private val _zipState = MutableStateFlow<ZipState>(ZipState.Idle)
    val zipState: StateFlow<ZipState> = _zipState.asStateFlow()

    fun toggle(document: RecentDocument) {
        _files.update { files ->
            if (files.any { it.path == document.path }) files.filterNot { it.path == document.path }
            else files + document
        }
    }

    fun remove(document: RecentDocument) {
        _files.update { files -> files.filterNot { it.path == document.path } }
    }

    fun createZip(output: File) {
        val files = _files.value.map { File(it.path) }
        if (files.isEmpty() || _zipState.value == ZipState.Running) return
        _zipState.value = ZipState.Running
        viewModelScope.launch {
            val result = runCatching { zip(files, output) }
            if (result.exceptionOrNull() is CancellationException) return@launch
            result.onFailure { Log.e(TAG, "Create ZIP failed", it) }
            if (result.isSuccess) {
                MediaScannerConnection.scanFile(appContext, arrayOf(output.path), arrayOf("application/zip"), null)
            }
            _zipState.value = if (result.isSuccess) ZipState.Done(output.path) else ZipState.Failed
        }
    }

    fun consumeZipResult() {
        _zipState.value = ZipState.Idle
    }

    private suspend fun zip(files: List<File>, output: File) = withContext(Dispatchers.IO) {
        val temp = File(output.parentFile, "${output.name}.tmp")
        try {
            ZipOutputStream(temp.outputStream().buffered()).use { zip ->
                val usedNames = HashSet<String>()
                files.forEach { file ->
                    ensureActive()
                    zip.putNextEntry(ZipEntry(uniqueEntryName(file, usedNames)).apply { time = file.lastModified() })
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            if (!temp.renameTo(output)) throw IOException("Cannot move ${temp.name} to ${output.name}")
        } finally {
            temp.delete()
        }
    }

    private fun uniqueEntryName(file: File, usedNames: MutableSet<String>): String {
        val extension = file.extension.takeIf { it.isNotEmpty() }?.let { ".$it" }.orEmpty()
        return generateSequence(1) { it + 1 }
            .map { if (it == 1) file.name else "${file.nameWithoutExtension} ($it)$extension" }
            .first { usedNames.add(it.lowercase()) }
    }
}

sealed interface ZipState {
    data object Idle : ZipState
    data object Running : ZipState
    data class Done(val path: String) : ZipState
    data object Failed : ZipState
}

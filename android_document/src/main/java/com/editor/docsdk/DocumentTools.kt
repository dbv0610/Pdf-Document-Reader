/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfTools
import com.wxiwei.office.editor.DocumentCreator
import com.wxiwei.office.editor.EditResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Document processing without any screen: PDF to Word, compress, merge, split, passwords, text
 * recognition (OCR), pictures to PDF, page pictures and new blank Office documents.
 *
 * Kotlin: every operation is a `suspend` function; call it from a coroutine (it runs on a
 * background thread by itself). Java: use the `...Async` methods, which call back on the main
 * thread and return a [Task] that can be cancelled.
 *
 * Operations write a new file at `output` (its folder is created when missing); the input is
 * never changed. A failed or cancelled operation leaves no output file. Every failure is a
 * [DocumentException].
 */
class DocumentTools(context: Context) {

    /** How much [compressPdf] shrinks pictures: LOW keeps the best quality, HIGH makes the smallest file. */
    enum class Compression { LOW, MEDIUM, HIGH }

    /** Progress of a long operation: [done] of [total] pages or files. Called on a background thread. */
    fun interface Progress {
        fun onProgress(done: Int, total: Int)
    }

    /** Result of an `...Async` operation, on the main thread. */
    interface Callback<T> {
        fun onSuccess(result: T)
        fun onError(error: DocumentException)
    }

    /** A running `...Async` operation. */
    class Task internal constructor(private val job: Job) {
        /** Stops the operation; its callback is not called and no output file is left. */
        fun cancel() = job.cancel()
        val isDone: Boolean get() = job.isCompleted
    }

    private val appContext = context.applicationContext
    private val pdf = PdfTools(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ---- Kotlin API ----

    /**
     * [input] PDF as a Word document (.docx) at [output]: text with its size, weight, slant,
     * color, font, alignment and indents, the pictures, each PDF page on a new page; scanned
     * pages are recognized with OCR. Returns the number of pages converted.
     */
    suspend fun pdfToWord(input: File, output: File, password: String? = null, progress: Progress? = null): Int =
        run(password) { pdf.toWord(PdfSource(input, password), prepare(output), progress.fn()) }

    /** [input] PDF with its pictures stored smaller. Returns how many pictures were made smaller. */
    suspend fun compressPdf(input: File, output: File, level: Compression = Compression.MEDIUM,
                            password: String? = null, progress: Progress? = null): Int =
        run(password) {
            val engineLevel = when (level) {
                Compression.LOW -> PdfTools.Compression.LOW
                Compression.MEDIUM -> PdfTools.Compression.MEDIUM
                Compression.HIGH -> PdfTools.Compression.HIGH
            }
            pdf.compress(PdfSource(input, password), prepare(output), engineLevel, progress.fn())
        }

    /** All pages of [inputs], in order, in one PDF at [output]. */
    suspend fun mergePdfs(inputs: List<File>, output: File, progress: Progress? = null) {
        run(null) {
            require(inputs.isNotEmpty()) { "No files to merge" }
            pdf.merge(inputs.map { PdfSource(it) }, prepare(output), progress.fn())
        }
    }

    /** Pages [pages] of [input] (0 based, in the order given) as a new PDF at [output]. */
    suspend fun splitPdf(input: File, pages: IntArray, output: File, password: String? = null) {
        run(password) {
            require(pages.isNotEmpty()) { "No pages" }
            pdf.split(PdfSource(input, password), pages, prepare(output))
        }
    }

    /**
     * A copy of [input] that asks for [newPassword] to open (AES-256). [allowPrint] and
     * [allowCopy] say whether readers may print or copy its text. [password] opens [input] when
     * it is already protected.
     */
    suspend fun protectPdf(input: File, output: File, newPassword: String, allowPrint: Boolean = true,
                           allowCopy: Boolean = true, password: String? = null) {
        run(password) {
            require(newPassword.isNotEmpty()) { "Empty password" }
            pdf.setPassword(PdfSource(input, password), prepare(output), newPassword, allowPrint = allowPrint, allowCopy = allowCopy)
        }
    }

    /** A copy of the protected [input] without its password. */
    suspend fun unprotectPdf(input: File, password: String, output: File) {
        run(password) { pdf.removePassword(PdfSource(input, password), prepare(output)) }
    }

    /**
     * A copy of [input] whose scanned pages get an invisible text layer (OCR, on the device), so
     * their text can be searched, selected and copied. Returns the number of pages recognized.
     */
    suspend fun recognizeText(input: File, output: File, password: String? = null, progress: Progress? = null): Int =
        run(password) { pdf.addTextLayer(PdfSource(input, password), prepare(output), progress.fn()) }

    /** [images] (JPEG, PNG, WebP... files), one per page in order, as a PDF at [output]. Returns the page count. */
    suspend fun imagesToPdf(images: List<File>, output: File, progress: Progress? = null): Int =
        run(null) {
            require(images.isNotEmpty()) { "No pictures" }
            var pages = 0
            pdf.createFromImages(prepare(output)) {
                images.forEachIndexed { i, file ->
                    val bitmap = decode(file) ?: throw DocumentException(DocumentException.Reason.DAMAGED, "Cannot read picture ${file.name}")
                    try { addImage(bitmap) } finally { bitmap.recycle() }
                    pages++
                    progress?.onProgress(i + 1, images.size)
                }
            }
            pages
        }

    /** Number of pages of [input]. */
    suspend fun pageCount(input: File, password: String? = null): Int =
        run(password) { pdf.pageCount(PdfSource(input, password)) }

    /** Page [page] (0 based) of [input] drawn [widthPx] pixels wide, for thumbnails or sharing. */
    suspend fun renderPage(input: File, page: Int, widthPx: Int, password: String? = null): Bitmap =
        run(password) {
            var result: Bitmap? = null
            pdf.renderPages(PdfSource(input, password), listOf(page), widthPx) { _, bitmap ->
                result = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            }
            result ?: throw DocumentException(DocumentException.Reason.UNKNOWN, "Page $page cannot be drawn")
        }

    /** A new blank document of [type] (WORD, EXCEL or POWERPOINT) at [output], whose extension must match (.docx, .xlsx, .pptx). */
    suspend fun createDocument(type: DocumentType, output: File): File = run(null) {
        val format = when (type) {
            DocumentType.WORD -> DocumentCreator.Format.WORD
            DocumentType.EXCEL -> DocumentCreator.Format.EXCEL
            DocumentType.POWERPOINT -> DocumentCreator.Format.POWERPOINT
            else -> throw DocumentException(DocumentException.Reason.UNSUPPORTED, "Only Word, Excel and PowerPoint documents can be created")
        }
        withContext(Dispatchers.IO) {
            when (val result = DocumentCreator.create(appContext, format, prepare(output))) {
                is EditResult.Ok -> result.file
                is EditResult.Error -> throw DocumentException(DocumentException.Reason.STORAGE, result.message, result.cause)
            }
        }
    }

    // ---- Java API: same operations, results on the main thread ----

    fun pdfToWordAsync(input: File, output: File, password: String?, progress: Progress?, callback: Callback<Int>): Task =
        async(callback) { pdfToWord(input, output, password, progress) }

    fun compressPdfAsync(input: File, output: File, level: Compression, password: String?, callback: Callback<Int>): Task =
        async(callback) { compressPdf(input, output, level, password) }

    fun mergePdfsAsync(inputs: List<File>, output: File, callback: Callback<File>): Task =
        async(callback) { mergePdfs(inputs, output); output }

    fun splitPdfAsync(input: File, pages: IntArray, output: File, password: String?, callback: Callback<File>): Task =
        async(callback) { splitPdf(input, pages, output, password); output }

    fun protectPdfAsync(input: File, output: File, newPassword: String, allowPrint: Boolean, allowCopy: Boolean,
                        password: String?, callback: Callback<File>): Task =
        async(callback) { protectPdf(input, output, newPassword, allowPrint, allowCopy, password); output }

    fun unprotectPdfAsync(input: File, password: String, output: File, callback: Callback<File>): Task =
        async(callback) { unprotectPdf(input, password, output); output }

    fun recognizeTextAsync(input: File, output: File, password: String?, progress: Progress?, callback: Callback<Int>): Task =
        async(callback) { recognizeText(input, output, password, progress) }

    fun imagesToPdfAsync(images: List<File>, output: File, callback: Callback<Int>): Task =
        async(callback) { imagesToPdf(images, output) }

    fun pageCountAsync(input: File, password: String?, callback: Callback<Int>): Task =
        async(callback) { pageCount(input, password) }

    fun renderPageAsync(input: File, page: Int, widthPx: Int, password: String?, callback: Callback<Bitmap>): Task =
        async(callback) { renderPage(input, page, widthPx, password) }

    fun createDocumentAsync(type: DocumentType, output: File, callback: Callback<File>): Task =
        async(callback) { createDocument(type, output) }

    // ---- internals ----

    private suspend fun <T> run(password: String?, block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        throw DocumentException.from(if (e is IllegalArgumentException) DocumentException(DocumentException.Reason.UNKNOWN, e.message, e) else e,
            hadPassword = password != null)
    }

    private fun <T> async(callback: Callback<T>, block: suspend () -> T): Task = Task(scope.launch {
        val result = try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: DocumentException) {
            callback.onError(e)
            return@launch
        }
        callback.onSuccess(result)
    })

    private fun prepare(output: File): File {
        output.absoluteFile.parentFile?.mkdirs()
        return output
    }

    private fun Progress?.fn(): (Int, Int) -> Unit = { done, total -> this?.onProgress(done, total) }

    /** A picture file, made at most [MAX_PICTURE_SIDE] pixels on its long side so pages stay printable but small. */
    private fun decode(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_PICTURE_SIDE) sample *= 2
        return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private companion object {
        const val MAX_PICTURE_SIDE = 2480
    }
}

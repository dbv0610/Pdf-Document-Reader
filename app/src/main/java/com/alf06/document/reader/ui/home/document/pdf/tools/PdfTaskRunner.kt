package com.alf06.document.reader.ui.home.document.pdf.tools

import android.media.MediaScannerConnection
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alf06.document.reader.R
import com.alf06.document.reader.base.shareFile
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.document.office.edit.DialogKit
import com.alf06.document.reader.ui.home.document.openDocument
import com.alf06.document.reader.utils.AppUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

/**
 * Runs a PDF tool that writes a new file: asks its name (in the app's documents folder), shows
 * the progress with a cancel button, then offers to open or share the result. A failed or
 * cancelled run leaves no file behind.
 */
internal class PdfTaskRunner(private val activity: AppCompatActivity) {

    /** Asks the name of the new file, [suffix] added to [sourceName] by default, then runs [task] into it. */
    fun run(title: String, sourceName: String, suffix: String, task: suspend (output: File, progress: (Int, Int) -> Unit) -> String?) {
        activity.lifecycleScope.launch {
            val dir = File(AppUtils.ensureDocumentDirectory())
            val base = "${sourceName}_$suffix"
            val name = generateSequence(1) { it + 1 }.map { if (it == 1) base else "${base}_$it" }.first { !File(dir, "$it.pdf").exists() }
            RenameFileDialog(activity, name, titleRes = R.string.pdf_tool_save_as, failedRes = R.string.file_name_exists, skipIfUnchanged = false) { chosen ->
                val output = File(dir, "$chosen.pdf")
                if (output.exists()) return@RenameFileDialog false
                start(title, output, task = task)
                true
            }.show()
        }
    }

    /**
     * Runs [task] into [output] (already chosen) with a progress dialog; then [onSuccess], or by
     * default the offer to open the new file.
     */
    fun start(title: String, output: File, onSuccess: ((message: String?) -> Unit)? = null,
              task: suspend (output: File, progress: (Int, Int) -> Unit) -> String?) {
        lateinit var job: Job
        var status: TextView? = null
        val dialog = DialogKit(activity).show(title, cancelable = false) {
            status = text(activity.getString(R.string.pdf_tool_working))
            keepOpenOnButtons()
            negative(activity.getString(android.R.string.cancel)) { job.cancel(); dialog?.dismiss() }
        }
        job = activity.lifecycleScope.launch {
            val result = try {
                Result.success(task(output) { done, total ->
                    activity.runOnUiThread { status?.text = activity.getString(R.string.pdf_tool_progress, done, total) }
                })
            } catch (e: CancellationException) {
                output.delete()
                dialog.dismiss()
                throw e
            } catch (e: Throwable) {
                output.delete()
                Result.failure(e)
            }
            dialog.dismiss()
            result.onSuccess { message ->
                if (onSuccess != null) return@onSuccess onSuccess(message)
                MediaScannerConnection.scanFile(activity.applicationContext, arrayOf(output.path), arrayOf("application/pdf"), null)
                done(output, message)
            }.onFailure {
                Toast.makeText(activity, activity.getString(R.string.pdf_tool_failed, it.message ?: it.javaClass.simpleName), Toast.LENGTH_LONG).show()
            }
        }
    }

    /** The file is written: open it, share it, or stay. */
    fun done(output: File, message: String?) {
        if (activity.isFinishing || activity.isDestroyed) return
        DialogKit(activity).show(activity.getString(R.string.pdf_tool_done)) {
            text((message?.let { "$it\n\n" } ?: "") + output.path)
            positive(activity.getString(R.string.pdf_tool_open)) {
                activity.openDocument(RecentDocument(path = output.path, lastModified = output.lastModified(), size = output.length(), type = DocumentType.Pdf))
            }
            neutral(activity.getString(R.string.file_action_share)) {
                if (!activity.shareFile(output.path)) Toast.makeText(activity, R.string.file_share_failed, Toast.LENGTH_SHORT).show()
            }
            negative(activity.getString(R.string.pdf_tool_close))
        }
    }

    companion object {
        /** A size in bytes for people: 1.2 MB, 350 KB. */
        fun size(bytes: Long): String = when {
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1024f / 1024f)
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes B"
        }
    }
}

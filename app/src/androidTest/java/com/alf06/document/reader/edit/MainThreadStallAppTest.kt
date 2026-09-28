package com.alf06.document.reader.edit

import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.ui.home.document.office.ReadDocumentActivity
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Manual check, not part of the normal run: opens `-e file <path>` in ReadDocumentActivity like a
 * user, swipes through it, and records every time the main thread stays busy over 400 ms, with
 * where it was, in files/stalls-app.txt (and logcat tag MainThreadStall).
 */
@RunWith(AndroidJUnit4::class)
class MainThreadStallAppTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun openSwipeAndWatchMainThread() {
        val path = InstrumentationRegistry.getArguments().getString("file")
        assumeTrue("no file given", path != null)
        val copy = File(context.filesDir, File(path!!).name).also { File(path).copyTo(it, overwrite = true) }
        val type = when (copy.extension.lowercase()) {
            "xls", "xlsx" -> DocumentType.Excel
            "ppt", "pptx" -> DocumentType.Ppt
            else -> DocumentType.Doc
        }
        val main = Handler(Looper.getMainLooper())
        val lastBeat = AtomicLong(SystemClock.uptimeMillis())
        val watching = AtomicBoolean(true)
        val report = StringBuilder()
        val started = SystemClock.uptimeMillis()
        val stallMs = InstrumentationRegistry.getArguments().getString("stallMs")?.toLong() ?: 400L
        val gapMs = if (stallMs < 400) 150L else 1000L
        val watchdog = Thread {
            var reported = 0L
            while (watching.get()) {
                main.post { lastBeat.set(SystemClock.uptimeMillis()) }
                Thread.sleep(if (stallMs < 400) 40 else 100)
                val stalled = SystemClock.uptimeMillis() - lastBeat.get()
                if (stalled > stallMs && SystemClock.uptimeMillis() - reported > gapMs) {
                    reported = SystemClock.uptimeMillis()
                    val stack = Looper.getMainLooper().thread.stackTrace.take(30).joinToString("\n    ")
                    val entry = "t=${reported - started} ms, main busy $stalled ms:\n    $stack\n"
                    Log.w("MainThreadStall", entry)
                    synchronized(report) { report.append(entry).append('\n') }
                }
            }
        }.apply { start() }
        try {
            val intent = Intent(context, ReadDocumentActivity::class.java)
                .putExtra(ReadDocumentActivity.ARG_DOCUMENT, RecentDocument(path = copy.absolutePath, size = copy.length(), type = type))
            ActivityScenario.launch<ReadDocumentActivity>(intent).use {
                val args = InstrumentationRegistry.getArguments()
                Thread.sleep((args.getString("seconds")?.toLong() ?: 8L) * 1000)
                val ui = instrumentation.uiAutomation
                val swipes = args.getString("swipes")?.toInt() ?: 6
                if (args.getString("swipe") != "false") repeat(swipes) {
                    ui.executeShellCommand("input swipe 500 1600 500 400 150").close()
                    Thread.sleep(700)
                }
                if (args.getString("swipe") != "false") repeat(3) {
                    ui.executeShellCommand("input swipe 900 1000 200 1000 150").close()
                    Thread.sleep(700)
                }
                Thread.sleep(5000)
                ui.takeScreenshot()?.let { b ->
                    File(context.getExternalFilesDir(null), "stalls-app.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
                }
            }
        } finally {
            watching.set(false)
            watchdog.join()
            File(context.getExternalFilesDir(null), "stalls-app.txt")
                .writeText("ran ${SystemClock.uptimeMillis() - started} ms\n" + synchronized(report) { report.toString() })
        }
    }
}

/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * Manual check, not part of the normal run: opens the file given as `-e file <path>` and, for
 * `-e seconds <n>` (30 by default), records every time the main thread stays busy for more than
 * 400 ms, with where it was. The report goes to logcat (tag MainThreadStall) and to
 * files/stalls.txt of the test app.
 */
@RunWith(AndroidJUnit4::class)
class MainThreadStallTest {

    @Test
    fun openAndWatchMainThread() {
        val args = InstrumentationRegistry.getArguments()
        val path = args.getString("file")
        assumeTrue("no file given", path != null)
        val seconds = args.getString("seconds")?.toInt() ?: 30
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val copy = File(context.cacheDir, File(path!!).name).also { File(path).copyTo(it, overwrite = true) }

        val main = Handler(Looper.getMainLooper())
        val lastBeat = AtomicLong(SystemClock.uptimeMillis())
        val report = StringBuilder()
        val watching = java.util.concurrent.atomic.AtomicBoolean(true)
        val started = SystemClock.uptimeMillis()
        val watchdog = Thread {
            var reported = 0L
            while (watching.get()) {
                main.post { lastBeat.set(SystemClock.uptimeMillis()) }
                Thread.sleep(100)
                val stalled = SystemClock.uptimeMillis() - lastBeat.get()
                if (stalled > 400 && SystemClock.uptimeMillis() - reported > 1000) {
                    reported = SystemClock.uptimeMillis()
                    val stack = Looper.getMainLooper().thread.stackTrace.take(25).joinToString("\n    ")
                    val entry = "t=${reported - started} ms, main busy ${stalled} ms:\n    $stack\n"
                    Log.w("MainThreadStall", entry)
                    synchronized(report) { report.append(entry).append('\n') }
                }
            }
        }.apply { start() }
        try {
            OpenDocument.open(copy, { true }) { _ ->
                delay(seconds * 1000L)
            }
        } finally {
            watching.set(false)
            watchdog.join()
            val out = File(context.getExternalFilesDir(null), "stalls.txt")
            out.writeText("open took until ${SystemClock.uptimeMillis() - started} ms\n" + synchronized(report) { report.toString() })
        }
    }
}

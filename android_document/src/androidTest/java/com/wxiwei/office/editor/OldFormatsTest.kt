/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.reader.OfficeReader
import com.wxiwei.office.reader.ReaderConfig
import com.wxiwei.office.reader.ReaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Word 97-2003 (.doc) and PowerPoint 97-2003 (.ppt) files (Apache POI test data, samples/old):
 * each opens, and laying out its pages or loading its slides afterwards reports no engine error
 * (the "System crash" / "File parsing error" dialog).
 */
@RunWith(AndroidJUnit4::class)
class OldFormatsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    /** Lines this process logged under ErrorUtil (each engine error). */
    private fun engineErrors(): List<String> =
        Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "tag", "ErrorUtil:W", "*:S")).inputStream.bufferedReader().readLines()
            .filter { it.startsWith("W/ErrorUtil") || it.startsWith("E/ErrorUtil") }

    /** Opens [name]; the state it reached, its page count, and the engine errors logged meanwhile. */
    private fun open(name: String): Triple<ReaderState, Int, List<String>> {
        val file = OpenDocument.copySample("old/$name", "old_$name")
        Runtime.getRuntime().exec(arrayOf("logcat", "-c")).waitFor()
        val before = engineErrors().size
        var result: Triple<ReaderState, Int, List<String>>? = null
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            lateinit var reader: OfficeReader
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)
                reader = OfficeReader(activity, container, ReaderConfig(showTxtEncodeDialog = false))
                reader.onOpenFailure = { true }
                reader.open(file.absolutePath)
            }
            runBlocking {
                val state = withTimeout(120_000) { reader.state.first { it.status == ReaderState.Status.Ready || it.status == ReaderState.Status.Failed } }
                // pages laid out and slides loaded in the background
                delay(6000)
                val errors = engineErrors().drop(before)
                val all = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "tag", "ErrorUtil:W", "AndroidRuntime:E", "*:S")).inputStream.bufferedReader().readText()
                if (errors.isNotEmpty() || state.status != ReaderState.Status.Ready) android.util.Log.i("OldFormatsTest", "$name: ${android.util.Log.getStackTraceString(state.error)}\n$all")
                result = Triple(reader.state.value, reader.state.value.pageCount, errors)
            }
        }
        return result!!
    }

    private fun opensClean(name: String) {
        val (state, pages, errors) = open(name)
        android.util.Log.i("OldFormatsTest", "$name: ${state.status} pages=$pages errors=${errors.size}")
        assertEquals("$name: ${state.error}", ReaderState.Status.Ready, state.status)
        assertTrue("$name: engine errors $errors", errors.isEmpty())
        assertTrue("$name: pages $pages", pages > 0)
    }

    @Test fun pptWithManySlides() = opensClean("37625.ppt")
    @Test fun pptImportedFromOpenOffice() = opensClean("23884_defense_FINAL_OOimport_edit.ppt")
    @Test fun docWithFloatingPictures() = opensClean("FloatingPictures.doc")
    @Test fun docGaia() = opensClean("GaiaTest.doc")
}

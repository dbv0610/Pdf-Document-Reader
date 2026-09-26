package com.wxiwei.office.editor

import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.reader.OfficeReader
import com.wxiwei.office.reader.ReaderConfig
import com.wxiwei.office.reader.ReaderState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import java.io.File

/** Test helpers: copy an asset sample to a file and open a file in an [OfficeReader]. */
internal object OpenDocument {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    fun copySample(name: String, as_: String = name): File {
        val file = File(instrumentation.targetContext.cacheDir, as_)
        instrumentation.context.assets.open("samples/$name").use { input -> file.outputStream().use { input.copyTo(it) } }
        return file
    }

    fun output(name: String): File = File(instrumentation.targetContext.cacheDir, name).also { it.delete() }

    /** Opens [file], waits until it is ready ([ready] decides when laid out enough), runs [block]. */
    fun open(file: File, ready: (ReaderState) -> Boolean = { true }, block: suspend (OfficeReader) -> Unit) {
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
                val state = withTimeout(180_000) {
                    reader.state.first { it.status == ReaderState.Status.Ready || it.status == ReaderState.Status.Failed }
                }
                assertEquals(state.error?.toString(), ReaderState.Status.Ready, state.status)
                withTimeout(180_000) { reader.state.first(ready) }
                block(reader)
            }
        }
    }

    /** Runs [block] on the main thread and returns its result. */
    fun <T> onMain(block: () -> T): T {
        var result: Result<T>? = null
        instrumentation.runOnMainSync { result = runCatching(block) }
        return result!!.getOrThrow()
    }
}

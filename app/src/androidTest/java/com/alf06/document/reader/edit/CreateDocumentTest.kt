package com.alf06.document.reader.edit

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.alf06.document.reader.R
import com.alf06.document.reader.ui.home.MainActivity
import com.alf06.document.reader.ui.home.document.office.ReadDocumentActivity
import com.alf06.document.reader.utils.AppUtils
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.reader.ReaderState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Exercises the actual Home → format → filename → editable reader flow for each format. */
@RunWith(AndroidJUnit4::class)
class CreateDocumentTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun waitFor(condition: () -> Boolean) {
        val until = System.currentTimeMillis() + 60_000
        while (System.currentTimeMillis() < until) {
            var ready = false
            instrumentation.runOnMainSync { ready = condition() }
            if (ready) return
            Thread.sleep(150)
        }
        fail("Timed out waiting for new document")
    }

    @Test fun createFromHomeOpensAllThreeEditorsAndRejectsDuplicates() {
        val context = instrumentation.targetContext
        assertTrue("Storage permission must be granted on the test device", AppUtils.hasStoragePermission(context))
        val base = "CreateTest-${System.nanoTime()}"
        val outputs = mutableListOf<File>()
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                for ((label, ext) in listOf("Word (.docx)" to "docx", "Excel (.xlsx)" to "xlsx", "PowerPoint (.pptx)" to "pptx")) {
                    onView(withId(R.id.btnCreateDocument)).perform(click())
                    onView(withText(label)).perform(click())
                    onView(withId(R.id.edtName)).perform(replaceText("$base.$ext"))
                    onView(withId(R.id.btnConfirm)).perform(click())
                    var readerActivity: ReadDocumentActivity? = null
                    waitFor {
                        readerActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                            .filterIsInstance<ReadDocumentActivity>().firstOrNull()
                        readerActivity?.findViewById<OfficeDocumentView>(R.id.officeViewer)?.state?.value?.status == ReaderState.Status.Ready &&
                            readerActivity?.findViewById<View>(R.id.editPanel)?.visibility == View.VISIBLE
                    }
                    val file = File(AppUtils.documentPath, "$base.$ext")
                    outputs += file
                    assertTrue("file exists without a double extension", file.length() > 0)
                    instrumentation.runOnMainSync { readerActivity!!.finish() }
                    waitFor {
                        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).any { it is MainActivity }
                    }
                }
                val original = outputs.first().readBytes()
                onView(withId(R.id.btnCreateDocument)).perform(click())
                onView(withText("Word (.docx)")).perform(click())
                onView(withId(R.id.edtName)).perform(replaceText("$base.docx"))
                onView(withId(R.id.btnConfirm)).perform(click())
                onView(withId(R.id.edtName)).check(androidx.test.espresso.assertion.ViewAssertions.matches(withText("$base.docx")))
                assertArrayEquals("duplicate must not overwrite", original, outputs.first().readBytes())
                onView(withId(R.id.btnCancel)).perform(click())
            }
        } finally {
            outputs.forEach { it.delete(); AppUtils.notifyMediaScanner(context, it.path) }
        }
    }

}

/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.editor.docsdk.DocumentView
import com.editor.docsdk.EditAction
import com.wxiwei.office.editor.OpenDocument.onMain
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipFile

/**
 * Slides added, moved and deleted through [DocumentView]: the page it reports is the slide the
 * edit shows, commands sent while it reopens are refused, and the last slide is never deleted.
 */
@RunWith(AndroidJUnit4::class)
class DocumentViewSlidesTest {
    private fun edit(file: File, block: suspend EditInView.Host.() -> Unit) = EditInView.edit(file, block)
    private fun office(view: DocumentView) = EditInView.office(view)

    /** Waits until the view reports [page] of [count], then checks it stays there. */
    private suspend fun EditInView.Host.settle(page: Int, count: Int) {
        withTimeoutOrNull(15_000) {
            while (onMain { view.currentPage != page || view.pageCount != count }) delay(100)
        }
        delay(500)
        val seen = "events " + onMain { events.toList() } + " state " + onMain { office(view).state.value.let { "p" + it.pageNumber + "/" + it.pageCount } }
        assertEquals(seen, page, onMain { view.currentPage })
        assertEquals(seen, count, onMain { view.pageCount })
    }

    /** Slide part names in the order of the presentation. */
    private fun slideOrder(file: File): List<String> = ZipFile(file).use { zip ->
        fun text(name: String) = zip.getInputStream(zip.getEntry(name)).bufferedReader().readText()
        val rels = Regex("""<Relationship [^>]*?Id="(rId\d+)"[^>]*?Target="([^"]+)"""").findAll(text("ppt/_rels/presentation.xml.rels"))
            .associate { it.groupValues[1] to it.groupValues[2] }
        Regex("""<p:sldId [^>]*?r:id="(rId\d+)"""").findAll(text("ppt/presentation.xml")).map { rels.getValue(it.groupValues[1]) }.toList()
    }

    @Test
    fun arrangeSlides() {
        val file = OpenDocument.copySample("sample.pptx", "arrange.pptx")
        val before = slideOrder(file)
        edit(file) {
            settle(0, 10)
            onMain { view.goToPage(1) }
            settle(1, 10)

            // the second tap comes while the first move reopens the document
            assertTrue(onMain { editor.run(EditAction.SLIDE_DOWN) })
            assertFalse(onMain { editor.run(EditAction.SLIDE_DOWN) })
            settle(2, 10)
            assertTrue(onMain { editor.run(EditAction.SLIDE_DOWN) })
            settle(3, 10)

            // the move is reported even though the page count stayed the same
            assertEquals(3 to 10, events.last())

            assertTrue(onMain { editor.run(EditAction.ADD_SLIDE) })
            settle(4, 11)
            assertTrue(onMain { editor.run(EditAction.DELETE_SLIDE) })
            settle(3, 10)

            assertTrue(onMain { editor.save() })
        }
        val after = slideOrder(file)
        assertEquals(before[1], after[3])
        assertEquals(before.toMutableList().apply { add(3, removeAt(1)) }, after)
    }

    @Test
    fun undoSlideChanges() {
        val file = OpenDocument.copySample("sample.pptx", "undo-slides.pptx")
        val before = slideOrder(file)
        edit(file) {
            settle(0, 10)
            onMain { view.goToPage(1) }
            settle(1, 10)
            assertFalse(onMain { editor.isEnabled(EditAction.UNDO) })

            // move, delete, add: each reopens the document
            assertTrue(onMain { editor.run(EditAction.SLIDE_DOWN) })
            settle(2, 10)
            assertTrue(onMain { editor.run(EditAction.DELETE_SLIDE) })
            settle(1, 9)
            assertTrue(onMain { editor.run(EditAction.ADD_SLIDE) })
            settle(2, 10)

            // undone one by one, each back on the slide it was done on
            assertTrue(onMain { editor.isEnabled(EditAction.UNDO) })
            assertTrue(onMain { editor.run(EditAction.UNDO) })
            settle(1, 9)
            assertTrue(onMain { editor.run(EditAction.UNDO) })
            settle(2, 10)
            assertTrue(onMain { editor.run(EditAction.UNDO) })
            settle(1, 10)
            assertFalse("nothing left to undo", onMain { editor.isEnabled(EditAction.UNDO) })

            // redone in order
            assertTrue(onMain { editor.isEnabled(EditAction.REDO) })
            assertTrue(onMain { editor.run(EditAction.REDO) })
            settle(2, 10)
            assertTrue(onMain { editor.run(EditAction.REDO) })
            settle(1, 9)
            assertTrue(onMain { editor.run(EditAction.REDO) })
            settle(2, 10)
            assertFalse("nothing left to redo", onMain { editor.isEnabled(EditAction.REDO) })

            // a new change after an undo drops what was undone
            assertTrue(onMain { editor.run(EditAction.UNDO) })
            settle(1, 9)
            assertTrue(onMain { editor.run(EditAction.SLIDE_UP) })
            settle(0, 9)
            assertFalse(onMain { editor.isEnabled(EditAction.REDO) })

            // back to the start: the saved file has the slides of the original, in order
            while (onMain { editor.isEnabled(EditAction.UNDO) }) {
                assertTrue(onMain { editor.run(EditAction.UNDO) })
                withTimeoutOrNull(15_000) { while (onMain { !editor.isEnabled(EditAction.UNDO) && !editor.isEnabled(EditAction.REDO) }) delay(100) }
                delay(300)
            }
            settle(1, 10)
            assertTrue(onMain { editor.save() })
        }
        assertEquals(before, slideOrder(file))
    }

    @Test
    fun lastSlideStays() {
        val file = OpenDocument.output("one.pptx")
        assertTrue(DocumentCreator.create(InstrumentationRegistry.getInstrumentation().targetContext, DocumentCreator.Format.POWERPOINT, file) is EditResult.Ok)
        assertEquals(1, slideOrder(file).size)
        edit(file) {
            settle(0, 1)
            onMain { editor.run(EditAction.DELETE_SLIDE) }
            delay(1000)
            settle(0, 1)
            assertFalse(onMain { editor.hasUnsavedChanges() })
        }
        assertEquals(1, slideOrder(file).size)
    }

    @Test
    fun doubleTapEditsText() {
        val file = OpenDocument.copySample("sample.pptx", "doubletap.pptx")
        edit(file) {
            settle(0, 10)
            delay(500)
            // a text shape on top at its own centre
            val shapes = com.wxiwei.office.editor.pptx.PptxEditor(file).listShapes(0)
            val shape = shapes.first { s ->
                val r = s.rectEmu
                s.kind == com.wxiwei.office.editor.pptx.ShapeKind.TEXT &&
                    com.wxiwei.office.editor.slide.SlideGeometry.hitTest(shapes, com.wxiwei.office.editor.pptx.Point(r.x + r.width / 2, r.y + r.height / 2)) == s
            }
            val (x, y) = onMain {
                val p = office(view).control!!.getView() as com.wxiwei.office.pg.control.Presentation
                val r = com.wxiwei.office.editor.slide.SlideGeometry.emuToView(p, shape.rectEmu)!!
                val origin = IntArray(2).also { p.getLocationOnScreen(it) }
                (origin[0] + r.centerX()) to (origin[1] + r.centerY())
            }
            // nothing selected yet: two quick taps
            val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
            fun touch(action: Int, down: Long, at: Long) = android.view.MotionEvent.obtain(down, at, action, x, y, 0).also {
                it.source = android.view.InputDevice.SOURCE_TOUCHSCREEN
                automation.injectInputEvent(it, true)
                it.recycle()
            }
            val t0 = android.os.SystemClock.uptimeMillis()
            touch(android.view.MotionEvent.ACTION_DOWN, t0, t0)
            touch(android.view.MotionEvent.ACTION_UP, t0, t0 + 40)
            touch(android.view.MotionEvent.ACTION_DOWN, t0 + 80, t0 + 80)
            touch(android.view.MotionEvent.ACTION_UP, t0 + 80, t0 + 120)
            delay(1000)
            val editText = onMain {
                fun find(v: android.view.View): android.widget.EditText? = when (v) {
                    is android.widget.EditText -> v
                    is android.view.ViewGroup -> (0 until v.childCount).firstNotNullOfOrNull { find(v.getChildAt(it)) }
                    else -> null
                }
                find(view)
            }
            assertTrue("a double tap on a text shape opens the text editor", editText != null)
            assertEquals(shape.text, onMain { editText!!.text.toString() })
        }
    }
}

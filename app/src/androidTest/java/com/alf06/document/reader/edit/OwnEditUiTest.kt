package com.alf06.document.reader.edit

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.editor.docsdk.CellAlignmentFormat
import com.editor.docsdk.DocumentEditor
import com.editor.docsdk.DocumentView
import com.editor.docsdk.DocumentViewerActivity
import com.editor.docsdk.EditAction
import com.editor.docsdk.EditDialogs
import com.editor.docsdk.EditFeature
import com.editor.docsdk.EditRequest
import com.editor.docsdk.ParagraphFormat
import com.editor.docsdk.TransitionFormat
import com.wxiwei.office.editor.ui.ExcelEditPanel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Editing without the SDK's bar (`startEditing(showToolbar = false)`): the app runs the commands
 * and answers every question, the big dialogs too, with its own UI.
 */
@RunWith(AndroidJUnit4::class)
class OwnEditUiTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    private fun <T : View> find(root: View, match: (View) -> Boolean): T? {
        if (match(root)) @Suppress("UNCHECKED_CAST") return root as T
        if (root is ViewGroup) for (i in 0 until root.childCount) find<T>(root.getChildAt(i), match)?.let { return it }
        return null
    }

    /** Opens the sample [name] in the SDK's viewer, starts editing without its bar, runs [body], then saves. */
    private fun edit(name: String, body: (ActivityScenario<DocumentViewerActivity>, DocumentEditor, MutableList<EditRequest>) -> Unit): File {
        val file = File(context.filesDir, "own-ui-$name")
        instrumentation.context.assets.open("samples/$name").use { i -> file.outputStream().use { i.copyTo(it) } }
        val intent = Intent(context, DocumentViewerActivity::class.java).setData(Uri.fromFile(file))
        ActivityScenario.launch<DocumentViewerActivity>(intent).use { scenario ->
            lateinit var view: DocumentView
            scenario.onActivity { view = find(it.window.decorView) { v -> v is DocumentView }!! }
            val end = System.currentTimeMillis() + 60_000
            var ready = false
            while (!ready && System.currentTimeMillis() < end) {
                Thread.sleep(300)
                scenario.onActivity { ready = view.canEdit }
            }
            Thread.sleep(1500)
            lateinit var editor: DocumentEditor
            val asked = ArrayList<EditRequest>()
            scenario.onActivity {
                editor = view.startEditing(EditFeature.all(), showToolbar = false)!!
                // every question comes to the app; none shows the SDK's dialog
                editor.dialogs = EditDialogs { request -> asked.add(request); true }
                assertNull("no SDK bar", find<View>(view) { v -> v.tag == EditAction.SAVE.name })
            }
            body(scenario, editor, asked)
            scenario.onActivity { assertTrue("saved", editor.save()) }
            Thread.sleep(1500)
        }
        return file
    }

    private fun part(file: File, entry: String) =
        java.util.zip.ZipFile(file).use { z -> z.getInputStream(z.getEntry(entry)).readBytes().toString(Charsets.UTF_8) }

    @Test
    fun excelCellAndAlignment() {
        val file = edit("sample.xlsx") { scenario, editor, asked ->
            val panel = editor.panel as ExcelEditPanel
            scenario.onActivity { assertTrue(editor.run(EditAction.GO_TO_CELL, "B3")) }
            Thread.sleep(800)
            scenario.onActivity {
                assertEquals("B3", panel.selectedRange)
                assertTrue(editor.run(EditAction.CELL_VALUE, "=1+2"))
                assertEquals("=1+2", panel.cellInput)
                assertTrue(editor.run(EditAction.CELL_ALIGNMENT))
                val request = asked.last() as EditRequest.CellAlignment
                request.answer(request.current.copy(horizontal = "center", wrap = true))
                assertTrue(editor.run(EditAction.CELL_ALIGNMENT, CellAlignmentFormat("right", "top", 1, false)))
                assertTrue(editor.hasUnsavedChanges())
            }
        }
        val sheet = part(file, "xl/worksheets/sheet1.xml")
        assertTrue("formula saved", sheet.contains("<f>1+2</f>"))
    }

    @Test
    fun wordFindAndParagraph() {
        val file = edit("sample.docx") { scenario, editor, asked ->
            scenario.onActivity {
                assertTrue(editor.run(EditAction.FIND_REPLACE))
                val find = asked.last() as EditRequest.FindReplace
                val found = find.findNext("MIDI")
                assertNotNull("found", found)
                assertTrue(found!!.count > 0)
                // the match is selected: the Paragraph request starts from its paragraph
                assertTrue(editor.run(EditAction.PARAGRAPH))
                val paragraph = asked.last() as EditRequest.Paragraph
                paragraph.answer(paragraph.current.copy(alignment = "center", spaceAfterPt = 12f))
                assertTrue(editor.run(EditAction.PARAGRAPH, ParagraphFormat("right", 0f, 0f, 0f, 0f, 6f)))
                assertTrue(find.replaceAll("MIDI", "Midi") > 0)
            }
            Thread.sleep(800)
        }
        val body = part(file, "word/document.xml")
        assertTrue("replaced", body.contains("Midi"))
        assertTrue("aligned", body.contains("<w:jc w:val=\"right\"/>"))
    }

    @Test
    fun slideShapesAnimationsTransition() {
        val file = edit("ppt2.pptx") { scenario, editor, asked ->
            scenario.onActivity {
                assertTrue(editor.run(EditAction.SHAPE_LIST))
                val shapes = asked.last() as EditRequest.Shapes
                assertTrue("shapes listed", shapes.shapes.isNotEmpty())
                shapes.select(shapes.shapes.first().id)
                assertTrue("selected", shapes.shapes.first().selected)
                assertTrue(editor.run(EditAction.ANIMATIONS))
                val animations = asked.last() as EditRequest.Animations
                assertEquals(shapes.shapes.first().id, animations.selectedShapeId)
                assertTrue(editor.run(EditAction.TRANSITION))
                val transition = asked.last() as EditRequest.Transition
                transition.answer(TransitionFormat("fade", durationMs = 1000))
                assertTrue(editor.run(EditAction.TRANSITION, TransitionFormat("push", "l")))
            }
        }
        assertTrue("transition saved", part(file, "ppt/slides/slide1.xml").contains("push"))
    }
}

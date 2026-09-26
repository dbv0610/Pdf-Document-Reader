package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.editor.pptx.PptxEditor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Duplicate, move and delete slides; the saved deck opens with the right order. */
@RunWith(AndroidJUnit4::class)
class PptxSlidesTest {
    private fun title(editor: PptxEditor, index: Int) = editor.listShapes(index).map { it.text }.firstOrNull { it.isNotBlank() } ?: ""

    @Test
    fun duplicateMoveDelete() {
        val source = OpenDocument.copySample("sample.pptx", "slides_source.pptx")
        val saved = OpenDocument.output("slides_saved.pptx")
        val editor = PptxEditor(source)
        assertEquals(10, editor.slideCount())
        val slide2 = title(editor, 1)
        val slide10 = title(editor, 9)
        assertEquals(2, editor.duplicateSlide(1))
        assertEquals(11, editor.slideCount())
        assertEquals(slide2, title(editor, 2))
        assertTrue(editor.lastError?.toString(), editor.moveSlide(10, 0))
        assertEquals(slide10, title(editor, 0))
        assertTrue(editor.lastError?.toString(), editor.deleteSlide(5))
        assertEquals(10, editor.slideCount())
        val result = editor.save(saved)
        assertTrue(result.toString(), result is EditResult.Ok)

        val reread = PptxEditor(saved)
        assertEquals(10, reread.slideCount())
        assertEquals(slide10, title(reread, 0))
        assertEquals(slide2, title(reread, 2))
        assertEquals(slide2, title(reread, 3))
        OpenDocument.open(saved, { it.pageCount >= 10 }) { reader ->
            assertEquals(10, reader.state.value.pageCount)
        }
    }
}

package com.wxiwei.office.wp.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.view.CharAttr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class LeafViewDrawTest {
    private class CountingPaint : Paint(ANTI_ALIAS_FLAG) {
        var measurements = 0

        override fun getTextWidths(text: String, widths: FloatArray): Int {
            measurements++
            return super.getTextWidths(text, widths)
        }
    }

    private class TextLeaf(text: String, from: Int = 0) : LeafView() {
        val textPaint = CountingPaint()
        val modelElement = LeafElement(text)

        init {
            modelElement.setStartOffset(0)
            modelElement.setEndOffset(text.length.toLong())
            setElement(modelElement)
            setStartOffset(from.toLong())
            setEndOffset(text.length.toLong())
            setSize(220, 40)
            paint = textPaint
            textPaint.textSize = 24f
            charAttr = CharAttr().apply { highlightedColor = -1 }
        }
    }

    private fun render(leaf: LeafView, zoom: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 120, Bitmap.Config.ARGB_8888)
        leaf.draw(Canvas(bitmap), 10, 10, zoom)
        return bitmap
    }

    @Test
    fun screenAndThumbnailReuseWidthsWithoutChangingPixels() {
        val leaf = TextLeaf("prefix Tiếng Việt e\u0301 😀\t text\n", 7)
        val screen = render(leaf, 1.25f)
        val worker = Executors.newSingleThreadExecutor()
        try {
            val thumbnail = worker.submit<Bitmap> { render(leaf, 0.5f) }.get()
            repeat(3) {
                val screenAgain = render(leaf, 1.25f)
                assertTrue(screen.sameAs(screenAgain))
                screenAgain.recycle()
                val thumbnailAgain = worker.submit<Bitmap> { render(leaf, 0.5f) }.get()
                assertTrue(thumbnail.sameAs(thumbnailAgain))
                thumbnailAgain.recycle()
            }
            assertEquals(2, leaf.textPaint.measurements)
            thumbnail.recycle()
        } finally {
            worker.shutdownNow()
            screen.recycle()
        }
    }

    @Test
    fun changedTextRangeAndSizeAreMeasuredAgain() {
        val leaf = TextLeaf("prefix first text", 7)
        render(leaf, 1f).recycle()
        render(leaf, 1f).recycle()
        assertEquals(1, leaf.textPaint.measurements)
        leaf.modelElement.setText("prefix other text")
        leaf.setEndOffset(leaf.modelElement.getEndOffset())
        render(leaf, 1f).recycle()
        leaf.setStartOffset(8)
        render(leaf, 1f).recycle()
        leaf.textPaint.textSize = 30f
        val changed = render(leaf, 1f)
        assertEquals(4, leaf.textPaint.measurements)
        val fresh = TextLeaf("prefix other text", 8)
        fresh.textPaint.textSize = 30f
        val expected = render(fresh, 1f)
        assertTrue(expected.sameAs(changed))
        changed.recycle()
        expected.recycle()
    }
}

/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.system.IMainFrame
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Excel pinch-zoom: frames stay cheap while the fingers move, the zoom follows the finger spread. */
@RunWith(AndroidJUnit4::class)
class PinchZoomTest {
    private fun event(down: Long, action: Int, x0: Float, x1: Float?, y: Float): MotionEvent {
        val ids = if (x1 == null) intArrayOf(0) else intArrayOf(0, 1)
        val props = ids.map { MotionEvent.PointerProperties().apply { id = it; toolType = MotionEvent.TOOL_TYPE_FINGER } }.toTypedArray()
        val coords = ids.map { i -> MotionEvent.PointerCoords().apply { x = if (i == 0) x0 else x1!!; this.y = y; pressure = 1f; size = 1f } }.toTypedArray()
        return MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, ids.size, props, coords, 0, 0, 1f, 1f, 0, 0, 0, 0)
    }

    /**
     * Fingers resting during a pinch must not count as a long press (in edit mode it starts a
     * range drag, which takes the touch away), and a pinch that is taken away still ends: the
     * preview frame, which has no row/column headers in place, must not stay on screen.
     */
    @Test
    fun pinchHeldOrCancelledKeepsHeaders() {
        val file = OpenDocument.copySample("sample.xlsx", "pinch-held.xlsx")
        OpenDocument.open(file) { reader ->
            delay(3000)
            val ss = onMain { (reader.control!!.getView() as ExcelView).getSpreadsheet()!! }
            val w = onMain { ss.width }.toFloat()
            val h = onMain { ss.height }.toFloat()
            var longPress = false
            onMain { reader.onDocumentGesture = { type, _ -> if (type == IMainFrame.ON_LONG_PRESS) longPress = true; false } }
            val cx = w / 2; val cy = h / 2
            val down = SystemClock.uptimeMillis()
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_DOWN, cx - 100, null, cy)) }
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), cx - 100, cx + 100, cy)) }
            for (i in 1..20) {
                val d = 100f + i * 5f
                onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_MOVE, cx - d, cx + d, cy)) }
            }
            delay(1500)
            assertFalse("no long press while pinching", longPress)
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_CANCEL, cx - 200, cx + 200, cy)) }
            val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
            onMain { ss.draw(Canvas(bitmap)) }
            assertEquals("headers are back", SSConstant.HEADER_FILL_COLOR, bitmap.getPixel(2, 2))
            assertEquals("the zoom reached is kept", 2f, onMain { ss.getSheetView()!!.getZoom() }, 0.05f)
        }
    }

    /**
     * While the fingers pinch, the row and column headers stay where they are and moving both
     * fingers drags the sheet; the finger left after the pinch does not make the sheet jump.
     */
    @Test
    fun pinchKeepsHeadersAndFollowsTheFingers() {
        val file = OpenDocument.copySample("sample.xlsx", "pinch-follow.xlsx")
        OpenDocument.open(file) { reader ->
            delay(3000)
            val ss = onMain { (reader.control!!.getView() as ExcelView).getSpreadsheet()!! }
            val view = onMain { ss.getSheetView()!! }
            val w = onMain { ss.width }.toFloat()
            val h = onMain { ss.height }.toFloat()
            val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
            fun corner(): Int = onMain { ss.draw(Canvas(bitmap)); bitmap.getPixel(2, 2) }
            val cx = w / 2; val cy = h / 2
            val down = SystemClock.uptimeMillis()
            fun pinch(action: Int, middle: Float, half: Float, y: Float) =
                onMain { ss.dispatchTouchEvent(event(down, action, middle - half, middle + half, y)) }
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_DOWN, cx - 100, null, cy)) }
            pinch(MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), cx, 100f, cy)
            for (i in 1..10) pinch(MotionEvent.ACTION_MOVE, cx, 100f + i * 10f, cy)
            assertEquals("zoomed while pinching", 2f, onMain { view.getZoom() }, 0.05f)
            assertEquals("the headers are drawn while pinching", SSConstant.HEADER_FILL_COLOR, corner())
            assertEquals("at their size for this zoom", 60, onMain { view.getColumnHeaderHeight() })

            // both fingers to the left: the sheet goes with them (down the sheet it is at its end already)
            val scrollX = onMain { view.getScrollX() }
            for (i in 1..10) pinch(MotionEvent.ACTION_MOVE, cx - i * 20f, 200f, cy)
            assertEquals("dragged 200 px at zoom 2", scrollX + 100f, onMain { view.getScrollX() }, 2f)
            assertEquals(SSConstant.HEADER_FILL_COLOR, corner())

            pinch(MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), cx - 200, 200f, cy)
            val after = onMain { view.getScrollX() to view.getScrollY() }
            for (i in 1..5) onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_MOVE, cx - 400 + i * 30f, null, cy - i * 30f)) }
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_UP, cx - 250, null, cy - 150)) }
            assertEquals("no jump from the finger left on the screen", after, onMain { view.getScrollX() to view.getScrollY() })
            assertEquals(SSConstant.HEADER_FILL_COLOR, corner())
        }
    }

    /** Word and PowerPoint: two fingers resting on the page are not a long press of the first. */
    @Test
    fun twoFingersAreNotALongPressInWordAndPowerPoint() {
        for (sample in listOf("sample.docx", "sample.pptx")) {
            OpenDocument.open(OpenDocument.copySample(sample, "pinch-$sample")) { reader ->
                delay(3000)
                val view = onMain { reader.control!!.getView()!! }
                var longPress = false
                onMain { reader.onDocumentGesture = { type, _ -> if (type == IMainFrame.ON_LONG_PRESS) longPress = true; false } }
                val cx = onMain { view.width } / 2f; val cy = onMain { view.height } / 2f
                val down = SystemClock.uptimeMillis()
                onMain { view.dispatchTouchEvent(event(down, MotionEvent.ACTION_DOWN, cx - 100, null, cy)) }
                onMain { view.dispatchTouchEvent(event(down, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), cx - 100, cx + 100, cy)) }
                delay(1500)
                onMain { view.dispatchTouchEvent(event(down, MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), cx - 100, cx + 100, cy)) }
                onMain { view.dispatchTouchEvent(event(down, MotionEvent.ACTION_UP, cx - 100, null, cy)) }
                assertFalse("no long press with two fingers down in $sample", longPress)
            }
        }
    }

    @Test
    fun pinchIsSmoothAndProportional() {
        val file = OpenDocument.copySample("sample.xlsx", "pinch.xlsx")
        OpenDocument.open(file) { reader ->
            delay(3000)
            val ss = onMain { (reader.control!!.getView() as ExcelView).getSpreadsheet()!! }
            val w = onMain { ss.width }.toFloat()
            val h = onMain { ss.height }.toFloat()
            val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            fun frameMs(): Double = onMain {
                val t = System.nanoTime(); ss.draw(canvas); (System.nanoTime() - t) / 1e6
            }
            val zoomBefore = onMain { ss.getSheetView()!!.getZoom() }
            val cx = w / 2; val cy = h / 2
            val down = SystemClock.uptimeMillis()
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_DOWN, cx - 100, null, cy)) }
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), cx - 100, cx + 100, cy)) }
            val frames = ArrayList<Double>()
            for (i in 1..20) {
                val d = 100f + i * 5f // spread 200 -> 400 px: zoom x2
                onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_MOVE, cx - d, cx + d, cy)) }
                frames.add(frameMs())
            }
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), cx - 200, cx + 200, cy)) }
            onMain { ss.dispatchTouchEvent(event(down, MotionEvent.ACTION_UP, cx - 200, null, cy)) }
            val zoomAfter = onMain { ss.getSheetView()!!.getZoom() }
            val after = frameMs()
            Log.i("PinchZoomTest", "zoom $zoomBefore -> $zoomAfter; pinch frames ms avg=${frames.average()} max=${frames.max()}; first frame after=$after")
            assertEquals("zoom follows the spread", minOf(zoomBefore * 2, 3f), zoomAfter, 0.05f)
            // a frame is the scaled picture of the cells plus the headers drawn for real (measured
            // here on a software canvas): within a frame on average, never as long as two
            assertTrue("pinch frames are cheap: avg ${frames.average()}ms", frames.average() < 16.0)
            assertTrue("no pinch frame is slow: ${frames.max()}ms", frames.max() < 32.0)
        }
    }
}

package com.wxiwei.office.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.ss.control.ExcelView
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
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
            assertTrue("pinch frames are cheap: ${frames.max()}ms", frames.max() < 16.0)
        }
    }
}

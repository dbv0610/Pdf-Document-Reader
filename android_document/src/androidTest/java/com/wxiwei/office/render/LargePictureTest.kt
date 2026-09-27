package com.wxiwei.office.render

import android.graphics.Bitmap
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.common.picture.PictureManage
import com.wxiwei.office.editor.OpenDocument
import com.wxiwei.office.editor.OpenDocument.onMain
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * The original Canva deck (34 MB, photos up to 3976x2652) shown on screen: its photos are decoded
 * at about the size they are drawn, not at full size. Push it first (too big for the test APK):
 * adb push big.pptx /sdcard/Android/data/com.wxiwei.office.test/files/big.pptx
 */
@RunWith(AndroidJUnit4::class)
class LargePictureTest {
    @Suppress("UNCHECKED_CAST")
    private fun cached(): List<Bitmap> = onMain {
        val field = PictureManage::class.java.getDeclaredField("bitmaps").apply { isAccessible = true }
        (field.get(null) as Map<String?, Bitmap?>).values.filterNotNull()
    }

    @Test
    fun photosDecodedAtShownSize() {
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "big.pptx")
        assumeTrue("big.pptx not pushed", file.isFile)
        OpenDocument.open(file, { it.pageCount >= 10 }) { reader ->
            val presentation = onMain { reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation }
            // slide 5 holds the 3976x2652 and 2882x1937 photos
            onMain { presentation.showSlide(4, false) }
            delay(4000)
            val bitmaps = cached()
            bitmaps.forEach { Log.i("LargePictureTest", "cached ${it.width}x${it.height} ${it.allocationByteCount / 1024} KB") }
            assertTrue("pictures drawn", bitmaps.isNotEmpty())
            val longest = bitmaps.maxOf { maxOf(it.width, it.height) }
            assertTrue("largest cached side $longest", longest < 3976)
        }
    }
}

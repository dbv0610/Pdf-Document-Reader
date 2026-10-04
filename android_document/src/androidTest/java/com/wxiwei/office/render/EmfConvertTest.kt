/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.render

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.thirdpart.emf.util.EMFUtil
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** An EMF picture (taken out of FloatingPictures.doc of the POI test files) is drawn. */
@RunWith(AndroidJUnit4::class)
class EmfConvertTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val outDir = File(instrumentation.targetContext.getExternalFilesDir(null), "render").apply { mkdirs() }

    private fun convert(name: String, width: Int, height: Int): Bitmap {
        val source = File(instrumentation.targetContext.cacheDir, name)
        instrumentation.context.assets.open("samples/old/$name").use { input -> source.outputStream().use { input.copyTo(it) } }
        val picture = File(outDir, name.substringBeforeLast('.') + "_emf.png").also { it.delete() }
        val bitmap = EMFUtil.convert(source.path, picture.path, width, height)
        assertTrue("written as a picture", picture.length() > 0)
        return bitmap
    }

    /** Pixels that are neither transparent nor white. */
    private fun drawn(bitmap: Bitmap): Int {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels.count { Color.alpha(it) != 0 && it != Color.WHITE }
    }

    @Test
    fun pictureOfAWordDocument() {
        val bitmap = convert("FloatingPictures.emf", 800, 150)
        assertTrue("something is drawn: ${drawn(bitmap)} pixels of ${bitmap.width}x${bitmap.height}", drawn(bitmap) > 200)
    }

}

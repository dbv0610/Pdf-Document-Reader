/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.fc.ppt.reader

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Canva's embedded fonts (.fntdata): uncompressed EOT around a TrueType font. */
class EmbeddedFontReaderTest {
    private fun resource(name: String) = javaClass.getResourceAsStream("/fonts/$name")!!.use { it.readBytes() }

    private fun sfnt(eot: ByteArray): ByteArray {
        val header = ByteBuffer.wrap(eot).order(ByteOrder.LITTLE_ENDIAN)
        val size = header.getInt(0)
        return eot.copyOfRange(size - header.getInt(4), size)
    }

    @Test fun canvaFontsAreRead() {
        for (name in listOf("bricolage-regular.fntdata", "bricolage-bold.fntdata")) {
            val font = EmbeddedFontReader.extractFontData(resource(name))
            assertNotNull(name, font)
            assertEquals(name, 0x00010000, ByteBuffer.wrap(font!!).getInt(0))
        }
    }

    @Test fun weightTellsTheBoldFace() {
        assertEquals(400, EmbeddedFontReader.weightClass(EmbeddedFontReader.extractFontData(resource("bricolage-regular.fntdata"))!!))
        assertEquals(700, EmbeddedFontReader.weightClass(EmbeddedFontReader.extractFontData(resource("bricolage-bold.fntdata"))!!))
    }

    @Test fun plainTrueTypeIsKept() {
        val font = sfnt(resource("bricolage-regular.fntdata"))
        assertArrayEquals(font, EmbeddedFontReader.extractFontData(font))
    }

    @Test fun compressedIsRefusedAndXorIsDecoded() {
        val eot = resource("bricolage-regular.fntdata")
        val flagsAt = 12
        fun withFlags(flags: Int) = eot.copyOf().also { ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN).putInt(flagsAt, flags) }
        // MTX compression (0x4) is not supported
        assertNull(EmbeddedFontReader.extractFontData(withFlags(0x4)))
        // XOR "encryption" (0x10000000) with key 0x50
        val xored = withFlags(0x10000000)
        val header = ByteBuffer.wrap(xored).order(ByteOrder.LITTLE_ENDIAN)
        val size = header.getInt(0)
        for (i in size - header.getInt(4) until size) xored[i] = (xored[i].toInt() xor 0x50).toByte()
        assertArrayEquals(sfnt(eot), EmbeddedFontReader.extractFontData(xored))
    }

    @Test fun malformedIsNull() {
        assertNull(EmbeddedFontReader.extractFontData(ByteArray(10)))
        assertEquals(0, EmbeddedFontReader.weightClass(ByteArray(8)))
    }
}

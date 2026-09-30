/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.fc.doc

import org.junit.Assert.assertEquals
import org.junit.Test

class ParseHexColorTest {
    @Test fun rgbAndArgb() {
        assertEquals(0xFF00695C.toInt(), parseHexColor("00695C", 0))
        assertEquals(0xFFFFFFFF.toInt(), parseHexColor("#FFFFFF", 0))
        assertEquals(0x80FF0000.toInt(), parseHexColor("#80FF0000", 0))
    }

    @Test fun oddValuesGiveTheDefault() {
        for (v in listOf(null, "", "auto", "12345", "GGGGGG")) assertEquals(v.toString(), 7, parseHexColor(v, 7))
    }
}

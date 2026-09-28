/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.simpletext.font

import org.junit.Assert.assertEquals
import org.junit.Test

/** Line breaks inside a word never split an emoji (surrogate pairs) or a base letter from its mark. */
class FontKitClusterTest {
    private val kit = FontKit()

    @Test fun surrogatePairs() {
        val text = "🎼🎹✅x"
        assertEquals(0, kit.clusterStart(text, 1))
        assertEquals(2, kit.clusterStart(text, 2))
        assertEquals(2, kit.clusterStart(text, 3))
        assertEquals(2, kit.clusterEnd(text, 0))
        assertEquals(4, kit.clusterEnd(text, 3))
        assertEquals(5, kit.clusterEnd(text, 4))
    }

    @Test fun combiningMarks() {
        val text = "Việt" // "Việt" with decomposed marks
        assertEquals(2, kit.clusterStart(text, 3))
        assertEquals(2, kit.clusterStart(text, 4))
        assertEquals(5, kit.clusterEnd(text, 2))
    }

    @Test fun longWordBreaksBetweenCharacters() {
        // one "word" of emoji: no line-break opportunity, fall back to a character boundary
        assertEquals(4, kit.findBreakOffset("🎼🎹🎼🎹", 5))
        assertEquals(2, kit.findBreakOffset("🎼🎹🎼🎹", 3))
    }
}

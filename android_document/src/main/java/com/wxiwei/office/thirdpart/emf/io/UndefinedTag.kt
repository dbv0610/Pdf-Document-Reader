// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

/**
 * Tag to hold the data for an Undefined Tag for the TaggedIn/OutputStreams. The
 * data is read in and written as the number of bytes is known.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/UndefinedTag.java
 * 96b41b903496 2005/11/21 19:50:18 duns $
 */
class UndefinedTag
/**
 * Create Undefined Tag with 0 length.
 */ @JvmOverloads constructor(
    tagID: Int = Tag.Companion.DEFAULT_TAG,
    private val bytes: IntArray = IntArray(0)
) : Tag(tagID, 3) {
    /**
     * Create Undefined Tag.
     * 
     * @param tagID
     * undefined tagID
     * @param bytes
     * bytes that follow the undefined tag
     */

    override val tagType: Int
        get() = 0

    @Throws(IOException::class)
    override fun read(tagID: Int, input: TaggedInputStream?, len: Int): Tag {
        val bytes = input!!.readUnsignedByte(len)
        val tag = UndefinedTag(tagID, bytes)
        return tag
    }

    override fun toString(): String {
        return ("UNDEFINED TAG: " + tag + " length: " + bytes.size)
    }
}

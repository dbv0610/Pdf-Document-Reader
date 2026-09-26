// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

/**
 * Generic Tag to be used by TaggedIn/OutputStreams. The tag contains an ID,
 * name and a version. Concrete subclasses should implement the IO Read and
 * Write methods.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/Tag.java 96b41b903496
 * 2005/11/21 19:50:18 duns $
 */
abstract class Tag protected constructor(
    /**
     * Get the tag number.
     * 
     * @return tagID
     */
    val tag: Int,
    /**
     * Get the version number.
     * 
     * @return version number
     */
    val version: Int
) {
    private var name: String? = null

    /**
     * Get the tag name.
     * 
     * @return tag name
     */
    fun getName(): String? {
        if (name == null) {
            name = javaClass.getName()
            val dot = name!!.lastIndexOf(".")
            name = if (dot >= 0) name!!.substring(dot + 1) else name
        }
        return name
    }

    open val tagType: Int
        /**
         * This returns the type of block
         * 
         * @return tag type
         */
        get() = 0

    /**
     * This reads the information from the given input and returns a new Tag
     * 
     * @param tagID
     * id of the tag to read
     * @param input
     * stream to read from
     * @param len
     * length to read
     * @return read Tag
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    abstract fun read(tagID: Int, input: TaggedInputStream?, len: Int): Tag?

    abstract override fun toString(): String

    companion object {
        /**
         * This is the tagID for the default tag handler.
         */
        val DEFAULT_TAG: Int = -1
    }
}

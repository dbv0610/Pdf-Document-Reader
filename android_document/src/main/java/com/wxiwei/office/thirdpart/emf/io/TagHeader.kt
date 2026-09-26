// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

/**
 * @return tagID
 */

/**
 * @return tag length
 */
/**
 * Keeps the tagID and Length of a specific tag. To be used in the InputStream
 * to return the tagID and Length, and in the OutputStream to write them.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/TagHeader.java 96b41b903496
 * 2005/11/21 19:50:18 duns $
 */
class TagHeader
/**
 * Creates a tag header
 * 
 * @param tagID
 * id of tag
 * @param length
 * length of the tag, including the header
 */(
    /**
     * Sets the tag id
     * 
     * @param tagID
     * new tag id
     */
    var tag: Int,
    /**
     * Sets the length of the tag
     * 
     * @param length
     * new length
     */
    var length: Long
)

// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

/**
 * Class to keep registered Tags, which should be used by the
 * TaggedIn/OutputStream. A set of recognized Tags can be added to this class. A
 * concrete implementation of this stream should install all allowed tags.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/TagSet.java 96b41b903496
 * 2005/11/21 19:50:18 duns $
 */
open class TagSet {
    /**
     * This holds the individual tags.
     */
    protected var tags: MutableMap<Int, Tag>

    /**
     * The default tag handler.
     */
    protected var defaultTag: Tag?

    /**
     * Creates a Tag Set.
     */
    init {
        // Initialize the tag classes.
        defaultTag = UndefinedTag()
        tags = HashMap()
    }

    /**
     * Add a new tag to this set. If the tagID returned is the DEFAULT_TAG, then
     * the default handler is set to the given handler.
     * 
     * @param tag
     * tag to be added to set
     */
    fun addTag(tag: Tag) {
        println("addTag==========")
        val id = tag.tag
        if (id != Tag.Companion.DEFAULT_TAG) {
            tags.put(id, tag)
        } else {
            defaultTag = tag
        }
    }

    /**
     * Find tag for tagID.
     * 
     * @param tagID
     * tagID to find
     * @return correspoding tag or UndefinedTag if tagID is not found.
     */
    fun get(tagID: Int): Tag? {
        var tag = tags.get(tagID)
        if (tag == null) {
            tag = defaultTag
        }
        return tag
    }

    /**
     * Finds out if Tag for TagID exists.
     * 
     * @param tagID
     * tagID to find
     * @return true if corresponding Tag for TagID exists
     */
    fun exists(tagID: Int): Boolean {
        return (tags.get(tagID) != null)
    }
}

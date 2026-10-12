// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf

import com.wxiwei.office.thirdpart.emf.data.GDIObject
import com.wxiwei.office.thirdpart.emf.io.Tag
import com.wxiwei.office.thirdpart.emf.io.TaggedInputStream
import java.io.IOException

/**
 * EMF specific tag, from which all other EMF Tags inherit.
 * 
 * @author Mark Donszelmann
 * @version $Id: EMFTag.java 10367 2007-01-22 19:26:48Z duns $
 */
abstract class EMFTag
/**
 * Constructs a EMFTag.
 * 
 * @param id id of the element
 * @param version emf version in which this element was first supported
 */
protected constructor(id: Int, version: Int) : Tag(id, version), GDIObject {
    @Throws(IOException::class)
    public override fun read(tagID: Int, input: TaggedInputStream?, len: Int): Tag? {
        return read(tagID, input as EMFInputStream, len)
    }

    @Throws(IOException::class)
    abstract fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag?

    /**
     * @return a description of the tagName and tagID
     */
    override fun toString(): String {
        return "EMFTag " + getName() + " (" + tag + ")"
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
    }
}

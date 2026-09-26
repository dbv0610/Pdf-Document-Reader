// Copyright 2001-2006, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream

/**
 * Class to read Tagged blocks from a Stream. The tagged blocks (Tags) contain a
 * tagID and a Length, so that known and unknown tags can be read and written
 * (using the TaggedOutputStream). The stream also allows to read Actions, which
 * again come with a actionCode and a length. A set of recognized Tags and
 * Actions can be added to this stream. A concrete implementation of this stream
 * should decode/read the TagHeader. All Concrete tags should be inherited from
 * the Tag class and implement their read methods.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/TaggedInputStream.java
 * 295a924a923b 2006/12/15 20:32:24 duns $
 */
abstract class TaggedInputStream
/**
 * Creates a Tagged Input Stream
 * 
 * @param in
 * stream to read from
 * @param tagSet
 * available tag set
 * @param actionSet
 * available action set
 */ @JvmOverloads constructor(
    `in`: InputStream,
    /**
     * Set of tags that can be used by this Stream
     */
    protected var tagSet: TagSet,
    /**
     * Set of actions that can be used by this Stream
     */
    protected var actionSet: ActionSet?, littleEndian: Boolean = false
) : ByteCountInputStream(`in`, littleEndian, 8) {
    /**
     * Returns the currently valid TagHeader. Can be called durring the
     * tag.read() method.
     */
    /**
     * Currently read tagHeader, valid during readTag call.
     */
    var tagHeader: TagHeader? = null
        private set

    /**
     * Creates a Tagged Input Stream
     * 
     * @param in
     * stream to read from
     * @param tagSet
     * available tag set
     * @param actionSet
     * available action set
     * @param littleEndian
     * true if stream is little endian
     */

    /**
     * Add tag to tagset
     * 
     * @param tag
     * new tag
     */
    fun addTag(tag: Tag) {
        tagSet.addTag(tag)
    }

    /**
     * Decodes and returns the TagHeader, which includes a TagID and a length.
     * 
     * @return Decoded TagHeader
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    protected abstract fun readTagHeader(): TagHeader?

    /**
     * Read a tag.
     * 
     * @return read tag
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readTag(): Tag? {
        tagHeader = readTagHeader()
        if (tagHeader == null) {
            return null
        }

        val size = tagHeader!!.length.toInt()

        // Look up the proper tag.
        var tag = tagSet.get(tagHeader!!.tag)

        // set max tag length and read tag
        pushBuffer(size)
        tag = tag!!.read(tagHeader!!.tag, this, size)
        val rest = popBuffer()

        // read non-read part of tag
        if (rest != null) {
            throw IncompleteTagException(tag, rest)
        }
        return tag
    }

    /**
     * Add action to action set.
     * 
     * @param action
     * new action
     */
    fun addAction(action: Action?) {
        actionSet!!.addAction(action!!)
    }

    /**
     * Decodes and returns the ActionHeader, which includes an actionCode and a
     * length.
     * 
     * @return decoded ActionHeader
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    protected abstract fun readActionHeader(): ActionHeader?

    /**
     * Reads action.
     * 
     * @return read action
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readAction(): Action? {
        val header = readActionHeader()
        if (header == null) {
            return null
        }

        val size = header.length.toInt()

        // Look up the proper action.
        var action = actionSet!!.get(header.action)

        pushBuffer(size)
        action = action!!.read(header.action, this, size)
        val rest = popBuffer()

        if (rest != null) {
            throw IncompleteActionException(action, rest)
        }
        return action
    }
}

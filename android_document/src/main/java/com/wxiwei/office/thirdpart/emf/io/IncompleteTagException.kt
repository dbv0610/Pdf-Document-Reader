// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

/**
 * Exception for the TaggedInputStream. Signals that the inputstream contains
 * more bytes than the stream has read for this tag.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/IncompleteTagException.java
 * 96b41b903496 2005/11/21 19:50:18 duns $
 */
class IncompleteTagException(
    /**
     * @return tag
     */
    val tag: Tag?, rest: ByteArray
) : IOException("Tag " + tag + " contains " + rest.size + " unread bytes") {
    /**
     * @return unused bytes
     */
    val bytes: ByteArray?

    /**
     * Creates an Incomplete Tag Exception
     * 
     * @param tag
     * incomplete tag
     * @param rest
     * unused bytes
     */
    init {
        this.bytes = rest
    }

    companion object {
        /**
         * 
         */
        private val serialVersionUID = -7808675150856818588L
    }
}

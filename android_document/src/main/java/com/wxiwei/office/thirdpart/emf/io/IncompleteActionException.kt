// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

/**
 * Exception for the TaggedInputStream. Signals that the inputstream contains
 * more bytes than the stream has read for this action.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id:
 * src/main/java/org/freehep/util/io/IncompleteActionException.java
 * 96b41b903496 2005/11/21 19:50:18 duns $
 */
class IncompleteActionException(
    /**
     * @return action
     */
    val action: Action?, rest: ByteArray
) : IOException("Action " + action + " contains " + rest.size + " unread bytes") {
    /**
     * @return unused bytes
     */
    val bytes: ByteArray?

    /**
     * Creates an Incomplete Action Exception
     * 
     * @param action
     * incompleted action
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
        private val serialVersionUID = -6817511986951461967L
    }
}

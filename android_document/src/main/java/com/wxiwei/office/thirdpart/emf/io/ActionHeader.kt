// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

/**
 * @return action code
 */

/**
 * @return length of this tag
 */
/**
 * Keeps the actionCode and Length of a specific action. To be used in the
 * TaggedInputStream to return the actionCode and Length, and in the
 * TaggedOutputStream to write them.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/ActionHeader.java
 * 96b41b903496 2005/11/21 19:50:18 duns $
 */
class ActionHeader
/**
 * Creates an action header
 * 
 * @param actionCode
 * code for action
 * @param length
 * total length of the tag
 */(
    /**
     * Sets the action code
     * 
     * @param actionCode
     * new action code
     */
    var action: Int,
    /**
     * Sets the length of this tag
     * 
     * @param length
     * new length
     */
    var length: Long
)

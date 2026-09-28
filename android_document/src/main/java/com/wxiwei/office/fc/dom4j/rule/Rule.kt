/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.rule

import com.wxiwei.office.fc.dom4j.Node

/**
 * 
 * 
 * `Rule` matches against DOM4J Node so that some action can be
 * performed such as in the XSLT processing model.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.7 $
 */
class Rule : Comparable<Any?> {
    /**
     * Getter for property mode.
     * 
     * @return Value of property mode.
     */
    /**
     * Setter for property mode.
     * 
     * @param mode
     * New value of property mode.
     */
    /** Holds value of property mode.  */
    var mode: String? = null

    /**
     * Getter for property importPrecedence.
     * 
     * @return Value of property importPrecedence.
     */
    /**
     * Setter for property importPrecedence.
     * 
     * @param importPrecedence
     * New value of property importPrecedence.
     */
    /** Holds value of property importPrecedence.  */
    var importPrecedence: Int = 0

    /**
     * Getter for property priority.
     * 
     * @return Value of property priority.
     */
    /**
     * Setter for property priority.
     * 
     * @param priority
     * New value of property priority.
     */
    /** Holds value of property priority.  */
    var priority: Double

    /**
     * Getter for property appearenceCount.
     * 
     * @return Value of property appearenceCount.
     */
    /**
     * Setter for property appearenceCount.
     * 
     * @param appearenceCount
     * New value of property appearenceCount.
     */
    /** Holds value of property appearenceCount.  */
    var appearenceCount: Int = 0

    /** Holds value of property pattern.  */
    private var pattern: Pattern? = null

    /**
     * Getter for property action.
     * 
     * @return Value of property action.
     */
    /**
     * Setter for property action.
     * 
     * @param action
     * New value of property action.
     */
    /** Holds value of property action.  */
    var action: Action? = null

    constructor() {
        this.priority = Pattern.Companion.DEFAULT_PRIORITY
    }

    constructor(pattern: Pattern) {
        this.pattern = pattern
        this.priority = pattern.priority
    }

    constructor(pattern: Pattern, action: Action?) : this(pattern) {
        this.action = action
    }

    /**
     * Constructs a new Rule with the same instance data as the given rule but a
     * different pattern.
     * 
     * @param that
     * DOCUMENT ME!
     * @param pattern
     * DOCUMENT ME!
     */
    constructor(that: Rule, pattern: Pattern) {
        this.mode = that.mode
        this.importPrecedence = that.importPrecedence
        this.priority = that.priority
        this.appearenceCount = that.appearenceCount
        this.action = that.action
        this.pattern = pattern
    }

    override fun equals(that: Any?): Boolean {
        if (that is Rule) {
            return compareTo(that) == 0
        }

        return false
    }

    override fun hashCode(): Int {
        return importPrecedence + appearenceCount
    }

    override fun compareTo(that: Any?): Int {
        if (that is Rule) {
            return compareTo(that)
        }

        return javaClass.getName().compareTo(that!!.javaClass.getName())
    }

    /**
     * Compares two rules in XSLT processing model order assuming that the modes
     * are equal.
     * 
     * @param that
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    fun compareTo(that: Rule): Int {
        var answer = this.importPrecedence - that.importPrecedence

        if (answer == 0) {
            answer = Math.round(this.priority - that.priority).toInt()

            if (answer == 0) {
                answer = this.appearenceCount - that.appearenceCount
            }
        }

        return answer
    }

    override fun toString(): String {
        return super.toString() + "[ pattern: " + getPattern() + " action: " + this.action + " ]"
    }

    /**
     * DOCUMENT ME!
     * 
     * @param node
     * DOCUMENT ME!
     * 
     * @return true if the pattern matches the given DOM4J node.
     */
    fun matches(node: Node?): Boolean {
        return pattern!!.matches(node)
    }

    val unionRules: Array<Rule?>?
        /**
         * If this rule contains a union pattern then this method should return an
         * array of Rules which describe the union rule, which should contain more
         * than one rule. Otherwise this method should return null.
         * 
         * @return an array of the rules which make up this union rule or null if
         * this rule is not a union rule
         */
        get() {
            val patterns =
                pattern!!.unionPatterns

            if (patterns == null) {
                return null
            }

            val size = patterns.size
            val answer =
                arrayOfNulls<Rule>(size)

            for (i in 0..<size) {
                answer[i] = Rule(this, patterns[i]!!)
            }

            return answer
        }

    val matchType: Short
        /**
         * DOCUMENT ME!
         * 
         * @return the type of node the pattern matches which by default should
         * return ANY_NODE if it can match any kind of node.
         */
        get() = pattern!!.matchType

    val matchesNodeName: String?
        /**
         * For patterns which only match an ATTRIBUTE_NODE or an ELEMENT_NODE then
         * this pattern may return the name of the element or attribute it matches.
         * This allows a more efficient rule matching algorithm to be performed,
         * rather than a brute force approach of evaluating every pattern for a
         * given Node.
         * 
         * @return the name of the element or attribute this pattern matches or null
         * if this pattern matches any or more than one name.
         */
        get() = pattern!!.matchesNodeName

    /**
     * Getter for property pattern.
     * 
     * @return Value of property pattern.
     */
    fun getPattern(): Pattern {
        return pattern!!
    }

    /**
     * Setter for property pattern.
     * 
     * @param pattern
     * New value of property pattern.
     */
    fun setPattern(pattern: Pattern) {
        this.pattern = pattern
    }
} /*
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name "DOM4J" must not be used to endorse or promote products derived
 * from this Software without prior written permission of MetaStuff, Ltd. For
 * written permission, please contact dom4j-info@metastuff.com.
 * 
 * 4. Products derived from this Software may not be called "DOM4J" nor may
 * "DOM4J" appear in their names without prior written permission of MetaStuff,
 * Ltd. DOM4J is a registered trademark of MetaStuff, Ltd.
 * 
 * 5. Due credit should be given to the DOM4J Project - http://www.dom4j.org
 * 
 * THIS SOFTWARE IS PROVIDED BY METASTUFF, LTD. AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL METASTUFF, LTD. OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 */


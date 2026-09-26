/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.rule

import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.rule.pattern.NodeTypePattern

/**
 * 
 * 
 * `RuleManager` manages a set of rules such that a rule can be
 * found for a given DOM4J Node using the XSLT processing model.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.9 $
 */
class RuleManager {
    /** Map of modes indexed by mode  */
    private val modes: HashMap<String?, Mode?> = HashMap<String?, Mode?>()

    /**
     * A counter so that rules can be ordered by the order in which they were
     * added to the rule base
     */
    private var appearenceCount = 0

    /**
     * DOCUMENT ME!
     * 
     * @return the default value-of action which is used in the default rules
     * for the pattern "text()|&#64;"
     */
    /**
     * Sets the default value-of action which is used in the default rules for
     * the pattern "text()|&#64;"
     * 
     * @param valueOfAction
     * DOCUMENT ME!
     */
    /** Holds value of property valueOfAction.  */
    var valueOfAction: Action? = null

    /**
     * DOCUMENT ME!
     * 
     * @param modeName
     * DOCUMENT ME!
     * 
     * @return the Mode instance for the given mode name. If one does not exist
     * then it will be created.
     */
    fun getMode(modeName: String?): Mode {
        var mode = modes.get(modeName) as Mode?

        if (mode == null) {
            mode = createMode()
            modes.put(modeName, mode)
        }

        return mode
    }

    fun addRule(rule: Rule) {
        rule.appearenceCount = ++appearenceCount

        val mode = getMode(rule.mode)
        val childRules = rule.unionRules

        if (childRules != null) {
            var i = 0
            val size = childRules.size
            while (i < size) {
                mode.addRule(childRules[i]!!)
                i++
            }
        } else {
            mode.addRule(rule)
        }
    }

    fun removeRule(rule: Rule) {
        val mode = getMode(rule.mode)
        val childRules = rule.unionRules

        if (childRules != null) {
            var i = 0
            val size = childRules.size
            while (i < size) {
                mode.removeRule(childRules[i]!!)
                i++
            }
        } else {
            mode.removeRule(rule)
        }
    }

    /**
     * Performs an XSLT processing model match for the rule which matches the
     * given Node the best.
     * 
     * @param modeName
     * is the name of the mode associated with the rule if any
     * @param node
     * is the DOM4J Node to match against
     * 
     * @return the matching Rule or no rule if none matched
     */
    fun getMatchingRule(modeName: String?, node: Node): Rule? {
        val mode = modes.get(modeName) as Mode?

        if (mode != null) {
            return mode.getMatchingRule(node)
        } else {
            println("Warning: No Mode for mode: " + mode)

            return null
        }
    }

    fun clear() {
        modes.clear()
        appearenceCount = 0
    }

    // Properties
    // -------------------------------------------------------------------------

    // Implementation methods
    // -------------------------------------------------------------------------
    /**
     * A factory method to return a new [Mode]instance which should add
     * the necessary default rules
     * 
     * @return DOCUMENT ME!
     */
    protected fun createMode(): Mode {
        val mode = Mode()
        addDefaultRules(mode)

        return mode
    }

    /**
     * Adds the default stylesheet rules to the given [Mode]instance
     * 
     * @param mode
     * DOCUMENT ME!
     */
    protected fun addDefaultRules(mode: Mode) {
        // add an apply templates rule
        val applyTemplates: Action = object : Action {
            @Throws(Exception::class)
            override fun run(node: Node?) {
                if (node is Element) {
                    mode.applyTemplates(node)
                } else if (node is Document) {
                    mode.applyTemplates(node)
                }
            }
        }

        val valueOf = this.valueOfAction

        addDefaultRule(mode, NodeTypePattern.Companion.ANY_DOCUMENT, applyTemplates)
        addDefaultRule(mode, NodeTypePattern.Companion.ANY_ELEMENT, applyTemplates)

        if (valueOf != null) {
            addDefaultRule(mode, NodeTypePattern.Companion.ANY_ATTRIBUTE, valueOf)
            addDefaultRule(mode, NodeTypePattern.Companion.ANY_TEXT, valueOf)
        }
    }

    protected fun addDefaultRule(mode: Mode, pattern: Pattern?, action: Action?) {
        val rule = createDefaultRule(pattern, action)
        mode.addRule(rule)
    }

    protected fun createDefaultRule(pattern: Pattern?, action: Action?): Rule {
        val rule = Rule(pattern!!, action)
        rule.importPrecedence = -1

        return rule
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


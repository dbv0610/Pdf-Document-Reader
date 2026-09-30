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

import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node

/**
 * 
 * 
 * `Mode` manages a number of RuleSet instances for the mode in a
 * stylesheet. It is responsible for finding the correct rule for a given DOM4J
 * Node using the XSLT processing model uses the smallest possible RuleSet to
 * reduce the number of Rule evaluations.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.9 $
 */
class Mode {
    private val ruleSets = arrayOfNulls<RuleSet>(Pattern.Companion.NUMBER_OF_TYPES.toInt())

    /** Map of exact (local) element names to RuleSet instances  */
    private var elementNameRuleSets: MutableMap<Any?, Any?>? = null

    /** Map of exact (local) attribute names to RuleSet instances  */
    private var attributeNameRuleSets: MutableMap<Any?, Any?>? = null

    /**
     * Runs the actions associated with the given node
     * 
     * @param node
     * DOCUMENT ME!
     * 
     * @throws Exception
     * DOCUMENT ME!
     */
    @Throws(Exception::class)
    fun fireRule(node: Node?) {
        if (node != null) {
            val rule = getMatchingRule(node)

            if (rule != null) {
                val action = rule.action

                if (action != null) {
                    action.run(node)
                }
            }
        }
    }

    @Throws(Exception::class)
    fun applyTemplates(element: Element) {
        run {
            var i = 0
            val size = element.attributeCount()
            while (i < size) {
                val attribute = element.attribute(i)
                fireRule(attribute)
                i++
            }
        }

        var i = 0
        val size = element.nodeCount()
        while (i < size) {
            val node = element.node(i)
            fireRule(node)
            i++
        }
    }

    @Throws(Exception::class)
    fun applyTemplates(document: Document) {
        var i = 0
        val size = document.nodeCount()
        while (i < size) {
            val node = document.node(i)
            fireRule(node)
            i++
        }
    }

    fun addRule(rule: Rule) {
        var matchType = rule.matchType.toInt()
        val name = rule.matchesNodeName

        if (name != null) {
            if (matchType == Node.Companion.ELEMENT_NODE.toInt()) {
                elementNameRuleSets = addToNameMap(elementNameRuleSets, name, rule)
            } else if (matchType == Node.Companion.ATTRIBUTE_NODE.toInt()) {
                attributeNameRuleSets = addToNameMap(attributeNameRuleSets, name, rule)
            }
        }

        if (matchType >= Pattern.Companion.NUMBER_OF_TYPES) {
            matchType = Pattern.Companion.ANY_NODE.toInt()
        }

        if (matchType == Pattern.Companion.ANY_NODE.toInt()) {
            // add rule to all other RuleSets if they exist
            var i = 1
            val size = ruleSets.size
            while (i < size) {
                val ruleSet = ruleSets[i]

                if (ruleSet != null) {
                    ruleSet.addRule(rule)
                }
                i++
            }
        }

        getRuleSet(matchType).addRule(rule)
    }

    fun removeRule(rule: Rule) {
        var matchType = rule.matchType.toInt()
        val name = rule.matchesNodeName

        if (name != null) {
            if (matchType == Node.Companion.ELEMENT_NODE.toInt()) {
                removeFromNameMap(elementNameRuleSets, name, rule)
            } else if (matchType == Node.Companion.ATTRIBUTE_NODE.toInt()) {
                removeFromNameMap(attributeNameRuleSets, name, rule)
            }
        }

        if (matchType >= Pattern.Companion.NUMBER_OF_TYPES) {
            matchType = Pattern.Companion.ANY_NODE.toInt()
        }

        getRuleSet(matchType).removeRule(rule)

        if (matchType != Pattern.Companion.ANY_NODE.toInt()) {
            getRuleSet(Pattern.Companion.ANY_NODE.toInt()).removeRule(rule)
        }
    }

    /**
     * Performs an XSLT processing model match for the rule which matches the
     * given Node the best.
     * 
     * @param node
     * is the DOM4J Node to match against
     * 
     * @return the matching Rule or no rule if none matched
     */
    fun getMatchingRule(node: Node): Rule? {
        var matchType = node.nodeType.toInt()

        if (matchType == Node.Companion.ELEMENT_NODE.toInt()) {
            if (elementNameRuleSets != null) {
                val name = node.name
                val ruleSet = elementNameRuleSets!!.get(name) as RuleSet?

                if (ruleSet != null) {
                    val answer = ruleSet.getMatchingRule(node)

                    if (answer != null) {
                        return answer
                    }
                }
            }
        } else if (matchType == Node.Companion.ATTRIBUTE_NODE.toInt()) {
            if (attributeNameRuleSets != null) {
                val name = node.name
                val ruleSet = attributeNameRuleSets!!.get(name) as RuleSet?

                if (ruleSet != null) {
                    val answer = ruleSet.getMatchingRule(node)

                    if (answer != null) {
                        return answer
                    }
                }
            }
        }

        if ((matchType < 0) || (matchType >= ruleSets.size)) {
            matchType = Pattern.Companion.ANY_NODE.toInt()
        }

        var answer: Rule? = null
        var ruleSet = ruleSets[matchType]

        if (ruleSet != null) {
            // try rules that match this kind of node first
            answer = ruleSet.getMatchingRule(node)
        }

        if ((answer == null) && (matchType != Pattern.Companion.ANY_NODE.toInt())) {
            // try general rules that match any kind of node
            ruleSet = ruleSets[Pattern.Companion.ANY_NODE.toInt()]

            if (ruleSet != null) {
                answer = ruleSet.getMatchingRule(node)
            }
        }

        return answer
    }

    /**
     * DOCUMENT ME!
     * 
     * @param matchType
     * DOCUMENT ME!
     * 
     * @return the RuleSet for the given matching type. This method will never
     * return null, a new instance will be created.
     */
    protected fun getRuleSet(matchType: Int): RuleSet {
        var ruleSet = ruleSets[matchType]

        if (ruleSet == null) {
            ruleSet = RuleSet()
            ruleSets[matchType] = ruleSet

            // add the patterns that match any node
            if (matchType != Pattern.Companion.ANY_NODE.toInt()) {
                val allRules = ruleSets[Pattern.Companion.ANY_NODE.toInt()]

                if (allRules != null) {
                    ruleSet.addAll(allRules)
                }
            }
        }

        return ruleSet
    }

    /**
     * Adds the Rule to a RuleSet for the given name.
     * 
     * @param map
     * DOCUMENT ME!
     * @param name
     * DOCUMENT ME!
     * @param rule
     * DOCUMENT ME!
     * 
     * @return the Map (which will be created if the given map was null
     */
    protected fun addToNameMap(
        map: MutableMap<Any?, Any?>?,
        name: String?,
        rule: Rule?
    ): MutableMap<Any?, Any?> {
        var map = map
        if (map == null) {
            map = HashMap<Any?, Any?>()
        }

        var ruleSet = map.get(name) as RuleSet?

        if (ruleSet == null) {
            ruleSet = RuleSet()
            map.put(name, ruleSet)
        }

        ruleSet.addRule(rule)

        return map
    }

    protected fun removeFromNameMap(map: MutableMap<Any?, Any?>?, name: String?, rule: Rule?) {
        if (map != null) {
            val ruleSet = map.get(name) as RuleSet?

            if (ruleSet != null) {
                ruleSet.removeRule(rule)
            }
        }
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


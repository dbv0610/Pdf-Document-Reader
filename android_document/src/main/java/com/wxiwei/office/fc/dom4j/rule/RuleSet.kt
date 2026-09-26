/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.rule

import com.wxiwei.office.fc.dom4j.Node
import java.util.Collections

/**
 * 
 * 
 * `RuleSet` manages a set of rules which are sorted in order of
 * relevance according to the XSLT defined conflict resolution policy. This
 * makes finding the correct rule for a DOM4J Node using the XSLT processing
 * model efficient as the rules can be evaluated in order of priority.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.10 $
 */
class RuleSet {
    /** An unordered list of Rule objects  */
    private val rules: ArrayList<Rule?> = ArrayList<Rule?>()

    /** A lazily evaluated and cached array of rules sorted  */
    private var ruleArray: Array<Rule?>? = null

    override fun toString(): String {
        return super.toString() + " [RuleSet: " + rules + " ]"
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
    fun getMatchingRule(node: Node?): Rule? {
        val matches = getRuleArray()

        for (i in matches.indices.reversed()) {
            val rule = matches[i]

            if (rule!!.matches(node)) {
                return rule
            }
        }

        return null
    }

    fun addRule(rule: Rule?) {
        rules.add(rule)
        ruleArray = null
    }

    fun removeRule(rule: Rule?) {
        rules.remove(rule)
        ruleArray = null
    }

    /**
     * Adds all the rules to this RuleSet from the given other rule set.
     * 
     * @param that
     * DOCUMENT ME!
     */
    fun addAll(that: RuleSet) {
        rules.addAll(that.rules)
        ruleArray = null
    }

    /**
     * Returns an array of sorted rules.
     * 
     * @return the rules as a sorted array in ascending precendence so that the
     * rules at the end of the array should be used first
     */
    protected fun getRuleArray(): Array<Rule?> {
        if (ruleArray == null) {
            Collections.sort(rules, Comparator { a, b -> a!!.compareTo(b) })

            val size = rules.size
            val array = arrayOfNulls<Rule>(size)
            rules.toArray(array)
            ruleArray = array
        }

        return ruleArray!!
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


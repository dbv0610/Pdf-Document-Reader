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
import com.wxiwei.office.fc.dom4j.XPath

/**
 * 
 * 
 * `Stylesheet` implements an XSLT stylesheet such that rules can
 * be added to the stylesheet and the stylesheet can be applied to a source
 * document or node.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.14 $
 */
class Stylesheet

/**
 * Creates a new empty stylesheet.
 */
{
    private val ruleManager = RuleManager()

    /**
     * DOCUMENT ME!
     * 
     * @return the name of the mode the stylesheet uses by default
     */
    /**
     * Sets the name of the mode that the stylesheet uses by default.
     * 
     * @param modeName
     * DOCUMENT ME!
     */
    /** Holds value of property mode.  */
    var modeName: String? = null

    /**
     * Add a rule to this stylesheet.
     * 
     * @param rule
     * the rule to add
     */
    fun addRule(rule: Rule) {
        ruleManager.addRule(rule)
    }

    /**
     * Removes the specified rule from this stylesheet.
     * 
     * @param rule
     * the rule to remove
     */
    fun removeRule(rule: Rule) {
        ruleManager.removeRule(rule)
    }

    /**
     * Runs this stylesheet on the given input which should be either a Node or
     * a List of Node objects.
     * 
     * @param input
     * the input to run this stylesheet on
     * 
     * @throws Exception
     * if something goes wrong
     */
    @JvmOverloads
    @Throws(Exception::class)
    fun run(input: Any?, mode: String? = this.modeName) {
        if (input is Node) {
            run(input, mode)
        } else if (input is MutableList<*>) {
            run(input, mode)
        }
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun run(list: MutableList<*>, mode: String? = this.modeName) {
        var i = 0
        val size = list.size
        while (i < size) {
            val `object`: Any? = list.get(i)

            if (`object` is Node) {
                run(`object`, mode)
            }
            i++
        }
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun run(node: Node?, mode: String? = this.modeName) {
        val mod = ruleManager.getMode(mode)
        mod.fireRule(node)
    }

    /**
     * Processes the result of the xpath expression in the given mode. The xpath
     * expression is evaluated against the provided input object.
     * 
     * @param input
     * the input object
     * @param xpath
     * the xpath expression
     * @param mode
     * the mode
     * @throws Exception
     * if something goes wrong
     */
    /**
     * Processes the result of the xpath expression. The xpath expression is
     * evaluated against the provided input object.
     * 
     * @param input
     * the input object
     * @param xpath
     * the xpath expression
     * @throws Exception
     * if something goes wrong
     */
    @JvmOverloads
    @Throws(Exception::class)
    fun applyTemplates(input: Any?, xpath: XPath, mode: String? = this.modeName) {
        val mod = ruleManager.getMode(mode)

        val list = xpath.selectNodes(input)
        val it: MutableIterator<*> = list!!.iterator()
        while (it.hasNext()) {
            val current = it.next() as Node?
            mod.fireRule(current)
        }
    }

    /**
     * Processes the result of the xpath expression. The xpath expression is
     * evaluated against the provided input object.
     * 
     * @param input
     * the input object
     * @param xpath
     * the xpath expression
     * @throws Exception
     * if something goes wrong
     * @param input
     * the input object
     * @param xpath
     * the xpath expression
     * @param mode
     * the mode
     * @throws Exception
     * if something goes wrong
     * @param input
     * the input object, this can either be a `Node` or
     * a `List`
     * @throws Exception
     * if something goes wrong
     */
    @Deprecated(
        """Use {@link Stylesheet#applyTemplates(Object, XPath)}instead.
      /
    public void applyTemplates(Object input, org.jaxen.XPath xpath) throws Exception
    {
        applyTemplates(input, xpath, this.modeName);
    }

    /**
      Processes the result of the xpath expression in the given mode. The xpath
      expression is evaluated against the provided input object.
      
      """
    )
    @Throws(Exception::class)
    fun applyTemplates(input: Any?) {
        applyTemplates(input, this.modeName)
    }

    /**
     * Processes the input object in the given mode. If input is a
     * `Node`, this will processes all of the children of that
     * node. If input is a `List` of `Nodes`s, these
     * nodes will be iterated and all children of each node will be processed.
     * 
     * @param input
     * the input object, this can either be a `Node` or
     * a `List`
     * @param mode
     * the mode
     * @throws Exception
     * if something goes wrong
     */
    @Throws(Exception::class)
    fun applyTemplates(input: Any?, mode: String?) {
        val mod = ruleManager.getMode(mode)

        if (input is Element) {
            // iterate through all children
            val element = input
            var i = 0
            val size = element.nodeCount()
            while (i < size) {
                val node = element.node(i)
                mod.fireRule(node)
                i++
            }
        } else if (input is Document) {
            // iterate through all children
            val document = input
            var i = 0
            val size = document.nodeCount()
            while (i < size) {
                val node = document.node(i)
                mod.fireRule(node)
                i++
            }
        } else if (input is MutableList<*>) {
            val list = input

            var i = 0
            val size = list.size
            while (i < size) {
                val `object`: Any? = list.get(i)

                if (`object` is Element) {
                    applyTemplates(`object`, mode)
                } else if (`object` is Document) {
                    applyTemplates(`object`, mode)
                }
                i++
            }
        }
    }

    fun clear() {
        ruleManager.clear()
    }

    // Properties
    // -------------------------------------------------------------------------

    var valueOfAction: Action?
        /**
         * DOCUMENT ME!
         * 
         * @return the default value-of action which is used in the default rules
         * for the pattern "text()|&#64;"
         */
        get() = ruleManager.valueOfAction
        /**
         * Sets the default value-of action which is used in the default rules for
         * the pattern "text()|&#64;"
         * 
         * @param valueOfAction
         * DOCUMENT ME!
         */
        set(valueOfAction) {
            ruleManager.valueOfAction = valueOfAction
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


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
package com.wxiwei.office.fc.dom4j.xpath

import com.wxiwei.office.fc.dom4j.InvalidXPathException
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.NodeFilter
import com.wxiwei.office.fc.dom4j.XPath
import java.io.Serializable
import java.util.Collections
import javax.xml.namespace.NamespaceContext

/**
 * 
 * 
 * Default implementation of [org.dom4j.XPath]which uses the [Jaxen ](http://jaxen.org) project.
 * 
 * 
 * @author bob mcwhirter
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 */
class DefaultXPath(text: String?) : XPath, NodeFilter, Serializable {
    override val text: String?

    private val xpath: XPath?

    override var namespaceContext: NamespaceContext? = null
        set(namespaceContext) {
            field = namespaceContext
            xpath!!.namespaceContext = namespaceContext
        }

    /**
     * Construct an XPath
     * 
     * @param text
     * DOCUMENT ME!
     * 
     * @throws InvalidXPathException
     * DOCUMENT ME!
     */
    init {
        this.text = text
        this.xpath = parse(text)
    }

    override fun toString(): String {
        return "[XPath: " + xpath + "]"
    }

    // XPath interface
    /**
     * Retrieve the textual XPath string used to initialize this Object
     * 
     * @return The XPath string
     */

    /*public FunctionContext getFunctionContext()
    {
        return xpath.getFunctionContext();
    }

    public void setFunctionContext(FunctionContext functionContext)
    {
        xpath.setFunctionContext(functionContext);
    }*/

    /*public void setNamespaceURIs(Map map)
    {
        setNamespaceContext(new SimpleNamespaceContext(map));
    }*/

    /*public VariableContext getVariableContext()
    {
        return xpath.getVariableContext();
    }*/
    /*public void setVariableContext(VariableContext variableContext)
    {
        xpath.setVariableContext(variableContext);
    }*/
    override fun evaluate(context: Any?): Any? {
        /*try
        {
            setNSContext(context);

            List answer = xpath.selectNodes(context);

            if ((answer != null) && (answer.size() == 1))
            {
                return answer.get(0);
            }

            return answer;
        }
        catch(JaxenException e)
        {
            handleJaxenException(e);

            return null;
        }*/
        return null
    }

    override fun selectObject(context: Any?): Any? {
        return evaluate(context)
    }

    override fun selectNodes(context: Any?): MutableList<*> {
        /*try
        {
            setNSContext(context);

            return xpath.selectNodes(context);
        }
        catch(JaxenException e)
        {
            handleJaxenException(e);

            return Collections.EMPTY_LIST;
        }*/
        return Collections.EMPTY_LIST
    }

    override fun selectNodes(context: Any?, sortXPath: XPath?): MutableList<*> {
        val answer = selectNodes(context)
        sortXPath!!.sort(answer)

        return answer
    }

    override fun selectNodes(context: Any?, sortXPath: XPath?, distinct: Boolean): MutableList<*> {
        val answer = selectNodes(context)
        sortXPath!!.sort(answer, distinct)

        return answer
    }

    override fun selectSingleNode(context: Any?): Node? {
        /*try
        {
            setNSContext(context);

            Object answer = xpath.selectSingleNode(context);

            if (answer instanceof Node)
            {
                return (Node)answer;
            }

            if (answer == null)
            {
                return null;
            }

            throw new XPathException("The result of the XPath expression is "
                + "not a Node. It was: " + answer + " of type: " + answer.getClass().getName());
        }
        catch(JaxenException e)
        {
            handleJaxenException(e);

            return null;
        }*/
        return null
    }

    override fun valueOf(context: Any?): String {
        /*try
        {
            setNSContext(context);

            return xpath.stringValueOf(context);
        }
        catch(JaxenException e)
        {
            handleJaxenException(e);

            return "";
        }*/
        return ""
    }

    override fun numberValueOf(context: Any?): Number? {
        /*try
        {
            setNSContext(context);

            return xpath.numberValueOf(context);
        }
        catch(JaxenException e)
        {
            handleJaxenException(e);

            return null;
        }*/
        return null
    }

    override fun booleanValueOf(context: Any?): Boolean {
        /*try
        {
            setNSContext(context);

            return xpath.booleanValueOf(context);
        }
        catch(JaxenException e)
        {
            handleJaxenException(e);

            return false;
        }*/
        return false
    }

    /**
     * 
     * 
     * `sort` sorts the given List of Nodes using this XPath
     * expression as a [Comparator].
     * 
     * 
     * @param list
     * is the list of Nodes to sort
     */
    override fun sort(list: MutableList<*>?) {
        sort(list, false)
    }

    /**
     * 
     * 
     * `sort` sorts the given List of Nodes using this XPath
     * expression as a [Comparator]and optionally removing duplicates.
     * 
     * 
     * @param list
     * is the list of Nodes to sort
     * @param distinct
     * if true then duplicate values (using the sortXPath for
     * comparisions) will be removed from the List
     */
    override fun sort(list: MutableList<*>?, distinct: Boolean) {
        if ((list != null) && !list.isEmpty()) {
            val size = list.size
            val sortValues: HashMap<Any?, Any?> = HashMap<Any?, Any?>(size)

            for (i in 0..<size) {
                val `object`: Any? = list.get(i)

                if (`object` is Node) {
                    val node = `object`
                    val expression = getCompareValue(node)
                    sortValues.put(node, expression)
                }
            }

            sort(list, sortValues)

            if (distinct) {
                removeDuplicates(list, sortValues)
            }
        }
    }

    override fun matches(node: Node?): Boolean {
        /*try
        {
            setNSContext(node);

            List answer = xpath.selectNodes(node);

            if ((answer != null) && (answer.size() > 0))
            {
                Object item = answer.get(0);

                if (item instanceof Boolean)
                {
                    return ((Boolean)item).booleanValue();
                }

                return answer.contains(node);
            }

            return false;
        }
        catch(JaxenException e)
        {
            handleJaxenException(e);

            return false;
        }*/
        return false
    }

    /**
     * Sorts the list based on the sortValues for each node
     * 
     * @param list
     * DOCUMENT ME!
     * @param sortValues
     * DOCUMENT ME!
     */
    protected fun sort(list: MutableList<*>, sortValues: MutableMap<*, *>) {
        Collections.sort<Any?>(list as MutableList<Any?>, object : Comparator<Any?> {
            override fun compare(o1: Any?, o2: Any?): Int {
                var o1 = o1
                var o2 = o2
                o1 = sortValues.get(o1)
                o2 = sortValues.get(o2)

                if (o1 === o2) {
                    return 0
                } else if (o1 is Comparable<*>) {
                    val c1 = o1 as Comparable<Any?>

                    return c1.compareTo(o2)
                } else if (o1 == null) {
                    return 1
                } else if (o2 == null) {
                    return -1
                } else {
                    return if (o1 == o2) 0 else (-1)
                }
            }
        })
    }

    // Implementation methods
    /**
     * Removes items from the list which have duplicate values
     * 
     * @param list
     * DOCUMENT ME!
     * @param sortValues
     * DOCUMENT ME!
     */
    protected fun removeDuplicates(list: MutableList<*>, sortValues: MutableMap<*, *>) {
        // remove distinct
        val distinctValues: HashSet<Any?> = HashSet<Any?>()

        val iter: MutableIterator<*> = list.iterator()
        while (iter.hasNext()) {
            val node = iter.next()
            val value = sortValues.get(node)

            if (distinctValues.contains(value)) {
                iter.remove()
            } else {
                distinctValues.add(value)
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param node
     * DOCUMENT ME!
     * 
     * @return the node expression used for sorting comparisons
     */
    protected fun getCompareValue(node: Node?): Any {
        return valueOf(node)
    }

    protected fun setNSContext(context: Any?) {
        if (namespaceContext == null) {
            xpath!!.namespaceContext = DefaultNamespaceContext.Companion.create(context)
        }
    }


    /*protected void handleJaxenException(JaxenException exception) throws XPathException
    {
        throw new XPathException(text, exception);
    }*/
    fun setNamespaceContext1(namespaceContext: NamespaceContext?) {
     
    }

    override fun setNamespaceURIs(map: MutableMap<*, *>?) {
     
    }

    companion object {
        protected fun parse(text: String?): XPath? {
            /*try
        {
            return new Dom4jXPath(text);
        }
        catch(JaxenException e)
        {
            throw new InvalidXPathException(text, e.getMessage());
        }
        catch(Throwable t)
        {
            throw new InvalidXPathException(text, t);
        }*/
            return null
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


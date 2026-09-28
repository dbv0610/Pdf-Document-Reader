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
package com.wxiwei.office.fc.dom4j.util

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.tree.DefaultElement

/**
 * 
 * 
 * `IndexedElement` is an implementation of [Element]which
 * maintains an index of the attributes and elements it contains to optimise
 * lookups via name.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.10 $
 */
class IndexedElement : DefaultElement {
    /** Lazily constructed index for elements  */
    private var elementIndex: MutableMap<*, *>? = null

    /** Lazily constructed index for attributes  */
    private var attributeIndex: MutableMap<*, *>? = null

    constructor(name: String?) : super(name)

    constructor(qname: QName?) : super(qname)

    constructor(qname: QName?, attributeCount: Int) : super(qname, attributeCount)

    override fun attribute(name: String?): Attribute? {
        return attributeIndex()!!.get(name) as Attribute?
    }

    override fun attribute(qName: QName?): Attribute? {
        return attributeIndex()!!.get(qName) as Attribute?
    }

    override fun element(name: String?): Element? {
        return asElement(elementIndex()!!.get(name))
    }

    override fun element(qName: QName?): Element? {
        return asElement(elementIndex()!!.get(qName))
    }

    override fun elements(name: String?): MutableList<*>? {
        return asElementList(elementIndex()!!.get(name))
    }

    override fun elements(qName: QName?): MutableList<*>? {
        return asElementList(elementIndex()!!.get(qName))
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected fun asElement(`object`: Any?): Element? {
        if (`object` is Element) {
            return `object`
        } else if (`object` != null) {
            val list = `object` as MutableList<*>

            if (list.size >= 1) {
                return list.get(0) as Element?
            }
        }

        return null
    }

    protected fun asElementList(`object`: Any?): MutableList<*>? {
        if (`object` is Element) {
            return createSingleResultList(`object`)
        } else if (`object` != null) {
            val list = `object` as MutableList<*>
            val answer = createResultList()

            var i = 0
            val size = list.size
            while (i < size) {
                answer.addLocal(list.get(i))
                i++
            }

            return answer
        }

        return createEmptyList()
    }

    /**
     * DOCUMENT ME!
     * 
     * @param object
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     */
    @Deprecated("WILL BE REMOVED IN dom4j-1.6 !!")
    protected fun asElementIterator(`object`: Any?): MutableIterator<*> {
        return asElementList(`object`)!!.iterator()
    }

    // #### could we override the add(Element) remove(Element methods?
    override fun addNode(node: Node?) {
        super.addNode(node)

        if ((elementIndex != null) && node is Element) {
            addToElementIndex(node)
        } else if ((attributeIndex != null) && node is Attribute) {
            addToAttributeIndex(node)
        }
    }

    override fun removeNode(node: Node?): Boolean {
        if (super.removeNode(node)) {
            if ((elementIndex != null) && node is Element) {
                removeFromElementIndex(node)
            } else if ((attributeIndex != null) && node is Attribute) {
                removeFromAttributeIndex(node)
            }

            return true
        }

        return false
    }

    protected fun attributeIndex(): MutableMap<*, *>? {
        if (attributeIndex == null) {
            attributeIndex = createAttributeIndex()

            val iter = attributeIterator()
            while (iter != null && iter.hasNext()) {
                addToAttributeIndex((iter.next() as com.wxiwei.office.fc.dom4j.Attribute?)!!)
            }
        }

        return attributeIndex
    }

    protected fun elementIndex(): MutableMap<*, *>? {
        if (elementIndex == null) {
            elementIndex = createElementIndex()

            val iter = elementIterator()
            while (iter != null && iter.hasNext()) {
                addToElementIndex((iter.next() as com.wxiwei.office.fc.dom4j.Element?)!!)
            }
        }

        return elementIndex
    }

    /**
     * A Factory Method to create the index for attributes
     * 
     * @return DOCUMENT ME!
     */
    protected fun createAttributeIndex(): MutableMap<*, *> {
        val answer = createIndex()

        return answer
    }

    /**
     * A Factory Method to create the index for elements
     * 
     * @return DOCUMENT ME!
     */
    protected fun createElementIndex(): MutableMap<*, *> {
        val answer = createIndex()

        return answer
    }

    protected fun addToElementIndex(element: Element) {
        val qName = element.getQName()
        val name = qName?.getName() ?: element.getName()
        addToElementIndex(qName, element)
        addToElementIndex(name, element)
    }

    protected fun addToElementIndex(key: Any?, value: Element?) {
        val map = elementIndex as MutableMap<Any?, Any?>? ?: return
        val oldValue = map.get(key)

        if (oldValue == null) {
            map.put(key, value)
        } else {
            if (oldValue is MutableList<*>) {
                val list = oldValue as MutableList<Any?>
                list.add(value)
            } else {
                val list = createList() as MutableList<Any?>
                list.add(oldValue)
                list.add(value)
                map.put(key, list)
            }
        }
    }

    protected fun removeFromElementIndex(element: Element) {
        val qName = element.getQName()
        val name = qName?.getName() ?: element.getName()
        removeFromElementIndex(qName, element)
        removeFromElementIndex(name, element)
    }

    protected fun removeFromElementIndex(key: Any?, value: Element?) {
        val oldValue = elementIndex!!.get(key)

        if (oldValue is MutableList<*>) {
            val list = oldValue as MutableList<Any?>
            list.remove(value)
        } else {
            elementIndex!!.remove(key)
        }
    }

    protected fun addToAttributeIndex(attribute: Attribute) {
        val qName = attribute.getQName()
        val name = qName?.getName() ?: attribute.getName()
        addToAttributeIndex(qName, attribute)
        addToAttributeIndex(name, attribute)
    }

    protected fun addToAttributeIndex(key: Any?, value: Attribute?) {
        val map = attributeIndex as MutableMap<Any?, Any?>? ?: return
        val oldValue = map.get(key)

        if (oldValue != null) {
            map.put(key, value)
        }
    }

    protected fun removeFromAttributeIndex(attribute: Attribute) {
        val qName = attribute.getQName()
        val name = qName?.getName() ?: attribute.getName()
        removeFromAttributeIndex(qName, attribute)
        removeFromAttributeIndex(name, attribute)
    }

    protected fun removeFromAttributeIndex(key: Any?, value: Attribute?) {
        val oldValue = attributeIndex!!.get(key)

        if ((oldValue != null) && oldValue == value) {
            attributeIndex!!.remove(key)
        }
    }

    /**
     * Factory method to return a new map implementation for indices
     * 
     * @return DOCUMENT ME!
     */
    protected fun createIndex(): MutableMap<*, *> {
        return HashMap<Any?, Any?>()
    }

    /**
     * Factory method to return a list implementation for indices
     * 
     * @return DOCUMENT ME!
     */
    protected fun createList(): MutableList<*> {
        return ArrayList<Any?>()
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


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
package com.wxiwei.office.fc.dom4j.dom

import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.CharacterData
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node
import org.w3c.dom.Attr
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.DocumentType
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.NodeList
import org.w3c.dom.Text

/**
 * 
 * 
 * `DOMNodeHelper` contains a collection of utility methods for use
 * across Node implementations.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.20 $
 */
object DOMNodeHelper {
    val EMPTY_NODE_LIST: NodeList = EmptyNodeList()

    // Node API
    // -------------------------------------------------------------------------
    fun supports(node: Node?, feature: String?, version: String?): Boolean {
        return false
    }

    fun getNamespaceURI(node: Node?): String? {
        return null
    }

    fun getPrefix(node: Node?): String? {
        return null
    }

    fun getLocalName(node: Node?): String? {
        return null
    }

    @Throws(DOMException::class)
    fun setPrefix(node: Node?, prefix: String?) {
        notSupported()
    }

    @Throws(DOMException::class)
    fun getNodeValue(node: Node): String? {
        return node.getText()
    }

    @Throws(DOMException::class)
    fun setNodeValue(node: Node, nodeValue: String?) {
        node.setText(nodeValue)
    }

    fun getParentNode(node: Node): org.w3c.dom.Node? {
        return asDOMNode(node.getParent())
    }

    fun getChildNodes(node: Node?): NodeList {
        return EMPTY_NODE_LIST
    }

    fun getFirstChild(node: Node?): org.w3c.dom.Node? {
        return null
    }

    fun getLastChild(node: Node?): org.w3c.dom.Node? {
        return null
    }

    fun getPreviousSibling(node: Node): org.w3c.dom.Node? {
        val parent = node.getParent()

        if (parent != null) {
            val index = parent.indexOf(node)

            if (index > 0) {
                val previous = parent.node(index - 1)

                return asDOMNode(previous)
            }
        }

        return null
    }

    fun getNextSibling(node: Node): org.w3c.dom.Node? {
        val parent = node.getParent()

        if (parent != null) {
            var index = parent.indexOf(node)

            if (index >= 0) {
                if (++index < parent.nodeCount()) {
                    val next = parent.node(index)

                    return asDOMNode(next)
                }
            }
        }

        return null
    }

    fun getAttributes(node: Node?): NamedNodeMap? {
        return null
    }

    fun getOwnerDocument(node: Node): Document? {
        return asDOMDocument(node.getDocument())
    }

    @Throws(DOMException::class)
    fun insertBefore(
        node: Node?, newChild: org.w3c.dom.Node?,
        refChild: org.w3c.dom.Node?
    ): org.w3c.dom.Node? {
        if (node is Branch) {
            val branch = node
            val list = branch.content() as MutableList<Any?>?
            val index = list?.indexOf(refChild) ?: -1

            if (index < 0) {
                branch.add(newChild as Node?)
            } else {
                list?.add(index, newChild)
            }

            return newChild
        } else {
            throw DOMException(
                DOMException.HIERARCHY_REQUEST_ERR,
                "Children not allowed for this node: " + node
            )
        }
    }

    @Throws(DOMException::class)
    fun replaceChild(
        node: Node?, newChild: org.w3c.dom.Node?,
        oldChild: org.w3c.dom.Node?
    ): org.w3c.dom.Node? {
        if (node is Branch) {
            val branch = node
            val list = branch.content() as MutableList<Any?>?
            val index = list?.indexOf(oldChild) ?: -1

            if (index < 0) {
                throw DOMException(
                    DOMException.NOT_FOUND_ERR,
                    "Tried to replace a non existing child " + "for node: " + node
                )
            }

            list?.set(index, newChild)

            return oldChild
        } else {
            throw DOMException(
                DOMException.HIERARCHY_REQUEST_ERR,
                "Children not allowed for this node: " + node
            )
        }
    }

    @Throws(DOMException::class)
    fun removeChild(node: Node?, oldChild: org.w3c.dom.Node?): org.w3c.dom.Node? {
        if (node is Branch) {
            val branch = node
            branch.remove(oldChild as Node?)

            return oldChild
        }

        throw DOMException(
            DOMException.HIERARCHY_REQUEST_ERR,
            "Children not allowed for this node: " + node
        )
    }

    @Throws(DOMException::class)
    fun appendChild(node: Node?, newChild: org.w3c.dom.Node?): org.w3c.dom.Node? {
        if (newChild == null) return null
        if (node is Branch) {
            val branch = node
            val previousParent = newChild.getParentNode()

            if (previousParent != null) {
                previousParent.removeChild(newChild)
            }

            branch.add(newChild as Node)

            return newChild
        }

        throw DOMException(
            DOMException.HIERARCHY_REQUEST_ERR,
            "Children not allowed for this node: " + node
        )
    }

    fun hasChildNodes(node: Node?): Boolean {
        return false
    }

    fun cloneNode(node: Node, deep: Boolean): org.w3c.dom.Node? {
        return asDOMNode(node.clone() as Node)
    }

    fun normalize(node: Node?) {
        notSupported()
    }

    fun isSupported(n: Node?, feature: String?, version: String?): Boolean {
        return false
    }

    fun hasAttributes(node: Node?): Boolean {
        if ((node != null) && node is Element) {
            return node.attributeCount() > 0
        } else {
            return false
        }
    }

    // CharacterData API
    // -------------------------------------------------------------------------
    @Throws(DOMException::class)
    fun getData(charData: CharacterData): String? {
        return charData.getText()
    }

    @Throws(DOMException::class)
    fun setData(charData: CharacterData, data: String?) {
        charData.setText(data)
    }

    fun getLength(charData: CharacterData): Int {
        val text = charData.getText()

        return if (text != null) text.length else 0
    }

    @Throws(DOMException::class)
    fun substringData(charData: CharacterData, offset: Int, count: Int): String {
        if (count < 0) {
            throw DOMException(DOMException.INDEX_SIZE_ERR, "Illegal value for count: " + count)
        }

        val text = charData.getText()
        val length = if (text != null) text.length else 0

        if ((offset < 0) || (offset >= length)) {
            throw DOMException(DOMException.INDEX_SIZE_ERR, "No text at offset: " + offset)
        }

        if ((offset + count) > length) {
            return text!!.substring(offset)
        }

        return text!!.substring(offset, offset + count)
    }

    @Throws(DOMException::class)
    fun appendData(charData: CharacterData, arg: String?) {
        if (charData.isReadOnly()) {
            throw DOMException(
                DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "CharacterData node is read only: " + charData
            )
        } else {
            val text = charData.getText()

            if (text == null) {
                charData.setText(text)
            } else {
                charData.setText(text + arg)
            }
        }
    }

    @Throws(DOMException::class)
    fun insertData(data: CharacterData, offset: Int, arg: String?) {
        if (data.isReadOnly()) {
            throw DOMException(
                DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "CharacterData node is read only: " + data
            )
        } else {
            val text = data.getText()

            if (text == null) {
                data.setText(arg)
            } else {
                val length = text.length

                if ((offset < 0) || (offset > length)) {
                    throw DOMException(
                        DOMException.INDEX_SIZE_ERR, "No text at offset: "
                                + offset
                    )
                } else {
                    val buffer = StringBuffer(text)
                    buffer.insert(offset, arg)
                    data.setText(buffer.toString())
                }
            }
        }
    }

    @Throws(DOMException::class)
    fun deleteData(charData: CharacterData, offset: Int, count: Int) {
        if (charData.isReadOnly()) {
            throw DOMException(
                DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "CharacterData node is read only: " + charData
            )
        } else {
            if (count < 0) {
                throw DOMException(
                    DOMException.INDEX_SIZE_ERR, "Illegal value for count: "
                            + count
                )
            }

            val text = charData.getText()

            if (text != null) {
                val length = text.length

                if ((offset < 0) || (offset >= length)) {
                    throw DOMException(
                        DOMException.INDEX_SIZE_ERR, "No text at offset: "
                                + offset
                    )
                } else {
                    val buffer = StringBuffer(text)
                    buffer.delete(offset, offset + count)
                    charData.setText(buffer.toString())
                }
            }
        }
    }

    @Throws(DOMException::class)
    fun replaceData(charData: CharacterData, offset: Int, count: Int, arg: String) {
        if (charData.isReadOnly()) {
            throw DOMException(
                DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "CharacterData node is read only: " + charData
            )
        } else {
            if (count < 0) {
                throw DOMException(
                    DOMException.INDEX_SIZE_ERR, "Illegal value for count: "
                            + count
                )
            }

            val text = charData.getText()

            if (text != null) {
                val length = text.length

                if ((offset < 0) || (offset >= length)) {
                    throw DOMException(
                        DOMException.INDEX_SIZE_ERR, "No text at offset: "
                                + offset
                    )
                } else {
                    val buffer = StringBuffer(text)
                    buffer.replace(offset, offset + count, arg)
                    charData.setText(buffer.toString())
                }
            }
        }
    }

    // Branch API
    // -------------------------------------------------------------------------
    fun appendElementsByTagName(list: MutableList<*>, parent: Branch, name: String) {
        val isStar = "*" == name

        var i = 0
        val size = parent.nodeCount()
        while (i < size) {
            val node = parent.node(i)

            if (node is Element) {
                val element = node

                if (isStar || name == element.getName()) {
                    (list as MutableList<Any?>).add(element)
                }

                appendElementsByTagName(list, element, name)
            }
            i++
        }
    }

    fun appendElementsByTagNameNS(
        list: MutableList<*>, parent: Branch, namespace: String?,
        localName: String
    ) {
        val isStarNS = "*" == namespace
        val isStar = "*" == localName

        var i = 0
        val size = parent.nodeCount()
        while (i < size) {
            val node = parent.node(i)

            if (node is Element) {
                val element = node
                val nsUri = element.getNamespaceURI()

                if ((isStarNS
                            || (((namespace == null) || (namespace.length == 0)) && ((nsUri == null) || (nsUri.length == 0))) || ((namespace != null) && (namespace
                            == nsUri)))
                    && (isStar || localName == element.getName())
                ) {
                    (list as MutableList<Any?>).add(element)
                }

                appendElementsByTagNameNS(list, element, namespace, localName)
            }
            i++
        }
    }

    // Helper methods
    // -------------------------------------------------------------------------
    fun createNodeList(list: List<*>?): NodeList {
        return object : NodeList {
            override fun item(index: Int): org.w3c.dom.Node? {
                if (list == null || index >= getLength()) {
                    /*
                     * From the NodeList specification: If index is greater than
                     * or equal to the number of nodes in the list, this returns
                     * null.
                     */
                    return null
                } else {
                    return asDOMNode(list.get(index) as Node?)
                }
            }

            override fun getLength(): Int {
                return list?.size ?: 0
            }
        }
    }

    fun asDOMNode(node: Node?): org.w3c.dom.Node? {
        if (node == null) {
            return null
        }

        if (node is org.w3c.dom.Node) {
            return node as org.w3c.dom.Node
        } else {
            // Use DOMWriter?
            notSupported()

            return null
        }
    }

    fun asDOMDocument(document: com.wxiwei.office.fc.dom4j.Document?): Document? {
        if (document == null) {
            return null
        }

        if (document is Document) {
            return document as Document
        } else {
            // Use DOMWriter?
            notSupported()

            return null
        }
    }

    fun asDOMDocumentType(dt: com.wxiwei.office.fc.dom4j.DocumentType?): DocumentType? {
        if (dt == null) {
            return null
        }

        if (dt is DocumentType) {
            return dt as DocumentType
        } else {
            // Use DOMWriter?
            notSupported()

            return null
        }
    }

    fun asDOMText(text: CharacterData?): Text? {
        if (text == null) {
            return null
        }

        if (text is Text) {
            return text as Text
        } else {
            // Use DOMWriter?
            notSupported()

            return null
        }
    }

    fun asDOMElement(element: Node?): org.w3c.dom.Element? {
        if (element == null) {
            return null
        }

        if (element is org.w3c.dom.Element) {
            return element as org.w3c.dom.Element
        } else {
            // Use DOMWriter?
            notSupported()

            return null
        }
    }

    fun asDOMAttr(attribute: Node?): Attr? {
        if (attribute == null) {
            return null
        }

        if (attribute is Attr) {
            return attribute as Attr
        } else {
            // Use DOMWriter?
            notSupported()

            return null
        }
    }

    /**
     * Called when a method has not been implemented yet
     * 
     * @throws DOMException
     * DOCUMENT ME!
     */
    fun notSupported() {
        throw DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported yet")
    }

    class EmptyNodeList : NodeList {
        override fun item(index: Int): org.w3c.dom.Node? {
            return null
        }

        override fun getLength(): Int {
            return 0
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


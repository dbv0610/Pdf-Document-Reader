/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.dom

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.tree.DefaultEntity
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.EntityReference
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.UserDataHandler

/**
 * 
 * 
 * `DOMEntity` implements a Entity node which supports the W3C DOM
 * API.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.12 $
 */
class DOMEntityReference : DefaultEntity, EntityReference {
    constructor(name: String?) : super(name)

    constructor(name: String?, text: String?) : super(name, text)

    constructor(parent: Element?, name: String?, text: String?) : super(parent, name, text)

    // org.w3c.dom.Node interface
    // -------------------------------------------------------------------------
    fun supports(feature: String?, version: String?): Boolean {
        return DOMNodeHelper.supports(this, feature, version)
    }

    override fun getNamespaceURI(): String? {
        return DOMNodeHelper.getNamespaceURI(this)
    }

    override fun getPrefix(): String? {
        return DOMNodeHelper.getPrefix(this)
    }

    @Throws(DOMException::class)
    override fun setPrefix(prefix: String?) {
        DOMNodeHelper.setPrefix(this, prefix)
    }

    override fun getLocalName(): String? {
        return DOMNodeHelper.getLocalName(this)
    }

    override fun getNodeName(): String? {
        return getName()
    }

    // already part of API
    //
    // public short getNodeType();
    @Throws(DOMException::class)
    override fun getNodeValue(): String? {
        return null
    }

    @Throws(DOMException::class)
    override fun setNodeValue(nodeValue: String?) {
    }

    override fun getParentNode(): Node? {
        return DOMNodeHelper.getParentNode(this)
    }

    override fun getChildNodes(): NodeList? {
        return DOMNodeHelper.getChildNodes(this)
    }

    override fun getFirstChild(): Node? {
        return DOMNodeHelper.getFirstChild(this)
    }

    override fun getLastChild(): Node? {
        return DOMNodeHelper.getLastChild(this)
    }

    override fun getPreviousSibling(): Node? {
        return DOMNodeHelper.getPreviousSibling(this)
    }

    override fun getNextSibling(): Node? {
        return DOMNodeHelper.getNextSibling(this)
    }

    override fun getAttributes(): NamedNodeMap? {
        return null
    }

    override fun getOwnerDocument(): Document? {
        return DOMNodeHelper.getOwnerDocument(this)
    }

    @Throws(DOMException::class)
    override fun insertBefore(newChild: Node, refChild: Node?): Node? {
        checkNewChildNode(newChild)

        return DOMNodeHelper.insertBefore(this, newChild, refChild)
    }

    @Throws(DOMException::class)
    override fun replaceChild(newChild: Node, oldChild: Node?): Node? {
        checkNewChildNode(newChild)

        return DOMNodeHelper.replaceChild(this, newChild, oldChild)
    }

    @Throws(DOMException::class)
    override fun removeChild(oldChild: Node?): Node? {
        return DOMNodeHelper.removeChild(this, oldChild)
    }

    @Throws(DOMException::class)
    override fun appendChild(newChild: Node): Node {
        checkNewChildNode(newChild)

        return DOMNodeHelper.appendChild(this, newChild)!!
    }

    @Throws(DOMException::class)
    private fun checkNewChildNode(newChild: Node) {
        val nodeType = newChild.getNodeType().toInt()

        if (!((nodeType == Node.ELEMENT_NODE.toInt())
                    || (nodeType == Node.TEXT_NODE.toInt())
                    || (nodeType == Node.COMMENT_NODE.toInt())
                    || (nodeType == Node.PROCESSING_INSTRUCTION_NODE.toInt())
                    || (nodeType == Node.CDATA_SECTION_NODE.toInt()) || (nodeType == Node.ENTITY_REFERENCE_NODE.toInt()))
        ) {
            throw DOMException(
                DOMException.HIERARCHY_REQUEST_ERR,
                "Given node cannot be a child of an entity " + "reference"
            )
        }
    }

    override fun hasChildNodes(): Boolean {
        return DOMNodeHelper.hasChildNodes(this)
    }

    override fun cloneNode(deep: Boolean): Node? {
        return DOMNodeHelper.cloneNode(this, deep)
    }

    override fun normalize() {
        DOMNodeHelper.normalize(this)
    }

    override fun isSupported(feature: String?, version: String?): Boolean {
        return DOMNodeHelper.isSupported(this, feature, version)
    }

    override fun hasAttributes(): Boolean {
        return DOMNodeHelper.hasAttributes(this)
    }

    override fun getBaseURI(): String? {
     
        return null
    }

    @Throws(DOMException::class)
    override fun compareDocumentPosition(other: Node?): Short {
     
        return 0
    }

    @Throws(DOMException::class)
    override fun getTextContent(): String? {
     
        return null
    }

    @Throws(DOMException::class)
    override fun setTextContent(textContent: String?) {
     
    }

    override fun isSameNode(other: Node?): Boolean {
     
        return false
    }

    override fun lookupPrefix(namespaceURI: String?): String? {
     
        return null
    }

    override fun isDefaultNamespace(namespaceURI: String?): Boolean {
     
        return false
    }

    override fun lookupNamespaceURI(prefix: String?): String? {
     
        return null
    }

    override fun isEqualNode(arg: Node?): Boolean {
     
        return false
    }

    override fun getFeature(feature: String?, version: String?): Any? {
     
        return null
    }

    override fun setUserData(key: String?, data: Any?, handler: UserDataHandler?): Any? {
     
        return null
    }

    override fun getUserData(key: String?): Any? {
     
        return null
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


/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.dom

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.tree.DefaultElement
import org.w3c.dom.Attr
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.TypeInfo
import org.w3c.dom.UserDataHandler

/**
 * 
 * 
 * `DOMElement` implements an XML element which supports the W3C
 * DOM API.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.23 $
 */
class DOMElement : DefaultElement, Element {
    constructor(name: String?) : super(name)

    constructor(qname: QName?) : super(qname)

    constructor(qname: QName?, attributeCount: Int) : super(qname, attributeCount)

    constructor(name: String?, namespace: Namespace?) : super(name, namespace)

    // org.w3c.dom.Node interface
    // -------------------------------------------------------------------------
    fun supports(feature: String?, version: String?): Boolean {
        return DOMNodeHelper.supports(this, feature, version)
    }

    override fun getNamespaceURI(): String? {
        return getQName()?.getNamespaceURI()
    }

    override fun getPrefix(): String? {
        return getQName()?.getNamespacePrefix()
    }

    @Throws(DOMException::class)
    override fun setPrefix(prefix: String?) {
        DOMNodeHelper.setPrefix(this, prefix)
    }

    override fun getLocalName(): String? {
        return getQName()?.getName()
    }

    override fun getNodeName(): String? {
        return getName()
    }

    override fun getNodeType(): Short {
        return Node.ELEMENT_NODE
    }
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

    override fun getChildNodes(): NodeList {
        return DOMNodeHelper.createNodeList(content())
    }

    override fun getFirstChild(): Node? {
        return DOMNodeHelper.asDOMNode(node(0))
    }

    override fun getLastChild(): Node? {
        return DOMNodeHelper.asDOMNode(node(nodeCount() - 1))
    }

    override fun getPreviousSibling(): Node? {
        return DOMNodeHelper.getPreviousSibling(this)
    }

    override fun getNextSibling(): Node? {
        return DOMNodeHelper.getNextSibling(this)
    }

    override fun getAttributes(): NamedNodeMap {
        return DOMAttributeNodeMap(this)
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

        if (!((nodeType == Node.ELEMENT_NODE.toInt()) || (nodeType == Node.TEXT_NODE.toInt())
                    || (nodeType == Node.COMMENT_NODE.toInt()) || (nodeType == Node.PROCESSING_INSTRUCTION_NODE.toInt())
                    || (nodeType == Node.CDATA_SECTION_NODE.toInt()) || (nodeType == Node.ENTITY_REFERENCE_NODE.toInt()))
        ) {
            throw DOMException(
                DOMException.HIERARCHY_REQUEST_ERR,
                "Given node cannot be a child of element"
            )
        }
    }

    override fun hasChildNodes(): Boolean {
        return nodeCount() > 0
    }

    override fun cloneNode(deep: Boolean): Node? {
        return DOMNodeHelper.cloneNode(this, deep)
    }

    override fun isSupported(feature: String?, version: String?): Boolean {
        return DOMNodeHelper.isSupported(this, feature, version)
    }

    override fun hasAttributes(): Boolean {
        return DOMNodeHelper.hasAttributes(this)
    }

    // org.w3c.dom.Element interface
    // -------------------------------------------------------------------------
    override fun getTagName(): String? {
        return getName()
    }

    override fun getAttribute(name: String?): String {
        val answer = attributeValue(name)

        return if (answer != null) answer else ""
    }

    @Throws(DOMException::class)
    override fun setAttribute(name: String?, value: String?) {
        addAttribute(name, value)
    }

    @Throws(DOMException::class)
    override fun removeAttribute(name: String?) {
        val attribute = attribute(name)

        if (attribute != null) {
            remove(attribute)
        }
    }

    override fun getAttributeNode(name: String?): Attr? {
        return DOMNodeHelper.asDOMAttr(attribute(name))
    }

    @Throws(DOMException::class)
    override fun setAttributeNode(newAttr: Attr): Attr? {
        if (this.isReadOnly()) {
            throw DOMException(
                DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "No modification allowed"
            )
        }

        val attribute = attribute(newAttr)

        if (attribute !== newAttr) {
            if (newAttr.getOwnerElement() != null) {
                throw DOMException(
                    DOMException.INUSE_ATTRIBUTE_ERR,
                    "Attribute is already in use"
                )
            }

            val newAttribute = createAttribute(newAttr)

            if (attribute != null) {
                attribute.detach()
            }

            add(newAttribute)
        }

        return DOMNodeHelper.asDOMAttr(attribute)
    }

    @Throws(DOMException::class)
    override fun removeAttributeNode(oldAttr: Attr): Attr? {
        val attribute = attribute(oldAttr)

        if (attribute != null) {
            attribute.detach()

            return DOMNodeHelper.asDOMAttr(attribute)
        } else {
            throw DOMException(DOMException.NOT_FOUND_ERR, "No such attribute")
        }
    }

    override fun getAttributeNS(namespaceURI: String?, localName: String): String {
        val attribute = attribute(namespaceURI, localName)

        if (attribute != null) {
            val answer = attribute.getValue()

            if (answer != null) {
                return answer
            }
        }

        return ""
    }

    @Throws(DOMException::class)
    override fun setAttributeNS(namespaceURI: String?, qualifiedName: String, value: String?) {
        val attribute = attribute(namespaceURI, qualifiedName)

        if (attribute != null) {
            attribute.setValue(value)
        } else {
            val qname = getQName(namespaceURI, qualifiedName)
            addAttribute(qname, value)
        }
    }

    @Throws(DOMException::class)
    override fun removeAttributeNS(namespaceURI: String?, localName: String) {
        val attribute = attribute(namespaceURI, localName)

        if (attribute != null) {
            remove(attribute)
        }
    }

    override fun getAttributeNodeNS(namespaceURI: String?, localName: String): Attr? {
        val attribute = attribute(namespaceURI, localName)

        if (attribute != null) {
            DOMNodeHelper.asDOMAttr(attribute)
        }

        return null
    }

    @Throws(DOMException::class)
    override fun setAttributeNodeNS(newAttr: Attr): Attr? {
        var attribute = attribute(newAttr.getNamespaceURI(), newAttr.getLocalName())

        if (attribute != null) {
            attribute.setValue(newAttr.getValue())
        } else {
            attribute = createAttribute(newAttr)
            add(attribute)
        }

        return DOMNodeHelper.asDOMAttr(attribute)
    }

    override fun getElementsByTagName(name: String?): NodeList {
        val list: ArrayList<Any?> = ArrayList<Any?>()
        DOMNodeHelper.appendElementsByTagName(list, this, name ?: "")

        return DOMNodeHelper.createNodeList(list)
    }

    override fun getElementsByTagNameNS(namespace: String?, lName: String?): NodeList {
        val list: ArrayList<Any?> = ArrayList<Any?>()
        DOMNodeHelper.appendElementsByTagNameNS(list, this, namespace, lName ?: "")

        return DOMNodeHelper.createNodeList(list)
    }

    override fun hasAttribute(name: String?): Boolean {
        return attribute(name) != null
    }

    override fun hasAttributeNS(namespaceURI: String?, localName: String): Boolean {
        return attribute(namespaceURI, localName) != null
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    override fun getDocumentFactory(): DocumentFactory {
        val factory = getQName()?.getDocumentFactory()

        return factory ?: DOCUMENT_FACTORY
    }

    protected fun attribute(attr: Attr): Attribute? {
        return attribute(
            DOCUMENT_FACTORY.createQName(
                attr.getLocalName(), attr.getPrefix(),
                attr.getNamespaceURI()
            )
        )
    }

    protected fun attribute(namespaceURI: String?, localName: String): Attribute? {
        val attributes = attributeList()
        val size = attributes.size

        for (i in 0..<size) {
            val attribute = attributes.get(i) as Attribute
            val nsUri = attribute.getNamespaceURI()

            if (localName == attribute.getName()
                && (((namespaceURI == null || namespaceURI.length == 0) && ((nsUri == null) || (nsUri.length == 0))) || ((namespaceURI != null) && (namespaceURI
                        == nsUri)))
            ) {
                return attribute
            }
        }

        return null
    }

    protected fun createAttribute(newAttr: Attr): Attribute {
        var qname: QName? = null
        var name = newAttr.getLocalName()

        if (name != null) {
            val prefix = newAttr.getPrefix()
            val uri = newAttr.getNamespaceURI()
            qname = getDocumentFactory()!!.createQName(name, prefix, uri)
        } else {
            name = newAttr.getName()
            qname = getDocumentFactory()!!.createQName(name)
        }

        return DOMAttribute(qname, newAttr.getValue())
    }

    protected fun getQName(namespace: String?, qualifiedName: String): QName? {
        val index = qualifiedName.indexOf(':')
        var prefix = ""
        var localName = qualifiedName

        if (index >= 0) {
            prefix = qualifiedName.substring(0, index)
            localName = qualifiedName.substring(index + 1)
        }

        return getDocumentFactory()!!.createQName(localName, prefix, namespace)
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

    override fun getSchemaTypeInfo(): TypeInfo? {
     
        return null
    }

    @Throws(DOMException::class)
    override fun setIdAttribute(name: String?, isId: Boolean) {
     
    }

    @Throws(DOMException::class)
    override fun setIdAttributeNS(namespaceURI: String?, localName: String?, isId: Boolean) {
     
    }

    @Throws(DOMException::class)
    override fun setIdAttributeNode(idAttr: Attr?, isId: Boolean) {
     
    }

    companion object {
        /** The `DocumentFactory` instance used by default  */
        private val DOCUMENT_FACTORY: DocumentFactory = DOMDocumentFactory.Companion.getInstance()
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


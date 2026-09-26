/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.dom

import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.tree.DefaultDocument
import org.w3c.dom.Attr
import org.w3c.dom.CDATASection
import org.w3c.dom.Comment
import org.w3c.dom.DOMConfiguration
import org.w3c.dom.DOMException
import org.w3c.dom.DOMImplementation
import org.w3c.dom.Document
import org.w3c.dom.DocumentFragment
import org.w3c.dom.DocumentType
import org.w3c.dom.Element
import org.w3c.dom.EntityReference
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.ProcessingInstruction
import org.w3c.dom.Text
import org.w3c.dom.UserDataHandler

/**
 * 
 * 
 * `DOMDocument` implements an XML document which supports the W3C
 * DOM API.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.17 $
 */
class DOMDocument : DefaultDocument, Document {
    constructor() {
        init()
    }

    constructor(name: String?) : super(name) {
        init()
    }

    constructor(rootElement: DOMElement?) : super(rootElement) {
        init()
    }

    constructor(docType: DOMDocumentType?) : super(docType) {
        init()
    }

    constructor(rootElement: DOMElement?, docType: DOMDocumentType?) : super(rootElement, docType) {
        init()
    }

    constructor(name: String?, rootElement: DOMElement?, docType: DOMDocumentType?) : super(
        name,
        rootElement,
        docType
    ) {
        init()
    }

    private fun init() {
        setDocumentFactory(DOCUMENT_FACTORY)
    }

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

    override fun getNodeName(): String {
        return "#document"
    }

    // already part of API
    //
    override fun getNodeType(): Short {
        return Node.DOCUMENT_NODE
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

    override fun getAttributes(): NamedNodeMap? {
        return null
    }

    override fun getOwnerDocument(): Document? {
        return null
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
                    || (nodeType == Node.COMMENT_NODE.toInt())
                    || (nodeType == Node.PROCESSING_INSTRUCTION_NODE.toInt()) || (nodeType == Node.DOCUMENT_TYPE_NODE.toInt()))
        ) {
            throw DOMException(
                DOMException.HIERARCHY_REQUEST_ERR,
                "Given node cannot be a child of document"
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

    // org.w3c.dom.Document interface
    // -------------------------------------------------------------------------
    override fun getElementsByTagName(name: String?): NodeList {
        val list: ArrayList<Any?> = ArrayList<Any?>()
        DOMNodeHelper.appendElementsByTagName(list, this, name ?: "")

        return DOMNodeHelper.createNodeList(list)
    }

    override fun getElementsByTagNameNS(namespace: String?, name: String?): NodeList {
        val list: ArrayList<Any?> = ArrayList<Any?>()
        DOMNodeHelper.appendElementsByTagNameNS(list, this, namespace, name ?: "")

        return DOMNodeHelper.createNodeList(list)
    }

    override fun getDoctype(): DocumentType? {
        return DOMNodeHelper.asDOMDocumentType(getDocType())
    }

    override fun getImplementation(): DOMImplementation? {
        if (getDocumentFactory() is DOMImplementation) {
            return getDocumentFactory() as DOMImplementation?
        } else {
            return DOCUMENT_FACTORY
        }
    }

    override fun getDocumentElement(): Element? {
        return DOMNodeHelper.asDOMElement(getRootElement())
    }

    @Throws(DOMException::class)
    override fun createElement(name: String?): Element? {
        return getDocumentFactory()!!.createElement(name) as Element?
    }

    override fun createDocumentFragment(): DocumentFragment? {
        DOMNodeHelper.notSupported()

        return null
    }

    override fun createTextNode(data: String?): Text? {
        return getDocumentFactory()!!.createText(data) as Text?
    }

    override fun createComment(data: String?): Comment? {
        return getDocumentFactory()!!.createComment(data) as Comment?
    }

    @Throws(DOMException::class)
    override fun createCDATASection(data: String?): CDATASection? {
        return getDocumentFactory()!!.createCDATA(data) as CDATASection?
    }

    @Throws(DOMException::class)
    override fun createProcessingInstruction(
        target: String?,
        data: String?
    ): ProcessingInstruction? {
        return getDocumentFactory()!!
            .createProcessingInstruction(target, data) as ProcessingInstruction?
    }

    @Throws(DOMException::class)
    override fun createAttribute(name: String?): Attr? {
        val qname = getDocumentFactory()!!.createQName(name)

        return getDocumentFactory()!!.createAttribute(null, qname, "") as Attr?
    }

    @Throws(DOMException::class)
    override fun createEntityReference(name: String?): EntityReference? {
        return getDocumentFactory()!!.createEntity(name, null) as EntityReference?
    }

    @Throws(DOMException::class)
    override fun importNode(importedNode: Node?, deep: Boolean): Node? {
        DOMNodeHelper.notSupported()

        return null
    }

    @Throws(DOMException::class)
    override fun createElementNS(namespaceURI: String?, qualifiedName: String?): Element? {
        val qname = getDocumentFactory()!!.createQName(qualifiedName, namespaceURI)

        return getDocumentFactory()!!.createElement(qname) as Element?
    }

    @Throws(DOMException::class)
    override fun createAttributeNS(namespaceURI: String?, qualifiedName: String?): Attr? {
        val qname = getDocumentFactory()!!.createQName(qualifiedName, namespaceURI)

        return getDocumentFactory()!!.createAttribute(null, qname, null) as Attr?
    }

    override fun getElementById(elementId: String?): Element? {
        return DOMNodeHelper.asDOMElement(elementByID(elementId))
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    override fun getDocumentFactory(): DocumentFactory {
        val factory = super.getDocumentFactory()
        if (factory == null) {
            return DOCUMENT_FACTORY
        } else {
            return factory
        }
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

    override fun getInputEncoding(): String? {
     
        return null
    }

    override fun getXmlEncoding(): String? {
     
        return null
    }

    override fun getXmlStandalone(): Boolean {
     
        return false
    }

    @Throws(DOMException::class)
    override fun setXmlStandalone(xmlStandalone: Boolean) {
     
    }

    override fun getXmlVersion(): String? {
     
        return null
    }

    @Throws(DOMException::class)
    override fun setXmlVersion(xmlVersion: String?) {
     
    }

    override fun getStrictErrorChecking(): Boolean {
     
        return false
    }

    override fun setStrictErrorChecking(strictErrorChecking: Boolean) {
     
    }

    override fun getDocumentURI(): String? {
     
        return null
    }

    override fun setDocumentURI(documentURI: String?) {
     
    }

    @Throws(DOMException::class)
    override fun adoptNode(source: Node?): Node? {
     
        return null
    }

    override fun getDomConfig(): DOMConfiguration? {
     
        return null
    }

    override fun normalizeDocument() {
     
    }

    @Throws(DOMException::class)
    override fun renameNode(n: Node?, namespaceURI: String?, qualifiedName: String?): Node? {
     
        return null
    }

    companion object {
        /** The `DocumentFactory` instance used by default  */
        private val DOCUMENT_FACTORY =
            DOMDocumentFactory.getInstance() as DOMDocumentFactory
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


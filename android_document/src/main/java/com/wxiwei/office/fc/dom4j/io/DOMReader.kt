/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.tree.NamespaceStack
import org.w3c.dom.DocumentType
import org.w3c.dom.Node

/**
 * 
 * 
 * `DOMReader` navigates a W3C DOM tree and creates a DOM4J tree
 * from it.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.17 $
 */
class DOMReader {
    /** `DocumentFactory` used to create new document objects  */
    private var factory: DocumentFactory?

    /** stack of `Namespace` and `QName` objects  */
    private val namespaceStack: NamespaceStack

    constructor() {
        this.factory = DocumentFactory.Companion.getInstance()
        this.namespaceStack = NamespaceStack(factory!!)
    }

    constructor(factory: DocumentFactory?) {
        this.factory = factory
        this.namespaceStack = NamespaceStack(factory!!)
    }

    var documentFactory: DocumentFactory?
        /**
         * DOCUMENT ME!
         * 
         * @return the `DocumentFactory` used to create document
         * objects
         */
        get() = factory
        /**
         * 
         * 
         * This sets the `DocumentFactory` used to create new
         * documents. This method allows the building of custom DOM4J tree objects
         * to be implemented easily using a custom derivation of
         * [DocumentFactory]
         * 
         * 
         * @param docFactory
         * `DocumentFactory` used to create DOM4J objects
         */
        set(docFactory) {
            this.factory = docFactory
            this.namespaceStack.documentFactory = factory!!
        }

    fun read(domDocument: org.w3c.dom.Document): Document? {
        if (domDocument is Document) {
            return domDocument as Document
        }

        val document = createDocument()

        clearNamespaceStack()

        val nodeList = domDocument.getChildNodes()

        var i = 0
        val size = nodeList.getLength()
        while (i < size) {
            readTree(nodeList.item(i), document)
            i++
        }

        return document
    }

    // Implementation methods
    protected fun readTree(node: Node, current: Branch) {
        var element: Element? = null
        var document: Document? = null

        if (current is Element) {
            element = current
        } else {
            document = current as Document?
        }

        when (node.getNodeType()) {
            Node.ELEMENT_NODE -> readElement(node, current)

            Node.PROCESSING_INSTRUCTION_NODE -> if (current is Element) {
                val currentEl = current
                currentEl.addProcessingInstruction(node.getNodeName(), node.getNodeValue())
            } else {
                val currentDoc = current as Document
                currentDoc.addProcessingInstruction(node.getNodeName(), node.getNodeValue())
            }

            Node.COMMENT_NODE -> if (current is Element) {
                current.addComment(node.getNodeValue())
            } else {
                (current as Document).addComment(node.getNodeValue())
            }

            Node.DOCUMENT_TYPE_NODE -> {
                val domDocType = node as DocumentType
                document!!.addDocType(
                    domDocType.getName(), domDocType.getPublicId(),
                    domDocType.getSystemId()
                )
            }

            Node.TEXT_NODE -> element!!.addText(node.getNodeValue())

            Node.CDATA_SECTION_NODE -> element!!.addCDATA(node.getNodeValue())

            Node.ENTITY_REFERENCE_NODE -> {
                // is there a better way to get the value of an entity?
                val firstChild = node.getFirstChild()

                if (firstChild != null) {
                    element!!.addEntity(node.getNodeName(), firstChild.getNodeValue())
                } else {
                    element!!.addEntity(node.getNodeName(), "")
                }
            }

            Node.ENTITY_NODE -> element!!.addEntity(node.getNodeName(), node.getNodeValue())

            else -> {}
        }
    }

    protected fun readElement(node: Node, current: Branch) {
        val previouslyDeclaredNamespaces = namespaceStack.size()

        var namespaceUri = node.getNamespaceURI()
        var elementPrefix = node.getPrefix()

        if (elementPrefix == null) {
            elementPrefix = ""
        }

        val attributeList = node.getAttributes()

        if ((attributeList != null) && (namespaceUri == null)) {
            // test if we have an "xmlns" attribute
            val attribute = attributeList.getNamedItem("xmlns")

            if (attribute != null) {
                namespaceUri = attribute.getNodeValue()
                elementPrefix = ""
            }
        }

        val qName = namespaceStack
            .getQName(namespaceUri, node.getLocalName(), node.getNodeName())
        val element = current.addElement(qName)!!

        if (attributeList != null) {
            var size = attributeList.getLength()
            val attributes: MutableList<Node> = ArrayList<Node>(size)

            for (i in 0..<size) {
                val attribute = attributeList.item(i)

                // Define all namespaces first then process attributes later
                val name = attribute.getNodeName()

                if (name.startsWith("xmlns")) {
                    val prefix = getPrefix(name)
                    val uri = attribute.getNodeValue()

                    val namespace = namespaceStack.addNamespace(prefix, uri)
                    element.add(namespace)
                } else {
                    attributes.add(attribute)
                }
            }

            // now add the attributes, the namespaces should be available
            size = attributes.size

            for (i in 0..<size) {
                val attribute = attributes.get(i) as Node
                val attributeQName = namespaceStack.getQName(
                    attribute.getNamespaceURI(),
                    attribute.getLocalName(), attribute.getNodeName()
                )
                element.addAttribute(attributeQName, attribute.getNodeValue())
            }
        }

        // Recurse on child nodes
        val children = node.getChildNodes()

        var i = 0
        val size = children.getLength()
        while (i < size) {
            val child = children.item(i)
            readTree(child, element)
            i++
        }

        // pop namespaces from the stack
        while (namespaceStack.size() > previouslyDeclaredNamespaces) {
            namespaceStack.pop()
        }
    }

    protected fun getNamespace(prefix: String?, uri: String?): Namespace? {
        return this.documentFactory!!.createNamespace(prefix, uri)
    }

    protected fun createDocument(): Document {
        return this.documentFactory!!.createDocument()
    }

    protected fun clearNamespaceStack() {
        namespaceStack.clear()

        if (!namespaceStack.contains(Namespace.Companion.XML_NAMESPACE)) {
            namespaceStack.push(Namespace.Companion.XML_NAMESPACE)
        }
    }

    private fun getPrefix(xmlnsDecl: String): String {
        val index = xmlnsDecl.indexOf(':', 5)

        if (index != -1) {
            return xmlnsDecl.substring(index + 1)
        } else {
            return ""
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


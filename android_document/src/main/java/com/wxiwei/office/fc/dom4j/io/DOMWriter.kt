/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.CDATA
import com.wxiwei.office.fc.dom4j.Comment
import com.wxiwei.office.fc.dom4j.DocumentException
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Entity
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.Text
import com.wxiwei.office.fc.dom4j.tree.NamespaceStack
import org.w3c.dom.DOMImplementation
import org.w3c.dom.Document
import org.w3c.dom.DocumentType
import org.w3c.dom.Node

/**
 * 
 * 
 * `DOMWriter` takes a DOM4J tree and outputs it as a W3C DOM
 * object
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.17 $
 */
class DOMWriter {
    // the Class used to create new DOM Document instances
    private var domDocumentClass: Class<*>? = null

    /** stack of `Namespace` objects  */
    private val namespaceStack = NamespaceStack()

    constructor()

    constructor(domDocumentClass: Class<*>?) {
        this.domDocumentClass = domDocumentClass
    }

    @Throws(DocumentException::class)
    fun getDomDocumentClass(): Class<*>? {
        var result = domDocumentClass

        if (result == null) {
            // lets try and find one in the classpath
            val size: Int = DEFAULT_DOM_DOCUMENT_CLASSES.size

            for (i in 0..<size) {
                try {
                    val name: String = DEFAULT_DOM_DOCUMENT_CLASSES[i]
                    result = Class.forName(name, true, DOMWriter::class.java.getClassLoader())

                    if (result != null) {
                        break
                    }
                } catch (e: Exception) {
                    // could not load class correctly
                    // lets carry on to the next one
                }
            }
        }

        return result
    }

    /**
     * Sets the DOM [Document]implementation class used by
     * the writer when creating DOM documents.
     * 
     * @param domDocumentClass
     * is the Class implementing the {@linkorg.w3c.dom.Document}
     * interface
     */
    fun setDomDocumentClass(domDocumentClass: Class<*>?) {
        this.domDocumentClass = domDocumentClass
    }

    /**
     * Sets the DOM [Document]implementation class name used
     * by the writer when creating DOM documents.
     * 
     * @param name
     * is the name of the Class implementing the [            ] interface
     * 
     * @throws DocumentException
     * if the class could not be loaded
     */
    @Throws(DocumentException::class)
    fun setDomDocumentClassName(name: String) {
        try {
            this.domDocumentClass =
                Class.forName(name, true, DOMWriter::class.java.getClassLoader())
        } catch (e: Exception) {
            throw DocumentException("Could not load the DOM Document " + "class: " + name, e)
        }
    }

    @Throws(DocumentException::class)
    fun write(document: com.wxiwei.office.fc.dom4j.Document): Document? {
        if (document is Document) {
            return document as Document
        }

        resetNamespaceStack()

        val domDocument = createDomDocument(document)
        appendDOMTree(domDocument, domDocument, document.content()!!)
        namespaceStack.clear()

        return domDocument
    }

    @Throws(DocumentException::class)
    fun write(
        document: com.wxiwei.office.fc.dom4j.Document,
        domImpl: DOMImplementation
    ): Document? {
        if (document is Document) {
            return document as Document
        }

        resetNamespaceStack()

        val domDocument = createDomDocument(document, domImpl)
        appendDOMTree(domDocument, domDocument, document.content()!!)
        namespaceStack.clear()

        return domDocument
    }

    protected fun appendDOMTree(
        domDocument: Document, domCurrent: Node,
        content: MutableList<*>
    ) {
        val size = content.size

        for (i in 0..<size) {
            val `object`: Any? = content.get(i)

            if (`object` is Element) {
                appendDOMTree(domDocument, domCurrent, `object`)
            } else if (`object` is String) {
                appendDOMTree(domDocument, domCurrent, `object`)
            } else if (`object` is Text) {
                val text = `object`
                appendDOMTree(domDocument, domCurrent, text.text)
            } else if (`object` is CDATA) {
                appendDOMTree(domDocument, domCurrent, `object`)
            } else if (`object` is Comment) {
                appendDOMTree(domDocument, domCurrent, `object`)
            } else if (`object` is Entity) {
                appendDOMTree(domDocument, domCurrent, `object`)
            } else if (`object` is ProcessingInstruction) {
                appendDOMTree(domDocument, domCurrent, `object`)
            }
        }
    }

    protected fun appendDOMTree(
        domDocument: Document, domCurrent: Node,
        element: Element
    ) {
        val elUri = element.namespaceURI
        val elName = element.qualifiedName
        val domElement = domDocument.createElementNS(elUri, elName)

        val stackSize = namespaceStack.size()

        // add the namespace of the element first
        val elementNamespace = element.namespace

        if (isNamespaceDeclaration(elementNamespace)) {
            namespaceStack.push(elementNamespace!!)
            writeNamespace(domElement, elementNamespace!!)
        }

        // add the additional declared namespaces
        val declaredNamespaces = element.declaredNamespaces()!!

        run {
            var i = 0
            val size = declaredNamespaces.size
            while (i < size) {
                val namespace = declaredNamespaces.get(i) as Namespace

                if (isNamespaceDeclaration(namespace)) {
                    namespaceStack.push(namespace)
                    writeNamespace(domElement, namespace)
                }
                i++
            }
        }

        // add the attributes
        var i = 0
        val size = element.attributeCount()
        while (i < size) {
            val attribute = element.attribute(i) as Attribute
            val attUri = attribute.namespaceURI
            val attName = attribute.qualifiedName
            val value = attribute.value
            domElement.setAttributeNS(attUri, attName, value)
            i++
        }

        // add content
        appendDOMTree(domDocument, domElement, element.content()!!)

        domCurrent.appendChild(domElement)

        while (namespaceStack.size() > stackSize) {
            namespaceStack.pop()
        }
    }

    protected fun appendDOMTree(
        domDocument: Document, domCurrent: Node,
        cdata: CDATA
    ) {
        val domCDATA = domDocument.createCDATASection(cdata.text)
        domCurrent.appendChild(domCDATA)
    }

    protected fun appendDOMTree(
        domDocument: Document, domCurrent: Node,
        comment: Comment
    ) {
        val domComment = domDocument.createComment(comment.text)
        domCurrent.appendChild(domComment)
    }

    protected fun appendDOMTree(
        domDocument: Document, domCurrent: Node,
        text: String?
    ) {
        val domText = domDocument.createTextNode(text)
        domCurrent.appendChild(domText)
    }

    protected fun appendDOMTree(
        domDocument: Document, domCurrent: Node,
        entity: Entity
    ) {
        val domEntity = domDocument.createEntityReference(entity.name)
        domCurrent.appendChild(domEntity)
    }

    protected fun appendDOMTree(
        domDoc: Document, domCurrent: Node,
        pi: ProcessingInstruction
    ) {
        val domPI = domDoc.createProcessingInstruction(
            pi.target, pi.text
        )
        domCurrent.appendChild(domPI)
    }

    protected fun writeNamespace(domElement: org.w3c.dom.Element, namespace: Namespace) {
        val attributeName = attributeNameForNamespace(namespace)

        // domElement.setAttributeNS("", attributeName, namespace.getURI());
        domElement.setAttribute(attributeName, namespace.getURI())
    }

    protected fun attributeNameForNamespace(namespace: Namespace): String {
        val xmlns = "xmlns"
        val prefix = namespace.getPrefix()

        if (prefix!!.length > 0) {
            return xmlns + ":" + prefix
        }

        return xmlns
    }

    @Throws(DocumentException::class)
    protected fun createDomDocument(document: com.wxiwei.office.fc.dom4j.Document?): Document {
        var result: Document? = null

        // use the given domDocumentClass (if not null)
        if (domDocumentClass != null) {
            try {
                result = domDocumentClass!!.newInstance() as Document
            } catch (e: Exception) {
                throw DocumentException(
                    ("Could not instantiate an instance "
                            + "of DOM Document with class: " + domDocumentClass!!.getName()), e
                )
            }
        } else {
            // lets try JAXP first before using the hardcoded default parsers
            result = createDomDocumentViaJAXP()

            if (result == null) {
                val theClass = getDomDocumentClass()

                try {
                    result = theClass!!.newInstance() as Document
                } catch (e: Exception) {
                    throw DocumentException(
                        ("Could not instantiate an "
                                + "instance of DOM Document " + "with class: " + theClass!!.getName()),
                        e
                    )
                }
            }
        }

        return result
    }

    @Throws(DocumentException::class)
    protected fun createDomDocumentViaJAXP(): Document? {
        try {
            return JAXPHelper.createDocument(false, true)
        } catch (e: Throwable) {
            if (!loggedWarning) {
                loggedWarning = true

                if (SAXHelper.isVerboseErrorReporting) {
                    // log all exceptions as warnings and carry
                    e.printStackTrace()
                } else {
                }
            }
        }

        return null
    }

    @Throws(DocumentException::class)
    protected fun createDomDocument(
        document: com.wxiwei.office.fc.dom4j.Document?,
        domImpl: DOMImplementation
    ): Document {
        val namespaceURI: String? = null
        val qualifiedName: String? = null
        val docType: DocumentType? = null

        return domImpl.createDocument(namespaceURI, qualifiedName, docType)
    }

    protected fun isNamespaceDeclaration(ns: Namespace?): Boolean {
        if ((ns != null) && (ns !== Namespace.Companion.NO_NAMESPACE) && (ns !== Namespace.Companion.XML_NAMESPACE)) {
            val uri = ns.getURI()

            if ((uri != null) && (uri.length > 0)) {
                if (!namespaceStack.contains(ns)) {
                    return true
                }
            }
        }

        return false
    }

    protected fun resetNamespaceStack() {
        namespaceStack.clear()
        namespaceStack.push(Namespace.Companion.XML_NAMESPACE)
    }

    companion object {
        private var loggedWarning = false

        private val DEFAULT_DOM_DOCUMENT_CLASSES = arrayOf<String>(
            "org.apache.xerces.dom.DocumentImpl",  // Xerces
            "gnu.xml.dom.DomDocument",  // GNU JAXP
            "org.apache.crimson.tree.XmlDocument",  // Crimson
            "com.sun.xml.tree.XmlDocument",  // Sun's Project X
            "oracle.xml.parser.v2.XMLDocument",  // Oracle V2
            "oracle.xml.parser.XMLDocument",  // Oracle V1
            "org.dom4j.dom.DOMDocument" // Internal DOM implementation
        )
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


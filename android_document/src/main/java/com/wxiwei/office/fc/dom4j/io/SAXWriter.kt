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
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.CDATA
import com.wxiwei.office.fc.dom4j.CharacterData
import com.wxiwei.office.fc.dom4j.Comment
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentType
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Entity
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.Text
import com.wxiwei.office.fc.dom4j.tree.NamespaceStack
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.DTDHandler
import org.xml.sax.EntityResolver
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXNotRecognizedException
import org.xml.sax.SAXNotSupportedException
import org.xml.sax.XMLReader
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.AttributesImpl
import org.xml.sax.helpers.LocatorImpl
import java.io.IOException
import kotlin.Any
import kotlin.Array
import kotlin.Int
import kotlin.String
import kotlin.Throws
import kotlin.arrayOf

/**
 * 
 * 
 * `SAXWriter` writes a DOM4J tree to a SAX ContentHandler.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.24 $
 */
class SAXWriter() : XMLReader {
    /** `ContentHandler` to which SAX events are raised  */
    private var contentHandler: ContentHandler? = null

    /** `DTDHandler` fired when a document has a DTD  */
    private var dtdHandler: DTDHandler? = null

    /** `EntityResolver` fired when a document has a DTD  */
    private var entityResolver: EntityResolver? = null

    private var errorHandler: ErrorHandler? = null

    /**
     * DOCUMENT ME!
     * 
     * @return the `LexicalHandler` used when a Document contains a
     * DTD
     */
    /**
     * Sets the `LexicalHandler`.
     * 
     * @param lexicalHandler
     * is the `LexicalHandler`
     */
    /** `LexicalHandler` fired on Entity and CDATA sections  */
    var lexicalHandler: LexicalHandler? = null

    /** `AttributesImpl` used when generating the Attributes  */
    private val attributes = AttributesImpl()

    /** Stores the features  */
    private val features: MutableMap<String?, Any?> = HashMap<String?, Any?>()

    /** Stores the properties  */
    private val properties: MutableMap<String?, Any?> = HashMap<String?, Any?>()

    /**
     * Should namespace declarations be converted to "xmlns" attributes. This
     * property defaults to `false` as per the SAX specification.
     * This property is set via the SAX feature
     * "http://xml.org/sax/features/namespace-prefixes"
     * 
     * @return DOCUMENT ME!
     */
    /**
     * Sets whether namespace declarations should be exported as "xmlns"
     * attributes or not. This property is set from the SAX feature
     * "http://xml.org/sax/features/namespace-prefixes"
     * 
     * @param declareNamespaceAttrs
     * DOCUMENT ME!
     */
    /** Whether namespace declarations are exported as attributes or not  */
    var isDeclareNamespaceAttributes: Boolean = false

    init {
        properties.put(FEATURE_NAMESPACE_PREFIXES, java.lang.Boolean.FALSE)
        properties.put(FEATURE_NAMESPACE_PREFIXES, java.lang.Boolean.TRUE)
    }

    constructor(contentHandler: ContentHandler) : this() {
        this.contentHandler = contentHandler
    }

    constructor(contentHandler: ContentHandler, lexicalHandler: LexicalHandler?) : this() {
        this.contentHandler = contentHandler
        this.lexicalHandler = lexicalHandler
    }

    constructor(
        contentHandler: ContentHandler, lexicalHandler: LexicalHandler?,
        entityResolver: EntityResolver?
    ) : this() {
        this.contentHandler = contentHandler
        this.lexicalHandler = lexicalHandler
        this.entityResolver = entityResolver
    }

    /**
     * A polymorphic method to write any Node to this SAX stream
     * 
     * @param node
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    fun write(node: Node) {
        val nodeType = node.nodeType

        when (nodeType) {
            Node.Companion.ELEMENT_NODE -> write(node as Element)

            Node.Companion.ATTRIBUTE_NODE -> write(node as Attribute)

            Node.Companion.TEXT_NODE -> write(node.text)

            Node.Companion.CDATA_SECTION_NODE -> write(node as CDATA)

            Node.Companion.ENTITY_REFERENCE_NODE -> write(node as Entity)

            Node.Companion.PROCESSING_INSTRUCTION_NODE -> write(node as ProcessingInstruction)

            Node.Companion.COMMENT_NODE -> write(node as Comment)

            Node.Companion.DOCUMENT_NODE -> write(node as Document?)

            Node.Companion.DOCUMENT_TYPE_NODE -> write(node as DocumentType)

            Node.Companion.NAMESPACE_NODE -> {}
            else -> throw SAXException("Invalid node type: " + node)
        }
    }

    /**
     * Generates SAX events for the given Document and all its content
     * 
     * @param document
     * is the Document to parse
     * 
     * @throws SAXException
     * if there is a SAX error processing the events
     */
    @Throws(SAXException::class)
    fun write(document: Document?) {
        if (document != null) {
            checkForNullHandlers()

            documentLocator(document)
            startDocument()
            entityResolver(document)
            dtdHandler(document)

            writeContent(document, NamespaceStack())
            endDocument()
        }
    }

    /**
     * Generates SAX events for the given Element and all its content
     * 
     * @param element
     * is the Element to parse
     * 
     * @throws SAXException
     * if there is a SAX error processing the events
     */
    @Throws(SAXException::class)
    fun write(element: Element) {
        write(element, NamespaceStack())
    }

    /**
     * 
     * 
     * Writes the opening tag of an [Element], including its [ ]s but without its content.
     * 
     * 
     * @param element
     * `Element` to output.
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    fun writeOpen(element: Element) {
        startElement(element, null)
    }

    /**
     * 
     * 
     * Writes the closing tag of an [Element]
     * 
     * 
     * @param element
     * `Element` to output.
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    fun writeClose(element: Element) {
        endElement(element)
    }

    /**
     * Generates SAX events for the given text
     * 
     * @param text
     * is the text to send to the SAX ContentHandler
     * 
     * @throws SAXException
     * if there is a SAX error processing the events
     */
    @Throws(SAXException::class)
    fun write(text: String?) {
        if (text != null) {
            val chars = text.toCharArray()
            contentHandler!!.characters(chars, 0, chars.size)
        }
    }

    /**
     * Generates SAX events for the given CDATA
     * 
     * @param cdata
     * is the CDATA to parse
     * 
     * @throws SAXException
     * if there is a SAX error processing the events
     */
    @Throws(SAXException::class)
    fun write(cdata: CDATA) {
        val text = cdata.text

        if (lexicalHandler != null) {
            lexicalHandler!!.startCDATA()
            write(text)
            lexicalHandler!!.endCDATA()
        } else {
            write(text)
        }
    }

    /**
     * Generates SAX events for the given Comment
     * 
     * @param comment
     * is the Comment to parse
     * 
     * @throws SAXException
     * if there is a SAX error processing the events
     */
    @Throws(SAXException::class)
    fun write(comment: Comment) {
        if (lexicalHandler != null) {
            val text = comment.text
            val chars = text!!.toCharArray()
            lexicalHandler!!.comment(chars, 0, chars.size)
        }
    }

    /**
     * Generates SAX events for the given Entity
     * 
     * @param entity
     * is the Entity to parse
     * 
     * @throws SAXException
     * if there is a SAX error processing the events
     */
    @Throws(SAXException::class)
    fun write(entity: Entity) {
        val text = entity.text

        if (lexicalHandler != null) {
            val name = entity.name
            lexicalHandler!!.startEntity(name)
            write(text)
            lexicalHandler!!.endEntity(name)
        } else {
            write(text)
        }
    }

    /**
     * Generates SAX events for the given ProcessingInstruction
     * 
     * @param pi
     * is the ProcessingInstruction to parse
     * 
     * @throws SAXException
     * if there is a SAX error processing the events
     */
    @Throws(SAXException::class)
    fun write(pi: ProcessingInstruction) {
        val target = pi.target
        val text = pi.text
        contentHandler!!.processingInstruction(target, text)
    }

    // XMLReader methods
    // -------------------------------------------------------------------------
    /**
     * DOCUMENT ME!
     * 
     * @return the `ContentHandler` called when SAX events are
     * raised
     */
    override fun getContentHandler(): ContentHandler {
        return contentHandler!!
    }

    /**
     * Sets the `ContentHandler` called when SAX events are raised
     * 
     * @param contentHandler
     * is the `ContentHandler` called when SAX events
     * are raised
     */
    override fun setContentHandler(contentHandler: ContentHandler) {
        this.contentHandler = contentHandler
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the `DTDHandler`
     */
    override fun getDTDHandler(): DTDHandler? {
        return dtdHandler
    }

    /**
     * Sets the `DTDHandler`.
     * 
     * @param handler
     * DOCUMENT ME!
     */
    override fun setDTDHandler(handler: DTDHandler?) {
        this.dtdHandler = handler
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the `ErrorHandler`
     */
    override fun getErrorHandler(): ErrorHandler? {
        return errorHandler
    }

    /**
     * Sets the `ErrorHandler`.
     * 
     * @param errorHandler
     * DOCUMENT ME!
     */
    override fun setErrorHandler(errorHandler: ErrorHandler?) {
        this.errorHandler = errorHandler
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the `EntityResolver` used when a Document contains a
     * DTD
     */
    override fun getEntityResolver(): EntityResolver? {
        return entityResolver
    }

    /**
     * Sets the `EntityResolver`.
     * 
     * @param entityResolver
     * is the `EntityResolver`
     */
    override fun setEntityResolver(entityResolver: EntityResolver?) {
        this.entityResolver = entityResolver
    }

    /**
     * Sets the `XMLReader` used to write SAX events to
     * 
     * @param xmlReader
     * is the `XMLReader`
     */
    fun setXMLReader(xmlReader: XMLReader) {
        setContentHandler(xmlReader.getContentHandler())
        setDTDHandler(xmlReader.getDTDHandler())
        setEntityResolver(xmlReader.getEntityResolver())
        setErrorHandler(xmlReader.getErrorHandler())
    }

    /**
     * Looks up the value of a feature.
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws SAXNotRecognizedException
     * DOCUMENT ME!
     * @throws SAXNotSupportedException
     * DOCUMENT ME!
     */
    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    override fun getFeature(name: String?): kotlin.Boolean {
        val answer = features.get(name) as kotlin.Boolean?

        return (answer != null) && answer
    }

    /**
     * This implementation does actually use any features but just stores them
     * for later retrieval
     * 
     * @param name
     * DOCUMENT ME!
     * @param value
     * DOCUMENT ME!
     * 
     * @throws SAXNotRecognizedException
     * DOCUMENT ME!
     * @throws SAXNotSupportedException
     * DOCUMENT ME!
     */
    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    override fun setFeature(name: String?, value: kotlin.Boolean) {
        if (FEATURE_NAMESPACE_PREFIXES == name) {
            this.isDeclareNamespaceAttributes = value
        } else if (FEATURE_NAMESPACE_PREFIXES == name) {
            if (!value) {
                val msg = "Namespace feature is always supported in dom4j"
                throw SAXNotSupportedException(msg)
            }
        }

        features.put(name, if (value) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE)
    }

    /**
     * Sets the given SAX property
     * 
     * @param name
     * DOCUMENT ME!
     * @param value
     * DOCUMENT ME!
     */
    override fun setProperty(name: String?, value: Any?) {
        for (i in LEXICAL_HANDLER_NAMES.indices) {
            if (LEXICAL_HANDLER_NAMES[i] == name) {
                this.lexicalHandler = value as LexicalHandler?

                return
            }
        }

        properties.put(name, value)
    }

    /**
     * Gets the given SAX property
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws SAXNotRecognizedException
     * DOCUMENT ME!
     * @throws SAXNotSupportedException
     * DOCUMENT ME!
     */
    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    override fun getProperty(name: String?): Any? {
        for (i in LEXICAL_HANDLER_NAMES.indices) {
            if (LEXICAL_HANDLER_NAMES[i] == name) {
                return this.lexicalHandler
            }
        }

        return properties.get(name)
    }

    /**
     * This method is not supported.
     * 
     * @param systemId
     * DOCUMENT ME!
     * 
     * @throws SAXNotSupportedException
     * DOCUMENT ME!
     */
    @Throws(SAXNotSupportedException::class)
    override fun parse(systemId: String?) {
        throw SAXNotSupportedException(
            "This XMLReader can only accept"
                    + " <dom4j> InputSource objects"
        )
    }

    /**
     * Parses an XML document. This method can only accept DocumentInputSource
     * inputs otherwise a [SAXNotSupportedException]exception is thrown.
     * 
     * @param input
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     * @throws SAXNotSupportedException
     * if the input source is not wrapping a dom4j document
     */
    @Throws(SAXException::class)
    override fun parse(input: InputSource?) {
        if (input is DocumentInputSource) {
            val documentInput = input
            val document = documentInput.getDocument()
            write(document as Document?)
        } else {
            throw SAXNotSupportedException(
                "This XMLReader can only accept "
                        + "<dom4j> InputSource objects"
            )
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    @Throws(SAXException::class)
    protected fun writeContent(branch: Branch, namespaceStack: NamespaceStack) {
        val iter = branch.nodeIterator()!!
        while (iter.hasNext()) {
            val `object` = iter.next()

            if (`object` is Element) {
                write(`object`, namespaceStack)
            } else if (`object` is CharacterData) {
                if (`object` is Text) {
                    val text = `object`
                    write(text.text)
                } else if (`object` is CDATA) {
                    write(`object`)
                } else if (`object` is Comment) {
                    write(`object`)
                } else {
                    throw SAXException(
                        ("Invalid Node in DOM4J content: " + `object`
                                + " of type: " + `object`.javaClass)
                    )
                }
            } else if (`object` is String) {
                write(`object`)
            } else if (`object` is Entity) {
                write(`object`)
            } else if (`object` is ProcessingInstruction) {
                write(`object`)
            } else if (`object` is Namespace) {
                write(`object`)
            } else {
                throw SAXException("Invalid Node in DOM4J content: " + `object`)
            }
        }
    }

    /**
     * The [org.xml.sax.Locator]is only really useful when parsing a
     * textual document as its main purpose is to identify the line and column
     * number. Since we are processing an in memory tree which will probably
     * have its line number information removed, we'll just use -1 for the line
     * and column numbers.
     * 
     * @param document
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    protected fun documentLocator(document: Document) {
        val locator = LocatorImpl()

        var publicID: String? = null
        var systemID: String? = null
        val docType = document.docType

        if (docType != null) {
            publicID = docType.publicID
            systemID = docType.systemID
        }

        if (publicID != null) {
            locator.setPublicId(publicID)
        }

        if (systemID != null) {
            locator.setSystemId(systemID)
        }

        locator.setLineNumber(-1)
        locator.setColumnNumber(-1)

        contentHandler!!.setDocumentLocator(locator)
    }

    @Throws(SAXException::class)
    protected fun entityResolver(document: Document) {
        if (entityResolver != null) {
            val docType = document.docType

            if (docType != null) {
                val publicID = docType.publicID
                val systemID = docType.systemID

                if ((publicID != null) || (systemID != null)) {
                    try {
                        entityResolver!!.resolveEntity(publicID, systemID)
                    } catch (e: IOException) {
                        throw SAXException(
                            ("Could not resolve publicID: " + publicID
                                    + " systemID: " + systemID), e
                        )
                    }
                }
            }
        }
    }

    /**
     * We do not yet support DTD or XML Schemas so this method does nothing
     * right now.
     * 
     * @param document
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    protected fun dtdHandler(document: Document?) {
    }

    @Throws(SAXException::class)
    protected fun startDocument() {
        contentHandler!!.startDocument()
    }

    @Throws(SAXException::class)
    protected fun endDocument() {
        contentHandler!!.endDocument()
    }

    @Throws(SAXException::class)
    protected fun write(element: Element, namespaceStack: NamespaceStack) {
        val stackSize = namespaceStack.size()
        val namespaceAttributes = startPrefixMapping(element, namespaceStack)
        startElement(element, namespaceAttributes)
        writeContent(element, namespaceStack)
        endElement(element)
        endPrefixMapping(namespaceStack, stackSize)
    }

    /**
     * Fires a SAX startPrefixMapping event for all the namespaceStack which
     * have just come into scope
     * 
     * @param element
     * DOCUMENT ME!
     * @param namespaceStack
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    protected fun startPrefixMapping(
        element: Element,
        namespaceStack: NamespaceStack
    ): AttributesImpl? {
        var namespaceAttributes: AttributesImpl? = null

        // start with the namespace of the element
        val elementNamespace = element.namespace

        if ((elementNamespace != null) && !isIgnoreableNamespace(
                elementNamespace,
                namespaceStack
            )
        ) {
            namespaceStack.push(elementNamespace)
            contentHandler!!.startPrefixMapping(
                elementNamespace.getPrefix(),
                elementNamespace.getURI()
            )
            namespaceAttributes = addNamespaceAttribute(namespaceAttributes, elementNamespace)
        }

        val declaredNamespaces = element.declaredNamespaces()!!

        var i = 0
        val size = declaredNamespaces.size
        while (i < size) {
            val namespace = declaredNamespaces.get(i) as Namespace

            if (!isIgnoreableNamespace(namespace, namespaceStack)) {
                namespaceStack.push(namespace)
                contentHandler!!.startPrefixMapping(namespace.getPrefix(), namespace.getURI())
                namespaceAttributes = addNamespaceAttribute(namespaceAttributes, namespace)
            }
            i++
        }

        return namespaceAttributes
    }

    /**
     * Fires a SAX endPrefixMapping event for all the namespaceStack which have
     * gone out of scope
     * 
     * @param stack
     * DOCUMENT ME!
     * @param stackSize
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    protected fun endPrefixMapping(stack: NamespaceStack, stackSize: Int) {
        while (stack.size() > stackSize) {
            val namespace = stack.pop()

            if (namespace != null) {
                contentHandler!!.endPrefixMapping(namespace.getPrefix())
            }
        }
    }

    @Throws(SAXException::class)
    protected fun startElement(element: Element, namespaceAttributes: AttributesImpl?) {
        contentHandler!!.startElement(
            element.namespaceURI, element.name,
            element.qualifiedName, createAttributes(element, namespaceAttributes)
        )
    }

    @Throws(SAXException::class)
    protected fun endElement(element: Element) {
        contentHandler!!.endElement(
            element.namespaceURI, element.name,
            element.qualifiedName
        )
    }

    @Throws(SAXException::class)
    protected fun createAttributes(element: Element, namespaceAttributes: Attributes?): Attributes {
        attributes.clear()

        if (namespaceAttributes != null) {
            attributes.setAttributes(namespaceAttributes)
        }

        val iter = element.attributeIterator()!!
        while (iter.hasNext()) {
            val attribute = iter.next() as Attribute
            attributes.addAttribute(
                attribute.namespaceURI, attribute.name,
                attribute.qualifiedName, "CDATA", attribute.value
            )
        }

        return attributes
    }

    /**
     * If isDelcareNamespaceAttributes() is enabled then this method will add
     * the given namespace declaration to the supplied attributes object,
     * creating one if it does not exist.
     * 
     * @param attrs
     * DOCUMENT ME!
     * @param namespace
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun addNamespaceAttribute(
        attrs: AttributesImpl?,
        namespace: Namespace
    ): AttributesImpl? {
        var attrs = attrs
        if (this.isDeclareNamespaceAttributes) {
            if (attrs == null) {
                attrs = AttributesImpl()
            }

            val prefix = namespace.getPrefix()
            var qualifiedName = "xmlns"

            if ((prefix != null) && (prefix.length > 0)) {
                qualifiedName = "xmlns:" + prefix
            }

            val uri = ""
            val localName = prefix
            val type = "CDATA"
            val value = namespace.getURI()

            attrs.addAttribute(uri, localName, qualifiedName, type, value)
        }

        return attrs
    }

    /**
     * DOCUMENT ME!
     * 
     * @param namespace
     * DOCUMENT ME!
     * @param namespaceStack
     * DOCUMENT ME!
     * 
     * @return true if the given namespace is an ignorable namespace (such as
     * Namespace.NO_NAMESPACE or Namespace.XML_NAMESPACE) or if the
     * namespace has already been declared in the current scope
     */
    protected fun isIgnoreableNamespace(
        namespace: Namespace,
        namespaceStack: NamespaceStack
    ): kotlin.Boolean {
        if (namespace == Namespace.Companion.NO_NAMESPACE || namespace == Namespace.Companion.XML_NAMESPACE) {
            return true
        }

        val uri = namespace.getURI()

        if ((uri == null) || (uri.length <= 0)) {
            return true
        }

        return namespaceStack.contains(namespace)
    }

    /**
     * Ensures non-null content handlers?
     */
    protected fun checkForNullHandlers() {
    }

    companion object {
        protected val LEXICAL_HANDLER_NAMES: Array<String?> = arrayOf<String?>(
            "http://xml.org/sax/properties/lexical-handler",
            "http://xml.org/sax/handlers/LexicalHandler"
        )

        protected const val FEATURE_NAMESPACE_PREFIXES: String =
            "http://xml.org/sax/features/namespace-prefixes"

        protected const val FEATURE_NAMESPACES: String = "http://xml.org/sax/features/namespaces"
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


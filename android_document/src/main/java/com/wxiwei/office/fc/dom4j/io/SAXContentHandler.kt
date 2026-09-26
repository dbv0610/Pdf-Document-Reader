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
import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.dtd.AttributeDecl
import com.wxiwei.office.fc.dom4j.dtd.ElementDecl
import com.wxiwei.office.fc.dom4j.dtd.ExternalEntityDecl
import com.wxiwei.office.fc.dom4j.dtd.InternalEntityDecl
import com.wxiwei.office.fc.dom4j.tree.AbstractElement
import com.wxiwei.office.fc.dom4j.tree.NamespaceStack
import org.xml.sax.Attributes
import org.xml.sax.DTDHandler
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.Locator
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.ext.DeclHandler
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.DefaultHandler

/**
 * 
 * 
 * `SAXContentHandler` builds a dom4j tree via SAX events.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.61 $
 */
open class SAXContentHandler(
    /** The factory used to create new `Document` instances  */
    private val documentFactory: DocumentFactory,
    /** the `ElementHandler` called as the elements are complete  */
    private val elementHandler: ElementHandler?,
    /** stack of `Element` objects  */
    elementStack: ElementStack?
) : DefaultHandler(), LexicalHandler, DeclHandler, DTDHandler {
    /** stack of `Element` objects  */
    var elementStack: ElementStack = elementStack ?: createElementStack()

    /** The document that is being built  */
    private var document: Document? = null

    // Properties
    // -------------------------------------------------------------------------

    /** stack of `Namespace` and `QName` objects  */
    private val namespaceStack: NamespaceStack

    /** the Locator  */
    private var locator: Locator? = null

    /** The name of the current entity  */
    private var entity: String? = null

    /** Flag used to indicate that we are inside a DTD section  */
    private var insideDTDSection = false

    /** Flag used to indicate that we are inside a CDATA section  */
    private var insideCDATASection = false

    /**
     * buffer to hold contents of cdata section across multiple characters
     * events
     */
    private var cdataText: StringBuffer? = null

    /** namespaces that are available for use  */
    private val availableNamespaceMap: MutableMap<*, *> = HashMap<Any?, Any?>()

    /** declared namespaces that are not yet available for use  */
    private val declaredNamespaceList: MutableList<*> = ArrayList<Any?>()

    /** internal DTD declarations  */
    private var internalDTDDeclarations: MutableList<Any?>? = null

    /** external DTD declarations  */
    private var externalDTDDeclarations: MutableList<Any?>? = null

    /** The number of namespaces that are declared in the current scope  */
    private var declaredNamespaceIndex = 0

    /** The entity resolver  */
    var entityResolver: EntityResolver? = null

    var inputSource: InputSource? = null

    /** The current element we are on  */
    private var currentElement: Element? = null

    /**
     * DOCUMENT ME!
     * 
     * @return whether internal DTD declarations should be expanded into the
     * DocumentType object or not.
     */
    /**
     * Sets whether internal DTD declarations should be expanded into the
     * DocumentType object or not.
     * 
     * @param include
     * whether or not DTD declarations should be expanded and
     * included into the DocumentType object.
     */
    /** Should internal DTD declarations be expanded into a List in the DTD  */
    var isIncludeInternalDTDDeclarations: Boolean = false

    /**
     * DOCUMENT ME!
     * 
     * @return whether external DTD declarations should be expanded into the
     * DocumentType object or not.
     */
    /**
     * Sets whether DTD external declarations should be expanded into the
     * DocumentType object or not.
     * 
     * @param include
     * whether or not DTD declarations should be expanded and
     * included into the DocumentType object.
     */
    /** Should external DTD declarations be expanded into a List in the DTD  */
    var isIncludeExternalDTDDeclarations: Boolean = false

    /** The number of levels deep we are inside a startEntity/endEntity call  */
    private var entityLevel = 0

    /** Are we in an internal DTD subset?  */
    private var internalDTDsubset = false

    /**
     * Returns whether adjacent text nodes should be merged together.
     * 
     * @return Value of property mergeAdjacentText.
     */
    /**
     * Sets whether or not adjacent text nodes should be merged together when
     * parsing.
     * 
     * @param mergeAdjacentText
     * New value of property mergeAdjacentText.
     */
    /** Whether adjacent text nodes should be merged  */
    var isMergeAdjacentText: Boolean = false

    /** Have we added text to the buffer  */
    private var textInTextBuffer = false

    /**
     * Returns whether we should ignore comments or not.
     * 
     * @return boolean
     */
    /**
     * Sets whether we should ignore comments or not.
     * 
     * @param ignoreComments
     * whether we should ignore comments or not.
     */
    /** Should we ignore comments  */
    var isIgnoreComments: Boolean = false

    /** Buffer used to concatenate text together  */
    private var textBuffer: StringBuffer? = null

    /**
     * Sets whether whitespace between element start and end tags should be
     * ignored
     * 
     * @return Value of property stripWhitespaceText.
     */
    /**
     * Sets whether whitespace between element start and end tags should be
     * ignored.
     * 
     * @param stripWhitespaceText
     * New value of property stripWhitespaceText.
     */
    /** Holds value of property stripWhitespaceText.  */
    var isStripWhitespaceText: Boolean = false

    @JvmOverloads
    constructor(
        documentFactory: DocumentFactory = DocumentFactory.Companion.getInstance(),
        elementHandler: ElementHandler? = null
    ) : this(documentFactory, elementHandler, null) {
        this.elementStack = createElementStack()
    }

    init {
        this.namespaceStack = NamespaceStack(documentFactory)
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the document that has been or is being built
     */
    fun getDocument(): Document? {
        if (document == null) {
            document = createDocument()
        }

        return document
    }

    // ContentHandler interface
    // -------------------------------------------------------------------------
    override fun setDocumentLocator(documentLocator: Locator?) {
        this.locator = documentLocator
    }

    @Throws(SAXException::class)
    override fun processingInstruction(target: String?, data: String?) {
        if (this.isMergeAdjacentText && textInTextBuffer) {
            completeCurrentTextNode()
        }

        if (currentElement != null) {
            currentElement!!.addProcessingInstruction(target, data)
        } else {
            getDocument()!!.addProcessingInstruction(target, data)
        }
    }

    @Throws(SAXException::class)
    override fun startPrefixMapping(prefix: String?, uri: String?) {
        namespaceStack.push(prefix, uri)
    }

    @Throws(SAXException::class)
    override fun endPrefixMapping(prefix: String?) {
        namespaceStack.pop(prefix)
        declaredNamespaceIndex = namespaceStack.size()
    }

    @Throws(SAXException::class)
    override fun startDocument() {
        // document = createDocument();
        document = null
        currentElement = null

        elementStack.clear()

        if ((elementHandler != null) && (elementHandler is DispatchHandler)) {
            elementStack.dispatchHandler = elementHandler
        }

        namespaceStack.clear()
        declaredNamespaceIndex = 0

        if (this.isMergeAdjacentText && (textBuffer == null)) {
            textBuffer = StringBuffer()
        }

        textInTextBuffer = false
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        namespaceStack.clear()
        elementStack.clear()
        currentElement = null
        textBuffer = null
    }

    @Throws(SAXException::class)
    override fun startElement(
        namespaceURI: String?, localName: String?, qualifiedName: String?,
        attributes: Attributes
    ) {
        if (this.isMergeAdjacentText && textInTextBuffer) {
            completeCurrentTextNode()
        }

        val qName = namespaceStack.getQName(namespaceURI, localName, qualifiedName)

        var branch: Branch? = currentElement

        if (branch == null) {
            branch = getDocument()
        }

        val element = branch!!.addElement(qName)!!

        // add all declared namespaces
        addDeclaredNamespaces(element)

        // now lets add all attribute values
        addAttributes(element, attributes)

        elementStack.pushElement(element)
        currentElement = element

        entity = null // fixes bug527062

        if (elementHandler != null) {
            elementHandler.onStart(elementStack)
        }
    }

    @Throws(SAXException::class)
    override fun endElement(namespaceURI: String?, localName: String?, qName: String?) {
        if (this.isMergeAdjacentText && textInTextBuffer) {
            completeCurrentTextNode()
        }

        if ((elementHandler != null) && (currentElement != null)) {
            elementHandler.onEnd(elementStack)
        }

        elementStack.popElement()
        currentElement = elementStack.peekElement()
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray?, start: Int, end: Int) {
        if (end == 0) {
            return
        }

        if (currentElement != null) {
            if (entity != null) {
                if (this.isMergeAdjacentText && textInTextBuffer) {
                    completeCurrentTextNode()
                }

                currentElement!!.addEntity(entity, kotlin.text.String(ch!!, start, end))
                entity = null
            } else if (insideCDATASection) {
                if (this.isMergeAdjacentText && textInTextBuffer) {
                    completeCurrentTextNode()
                }

                cdataText!!.append(kotlin.text.String(ch!!, start, end))
            } else {
                if (this.isMergeAdjacentText) {
                    textBuffer!!.append(ch, start, end)
                    textInTextBuffer = true
                } else {
                    currentElement!!.addText(kotlin.text.String(ch!!, start, end))
                }
            }
        }
    }

    // ErrorHandler interface
    // -------------------------------------------------------------------------
    /**
     * This method is called when a warning occurs during the parsing of the
     * document. This method does nothing.
     * 
     * @param exception
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    override fun warning(exception: SAXParseException?) {
        // ignore warnings by default
    }

    /**
     * This method is called when an error is detected during parsing such as a
     * validation error. This method rethrows the exception
     * 
     * @param exception
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    override fun error(exception: SAXParseException) {
        throw exception
    }

    /**
     * This method is called when a fatal error occurs during parsing. This
     * method rethrows the exception
     * 
     * @param exception
     * DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    override fun fatalError(exception: SAXParseException) {
        throw exception
    }

    // LexicalHandler interface
    // -------------------------------------------------------------------------
    @Throws(SAXException::class)
    override fun startDTD(name: String?, publicId: String?, systemId: String?) {
        getDocument()!!.addDocType(name, publicId, systemId)
        insideDTDSection = true
        internalDTDsubset = true
    }

    @Throws(SAXException::class)
    override fun endDTD() {
        insideDTDSection = false

        val docType = getDocument()!!.docType

        if (docType != null) {
            if (internalDTDDeclarations != null) {
                docType.internalDeclarations = internalDTDDeclarations
            }

            if (externalDTDDeclarations != null) {
                docType.externalDeclarations = externalDTDDeclarations
            }
        }

        internalDTDDeclarations = null
        externalDTDDeclarations = null
    }

    @Throws(SAXException::class)
    override fun startEntity(name: String?) {
        ++entityLevel

        // Ignore DTD references
        entity = null

        if (!insideDTDSection) {
            if (!isIgnorableEntity(name)) {
                entity = name
            }
        }

        // internal DTD subsets can only appear outside of a
        // startEntity/endEntity block
        // see the startDTD method in
        // http://dom4j.org/javadoc/org/xml/sax/ext/LexicalHandler.html
        internalDTDsubset = false
    }

    @Throws(SAXException::class)
    override fun endEntity(name: String?) {
        --entityLevel
        entity = null

        if (entityLevel == 0) {
            internalDTDsubset = true
        }
    }

    @Throws(SAXException::class)
    override fun startCDATA() {
        insideCDATASection = true
        cdataText = StringBuffer()
    }

    @Throws(SAXException::class)
    override fun endCDATA() {
        insideCDATASection = false
        currentElement!!.addCDATA(cdataText.toString())
    }

    @Throws(SAXException::class)
    override fun comment(ch: CharArray?, start: Int, end: Int) {
        if (!this.isIgnoreComments) {
            if (this.isMergeAdjacentText && textInTextBuffer) {
                completeCurrentTextNode()
            }

            val text = kotlin.text.String(ch!!, start, end)

            if (!insideDTDSection && (text.length > 0)) {
                if (currentElement != null) {
                    currentElement!!.addComment(text)
                } else {
                    getDocument()!!.addComment(text)
                }
            }
        }
    }

    // DeclHandler interface
    // -------------------------------------------------------------------------
    /**
     * Report an element type declaration.
     * 
     * 
     * 
     * The content model will consist of the string "EMPTY", the string "ANY",
     * or a parenthesised group, optionally followed by an occurrence indicator.
     * The model will be normalized so that all parameter entities are fully
     * resolved and all whitespace is removed,and will include the enclosing
     * parentheses. Other normalization (such as removing redundant parentheses
     * or simplifying occurrence indicators) is at the discretion of the parser.
     * 
     * 
     * @param name
     * The element type name.
     * @param model
     * The content model as a normalized string.
     * 
     * @exception SAXException
     * The application may raise an exception.
     */
    @Throws(SAXException::class)
    override fun elementDecl(name: String?, model: String?) {
        if (internalDTDsubset) {
            if (this.isIncludeInternalDTDDeclarations) {
                addDTDDeclaration(ElementDecl(name, model))
            }
        } else {
            if (this.isIncludeExternalDTDDeclarations) {
                addExternalDTDDeclaration(ElementDecl(name, model))
            }
        }
    }

    /**
     * Report an attribute type declaration.
     * 
     * 
     * 
     * Only the effective (first) declaration for an attribute will be reported.
     * The type will be one of the strings "CDATA", "ID", "IDREF", "IDREFS",
     * "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES", a parenthesized token group
     * with the separator "|" and all whitespace removed, or the word "NOTATION"
     * followed by a space followed by a parenthesized token group with all
     * whitespace removed.
     * 
     * 
     * 
     * 
     * Any parameter entities in the attribute value will be expanded, but
     * general entities will not.
     * 
     * 
     * @param eName
     * The name of the associated element.
     * @param aName
     * The name of the attribute.
     * @param type
     * A string representing the attribute type.
     * @param valueDefault
     * A string representing the attribute default ("#IMPLIED",
     * "#REQUIRED", or "#FIXED") or null if none of these applies.
     * @param val
     * A string representing the attribute's default value, or null
     * if there is none.
     * 
     * @exception SAXException
     * The application may raise an exception.
     */
    @Throws(SAXException::class)
    override fun attributeDecl(
        eName: String?, aName: String?, type: String?, valueDefault: String?,
        `val`: String?
    ) {
        if (internalDTDsubset) {
            if (this.isIncludeInternalDTDDeclarations) {
                addDTDDeclaration(AttributeDecl(eName, aName, type, valueDefault, `val`))
            }
        } else {
            if (this.isIncludeExternalDTDDeclarations) {
                addExternalDTDDeclaration(AttributeDecl(eName, aName, type, valueDefault, `val`))
            }
        }
    }

    /**
     * Report an internal entity declaration.
     * 
     * 
     * 
     * Only the effective (first) declaration for each entity will be reported.
     * All parameter entities in the value will be expanded, but general
     * entities will not.
     * 
     * 
     * @param name
     * The name of the entity. If it is a parameter entity, the name
     * will begin with '%'.
     * @param value
     * The replacement text of the entity.
     * 
     * @exception SAXException
     * The application may raise an exception.
     * 
     * @see .externalEntityDecl
     * 
     * @see DTDHandler.unparsedEntityDecl
     */
    @Throws(SAXException::class)
    override fun internalEntityDecl(name: String?, value: String?) {
        if (internalDTDsubset) {
            if (this.isIncludeInternalDTDDeclarations) {
                addDTDDeclaration(InternalEntityDecl(name, value))
            }
        } else {
            if (this.isIncludeExternalDTDDeclarations) {
                addExternalDTDDeclaration(InternalEntityDecl(name, value))
            }
        }
    }

    /**
     * Report a parsed external entity declaration.
     * 
     * 
     * 
     * Only the effective (first) declaration for each entity will be reported.
     * 
     * 
     * @param name
     * The name of the entity. If it is a parameter entity, the name
     * will begin with '%'.
     * @param publicId
     * The declared public identifier of the entity, or null if none
     * was declared.
     * @param sysId
     * The declared system identifier of the entity.
     * 
     * @exception SAXException
     * The application may raise an exception.
     * 
     * @see .internalEntityDecl
     * 
     * @see DTDHandler.unparsedEntityDecl
     */
    @Throws(SAXException::class)
    override fun externalEntityDecl(name: String?, publicId: String?, sysId: String?) {
        val declaration = ExternalEntityDecl(name, publicId, sysId)

        if (internalDTDsubset) {
            if (this.isIncludeInternalDTDDeclarations) {
                addDTDDeclaration(declaration)
            }
        } else {
            if (this.isIncludeExternalDTDDeclarations) {
                addExternalDTDDeclaration(declaration)
            }
        }
    }

    // DTDHandler interface
    // -------------------------------------------------------------------------
    /**
     * Receive notification of a notation declaration event.
     * 
     * 
     * 
     * It is up to the application to record the notation for later reference,
     * if necessary.
     * 
     * 
     * 
     * 
     * At least one of publicId and systemId must be non-null. If a system
     * identifier is present, and it is a URL, the SAX parser must resolve it
     * fully before passing it to the application through this event.
     * 
     * 
     * 
     * 
     * There is no guarantee that the notation declaration will be reported
     * before any unparsed entities that use it.
     * 
     * 
     * @param name
     * The notation name.
     * @param publicId
     * The notation's public identifier, or null if none was given.
     * @param systemId
     * The notation's system identifier, or null if none was given.
     * 
     * @exception SAXException
     * Any SAX exception, possibly wrapping another exception.
     * 
     * @see .unparsedEntityDecl
     * 
     * @see org.xml.sax.AttributeList
     */
    @Throws(SAXException::class)
    override fun notationDecl(name: String?, publicId: String?, systemId: String?) {
        // #### not supported yet!
    }

    /**
     * Receive notification of an unparsed entity declaration event.
     * 
     * 
     * 
     * Note that the notation name corresponds to a notation reported by the
     * [notationDecl][.notationDecl]event. It is up to the application to
     * record the entity for later reference, if necessary.
     * 
     * 
     * 
     * 
     * If the system identifier is a URL, the parser must resolve it fully
     * before passing it to the application.
     * 
     * 
     * @param name
     * The unparsed entity's name.
     * @param publicId
     * The entity's public identifier, or null if none was given.
     * @param systemId
     * The entity's system identifier.
     * @param notationName
     * The name of the associated notation.
     * 
     * @exception SAXException
     * Any SAX exception, possibly wrapping another exception.
     * 
     * @see .notationDecl
     * 
     * @see org.xml.sax.AttributeList
     */
    @Throws(SAXException::class)
    override fun unparsedEntityDecl(
        name: String?, publicId: String?, systemId: String?,
        notationName: String?
    ) {
        // #### not supported yet!
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    /**
     * If the current text buffer contains any text then create a new text node
     * with it and add it to the current element
     */
    protected fun completeCurrentTextNode() {
        if (this.isStripWhitespaceText) {
            var whitespace = true

            var i = 0
            val size = textBuffer!!.length
            while (i < size) {
                if (!Character.isWhitespace(textBuffer!!.get(i))) {
                    whitespace = false

                    break
                }
                i++
            }

            if (!whitespace) {
                currentElement!!.addText(textBuffer.toString())
            }
        } else {
            currentElement!!.addText(textBuffer.toString())
        }

        textBuffer!!.setLength(0)
        textInTextBuffer = false
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the current document
     */
    protected fun createDocument(): Document {
        val encoding = this.encoding
        val doc = documentFactory.createDocument(encoding)

        // set the EntityResolver
        doc.entityResolver = entityResolver

        if (inputSource != null) {
            doc.name = inputSource!!.getSystemId()
        }

        return doc
    }

    private val encoding: String?
        get() {
            val locator = this.locator
            if (locator == null) {
                return null
            }

            // use reflection to avoid dependency on Locator2
            // or other locator implemenations.
            try {
                val m = locator.javaClass.getMethod(
                    "getEncoding",
                    *arrayOf<Class<*>?>()
                )

                if (m != null) {
                    return m.invoke(locator) as String?
                }
            } catch (e: Exception) {
                // do nothing
            }

            // couldn't determine encoding, returning null...
            return null
        }

    /**
     * a Strategy Method to determine if a given entity name is ignorable
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun isIgnorableEntity(name: String?): Boolean {
        return "amp" == name || "apos" == name || "gt" == name || "lt" == name
                || "quot" == name
    }

    /**
     * Add all namespaces declared before the startElement() SAX event to the
     * current element so that they are available to child elements and
     * attributes
     * 
     * @param element
     * DOCUMENT ME!
     */
    protected fun addDeclaredNamespaces(element: Element) {
        val elementNamespace = element.namespace

        val size = namespaceStack.size()
        while (declaredNamespaceIndex < size) {
            val namespace = namespaceStack.getNamespace(declaredNamespaceIndex)

            // if ( namespace != elementNamespace ) {
            element.add(namespace)

            declaredNamespaceIndex++
        }
    }

    /**
     * Add all the attributes to the given elements
     * 
     * @param element
     * DOCUMENT ME!
     * @param attributes
     * DOCUMENT ME!
     */
    protected fun addAttributes(element: Element, attributes: Attributes) {
        // XXXX: as an optimisation, we could deduce this value from the current
        // SAX parser settings, the SAX namespaces-prefixes feature
        val noNamespaceAttributes = false

        if (element is AbstractElement) {
            // optimised method
            val baseElement = element
            baseElement.setAttributes(attributes, namespaceStack, noNamespaceAttributes)
        } else {
            val size = attributes.getLength()

            for (i in 0..<size) {
                val attributeQName = attributes.getQName(i)

                if (noNamespaceAttributes || !attributeQName.startsWith("xmlns")) {
                    val attributeURI = attributes.getURI(i)
                    val attributeLocalName = attributes.getLocalName(i)
                    val attributeValue = attributes.getValue(i)

                    val qName = namespaceStack.getAttributeQName(
                        attributeURI,
                        attributeLocalName, attributeQName
                    )
                    element.addAttribute(qName, attributeValue)
                }
            }
        }
    }

    /**
     * Adds an internal DTD declaration to the list of declarations
     * 
     * @param declaration
     * DOCUMENT ME!
     */
    protected fun addDTDDeclaration(declaration: Any?) {
        if (internalDTDDeclarations == null) {
            internalDTDDeclarations = ArrayList<Any?>()
        }

        internalDTDDeclarations!!.add(declaration)
    }

    /**
     * Adds an external DTD declaration to the list of declarations
     * 
     * @param declaration
     * DOCUMENT ME!
     */
    protected fun addExternalDTDDeclaration(declaration: Any?) {
        if (externalDTDDeclarations == null) {
            externalDTDDeclarations = ArrayList<Any?>()
        }

        externalDTDDeclarations!!.add(declaration)
    }

    protected fun createElementStack(): ElementStack {
        return ElementStack()
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


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

import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentException
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.ElementHandler
import org.xml.sax.EntityResolver
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.XMLFilter
import org.xml.sax.XMLReader
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.helpers.XMLReaderFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.Reader
import java.io.Serializable
import java.net.URL

/**
 * 
 * 
 * `SAXReader` creates a DOM4J tree from SAX parsing events.
 * 
 * 
 * 
 * 
 * The actual SAX parser that is used by this class is configurable so you can
 * use your favourite SAX parser if you wish. DOM4J comes configured with its
 * own SAX parser so you do not need to worry about configuring the SAX parser.
 * 
 * 
 * 
 * 
 * To explicitly configure the SAX parser that is used via Java code you can use
 * a constructor or use the [.setXMLReader]or [ ][.setXMLReaderClassName] methods.
 * 
 * 
 * 
 * 
 * If the parser is not specified explicitly then the standard SAX policy of
 * using the `org.xml.sax.driver` system property is used to
 * determine the implementation class of [XMLReader].
 * 
 * 
 * 
 * 
 * If the `org.xml.sax.driver` system property is not defined then
 * JAXP is used via reflection (so that DOM4J is not explicitly dependent on the
 * JAXP classes) to load the JAXP configured SAXParser. If there is any error
 * creating a JAXP SAXParser an informational message is output and then the
 * default (Aelfred) SAX parser is used instead.
 * 
 * 
 * 
 * 
 * If you are trying to use JAXP to explicitly set your SAX parser and are
 * experiencing problems, you can turn on verbose error reporting by defining
 * the system property `org.dom4j.verbose` to be "true" which will
 * output a more detailed description of why JAXP could not find a SAX parser
 * 
 * 
 * 
 * 
 * For more information on JAXP please go to [Sun's Java &amp; XML site ](http://java.sun.com/xml/)
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.58 $
 */
open class SAXReader {
    /** `DocumentFactory` used to create new document objects  */
    private var factory: DocumentFactory? = null

    /** `XMLReader` used to parse the SAX events  */
    private var xmlReader: XMLReader? = null

    /**
     * DOCUMENT ME!
     * 
     * @return the validation mode, true if validating will be done otherwise
     * false.
     */
    /** Whether validation should occur  */
    var isValidating: Boolean = false
        private set

    /** DispatchHandler to call when each `Element` is encountered  */
    private var dispatchHandler: DispatchHandler? = null

    /**
     * DOCUMENT ME!
     * 
     * @return the `ErrorHandler` used by SAX
     */
    /**
     * Sets the `ErrorHandler` used by the SAX
     * `XMLReader`.
     * 
     * @param errorHandler
     * is the `ErrorHandler` used by SAX
     */
    /** ErrorHandler class to use  */
    var errorHandler: ErrorHandler? = null

    /**
     * Returns the current entity resolver used to resolve entities
     * 
     * @return DOCUMENT ME!
     */
    /**
     * Sets the entity resolver used to resolve entities.
     * 
     * @param entityResolver
     * DOCUMENT ME!
     */
    /** The entity resolver  */
    var entityResolver: EntityResolver? = null

    /**
     * Sets whether String interning is enabled or disabled for element &
     * attribute names and namespace URIs. This proprety is enabled by default.
     * 
     * @return DOCUMENT ME!
     */
    /**
     * Sets whether String interning is enabled or disabled for element &
     * attribute names and namespace URIs
     * 
     * @param stringInternEnabled
     * DOCUMENT ME!
     */
    /** Should element & attribute names and namespace URIs be interned?  */
    var isStringInternEnabled: Boolean = true

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

    /**
     * Returns encoding used for InputSource (null means system default
     * encoding)
     * 
     * @return encoding used for InputSource
     */
    /**
     * Sets encoding used for InputSource (null means system default encoding)
     * 
     * @param encoding
     * is encoding used for InputSource
     */
    /** Encoding of InputSource - null means system default encoding  */
    var encoding: String? = null

    // private boolean includeExternalGeneralEntities = false;
    // private boolean includeExternalParameterEntities = false;
    /**
     * Returns the SAX filter being used to filter SAX events.
     * 
     * @return the SAX filter being used or null if no SAX filter is installed
     */
    /**
     * Sets the SAX filter to be used when filtering SAX events
     * 
     * @param filter
     * is the SAX filter to use or null to disable filtering
     */
    /** The SAX filter used to filter SAX events  */
    var xMLFilter: XMLFilter? = null

    constructor()

    constructor(validating: Boolean) {
        this.isValidating = validating
    }

    constructor(factory: DocumentFactory?) {
        this.factory = factory
    }

    constructor(factory: DocumentFactory?, validating: Boolean) {
        this.factory = factory
        this.isValidating = validating
    }

    constructor(xmlReader: XMLReader?) {
        this.xmlReader = xmlReader
    }

    constructor(xmlReader: XMLReader?, validating: Boolean) {
        this.xmlReader = xmlReader
        this.isValidating = validating
    }

    constructor(xmlReaderClassName: String?) {
        if (xmlReaderClassName != null) {
            this.xmlReader = XMLReaderFactory.createXMLReader(xmlReaderClassName)
        }
    }

    constructor(xmlReaderClassName: String?, validating: Boolean) {
        if (xmlReaderClassName != null) {
            this.xmlReader = XMLReaderFactory.createXMLReader(xmlReaderClassName)
        }

        this.isValidating = validating
    }

    /**
     * Allows a SAX property to be set on the underlying SAX parser. This can be
     * useful to set parser-specific properties such as the location of schema
     * or DTD resources. Though use this method with caution as it has the
     * possibility of breaking the standard behaviour. An alternative to calling
     * this method is to correctly configure an XMLReader object instance and
     * call the [.setXMLReader]method
     * 
     * @param name
     * is the SAX property name
     * @param value
     * is the value of the SAX property
     * 
     * @throws SAXException
     * if the XMLReader could not be created or the property could
     * not be changed.
     */
    @Throws(SAXException::class)
    fun setProperty(name: String?, value: Any?) {
        this.xMLReader!!.setProperty(name, value)
    }

    /**
     * Sets a SAX feature on the underlying SAX parser. This can be useful to
     * set parser-specific features. Though use this method with caution as it
     * has the possibility of breaking the standard behaviour. An alternative to
     * calling this method is to correctly configure an XMLReader object
     * instance and call the [.setXMLReader]method
     * 
     * @param name
     * is the SAX feature name
     * @param value
     * is the value of the SAX feature
     * 
     * @throws SAXException
     * if the XMLReader could not be created or the feature could
     * not be changed.
     */
    @Throws(SAXException::class)
    fun setFeature(name: String?, value: Boolean) {
        this.xMLReader!!.setFeature(name, value)
    }

    /**
     * 
     * 
     * Reads a Document from the given `File`
     * 
     * 
     * @param file
     * is the `File` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(file: File): Document? {
        try {
            /*
             * We cannot convert the file to an URL because if the filename
             * contains '#' characters, there will be problems with the URL in
             * the InputSource (because a URL like
             * http://myhost.com/index#anchor is treated the same as
             * http://myhost.com/index) Thanks to Christian Oetterli
             */
            val source = InputSource(FileInputStream(file))
            if (this.encoding != null) {
                source.setEncoding(this.encoding)
            }
            var path = file.getAbsolutePath()

            if (path != null) {
                // Code taken from Ant FileUtils
                val sb = StringBuffer("file://")

                // add an extra slash for filesystems with drive-specifiers
                if (!path.startsWith(File.separator)) {
                    sb.append("/")
                }

                path = path.replace('\\', '/')
                sb.append(path)

                source.setSystemId(sb.toString())
            }

            return read(source)
        } catch (e: FileNotFoundException) {
            throw DocumentException(e.message, e)
        }
    }

    /**
     * 
     * 
     * Reads a Document from the given `URL` using SAX
     * 
     * 
     * @param url
     * `URL` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(url: URL): Document? {
        val systemID = url.toExternalForm()

        val source = InputSource(systemID)
        if (this.encoding != null) {
            source.setEncoding(this.encoding)
        }

        return read(source)
    }

    /**
     * 
     * 
     * Reads a Document from the given URL or filename using SAX.
     * 
     * 
     * 
     * 
     * If the systemId contains a `':'` character then it is
     * assumed to be a URL otherwise its assumed to be a file name. If you want
     * finer grained control over this mechansim then please explicitly pass in
     * either a [URL]or a [File]instance instead of a [ ] to denote the source of the document.
     * 
     * 
     * @param systemId
     * is a URL for a document or a file name.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(systemId: String?): Document? {
        val source = InputSource(systemId)
        if (this.encoding != null) {
            source.setEncoding(this.encoding)
        }

        return read(source)
    }

    /**
     * 
     * 
     * Reads a Document from the given stream using SAX
     * 
     * 
     * @param in
     * `InputStream` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(`in`: InputStream?): Document? {
        val source = InputSource(`in`)
        if (this.encoding != null) {
            source.setEncoding(this.encoding)
        }

        return read(source)
    }

    /**
     * 
     * 
     * Reads a Document from the given `Reader` using SAX
     * 
     * 
     * @param reader
     * is the reader for the input
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(reader: Reader?): Document? {
        val source = InputSource(reader)
        if (this.encoding != null) {
            source.setEncoding(this.encoding)
        }

        return read(source)
    }

    /**
     * 
     * 
     * Reads a Document from the given stream using SAX
     * 
     * 
     * @param in
     * `InputStream` to read from.
     * @param systemId
     * is the URI for the input
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(`in`: InputStream?, systemId: String?): Document? {
        val source = InputSource(`in`)
        source.setSystemId(systemId)
        if (this.encoding != null) {
            source.setEncoding(this.encoding)
        }

        return read(source)
    }

    /**
     * 
     * 
     * Reads a Document from the given `Reader` using SAX
     * 
     * 
     * @param reader
     * is the reader for the input
     * @param systemId
     * is the URI for the input
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(reader: Reader?, systemId: String?): Document? {
        val source = InputSource(reader)
        source.setSystemId(systemId)
        if (this.encoding != null) {
            source.setEncoding(this.encoding)
        }

        return read(source)
    }

    /**
     * 
     * 
     * Reads a Document from the given `InputSource` using SAX
     * 
     * 
     * @param in
     * `InputSource` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * if an error occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun read(`in`: InputSource): Document? {
        try {
            var reader: XMLReader = this.xMLReader!!

            reader = installXMLFilter(reader)

            var thatEntityResolver = this.entityResolver

            if (thatEntityResolver == null) {
                thatEntityResolver = createDefaultEntityResolver(`in`.getSystemId())
                this.entityResolver = thatEntityResolver
            }

            reader.setEntityResolver(thatEntityResolver)

            val contentHandler = createContentHandler(reader)
            contentHandler.entityResolver = thatEntityResolver
            contentHandler.inputSource = `in`

            val internal = this.isIncludeInternalDTDDeclarations
            val external = this.isIncludeExternalDTDDeclarations

            contentHandler.isIncludeInternalDTDDeclarations = internal
            contentHandler.isIncludeExternalDTDDeclarations = external
            contentHandler.isMergeAdjacentText = this.isMergeAdjacentText
            contentHandler.isStripWhitespaceText = this.isStripWhitespaceText
            contentHandler.isIgnoreComments = this.isIgnoreComments
            reader.setContentHandler(contentHandler)

            configureReader(reader, contentHandler)

            reader.parse(`in`)

            return contentHandler.getDocument()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is SAXParseException) {
                // e.printStackTrace();
                val parseException = e
                var systemId = parseException.getSystemId()

                if (systemId == null) {
                    systemId = ""
                }

                val message = ("Error on line " + parseException.getLineNumber()
                        + " of document " + systemId + " : " + parseException.message)

                throw DocumentException(message, e)
            } else {
                throw DocumentException(e.message, e)
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------

    /**
     * Sets the validation mode.
     * 
     * @param validation
     * indicates whether or not validation should occur.
     */
    fun setValidation(validation: Boolean) {
        this.isValidating = validation
    }

    var documentFactory: DocumentFactory?
        /**
         * DOCUMENT ME!
         * 
         * @return the `DocumentFactory` used to create document
         * objects
         */
        get() {
            if (factory == null) {
                factory = DocumentFactory.Companion.getInstance()
            }

            return factory
        }
        /**
         * 
         * 
         * This sets the `DocumentFactory` used to create new
         * documents. This method allows the building of custom DOM4J tree objects
         * to be implemented easily using a custom derivation of
         * [DocumentFactory]
         * 
         * 
         * @param documentFactory
         * `DocumentFactory` used to create DOM4J objects
         */
        set(documentFactory) {
            this.factory = documentFactory
        }

    @get:Throws(SAXException::class)
    var xMLReader: XMLReader?
        /**
         * DOCUMENT ME!
         * 
         * @return the `XMLReader` used to parse SAX events
         * 
         * @throws SAXException
         * DOCUMENT ME!
         */
        get() {
            if (xmlReader == null) {
                xmlReader = createXMLReader()
            }

            return xmlReader
        }
        /**
         * Sets the `XMLReader` used to parse SAX events
         * 
         * @param reader
         * is the `XMLReader` to parse SAX events
         */
        set(reader) {
            this.xmlReader = reader
        }

    /**
     * Sets the class name of the `XMLReader` to be used to parse
     * SAX events.
     * 
     * @param xmlReaderClassName
     * is the class name of the `XMLReader` to parse SAX
     * events
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    fun setXMLReaderClassName(xmlReaderClassName: String?) {
        this.xMLReader = XMLReaderFactory.createXMLReader(xmlReaderClassName)
    }

    /**
     * Adds the `ElementHandler` to be called when the specified
     * path is encounted.
     * 
     * @param path
     * is the path to be handled
     * @param handler
     * is the `ElementHandler` to be called by the event
     * based processor.
     */
    fun addHandler(path: String?, handler: ElementHandler?) {
        getDispatchHandler().addHandler(path, handler)
    }

    /**
     * Removes the `ElementHandler` from the event based processor,
     * for the specified path.
     * 
     * @param path
     * is the path to remove the `ElementHandler` for.
     */
    fun removeHandler(path: String?) {
        getDispatchHandler().removeHandler(path)
    }

    /**
     * When multiple `ElementHandler` instances have been
     * registered, this will set a default `ElementHandler` to be
     * called for any path which does **NOT ** have a handler registered.
     * 
     * @param handler
     * is the `ElementHandler` to be called by the event
     * based processor.
     */
    fun setDefaultHandler(handler: ElementHandler?) {
        getDispatchHandler().setDefaultHandler(handler)
    }

    /**
     * This method clears out all the existing handlers and default handler
     * setting things back as if no handler existed. Useful when reusing an
     * object instance.
     */
    fun resetHandlers() {
        getDispatchHandler().resetHandlers()
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    /**
     * Installs any XMLFilter objects required to allow the SAX event stream to
     * be filtered and preprocessed before it gets to dom4j.
     * 
     * @param reader
     * DOCUMENT ME!
     * 
     * @return the new XMLFilter if applicable or the original XMLReader if no
     * filter is being used.
     */
    protected fun installXMLFilter(reader: XMLReader): XMLReader {
        val filter: XMLFilter? = this.xMLFilter

        if (filter != null) {
            // find the root XMLFilter
            var root: XMLFilter? = filter

            while (true) {
                val parent = root!!.getParent()

                if (parent is XMLFilter) {
                    root = parent
                } else {
                    break
                }
            }

            root.setParent(reader)

            return filter
        }

        return reader
    }

    protected fun getDispatchHandler(): DispatchHandler {
        if (dispatchHandler == null) {
            dispatchHandler = DispatchHandler()
        }

        return dispatchHandler!!
    }

    fun setDispatchHandler(dispatchHandler: DispatchHandler?) {
        this.dispatchHandler = dispatchHandler
    }

    /**
     * Factory Method to allow alternate methods of creating and configuring
     * XMLReader objects
     * 
     * @return DOCUMENT ME!
     * 
     * @throws SAXException
     * DOCUMENT ME!
     */
    @Throws(SAXException::class)
    protected fun createXMLReader(): XMLReader {
        return SAXHelper.createXMLReader(this.isValidating)
    }

    /**
     * Configures the XMLReader before use
     * 
     * @param reader
     * DOCUMENT ME!
     * @param handler
     * DOCUMENT ME!
     * 
     * @throws DocumentException
     * DOCUMENT ME!
     */
    @Throws(DocumentException::class)
    protected fun configureReader(reader: XMLReader, handler: DefaultHandler?) {
        // configure lexical handling
        SAXHelper.setParserProperty(reader, SAX_LEXICALHANDLER, handler)

        // try alternate property just in case
        SAXHelper.setParserProperty(reader, SAX_LEXICAL_HANDLER, handler)

        // register the DeclHandler
        if (this.isIncludeInternalDTDDeclarations || this.isIncludeExternalDTDDeclarations) {
            SAXHelper.setParserProperty(reader, SAX_DECL_HANDLER, handler)
        }

        // configure namespace support
        SAXHelper.setParserFeature(reader, SAX_NAMESPACES, true)

        SAXHelper.setParserFeature(reader, SAX_NAMESPACE_PREFIXES, false)

        // string interning
        SAXHelper.setParserFeature(
            reader, SAX_STRING_INTERNING,
            this.isStringInternEnabled
        )

        // external entites
        /*
         * SAXHelper.setParserFeature( reader,
         * "http://xml.org/sax/properties/external-general-entities",
         * includeExternalGeneralEntities ); SAXHelper.setParserFeature( reader,
         * "http://xml.org/sax/properties/external-parameter-entities",
         * includeExternalParameterEntities );
         */
        // use Locator2 if possible
        SAXHelper.setParserFeature(reader, "http://xml.org/sax/features/use-locator2", true)

        try {
            // configure validation support
            reader.setFeature("http://xml.org/sax/features/validation", this.isValidating)

            if (errorHandler != null) {
                reader.setErrorHandler(errorHandler)
            } else {
                reader.setErrorHandler(handler)
            }
        } catch (e: Exception) {
            if (this.isValidating) {
                throw DocumentException(
                    ("Validation not supported for" + " XMLReader: "
                            + reader), e
                )
            }
        }
    }

    /**
     * Factory Method to allow user derived SAXContentHandler objects to be used
     * 
     * @param reader
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected open fun createContentHandler(reader: XMLReader?): SAXContentHandler {
        return SAXContentHandler(this.documentFactory!!, dispatchHandler)
    }

    protected fun createDefaultEntityResolver(systemId: String?): EntityResolver {
        var prefix: String? = null

        if ((systemId != null) && (systemId.length > 0)) {
            val idx = systemId.lastIndexOf('/')

            if (idx > 0) {
                prefix = systemId.substring(0, idx + 1)
            }
        }

        return SAXEntityResolver(prefix)
    }

    protected class SAXEntityResolver(protected var uriPrefix: String?) : EntityResolver,
        Serializable {
        override fun resolveEntity(publicId: String?, systemId: String?): InputSource {
            // try create a relative URI reader...
            var systemId = systemId
            if ((systemId != null) && (systemId.length > 0)) {
                if ((uriPrefix != null) && (systemId.indexOf(':') <= 0)) {
                    systemId = uriPrefix + systemId
                }
            }

            return InputSource(systemId)
        }
    }

    companion object {
        private const val SAX_STRING_INTERNING = "http://xml.org/sax/features/string-interning"
        private const val SAX_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes"
        private const val SAX_NAMESPACES = "http://xml.org/sax/features/namespaces"
        private const val SAX_DECL_HANDLER = "http://xml.org/sax/properties/declaration-handler"
        private const val SAX_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler"
        private const val SAX_LEXICALHANDLER = "http://xml.org/sax/handlers/LexicalHandler"
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


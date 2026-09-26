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
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

/**
 * The SAXModifier reads, modifies and writes XML documents using SAX.
 * 
 * 
 * 
 * Registered [ElementModifier]objects can provide modifications to (part
 * of) the xml tree, while the document is still being processed. This makes it
 * possible to change large xml documents without having them in memory.
 * 
 * 
 * 
 * 
 * The modified document is written when the [XMLWriter]is specified.
 * 
 * 
 * @author Wonne Keysers (Realsoftware.be)
 * 
 * @see org.dom4j.io.SAXReader
 * 
 * @see org.dom4j.io.XMLWriter
 */
class SAXModifier {
    /**
     * Returns the current [XMLWriter].
     * 
     * @return XMLWriter
     */
    /**
     * Sets the [XMLWriter]used to write the modified document.
     * 
     * @param writer
     * The writer to use.
     */
    var xMLWriter: XMLWriter? = null

    private var xmlReader: XMLReader? = null

    /**
     * Returns true when xml elements are not kept in memory while parsing. The
     * [org.dom4j.Document]returned by the modify methods will be null.
     * 
     * @return Returns the pruneElements.
     */
    var isPruneElements: Boolean = false
        private set

    private var modifyReader: SAXModifyReader? = null

    private val modifiers: HashMap<String?, ElementModifier?> = HashMap<String?, ElementModifier?>()

    /**
     * Creates a new modifier. <br></br>
     * The XMLReader to parse the source will be created via the
     * org.xml.sax.driver system property or JAXP if the system property is not
     * set.
     */
    constructor()

    /**
     * Creates a new modifier. <br></br>
     * The XMLReader to parse the source will be created via the
     * org.xml.sax.driver system property or JAXP if the system property is not
     * set.
     * 
     * @param pruneElements
     * Set to true when the modified document must NOT be kept in
     * memory.
     */
    constructor(pruneElements: Boolean) {
        this.isPruneElements = pruneElements
    }

    /**
     * Creates a new modifier that will the specified [ ] to parse the source.
     * 
     * @param xmlReader
     * The XMLReader to use
     */
    constructor(xmlReader: XMLReader?) {
        this.xmlReader = xmlReader
    }

    /**
     * Creates a new modifier that will the specified [ ] to parse the source.
     * 
     * @param xmlReader
     * The XMLReader to use
     * @param pruneElements
     * Set to true when the modified document must NOT be kept in
     * memory.
     */
    constructor(xmlReader: XMLReader?, pruneElements: Boolean) {
        this.xmlReader = xmlReader
    }

    /**
     * Reads a Document from the given [File]and writes it to the
     * specified [XMLWriter]using SAX. Registered {@linkElementModifier}
     * objects are invoked on the fly.
     * 
     * @param source
     * is the `File` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: File?): Document? {
        try {
            return installModifyReader().read(source!!)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Reads a Document from the given [InputSource]and
     * writes it to the specified [XMLWriter]using SAX. Registered
     * [ElementModifier]objects are invoked on the fly.
     * 
     * @param source
     * is the `org.xml.sax.InputSource` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: InputSource?): Document? {
        try {
            return installModifyReader().read(source!!)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Reads a Document from the given [InputStream]and writes it
     * to the specified [XMLWriter]using SAX. Registered [ ] objects are invoked on the fly.
     * 
     * @param source
     * is the `java.io.InputStream` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: InputStream?): Document? {
        try {
            return installModifyReader().read(source)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Reads a Document from the given [InputStream]and writes it
     * to the specified [XMLWriter]using SAX. Registered [ ] objects are invoked on the fly.
     * 
     * @param source
     * is the `java.io.InputStream` to read from.
     * @param systemId
     * DOCUMENT ME!
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: InputStream?, systemId: String?): Document? {
        try {
            return installModifyReader().read(source)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Reads a Document from the given [Reader]and writes it to
     * the specified [XMLWriter]using SAX. Registered [ ] objects are invoked on the fly.
     * 
     * @param source
     * is the `java.io.Reader` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: Reader?): Document? {
        try {
            return installModifyReader().read(source)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Reads a Document from the given [Reader]and writes it to
     * the specified [XMLWriter]using SAX. Registered [ ] objects are invoked on the fly.
     * 
     * @param source
     * is the `java.io.Reader` to read from.
     * @param systemId
     * DOCUMENT ME!
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: Reader?, systemId: String?): Document? {
        try {
            return installModifyReader().read(source)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Reads a Document from the given [URL]and writes it to the
     * specified [XMLWriter]using SAX. Registered {@linkElementModifier}
     * objects are invoked on the fly.
     * 
     * @param source
     * is the `java.net.URL` to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: URL): Document? {
        try {
            return installModifyReader().read(source)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Reads a Document from the given URL or filename and writes it to the
     * specified [XMLWriter]using SAX. Registered {@linkElementModifier}
     * objects are invoked on the fly.
     * 
     * @param source
     * is the URL or filename to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     * DocumentException org.dom4j.DocumentException} if an error
     * occurs during parsing.
     */
    @Throws(DocumentException::class)
    fun modify(source: String?): Document? {
        try {
            return installModifyReader().read(source)
        } catch (ex: SAXModifyException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Adds the [ElementModifier]to be called when the specified element
     * path is encounted while parsing the source.
     * 
     * @param path
     * The element path to be handled
     * @param modifier
     * The [ElementModifier]to be called by the event based
     * processor.
     */
    fun addModifier(path: String?, modifier: ElementModifier?) {
        this.modifiers.put(path, modifier)
    }

    /**
     * Removes all registered [ElementModifier]instances from the event
     * based processor.
     */
    fun resetModifiers() {
        this.modifiers.clear()
        this.sAXModifyReader.resetHandlers()
    }

    /**
     * Removes the [ElementModifier]from the event based processor, for
     * the specified element path.
     * 
     * @param path
     * The path to remove the [ElementModifier]for.
     */
    fun removeModifier(path: String?) {
        this.modifiers.remove(path)
        this.sAXModifyReader.removeHandler(path)
    }

    var documentFactory: DocumentFactory?
        /**
         * Get the [org.dom4j.DocumentFactory]used to create the DOM4J
         * document structure
         * 
         * @return `DocumentFactory` that will be used
         */
        get() = this.sAXModifyReader.documentFactory
        /**
         * Sets the [org.dom4j.DocumentFactory]used to create the DOM4J
         * document tree.
         * 
         * @param factory
         * `DocumentFactory` to be used
         */
        set(factory) {
            this.sAXModifyReader.documentFactory = factory
        }

    @Throws(DocumentException::class)
    private fun installModifyReader(): SAXReader {
        try {
            val reader: SAXModifyReader = this.sAXModifyReader

            if (this.isPruneElements) {
                modifyReader!!.setDispatchHandler(PruningDispatchHandler())
            }

            reader.resetHandlers()

            val modifierIt = this.modifiers.entries.iterator()

            while (modifierIt.hasNext()) {
                val entry = modifierIt.next() as MutableMap.MutableEntry<*, *>

                val handler = SAXModifyElementHandler(
                    entry.value as ElementModifier
                )
                reader.addHandler(entry.key as String?, handler)
            }

            reader.xMLWriter = this.xMLWriter
            reader.xMLReader = this.xMLReader

            return reader
        } catch (ex: SAXException) {
            throw DocumentException(ex.message, ex)
        }
    }

    @get:Throws(SAXException::class)
    private val xMLReader: XMLReader
        get() {
            if (this.xmlReader == null) {
                xmlReader = SAXHelper.createXMLReader(false)
            }

            return this.xmlReader!!
        }

    private val sAXModifyReader: SAXModifyReader
        get() {
            if (modifyReader == null) {
                modifyReader = SAXModifyReader()
            }

            return modifyReader!!
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


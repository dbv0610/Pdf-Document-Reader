/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.jaxb

import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentException
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.io.ElementModifier
import com.wxiwei.office.fc.dom4j.io.OutputFormat
import com.wxiwei.office.fc.dom4j.io.SAXModifier
import com.wxiwei.office.fc.dom4j.io.XMLWriter
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.net.URL
import java.nio.charset.Charset

/**
 * Reads an XML document using SAX and writes its content to the provided
 * [org.dom4j.io.XMLWriter]. Modifications must be provided by [ ] objects, which are called prior to writing
 * the XML fragment they are registered for.
 * 
 * @author Wonne Keysers (Realsoftware.be)
 * 
 * @see org.dom4j.io.SAXModifier
 */
class JAXBModifier : JAXBSupport {
    private var modifier: SAXModifier? = null

    private var xMLWriter: XMLWriter? = null

    /**
     * Returns true when the modified [org.dom4j.Document]is not kept in
     * memory.
     * 
     * @return Returns true if elements are pruned.
     */
    /**
     * Define whether the modified [org.dom4j.Document]must only be
     * written to the output and pruned from the DOM4J tree.
     * 
     * @param pruneElements
     * When true, elements will not be kept in memory
     */
    var isPruneElements: Boolean = false

    private val outputFormat: OutputFormat

    private val modifiers: HashMap<String?, JAXBObjectModifier?> = HashMap<String?, JAXBObjectModifier?>()

    /**
     * Creates a new JAXBModifier for the given JAXB context path. This is the
     * Java package where JAXB can find the generated XML classes. This package
     * MUST contain jaxb.properties!
     * 
     * @param contextPath
     * JAXB context path to be used
     * 
     * @see javax.xml.bind.JAXBContext
     */
    constructor(contextPath: String?) : super(contextPath) {
        this.outputFormat = OutputFormat()
    }

    /**
     * Creates a new JAXBModifier for the given JAXB context path, using the
     * given [ClassLoader]. This is the Java package where JAXB
     * can find the generated XML classes. This package MUST contain
     * jaxb.properties!
     * 
     * @param contextPath
     * JAXB context path to be used
     * @param classloader
     * the classloader to use
     * 
     * @see javax.xml.bind.JAXBContext
     */
    constructor(contextPath: String?, classloader: ClassLoader?) : super(contextPath, classloader) {
        this.outputFormat = OutputFormat()
    }

    /**
     * Creates a new JAXBModifier for the given JAXB context path. The specified
     * [org.dom4j.io.OutputFormat]will be used while writing the XML
     * stream.
     * 
     * @param contextPath
     * JAXB context path to be used
     * @param outputFormat
     * the DOM4J [org.dom4j.io.OutputFormat]to be used
     * 
     * @see javax.xml.bind.JAXBContext
     */
    constructor(contextPath: String?, outputFormat: OutputFormat) : super(contextPath) {
        this.outputFormat = outputFormat
    }

    /**
     * Creates a new JAXBModifier for the given JAXB context path, using the
     * specified [java.lang.Classloader]. The specified [ ] will be used while writing the XML stream.
     * 
     * @param contextPath
     * JAXB context path to be used
     * @param classloader
     * the class loader to be used to load JAXB
     * @param outputFormat
     * the DOM4J [org.dom4j.io.OutputFormat]to be used
     * 
     * @see javax.xml.bind.JAXBContext
     */
    constructor(
        contextPath: String?,
        classloader: ClassLoader?,
        outputFormat: OutputFormat
    ) : super(contextPath, classloader) {
        this.outputFormat = outputFormat
    }

    /**
     * Parses the specified [File]with SAX
     * 
     * @param source
     * the file to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(source: File?): Document? {
        return installModifier()!!.modify(source)
    }

    /**
     * Parses the specified [File]with SAX, using the given
     * [Charset].
     * 
     * @param source
     * the file to parse
     * @param charset
     * the character set to use
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(source: File?, charset: Charset?): Document? {
        try {
            val reader: Reader = InputStreamReader(FileInputStream(source), charset)

            return installModifier()!!.modify(reader)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        } catch (ex: FileNotFoundException) {
            throw DocumentException(ex.message, ex)
        }
    }

    /**
     * Parses the specified [InputSource]with SAX.
     * 
     * @param source
     * the input source to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(source: InputSource?): Document? {
        try {
            return installModifier()!!.modify(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [InputStream]with SAX.
     * 
     * @param source
     * the inputstream to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(source: InputStream?): Document? {
        try {
            return installModifier()!!.modify(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [InputStream]with SAX.
     * 
     * @param source
     * the inputstream to parse
     * @param systemId
     * the URI of the given inputstream
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(source: InputStream?, systemId: String?): Document? {
        try {
            return installModifier()!!.modify(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [Reader]with SAX.
     * 
     * @param r
     * the reader to use for parsing
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(r: Reader?): Document? {
        try {
            return installModifier()!!.modify(r)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [Reader]with SAX.
     * 
     * @param source
     * the reader to parse
     * @param systemId
     * the URI of the given reader
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(source: Reader?, systemId: String?): Document? {
        try {
            return installModifier()!!.modify(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the the given URL or filename.
     * 
     * @param url
     * the URL or filename to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(url: String?): Document? {
        try {
            return installModifier()!!.modify(url)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the the given URL.
     * 
     * @param source
     * the URL to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     * @throws IOException
     * when an error occurs while writing to the [             ]
     */
    @Throws(DocumentException::class, IOException::class)
    fun modify(source: URL?): Document? {
        try {
            return installModifier()!!.modify(source!!)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Sets the Output to write the (modified) xml document to.
     * 
     * @param file
     * the [File]to write to
     * 
     * @throws IOException
     * when the file cannot be found or when the outputformat
     */
    @Throws(IOException::class)
    fun setOutput(file: File?) {
        createXMLWriter().setOutputStream(FileOutputStream(file))
    }

    /**
     * Sets the Output to write the (modified) xml document to.
     * 
     * @param outputStream
     * the [OutputStream]to write to
     * 
     * @throws IOException
     * when an error occurs
     */
    @Throws(IOException::class)
    fun setOutput(outputStream: OutputStream?) {
        createXMLWriter().setOutputStream(outputStream)
    }

    /**
     * Sets the Output to write the (modified) xml document to.
     * 
     * @param writer
     * the [Writer]to write to
     * 
     * @throws IOException
     * when an error occurs
     */
    @Throws(IOException::class)
    fun setOutput(writer: Writer?) {
        createXMLWriter().setWriter(writer!!)
    }

    /**
     * Adds the [JAXBObjectModifier]to be called when the specified xml
     * path is encounted while parsing the source.
     * 
     * @param path
     * the element path to listen for
     * @param mod
     * the modifier to register
     */
    fun addObjectModifier(path: String?, mod: JAXBObjectModifier?) {
        modifiers.put(path, mod)
    }

    /**
     * Removes the [JAXBObjectModifier]from the event based processor,
     * for the specified element path.
     * 
     * @param path
     * the xml path to remove the modifier for
     */
    fun removeObjectModifier(path: String?) {
        modifiers.remove(path)
        getModifier().removeModifier(path)
    }

    /**
     * Removes all registered [JAXBObjectModifier]instances from the
     * event based processor.
     */
    fun resetObjectModifiers() {
        modifiers.clear()
        getModifier().resetModifiers()
    }

    @Throws(IOException::class)
    private fun installModifier(): SAXModifier? {
        modifier = SAXModifier(this.isPruneElements)

        modifier!!.resetModifiers()

        val modifierIt = modifiers.entries.iterator()

        while (modifierIt.hasNext()) {
            val entry = modifierIt.next() as MutableMap.MutableEntry<*, *>
            val mod: ElementModifier = JAXBElementModifier(
                this,
                entry.value as JAXBObjectModifier?
            )
            getModifier().addModifier(entry.key as String?, mod)
        }

        modifier!!.xMLWriter = this.xMLWriter

        return modifier
    }

    private fun getModifier(): SAXModifier {
        if (this.modifier == null) {
            modifier = SAXModifier(this.isPruneElements)
        }

        return modifier!!
    }

    @Throws(IOException::class)
    private fun createXMLWriter(): XMLWriter {
        if (this.xMLWriter == null) {
            this.xMLWriter = XMLWriter(outputFormat)
        }

        return this.xMLWriter!!
    }

    private inner class JAXBElementModifier(
        private val jaxbModifier: JAXBModifier?,
        private val objectModifier: JAXBObjectModifier?
    ) : ElementModifier {
        @Throws(Exception::class)
        override fun modifyElement(element: Element?): Element? {
            /*javax.xml.bind.Element originalObject = jaxbModifier.unmarshal(element);
            javax.xml.bind.Element modifiedObject = objectModifier.modifyObject(originalObject);

            return jaxbModifier.marshal(modifiedObject);*/
            return null
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


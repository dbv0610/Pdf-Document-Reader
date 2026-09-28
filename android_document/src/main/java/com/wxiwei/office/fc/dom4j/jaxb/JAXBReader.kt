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
package com.wxiwei.office.fc.dom4j.jaxb

import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentException
import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.ElementPath
import com.wxiwei.office.fc.dom4j.io.SAXReader
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset

/**
 * Reads an XML document and creates a DOM4J tree from SAX parsing events.
 * [JAXBObjectHandler]objects can be registered to automatically receive
 * unmarshalled XML fragments. Registered {@linkorg.dom4j.ElementHandler}
 * implementations are notified when a certain element path is encountered
 * 
 * @author Wonne Keysers (Realsoftware.be)
 * 
 * @see org.dom4j.io.SAXReader
 * 
 * @see javax.xml.bind.JAXBContext
 */
class JAXBReader : JAXBSupport {
    private var reader: SAXReader? = null
        get() {
            if (field == null) {
                field = SAXReader()
            }

            return field
        }

    /**
     * When 'true', the DOM4J document will not be kept in memory while parsing.
     * 
     * @return Returns the pruneElements.
     */
    var isPruneElements: Boolean = false
        /**
         * Set to true when DOM4J elements must immediately be pruned from the tree.
         * The [Document]will not be available afterwards!
         * 
         * @param pruneElements
         */
        set(pruneElements) {
            field = pruneElements

            if (pruneElements) {
                this.reader!!.setDefaultHandler(PruningElementHandler())
            }
        }

    /**
     * Creates a new JAXBReader for the given JAXB context path. This is the
     * Java package where JAXB can find the generated XML classes. This package
     * MUST contain jaxb.properties!
     * 
     * @param contextPath
     * context path to be used
     * 
     * @see javax.xml.bind.JAXBContext
     */
    constructor(contextPath: String?) : super(contextPath)

    /**
     * Creates a new JAXBReader for the given JAXB context path, using the
     * specified [java.lang.Classloader]. This is the Java package where
     * JAXB can find the generated XML classes. This package MUST contain
     * jaxb.properties!
     * 
     * @param contextPath
     * to be used
     * @param classloader
     * to be used
     * 
     * @see javax.xml.bind.JAXBContext
     */
    constructor(contextPath: String?, classloader: ClassLoader?) : super(contextPath, classloader)

    /**
     * Parses the specified [File]
     * 
     * @param source
     * the file to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(source: File?): Document? {
        return this.reader!!.read(source!!)
    }

    /**
     * Parses the specified [File], using the given [ ].
     * 
     * @param file
     * the file to parse
     * @param charset
     * the charset to be used
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(file: File?, charset: Charset?): Document? {
        try {
            val xmlReader: Reader = InputStreamReader(FileInputStream(file), charset)

            return this.reader!!.read(xmlReader)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        } catch (ex: FileNotFoundException) {
            throw DocumentException(ex.message, ex)
        }
    }

    /**
     * Parses the specified [InputSource]
     * 
     * @param source
     * the source to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(source: InputSource?): Document? {
        try {
            return this.reader!!.read(source!!)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [InputStream]
     * 
     * @param source
     * the input stream to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(source: InputStream?): Document? {
        try {
            return this.reader!!.read(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [InputStream]
     * 
     * @param source
     * the input stream to parse
     * @param systemId
     * is the URI for the input
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(source: InputStream?, systemId: String?): Document? {
        try {
            return this.reader!!.read(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [Reader]
     * 
     * @param source
     * the input reader to use
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(source: Reader?): Document? {
        try {
            return this.reader!!.read(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the specified [Reader]
     * 
     * @param source
     * the input reader to parse
     * @param systemId
     * is the URI for the input
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(source: Reader?, systemId: String?): Document? {
        try {
            return this.reader!!.read(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Parses the the given URL or filename.
     * 
     * @param source
     * the location to parse
     * 
     * @return the resulting DOM4J document
     * 
     * @throws DocumentException
     * when an error occurs while parsing
     */
    @Throws(DocumentException::class)
    fun read(source: String?): Document? {
        try {
            return this.reader!!.read(source)
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
     */
    @Throws(DocumentException::class)
    fun read(source: URL): Document? {
        try {
            return this.reader!!.read(source)
        } catch (ex: JAXBRuntimeException) {
            val cause = ex.cause
            throw DocumentException(cause!!.message, cause)
        }
    }

    /**
     * Registers a [JAXBObjectHandler]that will be supplied with the
     * unmarshalled representation of the xml fragment whenever the specified
     * path is encounted.
     * 
     * @param path
     * the path to listen for
     * @param handler
     * the handler to be notified
     */
    fun addObjectHandler(path: String?, handler: JAXBObjectHandler?) {
        val eHandler: ElementHandler = UnmarshalElementHandler(this, handler)
        this.reader!!.addHandler(path, eHandler)
    }

    /**
     * Removes the [JAXBObjectHandler]from the event based processor, for
     * the specified element path.
     * 
     * @param path
     * The path to remove the [JAXBObjectHandler]for
     */
    fun removeObjectHandler(path: String?) {
        this.reader!!.removeHandler(path)
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
        this.reader!!.addHandler(path, handler)
    }

    /**
     * Removes the `ElementHandler` from the event based processor,
     * for the specified path.
     * 
     * @param path
     * is the path to remove the `ElementHandler` for.
     */
    fun removeHandler(path: String?) {
        this.reader!!.removeHandler(path)
    }

    /**
     * Removes all registered [JAXBObjectHandler]and [ ] instances from the event based processor.
     */
    fun resetHandlers() {
        this.reader!!.resetHandlers()
    }

    private inner class UnmarshalElementHandler(
        private val jaxbReader: JAXBReader?,
        private val handler: JAXBObjectHandler?
    ) : ElementHandler {
        override fun onStart(elementPath: ElementPath?) {
        }

        override fun onEnd(elementPath: ElementPath?) {
            /*try
            {
                org.dom4j.Element elem = elementPath.getCurrent();

                javax.xml.bind.Element jaxbObject = (javax.xml.bind.Element)jaxbReader
                    .unmarshal(elem);

                if (jaxbReader.isPruneElements())
                {
                    elem.detach();
                }

                handler.handleObject(jaxbObject);
            }
            catch(Exception ex)
            {
                throw new JAXBRuntimeException(ex);
            }*/
        }
    }

    private inner class PruningElementHandler : ElementHandler {
        override fun onStart(parm1: ElementPath?) {
        }

        override fun onEnd(elementPath: ElementPath?) {
            var elem = elementPath?.current
            elem?.detach()
            elem = null
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


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

import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.ElementHandler
import org.xml.sax.Attributes
import org.xml.sax.Locator
import org.xml.sax.SAXException
import java.io.IOException

/**
 * This extension of the SAXContentHandler writes SAX events immediately to the
 * provided XMLWriter, unless some [org.dom4.ElementHandler]is still
 * handling the current Element.
 * 
 * @author Wonne Keysers (Realsoftware.be)
 * 
 * @see org.dom4j.io.SAXContentHandler
 */
internal class SAXModifyContentHandler : SAXContentHandler {
    var xMLWriter: XMLWriter? = null

    constructor()

    constructor(documentFactory: DocumentFactory?) : super(documentFactory!!)

    constructor(documentFactory: DocumentFactory?, elementHandler: ElementHandler?) : super(
        documentFactory!!,
        elementHandler
    )

    constructor(
        documentFactory: DocumentFactory?, elementHandler: ElementHandler?,
        elementStack: ElementStack?
    ) : super(documentFactory!!, elementHandler, elementStack)

    @Throws(SAXException::class)
    override fun startCDATA() {
        super.startCDATA()

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.startCDATA()
        }
    }

    @Throws(SAXException::class)
    override fun startDTD(name: String?, publicId: String?, systemId: String?) {
        super.startDTD(name, publicId, systemId)

        if (this.xMLWriter != null) {
            xMLWriter!!.startDTD(name, publicId, systemId)
        }
    }

    @Throws(SAXException::class)
    override fun endDTD() {
        super.endDTD()

        if (this.xMLWriter != null) {
            xMLWriter!!.endDTD()
        }
    }

    @Throws(SAXException::class)
    override fun comment(characters: CharArray?, parm2: Int, parm3: Int) {
        super.comment(characters, parm2, parm3)

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.comment(characters, parm2, parm3)
        }
    }

    @Throws(SAXException::class)
    override fun startEntity(name: String?) {
        super.startEntity(name)

        if (this.xMLWriter != null) {
            xMLWriter!!.startEntity(name)
        }
    }

    @Throws(SAXException::class)
    override fun endCDATA() {
        super.endCDATA()

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.endCDATA()
        }
    }

    @Throws(SAXException::class)
    override fun endEntity(name: String?) {
        super.endEntity(name)

        if (this.xMLWriter != null) {
            xMLWriter!!.endEntity(name)
        }
    }

    @Throws(SAXException::class)
    override fun unparsedEntityDecl(
        name: String?,
        publicId: String?,
        systemId: String?,
        notation: String?
    ) {
        super.unparsedEntityDecl(name, publicId, systemId, notation)

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.unparsedEntityDecl(name, publicId, systemId, notation)
        }
    }

    @Throws(SAXException::class)
    override fun notationDecl(name: String?, publicId: String?, systemId: String?) {
        super.notationDecl(name, publicId, systemId)

        if (this.xMLWriter != null) {
            xMLWriter!!.notationDecl(name, publicId, systemId)
        }
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String?, localName: String?, qName: String?, atts: Attributes) {
        super.startElement(uri, localName, qName, atts)

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.startElement(uri, localName, qName, atts)
        }
    }

    @Throws(SAXException::class)
    override fun startDocument() {
        super.startDocument()

        if (this.xMLWriter != null) {
            xMLWriter!!.startDocument()
        }
    }

    @Throws(SAXException::class)
    override fun ignorableWhitespace(parm1: CharArray?, parm2: Int, parm3: Int) {
        super.ignorableWhitespace(parm1, parm2, parm3)

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.ignorableWhitespace(parm1, parm2, parm3)
        }
    }

    @Throws(SAXException::class)
    override fun processingInstruction(target: String?, data: String?) {
        super.processingInstruction(target, data)

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.processingInstruction(target, data)
        }
    }

    override fun setDocumentLocator(locator: Locator?) {
        super.setDocumentLocator(locator)

        if (this.xMLWriter != null) {
            xMLWriter!!.setDocumentLocator(locator)
        }
    }

    @Throws(SAXException::class)
    override fun skippedEntity(name: String?) {
        super.skippedEntity(name)

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.skippedEntity(name)
        }
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        super.endDocument()

        if (this.xMLWriter != null) {
            xMLWriter!!.endDocument()
        }
    }

    @Throws(SAXException::class)
    override fun startPrefixMapping(prefix: String?, uri: String?) {
        super.startPrefixMapping(prefix, uri)

        if (this.xMLWriter != null) {
            xMLWriter!!.startPrefixMapping(prefix, uri)
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String?, localName: String?, qName: String?) {
        val currentHandler = elementStack.dispatchHandler!!.getHandler(
            elementStack.path
        )

        super.endElement(uri, localName, qName)

        if (!activeHandlers()) {
            if (this.xMLWriter != null) {
                if (currentHandler == null) {
                    xMLWriter!!.endElement(uri, localName, qName)
                } else if (currentHandler is SAXModifyElementHandler) {
                    val modifyHandler = currentHandler
                    val modifiedElement = modifyHandler.modifiedElement

                    try {
                        xMLWriter!!.write(modifiedElement)
                    } catch (ex: IOException) {
                        throw SAXModifyException(ex)
                    }
                }
            }
        }
    }

    @Throws(SAXException::class)
    override fun endPrefixMapping(prefix: String?) {
        super.endPrefixMapping(prefix)

        if (this.xMLWriter != null) {
            xMLWriter!!.endPrefixMapping(prefix)
        }
    }

    @Throws(SAXException::class)
    override fun characters(parm1: CharArray?, parm2: Int, parm3: Int) {
        super.characters(parm1, parm2, parm3)

        if (!activeHandlers() && (this.xMLWriter != null)) {
            xMLWriter!!.characters(parm1, parm2, parm3)
        }
    }

    private fun activeHandlers(): Boolean {
        val handler = elementStack.dispatchHandler!!

        return handler.activeHandlerCount > 0
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


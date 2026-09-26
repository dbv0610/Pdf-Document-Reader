/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.util

import com.wxiwei.office.fc.dom4j.DocumentHelper
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.QName
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException

/**
 * `XMLErrorHandler` is a SAX [ErrorHandler]which turns the
 * SAX parsing errors into XML so that the output can be formatted using XSLT or
 * the errors can be included in a SOAP message.
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.7 $
 */
class XMLErrorHandler : ErrorHandler {
    // Properties
    // -------------------------------------------------------------------------
    /** Stores the errors that occur during a SAX parse  */
    var errors: Element

    // Allow the QNames used to create subelements to be changed
    /** QName used for error elements  */
    var errorQName: QName? = ERROR_QNAME

    /** QName used for fatalerror elements  */
    var fatalErrorQName: QName? = FATALERROR_QNAME

    /** QName used for warning elements  */
    var warningQName: QName? = WARNING_QNAME

    constructor() {
        this.errors = DocumentHelper.createElement("errors")!!
    }

    constructor(errors: Element) {
        this.errors = errors
    }

    override fun error(e: SAXParseException) {
        val element = errors.addElement(errorQName)!!
        addException(element, e)
    }

    override fun fatalError(e: SAXParseException) {
        val element = errors.addElement(fatalErrorQName)!!
        addException(element, e)
    }

    override fun warning(e: SAXParseException) {
        val element = errors.addElement(warningQName)!!
        addException(element, e)
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    /**
     * Adds the given parse exception information to the given element instance
     * 
     * @param element
     * DOCUMENT ME!
     * @param e
     * DOCUMENT ME!
     */
    protected fun addException(element: Element, e: SAXParseException) {
        element.addAttribute("column", e.getColumnNumber().toString())
        element.addAttribute("line", e.getLineNumber().toString())

        val publicID = e.getPublicId()

        if ((publicID != null) && (publicID.length > 0)) {
            element.addAttribute("publicID", publicID)
        }

        val systemID = e.getSystemId()

        if ((systemID != null) && (systemID.length > 0)) {
            element.addAttribute("systemID", systemID)
        }

        element.addText(e.message)
    }

    companion object {
        protected val ERROR_QNAME: QName? = QName.Companion.get("error")

        protected val FATALERROR_QNAME: QName? = QName.Companion.get("fatalError")

        protected val WARNING_QNAME: QName? = QName.Companion.get("warning")
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


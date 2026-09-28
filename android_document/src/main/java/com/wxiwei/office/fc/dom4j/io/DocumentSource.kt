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
import com.wxiwei.office.fc.dom4j.Node
import org.xml.sax.InputSource
import org.xml.sax.XMLFilter
import org.xml.sax.XMLReader
import javax.xml.transform.sax.SAXSource

/**
 * 
 * 
 * `DocumentSource` implements a JAXP [SAXSource]for a
 * {@linkDocument}.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.10 $
 */
class DocumentSource : SAXSource {
    /** The XMLReader to use  */
    private var xmlReader: XMLReader = SAXWriter()

    /**
     * Creates a JAXP [SAXSource]for the given [Node].
     * 
     * @param node
     * DOCUMENT ME!
     */
    constructor(node: Node) {
        this.document = node.document
    }

    /**
     * Creates a JAXP [SAXSource]for the given [Document].
     * 
     * @param document
     * DOCUMENT ME!
     */
    constructor(document: Document) {
        this.document = document
    }

    // Properties
    // -------------------------------------------------------------------------
    var document: Document?
        /**
         * DOCUMENT ME!
         * 
         * @return the document which is being used as the JAXP [SAXSource]
         */
        get() {
            val source = getInputSource() as DocumentInputSource
            return source.getDocument()
        }
        /**
         * Sets the document used as the JAXP [SAXSource]
         * 
         * @param document
         * DOCUMENT ME!
         */
        set(document) {
            super.setInputSource(DocumentInputSource(document!!))
        }

    // Overloaded methods
    // -------------------------------------------------------------------------
    /**
     * DOCUMENT ME!
     * 
     * @return the XMLReader to be used for the JAXP [SAXSource].
     */
    override fun getXMLReader(): XMLReader {
        return xmlReader
    }

    /**
     * This method is not supported as this source is always a {@linkDocument}
     * instance.
     * 
     * @param inputSource
     * DOCUMENT ME!
     * 
     * @throws UnsupportedOperationException
     * as this method is unsupported
     */
    @Throws(UnsupportedOperationException::class)
    override fun setInputSource(inputSource: InputSource?) {
        if (inputSource is DocumentInputSource) {
            super.setInputSource(inputSource)
        } else {
            throw UnsupportedOperationException()
        }
    }

    /**
     * Sets the XMLReader used for the JAXP [SAXSource].
     * 
     * @param reader
     * DOCUMENT ME!
     * 
     * @throws UnsupportedOperationException
     * DOCUMENT ME!
     */
    @Throws(UnsupportedOperationException::class)
    override fun setXMLReader(reader: XMLReader?) {
        if (reader is SAXWriter) {
            this.xmlReader = reader
        } else if (reader is XMLFilter) {
            var filter: XMLFilter = reader

            while (true) {
                val parent = filter.parent

                if (parent is XMLFilter) {
                    filter = parent
                } else {
                    break
                }
            }

            // install filter in SAXWriter....
            filter.setParent(xmlReader)
            xmlReader = filter
        } else {
            throw UnsupportedOperationException()
        }
    }

    companion object {
        /**
         * If [javax.xml.transform.TransformerFactory.getFeature]returns
         * `true` when passed this value as an argument then the
         * Transformer natively supports *dom4j *.
         */
        const val DOM4J_FEATURE: String = "http://org.dom4j.io.DoucmentSource/feature"
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


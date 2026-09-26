/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.Document
import org.xml.sax.InputSource
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter

/**
 * 
 * 
 * `DocumentInputSource` implements a SAX [InputSource]for a
 * [Document].
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.8 $
 */
internal class DocumentInputSource : InputSource {
    /** The document source  */
    private var document: Document? = null

    constructor()

    constructor(document: Document) {
        this.document = document
        setSystemId(document.name)
    }

    // Properties
    // -------------------------------------------------------------------------
    /**
     * DOCUMENT ME!
     * 
     * @return the document which is being used as the SAX [InputSource]
     */
    fun getDocument(): Document {
        return document!!
    }

    /**
     * Sets the document used as the SAX [InputSource]
     * 
     * @param document
     * DOCUMENT ME!
     */
    fun setDocument(document: Document) {
        this.document = document
        setSystemId(document.name)
    }

    // Overloaded methods
    // -------------------------------------------------------------------------
    /**
     * This method is not supported as this source is always a {@linkDocument}
     * instance.
     * 
     * @param characterStream
     * DOCUMENT ME!
     * 
     * @throws UnsupportedOperationException
     * as this method is unsupported
     */
    @Throws(UnsupportedOperationException::class)
    override fun setCharacterStream(characterStream: Reader?) {
        throw UnsupportedOperationException()
    }

    /**
     * Note this method is quite inefficent, it turns the in memory XML tree
     * object model into a single block of text which can then be read by other
     * XML parsers. Should only be used with care.
     * 
     * @return DOCUMENT ME!
     */
    override fun getCharacterStream(): Reader {
        try {
            val out = StringWriter()
            val writer = XMLWriter(out)
            writer.write(document)
            writer.flush()

            return StringReader(out.toString())
        } catch (e: IOException) {
            // this should never really happen
            // but for completeness we'll return a Reader
            // with the embedded exception inside it
            return object : Reader() {
                @Throws(IOException::class)
                override fun read(ch: CharArray?, offset: Int, length: Int): Int {
                    throw e
                }

                @Throws(IOException::class)
                override fun close() {
                }
            }
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


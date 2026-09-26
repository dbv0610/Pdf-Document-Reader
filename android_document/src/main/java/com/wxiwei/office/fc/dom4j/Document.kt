/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j

import org.xml.sax.EntityResolver

/**
 * 
 * 
 * `Document` defines an XML Document.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.14 $
 */
interface Document : Branch {
    /**
     * Returns the root [Element]for this document.
     * 
     * @return the root element for this document
     */
    /**
     * Sets the root element for this document
     * 
     * @param rootElement
     * the new root element for this document
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getRootElementProperty")
    @set:JvmName("setRootElementProperty")
    var rootElement: Element?
    fun getRootElement(): Element? = rootElement
    fun setRootElement(rootElement: Element?) { this.rootElement = rootElement }

    /**
     * Adds a new `Comment` node with the given text to this
     * branch.
     * 
     * @param comment
     * is the text for the `Comment` node.
     * 
     * @return this `Document` instance.
     */
    fun addComment(comment: String?): Document?

    /**
     * Adds a processing instruction for the given target
     * 
     * @param target
     * is the target of the processing instruction
     * @param text
     * is the textual data (key/value pairs) of the processing
     * instruction
     * 
     * @return this `Document` instance.
     */
    fun addProcessingInstruction(target: String?, text: String?): Document?

    /**
     * Adds a processing instruction for the given target
     * 
     * @param target
     * is the target of the processing instruction
     * @param data
     * is a Map of the key / value pairs of the processing
     * instruction
     * 
     * @return this `Document` instance.
     */
    fun addProcessingInstruction(target: String?, data: MutableMap<*, *>?): Document?

    /**
     * Adds a DOCTYPE declaration to this document
     * 
     * @param name
     * is the name of the root element
     * @param publicId
     * is the PUBLIC URI
     * @param systemId
     * is the SYSTEM URI
     * 
     * @return this `Document` instance.
     */
    fun addDocType(name: String?, publicId: String?, systemId: String?): Document?

    /**
     * DOCUMENT ME!
     * 
     * @return the DocumentType property
     */
    /**
     * Sets the DocumentType property
     * 
     * @param docType
     * DOCUMENT ME!
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDocTypeProperty")
    @set:JvmName("setDocTypeProperty")
    var docType: DocumentType?
    fun getDocType(): DocumentType? = docType
    fun setDocType(docType: DocumentType?) { this.docType = docType }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getEntityResolverProperty")
    @set:JvmName("setEntityResolverProperty")
    var entityResolver: EntityResolver?
    fun getEntityResolver(): EntityResolver? = entityResolver
    fun setEntityResolver(entityResolver: EntityResolver?) { this.entityResolver = entityResolver }

    /**
     * Return the encoding of this document, as part of the XML declaration This
     * is `null` when unspecified or when it is not known (such as
     * when the Document was created in memory) or when the implementation does
     * not support this operation.
     * 
     * 
     * 
     * The way this encoding is retrieved also depends on the way the XML source
     * is parsed. For instance, if the SAXReader is used and if the underlying
     * XMLReader implementation support the
     * `org.xml.sax.ext.Locator2` interface, the result returned by
     * this method is specified by the `getEncoding()` method of
     * that interface.
     * 
     * 
     * @return The encoding of this document, as stated in the XML declaration,
     * or `null` if unknown.
     * 
     * @since 1.5
     */
    /**
     * Sets the encoding of this document as it will appear in the XML
     * declaration part of the document.
     * 
     * @param encoding the encoding of the document
     * 
     * @since 1.6
     */
    var xMLEncoding: String?
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


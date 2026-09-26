/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.DocumentType
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.IllegalAddException
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import org.xml.sax.EntityResolver
import java.util.Collections

/**
 * 
 * 
 * `DefaultDocument` is the default DOM4J default implementation of
 * an XML document.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.34 $
 */
open class DefaultDocument : AbstractDocument {
    /** The name of the document  */
    private var docName: String? = null

    /** The root element of this document  */
    private var rootElementField: Element? = null

    /**
     * Store the contents of the document as a lazily created `List`
     */
    private var content: MutableList<Any?>? = null

    /** The document type for this document  */
    private var docTypeField: DocumentType? = null

    /** The document factory used by default  */
    private var documentFactory: DocumentFactory? = DocumentFactory.getInstance()

    /** The resolver of URIs  */
    @Transient
    private var entityResolverField: EntityResolver? = null

    constructor()

    constructor(name: String?) {
        this.docName = name
    }

    constructor(rootElement: Element?) {
        this.rootElementField = rootElement
    }

    constructor(docType: DocumentType?) {
        this.docTypeField = docType
    }

    constructor(rootElement: Element?, docType: DocumentType?) {
        this.rootElementField = rootElement
        this.docTypeField = docType
    }

    constructor(name: String?, rootElement: Element?, docType: DocumentType?) {
        this.docName = name
        this.rootElementField = rootElement
        this.docTypeField = docType
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNameProperty")
    @set:JvmName("setNameProperty")
    override var name: String?
        get() = docName
        set(name) {
            this.docName = name
        }

    override fun getRootElementValue(): Element? {
        return rootElementField
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDocTypeProperty")
    @set:JvmName("setDocTypeProperty")
    override var docType: DocumentType?
        get() = docTypeField
        set(docType) {
            this.docTypeField = docType
        }

    override fun addDocType(name: String?, publicId: String?, systemId: String?): Document? {
        docType = getDocumentFactory().createDocType(name, publicId, systemId)

        return this
    }

    override var xMLEncoding: String?
        get() = encoding
        set(enc) {
            super.xMLEncoding = enc
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getEntityResolverProperty")
    @set:JvmName("setEntityResolverProperty")
    override var entityResolver: EntityResolver?
        get() = entityResolverField
        set(entityResolver) {
            this.entityResolverField = entityResolver
        }

    override fun clone(): Any {
        val document = super.clone() as DefaultDocument
        document.rootElementField = null
        document.content = null
        document.appendContent(this)

        return document
    }

    override fun processingInstructions(): MutableList<*>? {
        val source = contentList()
        val answer = createResultList()
        val size = source.size

        for (i in 0 until size) {
            val `object`: Any? = source[i]

            if (`object` is ProcessingInstruction) {
                answer.add(`object`)
            }
        }

        return answer
    }

    override fun processingInstructions(target: String?): MutableList<*>? {
        val source = contentList()
        val answer = createResultList()
        val size = source.size

        for (i in 0 until size) {
            val `object`: Any? = source[i]

            if (`object` is ProcessingInstruction) {
                if (target == `object`.name) {
                    answer.add(`object`)
                }
            }
        }

        return answer
    }

    override fun processingInstruction(target: String?): ProcessingInstruction? {
        val source = contentList()
        val size = source.size

        for (i in 0 until size) {
            val `object`: Any? = source[i]

            if (`object` is ProcessingInstruction) {
                if (target == `object`.name) {
                    return `object`
                }
            }
        }

        return null
    }

    override fun removeProcessingInstruction(target: String?): Boolean {
        val source = contentList()

        val iter = source.iterator()
        while (iter.hasNext()) {
            val `object` = iter.next()

            if (`object` is ProcessingInstruction) {
                if (target == `object`.name) {
                    iter.remove()

                    return true
                }
            }
        }

        return false
    }

    override fun setContent(content: MutableList<*>?) {
        var content = content
        rootElementField = null
        contentRemoved()

        if (content is ContentListFacade) {
            content = content.backingList
        }

        if (content == null) {
            this.content = null
        } else {
            val size = content.size
            val newContent = createContentList(size)

            for (i in 0 until size) {
                val `object`: Any? = content[i]

                if (`object` is Node) {
                    var node: Node = `object`
                    val doc = node.document

                    if ((doc != null) && (doc !== this)) {
                        node = node.clone() as Node
                    }

                    if (node is Element) {
                        if (rootElementField == null) {
                            rootElementField = node
                        } else {
                            throw IllegalAddException(
                                ("A document may only "
                                        + "contain one root " + "element: " + content)
                            )
                        }
                    }

                    newContent.add(node)
                    childAdded(node)
                }
            }

            this.content = newContent
        }
    }

    override fun clearContent() {
        contentRemoved()
        content = null
        rootElementField = null
    }

    fun setDocumentFactory(documentFactory: DocumentFactory?) {
        this.documentFactory = documentFactory
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    override fun contentList(): MutableList<Any?> {
        if (content == null) {
            content = createContentList()

            if (rootElementField != null) {
                content!!.add(rootElementField)
            }
        }

        return content!!
    }

    override fun addNode(node: Node?) {
        if (node != null) {
            val document = node.document

            if ((document != null) && (document !== this)) {
                // XXX: could clone here
                val message = "The Node already has an existing document: " + document
                throw IllegalAddException(this as Branch, node, message)
            }

            contentList().add(node)
            childAdded(node)
        }
    }

    override fun addNode(index: Int, node: Node?) {
        if (node != null) {
            val document = node.document

            if ((document != null) && (document !== this)) {
                // XXX: could clone here
                val message = "The Node already has an existing document: " + document
                throw IllegalAddException(this as Branch, node, message)
            }

            contentList().add(index, node)
            childAdded(node)
        }
    }

    override fun removeNode(node: Node?): Boolean {
        if (node === rootElementField) {
            rootElementField = null
        }

        if (contentList().remove(node)) {
            childRemoved(node)

            return true
        }

        return false
    }

    override fun rootElementAdded(element: Element?) {
        this.rootElementField = element
        element!!.document = this
    }

    /**
     * The document factory set on this document, which may be null
     * (Java `DefaultDocument.getDocumentFactory()` could return null).
     */
    protected val documentFactoryOrNull: DocumentFactory?
        get() = documentFactory

    override fun getDocumentFactory(): DocumentFactory {
        return documentFactory!!
    }

    companion object {
        @JvmField
        val EMPTY_LIST: MutableList<Any?> = Collections.emptyList<Any?>()

        @JvmField
        val EMPTY_ITERATOR: MutableIterator<Any?> = EMPTY_LIST.iterator()
    }
}
/*
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


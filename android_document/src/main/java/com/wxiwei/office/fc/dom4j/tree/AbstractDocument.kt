/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.IllegalAddException
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.Visitor
import com.wxiwei.office.fc.dom4j.io.OutputFormat
import com.wxiwei.office.fc.dom4j.io.XMLWriter
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

/**
 * 
 * 
 * `AbstractDocument` is an abstract base class for tree
 * implementors to use for implementation inheritence.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.33 $
 */
abstract class AbstractDocument : AbstractBranch(), Document {
    /** The encoding of this document as stated in the XML declaration  */
    protected var encoding: String? = null

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    override val nodeType: Short
        get() = Node.DOCUMENT_NODE

    override fun getPath(context: Element?): String? {
        return "/"
    }

    override fun getUniquePath(context: Element?): String? {
        return "/"
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDocumentProperty")
    @set:JvmName("setDocumentProperty")
    override var document: Document?
        get() = this
        set(document) {
            super.document = document
        }

    override var xMLEncoding: String?
        get() = null
        set(enc) {
            this.encoding = enc
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String?
        get() {
            val root = rootElement

            return if (root != null) root.stringValue else ""
        }

    override fun asXML(): String? {
        val format = OutputFormat()
        format.setEncoding(encoding)

        try {
            val out = StringWriter()
            val writer = XMLWriter(out, format)
            writer.write(this as Document)
            writer.flush()

            return out.toString()
        } catch (e: IOException) {
            throw RuntimeException(
                ("IOException while generating textual " + "representation: "
                        + e.message)
            )
        }
    }

    @Throws(IOException::class)
    override fun write(writer: Writer?) {
        val format = OutputFormat()
        format.setEncoding(encoding)

        val xmlWriter = XMLWriter(writer!!, format)
        xmlWriter.write(this as Document)
    }

    /**
     * `accept` method is the `Visitor Pattern`
     * method.
     *
     * @param visitor
     * `Visitor` is the visitor.
     */
    override fun accept(visitor: Visitor?) {
        visitor!!.visit(this as Document)

        val docType = this.docType

        if (docType != null) {
            visitor.visit(docType)
        }

        // visit content
        val content = content()

        if (content != null) {
            val iter = content.iterator()
            while (iter.hasNext()) {
                val `object` = iter.next()

                if (`object` is String) {
                    val text = getDocumentFactory().createText(`object`)
                    visitor.visit(text)
                } else {
                    val node = `object` as Node
                    node.accept(visitor)
                }
            }
        }
    }

    override fun toString(): String {
        return super.toString() + " [Document: name " + name + "]"
    }

    override fun normalize() {
        val element = rootElement

        element?.normalize()
    }

    override fun addComment(comment: String?): Document? {
        val node = getDocumentFactory().createComment(comment)
        add(node)

        return this
    }

    override fun addProcessingInstruction(target: String?, text: String?): Document? {
        val node = getDocumentFactory().createProcessingInstruction(target, text)
        add(node)

        return this
    }

    override fun addProcessingInstruction(target: String?, data: MutableMap<*, *>?): Document? {
        val node = getDocumentFactory().createProcessingInstruction(target, data)
        add(node)

        return this
    }

    override fun addElement(name: String?): Element? {
        val element = getDocumentFactory().createElement(name)
        add(element)

        return element
    }

    override fun addElement(qualifiedName: String?, namespaceURI: String?): Element? {
        val element = getDocumentFactory().createElement(qualifiedName!!, namespaceURI)
        add(element)

        return element
    }

    override fun addElement(qname: QName?): Element? {
        val element = getDocumentFactory().createElement(qname)
        add(element)

        return element
    }

    /**
     * The root element of this document. The getter delegates to
     * [getRootElementValue] which subclasses implement (the Java
     * `getRootElement()` was abstract here).
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getRootElementProperty")
    @set:JvmName("setRootElementProperty")
    override var rootElement: Element?
        get() = getRootElementValue()
        set(rootElement) {
            clearContent()

            if (rootElement != null) {
                super.add(rootElement)
                rootElementAdded(rootElement)
            }
        }

    /**
     * @return the root element of this document (the implementation of
     * the Java abstract `getRootElement()`)
     */
    protected abstract fun getRootElementValue(): Element?

    override fun add(element: Element?) {
        checkAddElementAllowed(element)
        super.add(element)
        rootElementAdded(element)
    }

    override fun remove(element: Element?): Boolean {
        val answer = super.remove(element)
        val root = rootElement

        if ((root != null) && answer) {
            rootElement = null
        }

        element!!.document = null

        return answer
    }

    override fun asXPathResult(parent: Element?): Node? {
        return this
    }

    override fun childAdded(node: Node?) {
        if (node != null) {
            node.document = this
        }
    }

    override fun childRemoved(node: Node?) {
        if (node != null) {
            node.document = null
        }
    }

    protected fun checkAddElementAllowed(element: Element?) {
        val root = rootElement

        if (root != null) {
            throw IllegalAddException(
                this as Branch, element!!, ("Cannot add another element to this "
                        + "Document as it already has a root " + "element of: " + root.qualifiedName)
            )
        }
    }

    /**
     * Called to set the root element variable
     *
     * @param rootElement
     * DOCUMENT ME!
     */
    protected abstract fun rootElementAdded(rootElement: Element?)
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


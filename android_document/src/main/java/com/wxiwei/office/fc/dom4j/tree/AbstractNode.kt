/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.NodeFilter
import com.wxiwei.office.fc.dom4j.XPath
import com.wxiwei.office.fc.dom4j.rule.Pattern
import java.io.IOException
import java.io.Serializable
import java.io.Writer

/**
 * `AbstractNode` is an abstract base class for tree implementors
 * to use for implementation inheritence.
 *
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.31 $
 */
abstract class AbstractNode : Node, Cloneable, Serializable {

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    override val nodeType: Short
        get() = Node.UNKNOWN_NODE

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeNameProperty")
    override val nodeTypeName: String?
        get() {
            val type = nodeType.toInt()
            if ((type < 0) || (type >= NODE_TYPE_NAMES.size)) {
                return "Unknown"
            }
            return NODE_TYPE_NAMES[type]
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDocumentProperty")
    @set:JvmName("setDocumentProperty")
    override var document: Document?
        get() {
            val element = parent
            return element?.document
        }
        set(document) {
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getParentProperty")
    @set:JvmName("setParentProperty")
    override var parent: Element?
        get() = null
        set(parent) {
        }

    override fun supportsParent(): Boolean {
        return false
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("isReadOnlyProperty")
    override val isReadOnly: Boolean
        get() = true

    override fun hasContent(): Boolean {
        return false
    }

    override val path: String?
        get() = getPath(null)

    override val uniquePath: String?
        get() = getUniquePath(null)

    public override fun clone(): Any {
        if (isReadOnly) {
            return this
        } else {
            try {
                val answer = super<Cloneable>.clone() as Node
                answer.parent = null
                answer.document = null
                return answer
            } catch (e: CloneNotSupportedException) {
                // should never happen
                throw RuntimeException("This should never happen. Caught: $e")
            }
        }
    }

    override fun detach(): Node? {
        val parent = this.parent
        if (parent != null) {
            parent.remove(this)
        } else {
            val document = this.document
            document?.remove(this)
        }
        this.parent = null
        this.document = null
        return this
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNameProperty")
    @set:JvmName("setNameProperty")
    override var name: String?
        get() = null
        set(name) {
            throw UnsupportedOperationException("This node cannot be modified")
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() = null
        set(text) {
            throw UnsupportedOperationException("This node cannot be modified")
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String?
        get() = text

    override fun getStringValue(): String? = stringValue

    @Throws(IOException::class)
    override fun write(writer: Writer?) {
        writer!!.write(asXML())
    }

    // XPath methods
    override fun selectObject(xpathExpression: String?): Any? {
        val xpath = createXPath(xpathExpression)
        return xpath.evaluate(this)
    }

    override fun selectNodes(xpathExpression: String?): MutableList<*>? {
        val xpath = createXPath(xpathExpression)
        return xpath.selectNodes(this)
    }

    override fun selectNodes(
        xpathExpression: String?,
        comparisonXPathExpression: String?
    ): MutableList<*>? {
        return selectNodes(xpathExpression, comparisonXPathExpression, false)
    }

    override fun selectNodes(
        xpathExpression: String?, comparisonXPathExpression: String?,
        removeDuplicates: Boolean
    ): MutableList<*>? {
        val xpath = createXPath(xpathExpression)
        val sortBy = createXPath(comparisonXPathExpression)
        return xpath.selectNodes(this, sortBy, removeDuplicates)
    }

    override fun selectSingleNode(xpathExpression: String?): Node? {
        val xpath = createXPath(xpathExpression)
        return xpath.selectSingleNode(this)
    }

    override fun valueOf(xpathExpression: String?): String? {
        val xpath = createXPath(xpathExpression)
        return xpath.valueOf(this)
    }

    override fun numberValueOf(xpathExpression: String?): Number? {
        val xpath = createXPath(xpathExpression)
        return xpath.numberValueOf(this)
    }

    override fun matches(patternText: String?): Boolean {
        val filter = createXPathFilter(patternText)
        return filter.matches(this)
    }

    override fun createXPath(xpathExpression: String?): XPath {
        return getDocumentFactory().createXPath(xpathExpression)
    }

    fun createXPathFilter(patternText: String?): NodeFilter {
        return getDocumentFactory().createXPathFilter(patternText)
    }

    fun createPattern(patternText: String?): Pattern? {
        return getDocumentFactory().createPattern(patternText)
    }

    override fun asXPathResult(parent: Element?): Node? {
        if (supportsParent()) {
            return this
        }
        return createXPathResult(parent)
    }

    protected open fun createXPathResult(parent: Element?): Node? {
        throw RuntimeException("asXPathResult() not yet implemented fully for: $this")
    }

    protected open fun getDocumentFactory(): DocumentFactory {
        return DOCUMENT_FACTORY
    }

    companion object {
        @JvmField
        val NODE_TYPE_NAMES: Array<String?> = arrayOf(
            "Node", "Element", "Attribute", "Text",
            "CDATA", "Entity", "Entity", "ProcessingInstruction", "Comment", "Document",
            "DocumentType", "DocumentFragment", "Notation", "Namespace", "Unknown"
        )

        /** The `DocumentFactory` instance used by default  */
        private val DOCUMENT_FACTORY: DocumentFactory = DocumentFactory.getInstance()
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


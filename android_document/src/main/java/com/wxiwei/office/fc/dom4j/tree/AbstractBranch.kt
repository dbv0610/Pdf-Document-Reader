/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.Comment
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.IllegalAddException
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.QName
import java.util.StringTokenizer

/**
 * `AbstractBranch` is an abstract base class for tree implementors
 * to use for implementation inheritence.
 *
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.44 $
 */
abstract class AbstractBranch : AbstractNode(), Branch {

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("isReadOnlyProperty")
    override val isReadOnly: Boolean
        get() = false

    override fun hasContent(): Boolean {
        return nodeCount() > 0
    }

    override fun content(): MutableList<Any?>? {
        val backingList = contentList()

        return ContentListFacade(this, backingList)
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() {
            val content = contentList()

            if (content != null) {
                val size = content.size

                if (size >= 1) {
                    val first = content[0]
                    val firstText = getContentAsText(first)

                    if (size == 1) {
                        // optimised to avoid StringBuffer creation
                        return firstText
                    } else {
                        val buffer = StringBuffer(firstText)

                        for (i in 1 until size) {
                            val node = content[i]
                            buffer.append(getContentAsText(node))
                        }

                        return buffer.toString()
                    }
                }
            }

            return ""
        }
        set(text) {
            super.text = text
        }

    /**
     * DOCUMENT ME!
     *
     * @param content
     * DOCUMENT ME!
     *
     * @return the text value of the given content object as text which returns
     * the text value of CDATA, Entity or Text nodes
     */
    protected fun getContentAsText(content: Any?): String? {
        if (content is Node) {
            when (content.nodeType) {
                Node.CDATA_SECTION_NODE, Node.ENTITY_REFERENCE_NODE, Node.TEXT_NODE -> return content.text
                else -> {}
            }
        } else if (content is String) {
            return content
        }

        return ""
    }

    /**
     * DOCUMENT ME!
     *
     * @param content
     * DOCUMENT ME!
     *
     * @return the XPath defined string-value of the given content object
     */
    protected fun getContentAsStringValue(content: Any?): String? {
        if (content is Node) {
            when (content.nodeType) {
                Node.CDATA_SECTION_NODE, Node.ENTITY_REFERENCE_NODE, Node.TEXT_NODE, Node.ELEMENT_NODE -> return content.stringValue
                else -> {}
            }
        } else if (content is String) {
            return content
        }

        return ""
    }

    open val textTrim: String?
        get() {
            val text = this.text

            val textContent = StringBuffer()
            val tokenizer = StringTokenizer(text)

            while (tokenizer.hasMoreTokens()) {
                val str = tokenizer.nextToken()
                textContent.append(str)

                if (tokenizer.hasMoreTokens()) {
                    textContent.append(" ") // separator
                }
            }

            return textContent.toString()
        }

    // Content Model methods
    // -------------------------------------------------------------------------
    override fun setProcessingInstructions(listOfPIs: MutableList<*>?) {
        val iter = listOfPIs!!.iterator()
        while (iter.hasNext()) {
            val pi = iter.next() as ProcessingInstruction?
            addNode(pi)
        }
    }

    override fun addElement(name: String?): Element? {
        val node = getDocumentFactory().createElement(name)
        add(node)

        return node
    }

    override fun addElement(qualifiedName: String?, namespaceURI: String?): Element? {
        val node = getDocumentFactory().createElement(qualifiedName!!, namespaceURI)
        add(node)

        return node
    }

    override fun addElement(qname: QName?): Element? {
        val node = getDocumentFactory().createElement(qname)
        add(node)

        return node
    }

    fun addElement(name: String?, prefix: String?, uri: String?): Element? {
        val namespace = Namespace.get(prefix, uri)
        val qName = getDocumentFactory().createQName(name, namespace)

        return addElement(qName)
    }

    // polymorphic node methods
    override fun add(node: Node?) {
        when (node!!.nodeType) {
            Node.ELEMENT_NODE -> add(node as Element?)
            Node.COMMENT_NODE -> add(node as Comment?)
            Node.PROCESSING_INSTRUCTION_NODE -> add(node as ProcessingInstruction?)
            else -> invalidNodeTypeAddException(node)
        }
    }

    override fun remove(node: Node?): Boolean {
        when (node!!.nodeType) {
            Node.ELEMENT_NODE -> return remove(node as Element?)
            Node.COMMENT_NODE -> return remove(node as Comment?)
            Node.PROCESSING_INSTRUCTION_NODE -> return remove(node as ProcessingInstruction?)
            else -> {
                invalidNodeTypeAddException(node)

                return false
            }
        }
    }

    // typesafe versions using node classes
    override fun add(comment: Comment?) {
        addNode(comment)
    }

    override fun add(element: Element?) {
        addNode(element)
    }

    override fun add(pi: ProcessingInstruction?) {
        addNode(pi)
    }

    override fun remove(comment: Comment?): Boolean {
        return removeNode(comment)
    }

    override fun remove(element: Element?): Boolean {
        return removeNode(element)
    }

    override fun remove(pi: ProcessingInstruction?): Boolean {
        return removeNode(pi)
    }

    override fun elementByID(elementID: String?): Element? {
        var i = 0
        val size = nodeCount()
        while (i < size) {
            val node = node(i)

            if (node is Element) {
                var element: Element? = node
                val id = elementID(element!!)

                if ((id != null) && id == elementID) {
                    return element
                } else {
                    element = element.elementByID(elementID)

                    if (element != null) {
                        return element
                    }
                }
            }
            i++
        }

        return null
    }

    override fun appendContent(branch: Branch?) {
        var i = 0
        val size = branch!!.nodeCount()
        while (i < size) {
            val node = branch.node(i)
            add(node!!.clone() as Node)
            i++
        }
    }

    override fun node(index: Int): Node? {
        val `object` = contentList()[index]

        if (`object` is Node) {
            return `object`
        }

        if (`object` is String) {
            return getDocumentFactory().createText(`object`.toString())
        }

        return null
    }

    override fun nodeCount(): Int {
        return contentList().size
    }

    override fun indexOf(node: Node?): Int {
        return contentList().indexOf(node)
    }

    override fun nodeIterator(): MutableIterator<*>? {
        return contentList().iterator()
    }

    // Implementation methods

    /**
     * DOCUMENT ME!
     *
     * @param element
     * DOCUMENT ME!
     *
     * @return the ID of the given `Element`
     */
    protected fun elementID(element: Element): String? {
        // XXX: there will be other ways of finding the ID
        // XXX: should probably have an IDResolver or something
        return element.attributeValue("ID")
    }

    /**
     * DOCUMENT ME!
     *
     * @return the internal List used to manage the content
     */
    abstract fun contentList(): MutableList<Any?>

    /**
     * A Factory Method pattern which creates a List implementation used to
     * store content
     */
    protected fun createContentList(): MutableList<Any?> {
        return ArrayList(DEFAULT_CONTENT_LIST_SIZE)
    }

    /**
     * A Factory Method pattern which creates a List implementation used to
     * store content
     */
    protected fun createContentList(size: Int): MutableList<Any?> {
        return ArrayList(size)
    }

    /**
     * A Factory Method pattern which creates a BackedList implementation used
     * to store results of a filtered content query.
     */
    protected fun createResultList(): BackedList {
        return BackedList(this, contentList())
    }

    /**
     * A Factory Method pattern which creates a BackedList implementation which
     * contains a single result
     */
    protected fun createSingleResultList(result: Any?): MutableList<Any?> {
        val list = BackedList(this, contentList(), 1)
        list.addLocal(result)

        return list
    }

    /**
     * A Factory Method pattern which creates an empty a BackedList
     * implementation
     */
    protected fun createEmptyList(): MutableList<Any?> {
        return BackedList(this, contentList(), 0)
    }

    abstract fun addNode(node: Node?)

    abstract fun addNode(index: Int, node: Node?)

    abstract fun removeNode(node: Node?): Boolean

    /**
     * Called when a new child node has been added to me to allow any parent
     * relationships to be created or events to be fired.
     */
    abstract fun childAdded(node: Node?)

    /**
     * Called when a child node has been removed to allow any parent
     * relationships to be deleted or events to be fired.
     */
    abstract fun childRemoved(node: Node?)

    /**
     * Called when the given List content has been removed so each node should
     * have its parent and document relationships cleared
     */
    protected fun contentRemoved() {
        val content = contentList()

        var i = 0
        val size = content.size
        while (i < size) {
            val `object` = content[i]

            if (`object` is Node) {
                childRemoved(`object`)
            }
            i++
        }
    }

    /**
     * Called when an invalid node has been added. Throws an
     * [IllegalAddException].
     */
    protected fun invalidNodeTypeAddException(node: Node?) {
        throw IllegalAddException(
            ("Invalid node type. Cannot add node: " + node
                    + " to this branch: " + this)
        )
    }

    companion object {
        /** The default capacity for a content list  */
        const val DEFAULT_CONTENT_LIST_SIZE: Int = 5
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


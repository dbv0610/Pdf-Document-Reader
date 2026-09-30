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
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.IllegalAddException
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.QName

/**
 * 
 * 
 * `DefaultElement` is the default DOM4J default implementation of
 * an XML element.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.59 $
 */
open class DefaultElement : AbstractElement {
    /** The `QName` for this element  */
    private var qname: QName?

    /**
     * Stores the parent branch of this node which is either a Document if this
     * element is the root element in a document, or another Element if it is a
     * child of the root document, or null if it has not been added to a
     * document yet.
     */
    private var parentBranch: Branch? = null

    /**
     * Stores null for no content, a Node for a single content node or a List
     * for multiple content nodes. The List will be lazily constructed when
     * required.
     */
    private var content: Any? = null

    /** Lazily constructes list of attributes  */
    private var attributes: Any? = null

    constructor(name: String?) {
        this.qname = DOCUMENT_FACTORY.createQName(name)
    }

    constructor(qname: QName?) {
        this.qname = qname
    }

    constructor(qname: QName?, attributeCount: Int) {
        this.qname = qname

        if (attributeCount > 1) {
            this.attributes = ArrayList<Any?>(attributeCount)
        }
    }

    constructor(name: String?, namespace: Namespace?) {
        this.qname = DOCUMENT_FACTORY.createQName(name, namespace)
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getParentProperty")
    @set:JvmName("setParentProperty")
    override var parent: Element?
        get() {
            var result: Element? = null

            if (parentBranch is Element) {
                result = parentBranch as Element
            }

            return result
        }
        set(parent) {
            if (parentBranch is Element || (parent != null)) {
                parentBranch = parent
            }
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDocumentProperty")
    @set:JvmName("setDocumentProperty")
    override var document: Document?
        get() {
            if (parentBranch is Document) {
                return parentBranch as Document
            } else if (parentBranch is Element) {
                val parent = parentBranch as Element

                return parent.document
            }

            return null
        }
        set(document) {
            if (parentBranch is Document || (document != null)) {
                parentBranch = document
            }
        }

    override fun supportsParent(): Boolean {
        return true
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getQNameProperty")
    @set:JvmName("setQNameProperty")
    override var qName: QName?
        get() = qname
        set(name) {
            this.qname = name
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() {
            val contentShadow = content

            if (contentShadow is MutableList<*>) {
                return super.text
            } else {
                if (contentShadow != null) {
                    return getContentAsText(contentShadow)
                } else {
                    return ""
                }
            }
        }
        set(text) {
            super.text = text
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String?
        get() {
            val contentShadow = content

            if (contentShadow is MutableList<*>) {
                val list = contentShadow

                val size = list.size

                if (size > 0) {
                    if (size == 1) {
                        // optimised to avoid StringBuffer creation
                        return getContentAsStringValue(list[0])
                    } else {
                        val buffer = StringBuffer()

                        for (i in 0 until size) {
                            val node: Any? = list[i]

                            val string = getContentAsStringValue(node)

                            if (string!!.length > 0) {
                                if (USE_STRINGVALUE_SEPARATOR) {
                                    if (buffer.length > 0) {
                                        buffer.append(' ')
                                    }
                                }

                                buffer.append(string)
                            }
                        }

                        return buffer.toString()
                    }
                }
            } else {
                if (contentShadow != null) {
                    return getContentAsStringValue(contentShadow)
                }
            }

            return ""
        }

    override fun clone(): Any {
        val answer = super.clone() as DefaultElement

        if (answer !== this) {
            answer.content = null

            answer.attributes = null

            answer.appendAttributes(this)

            answer.appendContent(this)
        }

        return answer
    }

    override fun getNamespaceForPrefix(prefix: String?): Namespace? {
        var prefix = prefix
        if (prefix == null) {
            prefix = ""
        }

        if (prefix == namespacePrefix) {
            return namespace
        } else if (prefix == "xml") {
            return Namespace.XML_NAMESPACE
        } else {
            val contentShadow = content

            if (contentShadow is MutableList<*>) {
                val list = contentShadow

                val size = list.size

                for (i in 0 until size) {
                    val `object`: Any? = list[i]

                    if (`object` is Namespace) {
                        val namespace = `object`

                        if (prefix == namespace.getPrefix()) {
                            return namespace
                        }
                    }
                }
            } else if (contentShadow is Namespace) {
                val namespace = contentShadow

                if (prefix == namespace.getPrefix()) {
                    return namespace
                }
            }
        }

        val parent = parent

        if (parent != null) {
            val answer = parent.getNamespaceForPrefix(prefix)

            if (answer != null) {
                return answer
            }
        }

        if ((prefix == null) || (prefix.length <= 0)) {
            return Namespace.NO_NAMESPACE
        }

        return null
    }

    override fun getNamespaceForURI(uri: String?): Namespace? {
        if ((uri == null) || (uri.length <= 0)) {
            return Namespace.NO_NAMESPACE
        } else if (uri == namespaceURI) {
            return namespace
        } else {
            val contentShadow = content

            if (contentShadow is MutableList<*>) {
                val list = contentShadow

                val size = list.size

                for (i in 0 until size) {
                    val `object`: Any? = list[i]

                    if (`object` is Namespace) {
                        val namespace = `object`

                        if (uri == namespace.getURI()) {
                            return namespace
                        }
                    }
                }
            } else if (contentShadow is Namespace) {
                val namespace = contentShadow

                if (uri == namespace.getURI()) {
                    return namespace
                }
            }

            val parent = parent

            if (parent != null) {
                return parent.getNamespaceForURI(uri)
            }

            return null
        }
    }

    override fun declaredNamespaces(): MutableList<*>? {
        val answer = createResultList()

        // if (namespaceURI.length() > 0) {
        //
        // answer.addLocal(namespace);
        //
        // }
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is Namespace) {
                    answer.addLocal(`object`)
                }
            }
        } else {
            if (contentShadow is Namespace) {
                answer.addLocal(contentShadow)
            }
        }

        return answer
    }

    override fun additionalNamespaces(): MutableList<*>? {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            val size = list.size

            val answer = createResultList()

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is Namespace) {
                    val namespace = `object`

                    if (namespace != namespace) {
                        answer.addLocal(namespace)
                    }
                }
            }

            return answer
        } else {
            if (contentShadow is Namespace) {
                val namespace = contentShadow

                if (namespace == namespace) {
                    return createEmptyList()
                }

                return createSingleResultList(namespace)
            } else {
                return createEmptyList()
            }
        }
    }

    override fun additionalNamespaces(defaultNamespaceURI: String?): MutableList<*>? {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            val answer = createResultList()

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is Namespace) {
                    val namespace = `object`

                    if (defaultNamespaceURI != namespace.getURI()) {
                        answer.addLocal(namespace)
                    }
                }
            }

            return answer
        } else {
            if (contentShadow is Namespace) {
                val namespace = contentShadow

                if (defaultNamespaceURI != namespace.getURI()) {
                    return createSingleResultList(namespace)
                }
            }
        }

        return createEmptyList()
    }

    // Processing instruction API
    override fun processingInstructions(): MutableList<*>? {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            val answer = createResultList()

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is ProcessingInstruction) {
                    answer.addLocal(`object`)
                }
            }

            return answer
        } else {
            if (contentShadow is ProcessingInstruction) {
                return createSingleResultList(contentShadow)
            }

            return createEmptyList()
        }
    }

    override fun processingInstructions(target: String?): MutableList<*>? {
        val shadow = content

        if (shadow is MutableList<*>) {
            val list = shadow

            val answer = createResultList()

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is ProcessingInstruction) {
                    val pi = `object`

                    if (target == pi.name) {
                        answer.addLocal(pi)
                    }
                }
            }

            return answer
        } else {
            if (shadow is ProcessingInstruction) {
                val pi = shadow

                if (target == pi.name) {
                    return createSingleResultList(pi)
                }
            }

            return createEmptyList()
        }
    }

    override fun processingInstruction(target: String?): ProcessingInstruction? {
        val shadow = content

        if (shadow is MutableList<*>) {
            val list = shadow

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is ProcessingInstruction) {
                    val pi = `object`

                    if (target == pi.name) {
                        return pi
                    }
                }
            }
        } else {
            if (shadow is ProcessingInstruction) {
                val pi = shadow

                if (target == pi.name) {
                    return pi
                }
            }
        }

        return null
    }

    override fun removeProcessingInstruction(target: String?): Boolean {
        val shadow = content

        if (shadow is MutableList<*>) {
            val list = shadow

            val iter: MutableIterator<*> = list.iterator()
            while (iter.hasNext()) {
                val `object` = iter.next()

                if (`object` is ProcessingInstruction) {
                    val pi = `object`

                    if (target == pi.name) {
                        iter.remove()

                        return true
                    }
                }
            }
        } else {
            if (shadow is ProcessingInstruction) {
                val pi = shadow

                if (target == pi.name) {
                    this.content = null

                    return true
                }
            }
        }

        return false
    }

    override fun element(name: String?): Element? {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is Element) {
                    val element = `object`

                    if (name == element.name) {
                        return element
                    }
                }
            }
        } else {
            if (contentShadow is Element) {
                val element = contentShadow

                if (name == element.name) {
                    return element
                }
            }
        }

        return null
    }

    override fun element(qName: QName?): Element? {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is Element) {
                    val element = `object`

                    if (qName == element.qName) {
                        return element
                    }
                }
            }
        } else {
            if (contentShadow is Element) {
                val element = contentShadow

                if (qName == element.qName) {
                    return element
                }
            }
        }

        return null
    }

    override fun element(name: String?, namespace: Namespace?): Element? {
        return element(getDocumentFactory().createQName(name, namespace))
    }

    override fun setContent(content: MutableList<*>?) {
        var content = content
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
                    var node = `object`
                    val parent = node.parent

                    if ((parent != null) && (parent !== this)) {
                        node = node.clone() as Node
                    }

                    newContent.add(node)
                    childAdded(node)
                } else if (`object` != null) {
                    val text = `object`.toString()
                    val node: Node? = getDocumentFactory().createText(text)
                    newContent.add(node)
                    childAdded(node)
                }
            }

            this.content = newContent
        }
    }

    override fun clearContent() {
        if (content != null) {
            contentRemoved()

            content = null
        }
    }

    override fun node(index: Int): Node? {
        if (index >= 0) {
            val contentShadow = content
            val node: Any?

            if (contentShadow is MutableList<*>) {
                val list = contentShadow

                if (index >= list.size) {
                    return null
                }

                node = list.get(index)
            } else {
                node = if (index == 0) contentShadow else null
            }

            if (node != null) {
                if (node is Node) {
                    return node
                } else {
                    return DefaultText(node.toString())
                }
            }
        }

        return null
    }

    override fun indexOf(node: Node?): Int {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            return (list as MutableList<Any?>).indexOf(node)
        } else {
            if ((contentShadow != null) && contentShadow == node) {
                return 0
            } else {
                return -1
            }
        }
    }

    override fun nodeCount(): Int {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            return list.size
        } else {
            return if (contentShadow != null) 1 else 0
        }
    }

    override fun nodeIterator(): MutableIterator<*>? {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            val list = contentShadow

            return list.iterator()
        } else {
            if (contentShadow != null) {
                return createSingleIterator(contentShadow)
            } else {
                return AbstractElement.EMPTY_ITERATOR
            }
        }
    }

    override fun attributes(): MutableList<*>? {
        return ContentListFacade(this, attributeList())
    }

    override fun setAttributes(attributes: MutableList<*>?) {
        var attributes = attributes
        if (attributes is ContentListFacade) {
            attributes = attributes.backingList
        }

        this.attributes = attributes
    }

    override fun attributeIterator(): MutableIterator<*>? {
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            val list = attributesShadow

            return list.iterator()
        } else if (attributesShadow != null) {
            return createSingleIterator(attributesShadow)
        } else {
            return AbstractElement.EMPTY_ITERATOR
        }
    }

    override fun attribute(index: Int): Attribute? {
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            val list = attributesShadow

            return list.get(index) as Attribute?
        } else if ((attributesShadow != null) && (index == 0)) {
            return attributesShadow as Attribute
        } else {
            return null
        }
    }

    override fun attributeCount(): Int {
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            val list = attributesShadow

            return list.size
        } else {
            return if (attributesShadow != null) 1 else 0
        }
    }

    override fun attribute(name: String?): Attribute? {
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            val list = attributesShadow

            val size = list.size

            for (i in 0 until size) {
                val attribute = list[i] as Attribute

                if (name == attribute.name) {
                    return attribute
                }
            }
        } else if (attributesShadow != null) {
            val attribute = attributesShadow as Attribute

            if (name == attribute.name) {
                return attribute
            }
        }

        return null
    }

    override fun attribute(qName: QName?): Attribute? {
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            val list = attributesShadow

            val size = list.size

            for (i in 0 until size) {
                val attribute = list[i] as Attribute

                if (qName == attribute.qName) {
                    return attribute
                }
            }
        } else if (attributesShadow != null) {
            val attribute = attributesShadow as Attribute

            if (qName == attribute.qName) {
                return attribute
            }
        }

        return null
    }

    override fun attribute(name: String?, namespace: Namespace?): Attribute? {
        return attribute(getDocumentFactory().createQName(name, namespace))
    }

    override fun add(attribute: Attribute?) {
        if (attribute!!.parent != null) {
            val message = ("The Attribute already has an existing parent \""
                    + attribute.parent!!.qualifiedName + "\"")

            throw IllegalAddException(this as Element, attribute, message)
        }

        if (attribute.value == null) {
            // try remove a previous attribute with the same
            // name since adding an attribute with a null value
            // is equivalent to removing it.
            val oldAttribute = attribute(attribute.qName)

            if (oldAttribute != null) {
                remove(oldAttribute)
            }
        } else {
            if (attributes == null) {
                attributes = attribute
            } else {
                attributeList().add(attribute)
            }

            childAdded(attribute)
        }
    }

    override fun remove(attribute: Attribute?): Boolean {
        var answer = false
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            val list = attributesShadow

            answer = (list as MutableList<Any?>).remove(attribute)

            if (!answer) {
                // we may have a copy of the attribute
                val copy = attribute(attribute!!.qName)

                if (copy != null) {
                    (list as MutableList<Any?>).remove(copy)

                    answer = true
                }
            }
        } else if (attributesShadow != null) {
            if (attribute == attributesShadow) {
                this.attributes = null

                answer = true
            } else {
                // we may have a copy of the attribute
                val other = attributesShadow as Attribute

                if (attribute!!.qName == other.qName) {
                    attributes = null

                    answer = true
                }
            }
        }

        if (answer) {
            childRemoved(attribute)
        }

        return answer
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    override fun addNewNode(node: Node?) {
        val contentShadow = content

        if (contentShadow == null) {
            this.content = node
        } else {
            if (contentShadow is MutableList<*>) {
                val list = contentShadow

                (list as MutableList<Any?>).add(node)
            } else {
                val list = createContentList()

                list.add(contentShadow)

                list.add(node)

                this.content = list
            }
        }

        childAdded(node)
    }

    override fun removeNode(node: Node?): Boolean {
        var answer = false
        val contentShadow = content

        if (contentShadow != null) {
            if (contentShadow === node) {
                this.content = null

                answer = true
            } else if (contentShadow is MutableList<*>) {
                val list = contentShadow

                answer = (list as MutableList<Any?>).remove(node)
            }
        }

        if (answer) {
            childRemoved(node)
        }

        return answer
    }

    override fun contentList(): MutableList<Any?> {
        val contentShadow = content

        if (contentShadow is MutableList<*>) {
            return contentShadow as MutableList<Any?>
        } else {
            val list = createContentList()

            if (contentShadow != null) {
                list.add(contentShadow)
            }

            this.content = list

            return list
        }
    }

    override fun attributeList(): MutableList<Any?> {
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            return attributesShadow as MutableList<Any?>
        } else if (attributesShadow != null) {
            val list = createAttributeList()

            list.add(attributesShadow)

            this.attributes = list

            return list
        } else {
            val list = createAttributeList()

            this.attributes = list

            return list
        }
    }

    override fun attributeList(size: Int): MutableList<Any?> {
        val attributesShadow = this.attributes

        if (attributesShadow is MutableList<*>) {
            return attributesShadow as MutableList<Any?>
        } else if (attributesShadow != null) {
            val list = createAttributeList(size)

            list.add(attributesShadow)

            this.attributes = list

            return list
        } else {
            val list = createAttributeList(size)

            this.attributes = list

            return list
        }
    }

    protected fun setAttributeList(attributeList: MutableList<*>?) {
        this.attributes = attributeList
    }

    override fun getDocumentFactory(): DocumentFactory {
        val factory = qname!!.documentFactory

        return if (factory != null) factory else DOCUMENT_FACTORY
    }

    companion object {
        /** The `DocumentFactory` instance used by default  */
        @Transient
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


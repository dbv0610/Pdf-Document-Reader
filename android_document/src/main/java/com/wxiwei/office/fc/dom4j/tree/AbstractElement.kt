/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.CDATA
import com.wxiwei.office.fc.dom4j.CharacterData
import com.wxiwei.office.fc.dom4j.Comment
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Entity
import com.wxiwei.office.fc.dom4j.IllegalAddException
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.Text
import com.wxiwei.office.fc.dom4j.Visitor
import com.wxiwei.office.fc.dom4j.io.OutputFormat
import com.wxiwei.office.fc.dom4j.io.XMLWriter
import org.xml.sax.Attributes
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.Collections

/**
 * `AbstractElement` is an abstract base class for tree
 * implementors to use for implementation inheritence.
 *
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.80 $
 */
abstract class AbstractElement : AbstractBranch(), Element {

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    override val nodeType: Short
        get() = Node.ELEMENT_NODE

    override val isRootElement: Boolean
        get() {
            val document = this.document

            if (document != null) {
                val root = document.rootElement

                if (root === this) {
                    return true
                }
            }

            return false
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNameProperty")
    @set:JvmName("setNameProperty")
    override var name: String?
        get() = qName!!.name
        set(name) {
            qName = getDocumentFactory().createQName(name)
        }

    open fun setNamespace(namespace: Namespace?) {
        qName = getDocumentFactory().createQName(name, namespace)
    }

    /**
     * Returns the XPath expression to match this Elements name which is
     * getQualifiedName() if there is a namespace prefix defined or if no
     * namespace is present then it is getName() or if a namespace is defined
     * with no prefix then the expression is [name()='X'] where X = getName().
     */
    val xPathNameStep: String?
        get() {
            val uri = namespaceURI

            if ((uri == null) || (uri.length == 0)) {
                return name
            }

            val prefix = namespacePrefix

            if ((prefix == null) || (prefix.length == 0)) {
                return "*[name()='" + name + "']"
            }

            return qualifiedName
        }

    override fun getPath(context: Element?): String? {
        if (this === context) {
            return "."
        }

        val parent = this.parent

        if (parent == null) {
            return "/" + this.xPathNameStep
        } else if (parent === context) {
            return this.xPathNameStep
        }

        return parent.getPath(context) + "/" + this.xPathNameStep
    }

    override fun getUniquePath(context: Element?): String? {
        val parent = this.parent

        if (parent == null) {
            return "/" + this.xPathNameStep
        }

        val buffer = StringBuffer()

        if (parent !== context) {
            buffer.append(parent.getUniquePath(context))

            buffer.append("/")
        }

        buffer.append(this.xPathNameStep)

        val mySiblings = parent.elements(qName)!!

        if (mySiblings.size > 1) {
            var idx = mySiblings.indexOf(this)

            if (idx >= 0) {
                buffer.append("[")

                buffer.append((++idx).toString())

                buffer.append("]")
            }
        }

        return buffer.toString()
    }

    override fun asXML(): String? {
        try {
            val out = StringWriter()
            val writer = XMLWriter(out, OutputFormat())

            writer.write(this as Element)
            writer.flush()

            return out.toString()
        } catch (e: IOException) {
            throw RuntimeException(
                ("IOException while generating " + "textual representation: "
                        + e.message)
            )
        }
    }

    @Throws(IOException::class)
    override fun write(writer: Writer?) {
        val xmlWriter = XMLWriter(writer!!, OutputFormat())
        xmlWriter.write(this as Element)
    }

    /**
     * `accept` method is the `Visitor Pattern`
     * method.
     *
     * @param visitor
     * `Visitor` is the visitor.
     */
    override fun accept(visitor: Visitor?) {
        visitor!!.visit(this as Element)

        // visit attributes
        run {
            var i = 0
            val size = attributeCount()
            while (i < size) {
                val attribute = attribute(i)

                visitor.visit(attribute)
                i++
            }
        }

        // visit content
        var i = 0
        val size = nodeCount()
        while (i < size) {
            val node = node(i)

            node!!.accept(visitor)
            i++
        }
    }

    override fun toString(): String {
        val uri = namespaceURI

        if ((uri != null) && (uri.length > 0)) {
            if (VERBOSE_TOSTRING) {
                return (super.toString() + " [Element: <" + qualifiedName + " uri: " + uri
                        + " attributes: " + attributeList() + " content: " + contentList() + " />]")
            } else {
                return (super.toString() + " [Element: <" + qualifiedName + " uri: " + uri
                        + " attributes: " + attributeList() + "/>]")
            }
        } else {
            if (VERBOSE_TOSTRING) {
                return (super.toString() + " [Element: <" + qualifiedName + " attributes: "
                        + attributeList() + " content: " + contentList() + " />]")
            } else {
                return (super.toString() + " [Element: <" + qualifiedName + " attributes: "
                        + attributeList() + "/>]")
            }
        }
    }

    // QName methods
    // -------------------------------------------------------------------------
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespaceProperty")
    override val namespace: Namespace?
        get() = qName!!.namespace

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespacePrefixProperty")
    override val namespacePrefix: String?
        get() = qName!!.namespacePrefix

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespaceURIProperty")
    override val namespaceURI: String?
        get() = qName!!.namespaceURI

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getQualifiedNameProperty")
    override val qualifiedName: String?
        get() = qName!!.qualifiedName

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDataProperty")
    @set:JvmName("setDataProperty")
    override var data: Any?
        get() = text
        set(data) {
            // ignore this method
        }

    // Node methods
    // -------------------------------------------------------------------------
    override fun node(index: Int): Node? {
        if (index >= 0) {
            val list = contentList()

            if (index >= list.size) {
                return null
            }

            val node: Any? = list[index]

            if (node != null) {
                if (node is Node) {
                    return node
                } else {
                    return getDocumentFactory().createText(node.toString())
                }
            }
        }

        return null
    }

    override fun indexOf(node: Node?): Int {
        return contentList().indexOf(node)
    }

    override fun nodeCount(): Int {
        return contentList().size
    }

    override fun nodeIterator(): MutableIterator<*>? {
        return contentList().iterator()
    }

    // Element methods
    // -------------------------------------------------------------------------
    override fun element(name: String?): Element? {
        val list = contentList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Element) {
                if (name == `object`.name) {
                    return `object`
                }
            }
        }

        return null
    }

    override fun element(qName: QName?): Element? {
        val list = contentList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Element) {
                if (qName == `object`.qName) {
                    return `object`
                }
            }
        }

        return null
    }

    open fun element(name: String?, namespace: Namespace?): Element? {
        return element(getDocumentFactory().createQName(name, namespace))
    }

    override fun elements(): MutableList<*>? {
        val list = contentList()

        val answer = createResultList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Element) {
                answer.addLocal(`object`)
            }
        }

        return answer
    }

    override fun elements(name: String?): MutableList<*>? {
        val list = contentList()

        val answer = createResultList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Element) {
                if (name == `object`.name) {
                    answer.addLocal(`object`)
                }
            }
        }

        return answer
    }

    override fun elements(qName: QName?): MutableList<*>? {
        val list = contentList()

        val answer = createResultList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Element) {
                if (qName == `object`.qName) {
                    answer.addLocal(`object`)
                }
            }
        }

        return answer
    }

    open fun elements(name: String?, namespace: Namespace?): MutableList<*>? {
        return elements(getDocumentFactory().createQName(name, namespace))
    }

    override fun elementIterator(): MutableIterator<*>? {
        val list = elements()

        return list!!.iterator()
    }

    override fun elementIterator(name: String?): MutableIterator<*>? {
        val list = elements(name)

        return list!!.iterator()
    }

    override fun elementIterator(qName: QName?): MutableIterator<*>? {
        val list = elements(qName)

        return list!!.iterator()
    }

    open fun elementIterator(name: String?, ns: Namespace?): MutableIterator<*>? {
        return elementIterator(getDocumentFactory().createQName(name, ns))
    }

    // Attribute methods
    // -------------------------------------------------------------------------
    override fun attributes(): MutableList<*>? {
        return ContentListFacade(this, attributeList())
    }

    override fun attributeIterator(): MutableIterator<*>? {
        return attributeList().iterator()
    }

    override fun attribute(index: Int): Attribute? {
        return attributeList()[index] as Attribute?
    }

    override fun attributeCount(): Int {
        return attributeList().size
    }

    override fun attribute(name: String?): Attribute? {
        val list = attributeList()

        val size = list.size

        for (i in 0 until size) {
            val attribute = list[i] as Attribute

            if (name == attribute.name) {
                return attribute
            }
        }

        return null
    }

    override fun attribute(qName: QName?): Attribute? {
        val list = attributeList()

        val size = list.size

        for (i in 0 until size) {
            val attribute = list[i] as Attribute

            if (qName == attribute.qName) {
                return attribute
            }
        }

        return null
    }

    open fun attribute(name: String?, namespace: Namespace?): Attribute? {
        return attribute(getDocumentFactory().createQName(name, namespace))
    }

    /**
     * This method provides a more optimal way of setting all the attributes on
     * an Element particularly for use in [org.dom4j.io.SAXReader].
     *
     * @param attributes
     * DOCUMENT ME!
     * @param namespaceStack
     * DOCUMENT ME!
     * @param noNamespaceAttributes
     * DOCUMENT ME!
     */
    open fun setAttributes(
        attributes: Attributes, namespaceStack: NamespaceStack,
        noNamespaceAttributes: Boolean
    ) {
        // now lets add all attribute values
        val size = attributes.length

        if (size > 0) {
            val factory = getDocumentFactory()

            if (size == 1) {
                // allow lazy construction of the List of Attributes
                val name = attributes.getQName(0)

                if (noNamespaceAttributes || !name.startsWith("xmlns")) {
                    val attributeURI = attributes.getURI(0)

                    val attributeLocalName = attributes.getLocalName(0)

                    val attributeValue = attributes.getValue(0)

                    val attributeQName = namespaceStack.getAttributeQName(
                        attributeURI,
                        attributeLocalName, name
                    )

                    add(factory.createAttribute(this, attributeQName, attributeValue))
                }
            } else {
                val list = attributeList(size)

                list.clear()

                for (i in 0 until size) {
                    // optimised to avoid the call to attribute(QName) to
                    // lookup an attribute for a given QName
                    val attributeName = attributes.getQName(i)

                    if (noNamespaceAttributes || !attributeName.startsWith("xmlns")) {
                        val attributeURI = attributes.getURI(i)

                        val attributeLocalName = attributes.getLocalName(i)

                        val attributeValue = attributes.getValue(i)

                        val attributeQName = namespaceStack.getAttributeQName(
                            attributeURI,
                            attributeLocalName, attributeName
                        )

                        val attribute = factory.createAttribute(
                            this, attributeQName,
                            attributeValue
                        )

                        list.add(attribute)

                        childAdded(attribute)
                    }
                }
            }
        }
    }

    override fun attributeValue(name: String?): String? {
        val attrib = attribute(name)

        if (attrib == null) {
            return null
        } else {
            return attrib.value
        }
    }

    override fun attributeValue(qName: QName?): String? {
        val attrib = attribute(qName)

        if (attrib == null) {
            return null
        } else {
            return attrib.value
        }
    }

    override fun attributeValue(name: String?, defaultValue: String?): String? {
        val answer = attributeValue(name)

        return answer ?: defaultValue
    }

    override fun attributeValue(qName: QName?, defaultValue: String?): String? {
        val answer = attributeValue(qName)

        return answer ?: defaultValue
    }

    /**
     * @deprecated As of version 0.5. Please use
     * #addAttribute(String,String) instead. WILL BE REMOVED IN dom4j-1.6 !!
     */
    @Deprecated("As of version 0.5. Please use addAttribute(String,String) instead.")
    override fun setAttributeValue(name: String?, value: String?) {
        addAttribute(name, value)
    }

    /**
     * @deprecated As of version 0.5. Please use
     * #addAttribute(String,String) instead. WILL BE REMOVED IN dom4j-1.6 !!
     */
    @Deprecated("As of version 0.5. Please use addAttribute(QName,String) instead.")
    override fun setAttributeValue(qName: QName?, value: String?) {
        addAttribute(qName, value)
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
            attributeList().add(attribute)

            childAdded(attribute)
        }
    }

    override fun remove(attribute: Attribute?): Boolean {
        val list = attributeList()

        var answer = list.remove(attribute)

        if (answer) {
            childRemoved(attribute)
        } else {
            // we may have a copy of the attribute
            val copy = attribute(attribute!!.qName)

            if (copy != null) {
                list.remove(copy)

                answer = true
            }
        }

        return answer
    }

    // Processing instruction API
    // -------------------------------------------------------------------------
    override fun processingInstructions(): MutableList<*>? {
        val list = contentList()

        val answer = createResultList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is ProcessingInstruction) {
                answer.addLocal(`object`)
            }
        }

        return answer
    }

    override fun processingInstructions(target: String?): MutableList<*>? {
        val list = contentList()

        val answer = createResultList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is ProcessingInstruction) {
                if (target == `object`.name) {
                    answer.addLocal(`object`)
                }
            }
        }

        return answer
    }

    override fun processingInstruction(target: String?): ProcessingInstruction? {
        val list = contentList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is ProcessingInstruction) {
                if (target == `object`.name) {
                    return `object`
                }
            }
        }

        return null
    }

    override fun removeProcessingInstruction(target: String?): Boolean {
        val list = contentList()

        val iter = list.iterator()
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

    // Content Model methods
    // -------------------------------------------------------------------------
    override fun getXPathResult(index: Int): Node? {
        val answer = node(index)

        if ((answer != null) && !answer.supportsParent()) {
            return answer.asXPathResult(this)
        }

        return answer
    }

    override fun addAttribute(name: String?, value: String?): Element? {
        // adding a null value is equivalent to removing the attribute
        val attribute = attribute(name)

        if (value != null) {
            if (attribute == null) {
                add(getDocumentFactory().createAttribute(this, name, value))
            } else if (attribute.isReadOnly) {
                remove(attribute)

                add(getDocumentFactory().createAttribute(this, name, value))
            } else {
                attribute.value = value
            }
        } else if (attribute != null) {
            remove(attribute)
        }

        return this
    }

    override fun addAttribute(qName: QName?, value: String?): Element? {
        // adding a null value is equivalent to removing the attribute
        val attribute = attribute(qName)

        if (value != null) {
            if (attribute == null) {
                add(getDocumentFactory().createAttribute(this, qName, value))
            } else if (attribute.isReadOnly) {
                remove(attribute)

                add(getDocumentFactory().createAttribute(this, qName, value))
            } else {
                attribute.value = value
            }
        } else if (attribute != null) {
            remove(attribute)
        }

        return this
    }

    override fun addCDATA(cdata: String?): Element? {
        val node = getDocumentFactory().createCDATA(cdata)

        addNewNode(node)

        return this
    }

    override fun addComment(comment: String?): Element? {
        val node = getDocumentFactory().createComment(comment)

        addNewNode(node)

        return this
    }

    override fun addElement(name: String?): Element? {
        val factory = getDocumentFactory()

        val index = name!!.indexOf(":")

        var prefix = ""

        var localName: String = name

        val namespace: Namespace?

        if (index > 0) {
            prefix = name.substring(0, index)

            localName = name.substring(index + 1)

            namespace = getNamespaceForPrefix(prefix)

            if (namespace == null) {
                throw IllegalAddException(
                    ("No such namespace prefix: " + prefix
                            + " is in scope on: " + this + " so cannot add element: " + name)
                )
            }
        } else {
            namespace = getNamespaceForPrefix("")
        }

        val node: Element?

        if (namespace != null) {
            val qname = factory.createQName(localName, namespace)

            node = factory.createElement(qname)
        } else {
            node = factory.createElement(name)
        }

        addNewNode(node)

        return node
    }

    override fun addEntity(name: String?, text: String?): Element? {
        val node = getDocumentFactory().createEntity(name, text)

        addNewNode(node)

        return this
    }

    override fun addNamespace(prefix: String?, uri: String?): Element? {
        val node = getDocumentFactory().createNamespace(prefix, uri)
        addNewNode(node)
        return this
    }

    override fun addProcessingInstruction(target: String?, text: String?): Element? {
        val node = getDocumentFactory().createProcessingInstruction(target, text)
        addNewNode(node)
        return this
    }

    override fun addProcessingInstruction(target: String?, data: MutableMap<*, *>?): Element? {
        val node = getDocumentFactory().createProcessingInstruction(target, data)

        addNewNode(node)

        return this
    }

    override fun addText(text: String?): Element? {
        val node = getDocumentFactory().createText(text)

        addNewNode(node)

        return this
    }

    // polymorphic node methods
    override fun add(node: Node?) {
        when (node!!.nodeType) {
            Node.ELEMENT_NODE -> add(node as Element?)

            Node.ATTRIBUTE_NODE -> add(node as Attribute?)

            Node.TEXT_NODE -> add(node as Text?)

            Node.CDATA_SECTION_NODE -> add(node as CDATA?)

            Node.ENTITY_REFERENCE_NODE -> add(node as Entity?)

            Node.PROCESSING_INSTRUCTION_NODE -> add(node as ProcessingInstruction?)

            Node.COMMENT_NODE -> add(node as Comment?)

            Node.NAMESPACE_NODE -> add(node as Namespace?)

            else -> invalidNodeTypeAddException(node)
        }
    }

    override fun remove(node: Node?): Boolean {
        when (node!!.nodeType) {
            Node.ELEMENT_NODE -> return remove(node as Element?)

            Node.ATTRIBUTE_NODE -> return remove(node as Attribute?)

            Node.TEXT_NODE -> return remove(node as Text?)

            Node.CDATA_SECTION_NODE -> return remove(node as CDATA?)

            Node.ENTITY_REFERENCE_NODE -> return remove(node as Entity?)

            Node.PROCESSING_INSTRUCTION_NODE -> return remove(node as ProcessingInstruction?)

            Node.COMMENT_NODE -> return remove(node as Comment?)

            Node.NAMESPACE_NODE -> return remove(node as Namespace?)

            else -> return false
        }
    }

    // typesafe versions using node classes
    override fun add(cdata: CDATA?) {
        addNode(cdata)
    }

    override fun add(comment: Comment?) {
        addNode(comment)
    }

    override fun add(element: Element?) {
        addNode(element)
    }

    override fun add(entity: Entity?) {
        addNode(entity)
    }

    override fun add(namespace: Namespace?) {
        addNode(namespace)
    }

    override fun add(pi: ProcessingInstruction?) {
        addNode(pi)
    }

    override fun add(text: Text?) {
        addNode(text)
    }

    override fun remove(cdata: CDATA?): Boolean {
        return removeNode(cdata)
    }

    override fun remove(comment: Comment?): Boolean {
        return removeNode(comment)
    }

    override fun remove(element: Element?): Boolean {
        return removeNode(element)
    }

    override fun remove(entity: Entity?): Boolean {
        return removeNode(entity)
    }

    override fun remove(namespace: Namespace?): Boolean {
        return removeNode(namespace)
    }

    override fun remove(pi: ProcessingInstruction?): Boolean {
        return removeNode(pi)
    }

    override fun remove(text: Text?): Boolean {
        return removeNode(text)
    }

    // Helper methods
    // -------------------------------------------------------------------------
    override fun hasMixedContent(): Boolean {
        val content = contentList()

        if (content.isEmpty() || (content.size < 2)) {
            return false
        }

        var prevClass: Class<*>? = null

        val iter = content.iterator()
        while (iter.hasNext()) {
            val `object`: Any = iter.next()!!

            val newClass: Class<*> = `object`.javaClass

            if (newClass != prevClass) {
                if (prevClass != null) {
                    return true
                }

                prevClass = newClass
            }
        }

        return false
    }

    override val isTextOnly: Boolean
        get() {
            val content = contentList()

            if (content.isEmpty()) {
                return true
            }

            val iter = content.iterator()
            while (iter.hasNext()) {
                val `object` = iter.next()

                if (`object` !is CharacterData && `object` !is String) {
                    return false
                }
            }

            return true
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() = super<AbstractBranch>.text
        set(text) {
            /* remove all text nodes */
            val allContent = contentList()

            val it = allContent.iterator()

            while (it.hasNext()) {
                val node = it.next() as Node

                when (node.nodeType) {
                    Node.CDATA_SECTION_NODE, Node.ENTITY_REFERENCE_NODE, Node.TEXT_NODE -> it.remove()

                    else -> {}
                }
            }

            addText(text)
        }

    override val textTrim: String?
        get() = super<AbstractBranch>.textTrim

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String?
        get() {
            val list = contentList()

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

            return ""
        }

    /**
     * Puts all `Text` nodes in the full depth of the sub-tree
     * underneath this `Node`, including attribute nodes, into a
     * "normal" form where only structure (e.g., elements, comments, processing
     * instructions, CDATA sections, and entity references) separates
     * `Text` nodes, i.e., there are neither adjacent
     * `Text` nodes nor empty `Text` nodes.
     *
     * @since DOM Level 2
     */
    override fun normalize() {
        val content = contentList()

        var previousText: Text? = null

        var i = 0

        while (i < content.size) {
            val node = content[i] as Node?

            if (node is Text) {
                if (previousText != null) {
                    previousText.appendText(node.text)

                    remove(node as Text?)
                } else {
                    val value = node.text

                    // only remove empty Text nodes, not whitespace nodes
                    // if ( value == null || value.trim().length() <= 0 ) {
                    if ((value == null) || (value.length <= 0)) {
                        remove(node as Text?)
                    } else {
                        previousText = node

                        i++
                    }
                }
            } else {
                if (node is Element) {
                    node.normalize()
                }

                previousText = null

                i++
            }
        }
    }

    override fun elementText(name: String?): String? {
        val element = element(name)

        return element?.text
    }

    override fun elementText(qname: QName?): String? {
        val element = element(qname)

        return element?.text
    }

    override fun elementTextTrim(name: String?): String? {
        val element = element(name)

        return element?.textTrim
    }

    override fun elementTextTrim(qname: QName?): String? {
        val element = element(qname)

        return element?.textTrim
    }

    // add to me content from another element
    // analagous to the addAll(collection) methods in Java 2 collections
    override fun appendAttributes(element: Element?) {
        var i = 0
        val size = element!!.attributeCount()
        while (i < size) {
            val attribute = element.attribute(i)

            if (attribute!!.supportsParent()) {
                addAttribute(attribute.qName, attribute.value)
            } else {
                add(attribute)
            }
            i++
        }
    }

    /*
     * public Object clone() { Element clone = createElement(getQName());
     * clone.appendAttributes(this); clone.appendContent(this); return clone; }
     */
    override fun createCopy(): Element? {
        val clone = createElement(qName)

        clone.appendAttributes(this)

        clone.appendContent(this)

        return clone
    }

    override fun createCopy(name: String?): Element? {
        val clone = createElement(name)

        clone.appendAttributes(this)

        clone.appendContent(this)

        return clone
    }

    override fun createCopy(qName: QName?): Element? {
        val clone = createElement(qName)

        clone.appendAttributes(this)

        clone.appendContent(this)

        return clone
    }

    override fun getQName(qualifiedName: String?): QName? {
        var prefix = ""

        var localName: String? = qualifiedName

        val index = qualifiedName!!.indexOf(":")

        if (index > 0) {
            prefix = qualifiedName.substring(0, index)

            localName = qualifiedName.substring(index + 1)
        }

        val namespace = getNamespaceForPrefix(prefix)

        if (namespace != null) {
            return getDocumentFactory().createQName(localName, namespace)
        } else {
            return getDocumentFactory().createQName(localName)
        }
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
            val list = contentList()

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is Namespace) {
                    if (prefix == `object`.getPrefix()) {
                        return `object`
                    }
                }
            }
        }

        val parent = this.parent

        if (parent != null) {
            val answer = parent.getNamespaceForPrefix(prefix)

            if (answer != null) {
                return answer
            }
        }

        if (prefix.length <= 0) {
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
            val list = contentList()

            val size = list.size

            for (i in 0 until size) {
                val `object`: Any? = list[i]

                if (`object` is Namespace) {
                    if (uri == `object`.getURI()) {
                        return `object`
                    }
                }
            }

            return null
        }
    }

    override fun getNamespacesForURI(uri: String?): MutableList<*>? {
        val answer = createResultList()

        // if (getNamespaceURI().equals(uri)) {
        //
        // answer.addLocal(getNamespace());
        //
        // }
        val list = contentList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if ((`object` is Namespace) && `object`.getURI() == uri) {
                answer.addLocal(`object`)
            }
        }

        return answer
    }

    override fun declaredNamespaces(): MutableList<*>? {
        val answer = createResultList()

        // if (getNamespaceURI().length() > 0) {
        //
        // answer.addLocal(getNamespace());
        //
        // }
        //
        val list = contentList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Namespace) {
                answer.addLocal(`object`)
            }
        }

        return answer
    }

    override fun additionalNamespaces(): MutableList<*>? {
        val list = contentList()

        val size = list.size

        val answer = createResultList()

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Namespace) {
                if (`object` != namespace) {
                    answer.addLocal(`object`)
                }
            }
        }

        return answer
    }

    open fun additionalNamespaces(defaultNamespaceURI: String?): MutableList<*>? {
        val list = contentList()

        val answer = createResultList()

        val size = list.size

        for (i in 0 until size) {
            val `object`: Any? = list[i]

            if (`object` is Namespace) {
                if (defaultNamespaceURI != `object`.getURI()) {
                    answer.addLocal(`object`)
                }
            }
        }

        return answer
    }

    // Implementation helper methods
    // -------------------------------------------------------------------------
    /**
     * Ensures that the list of attributes has the given size
     *
     * @param minCapacity
     * DOCUMENT ME!
     */
    open fun ensureAttributesCapacity(minCapacity: Int) {
        if (minCapacity > 1) {
            val list = attributeList()

            if (list is ArrayList<*>) {
                list.ensureCapacity(minCapacity)
            }
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected open fun createElement(name: String?): Element {
        return getDocumentFactory().createElement(name)!!
    }

    protected open fun createElement(qName: QName?): Element {
        return getDocumentFactory().createElement(qName)!!
    }

    override fun addNode(node: Node?) {
        if (node!!.parent != null) {
            // XXX: could clone here
            val message = ("The Node already has an existing parent of \""
                    + node.parent!!.qualifiedName + "\"")

            throw IllegalAddException(this as Element, node, message)
        }

        addNewNode(node)
    }

    override fun addNode(index: Int, node: Node?) {
        if (node!!.parent != null) {
            // XXX: could clone here
            val message = ("The Node already has an existing parent of \""
                    + node.parent!!.qualifiedName + "\"")

            throw IllegalAddException(this as Element, node, message)
        }

        addNewNode(index, node)
    }

    /**
     * Like addNode() but does not require a parent check
     *
     * @param node
     * DOCUMENT ME!
     */
    protected open fun addNewNode(node: Node?) {
        contentList().add(node)

        childAdded(node)
    }

    protected open fun addNewNode(index: Int, node: Node?) {
        contentList().add(index, node)

        childAdded(node)
    }

    override fun removeNode(node: Node?): Boolean {
        val answer = contentList().remove(node)

        if (answer) {
            childRemoved(node)
        }

        return answer
    }

    /**
     * Called when a new child node is added to create any parent relationships
     *
     * @param node
     * DOCUMENT ME!
     */
    override fun childAdded(node: Node?) {
        if (node != null) {
            node.parent = this
        }
    }

    override fun childRemoved(node: Node?) {
        if (node != null) {
            node.parent = null

            node.document = null
        }
    }

    /**
     * @return the internal List used to store attributes or creates one if one
     * is not available
     */
    abstract fun attributeList(): MutableList<Any?>

    /**
     * @return the internal List used to store attributes or creates one with
     * the specified size if one is not available
     */
    abstract fun attributeList(attributeCount: Int): MutableList<Any?>

    override fun getDocumentFactory(): DocumentFactory {
        val qName = this.qName

        // QName might be null as we might not have been constructed yet
        if (qName != null) {
            val factory = qName.documentFactory

            if (factory != null) {
                return factory
            }
        }

        return DOCUMENT_FACTORY
    }

    /**
     * A Factory Method pattern which creates a List implementation used to
     * store attributes
     */
    protected fun createAttributeList(): MutableList<Any?> {
        return createAttributeList(DEFAULT_CONTENT_LIST_SIZE)
    }

    /**
     * A Factory Method pattern which creates a List implementation used to
     * store attributes
     */
    protected fun createAttributeList(size: Int): MutableList<Any?> {
        return ArrayList(size)
    }

    protected fun createSingleIterator(result: Any?): MutableIterator<Any?> {
        return SingleIterator(result)
    }

    companion object {
        /** The `DocumentFactory` instance used by default  */
        private val DOCUMENT_FACTORY: DocumentFactory = DocumentFactory.getInstance()

        @JvmField
        val EMPTY_LIST: MutableList<Any?> = Collections.emptyList<Any?>()

        @JvmField
        val EMPTY_ITERATOR: MutableIterator<Any?> = EMPTY_LIST.iterator()

        const val VERBOSE_TOSTRING: Boolean = false

        const val USE_STRINGVALUE_SEPARATOR: Boolean = false
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


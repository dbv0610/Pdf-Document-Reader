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
package com.wxiwei.office.fc.dom4j

/**
 * 
 * 
 * `Element` interface defines an XML element. An element can have
 * declared namespaces, attributes, child nodes and textual content.
 * 
 * 
 * 
 * 
 * Some of this interface is optional. Some implementations may be read-only and
 * not support being modified. Some implementations may not support the parent
 * relationship and methods such as [.getParent]or {@link#getDocument}.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.47 $
 */
interface Element : Branch {
    // Name and namespace related methods
    // -------------------------------------------------------------------------
    /**
     * 
     * 
     * Returns the `QName` of this element which represents the
     * local name, the qualified name and the `Namespace`.
     * 
     * 
     * @return the `QName` associated with this element
     */
    /**
     * 
     * 
     * Sets the `QName` of this element which represents the local
     * name, the qualified name and the `Namespace`.
     * 
     * 
     * @param qname
     * is the `QName` to be associated with this element
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getQNameProperty")
    @set:JvmName("setQNameProperty")
    var qName: QName?
    fun getQName(): QName? = qName
    fun setQName(qName: QName?) { this.qName = qName }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespaceProperty")
    val namespace: Namespace?
    fun getNamespace(): Namespace? = namespace

    /**
     * 
     * 
     * Returns the `QName` for the given qualified name, using the
     * namespace URI in scope for the given prefix of the qualified name or the
     * default namespace if the qualified name has no prefix.
     * 
     * 
     * @param qualifiedName
     * DOCUMENT ME!
     * 
     * @return the `QName` for the given qualified name
     */
    fun getQName(qualifiedName: String?): QName?

    /**
     * 
     * 
     * Returns the `Namespace` which is mapped to the given prefix
     * or null if it could not be found.
     * 
     * 
     * @param prefix
     * DOCUMENT ME!
     * 
     * @return the `Namespace` associated with the given prefix
     */
    fun getNamespaceForPrefix(prefix: String?): Namespace?

    /**
     * 
     * 
     * Returns the `Namespace` which is mapped to the given URI or
     * null if it could not be found. If there is more than one
     * `Namespace` mapped to the URI, which of them will be
     * returned is undetermined.
     * 
     * 
     * @param uri
     * DOCUMENT ME!
     * 
     * @return the `Namespace` associated with the given URI
     */
    fun getNamespaceForURI(uri: String?): Namespace?

    /**
     * 
     * 
     * Returns the all namespaces which are mapped to the given URI or an empty
     * list if no such namespaces could be found.
     * 
     * 
     * @param uri
     * DOCUMENT ME!
     * 
     * @return the namespaces associated with the given URI
     * 
     * @since 1.5
     */
    fun getNamespacesForURI(uri: String?): MutableList<*>?

    /**
     * 
     * 
     * Returns the namespace prefix of this element if one exists otherwise an
     * empty `String` is returned.
     * 
     * 
     * @return the prefix of the `Namespace` of this element or an
     * empty `String`
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespacePrefixProperty")
    val namespacePrefix: String?
    fun getNamespacePrefix(): String? = namespacePrefix

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespaceURIProperty")
    val namespaceURI: String?
    fun getNamespaceURI(): String? = namespaceURI

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getQualifiedNameProperty")
    val qualifiedName: String?
    fun getQualifiedName(): String? = qualifiedName

    /**
     * 
     * 
     * Returns any additional namespaces declarations for this element other
     * than namespace returned via the [.getNamespace]method. If no
     * additional namespace declarations are present for this element then an
     * empty list will be returned. The list is backed by the element such that
     * changes to the list will be reflected in the element though the reverse
     * is not the case.
     * 
     * 
     * @return a list of any additional namespace declarations.
     */
    fun additionalNamespaces(): MutableList<*>?

    /**
     * 
     * 
     * Returns all the namespaces declared by this element. If no namespaces are
     * declared for this element then an empty list will be returned. The list
     * is backed by the element such that changes to the list will be reflected
     * in the element though the reverse is not the case.
     * 
     * 
     * @return a list of namespaces declared for this element.
     */
    fun declaredNamespaces(): MutableList<*>?

    // Builder methods
    // -------------------------------------------------------------------------
    /**
     * 
     * 
     * Adds the attribute value of the given local name. If an attribute already
     * exists for the given name it will be replaced. Attributes with null
     * values are silently ignored. If the value of the attribute is null then
     * this method call will remove any attributes with the given name.
     * 
     * 
     * @param name
     * is the name of the attribute whose value is to be added or
     * updated
     * @param value
     * is the attribute's value
     * 
     * @return this `Element` instance.
     */
    fun addAttribute(name: String?, value: String?): Element?

    /**
     * 
     * 
     * Adds the attribute value of the given fully qualified name. If an
     * attribute already exists for the given name it will be replaced.
     * Attributes with null values are silently ignored. If the value of the
     * attribute is null then this method call will remove any attributes with
     * the given name.
     * 
     * 
     * @param qName
     * is the fully qualified name of the attribute whose value is to
     * be added or updated
     * @param value
     * is the attribute's value
     * 
     * @return this `Element` instance.
     */
    fun addAttribute(qName: QName?, value: String?): Element?

    /**
     * Adds a new `Comment` node with the given text to this
     * element.
     * 
     * @param comment
     * is the text for the `Comment` node.
     * 
     * @return this `Element` instance.
     */
    fun addComment(comment: String?): Element?

    /**
     * Adds a new `CDATA` node with the given text to this element.
     * 
     * @param cdata
     * is the text for the `CDATA` node.
     * 
     * @return this `Element` instance.
     */
    fun addCDATA(cdata: String?): Element?

    /**
     * Adds a new `Entity` node with the given name and text to
     * this element and returns a reference to the new node.
     * 
     * @param name
     * is the name for the `Entity` node.
     * @param text
     * is the text for the `Entity` node.
     * 
     * @return this `Element` instance.
     */
    fun addEntity(name: String?, text: String?): Element?

    /**
     * Adds a namespace to this element for use by its child content
     * 
     * @param prefix
     * is the prefix to use, which should not be null or blank
     * @param uri
     * is the namespace URI
     * 
     * @return this `Element` instance.
     */
    fun addNamespace(prefix: String?, uri: String?): Element?

    /**
     * Adds a processing instruction for the given target
     * 
     * @param target
     * is the target of the processing instruction
     * @param text
     * is the textual data (key/value pairs) of the processing
     * instruction
     * 
     * @return this `Element` instance.
     */
    fun addProcessingInstruction(target: String?, text: String?): Element?

    /**
     * Adds a processing instruction for the given target
     * 
     * @param target
     * is the target of the processing instruction
     * @param data
     * is a Map of the key / value pairs of the processing
     * instruction
     * 
     * @return this `Element` instance.
     */
    fun addProcessingInstruction(target: String?, data: MutableMap<*, *>?): Element?

    /**
     * Adds a new `Text` node with the given text to this element.
     * 
     * @param text
     * is the text for the `Text` node.
     * 
     * @return this `Element` instance.
     */
    fun addText(text: String?): Element?

    // Typesafe modifying methods
    // -------------------------------------------------------------------------
    /**
     * Adds the given `Attribute` to this element. If the given
     * node already has a parent defined then an
     * `IllegalAddException` will be thrown. Attributes with null
     * values are silently ignored.
     * 
     * 
     * 
     * If the value of the attribute is null then this method call will remove
     * any attributes with the QName of this attribute.
     * 
     * 
     * @param attribute
     * is the attribute to be added
     */
    fun add(attribute: Attribute?)

    /**
     * Adds the given `CDATA` to this element. If the given node
     * already has a parent defined then an `IllegalAddException`
     * will be thrown.
     * 
     * @param cdata
     * is the CDATA to be added
     */
    fun add(cdata: CDATA?)

    /**
     * Adds the given `Entity` to this element. If the given node
     * already has a parent defined then an `IllegalAddException`
     * will be thrown.
     * 
     * @param entity
     * is the entity to be added
     */
    fun add(entity: Entity?)

    /**
     * Adds the given `Text` to this element. If the given node
     * already has a parent defined then an `IllegalAddException`
     * will be thrown.
     * 
     * @param text
     * is the text to be added
     */
    fun add(text: Text?)

    /**
     * Adds the given `Namespace` to this element. If the given
     * node already has a parent defined then an
     * `IllegalAddException` will be thrown.
     * 
     * @param namespace
     * is the namespace to be added
     */
    fun add(namespace: Namespace?)

    /**
     * Removes the given `Attribute` from this element.
     * 
     * @param attribute
     * is the attribute to be removed
     * 
     * @return true if the attribute was removed
     */
    fun remove(attribute: Attribute?): Boolean

    /**
     * Removes the given `CDATA` if the node is an immediate child
     * of this element. If the given node is not an immediate child of this
     * element then the [detach]method should be used instead.
     * 
     * @param cdata
     * is the CDATA to be removed
     * 
     * @return true if the cdata was removed
     */
    fun remove(cdata: CDATA?): Boolean

    /**
     * Removes the given `Entity` if the node is an immediate child
     * of this element. If the given node is not an immediate child of this
     * element then the [detach]method should be used instead.
     * 
     * @param entity
     * is the entity to be removed
     * 
     * @return true if the entity was removed
     */
    fun remove(entity: Entity?): Boolean

    /**
     * Removes the given `Namespace` if the node is an immediate
     * child of this element. If the given node is not an immediate child of
     * this element then the [detach]method should be used
     * instead.
     * 
     * @param namespace
     * is the namespace to be removed
     * 
     * @return true if the namespace was removed
     */
    fun remove(namespace: Namespace?): Boolean

    /**
     * Removes the given `Text` if the node is an immediate child
     * of this element. If the given node is not an immediate child of this
     * element then the [detach]method should be used instead.
     * 
     * @param text
     * is the text to be removed
     * 
     * @return true if the text was removed
     */
    fun remove(text: Text?): Boolean

    // Text methods
    // -------------------------------------------------------------------------
    /**
     * Returns the text value of this element without recursing through child
     * elements. This method iterates through all [Text],[CDATA]
     * and [Entity]nodes that this element contains and appends the text
     * values together.
     * 
     * @return the textual content of this Element. Child elements are not
     * navigated. This method does not return null;
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?

    /**
     * DOCUMENT ME!
     * 
     * @return the trimmed text value where whitespace is trimmed and normalised
     * into single spaces. This method does not return null.
     */
    val textTrim: String?

    /**
     * Returns the XPath string-value of this node. The behaviour of this method
     * is defined in the [XPath
     * specification ](http://www.w3.org/TR/xpath). This method returns the string-value of all the
     * contained [Text],[CDATA],[Entity]and [ ] nodes all appended together.
     * 
     * @return the text from all the child Text and Element nodes appended
     * together.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String?

    /**
     * Accesses the data of this element which may implement data typing
     * bindings such as XML Schema or Java Bean bindings or will return the same
     * value as [.getText]
     * 
     * @return DOCUMENT ME!
     */
    /**
     * Sets the data value of this element if this element supports data binding
     * or calls [.setText]if it doesn't
     * 
     * @param data
     * DOCUMENT ME!
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDataProperty")
    @set:JvmName("setDataProperty")
    var data: Any?
    fun getData(): Any? = data
    fun setData(data: Any?) { this.data = data }

    // Attribute methods
    // -------------------------------------------------------------------------
    /**
     * 
     * 
     * Returns the [Attribute]instances this element contains as a backed
     * [List]so that the attributes may be modified directly using the
     * [List]interface. The `List` is backed by the
     * `Element` so that changes to the list are reflected in the
     * element and vice versa.
     * 
     * 
     * @return the attributes that this element contains as a `List`
     */
    fun attributes(): MutableList<*>?

    /**
     * Sets the attributes that this element contains
     * 
     * @param attributes
     * DOCUMENT ME!
     */
    fun setAttributes(attributes: MutableList<*>?)

    /**
     * DOCUMENT ME!
     * 
     * @return the number of attributes this element contains
     */
    fun attributeCount(): Int

    /**
     * DOCUMENT ME!
     * 
     * @return an iterator over the attributes of this element
     */
    fun attributeIterator(): MutableIterator<*>?

    /**
     * Returns the attribute at the specified indexGets the
     * 
     * @param index
     * DOCUMENT ME!
     * 
     * @return the attribute at the specified index where index &gt;= 0 and
     * index &lt; number of attributes or throws an
     * IndexOutOfBoundsException if the index is not within the
     * allowable range
     */
    fun attribute(index: Int): Attribute?

    /**
     * Returns the attribute with the given name
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return the attribute for the given local name in any namespace. If there
     * are more than one attributes with the given local name in
     * different namespaces then the first one is returned.
     */
    fun attribute(name: String?): Attribute?

    /**
     * DOCUMENT ME!
     * 
     * @param qName
     * is the fully qualified name
     * 
     * @return the attribute for the given fully qualified name or null if it
     * could not be found.
     */
    fun attribute(qName: QName?): Attribute?

    /**
     * 
     * 
     * This returns the attribute value for the attribute with the given name
     * and any namespace or null if there is no such attribute or the empty
     * string if the attribute value is empty.
     * 
     * 
     * @param name
     * is the name of the attribute value to be returnd
     * 
     * @return the value of the attribute, null if the attribute does not exist
     * or the empty string
     */
    fun attributeValue(name: String?): String?

    /**
     * 
     * 
     * This returns the attribute value for the attribute with the given name
     * and any namespace or the default value if there is no such attribute
     * value.
     * 
     * 
     * @param name
     * is the name of the attribute value to be returnd
     * @param defaultValue
     * is the default value to be returned if the attribute has no
     * value defined.
     * 
     * @return the value of the attribute or the defaultValue if the attribute
     * has no value defined.
     */
    fun attributeValue(name: String?, defaultValue: String?): String?

    /**
     * 
     * 
     * This returns the attribute value for the attribute with the given fully
     * qualified name or null if there is no such attribute or the empty string
     * if the attribute value is empty.
     * 
     * 
     * @param qName
     * is the fully qualified name
     * 
     * @return the value of the attribute, null if the attribute does not exist
     * or the empty string
     */
    fun attributeValue(qName: QName?): String?

    /**
     * 
     * 
     * This returns the attribute value for the attribute with the given fully
     * qualified name or the default value if there is no such attribute value.
     * 
     * 
     * @param qName
     * is the fully qualified name
     * @param defaultValue
     * is the default value to be returned if the attribute has no
     * value defined.
     * 
     * @return the value of the attribute or the defaultValue if the attribute
     * has no value defined.
     */
    fun attributeValue(qName: QName?, defaultValue: String?): String?

    /**
     * 
     * 
     * Sets the attribute value of the given local name.
     * 
     * 
     * @param name
     * is the name of the attribute whose value is to be added or
     * updated
     * @param value
     * is the attribute's value
     * 
     */
    @Deprecated(
        """As of version 0.5. Please use {@link
     *             #addAttribute(String,String)} instead. WILL BE REMOVED IN
                  dom4j-1.6 !!"""
    )
    fun setAttributeValue(name: String?, value: String?)

    /**
     * 
     * 
     * Sets the attribute value of the given fully qualified name.
     * 
     * 
     * @param qName
     * is the fully qualified name of the attribute whose value is to
     * be added or updated
     * @param value
     * is the attribute's value
     * 
     */
    @Deprecated(
        """As of version 0.5. Please use {@link
     *             #addAttribute(QName,String)} instead. WILL BE REMOVED IN
                  dom4j-1.6 !!"""
    )
    fun setAttributeValue(qName: QName?, value: String?)

    // Content methods
    // -------------------------------------------------------------------------
    /**
     * Returns the first element for the given local name and any namespace.
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return the first element with the given local name
     */
    fun element(name: String?): Element?

    /**
     * Returns the first element for the given fully qualified name.
     * 
     * @param qName
     * is the fully qualified name to search for
     * 
     * @return the first element with the given fully qualified name
     */
    fun element(qName: QName?): Element?

    /**
     * 
     * 
     * Returns the elements contained in this element. If this element does not
     * contain any elements then this method returns an empty list. The list is
     * backed by the element such that changes to the list will be reflected in
     * the element though the reverse is not the case.
     * 
     * 
     * @return a list of all the elements in this element.
     */
    fun elements(): MutableList<*>?

    /**
     * 
     * 
     * Returns the elements contained in this element with the given local name
     * and any namespace. If no elements are found then this method returns an
     * empty list. The list is backed by the element such that changes to the
     * list will be reflected in the element though the reverse is not the case.
     * 
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return a list of all the elements in this element for the given local
     * name
     */
    fun elements(name: String?): MutableList<*>?

    /**
     * 
     * 
     * Returns the elements contained in this element with the given fully
     * qualified name. If no elements are found then this method returns an
     * empty list. The list is backed by the element such that changes to the
     * list will be reflected in the element though the reverse is not the case.
     * 
     * 
     * @param qName
     * is the fully qualified name to search for
     * 
     * @return a list of all the elements in this element for the given fully
     * qualified name.
     */
    fun elements(qName: QName?): MutableList<*>?

    /**
     * Returns an iterator over all this elements child elements.
     * 
     * @return an iterator over the contained elements
     */
    fun elementIterator(): MutableIterator<*>?

    /**
     * Returns an iterator over the elements contained in this element which
     * match the given local name and any namespace.
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return an iterator over the contained elements matching the given local
     * name
     */
    fun elementIterator(name: String?): MutableIterator<*>?

    /**
     * Returns an iterator over the elements contained in this element which
     * match the given fully qualified name.
     * 
     * @param qName
     * is the fully qualified name to search for
     * 
     * @return an iterator over the contained elements matching the given fully
     * qualified name
     */
    fun elementIterator(qName: QName?): MutableIterator<*>?

    // Helper methods
    // -------------------------------------------------------------------------
    /**
     * DOCUMENT ME!
     * 
     * @return true if this element is the root element of a document and this
     * element supports the parent relationship else false.
     */
    val isRootElement: Boolean

    /**
     * 
     * 
     * Returns true if this `Element` has mixed content. Mixed
     * content means that an element contains both textual data and child
     * elements.
     * 
     * 
     * @return true if this element contains mixed content.
     */
    fun hasMixedContent(): Boolean

    /**
     * 
     * 
     * Returns true if this `Element` has text only content.
     * 
     * 
     * @return true if this element is empty or only contains text content.
     */
    val isTextOnly: Boolean

    /**
     * Appends the attributes of the given element to me. This method behaves
     * like the [java.util.Collection.addAll]
     * method.
     * 
     * @param element
     * is the element whose attributes will be added to me.
     */
    fun appendAttributes(element: Element?)

    /**
     * 
     * 
     * Creates a deep copy of this element The new element is detached from its
     * parent, and getParent() on the clone will return null.
     * 
     * 
     * @return a new deep copy Element
     */
    fun createCopy(): Element?

    /**
     * 
     * 
     * Creates a deep copy of this element with the given local name The new
     * element is detached from its parent, and getParent() on the clone will
     * return null.
     * 
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return a new deep copy Element
     */
    fun createCopy(name: String?): Element?

    /**
     * 
     * 
     * Creates a deep copy of this element with the given fully qualified name.
     * The new element is detached from its parent, and getParent() on the clone
     * will return null.
     * 
     * 
     * @param qName
     * DOCUMENT ME!
     * 
     * @return a new deep copy Element
     */
    fun createCopy(qName: QName?): Element?

    fun elementText(name: String?): String?

    fun elementText(qname: QName?): String?

    fun elementTextTrim(name: String?): String?

    fun elementTextTrim(qname: QName?): String?

    /**
     * Returns a node at the given index suitable for an XPath result set. This
     * means the resulting Node will either be null or it will support the
     * parent relationship.
     * 
     * @param index
     * DOCUMENT ME!
     * 
     * @return the Node for the given index which will support the parent
     * relationship or null if there is not a node at the given index.
     */
    fun getXPathResult(index: Int): Node?
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


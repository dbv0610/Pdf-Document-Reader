/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j

import com.wxiwei.office.fc.dom4j.rule.Pattern
import com.wxiwei.office.fc.dom4j.tree.AbstractDocument
import com.wxiwei.office.fc.dom4j.tree.DefaultAttribute
import com.wxiwei.office.fc.dom4j.tree.DefaultCDATA
import com.wxiwei.office.fc.dom4j.tree.DefaultComment
import com.wxiwei.office.fc.dom4j.tree.DefaultDocument
import com.wxiwei.office.fc.dom4j.tree.DefaultDocumentType
import com.wxiwei.office.fc.dom4j.tree.DefaultElement
import com.wxiwei.office.fc.dom4j.tree.DefaultEntity
import com.wxiwei.office.fc.dom4j.tree.DefaultProcessingInstruction
import com.wxiwei.office.fc.dom4j.tree.DefaultText
import com.wxiwei.office.fc.dom4j.tree.QNameCache
import com.wxiwei.office.fc.dom4j.util.SimpleSingleton
import com.wxiwei.office.fc.dom4j.util.SingletonStrategy
import com.wxiwei.office.fc.dom4j.xpath.DefaultXPath
import com.wxiwei.office.fc.dom4j.xpath.XPathPattern
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable

/**
 * 
 * 
 * `DocumentFactory` is a collection of factory methods to allow
 * easy custom building of DOM4J trees. The default tree that is built uses a
 * doubly linked tree.
 * 
 * 
 * 
 * 
 * The tree built allows full XPath expressions from anywhere on the tree.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 */
open class DocumentFactory : Serializable {
    @Transient
    protected var cache: QNameCache? = null

    /**
     * DOCUMENT ME!
     * 
     * @return the Map of namespace URIs that will be used by by XPath
     * expressions to resolve namespace prefixes into namespace URIs.
     * The map is keyed by namespace prefix and the value is the
     * namespace URI. This value could well be null to indicate no
     * namespace URIs are being mapped.
     */
    /**
     * Sets the namespace URIs to be used by XPath expressions created by this
     * factory or by nodes associated with this factory. The keys are namespace
     * prefixes and the values are namespace URIs.
     * 
     * @param namespaceURIs
     * DOCUMENT ME!
     */
    /** Default namespace prefix -> URI mappings for XPath expressions to use  */
    var xPathNamespaceURIs: MutableMap<*, *>? = null

    init {
        init()
    }

    // Factory methods
    open fun createDocument(): Document {
        val answer = DefaultDocument()
        answer.setDocumentFactory(this)

        return answer
    }

    /**
     * DOCUMENT ME!
     * 
     * @param encoding
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @since 1.5
     */
    fun createDocument(encoding: String?): Document {
        // to keep the DocumentFactory backwards compatible, we have to do this
        // in this not so nice way, since subclasses only need to extend the
        // createDocument() method.
        val answer = createDocument()

        if (answer is AbstractDocument) {
            answer.xMLEncoding = encoding
        }

        return answer
    }

    fun createDocument(rootElement: Element?): Document {
        val answer = createDocument()
        answer.rootElement = rootElement

        return answer
    }

    open fun createDocType(name: String?, publicId: String?, systemId: String?): DocumentType? {
        return DefaultDocumentType(name, publicId, systemId)
    }

    open fun createElement(qname: QName?): Element? {
        return DefaultElement(qname!!)
    }

    fun createElement(name: String?): Element? {
        return createElement(createQName(name))
    }

    fun createElement(qualifiedName: String, namespaceURI: String?): Element? {
        return createElement(createQName(qualifiedName, namespaceURI))
    }

    open fun createAttribute(owner: Element?, qname: QName?, value: String?): Attribute? {
        return DefaultAttribute(qname, value)
    }

    fun createAttribute(owner: Element?, name: String?, value: String?): Attribute? {
        return createAttribute(owner, createQName(name), value)
    }

    open fun createCDATA(text: String?): CDATA? {
        return DefaultCDATA(text)
    }

    open fun createComment(text: String?): Comment? {
        return DefaultComment(text)
    }

    open fun createText(text: String?): Text? {
        if (text == null) {
            val msg = "Adding text to an XML document must not be null"
            throw IllegalArgumentException(msg)
        }

        return DefaultText(text)
    }

    open fun createEntity(name: String?, text: String?): Entity? {
        return DefaultEntity(name, text)
    }

    open fun createNamespace(prefix: String?, uri: String?): Namespace? {
        return Namespace.Companion.get(prefix, uri)
    }

    open fun createProcessingInstruction(target: String?, data: String?): ProcessingInstruction? {
        return DefaultProcessingInstruction(target, data)
    }

    open fun createProcessingInstruction(
        target: String?,
        data: MutableMap<*, *>?
    ): ProcessingInstruction? {
        return DefaultProcessingInstruction(target, data)
    }

    fun createQName(localName: String?, namespace: Namespace?): QName {
        return cache!!.get(localName, namespace)
    }

    fun createQName(localName: String?): QName {
        return cache!!.get(localName)
    }

    fun createQName(name: String?, prefix: String?, uri: String?): QName {
        return cache!!.get(name, Namespace.get(prefix, uri))
    }

    fun createQName(qualifiedName: String?, uri: String?): QName {
        return cache!!.get(qualifiedName, uri)
    }

    /**
     * 
     * 
     * `createXPath` parses an XPath expression and creates a new
     * XPath `XPath` instance.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to create
     * 
     * @return a new `XPath` instance
     * 
     * @throws InvalidXPathException
     * if the XPath expression is invalid
     */
    @Throws(InvalidXPathException::class)
    fun createXPath(xpathExpression: String?): XPath {
        val xpath = DefaultXPath(xpathExpression)

        if (this.xPathNamespaceURIs != null) {
            xpath.setNamespaceURIs(this.xPathNamespaceURIs)
        }

        return xpath
    }

    /**
     * 
     * 
     * `createXPath` parses an XPath expression and creates a new
     * XPath `XPath` instance.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to create
     * @param variableContext
     * is the variable context to use when evaluating the XPath
     * 
     * @return a new `XPath` instance
     * /
     * public XPath createXPath(String xpathExpression, VariableContext variableContext)
     * {
     * XPath xpath = createXPath(xpathExpression);
     * xpath.setVariableContext(variableContext);
     * 
     * return xpath;
     * }
     * 
     * / **
     * 
     * 
     * `createXPathFilter` parses a NodeFilter from the given XPath
     * filter expression. XPath filter expressions occur within XPath
     * expressions such as `self::node()[ filterExpression ]`
     * 
     * 
     * @param xpathFilterExpression
     * is the XPath filter expression to create
     * @param variableContext
     * is the variable context to use when evaluating the XPath
     * 
     * @return a new `NodeFilter` instance
     * /
     * public NodeFilter createXPathFilter(String xpathFilterExpression,
     * VariableContext variableContext)
     * {
     * XPath answer = createXPath(xpathFilterExpression);
     * 
     * // DefaultXPath answer = new DefaultXPath( xpathFilterExpression );
     * answer.setVariableContext(variableContext);
     * 
     * return answer;
     * }
     * 
     * / **
     * 
     * 
     * `createXPathFilter` parses a NodeFilter from the given XPath
     * filter expression. XPath filter expressions occur within XPath
     * expressions such as `self::node()[ filterExpression ]`
     * 
     * 
     * @param xpathFilterExpression
     * is the XPath filter expression to create
     * 
     * @return a new `NodeFilter` instance
     */
    fun createXPathFilter(xpathFilterExpression: String?): NodeFilter {
        return createXPath(xpathFilterExpression)

        // return new DefaultXPath( xpathFilterExpression );
    }

    /**
     * 
     * 
     * `createPattern` parses the given XPath expression to create
     * an XSLT style [Pattern]instance which can then be used in an XSLT
     * processing model.
     * 
     * 
     * @param xpathPattern
     * is the XPath pattern expression to create
     * 
     * @return a new `Pattern` instance
     */
    fun createPattern(xpathPattern: String?): Pattern {
        return XPathPattern(xpathPattern)
    }

    // Properties
    // -------------------------------------------------------------------------
    val qNames: MutableList<*>?
        /**
         * Returns a list of all the QName instances currently used by this document
         * factory
         * 
         * @return DOCUMENT ME!
         */
        get() = cache!!.qNames

    /**
     * DOCUMENT ME!
     * 
     * @param qname
     * DOCUMENT ME!
     * 
     * @return the cached QName instance if there is one or adds the given qname
     * to the cache if not
     */
    protected fun intern(qname: QName): QName? {
        return cache!!.intern(qname)
    }

    /**
     * Factory method to create the QNameCache. This method should be overloaded
     * if you wish to use your own derivation of QName.
     * 
     * @return DOCUMENT ME!
     */
    protected fun createQNameCache(): QNameCache {
        return QNameCache(this)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        `in`.defaultReadObject()
        init()
    }

    protected fun init() {
        cache = createQNameCache()
    }

    companion object {
        private var singleton: SingletonStrategy? = null

        private fun createSingleton(): SingletonStrategy {
            var result: SingletonStrategy

            var documentFactoryClassName: String?
            try {
                documentFactoryClassName = System.getProperty(
                    "com.wxiwei.office.fc.dom4j.factory",
                    "com.wxiwei.office.fc.dom4j.DocumentFactory"
                )
            } catch (e: Exception) {
                documentFactoryClassName = "com.wxiwei.office.fc.dom4j.DocumentFactory"
            }

            try {
                val singletonClass = System.getProperty(
                    "com.wxiwei.office.fc.dom4j.DocumentFactory.singleton.strategy",
                    "com.wxiwei.office.fc.dom4j.util.SimpleSingleton"
                )
                val clazz = Class.forName(singletonClass)
                result = clazz.newInstance() as SingletonStrategy
            } catch (e: Exception) {
                result = SimpleSingleton()
            }

            result.setSingletonClassName(documentFactoryClassName)

            return result
        }

        @JvmStatic
        @Synchronized
        fun getInstance(): DocumentFactory
            /**
             * 
             * 
             * Access to singleton implementation of DocumentFactory which is used if no
             * DocumentFactory is specified when building using the standard builders.
             * 
             * 
             * @return the default singleon instance
             */
        {
                if (singleton == null) {
                    singleton =
                        createSingleton()
                }
                return singleton!!.instance() as DocumentFactory
            }

        // Implementation methods
        // -------------------------------------------------------------------------
        /**
         * 
         * 
         * `createSingleton` creates the singleton instance from the
         * given class name.
         * 
         * 
         * @param className
         * is the name of the DocumentFactory class to use
         * 
         * @return a new singleton instance.
         */
        protected fun createSingleton(className: String): DocumentFactory {
            // let's try and class load an implementation?
            try {
                // I'll use the current class loader
                // that loaded me to avoid problems in J2EE and web apps
                val theClass =
                    Class.forName(className, true, DocumentFactory::class.java.getClassLoader())

                return theClass.newInstance() as DocumentFactory
            } catch (e: Throwable) {
                println("WARNING: Cannot load DocumentFactory: " + className)

                return DocumentFactory()
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


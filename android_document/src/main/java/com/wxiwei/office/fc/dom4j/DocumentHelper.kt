/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j

import com.wxiwei.office.fc.dom4j.io.SAXReader
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.StringTokenizer

/**
 * 
 * 
 * `DocumentHelper` is a collection of helper methods for using
 * DOM4J.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.26 $
 */
object DocumentHelper {
    private val documentFactory: DocumentFactory?
        get() = DocumentFactory.getInstance()

    // Static helper methods
    @JvmStatic
    fun createDocument(): Document? {
        return documentFactory!!.createDocument()
    }

    fun createDocument(rootElement: Element?): Document? {
        return documentFactory!!.createDocument(rootElement)
    }

    fun createElement(qname: QName?): Element? {
        return documentFactory!!.createElement(qname)
    }

    fun createElement(name: String?): Element? {
        return documentFactory!!.createElement(name)
    }

    fun createAttribute(owner: Element?, qname: QName?, value: String?): Attribute? {
        return documentFactory!!.createAttribute(owner, qname, value)
    }

    fun createAttribute(owner: Element?, name: String?, value: String?): Attribute? {
        return documentFactory!!.createAttribute(owner, name, value)
    }

    fun createCDATA(text: String?): CDATA? {
        return DocumentFactory.getInstance().createCDATA(text)
    }

    fun createComment(text: String?): Comment? {
        return DocumentFactory.getInstance().createComment(text)
    }

    fun createText(text: String?): Text? {
        return DocumentFactory.getInstance().createText(text)
    }

    fun createEntity(name: String?, text: String?): Entity? {
        return DocumentFactory.getInstance().createEntity(name, text)
    }

    fun createNamespace(prefix: String?, uri: String?): Namespace? {
        return DocumentFactory.getInstance().createNamespace(prefix, uri)
    }

    fun createProcessingInstruction(pi: String?, d: String?): ProcessingInstruction? {
        return documentFactory!!.createProcessingInstruction(pi, d)
    }

    fun createProcessingInstruction(pi: String?, data: MutableMap<*, *>?): ProcessingInstruction? {
        return documentFactory!!.createProcessingInstruction(pi, data)
    }

    fun createQName(localName: String?, namespace: Namespace?): QName? {
        return documentFactory!!.createQName(localName, namespace)
    }

    fun createQName(localName: String?): QName? {
        return documentFactory!!.createQName(localName)
    }

    /**
     * 
     * 
     * `createXPath` parses an XPath expression and creates a new
     * XPath `XPath` instance using the singleton [ ].
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
        return documentFactory!!.createXPath(xpathExpression)
    }

    /**
     * 
     * 
     * `createXPath` parses an XPath expression and creates a new
     * XPath `XPath` instance using the singleton [ ].
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to create
     * @param context
     * is the variable context to use when evaluating the XPath
     * 
     * @return a new `XPath` instance
     * 
     * @throws InvalidXPathException
     * if the XPath expression is invalid
     * /
     * public static XPath createXPath(String xpathExpression, VariableContext context)
     * throws InvalidXPathException
     * {
     * return getDocumentFactory().createXPath(xpathExpression, context);
     * }
     * 
     * / **
     * 
     * 
     * `createXPathFilter` parses a NodeFilter from the given XPath
     * filter expression using the singleton [DocumentFactory]. XPath
     * filter expressions occur within XPath expressions such as
     * `self::node()[ filterExpression ]`
     * 
     * 
     * @param xpathFilterExpression
     * is the XPath filter expression to create
     * 
     * @return a new `NodeFilter` instance
     */
    fun createXPathFilter(xpathFilterExpression: String?): NodeFilter? {
        return documentFactory!!.createXPathFilter(xpathFilterExpression)
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
     * /
     * public static Pattern createPattern(String xpathPattern)
     * {
     * return getDocumentFactory().createPattern(xpathPattern);
     * }
     * 
     * / **
     * 
     * 
     * `selectNodes` performs the given XPath expression on the
     * [List]of [Node]instances appending all the results together
     * into a single list.
     * 
     * 
     * @param xpathFilterExpression
     * is the XPath filter expression to evaluate
     * @param nodes
     * is the list of nodes on which to evalute the XPath
     * 
     * @return the results of all the XPath evaluations as a single list
     */
    fun selectNodes(xpathFilterExpression: String?, nodes: MutableList<*>?): MutableList<*>? {
        val xpath = createXPath(xpathFilterExpression)

        return xpath.selectNodes(nodes)
    }

    /**
     * 
     * 
     * `selectNodes` performs the given XPath expression on the
     * [List]of [Node]instances appending all the results together
     * into a single list.
     * 
     * 
     * @param xpathFilterExpression
     * is the XPath filter expression to evaluate
     * @param node
     * is the Node on which to evalute the XPath
     * 
     * @return the results of all the XPath evaluations as a single list
     */
    fun selectNodes(xpathFilterExpression: String?, node: Node?): MutableList<*>? {
        val xpath = createXPath(xpathFilterExpression)

        return xpath.selectNodes(node)
    }

    /**
     * 
     * 
     * `sort` sorts the given List of Nodes using an XPath
     * expression as a [java.util.Comparator].
     * 
     * 
     * @param list
     * is the list of Nodes to sort
     * @param xpathExpression
     * is the XPath expression used for comparison
     */
    fun sort(list: MutableList<*>?, xpathExpression: String?) {
        val xpath = createXPath(xpathExpression)
        xpath.sort(list)
    }

    /**
     * 
     * 
     * `sort` sorts the given List of Nodes using an XPath
     * expression as a [java.util.Comparator]and optionally removing
     * duplicates.
     * 
     * 
     * @param list
     * is the list of Nodes to sort
     * @param expression
     * is the XPath expression used for comparison
     * @param distinct
     * if true then duplicate values (using the sortXPath for
     * comparisions) will be removed from the List
     */
    fun sort(list: MutableList<*>?, expression: String?, distinct: Boolean) {
        val xpath = createXPath(expression)
        xpath.sort(list, distinct)
    }

    /**
     * 
     * 
     * `parseText` parses the given text as an XML document and
     * returns the newly created Document.
     * 
     * 
     * @param text
     * the XML text to be parsed
     * 
     * @return a newly parsed Document
     * 
     * @throws DocumentException
     * if the document could not be parsed
     */
    @Throws(DocumentException::class)
    fun parseText(text: String): Document {
        val result: Document

        val reader = SAXReader()
        val encoding = getEncoding(text)

        val source = InputSource(StringReader(text))
        source.setEncoding(encoding)

        result = reader.read(source)!!

        // if the XML parser doesn't provide a way to retrieve the encoding,
        // specify it manually
        if (result.xMLEncoding == null) {
            result.xMLEncoding = encoding
        }

        return result
    }

    private fun getEncoding(text: String): String? {
        var result: String? = null

        val xml = text.trim { it <= ' ' }

        if (xml.startsWith("<?xml")) {
            val end = xml.indexOf("?>")
            val sub = xml.substring(0, end)
            val tokens = StringTokenizer(sub, " =\"\'")

            while (tokens.hasMoreTokens()) {
                val token = tokens.nextToken()

                if ("encoding" == token) {
                    if (tokens.hasMoreTokens()) {
                        result = tokens.nextToken()
                    }

                    break
                }
            }
        }

        return result
    }

    /**
     * 
     * 
     * makeElement
     * 
     * a helper method which navigates from the given Document or Element node
     * to some Element using the path expression, creating any necessary
     * elements along the way. For example the path `a/b/c` would
     * get the first child &lt;a&gt; element, which would be created if it did
     * not exist, then the next child &lt;b&gt; and so on until finally a
     * &lt;c&gt; element is returned.
     * 
     * @param source
     * is the Element or Document to start navigating from
     * @param path
     * is a simple path expression, seperated by '/' which denotes
     * the path from the source to the resulting element such as
     * a/b/c
     * 
     * @return the first Element on the given path which either already existed
     * on the path or were created by this method.
     */
    fun makeElement(source: Branch?, path: String?): Element? {
        val tokens = StringTokenizer(path, "/")
        var parent: Element?

        if (source is Document) {
            val document = source
            parent = document.rootElement

            // lets throw a NoSuchElementException
            // if we are given an empty path
            val name = tokens.nextToken()

            if (parent == null) {
                parent = document.addElement(name)
            }
        } else {
            parent = source as Element?
        }

        var element: Element? = null

        while (tokens.hasMoreTokens()) {
            val name = tokens.nextToken()

            if (name.indexOf(':') > 0) {
                element = parent!!.element(parent.getQName(name))
            } else {
                element = parent!!.element(name)
            }

            if (element == null) {
                element = parent.addElement(name)
            }

            parent = element
        }

        return element
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


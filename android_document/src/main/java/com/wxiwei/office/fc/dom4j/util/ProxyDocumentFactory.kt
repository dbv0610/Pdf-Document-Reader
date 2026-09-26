/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.util

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.CDATA
import com.wxiwei.office.fc.dom4j.Comment
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.DocumentType
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Entity
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.NodeFilter
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.Text
import com.wxiwei.office.fc.dom4j.XPath
import com.wxiwei.office.fc.dom4j.rule.Pattern

/**
 * 
 * 
 * `ProxyDocumentFactory` implements a proxy to a DocumentFactory
 * which is useful for implementation inheritence, allowing the pipelining of
 * various factory implementations. For example an EncodingDocumentFactory which
 * takes care of encoding strings outside of allowable XML ranges could be used
 * with a DatatypeDocumentFactory which is XML Schema Data Type aware.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.13 $
 */
abstract class ProxyDocumentFactory {
    private var proxy: DocumentFactory?

    constructor() {
        // use default factory
        this.proxy = DocumentFactory.Companion.getInstance()
    }

    constructor(proxy: DocumentFactory) {
        this.proxy = proxy
    }

    // Factory methods
    // -------------------------------------------------------------------------
    fun createDocument(): Document? {
        return proxy!!.createDocument()
    }

    fun createDocument(rootElement: Element?): Document? {
        return proxy!!.createDocument(rootElement)
    }

    fun createDocType(name: String?, publicId: String?, systemId: String?): DocumentType? {
        return proxy!!.createDocType(name, publicId, systemId)
    }

    fun createElement(qname: QName?): Element? {
        return proxy!!.createElement(qname)
    }

    fun createElement(name: String?): Element? {
        return proxy!!.createElement(name)
    }

    fun createAttribute(owner: Element?, qname: QName?, value: String?): Attribute? {
        return proxy!!.createAttribute(owner, qname, value)
    }

    fun createAttribute(owner: Element?, name: String?, value: String?): Attribute? {
        return proxy!!.createAttribute(owner, name, value)
    }

    fun createCDATA(text: String?): CDATA? {
        return proxy!!.createCDATA(text)
    }

    fun createComment(text: String?): Comment? {
        return proxy!!.createComment(text)
    }

    fun createText(text: String?): Text? {
        return proxy!!.createText(text)
    }

    fun createEntity(name: String?, text: String?): Entity? {
        return proxy!!.createEntity(name, text)
    }

    fun createNamespace(prefix: String?, uri: String?): Namespace? {
        return proxy!!.createNamespace(prefix, uri)
    }

    fun createProcessingInstruction(target: String?, data: String?): ProcessingInstruction? {
        return proxy!!.createProcessingInstruction(target, data)
    }

    fun createProcessingInstruction(
        target: String?,
        data: MutableMap<*, *>?
    ): ProcessingInstruction? {
        return proxy!!.createProcessingInstruction(target, data)
    }

    fun createQName(localName: String?, namespace: Namespace?): QName? {
        return proxy!!.createQName(localName, namespace)
    }

    fun createQName(localName: String?): QName? {
        return proxy!!.createQName(localName)
    }

    fun createQName(name: String?, prefix: String?, uri: String?): QName? {
        return proxy!!.createQName(name, prefix, uri)
    }

    fun createQName(qualifiedName: String?, uri: String?): QName? {
        return proxy!!.createQName(qualifiedName, uri)
    }

    fun createXPath(xpathExpression: String?): XPath? {
        return proxy!!.createXPath(xpathExpression!!)
    }

    /*public XPath createXPath(String xpathExpression, VariableContext variableContext)
    {
        return proxy.createXPath(xpathExpression, variableContext);
    }

    public NodeFilter createXPathFilter(String xpathFilterExpression,
        VariableContext variableContext)
    {
        return proxy.createXPathFilter(xpathFilterExpression, variableContext);
    }*/
    fun createXPathFilter(xpathFilterExpression: String?): NodeFilter? {
        return proxy!!.createXPathFilter(xpathFilterExpression)
    }

    fun createPattern(xpathPattern: String?): Pattern? {
        return proxy!!.createPattern(xpathPattern)
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected fun getProxy(): DocumentFactory {
        return proxy!!
    }

    protected fun setProxy(proxy: DocumentFactory?) {
        var proxy = proxy
        if (proxy == null) {
            // use default factory
            proxy = DocumentFactory.Companion.getInstance()
        }

        this.proxy = proxy
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


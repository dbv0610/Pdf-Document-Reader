/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.dom

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.CDATA
import com.wxiwei.office.fc.dom4j.Comment
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.DocumentType
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Entity
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.Text
import com.wxiwei.office.fc.dom4j.util.SingletonStrategy
import org.w3c.dom.DOMException
import org.w3c.dom.DOMImplementation

/**
 * 
 * 
 * `DOMDocumentFactory` is a factory of DOM4J objects which
 * implement the W3C DOM API.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.21 $
 */
class DOMDocumentFactory : DocumentFactory(), DOMImplementation {
    // Factory methods
    override fun createDocument(): Document {
        val answer = DOMDocument()
        answer.setDocumentFactory(this)

        return answer
    }

    override fun createDocType(name: String?, publicId: String?, systemId: String?): DocumentType {
        return DOMDocumentType(name, publicId, systemId)
    }

    override fun createElement(qname: QName?): Element {
        return DOMElement(qname)
    }

    fun createElement(qname: QName?, attributeCount: Int): Element {
        return DOMElement(qname, attributeCount)
    }

    override fun createAttribute(owner: Element?, qname: QName?, value: String?): Attribute {
        return DOMAttribute(qname, value)
    }

    override fun createCDATA(text: String?): CDATA {
        return DOMCDATA(text)
    }

    override fun createComment(text: String?): Comment {
        return DOMComment(text)
    }

    override fun createText(text: String?): Text {
        return DOMText(text)
    }

    fun createEntity(name: String?): Entity {
        return DOMEntityReference(name)
    }

    override fun createEntity(name: String?, text: String?): Entity {
        return DOMEntityReference(name, text)
    }

    override fun createNamespace(prefix: String?, uri: String?): Namespace {
        return DOMNamespace(prefix, uri)
    }

    override fun createProcessingInstruction(
        target: String?,
        data: String?
    ): ProcessingInstruction {
        return DOMProcessingInstruction(target, data)
    }

    override fun createProcessingInstruction(
        target: String?,
        data: MutableMap<*, *>?
    ): ProcessingInstruction {
        return DOMProcessingInstruction(target, data)
    }

    // org.w3c.dom.DOMImplementation interface
    override fun hasFeature(feat: String?, version: String?): Boolean {
        if ("XML".equals(feat, ignoreCase = true) || "Core".equals(feat, ignoreCase = true)) {
            return ((version == null) || (version.length == 0) || "1.0" == version || ("2.0"
                    == version))
        }

        return false
    }

    @Throws(DOMException::class)
    override fun createDocumentType(
        qualifiedName: String?, publicId: String?,
        systemId: String?
    ): org.w3c.dom.DocumentType {
        return DOMDocumentType(qualifiedName, publicId, systemId)
    }

    @Throws(DOMException::class)
    override fun createDocument(
        namespaceURI: String?, qualifiedName: String?,
        docType: org.w3c.dom.DocumentType?
    ): org.w3c.dom.Document {
        val document: DOMDocument

        if (docType != null) {
            val documentType = asDocumentType(docType)
            document = DOMDocument(documentType)
        } else {
            document = DOMDocument()
        }

        document.addElement(createQName(qualifiedName, namespaceURI ?: ""))

        return document
    }

    // Implementation methods
    protected fun asDocumentType(docType: org.w3c.dom.DocumentType): DOMDocumentType {
        if (docType is DOMDocumentType) {
            return docType
        } else {
            return DOMDocumentType(
                docType.getName(), docType.getPublicId(),
                docType.getSystemId()
            )
        }
    }

    override fun getFeature(feature: String?, version: String?): Any? {
     
        return null
    }

    companion object {
        /** The Singleton instance  */
        private var singleton: SingletonStrategy? = null

        init {
            try {
                val defaultSingletonClass = "org.dom4j.util.SimpleSingleton"
                var clazz: Class<*>? = null
                try {
                    var singletonClass: String? = defaultSingletonClass
                    singletonClass = System.getProperty(
                        "org.dom4j.dom.DOMDocumentFactory.singleton.strategy", singletonClass
                    )
                    clazz = Class.forName(singletonClass)
                } catch (exc1: Exception) {
                    try {
                        val singletonClass = defaultSingletonClass
                        clazz = Class.forName(singletonClass)
                    } catch (exc2: Exception) {
                    }
                }
                singleton = clazz!!.newInstance() as SingletonStrategy
                singleton!!.setSingletonClassName(DOMDocumentFactory::class.java.getName())
            } catch (exc3: Exception) {
            }
        }

        @get:JvmName("getInstanceProperty")
        val instance: DocumentFactory?
            get() {
                val fact =
                    singleton!!.instance() as DOMDocumentFactory?
                return fact
            }

        @JvmStatic
        fun getInstance(): DocumentFactory = instance!!
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


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

import com.wxiwei.office.fc.dom4j.tree.QNameCache
import com.wxiwei.office.fc.dom4j.util.SingletonStrategy
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * 
 * 
 * `QName` represents a qualified name value of an XML element or
 * attribute. It consists of a local name and a [Namespace]instance. This
 * object is immutable.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 */
class QName : Serializable {
    /**
     * DOCUMENT ME!
     * 
     * @return the local name
     */
    /** The local name of the element or attribute  */
    @get:JvmName("getNameProperty")
    val name: String?
    fun getName(): String? = name

    /** The qualified name of the element or attribute  */
    private var qualifiedNameCache: String? = null

    /**
     * DOCUMENT ME!
     * 
     * @return the namespace of this QName
     */
    /** The Namespace of this element or attribute  */
    @Transient
    @get:JvmName("getNamespaceProperty")
    var namespace: Namespace?
        private set
    fun getNamespace(): Namespace? = namespace

    /** A cached version of the hashcode for efficiency  */
    private var hashCode = 0

    /**
     * DOCUMENT ME!
     * 
     * @return the factory that should be used for Elements of this QName
     */
    /** The document factory used for this QName if specified or null  */
    @get:JvmName("getDocumentFactoryProperty")
    @set:JvmName("setDocumentFactoryProperty")
    var documentFactory: DocumentFactory? = null
    fun getDocumentFactory(): DocumentFactory? = documentFactory
    fun setDocumentFactory(factory: DocumentFactory?) { this.documentFactory = factory }

    @JvmOverloads
    constructor(name: String?, namespace: Namespace? = Namespace.Companion.NO_NAMESPACE) {
        this.name = if (name == null) "" else name
        this.namespace = if (namespace == null) Namespace.Companion.NO_NAMESPACE else namespace
    }

    constructor(name: String?, namespace: Namespace?, qualifiedName: String?) {
        this.name = if (name == null) "" else name
        this.qualifiedNameCache = qualifiedName
        this.namespace = if (namespace == null) Namespace.Companion.NO_NAMESPACE else namespace
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the qualified name in the format `prefix:localName`
     */
    @get:JvmName("getQualifiedNameProperty")
    val qualifiedName: String?
        get() {
            if (qualifiedNameCache == null) {
                val prefix = this.namespacePrefix

                if ((prefix != null) && (prefix.length > 0)) {
                    qualifiedNameCache = prefix + ":" + name
                } else {
                    qualifiedNameCache = name
                }
            }

            return qualifiedNameCache
        }
    fun getQualifiedName(): String? = qualifiedName

    @get:JvmName("getNamespacePrefixProperty")
    val namespacePrefix: String?
        /**
         * DOCUMENT ME!
         * 
         * @return the namespace URI of this QName
         */
        get() {
            if (namespace == null) {
                return ""
            }

            return namespace!!.getPrefix()
        }
    fun getNamespacePrefix(): String? = namespacePrefix

    @get:JvmName("getNamespaceURIProperty")
    val namespaceURI: String?
        /**
         * DOCUMENT ME!
         * 
         * @return the namespace URI of this QName
         */
        get() {
            if (namespace == null) {
                return ""
            }

            return namespace!!.getURI()
        }
    fun getNamespaceURI(): String? = namespaceURI

    /**
     * DOCUMENT ME!
     * 
     * @return the hash code based on the qualified name and the URI of the
     * namespace.
     */
    override fun hashCode(): Int {
        if (hashCode == 0) {
            hashCode = this.name.hashCode() xor this.namespaceURI.hashCode()

            if (hashCode == 0) {
                hashCode = 0xbabe
            }
        }

        return hashCode
    }

    override fun equals(`object`: Any?): Boolean {
        if (this === `object`) {
            return true
        } else if (`object` is QName) {
            val that = `object`

            // we cache hash codes so this should be quick
            if (hashCode() == that.hashCode()) {
                return this.name == that.name
                        && this.namespaceURI == that.namespaceURI
            }
        }

        return false
    }

    override fun toString(): String {
        return (super.toString() + " [name: " + this.name + " namespace: \"" + this.namespace
                + "\"]")
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        // We use writeObject() and not writeUTF() to minimize space
        // This allows for writing pointers to already written strings
        out.writeObject(namespace!!.getPrefix())
        out.writeObject(namespace!!.getURI())

        out.defaultWriteObject()
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val prefix = `in`.readObject() as String?
        val uri = `in`.readObject() as String?

        `in`.defaultReadObject()

        namespace = Namespace.Companion.get(prefix, uri)
    }

    companion object {
        /** The Singleton instance  */
        private var singleton: SingletonStrategy? = null

        init {
            try {
                val defaultSingletonClass = "com.wxiwei.fc.dom4j.util.SimpleSingleton"
                var clazz: Class<*>? = null
                try {
                    var singletonClass: String? = defaultSingletonClass
                    singletonClass = System.getProperty(
                        "com.wxiwei.fc.dom4j.QName.singleton.strategy",
                        singletonClass
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
                singleton!!.setSingletonClassName(QNameCache::class.java.getName())
            } catch (exc3: Exception) {
            }
        }

        fun get(name: String?): QName {
            return cache.get(name)
        }

        fun get(name: String?, namespace: Namespace?): QName {
            return cache.get(name, namespace)
        }

        fun get(name: String?, prefix: String?, uri: String?): QName {
            if (((prefix == null) || (prefix.length == 0)) && (uri == null)) {
                return get(name)
            } else if ((prefix == null) || (prefix.length == 0)) {
                return cache.get(name, Namespace.get(uri))
            } else if (uri == null) {
                return get(name)
            } else {
                return cache.get(name, Namespace.get(prefix, uri))
            }
        }

        fun get(qualifiedName: String, uri: String?): QName {
            if (uri == null) {
                return cache.get(qualifiedName)
            } else {
                return cache.get(qualifiedName, uri)
            }
        }

        fun get(localName: String?, namespace: Namespace?, qualifiedName: String?): QName {
            return cache.get(localName, namespace, qualifiedName)
        }

        private val cache: QNameCache
            get() {
                val cache =
                    singleton!!.instance() as QNameCache
                return cache
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


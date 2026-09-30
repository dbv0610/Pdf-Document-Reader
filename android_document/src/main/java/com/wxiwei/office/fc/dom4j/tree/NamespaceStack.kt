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

import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName

/**
 * NamespaceStack implements a stack of namespaces and optionally maintains a
 * cache of all the fully qualified names (`QName`) which are in
 * scope. This is useful when building or navigating a *dom4j * document.
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.13 $
 */
class NamespaceStack {
    /** The factory used to create new `Namespace` instances  */
    var documentFactory: DocumentFactory

    /** The Stack of namespaces  */
    private val namespaceStack: ArrayList<Namespace?> = ArrayList()

    /** The cache of qualifiedNames to QNames per namespace context  */
    private val namespaceCacheList: ArrayList<MutableMap<*, *>?> = ArrayList()

    /**
     * A cache of current namespace context cache of mapping from qualifiedName
     * to QName
     */
    private var currentNamespaceCache: MutableMap<*, *>? = null

    /**
     * A cache of mapping from qualifiedName to QName before any namespaces are
     * declared
     */
    private val rootNamespaceCache: MutableMap<*, *> = HashMap<Any?, Any?>()

    /** Caches the default namespace defined via xmlns=""  */
    private var defaultNamespace: Namespace? = null

    constructor() {
        this.documentFactory = DocumentFactory.Companion.getInstance()
    }

    constructor(documentFactory: DocumentFactory) {
        this.documentFactory = documentFactory
    }

    /**
     * Pushes the given namespace onto the stack so that its prefix becomes
     * available.
     * 
     * @param namespace
     * is the `Namespace` to add to the stack.
     */
    fun push(namespace: Namespace) {
        namespaceStack.add(namespace)
        namespaceCacheList.add(null)
        currentNamespaceCache = null

        val prefix = namespace.getPrefix()

        if ((prefix == null) || (prefix.length == 0)) {
            defaultNamespace = namespace
        }
    }

    /**
     * Pops the most recently used `Namespace` from the stack
     * 
     * @return Namespace popped from the stack
     */
    fun pop(): Namespace? {
        return remove(namespaceStack.size - 1)
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the number of namespaces on the stackce stack.
     */
    fun size(): Int {
        return namespaceStack.size
    }

    /**
     * Clears the stack
     */
    fun clear() {
        namespaceStack.clear()
        namespaceCacheList.clear()
        rootNamespaceCache.clear()
        currentNamespaceCache = null
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index
     * DOCUMENT ME!
     * 
     * @return the namespace at the specified index on the stack
     */
    fun getNamespace(index: Int): Namespace? {
        return namespaceStack.get(index) as Namespace?
    }

    /**
     * DOCUMENT ME!
     * 
     * @param prefix
     * DOCUMENT ME!
     * 
     * @return the namespace for the given prefix or null if it could not be
     * found.
     */
    fun getNamespaceForPrefix(prefix: String?): Namespace? {
        var prefix = prefix
        if (prefix == null) {
            prefix = ""
        }

        for (i in namespaceStack.indices.reversed()) {
            val namespace = namespaceStack.get(i) as Namespace

            if (prefix == namespace.getPrefix()) {
                return namespace
            }
        }

        return null
    }

    /**
     * DOCUMENT ME!
     * 
     * @param prefix
     * DOCUMENT ME!
     * 
     * @return the URI for the given prefix or null if it could not be found.
     */
    fun getURI(prefix: String?): String? {
        val namespace = getNamespaceForPrefix(prefix)

        return if (namespace != null) namespace.getURI() else null
    }

    /**
     * DOCUMENT ME!
     * 
     * @param namespace
     * DOCUMENT ME!
     * 
     * @return true if the given prefix is in the stack.
     */
    fun contains(namespace: Namespace): Boolean {
        val prefix = namespace.getPrefix()
        var current: Namespace? = null

        if ((prefix == null) || (prefix.length == 0)) {
            current = getDefaultNamespace()
        } else {
            current = getNamespaceForPrefix(prefix)
        }

        if (current == null) {
            return false
        }

        if (current === namespace) {
            return true
        }

        return namespace.getURI() == current.getURI()
    }

    fun getQName(namespaceURI: String?, localName: String?, qualifiedName: String?): QName? {
        var namespaceURI = namespaceURI
        var localName = localName
        var qualifiedName = qualifiedName
        if (localName == null) {
            localName = qualifiedName
        } else if (qualifiedName == null) {
            qualifiedName = localName
        }

        if (namespaceURI == null) {
            namespaceURI = ""
        }

        var prefix = ""
        val index = qualifiedName!!.indexOf(":")

        if (index > 0) {
            prefix = qualifiedName.substring(0, index)

            if (localName!!.trim { it <= ' ' }.length == 0) {
                localName = qualifiedName.substring(index + 1)
            }
        } else if (localName!!.trim { it <= ' ' }.length == 0) {
            localName = qualifiedName
        }

        val namespace = createNamespace(prefix, namespaceURI)

        return pushQName(localName, qualifiedName, namespace, prefix)
    }

    fun getAttributeQName(
        namespaceURI: String?,
        localName: String?,
        qualifiedName: String?
    ): QName? {
        var namespaceURI = namespaceURI
        var localName = localName
        var qualifiedName = qualifiedName
        if (qualifiedName == null) {
            qualifiedName = localName
        }

        val map = this.namespaceCache
        var answer = map.get(qualifiedName) as QName?

        if (answer != null) {
            return answer
        }

        if (localName == null) {
            localName = qualifiedName
        }

        if (namespaceURI == null) {
            namespaceURI = ""
        }

        var namespace: Namespace? = null
        var prefix = ""
        val index = qualifiedName!!.indexOf(":")

        if (index > 0) {
            prefix = qualifiedName.substring(0, index)
            namespace = createNamespace(prefix, namespaceURI)

            if (localName!!.trim { it <= ' ' }.length == 0) {
                localName = qualifiedName.substring(index + 1)
            }
        } else {
            // attributes with no prefix have no namespace
            namespace = Namespace.Companion.NO_NAMESPACE

            if (localName!!.trim { it <= ' ' }.length == 0) {
                localName = qualifiedName
            }
        }

        answer = pushQName(localName, qualifiedName, namespace, prefix)
        (map as MutableMap<Any?, Any?>).put(qualifiedName, answer)

        return answer
    }

    /**
     * Adds a namepace to the stack with the given prefix and URI
     * 
     * @param prefix
     * DOCUMENT ME!
     * @param uri
     * DOCUMENT ME!
     */
    fun push(prefix: String?, uri: String?) {
        var uri = uri
        if (uri == null) {
            uri = ""
        }

        val namespace = createNamespace(prefix, uri)
        push(namespace)
    }

    /**
     * Adds a new namespace to the stack
     * 
     * @param prefix
     * DOCUMENT ME!
     * @param uri
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    fun addNamespace(prefix: String?, uri: String?): Namespace {
        val namespace = createNamespace(prefix, uri)
        push(namespace)

        return namespace
    }

    /**
     * Pops a namepace from the stack with the given prefix and URI
     * 
     * @param prefix
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    fun pop(prefix: String?): Namespace? {
        var prefix = prefix
        if (prefix == null) {
            prefix = ""
        }

        var namespace: Namespace? = null

        for (i in namespaceStack.indices.reversed()) {
            val ns = namespaceStack.get(i) as Namespace

            if (prefix == ns.getPrefix()) {
                remove(i)
                namespace = ns

                break
            }
        }

        if (namespace == null) {
            println("Warning: missing namespace prefix ignored: " + prefix)
        }

        return namespace
    }

    override fun toString(): String {
        return super.toString() + " Stack: " + namespaceStack.toString()
    }

    fun getDefaultNamespace(): Namespace? {
        if (defaultNamespace == null) {
            defaultNamespace = findDefaultNamespace()
        }

        return defaultNamespace
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    /**
     * Adds the QName to the stack of available QNames
     * 
     * @param localName
     * DOCUMENT ME!
     * @param qualifiedName
     * DOCUMENT ME!
     * @param namespace
     * DOCUMENT ME!
     * @param prefix
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun pushQName(
        localName: String?, qualifiedName: String?, namespace: Namespace?,
        prefix: String?
    ): QName? {
        if ((prefix == null) || (prefix.length == 0)) {
            this.defaultNamespace = null
        }

        return createQName(localName, qualifiedName, namespace)
    }

    /**
     * Factory method to creeate new QName instances. By default this method
     * interns the QName
     * 
     * @param localName
     * DOCUMENT ME!
     * @param qualifiedName
     * DOCUMENT ME!
     * @param namespace
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun createQName(
        localName: String?,
        qualifiedName: String?,
        namespace: Namespace?
    ): QName? {
        return documentFactory.createQName(localName, namespace)
    }

    /**
     * Factory method to creeate new Namespace instances. By default this method
     * interns the Namespace
     * 
     * @param prefix
     * DOCUMENT ME!
     * @param namespaceURI
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun createNamespace(prefix: String?, namespaceURI: String?): Namespace {
        return documentFactory.createNamespace(prefix, namespaceURI) ?: Namespace(prefix, namespaceURI)
    }

    /**
     * Attempts to find the current default namespace on the stack right now or
     * returns null if one could not be found
     * 
     * @return DOCUMENT ME!
     */
    protected fun findDefaultNamespace(): Namespace? {
        for (i in namespaceStack.indices.reversed()) {
            val namespace = namespaceStack.get(i)

            if (namespace != null) {
                val prefix = namespace.getPrefix()

                if ((prefix == null) || (prefix.length == 0)) {
                    return namespace
                }
            }
        }

        return null
    }

    /**
     * Removes the namespace at the given index of the stack
     * 
     * @param index
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun remove(index: Int): Namespace? {
        val namespace = namespaceStack.removeAt(index)
        namespaceCacheList.removeAt(index)
        defaultNamespace = null
        currentNamespaceCache = null

        return namespace
    }

    protected val namespaceCache: MutableMap<*, *>
        get() {
            if (currentNamespaceCache == null) {
                val index = namespaceStack.size - 1

                if (index < 0) {
                    currentNamespaceCache = rootNamespaceCache
                } else {
                    currentNamespaceCache =
                        namespaceCacheList.get(index)

                    if (currentNamespaceCache == null) {
                        currentNamespaceCache = HashMap<Any?, Any?>()
                        namespaceCacheList.set(index, currentNamespaceCache)
                    }
                }
            }

            return currentNamespaceCache!!
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


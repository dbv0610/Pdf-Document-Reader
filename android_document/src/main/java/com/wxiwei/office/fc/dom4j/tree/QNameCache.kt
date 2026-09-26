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
import java.util.Collections
import java.util.WeakHashMap

/**
 * 
 * 
 * `QNameCache` caches instances of `QName` for reuse
 * both across documents and within documents.
 * < < < < < < < QNameCache.java
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.16 $ =======
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.16 $ >>>>>>> 1.15
 */
class QNameCache {
    /** Cache of [QName]instances with no namespace  */
    protected var noNamespaceCache: MutableMap<String, QName> =
        Collections.synchronizedMap(WeakHashMap())

    /**
     * Cache of [Map]instances indexed by namespace which contain caches
     * of [QName]for each name
     */
    protected var namespaceCache: MutableMap<Namespace?, MutableMap<String, QName>> =
        Collections.synchronizedMap(WeakHashMap())

    /**
     * The document factory associated with new QNames instances in this cache
     * or null if no instances should be associated by default
     */
    private var documentFactory: DocumentFactory? = null

    constructor()

    constructor(documentFactory: DocumentFactory?) {
        this.documentFactory = documentFactory
    }

    val qNames: MutableList<QName>
        /**
         * Returns a list of all the QName instances currently used
         * 
         * @return DOCUMENT ME!
         */
        get() {
            val answer = ArrayList<QName>()
            answer.addAll(noNamespaceCache.values as Collection<QName>)

            val it = namespaceCache.values.iterator()
            while (it.hasNext()) {
                val map =
                    it.next() as MutableMap<*, *>
                answer.addAll(map.values as Collection<QName>)
            }

            return answer
        }

    /**
     * DOCUMENT ME!
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return the QName for the given name and no namepsace
     */
    fun get(name: String?): QName {
        var name = name
        var answer: QName? = null

        if (name != null) {
            answer = noNamespaceCache.get(name) as QName?
        } else {
            name = ""
        }

        if (answer == null) {
            answer = createQName(name)
            answer.setDocumentFactory(documentFactory)
            noNamespaceCache.put(name, answer)
        }

        return answer
    }

    /**
     * DOCUMENT ME!
     * 
     * @param name
     * DOCUMENT ME!
     * @param namespace
     * DOCUMENT ME!
     * 
     * @return the QName for the given local name and namepsace
     */
    fun get(name: String?, namespace: Namespace?): QName {
        var name = name
        val cache = getNamespaceCache(namespace)
        var answer: QName? = null

        if (name != null) {
            answer = cache.get(name) as QName?
        } else {
            name = ""
        }

        if (answer == null) {
            answer = createQName(name, namespace)
            answer.setDocumentFactory(documentFactory)
            cache.put(name, answer)
        }

        return answer
    }

    /**
     * DOCUMENT ME!
     * 
     * @param localName
     * DOCUMENT ME!
     * @param namespace
     * DOCUMENT ME!
     * @param qName
     * DOCUMENT ME!
     * 
     * @return the QName for the given local name, qualified name and namepsace
     */
    fun get(localName: String?, namespace: Namespace?, qName: String?): QName {
        var localName = localName
        val cache = getNamespaceCache(namespace)
        var answer: QName? = null

        if (localName != null) {
            answer = cache.get(localName) as QName?
        } else {
            localName = ""
        }

        if (answer == null) {
            answer = createQName(localName, namespace, qName)
            answer.setDocumentFactory(documentFactory)
            cache.put(localName, answer)
        }

        return answer
    }

    fun get(qualifiedName: String?, uri: String?): QName {
        val qName = qualifiedName ?: ""
        val index = qName.indexOf(':')

        if (index < 0) {
            return get(qName, Namespace.get(uri))
        } else {
            val name = qName.substring(index + 1)
            val prefix = qName.substring(0, index)

            return get(name, Namespace.Companion.get(prefix, uri))
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param qname
     * DOCUMENT ME!
     * 
     * @return the cached QName instance if there is one or adds the given qname
     * to the cache if not
     */
    fun intern(qname: QName): QName {
        return get(qname.getName(), qname.getNamespace(), qname.getQualifiedName() ?: "")
    }

    /**
     * DOCUMENT ME!
     * 
     * @param namespace
     * DOCUMENT ME!
     * 
     * @return the cache for the given namespace. If one does not currently
     * exist it is created.
     */
    protected fun getNamespaceCache(namespace: Namespace?): MutableMap<String, QName> {
        if (namespace === Namespace.NO_NAMESPACE) {
            return noNamespaceCache
        }

        var answer: MutableMap<String, QName>? = null

        if (namespace != null) {
            answer = namespaceCache[namespace]
        }

        if (answer == null) {
            answer = createMap()
            namespaceCache[namespace] = answer
        }

        return answer
    }

    /**
     * A factory method
     * 
     * @return a newly created [Map]instance.
     */
    protected fun createMap(): MutableMap<String, QName> {
        return Collections.synchronizedMap(HashMap())
    }

    /**
     * Factory method to create a new QName object which can be overloaded to
     * create derived QName instances
     * 
     * @param name
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun createQName(name: String?): QName {
        return QName(name)
    }

    /**
     * Factory method to create a new QName object which can be overloaded to
     * create derived QName instances
     * 
     * @param name
     * DOCUMENT ME!
     * @param namespace
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun createQName(name: String?, namespace: Namespace?): QName {
        return QName(name, namespace)
    }

    /**
     * Factory method to create a new QName object which can be overloaded to
     * create derived QName instances
     * 
     * @param name
     * DOCUMENT ME!
     * @param namespace
     * DOCUMENT ME!
     * @param qualifiedName
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected fun createQName(name: String?, namespace: Namespace?, qualifiedName: String?): QName {
        return QName(name, namespace, qualifiedName)
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


/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Namespace
import java.lang.Float
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap
import kotlin.Any
import kotlin.String
import kotlin.Throwable
import kotlin.arrayOf
import kotlin.synchronized

/**
 * 
 * 
 * `NamespaceCache` caches instances of
 * `DefaultNamespace` for reuse both across documents and within
 * documents.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @author Maarten Coene
 * @author Brett Finnell
 * @version $Revision: 1.15 $
 */
class NamespaceCache {
    /**
     * DOCUMENT ME!
     * 
     * @param prefix
     * DOCUMENT ME!
     * @param uri
     * DOCUMENT ME!
     * 
     * @return the namespace for the given prefix and uri
     */
    fun get(prefix: String?, uri: String?): Namespace? {
        val uriCache = getURICache(uri) as MutableMap<Any?, Any?>
        var ref = uriCache.get(prefix) as WeakReference<*>?
        var answer: Namespace? = null

        if (ref != null) {
            answer = ref.get() as Namespace?
        }

        if (answer == null) {
            synchronized(uriCache) {
                ref = uriCache.get(prefix) as WeakReference<*>?
                if (ref != null) {
                    answer = ref.get() as Namespace?
                }
                if (answer == null) {
                    answer = createNamespace(prefix, uri)
                    uriCache.put(prefix, WeakReference<Any?>(answer))
                }
            }
        }

        return answer
    }

    /**
     * DOCUMENT ME!
     * 
     * @param uri
     * DOCUMENT ME!
     * 
     * @return the name model for the given name and namepsace
     */
    fun get(uri: String?): Namespace? {
        val c = noPrefixCache
        var ref = c.get(uri) as WeakReference<*>?
        var answer: Namespace? = null

        if (ref != null) {
            answer = ref.get() as Namespace?
        }

        if (answer == null) {
            synchronized(c) {
                ref = c.get(uri) as WeakReference<*>?
                if (ref != null) {
                    answer = ref.get() as Namespace?
                }
                if (answer == null) {
                    answer = createNamespace("", uri)
                    c.put(uri, WeakReference<Any?>(answer))
                }
            }
        }

        return answer
    }

    /**
     * DOCUMENT ME!
     * 
     * @param uri
     * DOCUMENT ME!
     * 
     * @return the cache for the given namespace URI. If one does not currently
     * exist it is created.
     */
    protected fun getURICache(uri: String?): MutableMap<*, *> {
        val c = cache
        var answer = c.get(uri) as MutableMap<*, *>?

        if (answer == null) {
            synchronized(c) {
                answer = c.get(uri) as MutableMap<*, *>?
                if (answer == null) {
                    answer = ConcurrentHashMap<Any?, Any?>()
                    c.put(uri, answer)
                }
            }
        }

        return answer!!
    }

    /**
     * A factory method to create [Namespace]instance
     * 
     * @param prefix
     * DOCUMENT ME!
     * @param uri
     * DOCUMENT ME!
     * 
     * @return a newly created [Namespace]instance.
     */
    protected fun createNamespace(prefix: String?, uri: String?): Namespace {
        return Namespace(prefix, uri)
    }

    companion object {
        private const val CONCURRENTREADERHASHMAP_CLASS =
            "EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap"

        /**
         * Cache of [Map]instances indexed by URI which contain caches of
         * [Namespace]for each prefix
         */
        protected var cache: MutableMap<Any?, Any?> = java.util.concurrent.ConcurrentHashMap()

        /**
         * Cache of [Namespace]instances indexed by URI for default
         * namespaces with no prefixes
         */
        protected var noPrefixCache: MutableMap<Any?, Any?> = java.util.concurrent.ConcurrentHashMap()
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


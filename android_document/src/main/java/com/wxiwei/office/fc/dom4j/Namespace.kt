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

import com.wxiwei.office.fc.dom4j.tree.AbstractNode
import com.wxiwei.office.fc.dom4j.tree.DefaultNamespace
import com.wxiwei.office.fc.dom4j.tree.NamespaceCache

/**
 * 
 * 
 * `Namespace` is a Flyweight Namespace that can be shared amongst
 * nodes.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.22 $
 */
open class Namespace(prefix: String?, uri: String?) : AbstractNode() {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getPrefixProperty")
    open val prefix: String? = prefix ?: ""
    open val uri: String = uri ?: ""
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getURIProperty")
    open val uRI: String get() = uri
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getURIUppercaseProperty")
    open val URI: String get() = uri

    /** A cached version of the hashcode for efficiency  */
    private var hashCode = 0

    init {
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    override val nodeType: Short
        get() = Node.NAMESPACE_NODE

    /**
     * DOCUMENT ME!
     * 
     * @return the hash code based on the qualified name and the URI of the
     * namespace.
     */
    override fun hashCode(): Int {
        if (hashCode == 0) {
            hashCode = createHashCode()
        }

        return hashCode
    }

    /**
     * Factory method to create the hashcode allowing derived classes to change
     * the behaviour
     * 
     * @return DOCUMENT ME!
     */
    protected open fun createHashCode(): Int {
        var result = uri.hashCode() xor prefix.hashCode()

        if (result == 0) {
            result = 0xbabe
        }

        return result
    }

    /**
     * Checks whether this Namespace equals the given Namespace. Two Namespaces
     * are equals if their URI and prefix are equal.
     * 
     * @param object
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    override fun equals(`object`: Any?): Boolean {
        if (this === `object`) {
            return true
        } else if (`object` is Namespace) {
            val that = `object`

            // we cache hash codes so this should be quick
            if (hashCode() == that.hashCode()) {
                return this.uri == that.uri && prefix == that.prefix
            }
        }

        return false
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() = this.uri
        set(value) {
            super.text = value
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String?
        get() = this.uri

    /**
     * DOCUMENT ME!
     *
     * @return the prefix for this `Namespace`.
     */
    open fun getPrefix(): String? {
        return prefix
    }

    /**
     * DOCUMENT ME!
     *
     * @return the URI for this `Namespace`.
     */
    open fun getURI(): String? {
        return uri
    }

    val xPathNameStep: String
        get() {
            if ((prefix != null) && "" != prefix) {
                return "namespace::" + prefix
            }

            return "namespace::*[name()='']"
        }

    override fun getPath(context: Element?): String? {
        val path = StringBuffer(10)
        val parent = this.parent

        if ((parent != null) && (parent !== context)) {
            path.append(parent.getPath(context))
            path.append('/')
        }

        path.append(this.xPathNameStep)

        return path.toString()
    }

    override fun getUniquePath(context: Element?): String? {
        val path = StringBuffer(10)
        val parent = this.parent

        if ((parent != null) && (parent !== context)) {
            path.append(parent.getUniquePath(context))
            path.append('/')
        }

        path.append(this.xPathNameStep)

        return path.toString()
    }

    override fun toString(): String {
        return (super.toString() + " [Namespace: prefix " + this.prefix + " mapped to URI \""
                + this.uri + "\"]")
    }

    override fun asXML(): String? {
        val asxml = StringBuffer(10)
        val pref = this.prefix

        if ((pref != null) && (pref.length > 0)) {
            asxml.append("xmlns:")
            asxml.append(pref)
            asxml.append("=\"")
        } else {
            asxml.append("xmlns=\"")
        }

        asxml.append(this.uri)
        asxml.append("\"")

        return asxml.toString()
    }

    override fun accept(visitor: Visitor?) {
        visitor!!.visit(this)
    }

    override fun createXPathResult(parent: Element?): Node? {
        return DefaultNamespace(parent, this.prefix, this.uri)
    }

    companion object {
        /** Cache of Namespace instances  */
        protected val CACHE: NamespaceCache = NamespaceCache()

        /** XML Namespace  */
        @JvmField
        val XML_NAMESPACE: Namespace = CACHE.get(
            "xml",
            "http://www.w3.org/XML/1998/namespace"
        )!!

        /** No Namespace present  */
        @JvmField
        val NO_NAMESPACE: Namespace = CACHE.get("", "")!!

        /**
         * A helper method to return the Namespace instance for the given prefix and
         * URI
         * 
         * @param prefix
         * DOCUMENT ME!
         * @param uri
         * DOCUMENT ME!
         * 
         * @return an interned Namespace object
         */
        @JvmStatic
        fun get(prefix: String?, uri: String?): Namespace? {
            return CACHE.get(prefix, uri)
        }

        /**
         * A helper method to return the Namespace instance for no prefix and the
         * URI
         * 
         * @param uri
         * DOCUMENT ME!
         * 
         * @return an interned Namespace object
         */
        @JvmStatic
        fun get(uri: String?): Namespace? {
            return CACHE.get(uri)
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


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

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace

/**
 * 
 * 
 * `DefaultNamespace` implements a doubly linked node which
 * supports the parent relationship and is mutable. It is useful when returning
 * results from XPath expressions.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.16 $
 */
open class DefaultNamespace : Namespace {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getParentProperty")
    @set:JvmName("setParentProperty")
    override var parent: Element?
        get() = getParent()
        set(value) { setParent(value) }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("isReadOnlyProperty")
    override val isReadOnly: Boolean
        get() = isReadOnly()


    /** The parent of this node  */
    private var parentNode: Element? = null

    /**
     * DOCUMENT ME!
     * 
     * @param prefix
     * is the prefix for this namespace
     * @param uri
     * is the URI for this namespace
     */
    constructor(prefix: String?, uri: String?) : super(prefix, uri)

    /**
     * DOCUMENT ME!
     * 
     * @param parent
     * is the parent element
     * @param prefix
     * is the prefix for this namespace
     * @param uri
     * is the URI for this namespace
     */
    constructor(parent: Element?, prefix: String?, uri: String?) : super(prefix, uri) {
        this.parentNode = parent
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the hash code based on the qualified name and the URI of the
     * namespace and the hashCode() of the parent element.
     */
    override fun createHashCode(): Int {
        var hashCode = super.createHashCode()

        if (parentNode != null) {
            hashCode = hashCode xor parentNode.hashCode()
        }

        return hashCode
    }

    /**
     * Implements an identity based comparsion using the parent element as well
     * as the prefix and URI
     * 
     * @param object
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    override fun equals(`object`: Any?): Boolean {
        if (`object` is DefaultNamespace) {
            val that = `object`

            if (that.parentNode === parentNode) {
                return super.equals(`object`)
            }
        }

        return false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun getParent(): Element? {
        return parentNode
    }

    override fun setParent(parent: Element?) {
        this.parentNode = parent
    }

    override fun supportsParent(): Boolean {
        return true
    }

    override fun isReadOnly(): Boolean {
        return false
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


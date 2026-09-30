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

import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName

/**
 * 
 * 
 * `BaseElement` is a useful base class for implemementation
 * inheritence of an XML element.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.9 $
 */
open class BaseElement : AbstractElement {
    /** The `QName` for this element  */
    private var qname: QName?

    /**
     * Stores the parent branch of this node which is either a Document if this
     * element is the root element in a document, or another Element if it is a
     * child of the root document, or null if it has not been added to a
     * document yet.
     */
    private var parentBranch: Branch? = null

    /** List of content nodes.  */
    @set:JvmName("setContentProperty")
    protected var content: MutableList<Any?>? = null

    /** list of attributes  */
    @set:JvmName("setAttributesProperty")
    protected var attributes: MutableList<Any?>? = null

    constructor(name: String?) {
        this.qname = getDocumentFactory().createQName(name)
    }

    constructor(qname: QName?) {
        this.qname = qname
    }

    constructor(name: String?, namespace: Namespace?) {
        this.qname = getDocumentFactory().createQName(name, namespace)
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getParentProperty")
    @set:JvmName("setParentProperty")
    override var parent: Element?
        get() {
            var result: Element? = null

            if (parentBranch is Element) {
                result = parentBranch as Element
            }

            return result
        }
        set(parent) {
            if (parentBranch is Element || (parent != null)) {
                parentBranch = parent
            }
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDocumentProperty")
    @set:JvmName("setDocumentProperty")
    override var document: Document?
        get() {
            if (parentBranch is Document) {
                return parentBranch as Document
            } else if (parentBranch is Element) {
                val parent = parentBranch as Element

                return parent.document
            }

            return null
        }
        set(document) {
            if (parentBranch is Document || (document != null)) {
                parentBranch = document
            }
        }

    override fun supportsParent(): Boolean {
        return true
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getQNameProperty")
    @set:JvmName("setQNameProperty")
    override var qName: QName?
        get() = qname
        set(name) {
            this.qname = name
        }

    override fun clearContent() {
        contentList().clear()
    }

    override fun setContent(content: MutableList<*>?) {
        this.content = content as MutableList<Any?>?

        if (content is ContentListFacade) {
            this.content = content.backingList
        }
    }

    override fun setAttributes(attributes: MutableList<*>?) {
        this.attributes = attributes as MutableList<Any?>?

        if (attributes is ContentListFacade) {
            this.attributes = attributes.backingList
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    override fun contentList(): MutableList<Any?> {
        if (content == null) {
            content = createContentList()
        }

        return content!!
    }

    override fun attributeList(): MutableList<Any?> {
        if (attributes == null) {
            attributes = createAttributeList()
        }

        return attributes!!
    }

    override fun attributeList(size: Int): MutableList<Any?> {
        if (attributes == null) {
            attributes = createAttributeList(size)
        }

        return attributes!!
    }

    protected fun setAttributeList(attributeList: MutableList<Any?>?) {
        this.attributes = attributeList
    }
}
/*
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


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

import com.wxiwei.office.fc.dom4j.DocumentType
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.Visitor
import java.io.IOException
import java.io.Writer

/**
 * 
 * 
 * `AbstractDocumentType` is an abstract base class for tree
 * implementors to use for implementation inheritence.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.17 $
 */
abstract class AbstractDocumentType : AbstractNode(), DocumentType {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNameProperty")
    @set:JvmName("setNameProperty")
    override var name: String?
        get() = getName()
        set(value) { setName(value) }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() = getText()
        set(value) { super.text = value }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    override val nodeType: Short
        get() = getNodeType()


    abstract override fun getElementName(): String?
    abstract override fun setElementName(elementName: String?)
    abstract override fun getPublicID(): String?
    abstract override fun setPublicID(publicID: String?)
    abstract override fun getSystemID(): String?
    abstract override fun setSystemID(systemID: String?)
    abstract override fun getInternalDeclarations(): MutableList<*>?
    abstract override fun setInternalDeclarations(internalDeclarations: MutableList<*>?)
    abstract override fun getExternalDeclarations(): MutableList<*>?
    abstract override fun setExternalDeclarations(externalDeclarations: MutableList<*>?)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getElementNameProperty")
    @set:JvmName("setElementNameProperty")
    override var elementName: String? get() = getElementName(); set(e) = setElementName(e)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getPublicIDProperty")
    @set:JvmName("setPublicIDProperty")
    override var publicID: String? get() = getPublicID(); set(p) = setPublicID(p)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getSystemIDProperty")
    @set:JvmName("setSystemIDProperty")
    override var systemID: String? get() = getSystemID(); set(s) = setSystemID(s)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getInternalDeclarationsProperty")
    @set:JvmName("setInternalDeclarationsProperty")
    override var internalDeclarations: MutableList<*>? get() = getInternalDeclarations(); set(i) = setInternalDeclarations(i)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getExternalDeclarationsProperty")
    @set:JvmName("setExternalDeclarationsProperty")
    override var externalDeclarations: MutableList<*>? get() = getExternalDeclarations(); set(e) = setExternalDeclarations(e)

    override fun getNodeType(): Short {
        return Node.Companion.DOCUMENT_TYPE_NODE
    }

    override fun getName(): String? {
        return getElementName()
    }

    override fun setName(name: String?) {
        setElementName(name)
    }

    override fun getPath(context: Element?): String {
        // not available in XPath
        return ""
    }

    override fun getUniquePath(context: Element?): String {
        // not available in XPath
        return ""
    }

    /**
     * Returns the text format of the declarations if applicable, or the empty
     * String
     * 
     * @return DOCUMENT ME!
     */
    override fun getText(): String {
        val list = getInternalDeclarations()

        if ((list != null) && (list.size > 0)) {
            val buffer = StringBuffer()
            val iter: MutableIterator<*> = list.iterator()

            if (iter.hasNext()) {
                var decl: Any = iter.next()!!
                buffer.append(decl.toString())

                while (iter.hasNext()) {
                    decl = iter.next()!!
                    buffer.append("\n")
                    buffer.append(decl.toString())
                }
            }

            return buffer.toString()
        }

        return ""
    }

    override fun toString(): String {
        return super.toString() + " [DocumentType: " + asXML() + "]"
    }

    override fun asXML(): String {
        val buffer = StringBuffer("<!DOCTYPE ")
        buffer.append(getElementName())

        var hasPublicID = false
        val publicID = getPublicID()

        if ((publicID != null) && (publicID.length > 0)) {
            buffer.append(" PUBLIC \"")
            buffer.append(publicID)
            buffer.append("\"")
            hasPublicID = true
        }

        val systemID = getSystemID()

        if ((systemID != null) && (systemID.length > 0)) {
            if (!hasPublicID) {
                buffer.append(" SYSTEM")
            }

            buffer.append(" \"")
            buffer.append(systemID)
            buffer.append("\"")
        }

        buffer.append(">")

        return buffer.toString()
    }

    @Throws(IOException::class)
    override fun write(writer: Writer?) {
        if (writer == null) return
        writer.write("<!DOCTYPE ")
        writer.write(getElementName())

        var hasPublicID = false
        val publicID = getPublicID()

        if ((publicID != null) && (publicID.length > 0)) {
            writer.write(" PUBLIC \"")
            writer.write(publicID)
            writer.write("\"")
            hasPublicID = true
        }

        val systemID = getSystemID()

        if ((systemID != null) && (systemID.length > 0)) {
            if (!hasPublicID) {
                writer.write(" SYSTEM")
            }

            writer.write(" \"")
            writer.write(systemID)
            writer.write("\"")
        }

        val list = getInternalDeclarations()

        if ((list != null) && (list.size > 0)) {
            writer.write(" [")

            val iter: MutableIterator<*> = list.iterator()
            while (iter.hasNext()) {
                val decl: Any = iter.next()!!
                writer.write("\n  ")
                writer.write(decl.toString())
            }

            writer.write("\n]")
        }

        writer.write(">")
    }

    override fun accept(visitor: Visitor?) {
        visitor?.visit(this as DocumentType)
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


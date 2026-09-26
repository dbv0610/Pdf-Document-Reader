/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.Visitor
import java.io.IOException
import java.io.Writer

/**
 * 
 * 
 * `AbstractNamespace` is an abstract base class for tree
 * implementors to use for implementation inheritence.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.21 $
 */
abstract class AbstractAttribute : AbstractNode(), Attribute {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNameProperty")
    @set:JvmName("setNameProperty")
    override var name: String?
        get() = getName()
        set(value) { super.name = value }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() = getText()
        set(value) { setText(value) }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    override val nodeType: Short
        get() = getNodeType()


    abstract override fun getQName(): QName?
    abstract override fun getValue(): String?

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getQNameProperty")
    override val qName: QName? get() = getQName()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getValueProperty")
    @set:JvmName("setValueProperty")
    override var value: String? get() = getValue(); set(v) = setValue(v)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDataProperty")
    @set:JvmName("setDataProperty")
    override var data: Any? get() = getData(); set(d) = setData(d)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespaceProperty")
    @set:JvmName("setNamespaceProperty")
    override var namespace: Namespace? get() = getNamespace(); set(ns) = setNamespace(ns)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespacePrefixProperty")
    override val namespacePrefix: String? get() = getNamespacePrefix()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNamespaceURIProperty")
    override val namespaceURI: String? get() = getNamespaceURI()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getQualifiedNameProperty")
    override val qualifiedName: String? get() = getQualifiedName()

    override fun getNodeType(): Short {
        return Node.Companion.ATTRIBUTE_NODE
    }

    override fun setNamespace(namespace: Namespace?) {
        val msg = "This Attribute is read only and cannot be changed"
        throw UnsupportedOperationException(msg)
    }

    override fun getText(): String? {
        return getValue()
    }

    override fun setText(text: String?) {
        setValue(text)
    }

    override fun setValue(value: String?) {
        val msg = "This Attribute is read only and cannot be changed"
        throw UnsupportedOperationException(msg)
    }

    override fun getData(): Any? {
        return getValue()
    }

    override fun setData(data: Any?) {
        setValue(data?.toString())
    }

    override fun toString(): String {
        return (super.toString() + " [Attribute: name " + getQualifiedName() + " value \""
                + getValue() + "\"]")
    }

    override fun asXML(): String {
        return getQualifiedName() + "=\"" + getValue() + "\""
    }

    @Throws(IOException::class)
    override fun write(writer: Writer?) {
        if (writer == null) return
        writer.write(getQualifiedName())
        writer.write("=\"")
        writer.write(getValue())
        writer.write("\"")
    }

    override fun accept(visitor: Visitor?) {
        visitor?.visit(this)
    }

    // QName methods
    override fun getNamespace(): Namespace? {
        return getQName()?.getNamespace()
    }

    override fun getName(): String? {
        return getQName()?.getName()
    }

    override fun getNamespacePrefix(): String? {
        return getQName()?.getNamespacePrefix()
    }

    override fun getNamespaceURI(): String? {
        return getQName()?.getNamespaceURI()
    }

    override fun getQualifiedName(): String? {
        return getQName()?.getQualifiedName()
    }

    override fun getPath(context: Element?): String {
        val result = StringBuffer()

        val parent = getParent()

        if ((parent != null) && (parent !== context)) {
            result.append(parent.getPath(context))
            result.append("/")
        }

        result.append("@")

        val uri = getNamespaceURI()
        val prefix = getNamespacePrefix()

        if ((uri == null) || (uri.length == 0) || (prefix == null) || (prefix.length == 0)) {
            result.append(getName())
        } else {
            result.append(getQualifiedName())
        }

        return result.toString()
    }

    override fun getUniquePath(context: Element?): String {
        val result = StringBuffer()

        val parent = getParent()

        if ((parent != null) && (parent !== context)) {
            result.append(parent.getUniquePath(context))
            result.append("/")
        }

        result.append("@")

        val uri = getNamespaceURI()
        val prefix = getNamespacePrefix()

        if ((uri == null) || (uri.length == 0) || (prefix == null) || (prefix.length == 0)) {
            result.append(getName())
        } else {
            result.append(getQualifiedName())
        }

        return result.toString()
    }

    override fun createXPathResult(parent: Element?): Node {
        return DefaultAttribute(parent, getQName(), getValue())
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


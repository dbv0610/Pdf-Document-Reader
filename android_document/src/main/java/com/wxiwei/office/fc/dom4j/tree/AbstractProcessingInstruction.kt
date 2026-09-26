/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.Visitor
import java.io.IOException
import java.io.Writer
import java.util.StringTokenizer

/**
 * 
 * 
 * `AbstractProcessingInstruction` is an abstract base class for
 * tree implementors to use for implementation inheritence.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.17 $
 */
abstract class AbstractProcessingInstruction : AbstractNode(), ProcessingInstruction {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNameProperty")
    @set:JvmName("setNameProperty")
    override var name: String?
        get() = getName()
        set(value) { setName(value) }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    override val nodeType: Short
        get() = getNodeType()


    abstract override fun getTarget(): String?
    abstract override fun setTarget(target: String?)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTargetProperty")
    @set:JvmName("setTargetProperty")
    override var target: String? get() = getTarget(); set(t) = setTarget(t)
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getValuesProperty")
    @set:JvmName("setValuesProperty")
    override var values: MutableMap<*, *>? get() = getValues(); set(v) = setValues(v)

    override fun getNodeType(): Short {
        return Node.Companion.PROCESSING_INSTRUCTION_NODE
    }

    override fun getPath(context: Element?): String {
        val parent = getParent()

        return if ((parent != null) && (parent !== context)) (parent.getPath(context) + "/processing-instruction()") else "processing-instruction()"
    }

    override fun getUniquePath(context: Element?): String {
        val parent = getParent()

        return if ((parent != null) && (parent !== context))
            (parent.getUniquePath(context) + "/processing-instruction()")
        else
            "processing-instruction()"
    }

    override fun toString(): String {
        return super.toString() + " [ProcessingInstruction: &" + getName() + ";]"
    }

    override fun asXML(): String {
        return "<?" + getName() + " " + getText() + "?>"
    }

    @Throws(IOException::class)
    override fun write(writer: Writer?) {
        if (writer == null) return
        writer.write("<?")
        writer.write(getName())
        writer.write(" ")
        writer.write(getText())
        writer.write("?>")
    }

    override fun accept(visitor: Visitor?) {
        visitor?.visit(this as ProcessingInstruction)
    }

    override fun setValue(name: String?, value: String?) {
        throw UnsupportedOperationException("This PI is read-only and " + "cannot be modified")
    }

    override fun setValues(data: MutableMap<*, *>?) {
        throw UnsupportedOperationException("This PI is read-only and " + "cannot be modified")
    }

    override fun getName(): String? {
        return getTarget()
    }

    override fun setName(name: String?) {
        setTarget(name)
    }

    override fun removeValue(name: String?): Boolean {
        return false
    }

    // Helper methods
    /**
     * 
     * 
     * This will convert the Map to a string representation.
     * 
     * 
     * @param values
     * is a `Map` of PI data to convert
     * 
     * @return DOCUMENT ME!
     */
    protected fun toString(values: MutableMap<*, *>): String {
        val buffer = StringBuffer()

        val iter: MutableIterator<*> = values.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next() as MutableMap.MutableEntry<*, *>
            val name = entry.key as String?
            val value = entry.value as String?

            buffer.append(name)
            buffer.append("=\"")
            buffer.append(value)
            buffer.append("\" ")
        }

        // remove the last space
        buffer.setLength(buffer.length - 1)

        return buffer.toString()
    }

    /**
     * 
     * 
     * Parses the raw data of PI as a `Map`.
     * 
     * 
     * @param text
     * `String` PI data to parse
     * 
     * @return DOCUMENT ME!
     */
    protected fun parseValues(text: String?): MutableMap<*, *> {
        val data: MutableMap<*, *> = HashMap<Any?, Any?>()

        val s = StringTokenizer(text, " =\'\"", true)

        while (s.hasMoreTokens()) {
            val name = getName(s)

            if (s.hasMoreTokens()) {
                val value = getValue(s)
                (data as MutableMap<Any?, Any?>).put(name, value)
            }
        }

        return data
    }

    private fun getName(tokenizer: StringTokenizer): String {
        var token = tokenizer.nextToken()
        val name = StringBuffer(token)

        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken()

            if (token != "=") {
                name.append(token)
            } else {
                break
            }
        }

        return name.toString().trim { it <= ' ' }
    }

    private fun getValue(tokenizer: StringTokenizer): String {
        var token = tokenizer.nextToken()
        val value = StringBuffer()

        /* get the quote */
        while (tokenizer.hasMoreTokens() && (token != "\'") && (token != "\"")) {
            token = tokenizer.nextToken()
        }

        val quote = token

        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken()

            if (quote != token) {
                value.append(token)
            } else {
                break
            }
        }

        return value.toString()
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


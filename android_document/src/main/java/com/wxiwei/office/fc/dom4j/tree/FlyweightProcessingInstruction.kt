/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Node
import java.util.Collections

/**
 * 
 * 
 * `FlyweightProcessingInstruction` is a Flyweight pattern
 * implementation of a singly linked, read-only XML Processing Instruction.
 * 
 * 
 * 
 * 
 * This node could be shared across documents and elements though it does not
 * support the parent relationship.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.7 $
 */
open class FlyweightProcessingInstruction : AbstractProcessingInstruction {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    override var text: String?
        get() = getText()
        set(value) { super.text = value }


    /** The target of the PI  */
    protected var targetStr: String? = null

    /** The values for the PI as a String  */
    protected var textStr: String? = null

    /** The values for the PI in name/value pairs  */
    protected var valuesMap: MutableMap<*, *>? = null

    /**
     * A default constructor for implementors to use.
     */
    constructor()

    /**
     * 
     * 
     * This will create a new PI with the given target and values
     * 
     * 
     * @param target
     * is the name of the PI
     * @param values
     * is the `Map` of the values for the PI
     */
    constructor(target: String?, values: MutableMap<*, *>) {
        this.targetStr = target
        this.valuesMap = values
        this.textStr = toString(values)
    }

    /**
     * 
     * 
     * This will create a new PI with the given target and values
     * 
     * 
     * @param target
     * is the name of the PI
     * @param text
     * is the values for the PI as text
     */
    constructor(target: String?, text: String?) {
        this.targetStr = target
        this.textStr = text
        this.valuesMap = parseValues(text)
    }

    override fun getTarget(): String? {
        return targetStr
    }

    override fun setTarget(target: String?) {
        throw UnsupportedOperationException("This PI is read-only and " + "cannot be modified")
    }

    override fun getText(): String? {
        return textStr
    }

    override fun getValue(name: String?): String? {
        val answer = valuesMap?.get(name) as String?

        if (answer == null) {
            return ""
        }

        return answer
    }

    override fun getValues(): MutableMap<*, *>? {
        return valuesMap
    }

    override fun createXPathResult(parent: Element?): Node {
        return DefaultProcessingInstruction(parent, getTarget(), getText())
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


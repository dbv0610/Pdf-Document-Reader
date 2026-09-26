/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.dtd

/**
 * 
 * 
 * `InternalEntityDecl` represents an internal entity declaration
 * in a DTD.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.9 $
 */
class InternalEntityDecl {
    /** Holds value of property name.  */
    private var name: String? = null

    /** Holds value of property value.  */
    private var value: String? = null

    constructor()

    constructor(name: String?, value: String?) {
        this.name = name
        this.value = value
    }

    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
    fun getName(): String {
        return name!!
    }

    /**
     * Setter for property name.
     * 
     * @param name
     * New value of property name.
     */
    fun setName(name: String) {
        this.name = name
    }

    /**
     * Getter for property value.
     * 
     * @return Value of property value.
     */
    fun getValue(): String {
        return value!!
    }

    /**
     * Setter for property value.
     * 
     * @param value
     * New value of property value.
     */
    fun setValue(value: String) {
        this.value = value
    }

    override fun toString(): String {
        val buffer = StringBuffer("<!ENTITY ")

        if (name!!.startsWith("%")) {
            buffer.append("% ")
            buffer.append(name!!.substring(1))
        } else {
            buffer.append(name)
        }

        buffer.append(" \"")
        buffer.append(escapeEntityValue(value!!))
        buffer.append("\">")

        return buffer.toString()
    }

    private fun escapeEntityValue(text: String): String {
        val result = StringBuffer()

        for (i in 0..<text.length) {
            val c = text.get(i)

            when (c) {
                '<' -> result.append("&#38;#60;")

                '>' -> result.append("&#62;")

                '&' -> result.append("&#38;#38;")

                '\'' -> result.append("&#39;")

                '\"' -> result.append("&#34;")

                else -> if (c.code < 32) {
                    result.append("&#" + c.code + ";")
                } else {
                    result.append(c)
                }

            }
        }

        return result.toString()
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


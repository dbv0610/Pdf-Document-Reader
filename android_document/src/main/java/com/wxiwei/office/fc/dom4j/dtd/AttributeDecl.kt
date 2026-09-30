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
package com.wxiwei.office.fc.dom4j.dtd

/**
 * 
 * 
 * `AttributeDecl` represents an attribute declaration in a DTD.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.7 $
 */
class AttributeDecl {
    /**
     * Getter for property elementName.
     * 
     * @return Value of property elementName.
     */
    /**
     * Setter for property elementName.
     * 
     * @param elementName
     * New value of property elementName.
     */
    /** Holds value of property elementName.  */
    var elementName: String? = null

    /**
     * Getter for property attributeName.
     * 
     * @return Value of property attributeName.
     */
    /**
     * Setter for property attributeName.
     * 
     * @param attributeName
     * New value of property attributeName.
     */
    /** Holds value of property attributeName.  */
    var attributeName: String? = null

    /**
     * Getter for property type.
     * 
     * @return Value of property type.
     */
    /**
     * Setter for property type.
     * 
     * @param type
     * New value of property type.
     */
    /** Holds value of property type.  */
    var type: String? = null

    /**
     * Getter for property value.
     * 
     * @return Value of property value.
     */
    /**
     * Setter for property value.
     * 
     * @param value
     * New value of property value.
     */
    /** Holds value of property value.  */
    var value: String? = null

    /**
     * Getter for property valueDefault.
     * 
     * @return Value of property valueDefault.
     */
    /**
     * Setter for property valueDefault.
     * 
     * @param valueDefault
     * New value of property valueDefault.
     */
    /** Holds value of property valueDefault.  */
    var valueDefault: String? = null

    constructor()

    constructor(
        elementName: String?, attributeName: String?, type: String?,
        valueDefault: String?, value: String?
    ) {
        this.elementName = elementName
        this.attributeName = attributeName
        this.type = type
        this.value = value
        this.valueDefault = valueDefault
    }

    override fun toString(): String {
        val buffer = StringBuffer("<!ATTLIST ")
        buffer.append(elementName)
        buffer.append(" ")
        buffer.append(attributeName)
        buffer.append(" ")
        buffer.append(type)
        buffer.append(" ")

        if (valueDefault != null) {
            buffer.append(valueDefault)

            if (valueDefault == "#FIXED") {
                buffer.append(" \"")
                buffer.append(value)
                buffer.append("\"")
            }
        } else {
            buffer.append("\"")
            buffer.append(value)
            buffer.append("\"")
        }

        buffer.append(">")

        return buffer.toString()
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


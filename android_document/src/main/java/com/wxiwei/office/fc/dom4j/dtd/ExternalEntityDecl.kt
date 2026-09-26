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
 * `ExternalEntityDecl` represents an external entity declaration
 * in a DTD.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.9 $
 */
class ExternalEntityDecl {
    /** Holds value of property name.  */
    private var name: String? = null

    /**
     * Getter for property publicID.
     * 
     * @return Value of property publicID.
     */
    /**
     * Setter for property publicID.
     * 
     * @param publicID
     * New value of property publicID.
     */
    /** Holds value of property publicID.  */
    var publicID: String? = null

    /**
     * Getter for property systemID.
     * 
     * @return Value of property systemID.
     */
    /**
     * Setter for property systemID.
     * 
     * @param systemID
     * New value of property systemID.
     */
    /** Holds value of property systemID.  */
    var systemID: String? = null

    constructor()

    constructor(name: String?, publicID: String?, systemID: String?) {
        this.name = name
        this.publicID = publicID
        this.systemID = systemID
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

    override fun toString(): String {
        val buffer = StringBuffer("<!ENTITY ")

        if (name!!.startsWith("%")) {
            buffer.append("% ")
            buffer.append(name!!.substring(1))
        } else {
            buffer.append(name)
        }

        if (publicID != null) {
            buffer.append(" PUBLIC \"")
            buffer.append(publicID)
            buffer.append("\" ")

            if (systemID != null) {
                buffer.append("\"")
                buffer.append(systemID)
                buffer.append("\" ")
            }
        } else if (systemID != null) {
            buffer.append(" SYSTEM \"")
            buffer.append(systemID)
            buffer.append("\" ")
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


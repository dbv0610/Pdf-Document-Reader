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

/**
 * 
 * 
 * `DefaultDocumentType` is the DOM4J default implementation of an
 * XML document type.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.10 $
 */
open class DefaultDocumentType : AbstractDocumentType {
    /** The root element name of the document typ  */
    protected var elementNameStr: String? = null

    /** Holds value of property publicID.  */
    private var publicIDStr: String? = null

    /** Holds value of property systemID.  */
    private var systemIDStr: String? = null

    /** The internal DTD declarations  */
    private var internalDeclarationsList: MutableList<*>? = null

    /** The external DTD declarations  */
    private var externalDeclarationsList: MutableList<*>? = null

    constructor()

    /**
     * 
     * 
     * This will create a new `DocumentType` with a reference to
     * the external DTD
     * 
     * 
     * @param elementName
     * is the root element name of the document type
     * @param systemID
     * is the system ID of the external DTD
     */
    constructor(elementName: String?, systemID: String?) {
        this.elementNameStr = elementName
        this.systemIDStr = systemID
    }

    /**
     * 
     * 
     * This will create a new `DocumentType` with a reference to
     * the external DTD
     * 
     * 
     * @param elementName
     * is the root element name of the document type
     * @param publicID
     * is the public ID of the DTD
     * @param systemID
     * is the system ID of the DTD
     */
    constructor(
        elementName: String?, publicID: String?,
        systemID: String?
    ) {
        this.elementNameStr = elementName
        this.publicIDStr = publicID
        this.systemIDStr = systemID
    }

    override fun getElementName(): String? {
        return elementNameStr
    }

    override fun setElementName(elementName: String?) {
        this.elementNameStr = elementName
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the public ID of the document type
     */
    override fun getPublicID(): String? {
        return publicIDStr
    }

    /**
     * Sets the public ID of the document type
     * 
     * @param publicID
     * DOCUMENT ME!
     */
    override fun setPublicID(publicID: String?) {
        this.publicIDStr = publicID
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the system ID of the document type
     */
    override fun getSystemID(): String? {
        return systemIDStr
    }

    /**
     * Sets the system ID of the document type
     * 
     * @param systemID
     * DOCUMENT ME!
     */
    override fun setSystemID(systemID: String?) {
        this.systemIDStr = systemID
    }

    override fun getInternalDeclarations(): MutableList<*>? {
        return internalDeclarationsList
    }

    override fun setInternalDeclarations(internalDeclarations: MutableList<*>?) {
        this.internalDeclarationsList = internalDeclarations
    }

    override fun getExternalDeclarations(): MutableList<*>? {
        return externalDeclarationsList
    }

    override fun setExternalDeclarations(externalDeclarations: MutableList<*>?) {
        this.externalDeclarationsList = externalDeclarations
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


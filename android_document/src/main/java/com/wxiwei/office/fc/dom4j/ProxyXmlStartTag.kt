/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j

//import org.gjt.xpp.XmlPullParserException;
//import org.gjt.xpp.XmlStartTag;
/**
 * `ProxyXmlStartTag` implements the XPP `XmlSmartTag`
 * interface while creating a dom4j `Element` underneath.
 * 
 * @author James Strachan
 * @author Maarten Coene
 * @author Wolfgang Baer
 */
class ProxyXmlStartTag //implements XmlStartTag

{
    /** The element being constructed  */
    var element: Element? = null
        private set

    /* public boolean removeAttributeByName(String namespaceURI, String localName)
        throws XmlPullParserException
    {
        if (element != null)
        {
            QName qname = QName.get(localName, namespaceURI);
            Attribute attribute = element.attribute(qname);
            return element.remove(attribute);
        }
        return false;
    }*/
    /*public boolean removeAttributeByRawName(String rawName) throws XmlPullParserException
       {
           if (element != null)
           {
               Attribute attribute = null;
               Iterator it = element.attributeIterator();
               while (it.hasNext())
               {
                   Attribute current = (Attribute)it.next();
                   if (current.getQualifiedName().equals(rawName))
                   {
                       attribute = current;
                       break;
                   }
               }
               return element.remove(attribute);
           }
           return false;
       }*/
    // Properties
    // -------------------------------------------------------------------------
    /** The factory used to create new elements  */
    var documentFactory: DocumentFactory = DocumentFactory.Companion.getInstance()

    constructor()

    constructor(element: Element?) {
        this.element = element
    }

    // XmlStartTag interface
    // -------------------------------------------------------------------------
    fun resetStartTag() {
        this.element = null
    }

    val attributeCount: Int
        get() = if (element != null) element!!.attributeCount() else 0

    fun getAttributeNamespaceUri(index: Int): String? {
        if (element != null) {
            val attribute = element!!.attribute(index)

            if (attribute != null) {
                return attribute.namespaceURI
            }
        }

        return null
    }

    fun getAttributeLocalName(index: Int): String? {
        if (element != null) {
            val attribute = element!!.attribute(index)

            if (attribute != null) {
                return attribute.name
            }
        }

        return null
    }

    fun getAttributePrefix(index: Int): String? {
        if (element != null) {
            val attribute = element!!.attribute(index)

            if (attribute != null) {
                val prefix = attribute.namespacePrefix

                if ((prefix != null) && (prefix.length > 0)) {
                    return prefix
                }
            }
        }

        return null
    }

    fun getAttributeRawName(index: Int): String? {
        if (element != null) {
            val attribute = element!!.attribute(index)

            if (attribute != null) {
                return attribute.qualifiedName
            }
        }

        return null
    }

    fun getAttributeValue(index: Int): String? {
        if (element != null) {
            val attribute = element!!.attribute(index)

            if (attribute != null) {
                return attribute.value
            }
        }

        return null
    }

    fun getAttributeValueFromRawName(rawName: String): String? {
        if (element != null) {
            val iter = element!!.attributeIterator()!!
            while (iter.hasNext()) {
                val attribute = iter.next() as Attribute

                if (rawName == attribute.qualifiedName) {
                    return attribute.value
                }
            }
        }

        return null
    }

    fun getAttributeValueFromName(namespaceURI: String, localName: String): String? {
        if (element != null) {
            val iter = element!!.attributeIterator()!!
            while (iter.hasNext()) {
                val attribute = iter.next() as Attribute

                if (namespaceURI == attribute.namespaceURI
                    && localName == attribute.name
                ) {
                    return attribute.value
                }
            }
        }

        return null
    }

    fun isAttributeNamespaceDeclaration(index: Int): Boolean {
        if (element != null) {
            val attribute = element!!.attribute(index)

            if (attribute != null) {
                return "xmlns" == attribute.namespacePrefix
            }
        }

        return false
    }

    val localName: String?
        /*
             * parameters modeled after SAX2 attribute approach
             * 
             * @param namespaceURI DOCUMENT ME!
             * @param localName DOCUMENT ME!
             * @param rawName DOCUMENT ME!
             * @param value DOCUMENT ME!
             * 
             * @throws XmlPullParserException DOCUMENT ME!
             * /
            public void addAttribute(String namespaceURI, String localName, String rawName, String value)
                throws XmlPullParserException
            {
                QName qname = QName.get(rawName, namespaceURI);
                element.addAttribute(qname, value);
            }
        
            public void addAttribute(String namespaceURI, String localName, String rawName, String value,
                boolean isNamespaceDeclaration) throws XmlPullParserException
            {
                if (isNamespaceDeclaration)
                {
                    String prefix = "";
                    int idx = rawName.indexOf(':');
        
                    if (idx > 0)
                    {
                        prefix = rawName.substring(0, idx);
                    }
        
                    element.addNamespace(prefix, namespaceURI);
                }
                else
                {
                    QName qname = QName.get(rawName, namespaceURI);
                    element.addAttribute(qname, value);
                }
            }
        
            public void ensureAttributesCapacity(int minCapacity) throws XmlPullParserException
            {
                if (element instanceof AbstractElement)
                {
                    AbstractElement elementImpl = (AbstractElement)element;
                    elementImpl.ensureAttributesCapacity(minCapacity);
                }
            }
        
            / **
             * Remove all atributes.
             * 
             * @deprecated Use {@link #removeAttributes()} instead.
             */
        get() = element!!.name

    val namespaceUri: String?
        get() = element!!.namespaceURI

    val prefix: String?
        get() = element!!.namespacePrefix

    val rawName: String?
        get() = element!!.qualifiedName

    fun modifyTag(namespaceURI: String?, lName: String?, rawName: String?) {
        this.element = documentFactory.createElement(rawName!!, namespaceURI)
    }

    fun resetTag() {
        this.element = null
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


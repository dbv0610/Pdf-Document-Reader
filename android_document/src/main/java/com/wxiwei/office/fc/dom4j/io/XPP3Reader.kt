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
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.DocumentException
import com.wxiwei.office.fc.dom4j.DocumentFactory
import com.wxiwei.office.fc.dom4j.ElementHandler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

/*import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;*/
/**
 * 
 * 
 * `XPP3Reader` is a Reader of DOM4J documents that uses the fast
 * [XML Pull Parser 3.x ](http://www.extreme.indiana.edu/soap/xpp/).
 * It is very fast for use in SOAP style environments.
 * 
 * 
 * @author [Pelle Braendgaard ](mailto:pelle@neubia.com)
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.3 $
 */
class XPP3Reader {
    /** `DocumentFactory` used to create new document objects  */
    private var factory: DocumentFactory? = null

    /** `XmlPullParser` used to parse XML  */ //private XmlPullParser xppParser;
    /** `XmlPullParser` used to parse XML  */ // private XmlPullParserFactory xppFactory;
    /** DispatchHandler to call when each `Element` is encountered  */
    protected var dispatchHandler: DispatchHandler? = null
        /*protected Document parseDocument() throws DocumentException, IOException,
             XmlPullParserException
         {
             DocumentFactory df = getDocumentFactory();
             Document document = df.createDocument();
             Element parent = null;
             XmlPullParser pp = getXPPParser();
             pp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
     
             while (true)
             {
                 int type = pp.nextToken();
     
                 switch (type)
                 {
                     case XmlPullParser.PROCESSING_INSTRUCTION:
                     {
                         String text = pp.getText();
                         int loc = text.indexOf(" ");
     
                         if (loc >= 0)
                         {
                             String target = text.substring(0, loc);
                             String txt = text.substring(loc + 1);
                             document.addProcessingInstruction(target, txt);
                         }
                         else
                         {
                             document.addProcessingInstruction(text, "");
                         }
     
                         break;
                     }
     
                     case XmlPullParser.COMMENT:
                     {
                         if (parent != null)
                         {
                             parent.addComment(pp.getText());
                         }
                         else
                         {
                             document.addComment(pp.getText());
                         }
     
                         break;
                     }
     
                     case XmlPullParser.CDSECT:
                     {
                         if (parent != null)
                         {
                             parent.addCDATA(pp.getText());
                         }
                         else
                         {
                             String msg = "Cannot have text content outside of the " + "root document";
                             throw new DocumentException(msg);
                         }
     
                         break;
                     }
     
                     case XmlPullParser.ENTITY_REF:
                         break;
     
                     case XmlPullParser.END_DOCUMENT:
                         return document;
     
                     case XmlPullParser.START_TAG:
                     {
                         QName qname = (pp.getPrefix() == null) ? df.createQName(pp.getName(),
                             pp.getNamespace()) : df.createQName(pp.getName(), pp.getPrefix(),
                             pp.getNamespace());
                         Element newElement = df.createElement(qname);
                         int nsStart = pp.getNamespaceCount(pp.getDepth() - 1);
                         int nsEnd = pp.getNamespaceCount(pp.getDepth());
     
                         for (int i = nsStart; i < nsEnd; i++)
                         {
                             if (pp.getNamespacePrefix(i) != null)
                             {
                                 newElement
                                     .addNamespace(pp.getNamespacePrefix(i), pp.getNamespaceUri(i));
                             }
                         }
     
                         for (int i = 0; i < pp.getAttributeCount(); i++)
                         {
                             QName qa = (pp.getAttributePrefix(i) == null) ? df.createQName(pp
                                 .getAttributeName(i)) : df.createQName(pp.getAttributeName(i),
                                 pp.getAttributePrefix(i), pp.getAttributeNamespace(i));
                             newElement.addAttribute(qa, pp.getAttributeValue(i));
                         }
     
                         if (parent != null)
                         {
                             parent.add(newElement);
                         }
                         else
                         {
                             document.add(newElement);
                         }
     
                         parent = newElement;
     
                         break;
                     }
     
                     case XmlPullParser.END_TAG:
                     {
                         if (parent != null)
                         {
                             parent = parent.getParent();
                         }
     
                         break;
                     }
     
                     case XmlPullParser.TEXT:
                     {
                         String text = pp.getText();
     
                         if (parent != null)
                         {
                             parent.addText(text);
                         }
                         else
                         {
                             String msg = "Cannot have text content outside of the " + "root document";
                             throw new DocumentException(msg);
                         }
     
                         break;
                     }
     
                     default:
                         break;
                 }
             }
         }*/
        get() {
            if (field == null) {
                field = DispatchHandler()
            }

            return field
        }

    constructor()

    constructor(factory: DocumentFactory?) {
        this.factory = factory
    }

    var documentFactory: DocumentFactory?
        /**
         * 
         * 
         * Reads a Document from the given `File`
         * 
         * 
         * @param file
         * is the `File` to read from.
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * if a URL could not be made for the given File
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(File file) throws DocumentException, IOException, XmlPullParserException
         * {
         * String systemID = file.getAbsolutePath();
         * 
         * return read(new BufferedReader(new FileReader(file)), systemID);
         * }
         * 
         * / **
         * 
         * 
         * Reads a Document from the given `URL`
         * 
         * 
         * @param url
         * `URL` to read from.
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * DOCUMENT ME!
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(URL url) throws DocumentException, IOException, XmlPullParserException
         * {
         * String systemID = url.toExternalForm();
         * 
         * return read(createReader(url.openStream()), systemID);
         * }
         * 
         * / **
         * 
         * 
         * Reads a Document from the given URL or filename.
         * 
         * 
         * 
         * 
         * If the systemID contains a `':'` character then it is
         * assumed to be a URL otherwise its assumed to be a file name. If you want
         * finer grained control over this mechansim then please explicitly pass in
         * either a [URL]or a [File]instance instead of a [ ] to denote the source of the document.
         * 
         * 
         * @param systemID
         * is a URL for a document or a file name.
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * if a URL could not be made for the given File
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(String systemID) throws DocumentException, IOException,
         * XmlPullParserException
         * {
         * if (systemID.indexOf(':') >= 0)
         * {
         * // lets assume its a URL
         * return read(new URL(systemID));
         * }
         * else
         * {
         * // lets assume that we are given a file name
         * return read(new File(systemID));
         * }
         * }
         * 
         * / **
         * 
         * 
         * Reads a Document from the given stream
         * 
         * 
         * @param in
         * `InputStream` to read from.
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * DOCUMENT ME!
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(InputStream in) throws DocumentException, IOException,
         * XmlPullParserException
         * {
         * return read(createReader(in));
         * }
         * 
         * / **
         * 
         * 
         * Reads a Document from the given `Reader`
         * 
         * 
         * @param reader
         * is the reader for the input
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * DOCUMENT ME!
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(Reader reader) throws DocumentException, IOException,
         * XmlPullParserException
         * {
         * getXPPParser().setInput(reader);
         * 
         * return parseDocument();
         * }
         * 
         * / **
         * 
         * 
         * Reads a Document from the given array of characters
         * 
         * 
         * @param text
         * is the text to parse
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * DOCUMENT ME!
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(char[] text) throws DocumentException, IOException, XmlPullParserException
         * {
         * getXPPParser().setInput(new CharArrayReader(text));
         * 
         * return parseDocument();
         * }
         * 
         * / **
         * 
         * 
         * Reads a Document from the given stream
         * 
         * 
         * @param in
         * `InputStream` to read from.
         * @param systemID
         * is the URI for the input
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * DOCUMENT ME!
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(InputStream in, String systemID) throws DocumentException, IOException,
         * XmlPullParserException
         * {
         * return read(createReader(in), systemID);
         * }
         * 
         * / **
         * 
         * 
         * Reads a Document from the given `Reader`
         * 
         * 
         * @param reader
         * is the reader for the input
         * @param systemID
         * is the URI for the input
         * 
         * @return the newly created Document instance
         * 
         * @throws DocumentException
         * if an error occurs during parsing.
         * @throws IOException
         * DOCUMENT ME!
         * @throws XmlPullParserException
         * DOCUMENT ME!
         * /
         * public Document read(Reader reader, String systemID) throws DocumentException, IOException,
         * XmlPullParserException
         * {
         * Document document = read(reader);
         * document.setName(systemID);
         * 
         * return document;
         * }
         * 
         * // Properties
         * // -------------------------------------------------------------------------
         * public XmlPullParser getXPPParser() throws XmlPullParserException
         * {
         * if (xppParser == null)
         * {
         * xppParser = getXPPFactory().newPullParser();
         * }
         * 
         * return xppParser;
         * }
         * 
         * public XmlPullParserFactory getXPPFactory() throws XmlPullParserException
         * {
         * if (xppFactory == null)
         * {
         * xppFactory = XmlPullParserFactory.newInstance();
         * }
         * 
         * xppFactory.setNamespaceAware(true);
         * 
         * return xppFactory;
         * }
         * 
         * public void setXPPFactory(XmlPullParserFactory xPPfactory)
         * {
         * this.xppFactory = xPPfactory;
         * }
         * 
         * / **
         * DOCUMENT ME!
         * 
         * @return the `DocumentFactory` used to create document
         * objects
         */
        get() {
            if (factory == null) {
                factory = DocumentFactory.Companion.getInstance()
            }

            return factory
        }
        /**
         * 
         * 
         * This sets the `DocumentFactory` used to create new
         * documents. This method allows the building of custom DOM4J tree objects
         * to be implemented easily using a custom derivation of
         * [DocumentFactory]
         * 
         * 
         * @param documentFactory
         * `DocumentFactory` used to create DOM4J objects
         */
        set(documentFactory) {
            this.factory = documentFactory
        }

    /**
     * Adds the `ElementHandler` to be called when the specified
     * path is encounted.
     * 
     * @param path
     * is the path to be handled
     * @param handler
     * is the `ElementHandler` to be called by the event
     * based processor.
     */
    fun addHandler(path: String?, handler: ElementHandler?) {
        this.dispatchHandler!!.addHandler(path, handler)
    }

    /**
     * Removes the `ElementHandler` from the event based processor,
     * for the specified path.
     * 
     * @param path
     * is the path to remove the `ElementHandler` for.
     */
    fun removeHandler(path: String?) {
        this.dispatchHandler!!.removeHandler(path)
    }

    /**
     * When multiple `ElementHandler` instances have been
     * registered, this will set a default `ElementHandler` to be
     * called for any path which does **NOT ** have a handler registered.
     * 
     * @param handler
     * is the `ElementHandler` to be called by the event
     * based processor.
     */
    fun setDefaultHandler(handler: ElementHandler?) {
        this.dispatchHandler!!.setDefaultHandler(handler)
    }

    /**
     * Factory method to create a Reader from the given InputStream.
     * 
     * @param in
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    protected fun createReader(`in`: InputStream?): Reader {
        return BufferedReader(InputStreamReader(`in`))
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


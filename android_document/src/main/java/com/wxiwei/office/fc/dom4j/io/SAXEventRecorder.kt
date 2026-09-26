/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.DTDHandler
import org.xml.sax.SAXException
import org.xml.sax.ext.DeclHandler
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.AttributesImpl
import org.xml.sax.helpers.DefaultHandler
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput

/**
 * 
 * 
 * Records SAX events such that they may be "replayed" at a later time. Provides
 * an alternative serialization approach when externalizing a DOM4J document.
 * Rather than serializing a document as text and re-parsing, the sax events may
 * be serialized instead.
 * 
 * Example usage:
 * 
 * <pre>
 * 
 * 
 * 
 * SAXEventRecorder recorder = new SAXEventRecorder();
 * SAXWriter saxWriter = new SAXWriter(recorder, recorder);
 * saxWriter.write(document);
 * out.writeObject(recorder);
 * ...
 * SAXEventRecorder recorder = (SAXEventRecorder)in.readObject();
 * SAXContentHandler saxContentHandler = new SAXContentHandler();
 * recorder.replay(saxContentHandler);
 * Document document = saxContentHandler.getDocument();
 * 
 * 
 * 
</pre> * 
 * 
 * @author Todd Wolff (Bluestem Software)
 */
class SAXEventRecorder : DefaultHandler(), LexicalHandler, DeclHandler, DTDHandler, Externalizable {
    private var events: MutableList<Any?>? = ArrayList<Any?>()

    private val prefixMappings: MutableMap<Any?, Any?> = HashMap<Any?, Any?>()

    @Throws(SAXException::class)
    fun replay(handler: ContentHandler) {
        var saxEvent: SAXEvent
        val itr = events!!.iterator()

        while (itr.hasNext()) {
            saxEvent = itr.next() as SAXEvent

            when (saxEvent.event) {
                SAXEvent.PROCESSING_INSTRUCTION -> handler.processingInstruction(
                    saxEvent.getParm(0) as String?,
                    saxEvent.getParm(1) as String?
                )

                SAXEvent.START_PREFIX_MAPPING -> handler.startPrefixMapping(
                    saxEvent.getParm(0) as String?,
                    saxEvent.getParm(1) as String?
                )

                SAXEvent.END_PREFIX_MAPPING -> handler.endPrefixMapping(saxEvent.getParm(0) as String?)

                SAXEvent.START_DOCUMENT -> handler.startDocument()

                SAXEvent.END_DOCUMENT -> handler.endDocument()

                SAXEvent.START_ELEMENT -> {
                    val attributes = AttributesImpl()
                    val attParmList = saxEvent.getParm(3) as MutableList<Any?>?

                    if (attParmList != null) {
                        val attsItr: MutableIterator<*> = attParmList.iterator()

                        while (attsItr.hasNext()) {
                            val attParms = attsItr.next() as Array<String?>
                            attributes.addAttribute(
                                attParms[0], attParms[1], attParms[2],
                                attParms[3], attParms[4]
                            )
                        }
                    }

                    handler.startElement(
                        saxEvent.getParm(0) as String?, saxEvent.getParm(1) as String?,
                        saxEvent.getParm(2) as String?, attributes
                    )
                }

                SAXEvent.END_ELEMENT -> handler.endElement(
                    saxEvent.getParm(0) as String?, saxEvent.getParm(1) as String?,
                    saxEvent.getParm(2) as String?
                )

                SAXEvent.CHARACTERS -> {
                    val chars = saxEvent.getParm(0) as CharArray?
                    val start = (saxEvent.getParm(1) as Int)
                    val end = (saxEvent.getParm(2) as Int)
                    handler.characters(chars, start, end)
                }

                SAXEvent.START_DTD -> (handler as LexicalHandler).startDTD(
                    saxEvent.getParm(0) as String?,
                    saxEvent.getParm(1) as String?, saxEvent.getParm(2) as String?
                )

                SAXEvent.END_DTD -> (handler as LexicalHandler).endDTD()

                SAXEvent.START_ENTITY -> (handler as LexicalHandler).startEntity(saxEvent.getParm(0) as String?)

                SAXEvent.END_ENTITY -> (handler as LexicalHandler).endEntity(saxEvent.getParm(0) as String?)

                SAXEvent.START_CDATA -> (handler as LexicalHandler).startCDATA()

                SAXEvent.END_CDATA -> (handler as LexicalHandler).endCDATA()

                SAXEvent.COMMENT -> {
                    val cchars = saxEvent.getParm(0) as CharArray?
                    val cstart = (saxEvent.getParm(1) as Int)
                    val cend = (saxEvent.getParm(2) as Int)
                    (handler as LexicalHandler).comment(cchars, cstart, cend)
                }

                SAXEvent.ELEMENT_DECL -> (handler as DeclHandler).elementDecl(
                    saxEvent.getParm(0) as String?,
                    saxEvent.getParm(1) as String?
                )

                SAXEvent.ATTRIBUTE_DECL -> (handler as DeclHandler).attributeDecl(
                    saxEvent.getParm(0) as String?,
                    saxEvent.getParm(1) as String?, saxEvent.getParm(2) as String?,
                    saxEvent.getParm(3) as String?, saxEvent.getParm(4) as String?
                )

                SAXEvent.INTERNAL_ENTITY_DECL -> (handler as DeclHandler).internalEntityDecl(
                    saxEvent.getParm(0) as String?,
                    saxEvent.getParm(1) as String?
                )

                SAXEvent.EXTERNAL_ENTITY_DECL -> (handler as DeclHandler).externalEntityDecl(
                    saxEvent.getParm(0) as String?,
                    saxEvent.getParm(1) as String?, saxEvent.getParm(2) as String?
                )

                else -> throw SAXException("Unrecognized event: " + saxEvent.event)
            }
        }
    }

    // ContentHandler interface
    // -------------------------------------------------------------------------
    @Throws(SAXException::class)
    override fun processingInstruction(target: String?, data: String?) {
        val saxEvent = SAXEvent(SAXEvent.PROCESSING_INSTRUCTION)
        saxEvent.addParm(target)
        saxEvent.addParm(data)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun startPrefixMapping(prefix: String?, uri: String?) {
        val saxEvent = SAXEvent(SAXEvent.START_PREFIX_MAPPING)
        saxEvent.addParm(prefix)
        saxEvent.addParm(uri)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun endPrefixMapping(prefix: String?) {
        val saxEvent = SAXEvent(SAXEvent.END_PREFIX_MAPPING)
        saxEvent.addParm(prefix)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun startDocument() {
        val saxEvent = SAXEvent(SAXEvent.START_DOCUMENT)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        val saxEvent = SAXEvent(SAXEvent.END_DOCUMENT)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun startElement(
        namespaceURI: String?, localName: String?, qualifiedName: String?,
        attributes: Attributes?
    ) {
        val saxEvent = SAXEvent(SAXEvent.START_ELEMENT)
        saxEvent.addParm(namespaceURI)
        saxEvent.addParm(localName)
        saxEvent.addParm(qualifiedName)

        var qName: QName? = null
        if (namespaceURI != null) {
            qName = QName(localName, Namespace.Companion.get(namespaceURI))
        } else {
            qName = QName(localName)
        }

        if ((attributes != null) && (attributes.getLength() > 0)) {
            val attParmList: MutableList<Any?> = ArrayList<Any?>(attributes.getLength())
            var attParms: Array<String?>? = null

            for (i in 0..<attributes.getLength()) {
                val attLocalName = attributes.getLocalName(i)

                if (attLocalName.startsWith(XMLNS)) {
                    // if SAXWriter is writing a DOMDocument, namespace
                    // decls are treated as attributes. record a start
                    // prefix mapping event

                    var prefix: String? = null
                    if (attLocalName.length > 5) {
                        prefix = attLocalName.substring(6)
                    } else {
                        prefix = EMPTY_STRING
                    }

                    val prefixEvent = SAXEvent(SAXEvent.START_PREFIX_MAPPING)
                    prefixEvent.addParm(prefix)
                    prefixEvent.addParm(attributes.getValue(i))
                    events!!.add(prefixEvent)

                    // 'register' the prefix so that we can generate
                    // an end prefix mapping event within endElement
                    var prefixes = prefixMappings.get(qName) as MutableList<Any?>?
                    if (prefixes == null) {
                        prefixes = ArrayList<Any?>()
                        prefixMappings.put(qName, prefixes)
                    }
                    prefixes.add(prefix)
                } else {
                    attParms = arrayOfNulls<String>(5)
                    attParms[0] = attributes.getURI(i)
                    attParms[1] = attLocalName
                    attParms[2] = attributes.getQName(i)
                    attParms[3] = attributes.getType(i)
                    attParms[4] = attributes.getValue(i)
                    attParmList.add(attParms)
                }
            }

            saxEvent.addParm(attParmList)
        }

        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun endElement(namespaceURI: String?, localName: String?, qName: String?) {
        val saxEvent = SAXEvent(SAXEvent.END_ELEMENT)
        saxEvent.addParm(namespaceURI)
        saxEvent.addParm(localName)
        saxEvent.addParm(qName)
        events!!.add(saxEvent)

        // check to see if a we issued a start prefix mapping event
        // for DOMDocument namespace decls
        var elementName: QName? = null
        if (namespaceURI != null) {
            elementName = QName(localName, Namespace.Companion.get(namespaceURI))
        } else {
            elementName = QName(localName)
        }

        val prefixes = prefixMappings.get(elementName) as MutableList<Any?>?
        if (prefixes != null) {
            val itr: MutableIterator<*> = prefixes.iterator()
            while (itr.hasNext()) {
                val prefixEvent = SAXEvent(SAXEvent.END_PREFIX_MAPPING)
                prefixEvent.addParm(itr.next())
                events!!.add(prefixEvent)
            }
        }
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray?, start: Int, end: Int) {
        val saxEvent = SAXEvent(SAXEvent.CHARACTERS)
        saxEvent.addParm(ch)
        saxEvent.addParm(start)
        saxEvent.addParm(end)
        events!!.add(saxEvent)
    }

    // LexicalHandler interface
    // -------------------------------------------------------------------------
    @Throws(SAXException::class)
    override fun startDTD(name: String?, publicId: String?, systemId: String?) {
        val saxEvent = SAXEvent(SAXEvent.START_DTD)
        saxEvent.addParm(name)
        saxEvent.addParm(publicId)
        saxEvent.addParm(systemId)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun endDTD() {
        val saxEvent = SAXEvent(SAXEvent.END_DTD)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun startEntity(name: String?) {
        val saxEvent = SAXEvent(SAXEvent.START_ENTITY)
        saxEvent.addParm(name)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun endEntity(name: String?) {
        val saxEvent = SAXEvent(SAXEvent.END_ENTITY)
        saxEvent.addParm(name)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun startCDATA() {
        val saxEvent = SAXEvent(SAXEvent.START_CDATA)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun endCDATA() {
        val saxEvent = SAXEvent(SAXEvent.END_CDATA)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun comment(ch: CharArray?, start: Int, end: Int) {
        val saxEvent = SAXEvent(SAXEvent.COMMENT)
        saxEvent.addParm(ch)
        saxEvent.addParm(start)
        saxEvent.addParm(end)
        events!!.add(saxEvent)
    }

    // DeclHandler interface
    // -------------------------------------------------------------------------
    @Throws(SAXException::class)
    override fun elementDecl(name: String?, model: String?) {
        val saxEvent = SAXEvent(SAXEvent.ELEMENT_DECL)
        saxEvent.addParm(name)
        saxEvent.addParm(model)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun attributeDecl(
        eName: String?, aName: String?, type: String?, valueDefault: String?,
        value: String?
    ) {
        val saxEvent = SAXEvent(SAXEvent.ATTRIBUTE_DECL)
        saxEvent.addParm(eName)
        saxEvent.addParm(aName)
        saxEvent.addParm(type)
        saxEvent.addParm(valueDefault)
        saxEvent.addParm(value)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun internalEntityDecl(name: String?, value: String?) {
        val saxEvent = SAXEvent(SAXEvent.INTERNAL_ENTITY_DECL)
        saxEvent.addParm(name)
        saxEvent.addParm(value)
        events!!.add(saxEvent)
    }

    @Throws(SAXException::class)
    override fun externalEntityDecl(name: String?, publicId: String?, sysId: String?) {
        val saxEvent = SAXEvent(SAXEvent.EXTERNAL_ENTITY_DECL)
        saxEvent.addParm(name)
        saxEvent.addParm(publicId)
        saxEvent.addParm(sysId)
        events!!.add(saxEvent)
    }

    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput) {
        if (events == null) {
            out.writeByte(NULL.toInt())
        } else {
            out.writeByte(OBJECT.toInt())
            out.writeObject(events)
        }
    }

    @Throws(ClassNotFoundException::class, IOException::class)
    override fun readExternal(`in`: ObjectInput) {
        if (`in`.readByte() != NULL) {
            events = `in`.readObject() as MutableList<Any?>?
        }
    }

    // SAXEvent inner class
    // -------------------------------------------------------------------------
    internal class SAXEvent : Externalizable {
        var event: Byte = 0

        protected var parms: MutableList<Any?>? = null

        constructor()

        constructor(event: Byte) {
            this.event = event
        }

        fun addParm(parm: Any?) {
            if (parms == null) {
                parms = ArrayList<Any?>(3)
            }

            parms!!.add(parm)
        }

        fun getParm(index: Int): Any? {
            if ((parms != null) && (index < parms!!.size)) {
                return parms!!.get(index)
            } else {
                return null
            }
        }

        @Throws(IOException::class)
        override fun writeExternal(out: ObjectOutput) {
            out.writeByte(event.toInt())

            if (parms == null) {
                out.writeByte(NULL.toInt())
            } else {
                out.writeByte(OBJECT.toInt())
                out.writeObject(parms)
            }
        }

        @Throws(ClassNotFoundException::class, IOException::class)
        override fun readExternal(`in`: ObjectInput) {
            event = `in`.readByte()

            if (`in`.readByte() != NULL) {
                parms = `in`.readObject() as MutableList<Any?>?
            }
        }

        companion object {
            const val serialVersionUID: Long = 1

            const val PROCESSING_INSTRUCTION: Byte = 1

            const val START_PREFIX_MAPPING: Byte = 2

            const val END_PREFIX_MAPPING: Byte = 3

            const val START_DOCUMENT: Byte = 4

            const val END_DOCUMENT: Byte = 5

            const val START_ELEMENT: Byte = 6

            const val END_ELEMENT: Byte = 7

            const val CHARACTERS: Byte = 8

            const val START_DTD: Byte = 9

            const val END_DTD: Byte = 10

            const val START_ENTITY: Byte = 11

            const val END_ENTITY: Byte = 12

            const val START_CDATA: Byte = 13

            const val END_CDATA: Byte = 14

            const val COMMENT: Byte = 15

            const val ELEMENT_DECL: Byte = 16

            const val ATTRIBUTE_DECL: Byte = 17

            const val INTERNAL_ENTITY_DECL: Byte = 18

            const val EXTERNAL_ENTITY_DECL: Byte = 19
        }
    }

    companion object {
        const val serialVersionUID: Long = 1

        private const val STRING: Byte = 0

        private const val OBJECT: Byte = 1

        private const val NULL: Byte = 2

        private const val XMLNS = "xmlns"

        private const val EMPTY_STRING = ""
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


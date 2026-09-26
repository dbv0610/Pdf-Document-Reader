/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hpsf

import com.wxiwei.office.fc.POIDocument
import com.wxiwei.office.fc.POITextExtractor
import com.wxiwei.office.fc.poifs.filesystem.NPOIFSFileSystem
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import com.wxiwei.office.fc.util.LittleEndian.getUInt
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * Extracts all of the HPSF properties, both
 * build in and custom, returning them in
 * textual form.
 */
class HPSFPropertiesExtractor : POITextExtractor {
    constructor(mainExtractor: POITextExtractor) : super(mainExtractor)
    constructor(doc: POIDocument?) : super(doc)
    constructor(fs: POIFSFileSystem) : super(PropertiesOnlyDocument(fs))
    constructor(fs: NPOIFSFileSystem) : super(PropertiesOnlyDocument(fs))

    val documentSummaryInformationText: String
        get() {
            val dsi = document.getDocumentSummaryInformation()
            val text = StringBuffer()

            // Normal properties
            text.append(getPropertiesText(dsi))

            // Now custom ones
            val cps = if (dsi == null) null else dsi.customProperties
            if (cps != null) {
                val keys =
                    cps.nameSet().iterator()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val `val`: String =
                        getPropertyValueText(cps.get(key))
                    text.append(key + " = " + `val` + "\n")
                }
            }

            // All done
            return text.toString()
        }
    val summaryInformationText: String
        get() {
            val si = document.getSummaryInformation()

            // Just normal properties
            return getPropertiesText(si)
        }

    /**
     * @return the text of all the properties defined in
     * the document.
     */
    override fun getText(): String {
        return this.summaryInformationText + this.documentSummaryInformationText
    }

    /**
     * Prevent recursion!
     */
    override fun getMetadataTextExtractor(): POITextExtractor? {
        throw IllegalStateException("You already have the Metadata Text Extractor, not recursing!")
    }

    /**
     * So we can get at the properties of any
     * random OLE2 document.
     */
    private class PropertiesOnlyDocument : POIDocument {
        constructor(fs: NPOIFSFileSystem) : super(fs.getRoot())
        constructor(fs: POIFSFileSystem) : super(fs)

        override fun write(out: OutputStream?) {
            throw IllegalStateException("Unable to write, only for properties!")
        }
    }

    companion object {
        private fun getPropertiesText(ps: SpecialPropertySet?): String {
            if (ps == null) {
                // Not defined, oh well
                return ""
            }

            val text = StringBuffer()

            val idMap = ps.getPropertySetIDMap()
            val props = ps.getProperties()!!
            for (i in props.indices) {
                var type = props[i].getID().toString()
                val typeObj = idMap!!.get(props[i].getID())
                if (typeObj != null) {
                    type = typeObj.toString()
                }

                val `val`: String = getPropertyValueText(props[i].getValue())
                text.append(type + " = " + `val` + "\n")
            }

            return text.toString()
        }

        private fun getPropertyValueText(`val`: Any?): String {
            if (`val` == null) {
                return "(not set)"
            }
            if (`val` is ByteArray) {
                val b = `val`
                if (b.size == 0) {
                    return ""
                }
                if (b.size == 1) {
                    return b[0].toString()
                }
                if (b.size == 2) {
                    return getUShort(b).toString()
                }
                if (b.size == 4) {
                    return getUInt(b).toString()
                }
                // Maybe it's a string? who knows!
                return String(b)
            }
            return `val`.toString()
        }

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            for (file in args) {
                val ext = HPSFPropertiesExtractor(
                    NPOIFSFileSystem(File(file))
                )
                println(ext.getText())
            }
        }
    }
}

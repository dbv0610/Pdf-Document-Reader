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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.hslf.record.ExObjList
import com.wxiwei.office.fc.hslf.record.InteractiveInfo
import com.wxiwei.office.fc.hslf.record.InteractiveInfoAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.Record.Companion.findChildRecords
import com.wxiwei.office.fc.hslf.record.TxInteractiveInfoAtom

/**
 * Represents a hyperlink in a PowerPoint document
 * 
 * @author Yegor Kozlov
 */
class Hyperlink {
    var id: Int = -1
    private var type = 0
    private var address: String? = null
    private var title: String? = null

    /**
     * Gets the beginning character position
     * 
     * @return the beginning character position
     */
    var startIndex: Int = 0
        private set

    /**
     * Gets the ending character position
     * 
     * @return the ending character position
     */
    var endIndex: Int = 0
        private set

    /**
     * Gets the type of the hyperlink action.
     * Must be a `LINK_*`  constant
     * 
     * @return the hyperlink URL
     * @see InteractiveInfoAtom
     */
    fun getType(): Int {
        return type
    }

    fun setType(`val`: Int) {
        type = `val`
        when (type) {
            LINK_NEXTSLIDE.toInt() -> {
                title = "NEXT"
                address = "1,-1,NEXT"
            }

            LINK_PREVIOUSSLIDE.toInt() -> {
                title = "PREV"
                address = "1,-1,PREV"
            }

            LINK_FIRSTSLIDE.toInt() -> {
                title = "FIRST"
                address = "1,-1,FIRST"
            }

            LINK_LASTSLIDE.toInt() -> {
                title = "LAST"
                address = "1,-1,LAST"
            }

            else -> {
                title = ""
                address = ""
            }
        }
    }

    /**
     * Gets the hyperlink URL
     * 
     * @return the hyperlink URL
     */
    fun getAddress(): String? {
        return address
    }

    fun setAddress(str: String?) {
        address = str
    }

    /**
     * Gets the hyperlink user-friendly title (if different from URL)
     * 
     * @return the  hyperlink user-friendly title
     */
    fun getTitle(): String? {
        return title
    }

    fun setTitle(str: String?) {
        title = str
    }

    companion object {
        val LINK_NEXTSLIDE: Byte = InteractiveInfoAtom.LINK_NextSlide
        val LINK_PREVIOUSSLIDE: Byte = InteractiveInfoAtom.LINK_PreviousSlide
        val LINK_FIRSTSLIDE: Byte = InteractiveInfoAtom.LINK_FirstSlide
        val LINK_LASTSLIDE: Byte = InteractiveInfoAtom.LINK_LastSlide
        val LINK_URL: Byte = InteractiveInfoAtom.LINK_Url
        val LINK_NULL: Byte = InteractiveInfoAtom.LINK_NULL

        /**
         * Find hyperlinks in a text run
         * 
         * @param run  `TextRun` to lookup hyperlinks in
         * @return found hyperlinks or `null` if not found
         */
        fun find(run: TextRun): Array<Hyperlink?>? {
            val lst = ArrayList<Hyperlink>()
            val ppt = run.sheet!!.slideShow!!
            //document-level container which stores info about all links in a presentation
            val exobj = ppt.documentRecord!!.exObjList
            if (exobj == null) {
                return null
            }
            val records = run.records
            if (records != null) find(records, exobj, lst)

            var links: Array<Hyperlink?>? = null
            if (lst.size > 0) {
                links = arrayOfNulls<Hyperlink>(lst.size)
                lst.toArray(links)
            }
            return links
        }

        /**
         * Find hyperlink assigned to the supplied shape
         * 
         * @param shape  `Shape` to lookup hyperlink in
         * @return found hyperlink or `null`
         */
        fun find(shape: Shape): Hyperlink? {
            val lst = ArrayList<Hyperlink>()
            val ppt = shape.sheet!!.slideShow!!
            //document-level container which stores info about all links in a presentation
            val exobj = ppt.documentRecord!!.exObjList
            if (exobj == null) {
                return null
            }

            val spContainer = shape.spContainer
            val it: MutableIterator<EscherRecord?> = spContainer!!.childIterator
            while (it.hasNext()) {
                val obj = it.next()
                if (obj!!.recordId == EscherClientDataRecord.RECORD_ID) {
                    val data = obj.serialize()
                    val records: Array<Record> = findChildRecords(data, 8, data.size - 8)
                    if (records != null) find(records, exobj, lst)
                }
            }

            return if (lst.size == 1) lst.get(0) else null
        }

        private fun find(records: Array<out Record?>, exobj: ExObjList, out: MutableList<Hyperlink>) {
            var i = 0
            while (i < records.size) {
                //see if we have InteractiveInfo in the textrun's records
                if (records[i] is InteractiveInfo) {
                    val hldr = records[i] as InteractiveInfo
                    val info = hldr.interactiveInfoAtom!!
                    val id = info.hyperlinkID
                    val linkRecord = exobj.get(id)
                    if (linkRecord != null) {
                        val link = Hyperlink()
                        link.title = linkRecord.linkTitle
                        link.address = linkRecord.linkURL
                        link.type = info.action.toInt()

                        if (++i < records.size && records[i] is TxInteractiveInfoAtom) {
                            val txinfo = records[i] as TxInteractiveInfoAtom
                            link.startIndex = txinfo.startIndex
                            link.endIndex = txinfo.endIndex
                        }
                        out.add(link)
                    }
                }
                i++
            }
        }
    }
}

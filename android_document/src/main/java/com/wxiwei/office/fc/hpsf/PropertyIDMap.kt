/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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

import java.util.Collections

/**
 * 
 * This is a dictionary which maps property ID values to property
 * ID strings.
 * 
 * 
 * The methods [.getSummaryInformationProperties] and [ ][.getDocumentSummaryInformationProperties] return singleton [ ]s. An application that wants to extend these maps
 * should treat them as unmodifiable, copy them and modifiy the
 * copies.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
class PropertyIDMap : HashMap<Any?, Any?> {
    /**
     * 
     * Creates a [PropertyIDMap].
     * 
     * @param initialCapacity The initial capacity as defined for
     * [HashMap]
     * @param loadFactor The load factor as defined for [HashMap]
     */
    constructor(initialCapacity: Int, loadFactor: Float) : super(initialCapacity, loadFactor)


    /**
     * 
     * Creates a [PropertyIDMap] backed by another map.
     * 
     * @param map The instance to be created is backed by this map.
     */
    constructor(map: MutableMap<*, *>) : super(map)


    /**
     * 
     * Puts a ID string for an ID into the [ ].
     * 
     * @param id The ID.
     * @param idString The ID string.
     * @return As specified by the [Map] interface, this method
     * returns the previous value associated with the specified
     * <var>id</var>, or `null` if there was no mapping for
     * key.
     */
    fun put(id: Long, idString: String?): Any? {
        return super.put(id, idString)
    }


    /**
     * 
     * Gets the ID string for an ID from the [ ].
     * 
     * @param id The ID.
     * @return The ID string associated with <var>id</var>.
     */
    fun get(id: Long): Any? {
        return super.get(id)
    }


    companion object {
        /*
     * The following definitions are for property IDs in the first
     * (and only) section of the Summary Information property set.
     */
        /** 
         *
         *ID of the property that denotes the document's title  */
        const val PID_TITLE: Int = 2

        /** 
         *
         *ID of the property that denotes the document's subject  */
        const val PID_SUBJECT: Int = 3

        /** 
         *
         *ID of the property that denotes the document's author  */
        const val PID_AUTHOR: Int = 4

        /** 
         *
         *ID of the property that denotes the document's keywords  */
        const val PID_KEYWORDS: Int = 5

        /** 
         *
         *ID of the property that denotes the document's comments  */
        const val PID_COMMENTS: Int = 6

        /** 
         *
         *ID of the property that denotes the document's template  */
        const val PID_TEMPLATE: Int = 7

        /** 
         *
         *ID of the property that denotes the document's last author  */
        const val PID_LASTAUTHOR: Int = 8

        /** 
         *
         *ID of the property that denotes the document's revision number  */
        const val PID_REVNUMBER: Int = 9

        /** 
         *
         *ID of the property that denotes the document's edit time  */
        const val PID_EDITTIME: Int = 10

        /** 
         *
         *ID of the property that denotes the date and time the document was
         * last printed  */
        const val PID_LASTPRINTED: Int = 11

        /** 
         *
         *ID of the property that denotes the date and time the document was
         * created.  */
        const val PID_CREATE_DTM: Int = 12

        /** 
         *
         *ID of the property that denotes the date and time the document was
         * saved  */
        const val PID_LASTSAVE_DTM: Int = 13

        /** 
         *
         *ID of the property that denotes the number of pages in the
         * document  */
        const val PID_PAGECOUNT: Int = 14

        /** 
         *
         *ID of the property that denotes the number of words in the
         * document  */
        const val PID_WORDCOUNT: Int = 15

        /** 
         *
         *ID of the property that denotes the number of characters in the
         * document  */
        const val PID_CHARCOUNT: Int = 16

        /** 
         *
         *ID of the property that denotes the document's thumbnail  */
        const val PID_THUMBNAIL: Int = 17

        /** 
         *
         *ID of the property that denotes the application that created the
         * document  */
        const val PID_APPNAME: Int = 18

        /** 
         *
         *ID of the property that denotes whether read/write access to the
         * document is allowed or whether is should be opened as read-only. It can
         * have the following values:
         * 
         * <table>
         * <tbody>
         * <tr>
         * <th>
         *
         *Value</th>
         * <th>
         *
         *Description</th>
        </tr> * 
         * <tr>
         * <th>
         *
         *0</th>
         * <th>
         *
         *No restriction</th>
        </tr> * 
         * <tr>
         * <th>
         *
         *2</th>
         * <th>
         *
         *Read-only recommended</th>
        </tr> * 
         * <tr>
         * <th>
         *
         *4</th>
         * <th>
         *
         *Read-only enforced</th>
        </tr> * 
        </tbody> * 
        </table> * 
         */
        const val PID_SECURITY: Int = 19


        /*
     * The following definitions are for property IDs in the first
     * section of the Document Summary Information property set.
     */
        /**
         * 
         * The entry is a dictionary.
         */
        const val PID_DICTIONARY: Int = 0

        /**
         * 
         * The entry denotes a code page.
         */
        const val PID_CODEPAGE: Int = 1

        /**
         * 
         * The entry is a string denoting the category the file belongs
         * to, e.g. review, memo, etc. This is useful to find documents of
         * same type.
         */
        const val PID_CATEGORY: Int = 2

        /**
         * 
         * Target format for power point presentation, e.g. 35mm,
         * printer, video etc.
         */
        const val PID_PRESFORMAT: Int = 3

        /**
         * 
         * Number of bytes.
         */
        const val PID_BYTECOUNT: Int = 4

        /**
         * 
         * Number of lines.
         */
        const val PID_LINECOUNT: Int = 5

        /**
         * 
         * Number of paragraphs.
         */
        const val PID_PARCOUNT: Int = 6

        /**
         * 
         * Number of slides in a power point presentation.
         */
        const val PID_SLIDECOUNT: Int = 7

        /**
         * 
         * Number of slides with notes.
         */
        const val PID_NOTECOUNT: Int = 8

        /**
         * 
         * Number of hidden slides.
         */
        const val PID_HIDDENCOUNT: Int = 9

        /**
         * 
         * Number of multimedia clips, e.g. sound or video.
         */
        const val PID_MMCLIPCOUNT: Int = 10

        /**
         * 
         * This entry is set to -1 when scaling of the thumbnail is
         * desired. Otherwise the thumbnail should be cropped.
         */
        const val PID_SCALE: Int = 11

        /**
         * 
         * This entry denotes an internally used property. It is a
         * vector of variants consisting of pairs of a string (VT_LPSTR)
         * and a number (VT_I4). The string is a heading name, and the
         * number tells how many document parts are under that
         * heading.
         */
        const val PID_HEADINGPAIR: Int = 12

        /**
         * 
         * This entry contains the names of document parts (word: names
         * of the documents in the master document, excel: sheet names,
         * power point: slide titles, binder: document names).
         */
        const val PID_DOCPARTS: Int = 13

        /**
         * 
         * This entry contains the name of the project manager.
         */
        const val PID_MANAGER: Int = 14

        /**
         * 
         * This entry contains the company name.
         */
        const val PID_COMPANY: Int = 15

        /**
         * 
         * If this entry is -1 the links are dirty and should be
         * re-evaluated.
         */
        const val PID_LINKSDIRTY: Int = 16

        /**
         * 
         * The highest well-known property ID. Applications are free to use higher values for custom purposes.
         */
        val PID_MAX: Int = PID_LINKSDIRTY


        /**
         * 
         * Contains the summary information property ID values and
         * associated strings. See the overall HPSF documentation for
         * details!
         */
        var summaryInformationProperties: PropertyIDMap? = null
            /**
             * @return the Summary Information properties singleton
             */
            get() {
                if (field == null) {
                    val m = PropertyIDMap(18, 1.0.toFloat())
                    m.put(PID_TITLE.toLong(), "PID_TITLE")
                    m.put(PID_SUBJECT.toLong(), "PID_SUBJECT")
                    m.put(PID_AUTHOR.toLong(), "PID_AUTHOR")
                    m.put(PID_KEYWORDS.toLong(), "PID_KEYWORDS")
                    m.put(PID_COMMENTS.toLong(), "PID_COMMENTS")
                    m.put(PID_TEMPLATE.toLong(), "PID_TEMPLATE")
                    m.put(PID_LASTAUTHOR.toLong(), "PID_LASTAUTHOR")
                    m.put(PID_REVNUMBER.toLong(), "PID_REVNUMBER")
                    m.put(PID_EDITTIME.toLong(), "PID_EDITTIME")
                    m.put(PID_LASTPRINTED.toLong(), "PID_LASTPRINTED")
                    m.put(PID_CREATE_DTM.toLong(), "PID_CREATE_DTM")
                    m.put(PID_LASTSAVE_DTM.toLong(), "PID_LASTSAVE_DTM")
                    m.put(PID_PAGECOUNT.toLong(), "PID_PAGECOUNT")
                    m.put(PID_WORDCOUNT.toLong(), "PID_WORDCOUNT")
                    m.put(PID_CHARCOUNT.toLong(), "PID_CHARCOUNT")
                    m.put(PID_THUMBNAIL.toLong(), "PID_THUMBNAIL")
                    m.put(PID_APPNAME.toLong(), "PID_APPNAME")
                    m.put(PID_SECURITY.toLong(), "PID_SECURITY")
                    field =
                        PropertyIDMap(
                            Collections.unmodifiableMap<Any?, Any?>(
                                m
                            )
                        )
                }
                return field
            }
            private set

        /**
         * 
         * Contains the summary information property ID values and
         * associated strings. See the overall HPSF documentation for
         * details!
         */
        var documentSummaryInformationProperties: PropertyIDMap? = null
            /**
             * 
             * Returns the Document Summary Information properties
             * singleton.
             * 
             * @return The Document Summary Information properties singleton.
             */
            get() {
                if (field == null) {
                    val m = PropertyIDMap(17, 1.0.toFloat())
                    m.put(PID_DICTIONARY.toLong(), "PID_DICTIONARY")
                    m.put(PID_CODEPAGE.toLong(), "PID_CODEPAGE")
                    m.put(PID_CATEGORY.toLong(), "PID_CATEGORY")
                    m.put(PID_PRESFORMAT.toLong(), "PID_PRESFORMAT")
                    m.put(PID_BYTECOUNT.toLong(), "PID_BYTECOUNT")
                    m.put(PID_LINECOUNT.toLong(), "PID_LINECOUNT")
                    m.put(PID_PARCOUNT.toLong(), "PID_PARCOUNT")
                    m.put(PID_SLIDECOUNT.toLong(), "PID_SLIDECOUNT")
                    m.put(PID_NOTECOUNT.toLong(), "PID_NOTECOUNT")
                    m.put(PID_HIDDENCOUNT.toLong(), "PID_HIDDENCOUNT")
                    m.put(PID_MMCLIPCOUNT.toLong(), "PID_MMCLIPCOUNT")
                    m.put(PID_SCALE.toLong(), "PID_SCALE")
                    m.put(PID_HEADINGPAIR.toLong(), "PID_HEADINGPAIR")
                    m.put(PID_DOCPARTS.toLong(), "PID_DOCPARTS")
                    m.put(PID_MANAGER.toLong(), "PID_MANAGER")
                    m.put(PID_COMPANY.toLong(), "PID_COMPANY")
                    m.put(PID_LINKSDIRTY.toLong(), "PID_LINKSDIRTY")
                    field =
                        PropertyIDMap(
                            Collections.unmodifiableMap<Any?, Any?>(
                                m
                            )
                        )
                }
                return field
            }
            private set


        /**
         * 
         * For the most basic testing.
         * 
         * @param args The command-line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val s1: PropertyIDMap = summaryInformationProperties!!
            val s2: PropertyIDMap = documentSummaryInformationProperties!!
            println("s1: " + s1)
            println("s2: " + s2)
        }
    }
}

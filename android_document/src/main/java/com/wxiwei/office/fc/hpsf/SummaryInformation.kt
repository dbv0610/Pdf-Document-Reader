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

import java.util.Date

/**
 * 
 * Convenience class representing a Summary Information stream in a
 * Microsoft Office document.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 * @see DocumentSummaryInformation
 */
class SummaryInformation(ps: PropertySet) : SpecialPropertySet(ps) {
    override fun getPropertySetIDMap(): PropertyIDMap? {
        return PropertyIDMap.summaryInformationProperties
    }


    /**
     * 
     * Creates a [SummaryInformation] from a given [ ].
     * 
     * @param ps A property set which should be created from a summary
     * information stream.
     * @throws UnexpectedPropertySetTypeException if <var>ps</var> does not
     * contain a summary information stream.
     */
    init {
        if (!isSummaryInformation()) throw UnexpectedPropertySetTypeException(
            "Not a "
                    + javaClass.getName()
        )
    }


    var title: String?
        /**
         * 
         * Returns the title (or `null`).
         * 
         * @return The title or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_TITLE) as String?
        /**
         * 
         * Sets the title.
         * 
         * @param title The title to set.
         */
        set(title) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_TITLE, title)
        }


    /**
     * 
     * Removes the title.
     */
    fun removeTitle() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_TITLE.toLong())
    }


    var subject: String?
        /**
         * 
         * Returns the subject (or `null`).
         * 
         * @return The subject or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_SUBJECT) as String?
        /**
         * 
         * Sets the subject.
         * 
         * @param subject The subject to set.
         */
        set(subject) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_SUBJECT, subject)
        }


    /**
     * 
     * Removes the subject.
     */
    fun removeSubject() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_SUBJECT.toLong())
    }


    var author: String?
        /**
         * 
         * Returns the author (or `null`).
         * 
         * @return The author or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_AUTHOR) as String?
        /**
         * 
         * Sets the author.
         * 
         * @param author The author to set.
         */
        set(author) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_AUTHOR, author)
        }


    /**
     * 
     * Removes the author.
     */
    fun removeAuthor() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_AUTHOR.toLong())
    }


    var keywords: String?
        /**
         * 
         * Returns the keywords (or `null`).
         * 
         * @return The keywords or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_KEYWORDS) as String?
        /**
         * 
         * Sets the keywords.
         * 
         * @param keywords The keywords to set.
         */
        set(keywords) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_KEYWORDS, keywords)
        }


    /**
     * 
     * Removes the keywords.
     */
    fun removeKeywords() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_KEYWORDS.toLong())
    }


    var comments: String?
        /**
         * 
         * Returns the comments (or `null`).
         * 
         * @return The comments or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_COMMENTS) as String?
        /**
         * 
         * Sets the comments.
         * 
         * @param comments The comments to set.
         */
        set(comments) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_COMMENTS, comments)
        }


    /**
     * 
     * Removes the comments.
     */
    fun removeComments() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_COMMENTS.toLong())
    }


    var template: String?
        /**
         * 
         * Returns the template (or `null`).
         * 
         * @return The template or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_TEMPLATE) as String?
        /**
         * 
         * Sets the template.
         * 
         * @param template The template to set.
         */
        set(template) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_TEMPLATE, template)
        }


    /**
     * 
     * Removes the template.
     */
    fun removeTemplate() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_TEMPLATE.toLong())
    }


    var lastAuthor: String?
        /**
         * 
         * Returns the last author (or `null`).
         * 
         * @return The last author or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_LASTAUTHOR) as String?
        /**
         * 
         * Sets the last author.
         * 
         * @param lastAuthor The last author to set.
         */
        set(lastAuthor) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_LASTAUTHOR, lastAuthor)
        }


    /**
     * 
     * Removes the last author.
     */
    fun removeLastAuthor() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_LASTAUTHOR.toLong())
    }


    var revNumber: String?
        /**
         * 
         * Returns the revision number (or `null`). 
         * 
         * @return The revision number or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_REVNUMBER) as String?
        /**
         * 
         * Sets the revision number.
         * 
         * @param revNumber The revision number to set.
         */
        set(revNumber) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_REVNUMBER, revNumber)
        }


    /**
     * 
     * Removes the revision number.
     */
    fun removeRevNumber() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_REVNUMBER.toLong())
    }


    var editTime: Long
        /**
         * 
         * Returns the total time spent in editing the document (or
         * `0`).
         * 
         * @return The total time spent in editing the document or 0 if the [         ] does not contain this information.
         */
        get() {
            val d =
                getProperty(PropertyIDMap.Companion.PID_EDITTIME) as Date?
            if (d == null) {
                return 0
            }
            return Util.dateToFileTime(d)
        }
        /**
         * 
         * Sets the total time spent in editing the document.
         * 
         * @param time The time to set.
         */
        set(time) {
            val d = Util.filetimeToDate(time)
            val s = getFirstSection() as MutableSection
            s.setProperty(
                PropertyIDMap.Companion.PID_EDITTIME,
                Variant.Companion.VT_FILETIME.toLong(),
                d
            )
        }


    /**
     * 
     * Remove the total time spent in editing the document.
     */
    fun removeEditTime() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_EDITTIME.toLong())
    }


    var lastPrinted: Date?
        /**
         * 
         * Returns the last printed time (or `null`).
         * 
         * @return The last printed time or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_LASTPRINTED) as Date?
        /**
         * 
         * Sets the lastPrinted.
         * 
         * @param lastPrinted The lastPrinted to set.
         */
        set(lastPrinted) {
            val s = getFirstSection() as MutableSection
            s.setProperty(
                PropertyIDMap.Companion.PID_LASTPRINTED,
                Variant.Companion.VT_FILETIME.toLong(),
                lastPrinted
            )
        }


    /**
     * 
     * Removes the lastPrinted.
     */
    fun removeLastPrinted() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_LASTPRINTED.toLong())
    }


    var createDateTime: Date?
        /**
         * 
         * Returns the creation time (or `null`).
         * 
         * @return The creation time or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_CREATE_DTM) as Date?
        /**
         * 
         * Sets the creation time.
         * 
         * @param createDateTime The creation time to set.
         */
        set(createDateTime) {
            val s = getFirstSection() as MutableSection
            s.setProperty(
                PropertyIDMap.Companion.PID_CREATE_DTM,
                Variant.Companion.VT_FILETIME.toLong(),
                createDateTime
            )
        }


    /**
     * 
     * Removes the creation time.
     */
    fun removeCreateDateTime() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_CREATE_DTM.toLong())
    }


    var lastSaveDateTime: Date?
        /**
         * 
         * Returns the last save time (or `null`).
         * 
         * @return The last save time or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_LASTSAVE_DTM) as Date?
        /**
         * 
         * Sets the total time spent in editing the document.
         * 
         * @param time The time to set.
         */
        set(time) {
            val s = getFirstSection() as MutableSection
            s
                .setProperty(
                    PropertyIDMap.Companion.PID_LASTSAVE_DTM,
                    Variant.Companion.VT_FILETIME.toLong(), time
                )
        }


    /**
     * 
     * Remove the total time spent in editing the document.
     */
    fun removeLastSaveDateTime() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_LASTSAVE_DTM.toLong())
    }


    var pageCount: Int
        /**
         * 
         * Returns the page count or 0 if the [SummaryInformation] does
         * not contain a page count.
         * 
         * @return The page count or 0 if the [SummaryInformation] does not
         * contain a page count.
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_PAGECOUNT)
        /**
         * 
         * Sets the page count.
         * 
         * @param pageCount The page count to set.
         */
        set(pageCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_PAGECOUNT, pageCount)
        }


    /**
     * 
     * Removes the page count.
     */
    fun removePageCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_PAGECOUNT.toLong())
    }


    var wordCount: Int
        /**
         * 
         * Returns the word count or 0 if the [SummaryInformation] does
         * not contain a word count.
         * 
         * @return The word count or `null`
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_WORDCOUNT)
        /**
         * 
         * Sets the word count.
         * 
         * @param wordCount The word count to set.
         */
        set(wordCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_WORDCOUNT, wordCount)
        }


    /**
     * 
     * Removes the word count.
     */
    fun removeWordCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_WORDCOUNT.toLong())
    }


    var charCount: Int
        /**
         * 
         * Returns the character count or 0 if the [SummaryInformation]
         * does not contain a char count.
         * 
         * @return The character count or `null`
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_CHARCOUNT)
        /**
         * 
         * Sets the character count.
         * 
         * @param charCount The character count to set.
         */
        set(charCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_CHARCOUNT, charCount)
        }


    /**
     * 
     * Removes the character count.
     */
    fun removeCharCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_CHARCOUNT.toLong())
    }


    var thumbnail: ByteArray?
        /**
         * 
         * Returns the thumbnail (or `null`) **when this
         * method is implemented. Please note that the return type is likely to
         * change!**
         * 
         * 
         * **Hint to developers:** Drew Varner &lt;Drew.Varner
         * -at- sc.edu&gt; said that this is an image in WMF or Clipboard (BMP?)
         * format. However, we won't do any conversion into any image type but
         * instead just return a byte array.
         * 
         * @return The thumbnail or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_THUMBNAIL) as ByteArray?
        /**
         * 
         * Sets the thumbnail.
         * 
         * @param thumbnail The thumbnail to set.
         */
        set(thumbnail) {
            val s = getFirstSection() as MutableSection
            s.setProperty(
                PropertyIDMap.Companion.PID_THUMBNAIL,  /* FIXME: */
                Variant.Companion.VT_LPSTR.toLong(), thumbnail
            )
        }


    /**
     * 
     * Removes the thumbnail.
     */
    fun removeThumbnail() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_THUMBNAIL.toLong())
    }


    var applicationName: String?
        /**
         * 
         * Returns the application name (or `null`).
         * 
         * @return The application name or `null`
         */
        get() = getProperty(PropertyIDMap.Companion.PID_APPNAME) as String?
        /**
         * 
         * Sets the application name.
         * 
         * @param applicationName The application name to set.
         */
        set(applicationName) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_APPNAME, applicationName)
        }


    /**
     * 
     * Removes the application name.
     */
    fun removeApplicationName() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_APPNAME.toLong())
    }


    var security: Int
        /**
         * 
         * Returns a security code which is one of the following values:
         * 
         * 
         * 
         *  * 
         *
         *0 if the [SummaryInformation] does not contain a
         * security field or if there is no security on the document. Use
         * [PropertySet.wasNull] to distinguish between the two
         * cases!
         * 
         *  * 
         *
         *1 if the document is password protected
         * 
         *  * 
         *
         *2 if the document is read-only recommended
         * 
         *  * 
         *
         *4 if the document is read-only enforced
         * 
         *  * 
         *
         *8 if the document is locked for annotations
         * 
         * 
         * 
         * @return The security code or `null`
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_SECURITY)
        /**
         * 
         * Sets the security code.
         * 
         * @param security The security code to set.
         */
        set(security) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_SECURITY, security)
        }


    /**
     * 
     * Removes the security code.
     */
    fun removeSecurity() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_SECURITY.toLong())
    }

    companion object {
        /**
         * 
         * The document name a summary information stream usually has in a POIFS
         * filesystem.
         */
        const val DEFAULT_STREAM_NAME: String = "\u0005SummaryInformation"
    }
}

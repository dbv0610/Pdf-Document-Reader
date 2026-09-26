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

/**
 * 
 * Convenience class representing a DocumentSummary Information stream in a
 * Microsoft Office document.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 * @author Drew Varner (Drew.Varner closeTo sc.edu)
 * @author robert_flaherty@hyperion.com
 * @see SummaryInformation
 */
class DocumentSummaryInformation(ps: PropertySet) : SpecialPropertySet(ps) {
    override fun getPropertySetIDMap(): PropertyIDMap? {
        return PropertyIDMap.documentSummaryInformationProperties
    }


    /**
     * 
     * Creates a [DocumentSummaryInformation] from a given
     * [PropertySet].
     * 
     * @param ps A property set which should be created from a
     * document summary information stream.
     * @throws UnexpectedPropertySetTypeException if <var>ps</var>
     * does not contain a document summary information stream.
     */
    init {
        if (!isDocumentSummaryInformation()) throw UnexpectedPropertySetTypeException("Not a " + javaClass.getName())
    }


    var category: String?
        /**
         * 
         * Returns the category (or `null`).
         * 
         * @return The category value
         */
        get() = getProperty(PropertyIDMap.Companion.PID_CATEGORY) as String?
        /**
         * 
         * Sets the category.
         * 
         * @param category The category to set.
         */
        set(category) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_CATEGORY, category)
        }

    /**
     * 
     * Removes the category.
     */
    fun removeCategory() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_CATEGORY.toLong())
    }


    var presentationFormat: String?
        /**
         * 
         * Returns the presentation format (or
         * `null`).
         * 
         * @return The presentation format value
         */
        get() = getProperty(PropertyIDMap.Companion.PID_PRESFORMAT) as String?
        /**
         * 
         * Sets the presentation format.
         * 
         * @param presentationFormat The presentation format to set.
         */
        set(presentationFormat) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_PRESFORMAT, presentationFormat)
        }

    /**
     * 
     * Removes the presentation format.
     */
    fun removePresentationFormat() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_PRESFORMAT.toLong())
    }


    var byteCount: Int
        /**
         * 
         * Returns the byte count or 0 if the [ ] does not contain a byte count.
         * 
         * @return The byteCount value
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_BYTECOUNT)
        /**
         * 
         * Sets the byte count.
         * 
         * @param byteCount The byte count to set.
         */
        set(byteCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_BYTECOUNT, byteCount)
        }

    /**
     * 
     * Removes the byte count.
     */
    fun removeByteCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_BYTECOUNT.toLong())
    }


    var lineCount: Int
        /**
         * 
         * Returns the line count or 0 if the [ ] does not contain a line count.
         * 
         * @return The line count value
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_LINECOUNT)
        /**
         * 
         * Sets the line count.
         * 
         * @param lineCount The line count to set.
         */
        set(lineCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_LINECOUNT, lineCount)
        }

    /**
     * 
     * Removes the line count.
     */
    fun removeLineCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_LINECOUNT.toLong())
    }


    var parCount: Int
        /**
         * 
         * Returns the par count or 0 if the [ ] does not contain a par count.
         * 
         * @return The par count value
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_PARCOUNT)
        /**
         * 
         * Sets the par count.
         * 
         * @param parCount The par count to set.
         */
        set(parCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_PARCOUNT, parCount)
        }

    /**
     * 
     * Removes the par count.
     */
    fun removeParCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_PARCOUNT.toLong())
    }


    var slideCount: Int
        /**
         * 
         * Returns the slide count or 0 if the [ ] does not contain a slide count.
         * 
         * @return The slide count value
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_SLIDECOUNT)
        /**
         * 
         * Sets the slideCount.
         * 
         * @param slideCount The slide count to set.
         */
        set(slideCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_SLIDECOUNT, slideCount)
        }

    /**
     * 
     * Removes the slide count.
     */
    fun removeSlideCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_SLIDECOUNT.toLong())
    }


    var noteCount: Int
        /**
         * 
         * Returns the note count or 0 if the [ ] does not contain a note count.
         * 
         * @return The note count value
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_NOTECOUNT)
        /**
         * 
         * Sets the note count.
         * 
         * @param noteCount The note count to set.
         */
        set(noteCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_NOTECOUNT, noteCount)
        }

    /**
     * 
     * Removes the noteCount.
     */
    fun removeNoteCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_NOTECOUNT.toLong())
    }


    var hiddenCount: Int
        /**
         * 
         * Returns the hidden count or 0 if the [ ] does not contain a hidden
         * count.
         * 
         * @return The hidden count value
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_HIDDENCOUNT)
        /**
         * 
         * Sets the hidden count.
         * 
         * @param hiddenCount The hidden count to set.
         */
        set(hiddenCount) {
            val s = getSections()!!.get(0) as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_HIDDENCOUNT, hiddenCount)
        }

    /**
     * 
     * Removes the hidden count.
     */
    fun removeHiddenCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_HIDDENCOUNT.toLong())
    }


    var mMClipCount: Int
        /**
         * 
         * Returns the mmclip count or 0 if the [ ] does not contain a mmclip
         * count.
         * 
         * @return The mmclip count value
         */
        get() = getPropertyIntValue(PropertyIDMap.Companion.PID_MMCLIPCOUNT)
        /**
         * 
         * Sets the mmclip count.
         * 
         * @param mmClipCount The mmclip count to set.
         */
        set(mmClipCount) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_MMCLIPCOUNT, mmClipCount)
        }

    /**
     * 
     * Removes the mmclip count.
     */
    fun removeMMClipCount() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_MMCLIPCOUNT.toLong())
    }


    var scale: Boolean
        /**
         * 
         * Returns `true` when scaling of the thumbnail is
         * desired, `false` if cropping is desired.
         * 
         * @return The scale value
         */
        get() = getPropertyBooleanValue(PropertyIDMap.Companion.PID_SCALE)
        /**
         * 
         * Sets the scale.
         * 
         * @param scale The scale to set.
         */
        set(scale) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_SCALE, scale)
        }

    /**
     * 
     * Removes the scale.
     */
    fun removeScale() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_SCALE.toLong())
    }


    var headingPair: ByteArray?
        /**
         * 
         * Returns the heading pair (or `null`)
         * **when this method is implemented. Please note that the
         * return type is likely to change!**
         * 
         * @return The heading pair value
         */
        get() {
            notYetImplemented("Reading byte arrays ")
            return getProperty(PropertyIDMap.Companion.PID_HEADINGPAIR) as ByteArray?
        }
        /**
         * 
         * Sets the heading pair.
         * 
         * @param headingPair The heading pair to set.
         */
        set(headingPair) {
            notYetImplemented("Writing byte arrays ")
        }

    /**
     * 
     * Removes the heading pair.
     */
    fun removeHeadingPair() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_HEADINGPAIR.toLong())
    }


    var docparts: ByteArray?
        /**
         * 
         * Returns the doc parts (or `null`)
         * **when this method is implemented. Please note that the
         * return type is likely to change!**
         * 
         * @return The doc parts value
         */
        get() {
            notYetImplemented("Reading byte arrays")
            return getProperty(PropertyIDMap.Companion.PID_DOCPARTS) as ByteArray?
        }
        /**
         * 
         * Sets the doc parts.
         * 
         * @param docparts The doc parts to set.
         */
        set(docparts) {
            notYetImplemented("Writing byte arrays")
        }


    /**
     * 
     * Removes the doc parts.
     */
    fun removeDocparts() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_DOCPARTS.toLong())
    }


    var manager: String?
        /**
         * 
         * Returns the manager (or `null`).
         * 
         * @return The manager value
         */
        get() = getProperty(PropertyIDMap.Companion.PID_MANAGER) as String?
        /**
         * 
         * Sets the manager.
         * 
         * @param manager The manager to set.
         */
        set(manager) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_MANAGER, manager)
        }

    /**
     * 
     * Removes the manager.
     */
    fun removeManager() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_MANAGER.toLong())
    }


    var company: String?
        /**
         * 
         * Returns the company (or `null`).
         * 
         * @return The company value
         */
        get() = getProperty(PropertyIDMap.Companion.PID_COMPANY) as String?
        /**
         * 
         * Sets the company.
         * 
         * @param company The company to set.
         */
        set(company) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_COMPANY, company)
        }

    /**
     * 
     * Removes the company.
     */
    fun removeCompany() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_COMPANY.toLong())
    }


    var linksDirty: Boolean
        /**
         * 
         * Returns `true` if the custom links are dirty. 
         *
         *
         * 
         * @return The links dirty value
         */
        get() = getPropertyBooleanValue(PropertyIDMap.Companion.PID_LINKSDIRTY)
        /**
         * 
         * Sets the linksDirty.
         * 
         * @param linksDirty The links dirty value to set.
         */
        set(linksDirty) {
            val s = getFirstSection() as MutableSection
            s.setProperty(PropertyIDMap.Companion.PID_LINKSDIRTY, linksDirty)
        }

    /**
     * 
     * Removes the links dirty.
     */
    fun removeLinksDirty() {
        val s = getFirstSection() as MutableSection
        s.removeProperty(PropertyIDMap.Companion.PID_LINKSDIRTY.toLong())
    }


    var customProperties: CustomProperties?
        /**
         * 
         * Gets the custom properties.
         * 
         * @return The custom properties.
         */
        get() {
            var cps: CustomProperties? = null
            if (getSectionCount() >= 2) {
                cps = CustomProperties()
                val section =
                    getSections()!!.get(1) as Section
                val dictionary =
                    section.getDictionary()
                val properties =
                    section.getProperties()
                var propertyCount = 0
                for (i in properties.indices) {
                    val p = properties[i]
                    val id = p.getID()
                    if (id != 0L && id != 1L) {
                        propertyCount++
                        val cp = CustomProperty(
                            p,
                            dictionary!!.get(id)
                        )
                        cps.put(cp.name, cp)
                    }
                }
                if (cps.size != propertyCount) cps.isPure = false
            }
            return cps
        }
        /**
         * 
         * Sets the custom properties.
         * 
         * @param customProperties The custom properties
         */
        set(customProperties) {
            ensureSection2()
            val section = getSections()!!.get(1) as MutableSection
            val dictionary =
                customProperties!!.dictionary
            section.clear()

            /* Set the codepage. If both custom properties and section have a
     * codepage, the codepage from the custom properties wins, else take the
     * one that is defined. If none is defined, take Unicode. */
            var cpCodepage = customProperties.codepage
            if (cpCodepage < 0) cpCodepage = section.codepage
            if (cpCodepage < 0) cpCodepage = Constants.CP_UNICODE
            customProperties.codepage = cpCodepage
            section.setCodepage(cpCodepage)
            section.setDictionary(dictionary)
            val i =
                customProperties.values.iterator()
            while (i.hasNext()) {
                val p: Property = i.next()
                section.setProperty(p)
            }
        }


    /**
     * 
     * Creates section 2 if it is not already present.
     * 
     */
    private fun ensureSection2() {
        if (getSectionCount() < 2) {
            val s2 = MutableSection()
            s2.setFormatID(SectionIDMap.Companion.DOCUMENT_SUMMARY_INFORMATION_ID[1])
            addSection(s2)
        }
    }


    /**
     * 
     * Removes the custom properties.
     */
    fun removeCustomProperties() {
        if (getSectionCount() >= 2) getSections()!!.removeAt(1)
        else throw HPSFRuntimeException("Illegal internal format of Document SummaryInformation stream: second section is missing.")
    }


    /**
     * 
     * Throws an [UnsupportedOperationException] with a message text
     * telling which functionality is not yet implemented.
     * 
     * @param msg text telling was leaves to be implemented, e.g.
     * "Reading byte arrays".
     */
    private fun notYetImplemented(msg: String?) {
        throw UnsupportedOperationException(msg + " is not yet implemented.")
    }

    companion object {
        /**
         * 
         * The document name a document summary information stream
         * usually has in a POIFS filesystem.
         */
        const val DEFAULT_STREAM_NAME: String = "\u0005DocumentSummaryInformation"
    }
}

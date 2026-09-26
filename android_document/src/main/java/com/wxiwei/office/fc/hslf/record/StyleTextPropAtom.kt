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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.hslf.model.textproperties.AlignmentTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.CharFlagsTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.ParagraphFlagsTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.TextProp
import com.wxiwei.office.fc.hslf.model.textproperties.TextPropCollection
import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.HexDump.dump
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.LinkedList

/**
 * A StyleTextPropAtom (type 4001). Holds basic character properties
 * (bold, italic, underline, font size etc) and paragraph properties
 * (alignment, line spacing etc) for the block of text (TextBytesAtom
 * or TextCharsAtom) that this record follows.
 * You will find two lists within this class.
 * 1 - Paragraph style list (paragraphStyles)
 * 2 - Character style list (charStyles)
 * Both are lists of TextPropCollections. These define how many characters
 * the style applies to, and what style elements make up the style (another
 * list, this time of TextProps). Each TextProp has a value, which somehow
 * encapsulates a property of the style
 * 
 * @author Nick Burch
 * @author Yegor Kozlov
 */
class StyleTextPropAtom : RecordAtom {
    private var _header: ByteArray?
    private var reserved: ByteArray?

    private var rawContents: ByteArray? // Holds the contents between write-outs

    /**
     * Only set to true once setParentTextSize(int) is called.
     * Until then, no stylings will have been decoded
     */
    private var initialised = false

    /**
     * Updates the link list of TextPropCollections which make up the
     * paragraph stylings
     */
    /**
     * The list of all the different paragraph stylings we code for.
     * Each entry is a TextPropCollection, which tells you how many
     * Characters the paragraph covers, and also contains the TextProps
     * that actually define the styling of the paragraph.
     */
    var paragraphStyles: LinkedList<TextPropCollection>?

    /**
     * Updates the link list of TextPropCollections which make up the
     * character stylings
     */
    /**
     * The list of all the different character stylings we code for.
     * Each entry is a TextPropCollection, which tells you how many
     * Characters the character styling covers, and also contains the
     * TextProps that actually define the styling of the characters.
     */
    var characterStyles: LinkedList<TextPropCollection>

    val paragraphTextLengthCovered: Int
        /**
         * Returns how many characters the paragraph's
         * TextPropCollections cover.
         * (May be one or two more than the underlying text does,
         * due to having extra characters meaning something
         * special to powerpoint)
         */
        get() = getCharactersCovered(paragraphStyles!!)

    val characterTextLengthCovered: Int
        /**
         * Returns how many characters the character's
         * TextPropCollections cover.
         * (May be one or two more than the underlying text does,
         * due to having extra characters meaning something
         * special to powerpoint)
         */
        get() = getCharactersCovered(this.characterStyles)

    private fun getCharactersCovered(styles: LinkedList<TextPropCollection>): Int {
        var length = 0
        for (tpc in styles) {
            length += tpc.charactersCovered
        }
        return length
    }

    /* *************** record code follows ********************** */
    /**
     * For the Text Style Properties (StyleTextProp) Atom
     */
    constructor(source: ByteArray, start: Int, len: Int) {
        // Sanity Checking - we're always at least 8+10 bytes long
        var len = len
        if (len < 18) {
            len = 18
            if (source.size - start < 18) {
                throw RuntimeException(
                    "Not enough data to form a StyleTextPropAtom (min size 18 bytes long) - found "
                            + (source.size - start)
                )
            }
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Save the contents of the atom, until we're asked to go and
        //  decode them (via a call to setParentTextSize(int)
        rawContents = ByteArray(len - 8)
        System.arraycopy(source, start + 8, rawContents, 0, rawContents!!.size)
        reserved = ByteArray(0)

        // Set empty linked lists, ready for when they call setParentTextSize
        paragraphStyles = LinkedList<TextPropCollection>()
        this.characterStyles = LinkedList<TextPropCollection>()
    }

    /**
     * A new set of text style properties for some text without any.
     */
    constructor(parentTextSize: Int) {
        _header = ByteArray(8)
        rawContents = ByteArray(0)
        reserved = ByteArray(0)

        // Set our type
        LittleEndian.putInt(_header!!, 2, _type.toShort().toInt())
        // Our initial size is 10
        LittleEndian.putInt(_header!!, 4, 10)

        // Set empty paragraph and character styles
        paragraphStyles = LinkedList<TextPropCollection>()
        this.characterStyles = LinkedList<TextPropCollection>()

        val defaultParagraphTextProps = TextPropCollection(
            parentTextSize,
            0.toShort()
        )
        paragraphStyles!!.add(defaultParagraphTextProps)

        val defaultCharacterTextProps = TextPropCollection(parentTextSize)
        characterStyles.add(defaultCharacterTextProps)

        // Set us as now initialised
        initialised = true
    }

    /**
     * We are of type 4001
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        // First thing to do is update the raw bytes of the contents, based
        //  on the properties
        updateRawContents()

        // Now ensure that the header size is correct
        val newSize = rawContents!!.size + reserved!!.size
        LittleEndian.putInt(_header!!, 4, newSize)

        // Write out the (new) header
        out.write(_header)

        // Write out the styles
        out.write(rawContents)

        // Write out any extra bits
        out.write(reserved)
    }

    /**
     * Tell us how much text the parent TextCharsAtom or TextBytesAtom
     * contains, so we can go ahead and initialise ourselves.
     */
    fun setParentTextSize(size: Int) {
        var pos = 0
        var textHandled = 0
        var para = 0
        var autoNumber = 0
        paraAutoNumberIndexs!!.clear()

        // While we have text in need of paragraph stylings, go ahead and
        // grok the contents as paragraph formatting data
        var prsize = size
        while (pos < rawContents!!.size && textHandled < prsize) {
            // First up, fetch the number of characters this applies to
            val textLen = LittleEndian.getInt(rawContents!!, pos)
            textHandled += textLen
            pos += 4

            val indent = LittleEndian.getShort(rawContents!!, pos)
            pos += 2

            // Grab the 4 byte value that tells us what properties follow
            val paraFlags = LittleEndian.getInt(rawContents!!, pos)
            pos += 4

            // Now make sense of those properties
            val thisCollection = TextPropCollection(textLen, indent)
            val plSize = thisCollection.buildTextPropList(
                paraFlags, paragraphTextPropTypes,
                rawContents!!, pos
            )
            pos += plSize

            // Save this properties set
            paragraphStyles!!.add(thisCollection)

            // Handle extra 1 paragraph styles at the end
            if (pos < rawContents!!.size && textHandled == size) {
                prsize++
            }


            // auto number index
            if (para > 0) {
                var para_flag = 0
                var temp = thisCollection.findByName("paragraph_flags")
                if (temp != null) {
                    para_flag = temp.value
                }
                if (para_flag != 1) {
                    var bulletChar = 0
                    temp = thisCollection.findByName("bullet.char")
                    if (temp != null) {
                        bulletChar = temp.value
                    }
                    if (para_flag != 2) {
                        if (bulletChar == 8226 || bulletChar == 8211) {
                            autoNumber++
                        } else {
                            val collection = paragraphStyles!!.get(para - 1)
                            if (collection != null) {
                                temp = collection.findByName("bullet.char")
                                if (temp != null) {
                                    bulletChar = temp.value
                                }
                            }
                            if (bulletChar == 8226 || bulletChar == 8211) {
                                autoNumber++
                            }
                        }
                    }
                } else {
                    autoNumber++
                }
            }
            paraAutoNumberIndexs!!.put(para, autoNumber)
            para++
        }

        /*if (rawContents.length > 0 && textHandled != (size + 1))
        {
            logger.log(POILogger.WARN, "Problem reading paragraph style runs: textHandled = "
                + textHandled + ", text.size+1 = " + (size + 1));
        }*/

        // Now do the character stylings
        textHandled = 0
        var chsize = size
        while (pos < rawContents!!.size && textHandled < chsize) {
            // First up, fetch the number of characters this applies to
            val textLen = LittleEndian.getInt(rawContents!!, pos)
            textHandled += textLen
            pos += 4

            // There is no 2 byte value
            val no_val: Short = -1

            // Grab the 4 byte value that tells us what properties follow
            val charFlags = LittleEndian.getInt(rawContents!!, pos)
            pos += 4

            // Now make sense of those properties
            // (Assuming we actually have some)
            val thisCollection = TextPropCollection(textLen, no_val)
            val chSize = thisCollection.buildTextPropList(
                charFlags, characterTextPropTypes,
                rawContents!!, pos
            )
            pos += chSize

            // Save this properties set
            characterStyles.add(thisCollection)

            // Handle extra 1 char styles at the end
            if (pos < rawContents!!.size && textHandled == size) {
                chsize++
            }
        }

        /*if (rawContents.length > 0 && textHandled != (size + 1))
        {
            logger.log(POILogger.WARN, "Problem reading character style runs: textHandled = "
                + textHandled + ", text.size+1 = " + (size + 1));
        }*/

        // Handle anything left over
        if (pos < rawContents!!.size) {
            reserved = ByteArray(rawContents!!.size - pos)
            System.arraycopy(rawContents, pos, reserved, 0, reserved!!.size)
        }

        initialised = true
    }

    /**
     * Updates the cache of the raw contents. Serialised the styles out.
     */
    @Throws(IOException::class)
    private fun updateRawContents() {
        if (!initialised) {
            // We haven't groked the styles since creation, so just stick
            // with what we found
            return
        }

        val baos = ByteArrayOutputStream()

        // First up, we need to serialise the paragraph properties
        for (i in paragraphStyles!!.indices) {
            val tpc = paragraphStyles!!.get(i)
            //tpc.writeOut(baos);
        }

        // Now, we do the character ones
        for (i in characterStyles.indices) {
            val tpc = characterStyles.get(i)
            //tpc.writeOut(baos);
        }

        rawContents = baos.toByteArray()
    }

    fun setRawContents(bytes: ByteArray?) {
        rawContents = bytes
        reserved = ByteArray(0)
        initialised = false
    }

    /**
     * Create a new Paragraph TextPropCollection, and add it to the list
     * @param charactersCovered The number of characters this TextPropCollection will cover
     * @return the new TextPropCollection, which will then be in the list
     */
    fun addParagraphTextPropCollection(charactersCovered: Int): TextPropCollection {
        val tpc = TextPropCollection(charactersCovered, 0.toShort())
        paragraphStyles!!.add(tpc)
        return tpc
    }

    /**
     * Create a new Character TextPropCollection, and add it to the list
     * @param charactersCovered The number of characters this TextPropCollection will cover
     * @return the new TextPropCollection, which will then be in the list
     */
    fun addCharacterTextPropCollection(charactersCovered: Int): TextPropCollection {
        val tpc = TextPropCollection(charactersCovered)
        characterStyles.add(tpc)
        return tpc
    }

    /* ************************************************************************ */
    /**
     * Dump the record content into `StringBuffer`
     * 
     * @return the string representation of the record data
     */
    override fun toString(): String {
        val out = StringBuffer()

        out.append("StyleTextPropAtom:\n")
        if (!initialised) {
            out.append("Uninitialised, dumping Raw Style Data\n")
        } else {
            out.append("Paragraph properties\n")

            for (pr in this.paragraphStyles!!) {
                out.append("  chars covered: " + pr.charactersCovered)
                out.append("  special mask flags: 0x" + toHex(pr.specialMask) + "\n")
                for (p in pr.textPropList!!) {
                    out.append("    " + p.name + " = " + p.value)
                    out.append(" (0x" + toHex(p.value) + ")\n")
                }

                out.append("  para bytes that would be written: \n")

                try {
                    val baos = ByteArrayOutputStream()
                    //pr.writeOut(baos);
                    val b = baos.toByteArray()
                    out.append(dump(b, 0, 0))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            out.append("Character properties\n")
            for (pr in this.characterStyles) {
                out.append("  chars covered: " + pr.charactersCovered)
                out.append("  special mask flags: 0x" + toHex(pr.specialMask) + "\n")
                for (p in pr.textPropList!!) {
                    out.append("    " + p.name + " = " + p.value)
                    out.append(" (0x" + toHex(p.value) + ")\n")
                }

                out.append("  char bytes that would be written: \n")

                try {
                    val baos = ByteArrayOutputStream()
                    //pr.writeOut(baos);
                    val b = baos.toByteArray()
                    out.append(dump(b, 0, 0))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        out.append("  original byte stream \n")
        out.append(HexDump.dump(rawContents!!, 0, 0))

        return out.toString()
    }

    /**
     * 
     * @param charcterIndex
     * @return
     */
    fun getAutoNumberIndex(charcterIndex: Int): Int {
        var paraIndex = 0
        if (paragraphStyles != null) {
            var start = 0
            for (i in paragraphStyles!!.indices) {
                val end = start + paragraphStyles!!.get(i).charactersCovered - 1
                if (charcterIndex >= start && charcterIndex <= end) {
                    paraIndex = i
                    break
                }
                start = end + 1
            }
        }
        if (paraIndex >= 0 && paraIndex < paraAutoNumberIndexs!!.size) {
            val index = paraAutoNumberIndexs!!.get(paraIndex)
            if (index != null) {
                return index
            }
        }
        return -1
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        reserved = null
        rawContents = null
        if (paraAutoNumberIndexs != null) {
            paraAutoNumberIndexs!!.clear()
            paraAutoNumberIndexs = null
        }
    }

    //
    private var paraAutoNumberIndexs: MutableMap<Int?, Int?>? = HashMap<Int?, Int?>()

    companion object {
        private const val _type = 4001L

        /** All the different kinds of paragraph properties we might handle  */
        var paragraphTextPropTypes: Array<TextProp?> = arrayOf<TextProp?>(
            TextProp(0, 0x1, "hasBullet"), TextProp(0, 0x2, "hasBulletFont"),
            TextProp(0, 0x4, "hasBulletColor"), TextProp(0, 0x8, "hasBulletSize"),
            ParagraphFlagsTextProp(), TextProp(2, 0x80, "bullet.char"),
            TextProp(2, 0x10, "bullet.font"), TextProp(2, 0x40, "bullet.size"),
            TextProp(4, 0x20, "bullet.color"), AlignmentTextProp(),
            TextProp(2, 0x100, "text.offset"), TextProp(2, 0x400, "bullet.offset"),
            TextProp(2, 0x1000, "linespacing"), TextProp(2, 0x2000, "spacebefore"),
            TextProp(2, 0x4000, "spaceafter"), TextProp(2, 0x8000, "defaultTabSize"),
            TextProp(2, 0x100000, "tabStops"), TextProp(2, 0x10000, "fontAlign"),
            TextProp(2, 0xE0000, "wrapFlags"), TextProp(2, 0x200000, "textDirection"),
            TextProp(2, 0x1000000, "buletScheme"), TextProp(2, 0x2000000, "bulletHasScheme")
        )

        /** All the different kinds of character properties we might handle  */
        var characterTextPropTypes: Array<TextProp?> = arrayOf<TextProp?>(
            TextProp(0, 0x1, "bold"), TextProp(0, 0x2, "italic"),
            TextProp(0, 0x4, "underline"), TextProp(0, 0x8, "unused1"),
            TextProp(0, 0x10, "shadow"), TextProp(0, 0x20, "fehint"),
            TextProp(0, 0x40, "unused2"), TextProp(0, 0x80, "kumi"),
            TextProp(0, 0x100, "unused3"), TextProp(0, 0x200, "emboss"),
            TextProp(0, 0x400, "nibble1"), TextProp(0, 0x800, "nibble2"),
            TextProp(0, 0x1000, "nibble3"), TextProp(0, 0x2000, "nibble4"),
            TextProp(0, 0x4000, "unused4"), TextProp(0, 0x8000, "unused5"),
            CharFlagsTextProp(), TextProp(2, 0x10000, "font.index"),
            TextProp(0, 0x100000, "pp10ext"), TextProp(2, 0x200000, "asian.font.index"),
            TextProp(2, 0x400000, "ansi.font.index"), TextProp(2, 0x800000, "symbol.font.index"),
            TextProp(2, 0x20000, "font.size"), TextProp(4, 0x40000, "font.color"),
            TextProp(2, 0x80000, "superscript"),
        )
    }
}

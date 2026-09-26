/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

/**
 * 
 * 
 * `OutputFormat` represents the format configuration used by
 * {@linkXMLWriter}and its base classes to format the XML output
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.17 $
 */
class OutputFormat : Cloneable {
    /**
     * DOCUMENT ME!
     * 
     * @return true if the output of the XML declaration (`<?xml
     * version="1.0"?>`)
     * should be suppressed else false.
     */
    /**
     * 
     * 
     * This will set whether the XML declaration (`<?xml version="1.0"
     * encoding="UTF-8"?>`)
     * is included or not. It is common to suppress this in protocols such as
     * WML and SOAP.
     * 
     * 
     * @param suppressDeclaration
     * `boolean` indicating whether or not the XML
     * declaration should be suppressed.
     */
    /**
     * Whether or not to suppress the XML declaration - default is
     * `false`
     */
    var isSuppressDeclaration: Boolean = false

    /**
     * DOCUMENT ME!
     * 
     * @return true if a new line should be printed following XML declaration
     */
    /**
     * 
     * 
     * This will set whether a new line is printed after the XML declaration
     * (assuming it is not supressed.)
     * 
     * 
     * @param newLineAfterDeclaration
     * `boolean` indicating whether or not to print new
     * line following the XML declaration. The default is true.
     */
    /**
     * Whether or not to print new line after the XML declaration - default is
     * `true`
     */
    var isNewLineAfterDeclaration: Boolean = true

    /** The encoding format  */
    private var encoding: String? = "UTF-8"

    /**
     * 
     * 
     * This will set whether the XML declaration (`<?xml version="1.0"
     * encoding="UTF-8"?>`)
     * includes the encoding of the document. It is common to suppress this in
     * protocols such as WML and SOAP.
     * 
     * 
     * @param omitEncoding
     * `boolean` indicating whether or not the XML
     * declaration should indicate the document encoding.
     */
    /**
     * Whether or not to output the encoding in the XML declaration - default is
     * `false`
     */
    var isOmitEncoding: Boolean = false

    /** The default indent is no spaces (as original document)  */
    private var indent: String? = null

    /**
     * 
     * 
     * This will set whether empty elements are expanded from
     * `<tagName>` to
     * `<tagName></tagName>`.
     * 
     * 
     * @param expandEmptyElements
     * `boolean` indicating whether or not empty
     * elements should be expanded.
     */
    /**
     * Whether or not to expand empty elements to
     * &lt;tagName&gt;&lt;/tagName&gt; - default is `false`
     */
    var isExpandEmptyElements: Boolean = false

    /**
     * DOCUMENT ME!
     * 
     * @param newlines
     * `true` indicates new lines should be printed,
     * else new lines are ignored (compacted).
     * 
     * @see .setLineSeparator
     */
    /**
     * The default new line flag, set to do new lines only as in original
     * document
     */
    var isNewlines: Boolean = false

    /**
     * 
     * 
     * This will set the new-line separator. The default is `\n`.
     * Note that if the "newlines" property is false, this value is irrelevant.
     * To make it output the system default line ending string, call
     * `setLineSeparator(System.getProperty("line.separator"))`
     * 
     * 
     * @param separator
     * `String` line separator to use.
     * 
     * @see .setNewlines
     */
    /** New line separator  */
    var lineSeparator: String? = "\n"

    /**
     * 
     * 
     * This will set whether the text is output verbatim (false) or with
     * whitespace stripped as per `[ ][org.dom4j.Element.getTextTrim]`.
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * Default: false
     * 
     * 
     * @param trimText
     * `boolean` true=>trim the whitespace, false=>use
     * text verbatim
     */
    /** should we preserve whitespace or not in text nodes?  */
    var isTrimText: Boolean = false

    /**
     * 
     * 
     * Ensure that text immediately preceded by or followed by an element will
     * be "padded" with a single space. This is used to allow make
     * browser-friendly HTML, avoiding trimText's transformation of, e.g.,
     * `The quick <b>brown</b> fox` into `The
     * quick<b>brown</b>fox`
     * (the latter will run the three separate words together into a single
     * word). This setting is not too useful if you haven't also called
     * [.setTrimText].
     * 
     * 
     * 
     * 
     * The padding string will only be added if the text itself starts or ends
     * with some whitespace characters.
     * 
     * 
     * 
     * 
     * Default: false
     * 
     * 
     * @param padText
     * `boolean` if true, pad string-element boundaries
     */
    /** pad string-element boundaries with whitespace  */
    var isPadText: Boolean = false

    /**
     * 
     * 
     * Whether or not to use the XHTML standard: like HTML but passes an XML
     * parser with real, closed tags. Also, XHTML CDATA sections will be output
     * with the CDATA delimiters: ( &quot; **&lt;![CDATA[ **&quot; and &quot;
     * **]]&gt; **&quot; ) otherwise, the class HTMLWriter will output the
     * CDATA text, but not the delimiters.
     * 
     * 
     * 
     * 
     * Default is `false`
     * 
     * 
     * @return DOCUMENT ME!
     */
    /**
     * 
     * 
     * This will set whether or not to use the XHTML standard: like HTML but
     * passes an XML parser with real, closed tags. Also, XHTML CDATA sections
     * will be output with the CDATA delimiters: ( &quot; **&lt;[CDATA[
     ** * &quot; and &quot; **]]&lt; **) otherwise, the class HTMLWriter
     * will output the CDATA text, but not the delimiters.
     * 
     * 
     * 
     * 
     * Default: false
     * 
     * 
     * @param xhtml
     * `boolean` true=>conform to XHTML, false=>conform
     * to HTML, can have unclosed tags, etc.
     */
    /** Whether or not to use XHTML standard.  */
    var isXHTML: Boolean = false

    /**
     * Controls output of a line.separator every tagCount tags when isNewlines
     * is false. If tagCount equals zero, it means don't do anything special. If
     * greater than zero, then a line.separator will be output after tagCount
     * tags have been output. Used when you would like to squeeze the html as
     * much as possible, but some browsers don't like really long lines. A tag
     * count of 10 would produce a line.separator in the output after 10 close
     * tags (including single tags).
     * 
     * @param tagCount
     * DOCUMENT ME!
     */
    /**
     * Controls when to output a line.separtor every so many tags in case of no
     * lines and total text trimming.
     */
    var newLineAfterNTags: Int = 0 // zero means don't bother.

    /** Quote character to use when writing attributes.  */
    private var attributeQuoteChar = '\"'

    /**
     * Creates an `OutputFormat` with no additional whitespace
     * (indent or new lines) added. The whitespace from the element text content
     * is fully preserved.
     */
    constructor()

    /**
     * Creates an `OutputFormat` with the given indent added but no
     * new lines added. All whitespace from element text will be included.
     * 
     * @param indent
     * is the indent string to be used for indentation (usually a
     * number of spaces).
     */
    constructor(indent: String?) {
        this.indent = indent
    }

    /**
     * Creates an `OutputFormat` with the given indent added with
     * optional newlines between the Elements. All whitespace from element text
     * will be included.
     * 
     * @param indent
     * is the indent string to be used for indentation (usually a
     * number of spaces).
     * @param newlines
     * whether new lines are added to layout the
     */
    constructor(indent: String?, newlines: Boolean) {
        this.indent = indent
        this.isNewlines = newlines
    }

    /**
     * Creates an `OutputFormat` with the given indent added with
     * optional newlines between the Elements and the given encoding format.
     * 
     * @param indent
     * is the indent string to be used for indentation (usually a
     * number of spaces).
     * @param newlines
     * whether new lines are added to layout the
     * @param encoding
     * is the text encoding to use for writing the XML
     */
    constructor(indent: String?, newlines: Boolean, encoding: String?) {
        this.indent = indent
        this.isNewlines = newlines
        this.encoding = encoding
    }

    fun getEncoding(): String? {
        return encoding
    }

    /**
     * DOCUMENT ME!
     * 
     * @param encoding
     * encoding format
     */
    fun setEncoding(encoding: String?) {
        if (encoding != null) {
            this.encoding = encoding
        }
    }

    fun getIndent(): String? {
        return indent
    }

    /**
     * 
     * 
     * This will set the indent `String` to use; this is usually a
     * `String` of empty spaces. If you pass null, or the empty
     * string (""), then no indentation will happen.
     * 
     * Default: none (null)
     * 
     * @param indent
     * `String` to use for indentation.
     */
    fun setIndent(indent: String?) {
        // nullify empty string to void unnecessary indentation code
        var indent = indent
        if ((indent != null) && (indent.length <= 0)) {
            indent = null
        }

        this.indent = indent
    }

    /**
     * Set the indent on or off. If setting on, will use the value of
     * STANDARD_INDENT, which is usually two spaces.
     * 
     * @param doIndent
     * if true, set indenting on; if false, set indenting off
     */
    fun setIndent(doIndent: Boolean) {
        if (doIndent) {
            this.indent = STANDARD_INDENT
        } else {
            this.indent = null
        }
    }

    /**
     * 
     * 
     * This will set the indent `String`'s size; an indentSize of
     * 4 would result in the indention being equivalent to the
     * `String` "&nbsp;&nbsp;&nbsp;&nbsp;" (four space characters).
     * 
     * 
     * @param indentSize
     * `int` number of spaces in indentation.
     */
    fun setIndentSize(indentSize: Int) {
        val indentBuffer = StringBuffer()

        for (i in 0..<indentSize) {
            indentBuffer.append(" ")
        }

        this.indent = indentBuffer.toString()
    }

    var attributeQuoteCharacter: Char
        get() = attributeQuoteChar
        /**
         * Sets the character used to quote attribute values. The specified
         * character must be a valid XML attribute quote character, otherwise an
         * `IllegalArgumentException` will be thrown.
         * 
         * @param quoteChar
         * The character to use when quoting attribute values.
         * 
         * @throws IllegalArgumentException
         * If the specified character is not a valid XML attribute quote
         * character.
         */
        set(quoteChar) {
            if ((quoteChar == '\'') || (quoteChar == '"')) {
                attributeQuoteChar = quoteChar
            } else {
                throw IllegalArgumentException(
                    ("Invalid attribute quote "
                            + "character (" + quoteChar + ")")
                )
            }
        }

    /**
     * Parses command line arguments of the form `-omitEncoding
     * -indentSize 3 -newlines -trimText`
     * 
     * @param args
     * is the array of command line arguments
     * @param i
     * is the index in args to start parsing options
     * 
     * @return the index of first parameter that we didn't understand
     */
    fun parseOptions(args: Array<String?>, i: Int): Int {
        var i = i
        val size = args.size
        while (i < size) {
            if (args[i] == "-suppressDeclaration") {
                this.isSuppressDeclaration = true
            } else if (args[i] == "-omitEncoding") {
                this.isOmitEncoding = true
            } else if (args[i] == "-indent") {
                setIndent(args[++i])
            } else if (args[i] == "-indentSize") {
                setIndentSize(args[++i]!!.toInt())
            } else if (args[i]!!.startsWith("-expandEmpty")) {
                this.isExpandEmptyElements = true
            } else if (args[i] == "-encoding") {
                setEncoding(args[++i])
            } else if (args[i] == "-newlines") {
                this.isNewlines = true
            } else if (args[i] == "-lineSeparator") {
                this.lineSeparator = args[++i]
            } else if (args[i] == "-trimText") {
                this.isTrimText = true
            } else if (args[i] == "-padText") {
                this.isPadText = true
            } else if (args[i]!!.startsWith("-xhtml")) {
                this.isXHTML = true
            } else {
                return i
            }
            i++
        }

        return i
    }

    companion object {
        /** standard value to indent by, if we are indenting  */
        protected const val STANDARD_INDENT: String = "  "

        /**
         * A static helper method to create the default pretty printing format. This
         * format consists of an indent of 2 spaces, newlines after each element and
         * all other whitespace trimmed, and XMTML is false.
         * 
         * @return DOCUMENT ME!
         */
        fun createPrettyPrint(): OutputFormat {
            val format = OutputFormat()
            format.setIndentSize(2)
            format.isNewlines = true
            format.isTrimText = true
            format.isPadText = true

            return format
        }

        /**
         * A static helper method to create the default compact format. This format
         * does not have any indentation or newlines after an alement and all other
         * whitespace trimmed
         * 
         * @return DOCUMENT ME!
         */
        fun createCompactFormat(): OutputFormat {
            val format = OutputFormat()
            format.setIndent(false)
            format.isNewlines = false
            format.isTrimText = true

            return format
        }
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


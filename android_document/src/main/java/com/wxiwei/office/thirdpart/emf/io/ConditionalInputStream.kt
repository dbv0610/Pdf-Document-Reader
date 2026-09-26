// Copyright 2001-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream
import java.util.Properties

/**
 * The ConditionalInputStream reads a stream and filters certain parts depending
 * of properties and statements in the input.
 * <P>
 * The following statements, all start with the -sign, are allowed:
</P> * <UL>
 * <LI><B>@ifdef property</B>, reads everything up to the next
 * 
 * <P> if the property is defined. </P></LI><LI><B>@ifndef property</B>, reads
 * everything up to the next
 * <P> if the property is not defined. </P></LI><LI><B>@else</B>, corresponding
 * else statement </LI><LI><B>@endif</B>, corresponging endif statement
</LI></UL> * 
 * The -sign itself must be escaped by a backslash, if used in
 * the text followed by any of the keywords described above and no
 * action should be taken.
 * <P>
 * @author Mark Donszelmann
</P> */
class ConditionalInputStream
/**
 * Creates a Conditional Input Stream from given stream.
 * 
 * @param input
 * stream to read from
 * @param defines
 * set of properties to be used in ifdefs
 */(private val `in`: InputStream, private val defines: Properties) : DecodingInputStream() {
    private val buffer = IntArray(4096)

    private var index = 0

    private var len = 0

    private var nesting = 0

    private val ok = BooleanArray(50)

    private var escape = false

    @Throws(IOException::class)
    override fun read(): Int {
        var b: Int
        var n: Int

        // read from buffer if possible
        if (index < len) {
            b = buffer[index]
            index++
        } else {
            b = `in`.read()
        }

        // return if End Of Stream
        if (b < 0) {
            return -1
        }

        // escape \@-signs
        if (b == '\\'.code) {
            n = `in`.read()
            if (n == '@'.code) {
                b = ' '.code
                escape = true
            }
            buffer[0] = n
            index = 0
            len = 1
        }

        // check on @ sign
        if (b == '@'.code) {
            if (escape) {
                escape = false
            } else {
                // read keyword (ifdef, ifndef, else, endif
                index = 0
                var s = StringBuffer()
                n = `in`.read()
                while ((n >= 0) && !Character.isWhitespace(n.toChar())) {
                    s.append(n.toChar())
                    buffer[index] = n
                    n = `in`.read()
                    index++
                }
                buffer[index] = n
                index++
                b = ' '.code

                // check on keyword
                val keyword = s.toString()
                if (keyword == "ifdef" || keyword == "ifndef") {
                    // skip whitespace and read property

                    s = StringBuffer()
                    n = `in`.read()
                    while ((n >= 0) && Character.isWhitespace(n.toChar())) {
                        buffer[index] = n
                        n = `in`.read()
                        index++
                    }
                    while ((n >= 0) && !Character.isWhitespace(n.toChar())) {
                        s.append(n.toChar())
                        buffer[index] = n
                        n = `in`.read()
                        index++
                    }
                    buffer[index] = n
                    index++

                    // check on property
                    val property = s.toString()
                    if (defines.getProperty(property) != null) {
                        ok[nesting] = (if (nesting > 0) ok[nesting - 1] else true)
                                && keyword == "ifdef"
                    } else {
                        ok[nesting] = (if (nesting > 0) ok[nesting - 1] else true)
                                && keyword == "ifndef"
                    }
                    nesting++
                    replaceBufferWithWhitespace(index)
                } else if (keyword == "else") {
                    // FIXME one could have multiple elses without endifs...
                    // calculate inclusion based on ifdef nesting
                    if (nesting <= 0) {
                        throw RuntimeException(
                            "@else without corresponding @ifdef"
                        )
                    }
                    ok[nesting - 1] = (if (nesting > 1) ok[nesting - 2] else true)
                            && !ok[nesting - 1]
                    replaceBufferWithWhitespace(index)
                } else if (keyword == "endif") {
                    // calculate inclusion based on ifdef nesting
                    if (nesting <= 0) {
                        throw RuntimeException(
                            "@endif without corresponding @ifdef"
                        )
                    }
                    nesting--
                    replaceBufferWithWhitespace(index)
                } else {
                    // not an known @
                    b = '@'.code
                }
                len = index
                index = 0
            }
        }

        if ((nesting > 0) && !ok[nesting - 1]) {
            if (!Character.isWhitespace(b.toChar())) {
                b = ' '.code
            }
        }
        return b and 0x00FF
    }

    private fun replaceBufferWithWhitespace(size: Int) {
        for (i in 0..<size) {
            if (!Character.isWhitespace(buffer[i].toChar())) {
                buffer[i] = ' '.code
            }
        }
    }
}

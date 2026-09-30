/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wxiwei.office.fc.codec

/**
 * Character encoding names required of every implementation of the Java platform.
 * 
 * From the Java documentation [Standard
 * charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html):
 * 
 * 
 * <cite>Every implementation of the Java platform is required to support the following character encodings. Consult the
 * release documentation for your implementation to see if any other encodings are supported. Consult the release
 * documentation for your implementation to see if any other encodings are supported. </cite>
 * 
 * 
 * 
 *  * `US-ASCII`<br></br>
 * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set.
 *  * `ISO-8859-1`<br></br>
 * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
 *  * `UTF-8`<br></br>
 * Eight-bit Unicode Transformation Format.
 *  * `UTF-16BE`<br></br>
 * Sixteen-bit Unicode Transformation Format, big-endian byte order.
 *  * `UTF-16LE`<br></br>
 * Sixteen-bit Unicode Transformation Format, little-endian byte order.
 *  * `UTF-16`<br></br>
 * Sixteen-bit Unicode Transformation Format, byte order specified by a mandatory initial byte-order mark (either order
 * accepted on input, big-endian used on output.)
 * 
 * 
 * This perhaps would best belong in the [lang] project. Even if a similar interface is defined in [lang], it is not
 * forseen that [codec] would be made to depend on [lang].
 * 
 * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
 * 
 * @author Apache Software Foundation
 * @since 1.4
 * @version $Id: CharEncoding.java 797857 2009-07-25 23:43:33Z ggregory $
 */
object CharEncoding {
    /**
     * CharEncodingISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1. 
     * 
     * 
     * Every implementation of the Java platform is required to support this character encoding.
     * 
     * 
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     */
    const val ISO_8859_1: String = "ISO-8859-1"

    /**
     * 
     * 
     * Seven-bit ASCII, also known as ISO646-US, also known as the Basic Latin block of the Unicode character set.
     * 
     * 
     * 
     * Every implementation of the Java platform is required to support this character encoding.
     * 
     * 
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     */
    const val US_ASCII: String = "US-ASCII"

    /**
     * 
     * 
     * Sixteen-bit Unicode Transformation Format, The byte order specified by a mandatory initial byte-order mark
     * (either order accepted on input, big-endian used on output)
     * 
     * 
     * 
     * Every implementation of the Java platform is required to support this character encoding.
     * 
     * 
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     */
    const val UTF_16: String = "UTF-16"

    /**
     * 
     * 
     * Sixteen-bit Unicode Transformation Format, big-endian byte order.
     * 
     * 
     * 
     * Every implementation of the Java platform is required to support this character encoding.
     * 
     * 
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     */
    const val UTF_16BE: String = "UTF-16BE"

    /**
     * 
     * 
     * Sixteen-bit Unicode Transformation Format, little-endian byte order.
     * 
     * 
     * 
     * Every implementation of the Java platform is required to support this character encoding.
     * 
     * 
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     */
    const val UTF_16LE: String = "UTF-16LE"

    /**
     * 
     * 
     * Eight-bit Unicode Transformation Format.
     * 
     * 
     * 
     * Every implementation of the Java platform is required to support this character encoding.
     * 
     * 
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     */
    const val UTF_8: String = "UTF-8"
}

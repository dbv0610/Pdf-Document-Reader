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

import java.io.UnsupportedEncodingException

/**
 * Converts String to and from bytes using the encodings required by the Java specification. These encodings are specified in [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
 * 
 * @see CharEncoding
 * 
 * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
 * 
 * @author [Gary Gregory](mailto:ggregory@seagullsw.com)
 * @version $Id: StringUtils.java 950460 2010-06-02 09:43:02Z sebb $
 * @since 1.4
 */
object StringUtils {
    /**
     * Encodes the given string into a sequence of bytes using the ISO-8859-1 charset, storing the result into a new
     * byte array.
     * 
     * @param string
     * the String to encode, may be `null`
     * @return encoded bytes, or `null` if the input string was `null`
     * @throws IllegalStateException
     * Thrown when the charset is missing, which should be never according the the Java specification.
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     * 
     * @see .getBytesUnchecked
     */
    fun getBytesIso8859_1(string: String?): ByteArray? {
        return getBytesUnchecked(string, CharEncoding.ISO_8859_1)
    }

    /**
     * Encodes the given string into a sequence of bytes using the US-ASCII charset, storing the result into a new byte
     * array.
     * 
     * @param string
     * the String to encode, may be `null`
     * @return encoded bytes, or `null` if the input string was `null`
     * @throws IllegalStateException
     * Thrown when the charset is missing, which should be never according the the Java specification.
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     * 
     * @see .getBytesUnchecked
     */
    fun getBytesUsAscii(string: String?): ByteArray? {
        return getBytesUnchecked(string, CharEncoding.US_ASCII)
    }

    /**
     * Encodes the given string into a sequence of bytes using the UTF-16 charset, storing the result into a new byte
     * array.
     * 
     * @param string
     * the String to encode, may be `null`
     * @return encoded bytes, or `null` if the input string was `null`
     * @throws IllegalStateException
     * Thrown when the charset is missing, which should be never according the the Java specification.
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     * 
     * @see .getBytesUnchecked
     */
    fun getBytesUtf16(string: String?): ByteArray? {
        return getBytesUnchecked(string, CharEncoding.UTF_16)
    }

    /**
     * Encodes the given string into a sequence of bytes using the UTF-16BE charset, storing the result into a new byte
     * array.
     * 
     * @param string
     * the String to encode, may be `null`
     * @return encoded bytes, or `null` if the input string was `null`
     * @throws IllegalStateException
     * Thrown when the charset is missing, which should be never according the the Java specification.
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     * 
     * @see .getBytesUnchecked
     */
    fun getBytesUtf16Be(string: String?): ByteArray? {
        return getBytesUnchecked(string, CharEncoding.UTF_16BE)
    }

    /**
     * Encodes the given string into a sequence of bytes using the UTF-16LE charset, storing the result into a new byte
     * array.
     * 
     * @param string
     * the String to encode, may be `null`
     * @return encoded bytes, or `null` if the input string was `null`
     * @throws IllegalStateException
     * Thrown when the charset is missing, which should be never according the the Java specification.
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     * 
     * @see .getBytesUnchecked
     */
    fun getBytesUtf16Le(string: String?): ByteArray? {
        return getBytesUnchecked(string, CharEncoding.UTF_16LE)
    }

    /**
     * Encodes the given string into a sequence of bytes using the UTF-8 charset, storing the result into a new byte
     * array.
     * 
     * @param string
     * the String to encode, may be `null`
     * @return encoded bytes, or `null` if the input string was `null`
     * @throws IllegalStateException
     * Thrown when the charset is missing, which should be never according the the Java specification.
     * @see [Standard charsets](http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html)
     * 
     * @see .getBytesUnchecked
     */
    fun getBytesUtf8(string: String?): ByteArray? {
        return getBytesUnchecked(string, CharEncoding.UTF_8)
    }

    /**
     * Encodes the given string into a sequence of bytes using the named charset, storing the result into a new byte
     * array.
     * 
     * 
     * This method catches [UnsupportedEncodingException] and rethrows it as [IllegalStateException], which
     * should never happen for a required charset name. Use this method when the encoding is required to be in the JRE.
     * 
     * 
     * @param string
     * the String to encode, may be `null`
     * @param charsetName
     * The name of a required [java.nio.charset.Charset]
     * @return encoded bytes, or `null` if the input string was `null`
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen for a
     * required charset name.
     * @see CharEncoding
     * 
     * @see String.getBytes
     */
    fun getBytesUnchecked(string: String?, charsetName: String): ByteArray? {
        if (string == null) {
            return null
        }
        try {
            return string.toByteArray(charset(charsetName))
        } catch (e: UnsupportedEncodingException) {
            throw newIllegalStateException(charsetName, e)
        }
    }

    private fun newIllegalStateException(
        charsetName: String?,
        e: UnsupportedEncodingException?
    ): IllegalStateException {
        return IllegalStateException(charsetName + ": " + e)
    }

    /**
     * Constructs a new `String` by decoding the specified array of bytes using the given charset.
     * 
     * 
     * This method catches [UnsupportedEncodingException] and re-throws it as [IllegalStateException], which
     * should never happen for a required charset name. Use this method when the encoding is required to be in the JRE.
     * 
     * 
     * @param bytes
     * The bytes to be decoded into characters, may be `null`
     * @param charsetName
     * The name of a required [java.nio.charset.Charset]
     * @return A new `String` decoded from the specified array of bytes using the given charset,
     * or `null` if the input byte arrray was `null`.
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen for a
     * required charset name.
     * @see CharEncoding
     * 
     * @see String.String
     */
    fun newString(bytes: ByteArray?, charsetName: String): String? {
        if (bytes == null) {
            return null
        }
        try {
            return String(bytes, charset(charsetName))
        } catch (e: UnsupportedEncodingException) {
            throw newIllegalStateException(charsetName, e)
        }
    }

    /**
     * Constructs a new `String` by decoding the specified array of bytes using the ISO-8859-1 charset.
     * 
     * @param bytes
     * The bytes to be decoded into characters, may be `null`
     * @return A new `String` decoded from the specified array of bytes using the ISO-8859-1 charset,
     * or `null` if the input byte array was `null`.
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen since the
     * charset is required.
     */
    fun newStringIso8859_1(bytes: ByteArray?): String? {
        return newString(bytes, CharEncoding.ISO_8859_1)
    }

    /**
     * Constructs a new `String` by decoding the specified array of bytes using the US-ASCII charset.
     * 
     * @param bytes
     * The bytes to be decoded into characters
     * @return A new `String` decoded from the specified array of bytes using the US-ASCII charset,
     * or `null` if the input byte array was `null`.
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen since the
     * charset is required.
     */
    fun newStringUsAscii(bytes: ByteArray?): String? {
        return newString(bytes, CharEncoding.US_ASCII)
    }

    /**
     * Constructs a new `String` by decoding the specified array of bytes using the UTF-16 charset.
     * 
     * @param bytes
     * The bytes to be decoded into characters
     * @return A new `String` decoded from the specified array of bytes using the UTF-16 charset
     * or `null` if the input byte array was `null`.
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen since the
     * charset is required.
     */
    fun newStringUtf16(bytes: ByteArray?): String? {
        return newString(bytes, CharEncoding.UTF_16)
    }

    /**
     * Constructs a new `String` by decoding the specified array of bytes using the UTF-16BE charset.
     * 
     * @param bytes
     * The bytes to be decoded into characters
     * @return A new `String` decoded from the specified array of bytes using the UTF-16BE charset,
     * or `null` if the input byte array was `null`.
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen since the
     * charset is required.
     */
    fun newStringUtf16Be(bytes: ByteArray?): String? {
        return newString(bytes, CharEncoding.UTF_16BE)
    }

    /**
     * Constructs a new `String` by decoding the specified array of bytes using the UTF-16LE charset.
     * 
     * @param bytes
     * The bytes to be decoded into characters
     * @return A new `String` decoded from the specified array of bytes using the UTF-16LE charset,
     * or `null` if the input byte array was `null`.
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen since the
     * charset is required.
     */
    fun newStringUtf16Le(bytes: ByteArray?): String? {
        return newString(bytes, CharEncoding.UTF_16LE)
    }

    /**
     * Constructs a new `String` by decoding the specified array of bytes using the UTF-8 charset.
     * 
     * @param bytes
     * The bytes to be decoded into characters
     * @return A new `String` decoded from the specified array of bytes using the UTF-8 charset,
     * or `null` if the input byte array was `null`.
     * @throws IllegalStateException
     * Thrown when a [UnsupportedEncodingException] is caught, which should never happen since the
     * charset is required.
     */
    fun newStringUtf8(bytes: ByteArray?): String? {
        return newString(bytes, CharEncoding.UTF_8)
    }
}

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

import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Operations to simplify common [MessageDigest] tasks. This class is thread safe.
 * 
 * @author Apache Software Foundation
 * @version $Id: DigestUtils.java 1064793 2011-01-28 17:42:55Z ggregory $
 */
object DigestUtils {
    private const val STREAM_BUFFER_LENGTH = 1024

    /**
     * Read through an InputStream and returns the digest for the data
     * 
     * @param digest
     * The MessageDigest to use (e.g. MD5)
     * @param data
     * Data to digest
     * @return MD5 digest
     * @throws IOException
     * On error reading from the stream
     */
    @Throws(IOException::class)
    private fun digest(digest: MessageDigest, data: InputStream): ByteArray {
        val buffer = ByteArray(STREAM_BUFFER_LENGTH)
        var read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)

        while (read > -1) {
            digest.update(buffer, 0, read)
            read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
        }

        return digest.digest()
    }

    /**
     * Calls [StringUtils.getBytesUtf8]
     * 
     * @param data
     * the String to encode
     * @return encoded bytes
     */
    private fun getBytesUtf8(data: String?): ByteArray? {
        return StringUtils.getBytesUtf8(data)
    }

    /**
     * Returns a `MessageDigest` for the given `algorithm`.
     * 
     * @param algorithm
     * the name of the algorithm requested. See [Appendix A in the Java
     * Cryptography Architecture API Specification & Reference](http://java.sun.com/j2se/1.3/docs/guide/security/CryptoSpec.html#AppA) for information about standard algorithm
     * names.
     * @return An MD5 digest instance.
     * @see MessageDigest.getInstance
     * @throws RuntimeException
     * when a [NoSuchAlgorithmException] is caught.
     */
    fun getDigest(algorithm: String): MessageDigest {
        try {
            return MessageDigest.getInstance(algorithm)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e.message)
        }
    }

    private val md5Digest: MessageDigest
        /**
         * Returns an MD5 MessageDigest.
         * 
         * @return An MD5 digest instance.
         * @throws RuntimeException
         * when a [NoSuchAlgorithmException] is caught.
         */
        get() = getDigest("MD5")

    private val sha256Digest: MessageDigest
        /**
         * Returns an SHA-256 digest.
         * 
         * 
         * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
         * 
         * 
         * @return An SHA-256 digest instance.
         * @throws RuntimeException
         * when a [NoSuchAlgorithmException] is caught.
         */
        get() = getDigest("SHA-256")

    private val sha384Digest: MessageDigest
        /**
         * Returns an SHA-384 digest.
         * 
         * 
         * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
         * 
         * 
         * @return An SHA-384 digest instance.
         * @throws RuntimeException
         * when a [NoSuchAlgorithmException] is caught.
         */
        get() = getDigest("SHA-384")

    private val sha512Digest: MessageDigest
        /**
         * Returns an SHA-512 digest.
         * 
         * 
         * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
         * 
         * 
         * @return An SHA-512 digest instance.
         * @throws RuntimeException
         * when a [NoSuchAlgorithmException] is caught.
         */
        get() = getDigest("SHA-512")

    private val shaDigest: MessageDigest
        /**
         * Returns an SHA-1 digest.
         * 
         * @return An SHA-1 digest instance.
         * @throws RuntimeException
         * when a [NoSuchAlgorithmException] is caught.
         */
        get() = getDigest("SHA")

    /**
     * Calculates the MD5 digest and returns the value as a 16 element `byte[]`.
     * 
     * @param data
     * Data to digest
     * @return MD5 digest
     */
    @JvmStatic
    fun md5(data: ByteArray): ByteArray {
        return md5Digest.digest(data)
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element `byte[]`.
     * 
     * @param data
     * Data to digest
     * @return MD5 digest
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun md5(data: InputStream): ByteArray {
        return digest(md5Digest, data)
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element `byte[]`.
     * 
     * @param data
     * Data to digest
     * @return MD5 digest
     */
    fun md5(data: String?): ByteArray {
        return DigestUtils.md5(getBytesUtf8(data)!!)
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     * 
     * @param data
     * Data to digest
     * @return MD5 digest as a hex string
     */
    fun md5Hex(data: ByteArray): String {
        return Hex.Companion.encodeHexString(md5(data))
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     * 
     * @param data
     * Data to digest
     * @return MD5 digest as a hex string
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun md5Hex(data: InputStream): String {
        return Hex.Companion.encodeHexString(md5(data))
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     * 
     * @param data
     * Data to digest
     * @return MD5 digest as a hex string
     */
    fun md5Hex(data: String?): String {
        return Hex.Companion.encodeHexString(md5(data))
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a `byte[]`.
     * 
     * @param data
     * Data to digest
     * @return SHA-1 digest
     */
    fun sha(data: ByteArray): ByteArray {
        return shaDigest.digest(data)
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a `byte[]`.
     * 
     * @param data
     * Data to digest
     * @return SHA-1 digest
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun sha(data: InputStream): ByteArray {
        return digest(shaDigest, data)
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a `byte[]`.
     * 
     * @param data
     * Data to digest
     * @return SHA-1 digest
     */
    fun sha(data: String?): ByteArray {
        return DigestUtils.sha(getBytesUtf8(data)!!)
    }

    /**
     * Calculates the SHA-256 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-256 digest
     * @since 1.4
     */
    fun sha256(data: ByteArray): ByteArray {
        return sha256Digest.digest(data)
    }

    /**
     * Calculates the SHA-256 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-256 digest
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun sha256(data: InputStream): ByteArray {
        return digest(sha256Digest, data)
    }

    /**
     * Calculates the SHA-256 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-256 digest
     * @since 1.4
     */
    fun sha256(data: String?): ByteArray {
        return DigestUtils.sha256(getBytesUtf8(data)!!)
    }

    /**
     * Calculates the SHA-256 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-256 digest as a hex string
     * @since 1.4
     */
    fun sha256Hex(data: ByteArray): String {
        return Hex.Companion.encodeHexString(sha256(data))
    }

    /**
     * Calculates the SHA-256 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-256 digest as a hex string
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun sha256Hex(data: InputStream): String {
        return Hex.Companion.encodeHexString(sha256(data))
    }

    /**
     * Calculates the SHA-256 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-256 digest as a hex string
     * @since 1.4
     */
    fun sha256Hex(data: String?): String {
        return Hex.Companion.encodeHexString(sha256(data))
    }

    /**
     * Calculates the SHA-384 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-384 digest
     * @since 1.4
     */
    fun sha384(data: ByteArray): ByteArray {
        return sha384Digest.digest(data)
    }

    /**
     * Calculates the SHA-384 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-384 digest
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun sha384(data: InputStream): ByteArray {
        return digest(sha384Digest, data)
    }

    /**
     * Calculates the SHA-384 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-384 digest
     * @since 1.4
     */
    fun sha384(data: String?): ByteArray {
        return DigestUtils.sha384(getBytesUtf8(data)!!)
    }

    /**
     * Calculates the SHA-384 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-384 digest as a hex string
     * @since 1.4
     */
    fun sha384Hex(data: ByteArray): String {
        return Hex.Companion.encodeHexString(sha384(data))
    }

    /**
     * Calculates the SHA-384 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-384 digest as a hex string
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun sha384Hex(data: InputStream): String {
        return Hex.Companion.encodeHexString(sha384(data))
    }

    /**
     * Calculates the SHA-384 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-384 digest as a hex string
     * @since 1.4
     */
    fun sha384Hex(data: String?): String {
        return Hex.Companion.encodeHexString(sha384(data))
    }

    /**
     * Calculates the SHA-512 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-512 digest
     * @since 1.4
     */
    fun sha512(data: ByteArray): ByteArray {
        return sha512Digest.digest(data)
    }

    /**
     * Calculates the SHA-512 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-512 digest
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun sha512(data: InputStream): ByteArray {
        return digest(sha512Digest, data)
    }

    /**
     * Calculates the SHA-512 digest and returns the value as a `byte[]`.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-512 digest
     * @since 1.4
     */
    fun sha512(data: String?): ByteArray {
        return DigestUtils.sha512(getBytesUtf8(data)!!)
    }

    /**
     * Calculates the SHA-512 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-512 digest as a hex string
     * @since 1.4
     */
    fun sha512Hex(data: ByteArray): String {
        return Hex.Companion.encodeHexString(sha512(data))
    }

    /**
     * Calculates the SHA-512 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-512 digest as a hex string
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun sha512Hex(data: InputStream): String {
        return Hex.Companion.encodeHexString(sha512(data))
    }

    /**
     * Calculates the SHA-512 digest and returns the value as a hex string.
     * 
     * 
     * Throws a `RuntimeException` on JRE versions prior to 1.4.0.
     * 
     * 
     * @param data
     * Data to digest
     * @return SHA-512 digest as a hex string
     * @since 1.4
     */
    fun sha512Hex(data: String?): String {
        return Hex.Companion.encodeHexString(sha512(data))
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a hex string.
     * 
     * @param data
     * Data to digest
     * @return SHA-1 digest as a hex string
     */
    fun shaHex(data: ByteArray): String {
        return Hex.Companion.encodeHexString(sha(data))
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a hex string.
     * 
     * @param data
     * Data to digest
     * @return SHA-1 digest as a hex string
     * @throws IOException
     * On error reading from the stream
     * @since 1.4
     */
    @Throws(IOException::class)
    fun shaHex(data: InputStream): String {
        return Hex.Companion.encodeHexString(sha(data))
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a hex string.
     * 
     * @param data
     * Data to digest
     * @return SHA-1 digest as a hex string
     */
    fun shaHex(data: String?): String {
        return Hex.Companion.encodeHexString(sha(data))
    }
}

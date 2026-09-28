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
package com.wxiwei.office.fc.hssf.record.crypto

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.min

class Biff8EncryptionKey internal constructor(keyDigest: ByteArray) {
    private val _keyDigest: ByteArray

    /**
     * @return `true` if the keyDigest is compatible with the specified saltData and saltHash
     */
    fun validate(saltData: ByteArray, saltHash: ByteArray): Boolean {
        check16Bytes(saltData, "saltData")
        check16Bytes(saltHash, "saltHash")

        // validation uses the RC4 for block zero
        val rc4 = createRC4(0)
        val saltDataPrime = saltData.clone()
        rc4.encrypt(saltDataPrime)

        val saltHashPrime = saltHash.clone()
        rc4.encrypt(saltHashPrime)

        val md5: MessageDigest?
        try {
            md5 = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        md5.update(saltDataPrime)
        val finalSaltResult = md5.digest()

        if (false) { // set true to see a valid saltHash value
            val saltHashThatWouldWork: ByteArray =
                xor(saltHash, xor(saltHashPrime, finalSaltResult))
            println(toHex(saltHashThatWouldWork))
        }

        return saltHashPrime.contentEquals(finalSaltResult)
    }

    /**
     * The [RC4] instance needs to be changed every 1024 bytes.
     * @param keyBlockNo used to seed the newly created [RC4]
     */
    fun createRC4(keyBlockNo: Int): RC4 {
        val md5: MessageDigest?
        try {
            md5 = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

        md5.update(_keyDigest)
        val baos = ByteArrayOutputStream(4)
        LittleEndianOutputStream(baos).writeInt(keyBlockNo)
        md5.update(baos.toByteArray())

        val digest = md5.digest()
        return RC4(digest)
    }


    init {
        require(keyDigest.size == KEY_DIGEST_LENGTH) {
            "Expected 5 byte key digest, but got " + toHex(
                keyDigest
            )
        }
        _keyDigest = keyDigest
    }

    companion object {
        // these two constants coincidentally have the same value
        private const val KEY_DIGEST_LENGTH = 5
        private const val PASSWORD_HASH_NUMBER_OF_BYTES_USED = 5

        /**
         * Create using the default password and a specified docId
         * @param docId 16 bytes
         */
        @JvmStatic
        fun create(docId: ByteArray): Biff8EncryptionKey {
            return Biff8EncryptionKey(createKeyDigest("VelvetSweatshop", docId))
        }

        @JvmStatic
        fun create(password: String, docIdData: ByteArray): Biff8EncryptionKey {
            return Biff8EncryptionKey(createKeyDigest(password, docIdData))
        }

        fun createKeyDigest(password: String, docIdData: ByteArray): ByteArray {
            check16Bytes(docIdData, "docId")
            val nChars = min(password.length, 16)
            val passwordData = ByteArray(nChars * 2)
            for (i in 0..<nChars) {
                val ch = password.get(i)
                passwordData[i * 2 + 0] = ((ch.code shl 0) and 0xFF).toByte()
                passwordData[i * 2 + 1] = ((ch.code shl 8) and 0xFF).toByte()
            }

            val kd: ByteArray?
            val md5: MessageDigest?
            try {
                md5 = MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

            md5.update(passwordData)
            val passwordHash = md5.digest()
            md5.reset()

            for (i in 0..15) {
                md5.update(passwordHash, 0, PASSWORD_HASH_NUMBER_OF_BYTES_USED)
                md5.update(docIdData, 0, docIdData.size)
            }
            kd = md5.digest()
            val result = ByteArray(KEY_DIGEST_LENGTH)
            System.arraycopy(kd, 0, result, 0, KEY_DIGEST_LENGTH)
            return result
        }

        private fun xor(a: ByteArray, b: ByteArray): ByteArray {
            val c = ByteArray(a.size)
            for (i in c.indices) {
                c[i] = (a[i].toInt() xor b[i].toInt()).toByte()
            }
            return c
        }

        private fun check16Bytes(data: ByteArray, argName: String?) {
            require(data.size == 16) { "Expected 16 byte " + argName + ", but got " + toHex(data) }
        }

        /**
         * Stores the BIFF8 encryption/decryption password for the current thread.  This has been done
         * using a [ThreadLocal] in order to avoid further overloading the various public APIs
         * (e.g. [HSSFWorkbook]) that need this functionality.
         */
        private val _userPasswordTLS = ThreadLocal<String?>()

        @JvmStatic
        @get:JvmName("getCurrentUserPasswordProperty")
        @set:JvmName("setCurrentUserPasswordProperty")
        var currentUserPassword: String?
            get() = _userPasswordTLS.get()
            set(password) {
                _userPasswordTLS.set(password)
            }

        @JvmStatic
        fun setCurrentUserPassword(password: String?) {
            currentUserPassword = password
        }

        @JvmStatic
        fun getCurrentUserPassword(): String? = currentUserPassword
    }
}

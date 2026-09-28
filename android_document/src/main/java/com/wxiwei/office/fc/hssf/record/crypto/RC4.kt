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

import com.wxiwei.office.fc.util.HexDump.dump

/**
 * Simple implementation of the alleged RC4 algorithm.
 * 
 * Inspired by <A HREF="http://en.wikipedia.org/wiki/RC4">wikipedia's RC4 article</A>
 * 
 * @author Josh Micich
 */
class RC4(key: ByteArray) {
    private var _i: Int
    private var _j: Int
    private val _s = ByteArray(256)

    init {
        val key_length = key.size

        for (i in 0..255) _s[i] = i.toByte()

        var i = 0
        var j = 0
        while (i < 256) {
            val temp: Byte

            j = (j + key[i % key_length] + _s[i]) and 255
            temp = _s[i]
            _s[i] = _s[j]
            _s[j] = temp
            i++
        }

        _i = 0
        _j = 0
    }

    fun output(): Byte {
        val temp: Byte
        _i = (_i + 1) and 255
        _j = (_j + _s[_i]) and 255

        temp = _s[_i]
        _s[_i] = _s[_j]
        _s[_j] = temp

        return _s[(_s[_i] + _s[_j]) and 255]
    }

    fun encrypt(`in`: ByteArray) {
        for (i in `in`.indices) {
            `in`[i] = (`in`[i].toInt() xor output().toInt()).toByte()
        }
    }

    fun encrypt(`in`: ByteArray, offset: Int, len: Int) {
        val end = offset + len
        for (i in offset..<end) {
            `in`[i] = (`in`[i].toInt() xor output().toInt()).toByte()
        }
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append(javaClass.getName()).append(" [")
        sb.append("i=").append(_i)
        sb.append(" j=").append(_j)
        sb.append("]")
        sb.append("\n")
        sb.append(dump(_s, 0, 0))

        return sb.toString()
    }
}

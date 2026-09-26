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

package com.wxiwei.office.constant.fc

import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil

/**
 * To support Constant Values (2.5.7) as required by the CRN record.
 * This class is also used for two dimensional arrays which are encoded by
 * EXTERNALNAME (5.39) records and Array tokens.
 *
 * @author Josh Micich
 */
object ConstantValueParser {
    // note - these (non-combinable) enum values are sparse.
    private const val TYPE_EMPTY = 0
    private const val TYPE_NUMBER = 1
    private const val TYPE_STRING = 2
    private const val TYPE_BOOLEAN = 4
    private const val TYPE_ERROR_CODE = 16 // TODO - update OOO document to include this value

    private const val TRUE_ENCODING = 1
    private const val FALSE_ENCODING = 0

    // TODO - is this the best way to represent 'EMPTY'?
    private val EMPTY_REPRESENTATION: Any? = null

    @JvmStatic
    fun parse(`in`: LittleEndianInput, nValues: Int): Array<Any?> {
        return Array(nValues) { readAConstantValue(`in`) }
    }

    private fun readAConstantValue(`in`: LittleEndianInput): Any? {
        val grbit = `in`.readByte()
        when (grbit.toInt()) {
            TYPE_EMPTY -> {
                `in`.readLong() // 8 byte 'not used' field
                return EMPTY_REPRESENTATION
            }
            TYPE_NUMBER -> return `in`.readDouble()
            TYPE_STRING -> return StringUtil.readUnicodeString(`in`)
            TYPE_BOOLEAN -> return readBoolean(`in`)
            TYPE_ERROR_CODE -> {
                val errCode = `in`.readUShort()
                // next 6 bytes are unused
                `in`.readUShort()
                `in`.readInt()
                return ErrorConstant.valueOf(errCode)
            }
        }
        throw RuntimeException("Unknown grbit value ($grbit)")
    }

    private fun readBoolean(`in`: LittleEndianInput): Any {
        val value = `in`.readLong().toByte() // 7 bytes 'not used'
        when (value.toInt()) {
            FALSE_ENCODING -> return java.lang.Boolean.FALSE
            TRUE_ENCODING -> return java.lang.Boolean.TRUE
        }
        // Don't tolerate unusual boolean encoded values (unless it becomes evident that they occur)
        throw RuntimeException("unexpected boolean encoding ($value)")
    }

    @JvmStatic
    fun getEncodedSize(values: Array<Any?>): Int {
        // start with one byte 'type' code for each value
        var result = values.size * 1
        for (value in values) {
            result += getEncodedSize(value)
        }
        return result
    }

    /**
     * @return encoded size without the 'type' code byte
     */
    private fun getEncodedSize(any: Any?): Int {
        if (any == EMPTY_REPRESENTATION) {
            return 8
        }
        val cls = any!!.javaClass

        if (cls == java.lang.Boolean::class.java || cls == java.lang.Double::class.java || cls == ErrorConstant::class.java) {
            return 8
        }
        val strVal = any as String
        return StringUtil.getEncodedSize(strVal)
    }

    @JvmStatic
    fun encode(out: LittleEndianOutput, values: Array<Any?>) {
        for (value in values) {
            encodeSingleValue(out, value)
        }
    }

    private fun encodeSingleValue(out: LittleEndianOutput, value: Any?) {
        if (value == EMPTY_REPRESENTATION) {
            out.writeByte(TYPE_EMPTY)
            out.writeLong(0L)
            return
        }
        if (value is Boolean) {
            out.writeByte(TYPE_BOOLEAN)
            val longVal = if (value) 1L else 0L
            out.writeLong(longVal)
            return
        }
        if (value is Double) {
            out.writeByte(TYPE_NUMBER)
            out.writeDouble(value)
            return
        }
        if (value is String) {
            out.writeByte(TYPE_STRING)
            StringUtil.writeUnicodeString(out, value)
            return
        }
        if (value is ErrorConstant) {
            out.writeByte(TYPE_ERROR_CODE)
            val longVal = value.errorCode.toLong()
            out.writeLong(longVal)
            return
        }

        throw IllegalStateException("Unexpected value type (" + value!!.javaClass.name + "'")
    }
}

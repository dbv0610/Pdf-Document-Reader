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
package com.wxiwei.office.fc.ddf

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.util.Collections

/**
 * Common abstract class for [EscherOptRecord] and
 * [EscherTertiaryOptRecord]
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 * @author Glen Stampoultzis
 */
abstract class AbstractEscherOptRecord : EscherRecord() {
    /**
     * The list of properties stored by this record.
     */
    @get:JvmName("getEscherPropertiesProperty")
    var escherProperties: MutableList<EscherProperty> = ArrayList<EscherProperty>()
        protected set

    fun getEscherProperties(): MutableList<EscherProperty> = escherProperties

    /**
     * Add a property to this record.
     */
    fun addEscherProperty(prop: EscherProperty?) {
        escherProperties.add(prop!!)
    }

    override fun fillFields(
        data: ByteArray?, offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesRemaining = readHeader(data, offset)
        val pos = offset + 8

        val f = EscherPropertyFactory()
        this.escherProperties = f.createProperties(data, pos, instance)
        return bytesRemaining + 8
    }

    /**
     * The list of properties stored by this record.
     */
    fun getEscherProperty(index: Int): EscherProperty? {
        return escherProperties.get(index)
    }

    private val propertiesSize: Int
        get() {
            var totalSize = 0
            for (property in this.escherProperties) {
                totalSize += property.propertySize
            }

            return totalSize
        }

    override val recordSize: Int
        get() {
        return 8 + this.propertiesSize
    }

    fun <T : EscherProperty?> lookup(propId: Int): T? {
        for (prop in this.escherProperties) {
            if (prop.propertyNumber.toInt() == propId) {
                val result = prop as T?
                return result
            }
        }
        return null
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
        listener.beforeRecordSerialize(offset, recordId, this)

        putShort(data, offset, options)
        putShort(data, offset + 2, recordId)
        putInt(data, offset + 4, this.propertiesSize)
        var pos = offset + 8
        for (property in this.escherProperties) {
            pos += property.serializeSimplePart(data, pos)
        }
        for (property in this.escherProperties) {
            pos += property.serializeComplexPart(data, pos)
        }
        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
    }

    /**
     * Records should be sorted by property number before being stored.
     */
    fun sortProperties() {
        Collections.sort<EscherProperty>(
            this.escherProperties,
            object : Comparator<EscherProperty> {
                override fun compare(p1: EscherProperty, p2: EscherProperty): Int {
                    val s1 = p1.propertyNumber
                    val s2 = p2.propertyNumber
                    return if (s1 < s2) -1 else if (s1 == s2) 0 else 1
                }
            })
    }

    /**
     * Retrieve the string representation of this record.
     */
    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        val stringBuilder = StringBuilder()
        stringBuilder.append(javaClass.getName())
        stringBuilder.append(":")
        stringBuilder.append(nl)
        stringBuilder.append("  isContainer: ")
        stringBuilder.append(isContainerRecord)
        stringBuilder.append(nl)
        stringBuilder.append("  options: 0x")
        stringBuilder.append(toHex(options))
        stringBuilder.append(nl)
        stringBuilder.append("  recordId: 0x")
        stringBuilder.append(toHex(recordId))
        stringBuilder.append(nl)
        stringBuilder.append("  numchildren: ")
        stringBuilder.append(childRecords.size)
        stringBuilder.append(nl)
        stringBuilder.append("  properties:")
        stringBuilder.append(nl)

        for (property in this.escherProperties) {
            stringBuilder.append("    " + property.toString() + nl)
        }

        return stringBuilder.toString()
    }
}

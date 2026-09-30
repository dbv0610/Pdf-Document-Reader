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

import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort


/**
 * Generates a property given a reference into the byte array storing that property.
 * 
 * @author Glen Stampoultzis
 */
class EscherPropertyFactory {
    /**
     * Create new properties from a byte array.
     * 
     * @param data              The byte array containing the property
     * @param offset            The starting offset into the byte array
     * @return                  The new properties
     */
    fun createProperties(
        data: ByteArray,
        offset: Int,
        numProperties: Short
    ): MutableList<EscherProperty> {
        val results: MutableList<EscherProperty> = ArrayList<EscherProperty>()

        var pos = offset

        //        while ( bytesRemaining >= 6 )
        for (i in 0..<numProperties) {
            val propId: Short
            val propData: Int
            propId = getShort(data, pos)
            propData = getInt(data, pos + 2)
            val propNumber = (propId.toInt() and 0x3FFF.toShort().toInt()).toShort()
            val isComplex = (propId.toInt() and 0x8000.toShort().toInt()) != 0
            val isBlipId = (propId.toInt() and 0x4000.toShort().toInt()) != 0

            val propertyType = EscherProperties.getPropertyType(propNumber)
            if (propertyType == EscherPropertyMetaData.Companion.TYPE_BOOLEAN) results.add(
                EscherBoolProperty(propId, propData)
            )
            else if (propertyType == EscherPropertyMetaData.Companion.TYPE_RGB) results.add(
                EscherRGBProperty(propId, propData)
            )
            else if (propertyType == EscherPropertyMetaData.Companion.TYPE_SHAPEPATH) results.add(
                EscherShapePathProperty(propId, propData)
            )
            else {
                if (!isComplex) results.add(EscherSimpleProperty(propId, propData))
                else {
                    if (propertyType == EscherPropertyMetaData.Companion.TYPE_ARRAY) results.add(
                        EscherArrayProperty(propId, ByteArray(propData))
                    )
                    else results.add(EscherComplexProperty(propId, ByteArray(propData)))
                }
            }
            pos += 6
            //            bytesRemaining -= 6 + complexBytes;
        }

        // Get complex data
        val iterator = results.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p is EscherComplexProperty) {
                if (p is EscherArrayProperty) {
                    pos += p.setArrayData(data, pos)
                } else {
                    val complexData = p.complexData
                    System.arraycopy(data, pos, complexData, 0, complexData.size)
                    pos += complexData.size
                }
            }
        }
        return results
    }
}

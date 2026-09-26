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
package com.wxiwei.office.fc.hssf.util

/**
 * Utility for delaying the concatenation of multiple byte arrays.  Doing this up-front
 * causes significantly more copying, which for a large number of byte arrays can cost
 * a large amount of time.
 */
class LazilyConcatenatedByteArray {
    private val arrays: MutableList<ByteArray> = ArrayList<ByteArray>(1)

    /**
     * Clears the array (sets the concatenated length back to zero.
     */
    fun clear() {
        arrays.clear()
    }

    /**
     * Concatenates an array onto the end of our array.
     * This is a relatively fast operation.
     * 
     * @param array the array to concatenate.
     * @throws IllegalArgumentException if `array` is `null`.
     */
    fun concatenate(array: ByteArray) {
        requireNotNull(array) { "array cannot be null" }
        arrays.add(array)
    }

    /**
     * Gets the concatenated contents as a single byte array.
     * 
     * This is a slower operation, but the concatenated array is stored off as a single
     * array again so that subsequent calls will not perform additional copying.
     * 
     * @return the byte array.  Returns `null` if no data has been placed into it.
     */
    fun toArray(): ByteArray? {
        if (arrays.isEmpty()) {
            return null
        } else if (arrays.size > 1) {
            var totalLength = 0
            for (array in arrays) {
                totalLength += array.size
            }

            val concatenated = ByteArray(totalLength)
            var destPos = 0
            for (array in arrays) {
                System.arraycopy(array, 0, concatenated, destPos, array.size)
                destPos += array.size
            }

            arrays.clear()
            arrays.add(concatenated)
        }

        return arrays.get(0)
    }
}


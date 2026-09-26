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
package com.wxiwei.office.fc.hssf.formula

import kotlin.math.abs

/**
 * A custom implementation of [java.util.HashSet] in order to reduce memory consumption.
 * 
 * Profiling tests (Oct 2008) have shown that each element [FormulaCellCacheEntry] takes
 * around 32 bytes to store in a HashSet, but around 6 bytes to store here.  For Spreadsheets with
 * thousands of formula cells with multiple interdependencies, the savings can be very significant.
 * 
 * @author Josh Micich
 */
internal class FormulaCellCacheEntrySet {
    private var _size = 0
    private var _arr: Array<FormulaCellCacheEntry?>

    init {
        _arr = EMPTY_ARRAY
    }

    fun toArray(): Array<FormulaCellCacheEntry> {
        val nItems = _size
        if (nItems < 1) {
            return EMPTY_RESULT
        }
        val result = arrayOfNulls<FormulaCellCacheEntry>(nItems)
        var j = 0
        for (i in _arr.indices) {
            val cce = _arr[i]
            if (cce != null) {
                result[j++] = cce
            }
        }
        check(j == nItems) { "size mismatch" }
        return result.requireNoNulls()
    }


    fun add(cce: FormulaCellCacheEntry) {
        if (_size * 3 >= _arr.size * 2) {
            // re-hash
            val prevArr = _arr
            val newArr = arrayOfNulls<FormulaCellCacheEntry>(4 + _arr.size * 3 / 2) // grow 50%
            for (i in prevArr.indices) {
                val prevCce = _arr[i]
                if (prevCce != null) {
                    Companion.addInternal(newArr, prevCce)
                }
            }
            _arr = newArr
        }
        if (Companion.addInternal(_arr, cce)) {
            _size++
        }
    }


    fun remove(cce: FormulaCellCacheEntry): Boolean {
        val arr = _arr

        if (_size * 3 < _arr.size && _arr.size > 8) {
            // re-hash
            var found = false
            val prevArr = _arr
            val newArr = arrayOfNulls<FormulaCellCacheEntry>(_arr.size / 2) // shrink 50%
            for (i in prevArr.indices) {
                val prevCce = _arr[i]
                if (prevCce != null) {
                    if (prevCce == cce) {
                        found = true
                        _size--
                        // skip it
                        continue
                    }
                    Companion.addInternal(newArr, prevCce)
                }
            }
            _arr = newArr
            return found
        }

        // else - usual case
        // delete single element (without re-hashing)
        val startIx = abs(cce.hashCode() % arr.size)

        // note - can't exit loops upon finding null because of potential previous deletes
        for (i in startIx..<arr.size) {
            val item = arr[i]
            if (item == cce) {
                // found it
                arr[i] = null
                _size--
                return true
            }
        }
        for (i in 0..<startIx) {
            val item = arr[i]
            if (item == cce) {
                // found it
                arr[i] = null
                _size--
                return true
            }
        }
        return false
    }

    companion object {
        private val EMPTY_ARRAY = arrayOf<FormulaCellCacheEntry?>()
        private val EMPTY_RESULT = arrayOf<FormulaCellCacheEntry>()

        private fun addInternal(arr: Array<FormulaCellCacheEntry?>, cce: FormulaCellCacheEntry): Boolean {
            val startIx = abs(cce.hashCode() % arr.size)

            for (i in startIx..<arr.size) {
                val item = arr[i]
                if (item === cce) {
                    // already present
                    return false
                }
                if (item == null) {
                    arr[i] = cce
                    return true
                }
            }
            for (i in 0..<startIx) {
                val item = arr[i]
                if (item === cce) {
                    // already present
                    return false
                }
                if (item == null) {
                    arr[i] = cce
                    return true
                }
            }
            throw IllegalStateException("No empty space found")
        }
    }
}

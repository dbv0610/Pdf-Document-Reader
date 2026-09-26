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


package com.wxiwei.office.fc.util

/**
 * A List of objects that are indexed AND keyed by an int; also allows for getting
 * the index of a value in the list
 *
 * I am happy is someone wants to re-implement this without using the
 * internal list and hashmap. If so could you please make sure that
 * you can add elements half way into the list and have the value-key mappings
 * update
 *
 *
 * @author Jason Height
 */
open class IntMapper<T>(initialCapacity: Int) {
    private val elements: MutableList<T> = ArrayList(initialCapacity)
    private val valueKeyMap: MutableMap<T, Int> = HashMap(initialCapacity)

    /**
     * create an IntMapper of default size
     */
    constructor() : this(_default_size)

    /**
     * Appends the specified element to the end of this list
     *
     * @param value element to be appended to this list.
     *
     * @return true (as per the general contract of the Collection.add
     *         method).
     */
    open fun add(value: T): Boolean {
        val index = elements.size
        elements.add(value)
        valueKeyMap[value] = index
        return true
    }

    open fun size(): Int {
        return elements.size
    }

    open fun get(index: Int): T {
        return elements[index]
    }

    open fun getIndex(o: T): Int {
        val i = valueKeyMap[o]
        if (i == null) {
            return -1
        }
        return i.toInt()
    }

    open fun iterator(): MutableIterator<T> {
        return elements.iterator()
    }

    companion object {
        private const val _default_size = 10
    }
} // end public class IntMapper

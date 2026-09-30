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

/**
 * Compares Strings using a [StringEncoder]. This comparator is used to sort Strings by an encoding scheme such as
 * Soundex, Metaphone, etc. This class can come in handy if one need to sort Strings by an encoded form of a name such
 * as Soundex.
 * 
 * @author Apache Software Foundation
 * @version $Id: StringEncoderComparator.java 1080701 2011-03-11 17:52:27Z ggregory $
 */
class StringEncoderComparator : Comparator<Any?> {
    /**
     * Internal encoder instance.
     */
    private val stringEncoder: StringEncoder?

    /**
     * Constructs a new instance.
     * 
     */
    @Deprecated(
        """Creating an instance without a {@link StringEncoder} leads to a {@link NullPointerException}. Will be
                  removed in 2.0."""
    )
    constructor() {
        this.stringEncoder = null // Trying to use this will cause things to break
    }

    /**
     * Constructs a new instance with the given algorithm.
     * 
     * @param stringEncoder
     * the StringEncoder used for comparisons.
     */
    constructor(stringEncoder: StringEncoder?) {
        this.stringEncoder = stringEncoder
    }

    /**
     * Compares two strings based not on the strings themselves, but on an encoding of the two strings using the
     * StringEncoder this Comparator was created with.
     * 
     * If an [EncoderException] is encountered, return `0`.
     * 
     * @param o1
     * the object to compare
     * @param o2
     * the object to compare to
     * @return the Comparable.compareTo() return code or 0 if an encoding error was caught.
     * @see Comparable
     */
    override fun compare(o1: Any?, o2: Any?): Int {
        var compareCode = 0

        try {
            @Suppress("UNCHECKED_CAST")
            val s1 = this.stringEncoder!!.encode(o1) as Comparable<Any?>
            val s2 = this.stringEncoder.encode(o2)
            compareCode = s1.compareTo(s2)
        } catch (ee: EncoderException) {
            compareCode = 0
        }
        return compareCode
    }
}

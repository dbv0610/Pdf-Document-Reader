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
package com.wxiwei.office.fc.hssf.record

import java.io.ByteArrayInputStream

/**
 * Title: Record
 * Description: All HSSF Records inherit from this class.
 * @author Andrew C. Oliver
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
abstract class Record protected constructor() : RecordBase(), Cloneable {
    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     * 
     * @return byte array containing instance data
     */
    fun serialize(): ByteArray {
        val retval = ByteArray(getRecordSize())

        serialize(0, retval)
        return retval
    }

    /**
     * get a string representation of the record (for biffview/debugging)
     */
    override fun toString(): String {
        return super.toString()
    }

    /**
     * return the non static version of the id for this record.
     */
    abstract fun getSid(): Short

    public override fun clone(): Any {
        if (false) {
            // TODO - implement clone in a more standardised way
            try {
                return super.clone()
            } catch (e: CloneNotSupportedException) {
                throw RuntimeException(e)
            }
        }
        throw RuntimeException("The class " + javaClass.getName() + " needs to define a clone method")
    }

    /**
     * Clone the current record, via a call to serialize
     * it, and another to create a new record from the
     * bytes.
     * May only be used for classes which don't have
     * internal counts / ids in them. For those which
     * do, a full model-aware cloning is needed, which
     * allocates new ids / counts as needed.
     */
    fun cloneViaReserialise(): Record {
        // Do it via a re-serialization
        // It's a cheat, but it works...
        val b = serialize()
        val rinp = RecordInputStream(ByteArrayInputStream(b))
        rinp.nextRecord()

        val r = RecordFactory.createRecord(rinp)
        check(r.size == 1) { "Re-serialised a record to clone it, but got " + r.size + " records back!" }
        return r[0]!!
    }
}

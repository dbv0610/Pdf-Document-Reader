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
package com.wxiwei.office.fc.hssf.record.aggregates

import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase

/**
 * <tt>RecordAggregate</tt>s are groups of of BIFF <tt>Record</tt>s that are typically stored
 * together and/or updated together.  Workbook / Sheet records are typically stored in a sequential
 * list, which does not provide much structure to coordinate updates.
 * 
 * @author Josh Micich
 */
abstract class RecordAggregate : RecordBase() {
    /**
     * Visit each of the atomic BIFF records contained in this [RecordAggregate] in the order
     * that they should be written to file.  Implementors may or may not return the actual
     * [Record]s being used to manage POI's internal implementation.  Callers should not
     * assume either way, and therefore only attempt to modify those [Record]s after cloning
     */
    abstract fun visitContainedRecords(rv: RecordVisitor)

    override fun serialize(offset: Int, data: ByteArray): Int {
        val srv = SerializingRecordVisitor(data, offset)
        visitContainedRecords(srv)
        return srv.countBytesWritten()
    }

    override fun getRecordSize(): Int {
        val rsv = RecordSizingVisitor()
        visitContainedRecords(rsv)
        return rsv.totalSize
    }

    interface RecordVisitor {
        /**
         * Implementors may call non-mutating methods on Record r.
         * @param r must not be `null`
         */
        fun visitRecord(r: Record)
    }

    private class SerializingRecordVisitor(
        private val _data: ByteArray,
        private val _startOffset: Int
    ) : RecordVisitor {
        private var _countBytesWritten = 0

        fun countBytesWritten(): Int {
            return _countBytesWritten
        }

        override fun visitRecord(r: Record) {
            val currentOffset = _startOffset + _countBytesWritten
            _countBytesWritten += r.serialize(currentOffset, _data)
        }
    }

    private class RecordSizingVisitor : RecordVisitor {
        var totalSize: Int = 0
            private set

        override fun visitRecord(r: Record) {
            this.totalSize += r.getRecordSize()
        }
    }

    /**
     * A wrapper for [RecordVisitor] which accumulates the sizes of all
     * records visited.
     */
    class PositionTrackingVisitor(private val _rv: RecordVisitor, var position: Int) :
        RecordVisitor {
        override fun visitRecord(r: Record) {
            this.position += r.getRecordSize()
            _rv.visitRecord(r)
        }
    }
}

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
import java.io.PrintWriter


/**
 * The base abstract record from which all escher records are defined.  Subclasses will need
 * to define methods for serialization/deserialization and for determining the record size.
 * 
 * @author Glen Stampoultzis
 */
abstract class EscherRecord
/**
 * Create a new instance
 */
    : Cloneable {
    /**
     * @return The options field for this record.  All records have one.
     */
    /**
     * Set the options this this record.  Container records should have the
     * last nibble set to 0xF.
     */
    open var options: Short = 0
    /**
     * Return the current record id.
     * 
     * @return  The 16 bit record id.
     */
    /**
     * Sets the record id for this record.
     */
    open var recordId: Short = 0

    /**
     * Delegates to fillFields(byte[], int, EscherRecordFactory)
     * 
     * @see .fillFields
     */
    protected fun fillFields(data: ByteArray?, f: EscherRecordFactory?): Int {
        return fillFields(data, 0, f)
    }

    /**
     * The contract of this method is to deserialize an escher record including
     * it's children.
     * 
     * @param data      The byte array containing the serialized escher
     * records.
     * @param offset    The offset into the byte array.
     * @param recordFactory     A factory for creating new escher records.
     * @return          The number of bytes written.
     */
    abstract fun fillFields(data: ByteArray?, offset: Int, recordFactory: EscherRecordFactory?): Int

    /**
     * 
     */
    abstract fun dispose()

    /**
     * Reads the 8 byte header information and populates the `options`
     * and `recordId` records.
     * 
     * @param data      the byte array to read from
     * @param offset    the offset to start reading from
     * @return          the number of bytes remaining in this record.  This
     * may include the children if this is a container.
     */
    fun readHeader(data: ByteArray, offset: Int): Int {
        val header = EscherRecordHeader.readHeader(data, offset)
        this.options = header.options
        this.recordId = header.recordId
        return header.remainingBytes
    }

    val isContainerRecord: Boolean
        /**
         * Determine whether this is a container record by inspecting the option
         * field.
         * @return  true is this is a container field.
         */
        get() = (options.toInt() and 0x000f.toShort().toInt()) == 0x000f.toShort().toInt()

    /**
     * Serializes to a new byte array.  This is done by delegating to
     * serialize(int, byte[]);
     * 
     * @return  the serialized record.
     * @see .serialize
     */
    fun serialize(): ByteArray {
        val retval = ByteArray(this.recordSize)

        serialize(0, retval)
        return retval
    }

    /**
     * Serializes to an existing byte array without serialization listener.
     * This is done by delegating to serialize(int, byte[], EscherSerializationListener).
     * 
     * @param offset    the offset within the data byte array.
     * @param data      the data array to serialize to.
     * @return          The number of bytes written.
     * 
     * @see .serialize
     */
    fun serialize(offset: Int, data: ByteArray?): Int {
        return serialize(offset, data, NullEscherSerializationListener())
    }

    /**
     * Serializes the record to an existing byte array.
     * 
     * @param offset    the offset within the byte array
     * @param data      the data array to serialize to
     * @param listener  a listener for begin and end serialization events.  This
     * is useful because the serialization is
     * hierarchical/recursive and sometimes you need to be able
     * break into that.
     * @return the number of bytes written.
     */
    abstract fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int

    /**
     * Subclasses should effeciently return the number of bytes required to
     * serialize the record.
     * 
     * @return  number of bytes
     */
    abstract val recordSize: Int

    open var childRecords: MutableList<EscherRecord>
        /**
         * @return  Returns the children of this record.  By default this will
         * be an empty list.  EscherCotainerRecord is the only record
         * that may contain children.
         * 
         * @see EscherContainerRecord
         */
        get() = mutableListOf<EscherRecord>()
        /**
         * Sets the child records for this record.  By default this will throw
         * an exception as only EscherContainerRecords may have children.
         * 
         * @param childRecords  Not used in base implementation.
         */
        set(childRecords) {
            throw UnsupportedOperationException("This record does not support child records.")
        }

    /**
     * Escher records may need to be clonable in the future.
     */
    public override fun clone(): Any {
        throw RuntimeException("The class " + javaClass.getName() + " needs to define a clone method")
    }

    /**
     * Returns the indexed child record.
     */
    open fun getChild(index: Int): EscherRecord? {
        return this.childRecords.get(index)
    }

    /**
     * The display methods allows escher variables to print the record names
     * according to their hierarchy.
     * 
     * @param w         The print writer to output to.
     * @param indent    The current indent level.
     */
    open fun display(w: PrintWriter, indent: Int) {
        for (i in 0..<indent * 4) w.print(' ')
        w.println(this.recordName)
    }

    /**
     * Subclasses should return the short name for this escher record.
     */
    abstract val recordName: String?

    val instance: Short
        /**
         * Returns the instance part of the option record.
         * 
         * @return The instance part of the record
         */
        get() = (options.toInt() shr 4).toShort()

    /**
     * This class reads the standard escher header.
     */
    internal class EscherRecordHeader
    private constructor() {
        var options: Short = 0
            private set
        var recordId: Short = 0
            private set
        var remainingBytes: Int = 0
            private set

        override fun toString(): String {
            return "EscherRecordHeader{" +
                    "options=" + options +
                    ", recordId=" + recordId +
                    ", remainingBytes=" + remainingBytes +
                    "}"
        }

        companion object {
            fun readHeader(data: ByteArray, offset: Int): EscherRecordHeader {
                val header = EscherRecordHeader()
                header.options = getShort(data, offset)
                header.recordId = getShort(data, offset + 2)
                header.remainingBytes = getInt(data, offset + 4)
                return header
            }
        }
    }
}

package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.hslf.exceptions.CorruptPowerPointFileException
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.POILogFactory
import com.wxiwei.office.fc.util.POILogger
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList

abstract class Record {
    @JvmField
    protected var logger: POILogger = POILogFactory.getLogger(this.javaClass)

    abstract fun isAnAtom(): Boolean

    abstract fun getRecordType(): Long

    abstract fun getChildRecords(): Array<Record>?

    abstract fun dispose()

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun writeLittleEndian(i: Int, o: OutputStream) {
            val bi = ByteArray(4)
            LittleEndian.putInt(bi, i)
            o.write(bi)
        }

        @Throws(IOException::class)
        @JvmStatic
        fun writeLittleEndian(s: Short, o: OutputStream) {
            val bs = ByteArray(2)
            LittleEndian.putShort(bs, s)
            o.write(bs)
        }

        @JvmStatic
        fun buildRecordAtOffset(b: ByteArray, offset: Int): Record? {
            val type = LittleEndian.getUShort(b, offset + 2)
            val rlen = LittleEndian.getUInt(b, offset + 4)
            var rleni = rlen.toInt()
            if (rleni < 0) {
                rleni = 0
            }
            return createRecordForType(type.toLong(), b, offset, 8 + rleni)
        }

        @JvmStatic
        fun buildRecordAtOffset(b: ByteArray, offset: Int, myLastOnDiskOffset: Int): Record? {
            val type = LittleEndian.getUShort(b, offset + 2)
            val rlen = LittleEndian.getUInt(b, offset + 4)
            var rleni = rlen.toInt()
            if (rleni < 0) {
                rleni = 0
            }
            return createRecordForType(type.toLong(), b, offset, 8 + rleni, myLastOnDiskOffset)
        }

        @JvmStatic
        fun findChildRecords(b: ByteArray, start: Int, len: Int): Array<Record> {
            val children = ArrayList<Record>(5)
            var pos = start
            while (pos <= start + len - 8) {
                val type = LittleEndian.getUShort(b, pos + 2)
                val rlen = LittleEndian.getUInt(b, pos + 4)
                var rleni = rlen.toInt()
                if (rleni < 0) {
                    rleni = 0
                }
                if (pos == 0 && type == 0 && rleni == 0xffff) {
                    throw CorruptPowerPointFileException("Corrupt document - starts with record of type 0000 and length 0xFFFF")
                }
                val r = createRecordForType(type.toLong(), b, pos, 8 + rleni)
                if (r != null) {
                    children.add(r)
                }
                pos += 8
                pos += rleni
            }
            val nc = java.lang.reflect.Array.newInstance(Record::class.java, children.size) as Array<Record>
            return children.toArray(nc)
        }

        @JvmStatic
        fun createRecordForType(type: Long, b: ByteArray, start: Int, len: Int): Record? {
            if (start + len > b.size) {
                System.err.println("Warning: Skipping record of type " + type + " at position " + start + " which claims to be longer than the file! (" + len + " vs " + (b.size - start) + ")")
                return null
            }
            var c: Class<out Record>? = null
            var toReturn: Record? = null
            try {
                c = RecordTypes.recordHandlingClass(type.toInt())
                if (c == null) {
                    c = RecordTypes.recordHandlingClass(RecordTypes.Unknown.typeID)
                }
                val con = c!!.getDeclaredConstructor(ByteArray::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                toReturn = con.newInstance(b, start, len)
            } catch (ie: InstantiationException) {
                throw RuntimeException("Couldn't instantiate the class for type with id $type on class $c : $ie", ie)
            } catch (ite: InvocationTargetException) {
                throw RuntimeException("Couldn't instantiate the class for type with id $type on class $c : $ite\nCause was : " + ite.cause, ite)
            } catch (iae: IllegalAccessException) {
                throw RuntimeException("Couldn't access the constructor for type with id $type on class $c : $iae", iae)
            } catch (nsme: NoSuchMethodException) {
                throw RuntimeException("Couldn't access the constructor for type with id $type on class $c : $nsme", nsme)
            }

            if (toReturn is PositionDependentRecord) {
                (toReturn as PositionDependentRecord).setLastOnDiskOffset(start)
            }
            return toReturn
        }

        @JvmStatic
        fun createRecordForType(type: Long, b: ByteArray, start: Int, len: Int, myLastOnDiskOffset: Int): Record? {
            if (start + len > b.size) {
                System.err.println("Warning: Skipping record of type " + type + " at position " + start + " which claims to be longer than the file! (" + len + " vs " + (b.size - start) + ")")
                return null
            }
            var c: Class<out Record>? = null
            var toReturn: Record? = null
            try {
                c = RecordTypes.recordHandlingClass(type.toInt())
                if (c == null) {
                    c = RecordTypes.recordHandlingClass(RecordTypes.Unknown.typeID)
                }
                val con = c!!.getDeclaredConstructor(ByteArray::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                toReturn = con.newInstance(b, start, len)
            } catch (ie: InstantiationException) {
                throw RuntimeException("Couldn't instantiate the class for type with id $type on class $c : $ie", ie)
            } catch (ite: InvocationTargetException) {
                throw RuntimeException("Couldn't instantiate the class for type with id $type on class $c : $ite\nCause was : " + ite.cause, ite)
            } catch (iae: IllegalAccessException) {
                throw RuntimeException("Couldn't access the constructor for type with id $type on class $c : $iae", iae)
            } catch (nsme: NoSuchMethodException) {
                throw RuntimeException("Couldn't access the constructor for type with id $type on class $c : $nsme", nsme)
            }

            if (toReturn is PositionDependentRecord) {
                (toReturn as PositionDependentRecord).setLastOnDiskOffset(myLastOnDiskOffset)
            }
            return toReturn
        }
    }
}

package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.ArrayUtil
import java.util.ArrayList

abstract class RecordContainer : Record() {
    @JvmField
    var _children: Array<Record> = emptyArray()
    private val changingChildRecordsLock = Any()

    override fun getChildRecords(): Array<Record> {
        return _children
    }

    override fun isAnAtom(): Boolean {
        return false
    }

    private fun findChildLocation(child: Record): Int {
        synchronized(changingChildRecordsLock) {
            for (i in _children.indices) {
                if (_children[i] == child) {
                    return i
                }
            }
        }
        return -1
    }

    private fun appendChild(newChild: Record) {
        synchronized(changingChildRecordsLock) {
            val currentLen = _children.size
            val nc = java.lang.reflect.Array.newInstance(Record::class.java, currentLen + 1) as Array<Record>
            System.arraycopy(_children, 0, nc, 0, currentLen)
            nc[currentLen] = newChild
            _children = nc
        }
    }

    private fun addChildAt(newChild: Record, position: Int) {
        synchronized(changingChildRecordsLock) {
            appendChild(newChild)
            moveChildRecords(_children.size - 1, position, 1)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun moveChildRecords(oldLoc: Int, newLoc: Int, number: Int) {
        if (oldLoc == newLoc) return
        if (number == 0) return
        if (oldLoc + number > _children.size) {
            throw IllegalArgumentException("Asked to move more records than there are!")
        }
        ArrayUtil.arrayMoveWithin(_children as Array<Any?>, oldLoc, newLoc, number)
    }

    fun findFirstOfType(type: Long): Record? {
        for (i in _children.indices) {
            if (_children[i].getRecordType() == type) {
                return _children[i]
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun removeChild(ch: Record): Record? {
        var rm: Record? = null
        val lst = ArrayList<Record>()
        for (r in _children) {
            if (r != ch) {
                lst.add(r)
            } else {
                rm = r
            }
        }
        val nc = java.lang.reflect.Array.newInstance(Record::class.java, lst.size) as Array<Record>
        _children = lst.toArray(nc)
        return rm
    }

    fun appendChildRecord(newChild: Record) {
        synchronized(changingChildRecordsLock) {
            appendChild(newChild)
        }
    }

    fun addChildAfter(newChild: Record, after: Record) {
        synchronized(changingChildRecordsLock) {
            val loc = findChildLocation(after)
            if (loc == -1) {
                throw IllegalArgumentException("Asked to add a new child after another record, but that record wasn't one of our children!")
            }
            addChildAt(newChild, loc + 1)
        }
    }

    fun addChildBefore(newChild: Record, before: Record) {
        synchronized(changingChildRecordsLock) {
            val loc = findChildLocation(before)
            if (loc == -1) {
                throw IllegalArgumentException("Asked to add a new child before another record, but that record wasn't one of our children!")
            }
            addChildAt(newChild, loc)
        }
    }

    fun moveChildBefore(child: Record, before: Record) {
        moveChildrenBefore(child, 1, before)
    }

    fun moveChildrenBefore(firstChild: Record, number: Int, before: Record) {
        if (number < 1) return
        synchronized(changingChildRecordsLock) {
            val newLoc = findChildLocation(before)
            if (newLoc == -1) {
                throw IllegalArgumentException("Asked to move children before another record, but that record wasn't one of our children!")
            }
            val oldLoc = findChildLocation(firstChild)
            if (oldLoc == -1) {
                throw IllegalArgumentException("Asked to move a record that wasn't a child!")
            }
            moveChildRecords(oldLoc, newLoc, number)
        }
    }

    fun moveChildrenAfter(firstChild: Record, number: Int, after: Record) {
        if (number < 1) return
        synchronized(changingChildRecordsLock) {
            var newLoc = findChildLocation(after)
            if (newLoc == -1) {
                throw IllegalArgumentException("Asked to move children before another record, but that record wasn't one of our children!")
            }
            newLoc++
            val oldLoc = findChildLocation(firstChild)
            if (oldLoc == -1) {
                throw IllegalArgumentException("Asked to move a record that wasn't a child!")
            }
            moveChildRecords(oldLoc, newLoc, number)
        }
    }

    fun setChildRecord(records: Array<Record>?) {
        this._children = records ?: emptyArray()
    }

    override fun dispose() {
        for (rec in _children) {
            rec.dispose()
        }
        _children = emptyArray()
    }

    companion object {
        @JvmStatic
        fun handleParentAwareRecords(br: RecordContainer) {
            val children = br.getChildRecords()
            for (record in children) {
                if (record is ParentAwareRecord) {
                    record.setParentRecord(br)
                }
                if (record is RecordContainer) {
                    handleParentAwareRecords(record)
                }
            }
        }
    }
}

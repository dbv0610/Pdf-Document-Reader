package com.wxiwei.office.fc.util

class ShortList {
    private var array: ShortArray
    private var limit = 0
    constructor() : this(128)
    constructor(initialCapacity: Int) { array = ShortArray(initialCapacity) }
    constructor(list: ShortList) { array = list.array.copyOf(); limit = list.limit }
    fun add(index: Int, value: Short) { checkInsert(index); ensure(limit + 1); System.arraycopy(array, index, array, index + 1, limit - index); array[index] = value; limit++ }
    fun add(value: Short): Boolean { ensure(limit + 1); array[limit++] = value; return true }
    fun addAll(c: ShortList): Boolean { ensure(limit + c.limit); System.arraycopy(c.array, 0, array, limit, c.limit); limit += c.limit; return true }
    fun addAll(index: Int, c: ShortList): Boolean { checkInsert(index); ensure(limit + c.limit); System.arraycopy(array, index, array, index + c.limit, limit - index); System.arraycopy(c.array, 0, array, index, c.limit); limit += c.limit; return true }
    fun clear() { limit = 0 }
    fun contains(o: Short) = indexOf(o) >= 0
    fun containsAll(c: ShortList): Boolean { for (i in 0 until c.limit) if (!contains(c.array[i])) return false; return true }
    override fun equals(other: Any?): Boolean { if (other !is ShortList || other.limit != limit) return false; for (i in 0 until limit) if (array[i] != other.array[i]) return false; return true }
    fun get(index: Int): Short { check(index); return array[index] }
    override fun hashCode(): Int { var h = 1; for (i in 0 until limit) h = 31 * h + array[i]; return h }
    fun indexOf(o: Short): Int { for (i in 0 until limit) if (array[i] == o) return i; return -1 }
    fun isEmpty() = limit == 0
    fun lastIndexOf(o: Short): Int { for (i in limit - 1 downTo 0) if (array[i] == o) return i; return -1 }
    fun remove(index: Int): Short { check(index); val old = array[index]; System.arraycopy(array, index + 1, array, index, limit - index - 1); limit--; return old }
    fun removeValue(o: Short): Boolean { val i = indexOf(o); if (i < 0) return false; remove(i); return true }
    fun removeAll(c: ShortList): Boolean { var changed = false; var i = 0; while (i < limit) if (c.contains(array[i])) { remove(i); changed = true } else i++; return changed }
    fun retainAll(c: ShortList): Boolean { var changed = false; var i = 0; while (i < limit) if (!c.contains(array[i])) { remove(i); changed = true } else i++; return changed }
    fun set(index: Int, element: Short): Short { check(index); val old = array[index]; array[index] = element; return old }
    fun size() = limit
    fun toArray(): ShortArray = array.copyOf(limit)
    fun toArray(a: ShortArray): ShortArray { if (a.size < limit) return toArray(); System.arraycopy(array, 0, a, 0, limit); return a }
    private fun check(index: Int) { if (index < 0 || index >= limit) throw IndexOutOfBoundsException() }
    private fun checkInsert(index: Int) { if (index < 0 || index > limit) throw IndexOutOfBoundsException() }
    private fun ensure(size: Int) { if (size > array.size) array = array.copyOf(maxOf(size, maxOf(1, array.size * 2))) }
}

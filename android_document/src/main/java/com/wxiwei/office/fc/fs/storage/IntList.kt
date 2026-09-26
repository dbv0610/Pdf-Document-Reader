package com.wxiwei.office.fc.fs.storage

import com.wxiwei.office.fc.fs.filesystem.CFBConstants

/**
 * A List of int's; as full an implementation of the java.util.List
 * interface as possible, with an eye toward minimal creation of
 * objects
 * 
 * the mimicry of List is as follows:
 * 
 *  *  if possible, operations designated 'optional' in the List
 * interface are attempted
 *  *  wherever the List interface refers to an Object, substitute
 * int
 *  *  wherever the List interface refers to a Collection or List,
 * substitute IntList
 * 
 * 
 * the mimicry is not perfect, however:
 * 
 *  *  operations involving Iterators or ListIterators are not
 * supported
 *  *  remove(Object) becomes removeValue to distinguish it from
 * remove(int index)
 *  *  subList is not supported
 * 
 */
class IntList
@JvmOverloads constructor(initialCapacity: Int = _default_size, fillvalue: Int = 0) {
    private var _array: IntArray
    private var _limit: Int
    private var fillval = 0
    /**
     * create an IntList with a predefined initial size
     * 
     * @param initialCapacity the size for the internal array
     */
    /**
     * create an IntList of default size
     */
    init {
        _array = IntArray(initialCapacity)
        if (fillval != 0) {
            fillval = fillvalue
            fillArray(fillval, _array, 0)
        }
        _limit = 0
    }

    /**
     * 
     * @param val
     * @param array
     * @param index
     */
    private fun fillArray(`val`: Int, array: IntArray, index: Int) {
        for (k in index..<array.size) {
            array[k] = `val`
        }
    }


    /**
     * Appends the specified element to the end of this list
     * 
     * @param value element to be appended to this list.
     * 
     * @return true (as per the general contract of the Collection.add
     * method).
     */
    fun add(value: Int): Boolean {
        if (_limit == _array.size) {
            growArray(_limit * 2)
        }
        _array[_limit++] = value
        return true
    }


    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index index of element to return.
     * 
     * @return the element at the specified position in this list.
     * 
     * @exception IndexOutOfBoundsException if the index is out of
     * range (index < 0 || index >= size()).
     */
    fun get(index: Int): Int {
        if (index >= _limit) {
            return CFBConstants.Companion.END_OF_CHAIN
            //throw new IndexOutOfBoundsException(index + " not accessible in a list of length "
            //    + _limit);
        }
        return _array[index]
    }


    /**
     * Returns the number of elements in this list. If this list
     * contains more than Integer.MAX_VALUE elements, returns
     * Integer.MAX_VALUE.
     * 
     * @return the number of elements in this IntList
     */
    fun size(): Int {
        return _limit
    }

    /**
     * 
     */
    private fun growArray(new_size: Int) {
        val size = if (new_size == _array.size) new_size + 1 else new_size
        val new_array = IntArray(size)

        if (fillval != 0) {
            fillArray(fillval, new_array, _array.size)
        }

        System.arraycopy(_array, 0, new_array, 0, _limit)
        _array = new_array
    }

    companion object {
        private const val _default_size = 128
    }
}


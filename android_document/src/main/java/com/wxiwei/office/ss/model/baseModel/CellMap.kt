/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.ss.model.baseModel

/**
 * The cells of a row by column. Replaces a Hashtable<Int, Cell>, whose entry, boxed key and
 * table cost ~40 bytes per cell on top of the cell: sheets of a million cells ran out of heap.
 * Columns from the first used one are kept in an array; a column far from the others (a sparse
 * row) goes to a small map instead, so one cell in XFD does not make an array of 16k slots.
 * Synchronized like the Hashtable: the reader fills rows while the view reads them.
 */
class CellMap {
    private var first = 0
    private var slots: Array<Cell?> = EMPTY
    private var inSlots = 0
    private var far: HashMap<Int, Cell>? = null

    @get:Synchronized
    val size: Int get() = inSlots + (far?.size ?: 0)

    @Synchronized
    operator fun get(column: Int): Cell? {
        val i = column - first
        return if (i >= 0 && i < slots.size) slots[i] ?: far?.get(column) else far?.get(column)
    }

    @Synchronized
    operator fun set(column: Int, cell: Cell) {
        if (!fits(column)) {
            (far ?: HashMap<Int, Cell>().also { far = it })[column] = cell
            return
        }
        grow(column)
        val i = column - first
        if (slots[i] == null) inSlots++
        slots[i] = cell
        far?.remove(column)
    }

    @Synchronized
    fun remove(column: Int): Cell? {
        val i = column - first
        if (i >= 0 && i < slots.size && slots[i] != null) {
            val cell = slots[i]
            slots[i] = null
            inSlots--
            return cell
        }
        return far?.remove(column)
    }

    @Synchronized
    fun clear() {
        slots = EMPTY
        first = 0
        inSlots = 0
        far = null
    }

    /** The columns that have a cell, in order; a copy. */
    @get:Synchronized
    val keys: List<Int> get() {
        val out = ArrayList<Int>(size)
        for (i in slots.indices) if (slots[i] != null) out += first + i
        far?.keys?.let { out += it; out.sort() }
        return out
    }

    /** The cells, by column; a copy, so it can be walked while the reader adds cells. */
    @get:Synchronized
    val values: MutableCollection<Cell> get() {
        val out = ArrayList<Cell>(size)
        for (cell in slots) if (cell != null) out += cell
        far?.let { map -> out += map.entries.sortedBy { it.key }.map { it.value } }
        return out
    }

    /** Whether [column] can go in the array without making it mostly empty. */
    private fun fits(column: Int): Boolean {
        if (slots.isEmpty()) return true
        val from = minOf(first, column)
        val to = maxOf(first + slots.size, column + 1)
        val span = to - from
        return span <= DENSE_SPAN || span <= SPARSE_FACTOR * (inSlots + 1)
    }

    private fun grow(column: Int) {
        if (slots.isEmpty()) {
            first = column
            slots = arrayOfNulls(1)
            return
        }
        if (column < first) {
            val bigger = arrayOfNulls<Cell>(slots.size + (first - column))
            System.arraycopy(slots, 0, bigger, first - column, slots.size)
            slots = bigger
            first = column
        } else if (column - first >= slots.size) {
            // grows by half, so a row filled left to right ends close to its size
            val size = maxOf(column - first + 1, slots.size + slots.size / 2)
            slots = slots.copyOf(size)
        }
    }

    private companion object {
        val EMPTY = arrayOfNulls<Cell>(0)
        /** Any row up to this many columns wide stays in the array. */
        const val DENSE_SPAN = 64
        /** Wider rows stay in the array while at least one slot in this many is used. */
        const val SPARSE_FACTOR = 4
    }
}

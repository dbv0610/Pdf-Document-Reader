/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.util

/**
 * This class contains various methods for manipulating arrays (such as
 * sorting and searching). This class also contains a static factory
 * that allows arrays to be viewed as lists.
 *
 * The methods in this class all throw a <tt>NullPointerException</tt> if
 * the specified array reference is null, except where noted.
 */
object Arrays {

    /**
     * Sorts the specified array of longs into ascending numerical order.
     */
    @JvmStatic
    fun sort(a: LongArray) {
        sort1(a, 0, a.size)
    }

    /**
     * Sorts the specified range of the specified array of longs into
     * ascending numerical order.
     */
    @JvmStatic
    fun sort(a: LongArray, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        sort1(a, fromIndex, toIndex - fromIndex)
    }

    /**
     * Sorts the specified array of ints into ascending numerical order.
     */
    @JvmStatic
    fun sort(a: IntArray) {
        sort1(a, 0, a.size)
    }

    /**
     * Sorts the specified range of the specified array of ints into
     * ascending numerical order.
     */
    @JvmStatic
    fun sort(a: IntArray, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        sort1(a, fromIndex, toIndex - fromIndex)
    }

    /**
     * Sorts the specified array of shorts into ascending numerical order.
     */
    @JvmStatic
    fun sort(a: ShortArray) {
        sort1(a, 0, a.size)
    }

    /**
     * Sorts the specified range of the specified array of shorts into
     * ascending numerical order.
     */
    @JvmStatic
    fun sort(a: ShortArray, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        sort1(a, fromIndex, toIndex - fromIndex)
    }

    /**
     * Sorts the specified array of chars into ascending numerical order.
     */
    @JvmStatic
    fun sort(a: CharArray) {
        sort1(a, 0, a.size)
    }

    /**
     * Sorts the specified range of the specified array of chars into
     * ascending numerical order.
     */
    @JvmStatic
    fun sort(a: CharArray, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        sort1(a, fromIndex, toIndex - fromIndex)
    }

    /**
     * Sorts the specified array of bytes into ascending numerical order.
     */
    @JvmStatic
    fun sort(a: ByteArray) {
        sort1(a, 0, a.size)
    }

    /**
     * Sorts the specified range of the specified array of bytes into
     * ascending numerical order.
     */
    @JvmStatic
    fun sort(a: ByteArray, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        sort1(a, fromIndex, toIndex - fromIndex)
    }

    /**
     * Sorts the specified array of doubles into ascending numerical order.
     */
    @JvmStatic
    fun sort(a: DoubleArray) {
        sort2(a, 0, a.size)
    }

    /**
     * Sorts the specified range of the specified array of doubles into
     * ascending numerical order.
     */
    @JvmStatic
    fun sort(a: DoubleArray, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        sort2(a, fromIndex, toIndex)
    }

    /**
     * Sorts the specified array of floats into ascending numerical order.
     */
    @JvmStatic
    fun sort(a: FloatArray) {
        sort2(a, 0, a.size)
    }

    /**
     * Sorts the specified range of the specified array of floats into
     * ascending numerical order.
     */
    @JvmStatic
    fun sort(a: FloatArray, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        sort2(a, fromIndex, toIndex)
    }

    private fun sort2(a: DoubleArray, fromIndex: Int, toIndex: Int) {
        val NEG_ZERO_BITS = java.lang.Double.doubleToLongBits(-0.0)
        /*
         * The sort is done in three phases to avoid the expense of using
         * NaN and -0.0 aware comparisons during the main sort.
         */

        /*
         * Preprocessing phase:  Move any NaN's to end of array, count the
         * number of -0.0's, and turn them into 0.0's.
         */
        var numNegZeros = 0
        var i = fromIndex
        var n = toIndex
        while (i < n) {
            if (a[i] != a[i]) {
                val swap = a[i]
                a[i] = a[--n]
                a[n] = swap
            } else {
                if (a[i] == 0.0 && java.lang.Double.doubleToLongBits(a[i]) == NEG_ZERO_BITS) {
                    a[i] = 0.0
                    numNegZeros++
                }
                i++
            }
        }

        // Main sort phase: quicksort everything but the NaN's
        sort1(a, fromIndex, n - fromIndex)

        // Postprocessing phase: change 0.0's to -0.0's as required
        if (numNegZeros != 0) {
            var j = binarySearch0(a, fromIndex, n, 0.0) // posn of ANY zero
            do {
                j--
            } while (j >= 0 && a[j] == 0.0)

            // j is now one less than the index of the FIRST zero
            for (k in 0 until numNegZeros)
                a[++j] = -0.0
        }
    }

    private fun sort2(a: FloatArray, fromIndex: Int, toIndex: Int) {
        val NEG_ZERO_BITS = java.lang.Float.floatToIntBits(-0.0f)
        /*
         * The sort is done in three phases to avoid the expense of using
         * NaN and -0.0 aware comparisons during the main sort.
         */

        /*
         * Preprocessing phase:  Move any NaN's to end of array, count the
         * number of -0.0's, and turn them into 0.0's.
         */
        var numNegZeros = 0
        var i = fromIndex
        var n = toIndex
        while (i < n) {
            if (a[i] != a[i]) {
                val swap = a[i]
                a[i] = a[--n]
                a[n] = swap
            } else {
                if (a[i] == 0f && java.lang.Float.floatToIntBits(a[i]) == NEG_ZERO_BITS) {
                    a[i] = 0.0f
                    numNegZeros++
                }
                i++
            }
        }

        // Main sort phase: quicksort everything but the NaN's
        sort1(a, fromIndex, n - fromIndex)

        // Postprocessing phase: change 0.0's to -0.0's as required
        if (numNegZeros != 0) {
            var j = binarySearch0(a, fromIndex, n, 0.0f) // posn of ANY zero
            do {
                j--
            } while (j >= 0 && a[j] == 0.0f)

            // j is now one less than the index of the FIRST zero
            for (k in 0 until numNegZeros)
                a[++j] = -0.0f
        }
    }

    /**
     * Sorts the specified sub-array of longs into ascending order.
     */
    private fun sort1(x: LongArray, off: Int, len: Int) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (i in off until len + off) {
                var j = i
                while (j > off && x[j - 1] > x[j]) {
                    swap(x, j, j - 1)
                    j--
                }
            }
            return
        }

        // Choose a partition element, v
        var m = off + (len shr 1) // Small arrays, middle element
        if (len > 7) {
            var l = off
            var n = off + len - 1
            if (len > 40) { // Big arrays, pseudomedian of 9
                val s = len / 8
                l = med3(x, l, l + s, l + 2 * s)
                m = med3(x, m - s, m, m + s)
                n = med3(x, n - 2 * s, n - s, n)
            }
            m = med3(x, l, m, n) // Mid-size, med of 3
        }
        val v = x[m]

        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b)
                b++
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--)
                c--
            }
            if (b > c)
                break
            swap(x, b++, c--)
        }

        // Swap partition elements back to middle
        var s: Int
        val n = off + len
        s = Math.min(a - off, b - a)
        vecswap(x, off, b - s, s)
        s = Math.min(d - c, n - d - 1)
        vecswap(x, b, n - s, s)

        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
            sort1(x, off, s)
        s = d - c
        if (s > 1)
            sort1(x, n - s, s)
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: LongArray, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private fun vecswap(x: LongArray, a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var i = 0
        while (i < n) {
            swap(x, a, b)
            i++
            a++
            b++
        }
    }

    /**
     * Returns the index of the median of the three indexed longs.
     */
    private fun med3(x: LongArray, a: Int, b: Int, c: Int): Int {
        return if (x[a] < x[b]) (if (x[b] < x[c]) b else if (x[a] < x[c]) c else a) else (if (x[b] > x[c]) b
        else if (x[a] > x[c]) c else a)
    }

    /**
     * Sorts the specified sub-array of ints into ascending order.
     */
    private fun sort1(x: IntArray, off: Int, len: Int) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (i in off until len + off) {
                var j = i
                while (j > off && x[j - 1] > x[j]) {
                    swap(x, j, j - 1)
                    j--
                }
            }
            return
        }

        // Choose a partition element, v
        var m = off + (len shr 1) // Small arrays, middle element
        if (len > 7) {
            var l = off
            var n = off + len - 1
            if (len > 40) { // Big arrays, pseudomedian of 9
                val s = len / 8
                l = med3(x, l, l + s, l + 2 * s)
                m = med3(x, m - s, m, m + s)
                n = med3(x, n - 2 * s, n - s, n)
            }
            m = med3(x, l, m, n) // Mid-size, med of 3
        }
        val v = x[m]

        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b)
                b++
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--)
                c--
            }
            if (b > c)
                break
            swap(x, b++, c--)
        }

        // Swap partition elements back to middle
        var s: Int
        val n = off + len
        s = Math.min(a - off, b - a)
        vecswap(x, off, b - s, s)
        s = Math.min(d - c, n - d - 1)
        vecswap(x, b, n - s, s)

        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
            sort1(x, off, s)
        s = d - c
        if (s > 1)
            sort1(x, n - s, s)
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: IntArray, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private fun vecswap(x: IntArray, a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var i = 0
        while (i < n) {
            swap(x, a, b)
            i++
            a++
            b++
        }
    }

    /**
     * Returns the index of the median of the three indexed ints.
     */
    private fun med3(x: IntArray, a: Int, b: Int, c: Int): Int {
        return if (x[a] < x[b]) (if (x[b] < x[c]) b else if (x[a] < x[c]) c else a) else (if (x[b] > x[c]) b
        else if (x[a] > x[c]) c else a)
    }

    /**
     * Sorts the specified sub-array of shorts into ascending order.
     */
    private fun sort1(x: ShortArray, off: Int, len: Int) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (i in off until len + off) {
                var j = i
                while (j > off && x[j - 1] > x[j]) {
                    swap(x, j, j - 1)
                    j--
                }
            }
            return
        }

        // Choose a partition element, v
        var m = off + (len shr 1) // Small arrays, middle element
        if (len > 7) {
            var l = off
            var n = off + len - 1
            if (len > 40) { // Big arrays, pseudomedian of 9
                val s = len / 8
                l = med3(x, l, l + s, l + 2 * s)
                m = med3(x, m - s, m, m + s)
                n = med3(x, n - 2 * s, n - s, n)
            }
            m = med3(x, l, m, n) // Mid-size, med of 3
        }
        val v = x[m]

        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b)
                b++
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--)
                c--
            }
            if (b > c)
                break
            swap(x, b++, c--)
        }

        // Swap partition elements back to middle
        var s: Int
        val n = off + len
        s = Math.min(a - off, b - a)
        vecswap(x, off, b - s, s)
        s = Math.min(d - c, n - d - 1)
        vecswap(x, b, n - s, s)

        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
            sort1(x, off, s)
        s = d - c
        if (s > 1)
            sort1(x, n - s, s)
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: ShortArray, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private fun vecswap(x: ShortArray, a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var i = 0
        while (i < n) {
            swap(x, a, b)
            i++
            a++
            b++
        }
    }

    /**
     * Returns the index of the median of the three indexed shorts.
     */
    private fun med3(x: ShortArray, a: Int, b: Int, c: Int): Int {
        return if (x[a] < x[b]) (if (x[b] < x[c]) b else if (x[a] < x[c]) c else a) else (if (x[b] > x[c]) b
        else if (x[a] > x[c]) c else a)
    }

    /**
     * Sorts the specified sub-array of chars into ascending order.
     */
    private fun sort1(x: CharArray, off: Int, len: Int) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (i in off until len + off) {
                var j = i
                while (j > off && x[j - 1] > x[j]) {
                    swap(x, j, j - 1)
                    j--
                }
            }
            return
        }

        // Choose a partition element, v
        var m = off + (len shr 1) // Small arrays, middle element
        if (len > 7) {
            var l = off
            var n = off + len - 1
            if (len > 40) { // Big arrays, pseudomedian of 9
                val s = len / 8
                l = med3(x, l, l + s, l + 2 * s)
                m = med3(x, m - s, m, m + s)
                n = med3(x, n - 2 * s, n - s, n)
            }
            m = med3(x, l, m, n) // Mid-size, med of 3
        }
        val v = x[m]

        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b)
                b++
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--)
                c--
            }
            if (b > c)
                break
            swap(x, b++, c--)
        }

        // Swap partition elements back to middle
        var s: Int
        val n = off + len
        s = Math.min(a - off, b - a)
        vecswap(x, off, b - s, s)
        s = Math.min(d - c, n - d - 1)
        vecswap(x, b, n - s, s)

        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
            sort1(x, off, s)
        s = d - c
        if (s > 1)
            sort1(x, n - s, s)
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: CharArray, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private fun vecswap(x: CharArray, a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var i = 0
        while (i < n) {
            swap(x, a, b)
            i++
            a++
            b++
        }
    }

    /**
     * Returns the index of the median of the three indexed chars.
     */
    private fun med3(x: CharArray, a: Int, b: Int, c: Int): Int {
        return if (x[a] < x[b]) (if (x[b] < x[c]) b else if (x[a] < x[c]) c else a) else (if (x[b] > x[c]) b
        else if (x[a] > x[c]) c else a)
    }

    /**
     * Sorts the specified sub-array of bytes into ascending order.
     */
    private fun sort1(x: ByteArray, off: Int, len: Int) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (i in off until len + off) {
                var j = i
                while (j > off && x[j - 1] > x[j]) {
                    swap(x, j, j - 1)
                    j--
                }
            }
            return
        }

        // Choose a partition element, v
        var m = off + (len shr 1) // Small arrays, middle element
        if (len > 7) {
            var l = off
            var n = off + len - 1
            if (len > 40) { // Big arrays, pseudomedian of 9
                val s = len / 8
                l = med3(x, l, l + s, l + 2 * s)
                m = med3(x, m - s, m, m + s)
                n = med3(x, n - 2 * s, n - s, n)
            }
            m = med3(x, l, m, n) // Mid-size, med of 3
        }
        val v = x[m]

        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b)
                b++
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--)
                c--
            }
            if (b > c)
                break
            swap(x, b++, c--)
        }

        // Swap partition elements back to middle
        var s: Int
        val n = off + len
        s = Math.min(a - off, b - a)
        vecswap(x, off, b - s, s)
        s = Math.min(d - c, n - d - 1)
        vecswap(x, b, n - s, s)

        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
            sort1(x, off, s)
        s = d - c
        if (s > 1)
            sort1(x, n - s, s)
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: ByteArray, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private fun vecswap(x: ByteArray, a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var i = 0
        while (i < n) {
            swap(x, a, b)
            i++
            a++
            b++
        }
    }

    /**
     * Returns the index of the median of the three indexed bytes.
     */
    private fun med3(x: ByteArray, a: Int, b: Int, c: Int): Int {
        return if (x[a] < x[b]) (if (x[b] < x[c]) b else if (x[a] < x[c]) c else a) else (if (x[b] > x[c]) b
        else if (x[a] > x[c]) c else a)
    }

    /**
     * Sorts the specified sub-array of doubles into ascending order.
     */
    private fun sort1(x: DoubleArray, off: Int, len: Int) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (i in off until len + off) {
                var j = i
                while (j > off && x[j - 1] > x[j]) {
                    swap(x, j, j - 1)
                    j--
                }
            }
            return
        }

        // Choose a partition element, v
        var m = off + (len shr 1) // Small arrays, middle element
        if (len > 7) {
            var l = off
            var n = off + len - 1
            if (len > 40) { // Big arrays, pseudomedian of 9
                val s = len / 8
                l = med3(x, l, l + s, l + 2 * s)
                m = med3(x, m - s, m, m + s)
                n = med3(x, n - 2 * s, n - s, n)
            }
            m = med3(x, l, m, n) // Mid-size, med of 3
        }
        val v = x[m]

        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b)
                b++
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--)
                c--
            }
            if (b > c)
                break
            swap(x, b++, c--)
        }

        // Swap partition elements back to middle
        var s: Int
        val n = off + len
        s = Math.min(a - off, b - a)
        vecswap(x, off, b - s, s)
        s = Math.min(d - c, n - d - 1)
        vecswap(x, b, n - s, s)

        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
            sort1(x, off, s)
        s = d - c
        if (s > 1)
            sort1(x, n - s, s)
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: DoubleArray, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private fun vecswap(x: DoubleArray, a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var i = 0
        while (i < n) {
            swap(x, a, b)
            i++
            a++
            b++
        }
    }

    /**
     * Returns the index of the median of the three indexed doubles.
     */
    private fun med3(x: DoubleArray, a: Int, b: Int, c: Int): Int {
        return if (x[a] < x[b]) (if (x[b] < x[c]) b else if (x[a] < x[c]) c else a) else (if (x[b] > x[c]) b
        else if (x[a] > x[c]) c else a)
    }

    /**
     * Sorts the specified sub-array of floats into ascending order.
     */
    private fun sort1(x: FloatArray, off: Int, len: Int) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (i in off until len + off) {
                var j = i
                while (j > off && x[j - 1] > x[j]) {
                    swap(x, j, j - 1)
                    j--
                }
            }
            return
        }

        // Choose a partition element, v
        var m = off + (len shr 1) // Small arrays, middle element
        if (len > 7) {
            var l = off
            var n = off + len - 1
            if (len > 40) { // Big arrays, pseudomedian of 9
                val s = len / 8
                l = med3(x, l, l + s, l + 2 * s)
                m = med3(x, m - s, m, m + s)
                n = med3(x, n - 2 * s, n - s, n)
            }
            m = med3(x, l, m, n) // Mid-size, med of 3
        }
        val v = x[m]

        // Establish Invariant: v* (<v)* (>v)* v*
        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b)
                b++
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--)
                c--
            }
            if (b > c)
                break
            swap(x, b++, c--)
        }

        // Swap partition elements back to middle
        var s: Int
        val n = off + len
        s = Math.min(a - off, b - a)
        vecswap(x, off, b - s, s)
        s = Math.min(d - c, n - d - 1)
        vecswap(x, b, n - s, s)

        // Recursively sort non-partition-elements
        s = b - a
        if (s > 1)
            sort1(x, off, s)
        s = d - c
        if (s > 1)
            sort1(x, n - s, s)
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: FloatArray, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private fun vecswap(x: FloatArray, a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var i = 0
        while (i < n) {
            swap(x, a, b)
            i++
            a++
            b++
        }
    }

    /**
     * Returns the index of the median of the three indexed floats.
     */
    private fun med3(x: FloatArray, a: Int, b: Int, c: Int): Int {
        return if (x[a] < x[b]) (if (x[b] < x[c]) b else if (x[a] < x[c]) c else a) else (if (x[b] > x[c]) b
        else if (x[a] > x[c]) c else a)
    }

    /**
     * Sorts the specified array of objects into ascending order, according to
     * the [natural ordering][Comparable] of its elements.
     */
    @JvmStatic
    fun sort(a: Array<out Any?>) {
        @Suppress("UNCHECKED_CAST")
        val dest = a as Array<Any?>
        val aux = dest.clone()
        mergeSort(aux, dest, 0, a.size, 0)
    }

    /**
     * Sorts the specified range of the specified array of objects into
     * ascending order, according to the [natural ordering][Comparable] of its
     * elements.
     */
    @JvmStatic
    fun sort(a: Array<out Any?>, fromIndex: Int, toIndex: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        @Suppress("UNCHECKED_CAST")
        val dest = a as Array<Any?>
        val aux = copyOfRange(dest, fromIndex, toIndex)
        mergeSort(aux, dest, fromIndex, toIndex, -fromIndex)
    }

    /**
     * Tuning parameter: list size at or below which insertion sort will be
     * used in preference to mergesort or quicksort.
     */
    private const val INSERTIONSORT_THRESHOLD = 7

    /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset to generate corresponding low, high in src
     */
    @Suppress("UNCHECKED_CAST")
    private fun mergeSort(src: Array<Any?>, dest: Array<Any?>, low: Int, high: Int, off: Int) {
        var low = low
        var high = high
        val length = high - low

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (i in low until high) {
                var j = i
                while (j > low && (dest[j - 1] as Comparable<Any?>).compareTo(dest[j]) > 0) {
                    swap(dest, j, j - 1)
                    j--
                }
            }
            return
        }

        // Recursively sort halves of dest into src
        val destLow = low
        val destHigh = high
        low += off
        high += off
        val mid = (low + high) ushr 1
        mergeSort(dest, src, low, mid, -off)
        mergeSort(dest, src, mid, high, -off)

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if ((src[mid - 1] as Comparable<Any?>).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length)
            return
        }

        // Merge sorted halves (now in src) into dest
        var i = destLow
        var p = low
        var q = mid
        while (i < destHigh) {
            if (q >= high || p < mid && (src[p] as Comparable<Any?>).compareTo(src[q]) <= 0)
                dest[i] = src[p++]
            else
                dest[i] = src[q++]
            i++
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private fun swap(x: Array<Any?>, a: Int, b: Int) {
        val t = x[a]
        x[a] = x[b]
        x[b] = t
    }

    /**
     * Sorts the specified array of objects according to the order induced by
     * the specified comparator.
     */
    @JvmStatic
    fun <T> sort(a: Array<T>, c: Comparator<in T>?) {
        val aux = a.clone()
        @Suppress("UNCHECKED_CAST")
        if (c == null)
            mergeSort(aux as Array<Any?>, a as Array<Any?>, 0, a.size, 0)
        else
            mergeSort(aux as Array<Any?>, a as Array<Any?>, 0, a.size, 0, c as Comparator<Any?>)
    }

    /**
     * Sorts the specified range of the specified array of objects according
     * to the order induced by the specified comparator.
     */
    @JvmStatic
    fun <T> sort(a: Array<T>, fromIndex: Int, toIndex: Int, c: Comparator<in T>?) {
        rangeCheck(a.size, fromIndex, toIndex)
        val aux = copyOfRange(a, fromIndex, toIndex)
        @Suppress("UNCHECKED_CAST")
        if (c == null)
            mergeSort(aux as Array<Any?>, a as Array<Any?>, fromIndex, toIndex, -fromIndex)
        else
            mergeSort(aux as Array<Any?>, a as Array<Any?>, fromIndex, toIndex, -fromIndex, c as Comparator<Any?>)
    }

    /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset into src corresponding to low in dest
     */
    private fun mergeSort(src: Array<Any?>, dest: Array<Any?>, low: Int, high: Int, off: Int,
                          c: Comparator<Any?>) {
        var low = low
        var high = high
        val length = high - low

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (i in low until high) {
                var j = i
                while (j > low && c.compare(dest[j - 1], dest[j]) > 0) {
                    swap(dest, j, j - 1)
                    j--
                }
            }
            return
        }

        // Recursively sort halves of dest into src
        val destLow = low
        val destHigh = high
        low += off
        high += off
        val mid = (low + high) ushr 1
        mergeSort(dest, src, low, mid, -off, c)
        mergeSort(dest, src, mid, high, -off, c)

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length)
            return
        }

        // Merge sorted halves (now in src) into dest
        var i = destLow
        var p = low
        var q = mid
        while (i < destHigh) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
                dest[i] = src[p++]
            else
                dest[i] = src[q++]
            i++
        }
    }

    /**
     * Check that fromIndex and toIndex are in range, and throw an
     * appropriate exception if they aren't.
     */
    private fun rangeCheck(arrayLen: Int, fromIndex: Int, toIndex: Int) {
        if (fromIndex > toIndex)
            throw IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex
                    + ")")
        if (fromIndex < 0)
            throw ArrayIndexOutOfBoundsException(fromIndex)
        if (toIndex > arrayLen)
            throw ArrayIndexOutOfBoundsException(toIndex)
    }

    /**
     * Searches the specified array of longs for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: LongArray, key: Long): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array of longs for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: LongArray, fromIndex: Int, toIndex: Int, key: Long): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    private fun binarySearch0(a: LongArray, fromIndex: Int, toIndex: Int, key: Long): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            if (midVal < key)
                low = mid + 1
            else if (midVal > key)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array of ints for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: IntArray, key: Int): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array of ints for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: IntArray, fromIndex: Int, toIndex: Int, key: Int): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    private fun binarySearch0(a: IntArray, fromIndex: Int, toIndex: Int, key: Int): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            if (midVal < key)
                low = mid + 1
            else if (midVal > key)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array of shorts for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: ShortArray, key: Short): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array of shorts for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: ShortArray, fromIndex: Int, toIndex: Int, key: Short): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    private fun binarySearch0(a: ShortArray, fromIndex: Int, toIndex: Int, key: Short): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            if (midVal < key)
                low = mid + 1
            else if (midVal > key)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array of chars for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: CharArray, key: Char): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array of chars for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: CharArray, fromIndex: Int, toIndex: Int, key: Char): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    private fun binarySearch0(a: CharArray, fromIndex: Int, toIndex: Int, key: Char): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            if (midVal < key)
                low = mid + 1
            else if (midVal > key)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array of bytes for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: ByteArray, key: Byte): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array of bytes for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: ByteArray, fromIndex: Int, toIndex: Int, key: Byte): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    private fun binarySearch0(a: ByteArray, fromIndex: Int, toIndex: Int, key: Byte): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            if (midVal < key)
                low = mid + 1
            else if (midVal > key)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array of doubles for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: DoubleArray, key: Double): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array of doubles for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: DoubleArray, fromIndex: Int, toIndex: Int, key: Double): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    private fun binarySearch0(a: DoubleArray, fromIndex: Int, toIndex: Int, key: Double): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            val cmp: Int
            if (midVal < key) {
                cmp = -1 // Neither val is NaN, thisVal is smaller
            } else if (midVal > key) {
                cmp = 1 // Neither val is NaN, thisVal is larger
            } else {
                val midBits = java.lang.Double.doubleToLongBits(midVal)
                val keyBits = java.lang.Double.doubleToLongBits(key)
                cmp = (if (midBits == keyBits) 0 // Values are equal
                else (if (midBits < keyBits) -1 // (-0.0, 0.0) or (!NaN, NaN)
                else 1)) // (0.0, -0.0) or (NaN, !NaN)
            }

            if (cmp < 0)
                low = mid + 1
            else if (cmp > 0)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array of floats for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: FloatArray, key: Float): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array of floats for the specified value using the
     * binary search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: FloatArray, fromIndex: Int, toIndex: Int, key: Float): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    private fun binarySearch0(a: FloatArray, fromIndex: Int, toIndex: Int, key: Float): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]

            val cmp: Int
            if (midVal < key) {
                cmp = -1 // Neither val is NaN, thisVal is smaller
            } else if (midVal > key) {
                cmp = 1 // Neither val is NaN, thisVal is larger
            } else {
                val midBits = java.lang.Float.floatToIntBits(midVal)
                val keyBits = java.lang.Float.floatToIntBits(key)
                cmp = (if (midBits == keyBits) 0 // Values are equal
                else (if (midBits < keyBits) -1 // (-0.0, 0.0) or (!NaN, NaN)
                else 1)) // (0.0, -0.0) or (NaN, !NaN)
            }

            if (cmp < 0)
                low = mid + 1
            else if (cmp > 0)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array for the specified object using the binary
     * search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: Array<out Any?>, key: Any?): Int {
        return binarySearch0(a, 0, a.size, key)
    }

    /**
     * Searches a range of
     * the specified array for the specified object using the binary
     * search algorithm.
     */
    @JvmStatic
    fun binarySearch(a: Array<out Any?>, fromIndex: Int, toIndex: Int, key: Any?): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key)
    }

    // Like public version, but without range checks.
    @Suppress("UNCHECKED_CAST")
    private fun binarySearch0(a: Array<out Any?>, fromIndex: Int, toIndex: Int, key: Any?): Int {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid] as Comparable<Any?>
            val cmp = midVal.compareTo(key)

            if (cmp < 0)
                low = mid + 1
            else if (cmp > 0)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Searches the specified array for the specified object using the binary
     * search algorithm.
     */
    @JvmStatic
    fun <T> binarySearch(a: Array<T>, key: T, c: Comparator<in T>?): Int {
        return binarySearch0(a, 0, a.size, key, c)
    }

    /**
     * Searches a range of
     * the specified array for the specified object using the binary
     * search algorithm.
     */
    @JvmStatic
    fun <T> binarySearch(a: Array<T>, fromIndex: Int, toIndex: Int, key: T,
                         c: Comparator<in T>?): Int {
        rangeCheck(a.size, fromIndex, toIndex)
        return binarySearch0(a, fromIndex, toIndex, key, c)
    }

    // Like public version, but without range checks.
    private fun <T> binarySearch0(a: Array<T>, fromIndex: Int, toIndex: Int, key: T,
                                  c: Comparator<in T>?): Int {
        if (c == null) {
            return binarySearch0(a, fromIndex, toIndex, key as Any?)
        }
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = a[mid]
            val cmp = c.compare(midVal, key)

            if (cmp < 0)
                low = mid + 1
            else if (cmp > 0)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1) // key not found.
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of longs are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: LongArray?, a2: LongArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (a[i] != a2[i])
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of ints are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: IntArray?, a2: IntArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (a[i] != a2[i])
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of shorts are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: ShortArray?, a2: ShortArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (a[i] != a2[i])
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of chars are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: CharArray?, a2: CharArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (a[i] != a2[i])
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of bytes are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: ByteArray?, a2: ByteArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (a[i] != a2[i])
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of booleans are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: BooleanArray?, a2: BooleanArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (a[i] != a2[i])
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of doubles are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: DoubleArray?, a2: DoubleArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (java.lang.Double.doubleToLongBits(a[i]) != java.lang.Double.doubleToLongBits(a2[i]))
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of floats are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: FloatArray?, a2: FloatArray?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length)
            if (java.lang.Float.floatToIntBits(a[i]) != java.lang.Float.floatToIntBits(a2[i]))
                return false

        return true
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of Objects are
     * <i>equal</i> to one another.
     */
    @JvmStatic
    fun equals(a: Array<out Any?>?, a2: Array<out Any?>?): Boolean {
        if (a === a2)
            return true
        if (a == null || a2 == null)
            return false

        val length = a.size
        if (a2.size != length)
            return false

        for (i in 0 until length) {
            val o1 = a[i]
            val o2 = a2[i]
            if (!(if (o1 == null) o2 == null else o1.equals(o2)))
                return false
        }

        return true
    }

    /**
     * Assigns the specified long value to each element of the specified array
     * of longs.
     */
    @JvmStatic
    fun fill(a: LongArray, `val`: Long) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified long value to each element of the specified
     * range of the specified array of longs.
     */
    @JvmStatic
    fun fill(a: LongArray, fromIndex: Int, toIndex: Int, `val`: Long) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified int value to each element of the specified array
     * of ints.
     */
    @JvmStatic
    fun fill(a: IntArray, `val`: Int) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified int value to each element of the specified
     * range of the specified array of ints.
     */
    @JvmStatic
    fun fill(a: IntArray, fromIndex: Int, toIndex: Int, `val`: Int) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified short value to each element of the specified array
     * of shorts.
     */
    @JvmStatic
    fun fill(a: ShortArray, `val`: Short) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified short value to each element of the specified
     * range of the specified array of shorts.
     */
    @JvmStatic
    fun fill(a: ShortArray, fromIndex: Int, toIndex: Int, `val`: Short) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified char value to each element of the specified array
     * of chars.
     */
    @JvmStatic
    fun fill(a: CharArray, `val`: Char) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified char value to each element of the specified
     * range of the specified array of chars.
     */
    @JvmStatic
    fun fill(a: CharArray, fromIndex: Int, toIndex: Int, `val`: Char) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified byte value to each element of the specified array
     * of bytes.
     */
    @JvmStatic
    fun fill(a: ByteArray, `val`: Byte) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified byte value to each element of the specified
     * range of the specified array of bytes.
     */
    @JvmStatic
    fun fill(a: ByteArray, fromIndex: Int, toIndex: Int, `val`: Byte) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified boolean value to each element of the specified array
     * of booleans.
     */
    @JvmStatic
    fun fill(a: BooleanArray, `val`: Boolean) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified boolean value to each element of the specified
     * range of the specified array of booleans.
     */
    @JvmStatic
    fun fill(a: BooleanArray, fromIndex: Int, toIndex: Int, `val`: Boolean) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified double value to each element of the specified array
     * of doubles.
     */
    @JvmStatic
    fun fill(a: DoubleArray, `val`: Double) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified double value to each element of the specified
     * range of the specified array of doubles.
     */
    @JvmStatic
    fun fill(a: DoubleArray, fromIndex: Int, toIndex: Int, `val`: Double) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified float value to each element of the specified array
     * of floats.
     */
    @JvmStatic
    fun fill(a: FloatArray, `val`: Float) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified float value to each element of the specified
     * range of the specified array of floats.
     */
    @JvmStatic
    fun fill(a: FloatArray, fromIndex: Int, toIndex: Int, `val`: Float) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Assigns the specified Object reference to each element of the specified
     * array of Objects.
     */
    @JvmStatic
    fun fill(a: Array<Any?>, `val`: Any?) {
        fill(a, 0, a.size, `val`)
    }

    /**
     * Assigns the specified Object reference to each element of the specified
     * range of the specified array of Objects.
     */
    @JvmStatic
    fun fill(a: Array<Any?>, fromIndex: Int, toIndex: Int, `val`: Any?) {
        rangeCheck(a.size, fromIndex, toIndex)
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun <T> copyOf(original: Array<T>, newLength: Int): Array<T> {
        @Suppress("UNCHECKED_CAST")
        return copyOf(original, newLength, original.javaClass as Class<out Array<T>>)
    }

    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T, U> copyOf(original: Array<U>, newLength: Int, newType: Class<out Array<T>>): Array<T> {
        val copy: Array<T> = if ((newType as Any) === (Array<Any>::class.java as Any))
            arrayOfNulls<Any>(newLength) as Array<T>
        else
            java.lang.reflect.Array.newInstance(newType.componentType, newLength) as Array<T>
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: ByteArray, newLength: Int): ByteArray {
        val copy = ByteArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: ShortArray, newLength: Int): ShortArray {
        val copy = ShortArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: IntArray, newLength: Int): IntArray {
        val copy = IntArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: LongArray, newLength: Int): LongArray {
        val copy = LongArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: CharArray, newLength: Int): CharArray {
        val copy = CharArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: FloatArray, newLength: Int): FloatArray {
        val copy = FloatArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: DoubleArray, newLength: Int): DoubleArray {
        val copy = DoubleArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified array, truncating or padding (if necessary)
     * so the copy has the specified length.
     */
    @JvmStatic
    fun copyOf(original: BooleanArray, newLength: Int): BooleanArray {
        val copy = BooleanArray(newLength)
        System.arraycopy(original, 0, copy, 0,
            Math.min(original.size, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun <T> copyOfRange(original: Array<T>, from: Int, to: Int): Array<T> {
        @Suppress("UNCHECKED_CAST")
        return copyOfRange(original, from, to, original.javaClass as Class<Array<T>>)
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T, U> copyOfRange(original: Array<U>, from: Int, to: Int,
                           newType: Class<out Array<T>>): Array<T> {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy: Array<T> = if ((newType as Any) === (Array<Any>::class.java as Any))
            arrayOfNulls<Any>(newLength) as Array<T>
        else
            java.lang.reflect.Array.newInstance(newType.componentType, newLength) as Array<T>
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = ByteArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: ShortArray, from: Int, to: Int): ShortArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = ShortArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: IntArray, from: Int, to: Int): IntArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = IntArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: LongArray, from: Int, to: Int): LongArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = LongArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: CharArray, from: Int, to: Int): CharArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = CharArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: FloatArray, from: Int, to: Int): FloatArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = FloatArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: DoubleArray, from: Int, to: Int): DoubleArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = DoubleArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Copies the specified range of the specified array into a new array.
     */
    @JvmStatic
    fun copyOfRange(original: BooleanArray, from: Int, to: Int): BooleanArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException("$from > $to")
        val copy = BooleanArray(newLength)
        System.arraycopy(original, from, copy, 0,
            Math.min(original.size - from, newLength))
        return copy
    }

    /**
     * Returns a fixed-size list backed by the specified array.
     */
    @JvmStatic
    fun <T> asList(vararg a: T): List<T> {
        @Suppress("UNCHECKED_CAST")
        return ArrayList(a as Array<T>)
    }

    /**
     * @serial include
     */
    private class ArrayList<E>(array: Array<E>?) : java.util.AbstractList<E>(), RandomAccess,
        java.io.Serializable {
        private val a: Array<E>

        init {
            if (array == null)
                throw NullPointerException()
            a = array
        }

        override val size: Int
            get() = a.size

        override fun toArray(): Array<Any?> {
            @Suppress("UNCHECKED_CAST")
            return (a as Array<Any?>).clone()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> toArray(a: Array<T>): Array<T> {
            val size = size
            if (a.size < size)
                return Arrays.copyOf(this.a, size, a.javaClass as Class<out Array<T>>)
            System.arraycopy(this.a, 0, a, 0, size)
            if (a.size > size)
                (a as Array<Any?>)[size] = null
            return a
        }

        override fun get(index: Int): E {
            return a[index]
        }

        override fun set(index: Int, element: E): E {
            val oldValue = a[index]
            a[index] = element
            return oldValue
        }

        override fun indexOf(element: E): Int {
            val o: Any? = element
            if (o == null) {
                for (i in a.indices)
                    if (a[i] == null)
                        return i
            } else {
                for (i in a.indices)
                    if (o.equals(a[i]))
                        return i
            }
            return -1
        }

        override fun contains(element: E): Boolean {
            return indexOf(element) != -1
        }

        companion object {
            private const val serialVersionUID = -2764017481108945198L
        }
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: LongArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            val elementHash = (element xor (element ushr 32)).toInt()
            result = 31 * result + elementHash
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: IntArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            result = 31 * result + element
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: ShortArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            result = 31 * result + element
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: CharArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            result = 31 * result + element.code
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: ByteArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            result = 31 * result + element
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: BooleanArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            result = 31 * result + (if (element) 1231 else 1237)
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: FloatArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            result = 31 * result + java.lang.Float.floatToIntBits(element)
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: DoubleArray?): Int {
        if (a == null)
            return 0

        var result = 1
        for (element in a) {
            val bits = java.lang.Double.doubleToLongBits(element)
            result = 31 * result + (bits xor (bits ushr 32)).toInt()
        }

        return result
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     */
    @JvmStatic
    fun hashCode(a: Array<out Any?>?): Int {
        if (a == null)
            return 0

        var result = 1

        for (element in a)
            result = 31 * result + (element?.hashCode() ?: 0)

        return result
    }

    /**
     * Returns a hash code based on the "deep contents" of the specified
     * array.
     */
    @JvmStatic
    fun deepHashCode(a: Array<out Any?>?): Int {
        if (a == null)
            return 0

        var result = 1

        for (element in a) {
            var elementHash = 0
            if (element is Array<*>)
                elementHash = deepHashCode(element)
            else if (element is ByteArray)
                elementHash = hashCode(element)
            else if (element is ShortArray)
                elementHash = hashCode(element)
            else if (element is IntArray)
                elementHash = hashCode(element)
            else if (element is LongArray)
                elementHash = hashCode(element)
            else if (element is CharArray)
                elementHash = hashCode(element)
            else if (element is FloatArray)
                elementHash = hashCode(element)
            else if (element is DoubleArray)
                elementHash = hashCode(element)
            else if (element is BooleanArray)
                elementHash = hashCode(element)
            else if (element != null)
                elementHash = element.hashCode()

            result = 31 * result + elementHash
        }

        return result
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays are <i>deeply
     * equal</i> to one another.
     */
    @JvmStatic
    fun deepEquals(a1: Array<out Any?>?, a2: Array<out Any?>?): Boolean {
        if (a1 === a2)
            return true
        if (a1 == null || a2 == null)
            return false
        val length = a1.size
        if (a2.size != length)
            return false

        for (i in 0 until length) {
            val e1 = a1[i]
            val e2 = a2[i]

            if (e1 === e2)
                continue
            if (e1 == null)
                return false

            // Figure out whether the two elements are equal
            val eq: Boolean
            if (e1 is Array<*> && e2 is Array<*>)
                eq = deepEquals(e1, e2)
            else if (e1 is ByteArray && e2 is ByteArray)
                eq = equals(e1, e2)
            else if (e1 is ShortArray && e2 is ShortArray)
                eq = equals(e1, e2)
            else if (e1 is IntArray && e2 is IntArray)
                eq = equals(e1, e2)
            else if (e1 is LongArray && e2 is LongArray)
                eq = equals(e1, e2)
            else if (e1 is CharArray && e2 is CharArray)
                eq = equals(e1, e2)
            else if (e1 is FloatArray && e2 is FloatArray)
                eq = equals(e1, e2)
            else if (e1 is DoubleArray && e2 is DoubleArray)
                eq = equals(e1, e2)
            else if (e1 is BooleanArray && e2 is BooleanArray)
                eq = equals(e1, e2)
            else
                eq = e1.equals(e2)

            if (!eq)
                return false
        }
        return true
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: LongArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i])
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: IntArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i])
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: ShortArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i].toInt())
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: CharArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i])
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: ByteArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i].toInt())
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: BooleanArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i])
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: FloatArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i])
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: DoubleArray?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i])
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     */
    @JvmStatic
    fun toString(a: Array<out Any?>?): String {
        if (a == null)
            return "null"
        val iMax = a.size - 1
        if (iMax == -1)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            b.append(a[i].toString())
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    /**
     * Returns a string representation of the "deep contents" of the specified
     * array.
     */
    @JvmStatic
    fun deepToString(a: Array<out Any?>?): String {
        if (a == null)
            return "null"

        var bufLen = 20 * a.size
        if (a.size != 0 && bufLen <= 0)
            bufLen = Int.MAX_VALUE
        val buf = StringBuilder(bufLen)
        deepToString(a, buf, HashSet())
        return buf.toString()
    }

    private fun deepToString(a: Array<out Any?>?, buf: StringBuilder, dejaVu: MutableSet<Any?>) {
        if (a == null) {
            buf.append("null")
            return
        }
        dejaVu.add(a)
        buf.append('[')
        for (i in a.indices) {
            if (i != 0)
                buf.append(", ")

            val element = a[i]
            if (element == null) {
                buf.append("null")
            } else {
                val eClass: Class<*> = element.javaClass

                if (eClass.isArray) {
                    if (eClass == ByteArray::class.java)
                        buf.append(toString(element as ByteArray))
                    else if (eClass == ShortArray::class.java)
                        buf.append(toString(element as ShortArray))
                    else if (eClass == IntArray::class.java)
                        buf.append(toString(element as IntArray))
                    else if (eClass == LongArray::class.java)
                        buf.append(toString(element as LongArray))
                    else if (eClass == CharArray::class.java)
                        buf.append(toString(element as CharArray))
                    else if (eClass == FloatArray::class.java)
                        buf.append(toString(element as FloatArray))
                    else if (eClass == DoubleArray::class.java)
                        buf.append(toString(element as DoubleArray))
                    else if (eClass == BooleanArray::class.java)
                        buf.append(toString(element as BooleanArray))
                    else { // element is an array of object references
                        if (dejaVu.contains(element))
                            buf.append("[...]")
                        else
                            deepToString(element as Array<*>, buf, dejaVu)
                    }
                } else { // element is non-null and not an array
                    buf.append(element.toString())
                }
            }
        }
        buf.append(']')
        dejaVu.remove(a)
    }
}

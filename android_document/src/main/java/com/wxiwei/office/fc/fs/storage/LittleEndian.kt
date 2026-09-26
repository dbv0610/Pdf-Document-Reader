package com.wxiwei.office.fc.fs.storage

object LittleEndian {
    const val BYTE_SIZE: Int = 1
    const val SHORT_SIZE: Int = 2
    const val INT_SIZE: Int = 4
    const val LONG_SIZE: Int = 8
    const val DOUBLE_SIZE: Int = 8

    /**
     * get a short value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the short (16-bit) value
     */
    fun getShort(data: ByteArray, offset: Int): Short {
        val b0 = data[offset].toInt() and 0xFF
        val b1 = data[offset + 1].toInt() and 0xFF
        return ((b1 shl 8) + (b0 shl 0)).toShort()
    }

    /**
     * get an unsigned short value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the unsigned short (16-bit) value in an integer
     */
    @JvmStatic
    fun getUShort(data: ByteArray, offset: Int): Int {
        val b0 = data[offset].toInt() and 0xFF
        val b1 = data[offset + 1].toInt() and 0xFF
        return (b1 shl 8) + (b0 shl 0)
    }

    /**
     * get a short value from the beginning of a byte array
     * 
     * @param  data  the byte array
     * @return       the short (16-bit) value
     */
    fun getShort(data: ByteArray): Short {
        return getShort(data, 0)
    }

    /**
     * get an unsigned short value from the beginning of a byte array
     * 
     * @param  data  the byte array
     * @return       the unsigned short (16-bit) value in an int
     */
    fun getUShort(data: ByteArray): Int {
        return getUShort(data, 0)
    }

    /**
     * get an int value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the int (32-bit) value
     */
    @JvmStatic
    fun getInt(data: ByteArray, offset: Int): Int {
        var i = offset
        val b0 = data[i++].toInt() and 0xFF
        val b1 = data[i++].toInt() and 0xFF
        val b2 = data[i++].toInt() and 0xFF
        val b3 = data[i++].toInt() and 0xFF
        return (b3 shl 24) + (b2 shl 16) + (b1 shl 8) + (b0 shl 0)
    }

    /**
     * get an int value from the beginning of a byte array
     * 
     * @param  data  the byte array
     * @return the int (32-bit) value
     */
    fun getInt(data: ByteArray): Int {
        return getInt(data, 0)
    }

    /**
     * get an unsigned int value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the unsigned int (32-bit) value in a long
     */
    @JvmStatic
    fun getUInt(data: ByteArray, offset: Int): Long {
        val retNum = getInt(data, offset).toLong()
        return retNum and 0x00FFFFFFFFL
    }

    /**
     * get an unsigned int value from a byte array
     * 
     * @param  data    the byte array
     * @return         the unsigned int (32-bit) value in a long
     */
    fun getUInt(data: ByteArray): Long {
        return getUInt(data, 0)
    }

    /**
     * get a long value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the long (64-bit) value
     */
    @JvmStatic
    fun getLong(data: ByteArray, offset: Int): Long {
        var result: Long = 0

        for (j in offset + LONG_SIZE - 1 downTo offset) {
            result = result shl 8
            result = result or (0xff and data[j].toInt()).toLong()
        }
        return result
    }
}

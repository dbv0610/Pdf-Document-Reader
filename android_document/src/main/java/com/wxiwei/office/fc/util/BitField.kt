package com.wxiwei.office.fc.util

class BitField(mask: Int) {
    private val mask: Int = mask
    private val shiftCount: Int

    init {
        var count = 0
        var bitPattern = mask
        if (bitPattern != 0) {
            while ((bitPattern and 1) == 0) {
                count++
                bitPattern = bitPattern shr 1
            }
        }
        shiftCount = count
    }

    fun getValue(holder: Int): Int = getRawValue(holder) ushr shiftCount
    fun getShortValue(holder: Short): Short = getValue(holder.toInt()).toShort()
    fun getRawValue(holder: Int): Int = holder and mask
    fun getShortRawValue(holder: Short): Short = getRawValue(holder.toInt()).toShort()
    fun isSet(holder: Int): Boolean = holder and mask != 0
    fun isAllSet(holder: Int): Boolean = holder and mask == mask
    fun setValue(holder: Int, value: Int): Int = (holder and mask.inv()) or ((value shl shiftCount) and mask)
    fun setShortValue(holder: Short, value: Short): Short = setValue(holder.toInt(), value.toInt()).toShort()
    fun clear(holder: Int): Int = holder and mask.inv()
    fun clearShort(holder: Short): Short = clear(holder.toInt()).toShort()
    fun clearByte(holder: Byte): Byte = clear(holder.toInt()).toByte()
    fun set(holder: Int): Int = holder or mask
    fun setShort(holder: Short): Short = set(holder.toInt()).toShort()
    fun setByte(holder: Byte): Byte = set(holder.toInt()).toByte()
    fun setBoolean(holder: Int, flag: Boolean): Int = if (flag) set(holder) else clear(holder)
    fun setShortBoolean(holder: Short, flag: Boolean): Short = if (flag) setShort(holder) else clearShort(holder)
    fun setByteBoolean(holder: Byte, flag: Boolean): Byte = if (flag) setByte(holder) else clearByte(holder)
}

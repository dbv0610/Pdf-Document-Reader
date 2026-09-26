package com.wxiwei.office.fc.fs.filesystem

import com.wxiwei.office.fc.fs.storage.LittleEndian

/**
 * 
 * A class describing attributes of the Big Block Size
 */
class BlockSize
    (
    val bigBlockSize: Int,
    /**
     * Returns the value that gets written into the
     * header.
     * Is the power of two that corresponds to the
     * size of the block, eg 512 => 9
     */
    val headerValue: Short
) {
    val propertiesPerBlock: Int
        get() = bigBlockSize / CFBConstants.Companion.PROPERTY_SIZE

    val bATEntriesPerBlock: Int
        get() = bigBlockSize / LittleEndian.INT_SIZE

    val xBATEntriesPerBlock: Int
        get() = this.bATEntriesPerBlock - 1

    val nextXBATChainOffset: Int
        get() = this.xBATEntriesPerBlock * LittleEndian.INT_SIZE
}

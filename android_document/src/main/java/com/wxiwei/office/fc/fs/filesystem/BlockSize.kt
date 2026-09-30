/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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

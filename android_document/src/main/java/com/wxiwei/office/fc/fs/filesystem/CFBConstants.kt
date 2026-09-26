package com.wxiwei.office.fc.fs.filesystem


/**
 * 
 * A repository for constants shared by POI classes.
 */
interface CFBConstants {
    companion object {
        /** Most files use 512 bytes as their big block size  */
        const val SMALLER_BIG_BLOCK_SIZE: Int = 0x0200
        val SMALLER_BIG_BLOCK_SIZE_DETAILS: BlockSize =
            BlockSize(SMALLER_BIG_BLOCK_SIZE, 9.toShort())

        /** Some use 4096 bytes  */
        const val LARGER_BIG_BLOCK_SIZE: Int = 0x1000
        val LARGER_BIG_BLOCK_SIZE_DETAILS: BlockSize =
            BlockSize(LARGER_BIG_BLOCK_SIZE, 12.toShort())

        /** How big a block in the small block stream is. Fixed size  */
        const val SMALL_BLOCK_SIZE: Int = 0x0040

        /** How big a single property is  */
        const val PROPERTY_SIZE: Int = 0x0080

        /**
         * The minimum size of a document before it's stored using
         * Big Blocks (normal streams). Smaller documents go in the
         * Mini Stream (SBAT / Small Blocks)
         */
        const val BIG_BLOCK_MINIMUM_DOCUMENT_SIZE: Int = 0x1000

        /** The highest sector number you're allowed, 0xFFFFFFFA  */
        val LARGEST_REGULAR_SECTOR_NUMBER: Int = -5

        /** Indicates the sector holds a DIFAT block (0xFFFFFFFC)  */
        val DIFAT_SECTOR_BLOCK: Int = -4

        /** Indicates the sector holds a FAT block (0xFFFFFFFD)  */
        val FAT_SECTOR_BLOCK: Int = -3

        /** Indicates the sector is the end of a chain (0xFFFFFFFE)  */
        val END_OF_CHAIN: Int = -2

        /** Indicates the sector is not used (0xFFFFFFFF)  */
        val UNUSED_BLOCK: Int = -1
    }
}

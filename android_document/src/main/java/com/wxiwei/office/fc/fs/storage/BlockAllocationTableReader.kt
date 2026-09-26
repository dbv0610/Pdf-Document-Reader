package com.wxiwei.office.fc.fs.storage

import com.wxiwei.office.fc.fs.filesystem.BlockSize
import com.wxiwei.office.fc.fs.filesystem.CFBConstants
import java.io.IOException
import kotlin.math.min

/**
 * This class manages and creates the Block Allocation Table, which is
 * basically a set of linked lists of block indices.
 * <P>
 * Each block of the filesystem has an index. The first block, the
 * header, is skipped; the first block after the header is index 0,
 * the next is index 1, and so on.
</P> * <P>
 * A block's index is also its index into the Block Allocation
 * Table. The entry that it finds in the Block Allocation Table is the
 * index of the next block in the linked list of blocks making up a
 * file, or it is set to -2: end of list.
 * 
</P> */
class BlockAllocationTableReader {
    /**
     * Maximum number size (in blocks) of the allocation table as supported by
     * POI.<br></br>
     * 
     * This constant has been chosen to help POI identify corrupted data in the
     * header block (rather than crash immediately with [OutOfMemoryError]
     * ). It's not clear if the compound document format actually specifies any
     * upper limits. For files with 512 byte blocks, having an allocation table
     * of 65,335 blocks would correspond to a total file size of 4GB. Needless
     * to say, POI probably cannot handle files anywhere near that size.
     */
    private val _entries: IntList
    private val bigBlockSize: BlockSize

    /**
     * create a BlockAllocationTableReader for an existing filesystem. Side
     * effect: when this method finishes, the BAT blocks will have
     * been removed from the raw block list, and any blocks labeled as
     * 'unused' in the block allocation table will also have been
     * removed from the raw block list.
     * 
     * @param block_count the number of BAT blocks making up the block
     * allocation table
     * @param block_array the array of BAT block indices from the
     * filesystem's header
     * @param xbat_count the number of XBAT blocks
     * @param xbat_index the index of the first XBAT block
     * @param raw_block_list the list of RawDataBlocks
     * 
     * @exception IOException if, in trying to create the table, we
     * encounter logic errors
     */
    constructor(
        bigBlockSize: BlockSize, block_count: Int,
        block_array: IntArray, xbat_count: Int, xbat_index: Int, raw_block_list: BlockList
    ) {
        this.bigBlockSize = bigBlockSize
        _entries = IntList()
        var limit = min(block_count, block_array.size)
        var block_index: Int

        // This will hold all of the BAT blocks in order
        val blocks: Array<RawDataBlock?>? = arrayOfNulls<RawDataBlock>(block_count)
        // Process the first (up to) 109 BAT blocks
        block_index = 0
        while (block_index < limit) {
            // Check that the sector number of the BAT block is a valid one
            val nextOffset = block_array[block_index]
            if (nextOffset > raw_block_list.blockCount()) {
                throw IOException(
                    ("Your file contains " + raw_block_list.blockCount()
                            + " sectors, but the initial DIFAT array at index " + block_index
                            + " referenced block # " + nextOffset + ". This isn't allowed and "
                            + " your file is corrupt")
                )
            }
            // Record the sector number of this BAT block 
            blocks!![block_index] = raw_block_list.remove(nextOffset)
            block_index++
        }

        // Process additional BAT blocks via the XBATs
        if (block_index < block_count) {
            // must have extended blocks

            if (xbat_index < 0) {
                throw IOException(
                    "BAT count exceeds limit, yet XBAT index indicates no valid entries"
                )
            }
            var chain_index = xbat_index
            val max_entries_per_block = bigBlockSize.xBATEntriesPerBlock
            val chain_index_offset = bigBlockSize.nextXBATChainOffset

            // Each XBAT block contains either:
            //  (maximum number of sector indexes) + index of next XBAT
            //  some sector indexes + FREE sectors to max # + EndOfChain
            for (j in 0..<xbat_count) {
                limit = min(block_count - block_index, max_entries_per_block)
                val data = raw_block_list.remove(chain_index)!!.data
                var offset = 0

                for (k in 0..<limit) {
                    blocks!![block_index++] =
                        raw_block_list.remove(LittleEndian.getInt(data, offset))
                    offset += LittleEndian.INT_SIZE
                }
                chain_index = LittleEndian.getInt(data, chain_index_offset)
                if (chain_index == CFBConstants.Companion.END_OF_CHAIN) {
                    break
                }
            }
        }
        if (block_index != block_count) {
            throw IOException("Could not find all blocks")
        }

        // Now that we have all of the raw data blocks which make
        //  up the FAT, go through and create the indices
        setEntries(blocks!!, raw_block_list)
    }

    /**
     * create a BlockAllocationTableReader from an array of raw data blocks
     * 
     * @param blocks the raw data
     * @param raw_block_list the list holding the managed blocks
     * 
     * @exception IOException
     */
    constructor(
        bigBlockSize: BlockSize, blocks: Array<RawDataBlock?>,
        raw_block_list: BlockList
    ) {
        this.bigBlockSize = bigBlockSize
        _entries = IntList()
        setEntries(blocks, raw_block_list)
    }

    /**
     * walk the entries from a specified point and return the
     * associated blocks. The associated blocks are removed from the
     * block list
     * 
     * @param startBlock the first block in the chain
     * @param blockList the raw data block list
     * 
     * @return array of ListManagedBlocks, in their correct order
     * 
     * @exception IOException if there is a problem acquiring the blocks
     */
    @Throws(IOException::class)
    fun fetchBlocks(
        startBlock: Int, headerPropertiesStartBlock: Int,
        blockList: BlockList
    ): Array<RawDataBlock?> {
        val blocks: MutableList<RawDataBlock?> = ArrayList<RawDataBlock?>()
        var currentBlock = startBlock
        var firstPass = true
        var dataBlock: RawDataBlock? = null

        // Process the chain from the start to the end
        // Normally we have header, data, end
        // Sometimes we have data, header, end
        // For those cases, stop at the header, not the end
        while (currentBlock != CFBConstants.Companion.END_OF_CHAIN) {
            try {
                // Grab the data at the current block offset
                dataBlock = blockList.remove(currentBlock)
                blocks.add(dataBlock)
                // Now figure out which block we go to next
                currentBlock = _entries.get(currentBlock)
                firstPass = false
            } catch (e: IOException) {
                if (currentBlock == headerPropertiesStartBlock) {
                    currentBlock = CFBConstants.Companion.END_OF_CHAIN
                } else if (currentBlock == 0 && firstPass) {
                    currentBlock = CFBConstants.Companion.END_OF_CHAIN
                } else {
                    // Ripple up
                    throw e
                }
            }
        }

        return blocks.toTypedArray<RawDataBlock?>()
    }

    /**
     * Convert an array of blocks into a set of integer indices
     * 
     * @param blocks the array of blocks containing the indices
     * @param raw_blocks the list of blocks being managed. Unused
     * blocks will be eliminated from the list
     */
    @Throws(IOException::class)
    private fun setEntries(blocks: Array<RawDataBlock?>, raw_blocks: BlockList) {
        val limit = bigBlockSize.bATEntriesPerBlock

        for (block_index in blocks.indices) {
            val data = blocks[block_index]!!.data
            var offset = 0
            for (k in 0..<limit) {
                val entry = LittleEndian.getInt(data, offset)

                if (entry == CFBConstants.Companion.UNUSED_BLOCK) {
                    raw_blocks.zap(_entries.size())
                }
                _entries.add(entry)
                offset += LittleEndian.INT_SIZE
            }

            // discard block
            blocks[block_index] = null
        }
        raw_blocks.setBAT(this)
    }
}

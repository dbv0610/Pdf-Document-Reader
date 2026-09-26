package com.wxiwei.office.fc.fs.storage

import com.wxiwei.office.fc.fs.filesystem.BlockSize
import java.io.IOException
import java.io.InputStream

/**
 * A simple implementation of BlockList
 */
class BlockList {
    /**
     * Constructor RawDataBlockList
     * 
     * @param stream the InputStream from which the data will be read
     * @param bigBlockSize The big block size, either 512 bytes or 4096 bytes
     * 
     * @exception IOException on I/O errors, and if an incomplete
     * block is read
     */
    constructor(stream: InputStream, bigBlockSize: BlockSize) {
        val blocks: MutableList<RawDataBlock?> = ArrayList<RawDataBlock?>()
        val size = bigBlockSize.bigBlockSize
        while (true) {
            val b = ByteArray(size)
            val count = stream.read(b)
            if (count <= 0) {
                break
            }
            val block = RawDataBlock(b)
            blocks.add(block)
            if (count != size) {
                break
            }
        }
        _blocks = blocks.toTypedArray<RawDataBlock?>()
    }

    /**
     * 
     * @param _blocks
     */
    constructor(_blocks: Array<RawDataBlock?>) {
        this._blocks = _blocks
    }

    /**
     * remove the specified block from the list
     * 
     * @param index the index of the specified block; if the index is
     * out of range, that's ok
     */
    fun zap(index: Int) {
        if ((index >= 0) && (index < _blocks.size)) {
            _blocks[index] = null
        }
    }

    /**
     * Unit testing method. Gets, without sanity checks or
     * removing.
     */
    protected fun get(index: Int): RawDataBlock? {
        return _blocks[index]
    }

    /**
     * remove and return the specified block from the list
     * 
     * @param index the index of the specified block
     * 
     * @return the specified block
     * 
     * @exception IOException if the index is out of range or has
     * already been removed
     */
    @Throws(IOException::class)
    fun remove(index: Int): RawDataBlock? {
        if (index < 0 || index >= _blocks.size) {
            return null
        }
        val result = _blocks[index]
        _blocks[index] = null
        return result
    }

    /**
     * get the blocks making up a particular stream in the list. The
     * blocks are removed from the list.
     * 
     * @param startBlock the index of the first block in the stream
     * 
     * @return the stream as an array of correctly ordered blocks
     * 
     * @exception IOException if blocks are missing
     */
    @Throws(IOException::class)
    fun fetchBlocks(startBlock: Int, headerPropertiesStartBlock: Int): Array<RawDataBlock?> {
        if (_bat == null) {
            throw IOException("Improperly initialized list: no block allocation table provided")
        }
        return _bat!!.fetchBlocks(startBlock, headerPropertiesStartBlock, this)
    }

    /**
     * set the associated BlockAllocationTable
     * 
     * @param bat the associated BlockAllocationTable
     */
    @Throws(IOException::class)
    fun setBAT(bat: BlockAllocationTableReader) {
        _bat = bat
    }

    /**
     * Returns the count of the number of blocks
     */
    fun blockCount(): Int {
        return _blocks.size
    }

    //
    private val _blocks: Array<RawDataBlock?>

    //
    private var _bat: BlockAllocationTableReader? = null
}

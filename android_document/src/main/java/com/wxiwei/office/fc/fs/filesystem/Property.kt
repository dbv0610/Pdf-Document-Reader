package com.wxiwei.office.fc.fs.filesystem

import com.wxiwei.office.fc.fs.storage.LittleEndian
import com.wxiwei.office.fc.fs.storage.RawDataBlock
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * This abstract base class is the ancestor of all classes
 * implementing POIFS Property behavior.
 * 
 */
class Property
    (index: Int, array: ByteArray, offset: Int) {
    /**
     * Based on the currently defined size, should this property use
     * small blocks?
     * 
     * @return true if the size is less than _big_block_minimum_bytes
     */
    fun shouldUseSmallBlocks(): Boolean {
        return this.size < _big_block_minimum_bytes
    }

    val propertyRawDataSize: Long
        /**
         * 
         * @param propertyName
         * @return
         */
        get() {
            if (blocks != null) {
                return (blocks!![0]!!.data.size * blocks!!.size).toLong()
            }
            return documentRawData!!.size.toLong()
        }

    /**
     * @return Returns the blocks.
     */
    fun getBlocks(): Array<RawDataBlock?>? {
        return blocks
    }

    /**
     * @param blocks The blocks to set.
     */
    fun setBlocks(blocks: Array<RawDataBlock?>) {
        this.blocks = blocks
        blockSize = blocks[0]!!.data.size
    }

    val isDocument: Boolean
        /**
         * 
         */
        get() = _property_type == DOCUMENT_TYPE

    val isDirectory: Boolean
        /**
         * 
         */
        get() = _property_type == DIRECTORY_TYPE

    val isRoot: Boolean
        /**
         * 
         */
        get() = _property_type == ROOT_TYPE

    /**
     * get an unsigned short value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the unsigned short (16-bit) value in an integer
     */
    fun getUShort(offset: Int): Int {
        val b0 = getByteForOffset(offset)
        val b1 = getByteForOffset(offset + 1)

        return (b1 shl 8) + (b0 shl 0)
    }

    fun getUInt(offset: Int): Long {
        val retNum = getInt(offset).toLong()
        return retNum and 0x00FFFFFFFFL
    }

    /**
     * get an int value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the int (32-bit) value
     */
    fun getInt(offset: Int): Int {
        val b0 = getByteForOffset(offset)
        val b1 = getByteForOffset(offset + 1)
        val b2 = getByteForOffset(offset + 2)
        val b3 = getByteForOffset(offset + 3)


        /*int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;*/
        return (b3 shl 24) + (b2 shl 16) + (b1 shl 8) + (b0 shl 0)
    }

    /**
     * get a long value from a byte array
     * 
     * @param  data    the byte array
     * @param  offset  a starting offset into the byte array
     * @return         the long (64-bit) value
     */
    fun getLong(offset: Int): Long {
        var result: Long = 0
        for (j in offset + 8 - 1 downTo offset) {
            result = result shl 8
            result = result or (0xff and getByteForOffset(j)).toLong()
        }
        return result
    }


    /**
     * 
     * @param out
     * @param offset
     * @param len
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeByte(out: OutputStream, offset: Int, len: Int) {
        //write BLOCKNUMBER blocks one time 
        var len = len
        val BLOCKNUMBER = 16

        val length = min(len, blockSize * BLOCKNUMBER)
        var data: ByteArray? = ByteArray(length)
        var index = getBlockIndexForOffset(offset)


        //the first data in the index-th block
        val off = offset - blockSize * index
        var writeLen = min(len, blockSize - off)
        System.arraycopy(blocks!![index]!!.data, off, data, 0, writeLen)
        var blockCnt = 1
        while (writeLen <= len && index < blocks!!.size) {
            if (blockCnt < BLOCKNUMBER) {
                index++
                blockCnt++
                if (writeLen + blockSize > len) {
                    //the rest data
                    if (len > writeLen && index < blocks!!.size) {
                        System.arraycopy(
                            blocks!![index]!!.data,
                            0,
                            data,
                            writeLen,
                            len - writeLen
                        )
                    }
                    out.write(data, 0, len)
                    break
                }
                System.arraycopy(blocks!![index]!!.data, 0, data, writeLen, blockSize)
                writeLen += blockSize
            } else {
                //has composite BLOCKNUMBER blocks data, then write it to out
                out.write(data, 0, writeLen)


                //update state
                len -= writeLen
                blockCnt = 0
                writeLen = 0
            }
        }

        data = null
    }


    /**
     * 
     */
    private fun getBlockIndexForOffset(offset: Int): Int {
        return offset / blockSize
    }

    /**
     * 
     */
    private fun getByteForOffset(offset: Int): Int {
        val index = offset / blockSize
        val off = offset - blockSize * index
        return blocks!![index]!!.data[off].toInt() and 0xFF
    }

    fun getRecordData(usrOffset: Int): ByteArray? {
        //len
        var rlen = getUInt(usrOffset + 4).toInt() + 8

        // Sanity check the length
        if (rlen < 0) {
            rlen = 0
        }

        if (documentRawData == null || documentRawData!!.size < rlen) {
            documentRawData = ByteArray(max(rlen, blockSize))
        }


        //save record data to documentRawData        
        val startIndex = usrOffset / blockSize
        val endIndex = (usrOffset + rlen) / blockSize

        if (endIndex > startIndex) {
            //multi blocks
            var off = usrOffset % blockSize
            //first block
            System.arraycopy(
                blocks!![startIndex]!!.data,
                off,
                documentRawData,
                0,
                blockSize - off
            )

            off = blockSize - off
            //middle whole block
            if (startIndex + 1 < endIndex) {
                for (i in startIndex + 1..<endIndex) {
                    System.arraycopy(blocks!![i]!!.data, 0, documentRawData, off, blockSize)
                    off = off + blockSize
                }
            }


            //last block
            if (endIndex < blocks!!.size) {
                System.arraycopy(
                    blocks!![endIndex]!!.data,
                    0,
                    documentRawData,
                    off,
                    (usrOffset + rlen) % blockSize
                )
            }
        } else {
            //single block
            val off = usrOffset % blockSize
            System.arraycopy(blocks!![startIndex]!!.data, off, documentRawData, 0, rlen)
        }
        return documentRawData
    }

    /**
     * 
     * @param property
     */
    fun addChildProperty(property: Property) {
        properties!!.put(property.name, property)
    }

    /**
     * 
     */
    fun getChlidProperty(name: String?): Property? {
        return properties!!.get(name)
    }

    /**
     * 
     */
    fun dispose() {
        //_raw_data = null;
        documentRawData = null
        this.name = null
        blocks = null
        if (properties != null) {
            val set = properties!!.keys
            for (key in set) {
                properties!!.get(key)!!.dispose()
            }
            properties!!.clear()
            properties = null
        }
    }

    /**
     * Get the name of this property
     * 
     * @return property name as String
     */
    var name: String? = null
        private set
    private val _name_size: Short
    private var _property_type: Byte

    /**
     * @return the start block
     */
    val startBlock: Int

    /**
     * find out the document size
     * 
     * @return size in bytes
     */
    val size: Int

    /**
     * 
     */
    val childPropertyIndex: Int

    /**
     * 
     */
    val nextPropertyIndex: Int

    /**
     * 
     * @return
     */
    val previousPropertyIndex: Int
    /**
     * 
     * @return
     */
    /**
     * 
     * @return
     */
    //private byte[] _raw_data;
    @JvmField
    var documentRawData: ByteArray? = null

    //
    private var blocks: Array<RawDataBlock?>? = null

    //
    private var blockSize = 0

    //
    var properties: MutableMap<String?, Property?>? = HashMap<String?, Property?>()

    /**
     * Constructor from byte data
     * 
     * @param index index number
     * @param array byte data
     * @param offset offset into byte data
     */
    init {
        //_raw_data = new byte[CFBConstants.PROPERTY_SIZE];
        //System.arraycopy(array, offset, _raw_data, 0, CFBConstants.PROPERTY_SIZE);
        //
        _name_size = LittleEndian.getShort(array, NAME_SIZE_OFFSET + offset)
        //
        this.previousPropertyIndex =
            LittleEndian.getShort(array, PREVIOUS_PROPERTY_OFFSET + offset).toInt()
        //
        this.nextPropertyIndex = LittleEndian.getShort(array, NEXT_PROPERTY_OFFSET + offset).toInt()
        //
        this.childPropertyIndex =
            LittleEndian.getShort(array, CHILD_PROPERTY_OFFSET + offset).toInt()
        //
        this.startBlock = LittleEndian.getInt(array, START_BLOCK_OFFSET + offset)
        //
        this.size = LittleEndian.getInt(array, SIZE_OFFSET + offset)
        //
        _property_type = array[PROPERTY_TYPE_OFFSET + offset]
        val name_length = (_name_size / LittleEndian.SHORT_SIZE) - 1

        if (name_length < 1) {
            if (_property_type == ROOT_TYPE) {
                this.name = "Root Entry"
            } else {
                this.name = "aaa"
            }
        } else {
            val char_array = CharArray(name_length)
            var name_offset = 0

            for (j in 0..<name_length) {
                char_array[j] = Char(LittleEndian.getShort(array, name_offset + offset).toUShort())
                name_offset += LittleEndian.SHORT_SIZE
            }
            this.name = String(char_array, 0, name_length)
        }
    }

    companion object {
        const val PROPERTY_TYPE_OFFSET: Int = 0x42

        // the property types
        const val DIRECTORY_TYPE: Byte = 1
        const val DOCUMENT_TYPE: Byte = 2
        const val ROOT_TYPE: Byte = 5

        private const val NAME_SIZE_OFFSET = 0x40

        //
        private const val PREVIOUS_PROPERTY_OFFSET = 0x44

        //
        private const val NEXT_PROPERTY_OFFSET = 0x48

        //
        private const val CHILD_PROPERTY_OFFSET = 0x4c

        //private static final  int _max_name_length = (_name_size_offset / LittleEndianConsts.SHORT_SIZE) - 1;
        protected val _NO_INDEX: Int = -1

        // documents must be at least this size to be stored in big blocks
        private val _big_block_minimum_bytes: Int =
            CFBConstants.Companion.BIG_BLOCK_MINIMUM_DOCUMENT_SIZE

        private const val START_BLOCK_OFFSET = 0x74
        private const val SIZE_OFFSET = 0x78

        // node colors
        protected const val _NODE_BLACK: Byte = 1
        protected const val _NODE_RED: Byte = 0
    }
}

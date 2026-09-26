package com.wxiwei.office.fc.fs.storage

import com.wxiwei.office.fc.fs.filesystem.BlockSize
import com.wxiwei.office.fc.fs.filesystem.CFBConstants
import com.wxiwei.office.fc.util.HexDump
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

/**
 * The block containing the archive header
 */
class HeaderBlock
    (stream: InputStream) {
    /**
     * @return The Big Block size, normally 512 bytes, sometimes 4096 bytes
     */
    /**
     * What big block size the file uses. Most files
     * use 512 bytes, but a few use 4096
     */
    var bigBlockSize: BlockSize? = null
        private set

    /**
     * @return number of BAT blocks
     */
    /**
     * Sets the number of BAT blocks that are used.
     * This is the number used in both the BAT and XBAT.
     */
    /**
     * Number of big block allocation table blocks (int).
     * (Number of FAT Sectors in Microsoft parlance).
     */
    var bATCount: Int

    /**
     * get start of Property Table
     * 
     * @return the index of the first block of the Property Table
     */
    /**
     * Set start of Property Table
     * 
     * @param startBlock the index of the first block of the Property Table
     */
    /**
     * Start of the property set block (int index of the property set
     * chain's first big block).
     */
    var propertyStart: Int

    /**
     * @return start of small block (MiniFAT) allocation table
     */
    /**
     * Set start of small block allocation table
     * 
     * @param startBlock the index of the first big block of the small
     * block allocation table
     */
    /**
     * start of the small block allocation table (int index of small
     * block allocation table's first big block)
     */
    var sBATStart: Int

    /**
     * Number of small block allocation table blocks (int)
     * (Number of MiniFAT Sectors in Microsoft parlance)
     */
    var sBATCount: Int
        private set

    /**
     * @return XBAT (DIFAT) index
     */
    /**
     * Big block index for extension to the big block allocation table
     */
    val xBATIndex: Int
    /**
     * @return XBAT (DIFAT) count
     */
    /**
     * Number of big block allocation table blocks (int)
     * (Number of DIFAT Sectors in Microsoft parlance)
     */
    val xBATCount: Int

    /**
     * The data. Only ever 512 bytes, because 4096 byte
     * files use zeros for the extra header space.
     */
    private var _data: ByteArray?

    /**
     * create a new HeaderBlockReader from an InputStream
     * 
     * @param stream the source InputStream
     * 
     * @exception IOException on errors or bad data
     */
    init {
        // Grab the first 512 bytes
        val data = ByteArray(512)
        _data = data
        stream.read(data)
        // verify signature
        val signature = LittleEndian.getLong(data, _signature_offset)

        if (signature != _signature) {
            // Is it one of the usual suspects?
            /*byte[] OOXML_FILE_HEADER = POIFSConstants.OOXML_FILE_HEADER;
            if (_data[0] == OOXML_FILE_HEADER[0] && _data[1] == OOXML_FILE_HEADER[1]
                && _data[2] == OOXML_FILE_HEADER[2] && _data[3] == OOXML_FILE_HEADER[3])
            {
                throw new OfficeXmlFileException(
                    "The supplied data appears to be in the Office 2007+ XML. You are calling the part of POI that deals with OLE2 Office Documents. You need to call a different part of POI to process this data (eg XSSF instead of HSSF)");
            }
            if ((signature & 0xFF8FFFFFFFFFFFFFL) == 0x0010000200040009L)
            {
                // BIFF2 raw stream starts with BOF (sid=0x0009, size=0x0004, data=0x00t0)
                throw new IllegalArgumentException(
                    "The supplied data appears to be in BIFF2 format.  "
                        + "POI only supports BIFF8 format");
            }*/

            // Give a generic error

            throw IOException(
                ("Invalid header signature; read " + longToHex(signature)
                        + ", expected " + longToHex(_signature))
            )
        }
        // Figure out our block size
        if (data[30].toInt() == 12) {
            this.bigBlockSize = CFBConstants.Companion.LARGER_BIG_BLOCK_SIZE_DETAILS
        } else if (data[30].toInt() == 9) {
            this.bigBlockSize = CFBConstants.Companion.SMALLER_BIG_BLOCK_SIZE_DETAILS
        } else {
            throw IOException(
                ("Unsupported blocksize  (2^" + data[30]
                        + "). Expected 2^9 or 2^12.")
            )
        }

        // Setup the fields to read and write the counts and starts
        this.bATCount = LittleEndian.getInt(data, _bat_count_offset)
        this.propertyStart = LittleEndian.getInt(data, _property_start_offset)
        this.sBATStart = LittleEndian.getInt(data, _sbat_start_offset)
        this.sBATCount = LittleEndian.getInt(data, _sbat_block_count_offset)
        this.xBATIndex = LittleEndian.getInt(data, _xbat_start_offset)
        this.xBATCount = LittleEndian.getInt(data, _xbat_count_offset)
    }

    /**
     * 
     * @param value
     * @return
     */
    private fun longToHex(value: Long): String {
        return String(HexDump.longToHex(value))
    }

    /**
     * Set count of SBAT blocks
     * 
     * @param count the number of SBAT blocks
     */
    fun setSBATBlockCount(count: Int) {
        this.sBATCount = count
    }

    val bATArray: IntArray
        /**
         * Returns the offsets to the first (up to) 109
         * BAT sectors.
         * Any additional BAT sectors are held in the XBAT (DIFAT)
         * sectors in a chain.
         * @return BAT offset array
         */
        get() {
            // Read them in
            val data = _data!!
            val result = IntArray(
                min(
                    this.bATCount,
                    _max_bats_in_header
                )
            )
            var offset: Int =
                _bat_array_offset
            for (j in result.indices) {
                result[j] = LittleEndian.getInt(data, offset)
                offset += LittleEndian.INT_SIZE
            }
            return result
        }

    /**
     * 
     */
    fun dispose() {
        _data = null
        bigBlockSize = null
    }

    companion object {
        const val _signature: Long = -0x1ee54e5e1fee3030L
        const val _bat_array_offset: Int = 0x4c
        val _max_bats_in_header: Int =
            (CFBConstants.Companion.SMALLER_BIG_BLOCK_SIZE - _bat_array_offset) / 4

        // useful offsets
        const val _signature_offset: Int = 0
        const val _bat_count_offset: Int = 0x2C
        const val _property_start_offset: Int = 0x30
        const val _sbat_start_offset: Int = 0x3C
        const val _sbat_block_count_offset: Int = 0x40
        const val _xbat_start_offset: Int = 0x44
        const val _xbat_count_offset: Int = 0x48
    }
}

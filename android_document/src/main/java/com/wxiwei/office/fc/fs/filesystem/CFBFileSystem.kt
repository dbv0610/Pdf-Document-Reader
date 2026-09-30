/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.fc.fs.filesystem

import com.wxiwei.office.fc.fs.storage.BlockAllocationTableReader
import com.wxiwei.office.fc.fs.storage.BlockList
import com.wxiwei.office.fc.fs.storage.HeaderBlock
import com.wxiwei.office.fc.fs.storage.RawDataBlock
import java.io.IOException
import java.io.InputStream
import java.util.Stack

/**
 * This is the main class of the POIFS system; it manages the entire
 * life cycle of the filesystem.
 * 
 * @author Marc Johnson (mjohnson at apache dot org)
 */
class CFBFileSystem
@JvmOverloads constructor(
    stream: InputStream, //
    private val isGetThumbnail: Boolean = false
) {
    /**
     * What big block size the file uses. Most files
     * use 512 bytes, but a few use 4096
     */
    private var bigBlockSize: BlockSize = CFBConstants.Companion.SMALLER_BIG_BLOCK_SIZE_DETAILS

    /**
     * 
     */
    private fun createPropertyTree(directory: Property, properties: MutableList<Property?>) {
        var index = directory.childPropertyIndex
        if (index < 0) {
            return
        }
        var property: Property
        val children = Stack<Property>()
        children.push(properties.get(index)!!)
        while (!children.isEmpty()) {
            property = children.pop()
            directory.addChildProperty(property)
            if (property.isDirectory) {
                createPropertyTree(property, properties)
            }
            // 前一个
            index = property.previousPropertyIndex
            if (index >= 0) {
                children.push(properties.get(index)!!)
            }
            // 下一个
            index = property.nextPropertyIndex
            if (index >= 0) {
                children.push(properties.get(index)!!)
            }
        }
    }

    /**
     * Convert raw data blocks to an array of Property's
     * 
     * @param blocks to be converted
     * 
     * @return the converted List of Property objects. May contain
     * nulls, but will not be null
     * 
     * @exception IOException if any of the blocks are empty
     */
    @Throws(IOException::class)
    private fun readProperties(
        propertyBlocks: Array<RawDataBlock?>,
        rawBlockList: BlockList?,
        properties: MutableList<Property?>
    ) {
        for (j in propertyBlocks.indices) {
            val data = propertyBlocks[j]!!.data
            val property_count: Int = data.size / CFBConstants.Companion.PROPERTY_SIZE
            var offset = 0

            for (k in 0..<property_count) {
                when (data[offset + Property.Companion.PROPERTY_TYPE_OFFSET]) {
                    Property.Companion.DIRECTORY_TYPE -> properties.add(
                        Property(
                            properties.size,
                            data,
                            offset
                        )
                    )

                    Property.Companion.DOCUMENT_TYPE -> properties.add(
                        Property(
                            properties.size,
                            data,
                            offset
                        )
                    )

                    Property.Companion.ROOT_TYPE -> {
                        root = Property(properties.size, data, offset)
                        properties.add(root)
                    }
                }
                offset += CFBConstants.Companion.PROPERTY_SIZE
            }
        }
    }

    /**
     * 
     */
    @Throws(IOException::class)
    private fun readSmallRawDataBlock(rawDataBlockList: BlockList): BlockList {
        val block_size = 64
        val smallRawDataBlocks = rawDataBlockList.fetchBlocks(root!!.startBlock, -1)

        val _blocks_per_big_block = headerBlock!!.bigBlockSize!!.bigBlockSize / block_size
        val sdbs: MutableList<RawDataBlock?> = ArrayList<RawDataBlock?>()
        for (j in smallRawDataBlocks.indices) {
            val data = smallRawDataBlocks[j]!!.data

            for (k in 0..<_blocks_per_big_block) {
                val smallData = ByteArray(block_size)
                System.arraycopy(data, k * block_size, smallData, 0, block_size)
                sdbs.add(RawDataBlock(smallData))
            }
        }
        val smallBlockList = BlockList(sdbs.toTypedArray<RawDataBlock?>())
        BlockAllocationTableReader(
            bigBlockSize,
            rawDataBlockList.fetchBlocks(headerBlock!!.sBATStart, -1),
            smallBlockList
        )
        return smallBlockList
    }

    /**
     * 
     * @param smallBlockList
     * @param rawBlockList
     */
    @Throws(IOException::class)
    private fun readPrepertiesRawData(
        smallBlockList: BlockList,
        rawBlockList: BlockList,
        directory: Property
    ) {
        val ite = directory.properties!!.values.iterator()
        while (ite.hasNext()) {
            val property = ite.next()!!
            if (property.isDocument) {
                getPropertyRawData(property, smallBlockList, rawBlockList)
            } else if (property.isDirectory) {
                readPrepertiesRawData(smallBlockList, rawBlockList, property)
            }
        }
    }

    /**
     * 
     * @param property
     * @param rawBlockList
     */
    @Throws(IOException::class)
    private fun getPropertyRawData(
        property: Property,
        smallBlockList: BlockList,
        rawBlockList: BlockList
    ) {
        val name = property.name!!
        val startBlock = property.startBlock
        val blocks: Array<RawDataBlock?>?
        if (property.shouldUseSmallBlocks()) {
            blocks = smallBlockList.fetchBlocks(startBlock, headerBlock!!.propertyStart)
        } else {
            blocks = rawBlockList.fetchBlocks(startBlock, headerBlock!!.propertyStart)
        }
        if (blocks == null || blocks.size == 0) {
            return
        }

        if (name == "Pictures"
            || name.endsWith("WorkBook")
            || name == "PowerPoint Document"
            || name.endsWith("Ole")
            || name.endsWith("ObjInfo")
            || name.endsWith("ComObj")
            || name.endsWith("EPRINT")
        ) {
            property.setBlocks(blocks)
            return
        }
        val bSize = blocks[0]!!.data.size
        val b = ByteArray(blocks.size * bSize)
        var offset = 0
        for (i in blocks.indices) {
            System.arraycopy(blocks[i]!!.data, 0, b, offset, bSize)
            offset += bSize
        }
        property.documentRawData = b
    }


    /**
     * 
     */
    fun getPropertyRawData(propertyName: String?): ByteArray? {
        val p = getProperty(propertyName)
        if (p != null) {
            return p.documentRawData
        }
        return null
    }

    /**
     * 
     */
    fun getProperty(propertyName: String?): Property? {
        return root!!.getChlidProperty(propertyName)
    }

    /**
     * 
     */
    fun dispose() {
        /*if (properties != null)
        {
            Set<String> set = properties.keySet();
            for (String key : set)
            {
                properties.get(key).dispose();
            }
            properties.clear();
            properties = null;
        }*/
        if (headerBlock != null) {
            headerBlock!!.dispose()
            headerBlock = null
        }
        if (root != null) {
            root!!.dispose()
        }
    }

    //
    //protected Map<String, Property> properties = new HashMap<String, Property>();
    private var root: Property? = null

    //
    private var headerBlock: HeaderBlock? = null
    /**
     * 
     * @param stream
     * @param isGetThumbnail
     * @throws IOException
     */
    /**
     * Create a POIFSFileSystem from an <tt>InputStream</tt>.  Normally the stream is read until
     * EOF.  The stream is always closed.
     *
     *
     * 
     * Some streams are usable after reaching EOF (typically those that return `true`
     * for <tt>markSupported()</tt>).  In the unlikely case that the caller has such a stream
     * *and* needs to use it after this constructor completes, a work around is to wrap the
     * stream in order to trap the <tt>close()</tt> call.  A convenience method (
     * <tt>createNonClosingInputStream()</tt>) has been provided for this purpose:
     * <pre>
     * InputStream wrappedStream = POIFSFileSystem.createNonClosingInputStream(is);
     * HSSFWorkbook wb = new HSSFWorkbook(wrappedStream);
     * is.reset();
     * doSomethingElse(is);
    </pre> * 
     * Note also the special case of <tt>ByteArrayInputStream</tt> for which the <tt>close()</tt>
     * method does nothing.
     * <pre>
     * ByteArrayInputStream bais = ...
     * HSSFWorkbook wb = new HSSFWorkbook(bais); // calls bais.close() !
     * bais.reset(); // no problem
     * doSomethingElse(bais);
    </pre> * 
     * 
     * @param stream the InputStream from which to read the data
     * 
     * @exception IOException on errors reading, or on invalid data
     */
    init {
        val rawDataBlockList: BlockList
        try {
            // read the header block from the stream
            headerBlock = HeaderBlock(stream)
            bigBlockSize = headerBlock!!.bigBlockSize!!
            // read the rest of the stream into blocks
            rawDataBlockList = BlockList(stream, bigBlockSize)
        } finally {
            stream.close()
        }

        // set up the block allocation table (necessary for the
        // data_blocks to be manageable
        BlockAllocationTableReader(
            headerBlock!!.bigBlockSize!!,
            headerBlock!!.bATCount, headerBlock!!.bATArray,
            headerBlock!!.xBATCount, headerBlock!!.xBATIndex, rawDataBlockList
        )

        val properties: MutableList<Property?> = ArrayList<Property?>()
        // get property table from the document
        readProperties(
            rawDataBlockList.fetchBlocks(headerBlock!!.propertyStart, -1),
            rawDataBlockList,
            properties
        )
        // create Property tree;
        createPropertyTree(root!!, properties)
        // 
        val smallBlockList = readSmallRawDataBlock(rawDataBlockList)
        //
        readPrepertiesRawData(smallBlockList, rawDataBlockList, root!!)
    }
}



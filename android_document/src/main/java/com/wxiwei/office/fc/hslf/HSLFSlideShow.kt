/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hslf

import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.fc.fs.filesystem.CFBFileSystem
import com.wxiwei.office.fc.fs.filesystem.Property
import com.wxiwei.office.fc.hslf.blip.Metafile
import com.wxiwei.office.fc.hslf.exceptions.CorruptPowerPointFileException
import com.wxiwei.office.fc.hslf.exceptions.EncryptedPowerPointFileException
import com.wxiwei.office.fc.hslf.model.Picture
import com.wxiwei.office.fc.hslf.record.CurrentUserAtom
import com.wxiwei.office.fc.hslf.record.ExOleObjStg
import com.wxiwei.office.fc.hslf.record.PersistPtrHolder
import com.wxiwei.office.fc.hslf.record.PersistRecord
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.UserEditAtom
import com.wxiwei.office.fc.hslf.usermodel.ObjectData
import com.wxiwei.office.fc.hslf.usermodel.PictureData
import com.wxiwei.office.fc.hslf.usermodel.PictureData.Companion.create
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndianConsts
import com.wxiwei.office.system.IControl
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays

/**
 * This class contains the main functionality for the Powerpoint file
 * "reader". It is only a very basic class for now
 * 
 * @author Nick Burch
 */
class HSLFSlideShow /* extends POIDocument*/
    (control: IControl?, fileName: String?) {
    /**
     * Fetch the Current User Atom of the document
     */
    // For logging
    //private POILogger logger = POILogFactory.getLogger(this.getClass());
    // Holds metadata on where things are in our document
    var currentUserAtom: CurrentUserAtom? = null
        private set

    /**
     * Returns an array of the bytes of the file. Only correct after a
     * call to open or write - at all other times might be wrong!
     */
    // Low level contents of the file
    var underlyingBytes: ByteArray? = null
        private set
    private var _docProp: Property? = null

    /**
     * Returns an array of all the records found in the slideshow
     */
    // Low level contents
    var records: Array<Record?>? = null
        private set

    // Raw Pictures contained in the pictures stream
    private var _pictures: MutableList<PictureData>? = null

    // Embedded objects stored in storage records in the document stream, lazily populated.
    private var _objects: Array<ObjectData>? = null

    //
    private var cfbFS: CFBFileSystem?

    //
    private var control: IControl?

    /**
     * Returns the directory in the underlying POIFSFileSystem for the
     * document that is open.
     */
    /*protected DirectoryNode getPOIFSDirectory()
    {
        return directory;
    }*/
    /**
     * Constructs a Powerpoint document from fileName. Parses the document
     * and places all the important stuff into data structures.
     * 
     * @param fileName The name of the file to read.
     * @throws IOException if there is a problem while parsing the document.
     */
    init {
        this.control = control
        cfbFS = CFBFileSystem(FileInputStream(fileName))

        // First up, grab the "Current User" stream
        // We need this before we can detect Encrypted Documents
        readCurrentUserStream()

        // Next up, grab the data that makes up the
        //  PowerPoint stream
        readPowerPointStream()

        // Check to see if we have an encrypted document,
        //  bailing out if we do
        val encrypted = cfbFS!!.getPropertyRawData("EncryptedSummary") != null
        if (encrypted) {
            throw EncryptedPowerPointFileException(
                "Cannot process encrypted office files!"
            )
        }

        // Now, build records based on the PowerPoint stream
        buildRecords()

        // Look for any other streams
        readOtherStreams()
    }

    /**
     * Constructs a Powerpoint document from a specific point in a
     * POIFS Filesystem. Parses the document and places all the
     * important stuff into data structures.
     * 
     * @param dir the POIFS directory to read from
     * @throws IOException if there is a problem while parsing the document.
     */
    /*public HSLFSlideShow(DirectoryNode dir) throws IOException
    {
        super(dir);

        // First up, grab the "Current User" stream
        // We need this before we can detect Encrypted Documents
        readCurrentUserStream();

        // Next up, grab the data that makes up the
        //  PowerPoint stream
        readPowerPointStream();

        // Check to see if we have an encrypted document,
        //  bailing out if we do
        boolean encrypted = EncryptedSlideShow.checkIfEncrypted(this);
        if (encrypted)
        {
            throw new EncryptedPowerPointFileException(
                "Encrypted PowerPoint files are not supported");
        }

        // Now, build records based on the PowerPoint stream
        buildRecords();

        // Look for any other streams
        readOtherStreams();
    }*/
    /**
     * Extracts the main PowerPoint document stream from the
     * POI file, ready to be passed
     * 
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun readPowerPointStream() {
        // Get the main document stream
        //DocumentEntry docProps = (DocumentEntry)directory.getEntry("PowerPoint Document");

        // Grab the document stream

        this.underlyingBytes =
            cfbFS!!.getPropertyRawData("PowerPoint Document") //new byte[docProps.getSize()];

        //directory.createDocumentInputStream("PowerPoint Document").read(_docstream);
        _docProp = cfbFS!!.getProperty("PowerPoint Document")
    }

    /**
     * Builds the list of records, based on the contents
     * of the PowerPoint stream
     */
    private fun buildRecords() {
        // The format of records in a powerpoint file are:
        //   <little endian 2 byte "info">
        //   <little endian 2 byte "type">
        //   <little endian 4 byte "length">
        // If it has a zero length, following it will be another record
        //		<xx xx yy yy 00 00 00 00> <xx xx yy yy zz zz zz zz>
        // If it has a length, depending on its type it may have children or data
        // If it has children, these will follow straight away
        //		<xx xx yy yy zz zz zz zz <xx xx yy yy zz zz zz zz>>
        // If it has data, this will come straigh after, and run for the length
        //      <xx xx yy yy zz zz zz zz dd dd dd dd dd dd dd>
        // All lengths given exclude the 8 byte record header
        // (Data records are known as Atoms)

        // Document should start with:
        //   0F 00 E8 03 ## ## ## ##
        //     (type 1000 = document, info 00 0f is normal, rest is document length)
        //   01 00 E9 03 28 00 00 00
        //     (type 1001 = document atom, info 00 01 normal, 28 bytes long)
        //   80 16 00 00 E0 10 00 00 xx xx xx xx xx xx xx xx
        //   05 00 00 00 0A 00 00 00 xx xx xx
        //     (the contents of the document atom, not sure what it means yet)
        //   (records then follow)

        // When parsing a document, look to see if you know about that type
        //  of the current record. If you know it's a type that has children,
        //  process the record's data area looking for more records
        // If you know about the type and it doesn't have children, either do
        //  something with the data (eg TextRun) or skip over it
        // If you don't know about the type, play safe and skip over it (using
        //  its length to know where the next record will start)
        //

        this.records = read( /*_docstream,*/currentUserAtom!!.currentEditOffset.toInt())
    }

    private fun read( /*byte[] docstream, */
                      usrOffset: Int
    ): Array<Record?> {
        var usrOffset = usrOffset
        val lst = ArrayList<Int?>()
        val offset2id = HashMap<Int?, Int?>()
        while (usrOffset != 0) {
//            UserEditAtom usr = (UserEditAtom)Record.buildRecordAtOffset(docstream, usrOffset);
            var data = _docProp!!.getRecordData(usrOffset)
            val usr = Record.buildRecordAtOffset(data!!, 0, usrOffset) as UserEditAtom?

            lst.add(usrOffset)
            val psrOffset = usr!!.persistPointersOffset

            //            PersistPtrHolder ptr = (PersistPtrHolder)Record.buildRecordAtOffset(docstream,
//                psrOffset);
            data = _docProp!!.getRecordData(psrOffset)
            val ptr = Record.buildRecordAtOffset(data!!, 0, psrOffset) as PersistPtrHolder?

            lst.add(psrOffset)
            val entries = ptr!!.slideLocationsLookup
            for (id in entries.keys()) {
                val offset = entries.get(id)
                lst.add(offset)
                offset2id.put(offset, id)
            }

            usrOffset = usr.lastUserEditAtomOffset
        }
        //sort found records by offset.
        //(it is not necessary but SlideShow.findMostRecentCoreRecords() expects them sorted)
        val a: Array<Int> = lst.map { it!! }.toTypedArray()
        Arrays.sort(a)
        val rec: Array<Record?> = arrayOfNulls<Record>(lst.size)
        for (i in a.indices) {
            val offset = a[i]
            //            rec[i] = Record.buildRecordAtOffset(docstream, offset.intValue());
            val data = _docProp!!.getRecordData(offset)
            rec[i] = Record.buildRecordAtOffset(data!!, 0, offset)

            if (rec[i] is PersistRecord) {
                val psr = rec[i] as PersistRecord
                val id = offset2id.get(offset)
                psr.persistId = id!!
            }
        }

        return rec
    }

    /**
     * Find the "Current User" stream, and load it
     */
    private fun readCurrentUserStream() {
        try {
            this.currentUserAtom = CurrentUserAtom(cfbFS!!)
        } catch (ie: IOException) {
            //logger.log(POILogger.ERROR, "Error finding Current User Atom:\n" + ie);
            this.currentUserAtom = CurrentUserAtom()
        }
    }

    /**
     * Find any other streams from the filesystem, and load them
     */
    private fun readOtherStreams() {
        // Currently, there aren't any
    }

    /**
     * Find and read in pictures contained in this presentation.
     * This is lazily called as and when we want to touch pictures.
     */
    @Throws(IOException::class)
    private fun readPictures() {
        if (control == null) {
            return
        }

        //byte[] pictstream = cfbFS.getPropertyRawData("Pictures");

        /*try
        {
            pictstream = cfbFS.getPropertyRawData("Pictures");
            DocumentEntry entry = (DocumentEntry)directory.getEntry("Pictures");
            pictstream = new byte[entry.getSize()];
            DocumentInputStream is = directory.createDocumentInputStream("Pictures");
            is.read(pictstream);
        }
        catch(FileNotFoundException e)
        {
            // Silently catch exceptions if the presentation doesn't
            //  contain pictures - will use a null set instead
            return;
        }*/
        val property = cfbFS!!.getProperty("Pictures")
        if (property == null) {
            return
        }

        _pictures = ArrayList<PictureData>()
        val rawDataSize = property.propertyRawDataSize

        var pos = 0
        // An empty picture record (length 0) will take up 8 bytes
        while (pos <= (rawDataSize - 8)) {
            val offset = pos

            // Image signature
            val signature = property.getUShort(pos) //LittleEndian.getUShort(pictstream, pos);
            pos += LittleEndianConsts.SHORT_SIZE
            // Image type + 0xF018
            val type = property.getUShort(pos) //LittleEndian.getUShort(pictstream, pos);
            pos += LittleEndianConsts.SHORT_SIZE
            // Image size (excluding the 8 byte header)
            val imgsize = property.getInt(pos) //LittleEndian.getInt(pictstream, pos);
            pos += LittleEndianConsts.INT_SIZE

            // The image size must be 0 or greater
            // (0 is allowed, but odd, since we do wind on by the header each
            //  time, so we won't get stuck)
            if (imgsize < 0) {
                break
                //                throw new CorruptPowerPointFileException(
//                    "The file contains a picture, at position "
//                        + _pictures.size()
//                        + ", which has a negatively sized data length, so we can't trust any of the picture data");
            }

            // If they type (including the bonus 0xF018) is 0, skip it
            if (type == 0) {
                /*logger.log(POILogger.ERROR,
                    "Problem reading picture: Invalid image type 0, on picture with length "
                        + imgsize
                        + ".\nYou document will probably become corrupted if you save it!");
                logger.log(POILogger.ERROR, "" + pos);*/
            } else {
                // Build the PictureData object from the data
                try {
                    val pict = create(type - 0xF018)
                    pict.offset = offset


                    // Copy the data, ready to pass to PictureData
                    //byte[] imgdata = new byte[imgsize];
                    //System.arraycopy(pictstream, pos, imgdata, 0, imgdata.length);
                    //pict.setRawData(imgdata);
                    if (pict.getType() == Picture.JPEG || pict.getType() == Picture.PNG || pict.getType() == Picture.DIB.toInt() || pict.getType() == Picture.WMF || pict.getType() == Picture.EMF) {
                        val name = System.currentTimeMillis().toString() + ".tmp"
                        val file = File(
                            control!!.getSysKit().getPictureManage()
                                .getPicTempPath() + File.separator + name
                        )
                        try {
                            file.createNewFile()
                            val out = FileOutputStream(file)
                            if (pict.getType() == Picture.WMF || pict.getType() == Picture.EMF) {
                                val rawdata = property.getRecordData(pict.offset)

                                pict.setRawData(rawdata)
                                //                                out.write(pict.getData());
                                (pict as Metafile).writeByte_WMFAndEMF(out)
                            } else {
                                if (pict.getType() == Picture.PNG) {
                                    var a = property.getLong(pos + 17)
                                    if (a == 0x0A1A0A0D474E5089L) {
                                        property.writeByte(out, pos + 17, imgsize - 17)
                                    } else {
                                        // create PNG base on Mac/OS
                                        a = property.getLong(pos + 33)
                                        if (a == 0x0A1A0A0D474E5089L) {
                                            property.writeByte(out, pos + 33, imgsize - 33)
                                        }
                                    }
                                } else {
                                    property.writeByte(out, pos + 17, imgsize - 17)
                                }
                            }

                            out.close()
                        } catch (e: Exception) {
                            control!!.getSysKit().getErrorKit().writerLog(e)
                        }
                        pict.tempFilePath = file.getAbsolutePath()
                    }
                    _pictures!!.add(pict)
                } catch (e: IllegalArgumentException) {
                    /*logger.log(POILogger.ERROR, "Problem reading picture: " + e
                        + "\nYou document will probably become corrupted if you save it!");*/
                    control!!.getSysKit().getErrorKit().writerLog(e)
                }
            }

            pos += imgsize
        }
    }

    /**
     * Writes out the slideshow file the is represented by an instance
     * of this class.
     * It will write out the common OLE2 streams. If you require all
     * streams to be written out, pass in preserveNodes
     * @param out The OutputStream to write to.
     * @throws IOException If there is an unexpected IOException from
     * the passed in OutputStream
     */
    /*public void write(OutputStream out) throws IOException
    {
        // Write out, but only the common streams
        write(out, false);
    }*/
    /**
     * Writes out the slideshow file the is represented by an instance
     * of this class.
     * If you require all streams to be written out (eg Marcos, embeded
     * documents), then set preserveNodes to true
     * @param out The OutputStream to write to.
     * @param preserveNodes Should all OLE2 streams be written back out, or only the common ones?
     * @throws IOException If there is an unexpected IOException from
     * the passed in OutputStream
     */
    /*public void write(OutputStream out, boolean preserveNodes) throws IOException
    {
        // Get a new Filesystem to write into
        POIFSFileSystem outFS = new POIFSFileSystem();

        // The list of entries we've written out
        List<String> writtenEntries = new ArrayList<String>(1);

        // Write out the Property Streams
        writeProperties(outFS, writtenEntries);

        // For position dependent records, hold where they were and now are
        // As we go along, update, and hand over, to any Position Dependent
        //  records we happen across
        Hashtable<Integer, Integer> oldToNewPositions = new Hashtable<Integer, Integer>();

        // First pass - figure out where all the position dependent
        //   records are going to end up, in the new scheme
        // (Annoyingly, some powerpoing files have PersistPtrHolders
        //  that reference slides after the PersistPtrHolder)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < _records.length; i++)
        {
            if (_records[i] instanceof PositionDependentRecord)
            {
                PositionDependentRecord pdr = (PositionDependentRecord)_records[i];
                int oldPos = pdr.getLastOnDiskOffset();
                int newPos = baos.size();
                pdr.setLastOnDiskOffset(newPos);
                oldToNewPositions.put(Integer.valueOf(oldPos), Integer.valueOf(newPos));
                //System.out.println(oldPos + " -> " + newPos);
            }

            // Dummy write out, so the position winds on properly
            _records[i].writeOut(baos);
        }

        // No go back through, actually writing ourselves out
        baos.reset();
        for (int i = 0; i < _records.length; i++)
        {
            // For now, we're only handling PositionDependentRecord's that
            //  happen at the top level.
            // In future, we'll need the handle them everywhere, but that's
            //  a bit trickier
            if (_records[i] instanceof PositionDependentRecord)
            {
                // We've already figured out their new location, and
                //  told them that
                // Tell them of the positions of the other records though
                PositionDependentRecord pdr = (PositionDependentRecord)_records[i];
                pdr.updateOtherRecordReferences(oldToNewPositions);
            }

            // Whatever happens, write out that record tree
            _records[i].writeOut(baos);
        }
        // Update our cached copy of the bytes that make up the PPT stream
        _docstream = baos.toByteArray();

        // Write the PPT stream into the POIFS layer
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        outFS.createDocument(bais, "PowerPoint Document");
        writtenEntries.add("PowerPoint Document");

        // Update and write out the Current User atom
        int oldLastUserEditAtomPos = (int)currentUser.getCurrentEditOffset();
        Integer newLastUserEditAtomPos = (Integer)oldToNewPositions.get(Integer
            .valueOf(oldLastUserEditAtomPos));
        if (newLastUserEditAtomPos == null)
        {
            throw new HSLFException(
                "Couldn't find the new location of the UserEditAtom that used to be at "
                    + oldLastUserEditAtomPos);
        }
        currentUser.setCurrentEditOffset(newLastUserEditAtomPos.intValue());
        currentUser.writeToFS(outFS);
        writtenEntries.add("Current User");

        // Write any pictures, into another stream
        if (_pictures == null)
        {
            readPictures();
        }
        if (_pictures.size() > 0)
        {
            ByteArrayOutputStream pict = new ByteArrayOutputStream();
            for (PictureData p : _pictures)
            {
                p.write(pict);
            }
            outFS.createDocument(new ByteArrayInputStream(pict.toByteArray()), "Pictures");
            writtenEntries.add("Pictures");
        }

        // If requested, write out any other streams we spot
        if (preserveNodes)
        {
            copyNodes(directory.getFileSystem(), outFS, writtenEntries);
        }

        // Send the POIFSFileSystem object out to the underlying stream
        outFS.writeFilesystem(out);
    }*/
    /* ******************* adding methods follow ********************* */
    /**
     * Adds a new root level record, at the end, but before the last
     * PersistPtrIncrementalBlock.
     */
    @Synchronized
    fun appendRootLevelRecord(newRecord: Record?): Int {
        var addedAt = -1
        val r: Array<Record?> = arrayOfNulls<Record>(
            records!!.size + 1
        )
        var added = false
        for (i in (records!!.size - 1) downTo 0) {
            if (added) {
                // Just copy over
                r[i] = this.records!![i]
            } else {
                r[(i + 1)] = this.records!![i]
                if (this.records!![i] is PersistPtrHolder) {
                    r[i] = newRecord
                    added = true
                    addedAt = i
                }
            }
        }
        this.records = r
        return addedAt
    }

    /**
     * Add a new picture to this presentation.
     * 
     * @return offset of this picture in the Pictures stream
     */
    fun addPicture(img: PictureData): Int {
        // Process any existing pictures if we haven't yet
        if (_pictures == null) {
            try {
                readPictures()
            } catch (e: IOException) {
                throw CorruptPowerPointFileException(e.message!!)
            }
        }

        // Add the new picture in
        var offset = 0
        if (_pictures!!.size > 0) {
            val prev = _pictures!!.get(_pictures!!.size - 1)
            offset = prev.offset + prev.getRawData()!!.size + 8
        }
        img.offset = offset
        _pictures!!.add(img)
        return offset
    }

    /* ******************* fetching methods follow ********************* */

    val pictures: Array<PictureData?>?
        /**
         * Return array of pictures contained in this presentation
         * 
         * @return array with the read pictures or `null` if the
         * presentation doesn't contain pictures.
         */
        get() {
            if (_pictures == null) {
                try {
                    readPictures()
                } catch (e: IOException) {
                    throw CorruptPowerPointFileException(e.message!!)
                } catch (e: OutOfMemoryError) {
                    control!!.getSysKit().getErrorKit().writerLog(e, true)
                    control!!.actionEvent(EventConstant.SYS_READER_FINSH_ID, true)
                    control = null
                }
            }

            if (_pictures != null) {
                return _pictures!!.toTypedArray<PictureData?>()
            }
            return null
        }

    val embeddedObjects: Array<ObjectData>
        /**
         * Gets embedded object data from the slide show.
         * 
         * @return the embedded objects.
         */
        get() {
            if (_objects == null) {
                val objects: MutableList<ObjectData> =
                    ArrayList<ObjectData>()
                for (i in records!!.indices) {
                    if (this.records!![i] is ExOleObjStg) {
                        objects.add(ObjectData(this.records!![i] as ExOleObjStg?))
                    }
                }
                _objects = objects.toTypedArray()
            }
            return _objects!!
        }


    /**
     * 
     */
    fun dispose() {
        if (this.currentUserAtom != null) {
            currentUserAtom!!.dispose()
            this.currentUserAtom = null
        }
        if (this.records != null) {
            for (rec in this.records!!) {
                rec?.dispose()
            }
            this.records = null
        }
        if (_pictures != null) {
            for (pd in _pictures!!) {
                pd.dispose()
            }
            _pictures!!.clear()
            _pictures = null
        }
        if (_objects != null) {
            for (od in _objects!!) {
                od.dispose()
            }
            _objects = null
        }
        if (cfbFS != null) {
            cfbFS!!.dispose()
            cfbFS = null
        }
        control = null
        this.underlyingBytes = null
    }

    companion object {
        protected const val CHECKSUM_SIZE: Int = 16
    }
}

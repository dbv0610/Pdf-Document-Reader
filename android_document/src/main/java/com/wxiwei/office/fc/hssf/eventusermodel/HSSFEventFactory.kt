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
package com.wxiwei.office.fc.hssf.eventusermodel

import com.wxiwei.office.fc.hssf.record.RecordFactoryInputStream
import com.wxiwei.office.fc.poifs.filesystem.DirectoryNode
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import java.io.IOException
import java.io.InputStream

/**
 * Low level event based HSSF reader.  Pass either a DocumentInputStream to
 * process events along with a request object or pass a POIFS POIFSFileSystem to
 * processWorkbookEvents along with a request.
 * 
 * This will cause your file to be processed a record at a time.  Each record with
 * a static id matching one that you have registered in your HSSFRequest will be passed
 * to your associated HSSFListener.
 * 
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Carey Sublette  (careysub@earthling.net)
 */
class HSSFEventFactory
/** Creates a new instance of HSSFEventFactory  */
{
    /**
     * Processes a file into essentially record events.
     * 
     * @param req an Instance of HSSFRequest which has your registered listeners
     * @param fs  a POIFS filesystem containing your workbook
     */
    @Throws(IOException::class)
    fun processWorkbookEvents(req: HSSFRequest, fs: POIFSFileSystem) {
        processWorkbookEvents(req, fs.getRoot())
    }

    /**
     * Processes a file into essentially record events.
     * 
     * @param req an Instance of HSSFRequest which has your registered listeners
     * @param dir  a DirectoryNode containing your workbook
     */
    @Throws(IOException::class)
    fun processWorkbookEvents(req: HSSFRequest, dir: DirectoryNode) {
        val `in`: InputStream? = dir.createDocumentInputStream("Workbook")

        processEvents(req, `in`)
    }

    /**
     * Processes a file into essentially record events.
     * 
     * @param req an Instance of HSSFRequest which has your registered listeners
     * @param fs  a POIFS filesystem containing your workbook
     * @return    numeric user-specified result code.
     */
    @Throws(IOException::class, HSSFUserException::class)
    fun abortableProcessWorkbookEvents(req: HSSFRequest, fs: POIFSFileSystem): Short {
        return abortableProcessWorkbookEvents(req, fs.getRoot())
    }

    /**
     * Processes a file into essentially record events.
     * 
     * @param req an Instance of HSSFRequest which has your registered listeners
     * @param dir  a DirectoryNode containing your workbook
     * @return    numeric user-specified result code.
     */
    @Throws(IOException::class, HSSFUserException::class)
    fun abortableProcessWorkbookEvents(req: HSSFRequest, dir: DirectoryNode): Short {
        val `in`: InputStream? = dir.createDocumentInputStream("Workbook")
        return abortableProcessEvents(req, `in`)
    }

    /**
     * Processes a DocumentInputStream into essentially Record events.
     * 
     * If an `AbortableHSSFListener` causes a halt to processing during this call
     * the method will return just as with `abortableProcessEvents`, but no
     * user code or `HSSFUserException` will be passed back.
     * 
     * @see POIFSFileSystem.createDocumentInputStream
     * @param req an Instance of HSSFRequest which has your registered listeners
     * @param in  a DocumentInputStream obtained from POIFS's POIFSFileSystem object
     */
    fun processEvents(req: HSSFRequest, `in`: InputStream?) {
        try {
            genericProcessEvents(req, `in`)
        } catch (hue: HSSFUserException) {
            /*If an HSSFUserException user exception is thrown, ignore it.*/
        }
    }


    /**
     * Processes a DocumentInputStream into essentially Record events.
     * 
     * @see POIFSFileSystem.createDocumentInputStream
     * @param req an Instance of HSSFRequest which has your registered listeners
     * @param in  a DocumentInputStream obtained from POIFS's POIFSFileSystem object
     * @return    numeric user-specified result code.
     */
    @Throws(HSSFUserException::class)
    fun abortableProcessEvents(req: HSSFRequest, `in`: InputStream?): Short {
        return genericProcessEvents(req, `in`)
    }

    /**
     * Processes a DocumentInputStream into essentially Record events.
     * 
     * @see POIFSFileSystem.createDocumentInputStream
     * @param req an Instance of HSSFRequest which has your registered listeners
     * @param in  a DocumentInputStream obtained from POIFS's POIFSFileSystem object
     * @return    numeric user-specified result code.
     */
    @Throws(HSSFUserException::class)
    private fun genericProcessEvents(req: HSSFRequest, `in`: InputStream?): Short {
        var userCode: Short = 0

        // Create a new RecordStream and use that
        val recordStream = RecordFactoryInputStream(`in`, false)

        // Process each record as they come in
        while (true) {
            val r = recordStream.nextRecord()
            if (r == null) {
                break
            }
            userCode = req.processRecord(r)
            if (userCode.toInt() != 0) {
                break
            }
        }

        // All done, return our last code
        return userCode
    }
}

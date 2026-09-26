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

import com.wxiwei.office.fc.hslf.exceptions.CorruptPowerPointFileException
import com.wxiwei.office.fc.hslf.record.DocumentEncryptionAtom
import com.wxiwei.office.fc.hslf.record.PersistPtrHolder
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.Record.Companion.buildRecordAtOffset
import com.wxiwei.office.fc.hslf.record.UserEditAtom

/**
 * This class provides helper functions for determining if a
 * PowerPoint document is Encrypted.
 * In future, it may also provide Encryption and Decryption
 * functions, but first we'd need to figure out how
 * PowerPoint encryption is really done!
 * 
 * @author Nick Burch
 */
object EncryptedSlideShow {
    /**
     * Check to see if a HSLFSlideShow represents an encrypted
     * PowerPoint document, or not
     * @param hss The HSLFSlideShow to check
     * @return true if encrypted, otherwise false
     */
    fun checkIfEncrypted(hss: HSLFSlideShow?): Boolean {
        // Easy way to check - contains a stream
        //  "EncryptedSummary"
        /*try
        {
            hss.getPOIFSDirectory().getEntry("EncryptedSummary");
            return true;
        }
        catch(FileNotFoundException fnfe)
        {
            // Doesn't have encrypted properties
        }

        // If they encrypted the document but not the properties,
        //  it's harder.
        // We need to see what the last record pointed to by the
        //  first PersistPrtHolder is - if it's a
        //  DocumentEncryptionAtom, then the file's Encrypted
        DocumentEncryptionAtom dea = fetchDocumentEncryptionAtom(hss);
        if (dea != null)
        {
            return true;
        }*/
        return false
    }

    /**
     * Return the DocumentEncryptionAtom for a HSLFSlideShow, or
     * null if there isn't one.
     * @return a DocumentEncryptionAtom, or null if there isn't one
     */
    fun fetchDocumentEncryptionAtom(hss: HSLFSlideShow): DocumentEncryptionAtom? {
        // Will be the last Record pointed to by the
        //  first PersistPrtHolder, if there is one

        val cua = hss.currentUserAtom!!
        if (cua.currentEditOffset != 0L) {
            // Check it's not past the end of the file
            if (cua.currentEditOffset > hss.underlyingBytes!!.size) {
                throw CorruptPowerPointFileException(
                    "The CurrentUserAtom claims that the offset of last edit details are past the end of the file"
                )
            }

            // Grab the details of the UserEditAtom there
            // If the record's messed up, we could AIOOB
            var r: Record? = null
            try {
                r = buildRecordAtOffset(
                    hss.underlyingBytes!!,
                    cua.currentEditOffset.toInt()
                )
            } catch (e: ArrayIndexOutOfBoundsException) {
                return null
            }
            if (r == null) {
                return null
            }
            if (r !is UserEditAtom) {
                return null
            }
            val uea = r

            // Now get the PersistPtrHolder
            val r2 = buildRecordAtOffset(
                hss.underlyingBytes!!,
                uea.persistPointersOffset
            )
            if (r2 !is PersistPtrHolder) {
                return null
            }
            val pph = r2

            // Now get the last record
            val slideIds = pph.knownSlideIDs
            var maxSlideId = -1
            for (i in slideIds.indices) {
                if (slideIds[i] > maxSlideId) {
                    maxSlideId = slideIds[i]
                }
            }
            if (maxSlideId == -1) {
                return null
            }

            val offset = (pph.slideLocationsLookup.get(maxSlideId) as Int)
            val r3 = buildRecordAtOffset(hss.underlyingBytes!!, offset)

            // If we have a DocumentEncryptionAtom, it'll be this one
            if (r3 is DocumentEncryptionAtom) {
                return r3
            }
        }

        return null
    }
}

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
package com.wxiwei.office.fc.util

import com.wxiwei.office.fc.poifs.filesystem.DirectoryEntry
import com.wxiwei.office.fc.poifs.filesystem.DocumentEntry
import com.wxiwei.office.fc.poifs.filesystem.DocumentInputStream
import com.wxiwei.office.fc.poifs.filesystem.Entry
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import java.io.IOException

@Internal
open class POIUtils {
    companion object {
        /**
         * Copies an Entry into a target POIFS directory, recursively
         */
        @Internal
        @JvmStatic
        @Throws(IOException::class)
        fun copyNodeRecursively(entry: Entry, target: DirectoryEntry) {
            // System.err.println("copyNodeRecursively called with "+entry.getName()+
            // ","+target.getName());
            var newTarget: DirectoryEntry? = null
            if (entry.isDirectoryEntry()) {
                newTarget = target.createDirectory(entry.getName())
                val entries = (entry as DirectoryEntry).getEntries()

                while (entries.hasNext()) {
                    copyNodeRecursively(entries.next(), newTarget)
                }
            } else {
                val dentry = entry as DocumentEntry
                val dstream = DocumentInputStream(dentry)
                target.createDocument(dentry.getName(), dstream)
                dstream.close()
            }
        }

        /**
         * Copies nodes from one POIFS to the other minus the excepts
         *
         * @param source
         *            is the source POIFS to copy from
         * @param target
         *            is the target POIFS to copy to
         * @param excepts
         *            is a list of Strings specifying what nodes NOT to copy
         */
        @JvmStatic
        @Throws(IOException::class)
        fun copyNodes(sourceRoot: DirectoryEntry, targetRoot: DirectoryEntry, excepts: List<String>) {
            val entries = sourceRoot.getEntries()
            while (entries.hasNext()) {
                val entry = entries.next()
                if (!excepts.contains(entry.getName())) {
                    copyNodeRecursively(entry, targetRoot)
                }
            }
        }

        /**
         * Copies nodes from one POIFS to the other minus the excepts
         *
         * @param source
         *            is the source POIFS to copy from
         * @param target
         *            is the target POIFS to copy to
         * @param excepts
         *            is a list of Strings specifying what nodes NOT to copy
         */
        @JvmStatic
        @Throws(IOException::class)
        fun copyNodes(source: POIFSFileSystem, target: POIFSFileSystem, excepts: List<String>) {
            // System.err.println("CopyNodes called");
            copyNodes(source.getRoot(), target.getRoot(), excepts)
        }
    }
}

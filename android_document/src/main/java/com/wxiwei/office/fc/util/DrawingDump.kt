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

import com.wxiwei.office.fc.hssf.usermodel.HSSFWorkbook
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import java.io.FileInputStream
import java.io.IOException

/**
 * Dump out the aggregated escher records
 */
open class DrawingDump {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun main(args: Array<String>) {
            val fs = POIFSFileSystem(FileInputStream(args[0]))
            val wb = HSSFWorkbook(fs)
            println("Drawing group:")
            wb.dumpDrawingGroupRecords(true)

            for (sheetNum in 1..wb.getNumberOfSheets()) {
                println("Sheet " + sheetNum + ":")
                val sheet = wb.getSheetAt(sheetNum - 1)
                sheet.dumpDrawingRecords(true)
            }
        }
    }
}

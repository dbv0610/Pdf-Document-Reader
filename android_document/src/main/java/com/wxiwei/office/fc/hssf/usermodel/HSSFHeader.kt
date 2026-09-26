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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.record.HeaderRecord
import com.wxiwei.office.fc.hssf.record.aggregates.PageSettingsBlock
import com.wxiwei.office.fc.ss.usermodel.Header


/**
 * Class to read and manipulate the header.
 */
class HSSFHeader(private val _psb: PageSettingsBlock) : HeaderFooter(), Header {
    override val rawText: String
        get() {
            val hf = _psb.header
            if (hf == null) {
                return ""
            }
            return hf.getText()
        }

    override fun setHeaderFooterText(text: String?) {
        var hfr = _psb.header
        if (hfr == null) {
            hfr = HeaderRecord(text)
            _psb.header = hfr
        } else {
            hfr.setText(text ?: "")
        }
    }
}

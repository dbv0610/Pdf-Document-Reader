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
package com.wxiwei.office.fc.hssf.record


/**
 * Title:        Footer Record (0x0015) 
 *
 *
 * Description:  Specifies the footer for a sheet<P>
 * REFERENCE:  PG 317 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P>
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Shawn Laubach (slaubach at apache dot org) Modified 3/14/02
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class FooterRecord : HeaderFooterBase {
    constructor(text: String?) : super(text)

    constructor(`in`: RecordInputStream) : super(`in`)

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FOOTER]\n")
        buffer.append("    .footer = ").append(getText()).append("\n")
        buffer.append("[/FOOTER]\n")
        return buffer.toString()
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        return FooterRecord(getText())
    }

    companion object {
        const val sid: Short = 0x0015
    }
}

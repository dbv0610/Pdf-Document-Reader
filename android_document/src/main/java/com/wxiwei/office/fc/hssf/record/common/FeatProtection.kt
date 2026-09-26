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
package com.wxiwei.office.fc.hssf.record.common

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.readUnicodeString

/**
 * Title: FeatProtection (Protection Shared Feature) common record part
 * <P>
 * This record part specifies Protection data for a sheet, stored
 * as part of a Shared Feature. It can be found in records such
 * as [FeatRecord]
</P> */
class FeatProtection : SharedFeature {
    var fSD: Int = 0
        private set

    /**
     * 0 means no password. Otherwise indicates the
     * password verifier algorithm (same kind as
     * [PasswordRecord] and
     * [PasswordRev4Record])
     */
    var passwordVerifier: Int = 0

    private var title: String? = null
    private val securityDescriptor: ByteArray

    constructor() {
        securityDescriptor = ByteArray(0)
    }

    constructor(`in`: RecordInputStream) {
        fSD = `in`.readInt()
        passwordVerifier = `in`.readInt()

        title = readUnicodeString(`in`)

        securityDescriptor = `in`.readRemainder()
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(" [FEATURE PROTECTION]\n")
        buffer.append("   Self Relative = " + fSD)
        buffer.append("   Password Verifier = " + passwordVerifier)
        buffer.append("   Title = " + title)
        buffer.append("   Security Descriptor Size = " + securityDescriptor.size)
        buffer.append(" [/FEATURE PROTECTION]\n")
        return buffer.toString()
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeInt(fSD)
        out.writeInt(passwordVerifier)
        StringUtil.writeUnicodeString(out, title!!)
        out.write(securityDescriptor)
    }

    override val dataSize: Int
        get() = 4 + 4 + StringUtil.getEncodedSize(title!!) + securityDescriptor.size

    fun getTitle(): String {
        return title!!
    }

    fun setTitle(title: String) {
        this.title = title
    }

    companion object {
        var NO_SELF_RELATIVE_SECURITY_FEATURE: Long = 0
        var HAS_SELF_RELATIVE_SECURITY_FEATURE: Long = 1
    }
}

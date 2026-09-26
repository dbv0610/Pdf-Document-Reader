// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * VERSION Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFVersionTable.java 8584 2006-08-10 23:06:37Z duns $
 */
abstract class TTFVersionTable : TTFTable() {
    var minorVersion: Int = 0

    var majorVersion: Int = 0

    @Throws(IOException::class)
    fun readVersion() {
        majorVersion = ttf.readUShort()
        minorVersion = ttf.readUShort()
    }

    override fun toString(): String {
        return super.toString() + " v" + majorVersion + "." + minorVersion
    }
}

// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetMapperFlags TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetMapperFlags.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetMapperFlags() : EMFTag(16, 1) {
    private var flags = 0

    constructor(flags: Int) : this() {
        this.flags = flags
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetMapperFlags(emf.readDWORD())
    }


    override fun toString(): String {
        return super.toString() + "\n  flags: " + flags
    }
}

// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * NAME Table.
 * 
 * @author Mark Donszelmann
 * @version $Id: TTFNameTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFNameTable : TTFTable() {
    private var format = 0

    private var numberOfNameRecords = 0

    private var stringStorage = 0

    private val name =
        Array<Array<String?>?>(4) { arrayOfNulls<String>(19) }  // 18 NameIDs according to

    // OpenType
    override fun getTag(): String {
        return "name"
    }

    // FIXME: fixed decoding for lucida files
    // PID = 0, -> UnicodeBig (Apple-Unicode-English)
    // PID = 1, EID = 0, LID = 0; -> Default Encoding (Mac-Roman-English)
    // PID = 3, EID = 1, LID = 1033; -> UnicodeBig (Win-UGL-ENU)
    // LID english, other languages ignored
    @Throws(IOException::class)
    override fun readTable() {
        format = ttf.readUShort()
        numberOfNameRecords = ttf.readUShort()
        stringStorage = ttf.readUShort()

        for (i in 0..<numberOfNameRecords) {
            val pid = ttf.readUShort()
            val eid = ttf.readUShort()
            val lid = ttf.readUShort()
            val nid = ttf.readUShort()
            val stringLen = ttf.readUShort()
            val stringOffset = ttf.readUShort()
            // long pos = ttf.getFilePointer();
            ttf.pushPos()
            ttf.seek((stringStorage + stringOffset).toLong())
            val b = ByteArray(stringLen)
            ttf.readFully(b)
            if (pid == 0) {
                // Apple Unicode
                name[pid]!![nid] = String(b, charset("UnicodeBig"))
            } else if ((pid == 1) && (eid == 0)) {
                if (lid == 0) {
                    // Mac-Roman-English
                    name[pid]!![nid] = String(b, charset("ISO8859-1"))
                }
                // ignore other languages
            } else if ((pid == 3) && (eid == 1)) {
                // Win-UGL
                if (lid == 0x0409) {
                    // ENU
                    name[pid]!![nid] = String(b, charset("UnicodeBig"))
                }
                // ignore other languages
            } else {
                println(
                    ("Unimplemented PID, EID, LID scheme: " + pid
                            + ", " + eid + ", " + lid)
                )
                println("NID = " + nid)
                name[pid]!![nid] = String(b, charset("Default"))
            }
            ttf.popPos()
            // ttf.seek(pos);
        }
    }

    override fun toString(): String {
        val s = StringBuffer()
        s.append(super.toString() + "\n")
        s.append("  format: " + format)
        for (i in name.indices) {
            for (j in name[i]!!.indices) {
                if (name[i]!![j] != null) {
                    s.append("\n  name[" + i + "][" + j + "]: " + name[i]!![j])
                }
            }
        }
        return s.toString()
    }
}

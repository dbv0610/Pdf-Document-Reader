// Copyright 2002-2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import java.io.IOException

/**
 * EMF Text
 * 
 * @author Mark Donszelmann
 * @version $Id: TextW.java 10367 2007-01-22 19:26:48Z duns $
 */
class TextW(pos: Point?, string: String?, options: Int, bounds: Rectangle?, widths: IntArray?) :
    Text(pos, string, options, bounds, widths) {
    override fun toString(): String {
        val widthsS = StringBuffer()
        for (i in 0..<string!!.length) {
            widthsS.append(",")
            widthsS.append(widths!![i])
        }
        widthsS.append(']')
        widthsS.setCharAt(0, '[')
        return ("  TextW\n" + "    pos: " + pos + "\n    options: " + options + "\n    bounds: "
                + bounds + "\n    string: " + string + "\n    widths: " + widthsS)
    }

    companion object {
        @Throws(IOException::class)
        fun read(emf: EMFInputStream): TextW {
            val pos = emf.readPOINTL()
            val sLen = emf.readDWORD()
            /* int sOffset = */
            emf.readDWORD()
            val options = emf.readDWORD()
            val bounds = emf.readRECTL()
            /* int cOffset = */
            emf.readDWORD()
            // FIXME: nothing done with offsets
            val string = String(emf.readBYTE(2 * sLen), charset("UTF-16LE"))
            if ((2 * sLen) % 4 != 0) for (i in 0..<4 - (2 * sLen) % 4) emf.readBYTE()
            val widths = IntArray(sLen)
            for (i in 0..<sLen) widths[i] = emf.readDWORD()
            return TextW(pos, string, options, bounds, widths)
        }
    }
}

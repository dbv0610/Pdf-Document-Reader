package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import java.io.IOException
import java.nio.charset.Charset

class TextA(pos: Point?, string: String?, options: Int, bounds: Rectangle?, widths: IntArray?) :
    Text(pos, string, options, bounds, widths) {
    override fun toString(): String {
        val widthsS = StringBuffer()
        for (i in 0..<string!!.length) {
            widthsS.append(",")
            widthsS.append(widths!![i])
        }
        widthsS.append(']')
        widthsS.setCharAt(0, '[')
        return ("  TextA\n" + "    pos: " + pos + "\n    options: " + options + "\n    bounds: "
                + bounds + "\n    string: " + string + "\n    widths: " + widthsS)
    }

    companion object {
        @Throws(IOException::class)
        fun read(emf: EMFInputStream): TextA {
            val pos = emf.readPOINTL()
            val sLen = emf.readDWORD()
            /* int sOffset = */
            emf.readDWORD()
            val options = emf.readDWORD()
            val bounds = emf.readRECTL()
            /* int cOffset = */
            emf.readDWORD()
            // FIXME: nothing done with offsets
            val csn = Charset.defaultCharset().name()
            val string = String(emf.readBYTE(sLen), charset(csn))
            if ((sLen) % 4 != 0) for (i in 0..<4 - (sLen) % 4) emf.readBYTE()
            val widths = IntArray(sLen)
            for (i in 0..<sLen) widths[i] = emf.readDWORD()
            return TextA(pos, string, options, bounds, widths)
        }
    }
}

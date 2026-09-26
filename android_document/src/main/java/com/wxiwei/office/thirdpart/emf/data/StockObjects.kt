// Copyright 2007 FreeHEP
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.thirdpart.emf.EMFConstants

/**
 * StockObjects.java
 * 
 * Created: Wed Dec 20 14:20:00 2006
 * 
 * Copyright:
 * Company:
 * 
 * @author Daniel Noll daniel@nuix.com
 * @version $Id$
 */
object StockObjects {
    // Supported by all versions of Win32...
    private const val WHITE_BRUSH = 0
    private const val LTGRAY_BRUSH = 1
    private const val GRAY_BRUSH = 2
    private const val DKGRAY_BRUSH = 3
    private const val BLACK_BRUSH = 4
    private const val NULL_BRUSH = 5
    private const val WHITE_PEN = 6
    private const val BLACK_PEN = 7
    private const val NULL_PEN = 8
    private const val OEM_FIXED_FONT = 10
    private const val ANSI_FIXED_FONT = 11
    private const val ANSI_VAR_FONT = 12
    private const val SYSTEM_FONT = 13
    private const val DEVICE_DEFAULT_FONT = 14
    private const val DEFAULT_PALETTE = 15
    private const val SYSTEM_FIXED_FONT = 16

    // Windows 2000 or later...
    private const val DEFAULT_GUI_FONT = 17

    // Windows XP or later...
    // These ones are magic.  They take the colour of the current device context
    // as set by SetDCBrushColor and SetDCPenColor
    private const val DC_BRUSH = 18
    private const val DC_PEN = 19

    private val objects: Array<GDIObject?> = arrayOfNulls<GDIObject>(20)

    init {
        val nullColor = Color(0, 0, 0, 0)

        objects[WHITE_BRUSH] = LogBrush32(EMFConstants.BS_SOLID, Color.WHITE, 0)
        objects[LTGRAY_BRUSH] = LogBrush32(EMFConstants.BS_SOLID, Color.LIGHT_GRAY, 0)
        objects[GRAY_BRUSH] = LogBrush32(EMFConstants.BS_SOLID, Color.GRAY, 0)
        objects[DKGRAY_BRUSH] = LogBrush32(EMFConstants.BS_SOLID, Color.DARK_GRAY, 0)
        objects[BLACK_BRUSH] = LogBrush32(EMFConstants.BS_SOLID, Color.BLACK, 0)
        objects[NULL_BRUSH] = LogBrush32(EMFConstants.BS_NULL, nullColor, 0)

        objects[WHITE_PEN] = LogPen(EMFConstants.PS_SOLID, 1, Color.WHITE)
        objects[BLACK_PEN] = LogPen(EMFConstants.PS_SOLID, 1, Color.BLACK)
        objects[NULL_PEN] = LogPen(EMFConstants.PS_NULL, 1, nullColor)

        // XXX: Should these depend on the look and feel?
        objects[OEM_FIXED_FONT] = LogFontW(Font("Monospaced", Font.PLAIN, 12))
        objects[ANSI_FIXED_FONT] = objects[OEM_FIXED_FONT]
        objects[ANSI_VAR_FONT] = LogFontW(Font("SansSerif", Font.PLAIN, 12))
        objects[SYSTEM_FONT] = LogFontW(Font("Dialog", Font.PLAIN, 12))
        objects[DEVICE_DEFAULT_FONT] = objects[ANSI_VAR_FONT]
        objects[SYSTEM_FIXED_FONT] = objects[OEM_FIXED_FONT]
        objects[DEFAULT_GUI_FONT] = objects[SYSTEM_FONT]

        // TODO: DEFAULT_PALETTE, DC_BRUSH and DC_PEN
    }

    /**
     * Gets a stock object by value.
     * 
     * @param value the value.
     * @return the stock object.
     */
    fun getStockObject(value: Int): GDIObject {
        var value = value
        require(value < 0) { "Value does not represent a stock object: " + value }

        value = value xor -0x80000000

        require(value < objects.size) { "Stock object is out of bounds: " + value }

        val `object` = objects[value]
        if (`object` == null) {
            throw UnsupportedOperationException("Stock object not yet supported: " + value)
        }

        return `object`
    }
}

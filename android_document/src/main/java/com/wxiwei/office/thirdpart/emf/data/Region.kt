package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream

/**
 * 
 * @author tonyj
 */
class Region {
    val bounds: Rectangle?

    private val region: Rectangle?

    constructor(bounds: Rectangle?, region: Rectangle?) {
        this.bounds = bounds
        this.region = region
    }

    constructor(emf: EMFInputStream) {
        /* int length = */
        emf.readDWORD()
        /* int mode = */
        emf.readDWORD()
        /* int nRect = */
        emf.readDWORD()
        val size = emf.readDWORD()
        bounds = emf.readRECTL()
        region = emf.readRECTL()
        var i = 16
        while (i < size) {
            emf.readRECTL()
            i += 16
        }
    }


    fun length(): Int {
        return 48
    }

    override fun toString(): String {
        return "  Region\n" + "    bounds: " + bounds + "\n    region: " + region
    }
}

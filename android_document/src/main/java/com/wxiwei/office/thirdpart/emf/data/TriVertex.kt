// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.EMFInputStream

class TriVertex(private var x: Int, private var y: Int, private var color: Color) {
    constructor(emf: EMFInputStream) : this(emf.readLONG(), emf.readLONG(), emf.readCOLOR16())

    override fun toString(): String {
        return "[$x, $y] $color"
    }
}

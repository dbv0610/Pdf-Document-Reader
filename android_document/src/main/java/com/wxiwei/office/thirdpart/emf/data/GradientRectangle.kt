// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream

class GradientRectangle(private var upperLeft: Int, private var lowerRight: Int) : Gradient() {
    constructor(emf: EMFInputStream) : this(emf.readULONG(), emf.readULONG())

    override fun toString(): String {
        return "  GradientRectangle: $upperLeft, $lowerRight"
    }
}

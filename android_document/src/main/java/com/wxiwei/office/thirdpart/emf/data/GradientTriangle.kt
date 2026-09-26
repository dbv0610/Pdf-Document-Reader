// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream

class GradientTriangle(
    private var vertex1: Int,
    private var vertex2: Int,
    private var vertex3: Int
) : Gradient() {
    constructor(emf: EMFInputStream) : this(emf.readULONG(), emf.readULONG(), emf.readULONG())

    override fun toString(): String {
        return "  GradientTriangle: $vertex1, $vertex2, $vertex3"
    }
}

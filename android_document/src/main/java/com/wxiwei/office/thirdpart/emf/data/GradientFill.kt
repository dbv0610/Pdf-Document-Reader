// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * GradientFill TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: GradientFill.java 10367 2007-01-22 19:26:48Z duns $
 */
class GradientFill() : EMFTag(118, 1), EMFConstants {
    private var bounds: Rectangle? = null

    private var mode = 0

    private lateinit var vertices: Array<TriVertex?>

    private lateinit var gradients: Array<Gradient?>

    constructor(
        bounds: Rectangle?,
        mode: Int,
        vertices: Array<TriVertex?>,
        gradients: Array<Gradient?>
    ) : this() {
        this.bounds = bounds
        this.mode = mode
        this.vertices = vertices
        this.gradients = gradients
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val bounds = emf.readRECTL()
        val vertices = arrayOfNulls<TriVertex>(emf.readDWORD())
        val gradients = arrayOfNulls<Gradient>(emf.readDWORD())
        val mode = emf.readULONG()

        for (i in vertices.indices) {
            vertices[i] = TriVertex(emf)
        }
        for (i in gradients.indices) {
            if (mode == EMFConstants.GRADIENT_FILL_TRIANGLE) {
                gradients[i] = GradientTriangle(emf)
            } else {
                gradients[i] = GradientRectangle(emf)
            }
        }
        return GradientFill(bounds, mode, vertices, gradients)
    }

    override fun toString(): String {
        val s = StringBuffer()
        s.append(super.toString())
        s.append("\n")
        s.append("  bounds: ")
        s.append(bounds)
        s.append("\n")
        s.append("  mode: ")
        s.append(mode)
        s.append("\n")
        for (i in vertices.indices) {
            s.append("  vertex[")
            s.append(i)
            s.append("]: ")
            s.append(vertices[i])
            s.append("\n")
        }
        for (i in gradients.indices) {
            s.append("  gradient[")
            s.append(i)
            s.append("]: ")
            s.append(gradients[i])
            s.append("\n")
        }
        return s.toString()
    }
}

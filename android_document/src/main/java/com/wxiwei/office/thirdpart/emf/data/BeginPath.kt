// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag

class BeginPath : EMFTag(59, 1) {
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return this
    }

    override fun render(renderer: EMFRenderer) {
        renderer.setPath(GeneralPath(renderer.getWindingRule()))
        renderer.setPathTransform(AffineTransform())
    }
}

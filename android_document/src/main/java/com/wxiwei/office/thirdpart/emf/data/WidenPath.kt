// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * WidenPath TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: WidenPath.java 10367 2007-01-22 19:26:48Z duns $
 */
class WidenPath : EMFTag(66, 1) {
    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream?, len: Int): EMFTag {
        return this
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        val currentPath = renderer.getPath()
        val currentPenStroke = renderer.getPenStroke()
        // The WidenPath function redefines the current path as the area
        // that would be painted if the path were stroked using the pen
        // currently selected into the given device context.
        if (currentPath != null && currentPenStroke != null) {
            val newPath = GeneralPath(
                renderer.getWindingRule()
            )
            newPath.append(currentPenStroke.createStrokedShape(currentPath)!!, false)
            renderer.setPath(newPath)
        }
    }
}

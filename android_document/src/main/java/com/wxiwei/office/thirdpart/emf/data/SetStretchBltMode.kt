package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Image
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetStretchBltMode TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetStretchBltMode.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetStretchBltMode() : EMFTag(21, 1), EMFConstants {
    private var mode = 0

    constructor(mode: Int) : this() {
        this.mode = mode
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetStretchBltMode(emf.readDWORD())
    }


    override fun toString(): String {
        return super.toString() + "\n  mode: " + mode
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // The stretching mode defines how the system combines rows or columns of a
        // bitmap with existing pixels on a display device when an application calls
        // the StretchBlt function.
        renderer.setScaleMode(getScaleMode(mode))
    }

    private fun getScaleMode(mode: Int): Int {
        //     COLORONCOLOR 	Deletes the pixels. This mode deletes all
        // eliminated lines of pixels without trying to preserve their information.
        if (mode == EMFConstants.COLORONCOLOR /*||
                                              mode == EMFConstants.STRETCH_DELETESCANS*/) {
            return Image.SCALE_FAST
        } else if (mode == EMFConstants.HALFTONE /*||
                                               mode == EMFConstants.STRETCH_HALFTONE*/) {
            return Image.SCALE_SMOOTH
        } else if (mode == EMFConstants.BLACKONWHITE /*||
                                                   mode == EMFConstants.STRETCH_ANDSCANS*/) {
            // TODO not sure
            return Image.SCALE_REPLICATE
        } else if (mode == EMFConstants.WHITEONBLACK /*||
                                                   mode == EMFConstants.STRETCH_ORSCANS*/) {
            // TODO not sure
            return Image.SCALE_REPLICATE
        } else {
            return Image.SCALE_DEFAULT
        }
    }
}

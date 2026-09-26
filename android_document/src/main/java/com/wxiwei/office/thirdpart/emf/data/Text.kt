// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFConstants

/**
 * EMF Text
 * 
 * @author Mark Donszelmann
 * @version $Id: Text.java 10313 2007-01-15 16:30:03Z duns $
 */
abstract class Text protected constructor(
    var pos: Point?,
    var string: String?,
    var options: Int,
    var bounds: Rectangle?,
    var widths: IntArray?
) : EMFConstants

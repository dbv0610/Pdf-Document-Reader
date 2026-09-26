// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream

/**
 * EMF Panose
 * 
 * @author Mark Donszelmann
 * @version $Id: Panose.java 10367 2007-01-22 19:26:48Z duns $
 */
class Panose : EMFConstants {
    private val familyType: Int

    private val serifStyle: Int

    private val weight: Int

    private val proportion: Int

    private val contrast: Int

    private val strokeVariation: Int

    private val armStyle: Int

    private val letterForm: Int

    private val midLine: Int

    private val xHeight: Int

    constructor() {
        // FIXME, fixed
        this.familyType = EMFConstants.PAN_NO_FIT
        this.serifStyle = EMFConstants.PAN_NO_FIT
        this.proportion = EMFConstants.PAN_NO_FIT
        this.weight = EMFConstants.PAN_NO_FIT
        this.contrast = EMFConstants.PAN_NO_FIT
        this.strokeVariation = EMFConstants.PAN_NO_FIT
        this.armStyle = EMFConstants.PAN_ANY
        this.letterForm = EMFConstants.PAN_ANY
        this.midLine = EMFConstants.PAN_ANY
        this.xHeight = EMFConstants.PAN_ANY
    }

    constructor(emf: EMFInputStream) {
        familyType = emf.readBYTE()
        serifStyle = emf.readBYTE()
        proportion = emf.readBYTE()
        weight = emf.readBYTE()
        contrast = emf.readBYTE()
        strokeVariation = emf.readBYTE()
        armStyle = emf.readBYTE()
        letterForm = emf.readBYTE()
        midLine = emf.readBYTE()
        xHeight = emf.readBYTE()
    }


    override fun toString(): String {
        return ("  Panose\n" + "    familytype: " + familyType + "\n    serifStyle: " + serifStyle
                + "\n    weight: " + weight + "\n    proportion: " + proportion + "\n    contrast: "
                + contrast + "\n    strokeVariation: " + strokeVariation + "\n    armStyle: "
                + armStyle + "\n    letterForm: " + letterForm + "\n    midLine: " + midLine
                + "\n    xHeight: " + xHeight)
    }
}

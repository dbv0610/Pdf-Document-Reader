/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 鏂囦欢鍚嶇О:          SlideShowSlideInfoAtom.java
 * 鐗堟潈鎵�鏈堾2001-2014 铏硅蒋锛堟澀宸烇級绉戞妧鏈夐檺鍏徃
 * 缂栬瘧鍣�:            android2.2
 * 鏃堕棿:              涓婂崍10:43:23
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.util.Hashtable

/**
 * TODO: An atom record that specifies what transition effect to perform
 * during a slide show, and how to advance to the next presentation slide.
 * 
 * 
 * 
 * 
 * Read鐗堟湰:        Read V1.0
 * 
 * 
 * 浣滆��:            jqin
 * 
 * 
 * 鏃ユ湡:            2013-1-8
 * 
 * 
 * 璐熻矗浜�:           jqin
 * 
 * 
 * 璐熻矗灏忕粍:
 * 
 * 
 * 
 * 
 */
class SlideShowSlideInfoAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?

    /**
     * an amount of time, in milliseconds, to wait before advancing to the
     * next presentation slide. It MUST be greater than or equal to 0
     * and less than or equal to 86399000. It MUST be ignored
     * unless fAutoAdvance is TRUE
     */
    private val slideTime: Int

    /**
     * which sound to play when the transition starts.
     */
    private val soundIdRef: Int

    /**
     * the variant of effectType. See the effectType field for further restriction
     * and specification of this field
     */
    private val effectDirection: Byte

    /**
     * which transition is used when transitioning to the next presentation slide
     * during a slide show. Any of the following samples are for sample purposes only.
     * Exact rendering of any transition is determined by the rendering application.
     * As such, the same transition can have many variations depending on the implementation.
     * 0    Cut
     * The following specifies the possible effectDirection values and their meanings:
     * 飩э��		0x00: The transition is not made through black. (The effect is the same as
     * no transition at all.)
     * 飩э��		0x01: The transition is made through black.
     * 1    Random
     * effectDirection MUST be ignored.
     * 2    Blinds
     * 飩э��		0x00: Vertical
     * 飩э��		0x01: Horizontal
     * 3    Checker
     * 飩э��		0x00: Horizontal
     * 飩э��		0x01: Vertical
     * 4    Cover
     * 飩э��		0x00: Left
     * 飩э��		0x01: Up
     * 飩э��		0x02: Right
     * 飩э��		0x03: Down
     * 飩э��		0x04: Left Up
     * 飩э��		0x05: Right Up
     * 飩э��		0x06: Left Down
     * 飩э��		0x07: Right Down
     * 5    Dissolve
     * effectDirection MUST be 0x00.
     * 6    Fade
     * effectDirection MUST be 0x00.
     * 7    Uncover
     * 飩э��		0x00: Left
     * 飩э�狅偋飥�	0x01: Up
     * 飩э�狅偋飥�	0x02: Right
     * 飩э�狅偋飥�	0x03: Down
     * 飩э�狅偋飥�	0x04: Left Up
     * 飩э�狅偋飥�	0x05: Right Up
     * 飩э�狅偋飥�	0x06: Left Down
     * 飩э�狅偋飥�	0x07: Right Down
     * 8    Random Bars
     * 飩э��		0x00: Horizontal
     * 0x01: Vertica
     * 9    Strips
     * 飥�		0x04: Left Up
     * 飥�	飩э��0x05: Right Up
     * 飥�	飩э��0x06: Left Down
     * 飥�		0x07: Right Down
     * 10   Wipe
     * 0x00: Left
     * 0x01: Up
     * 0x02: Right
     * 0x03: Down
     * 11   Box In/Out
     * 飥�		0x00: Out
     * 飥狅偋飥�	0x01: In
     * 13   Split
     * 飥�		0x00: Horizontally out
     * 飥�	飩э��0x01: Horizontally in
     * 飥�	飩э��0x02: Vertically out
     * 飥�	飩э��0x03: Vertically in
     * 17   Diamond
     * effectDirection MUST be 0x00.
     * 18   Plus
     * effectDirection MUST be 0x00.
     * 19   Wedge
     * effectDirection MUST be 0x00.
     * 20   Push
     * 飥�		0x00: Left
     * 飥狅偋飥�	0x01: Up
     * 飥狅偋飥�	0x02: Right
     * 飥狅偋飥�	0x03: Down
     * 21   Comb
     * 0x00: Horizontal
     * 飩э��0x01: Vertical
     * 22   Newsflash
     * effectDirection MUST be 0x00.
     * 23   AlphaFade
     * effectDirection MUST be 0x00.
     * 26   Wheel
     * The value MUST be one of 0x01, 0x02, 0x03, 0x04, or 0x08.
     * 27   Circle
     * effectDirection MUST be 0x00.
     * 255  Undefined and MUST be ignored
     */
    private val effectType: Byte

    //whether the presentation slide can be manually advanced by the user during the slide show.
    private val fManualAdvance = false

    //MUST be zero and MUST be ignored
    private val reserved1 = false

    //whether the corresponding slide is hidden and is not displayed during the slide show
    private val fHidden = false

    //MUST be zero and MUST be ignored.
    private val reserved2 = false

    //whether to play the sound specified by soundIfRef.
    private val fSound = false

    //MUST be zero and MUST be ignored.
    private val reserved3 = false

    //whether the sound specified by soundIdRef is looped continuously when playing until the next sound plays.
    private val fLoopSound = false

    //MUST be zero and MUST be ignored.
    private val reserved4 = false

    //whether to stop any currently playing sound when the transition starts.
    private val fStopSound = false

    //MUST be zero and MUST be ignored.
    private val reserved5 = false

    //whether the slide will automatically advance after slideTime milliseconds during the slide show.
    private val fAutoAdvance = false

    //MUST be zero and MUST be ignored.
    private val reserved6 = false

    //whether to display the cursor during the slide show
    private val fCursorVisible = false

    //MUST be zero and MUST be ignored.
    private val reserved7: Byte = 0

    //how long the transition takes to run.
    //0x00    0.75 seconds
    //0x01    0.5 seconds
    //0x02    0.25 seconds
    private val speed: Byte

    private val unused: ByteArray? = null

    /**
     * 
     */
    /**
     * For the UserEdit Atom
     */
    init {
        // Sanity Checking
        var len = len
        if (len < 24) {
            len = 24
        }


        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        slideTime = getInt(source, start + 8)
        soundIdRef = getInt(source, start + 12)
        effectDirection = source[start + 16]
        effectType = source[start + 17]

        speed = source[start + 20]
    }

    val isValidateTransition: Boolean
        get() {
            when (effectType.toInt()) {
                0 -> return effectDirection >= 0 && effectDirection <= 1
                1 -> return true
                2, 3 -> return effectDirection >= 0 && effectDirection <= 1
                4 -> return effectDirection >= 0 && effectDirection <= 7
                5 -> return effectDirection.toInt() == 0
                6 -> return effectDirection.toInt() == 0
                7 -> return effectDirection >= 0 && effectDirection <= 7
                8 -> return effectDirection >= 0 && effectDirection <= 1
                9 -> return effectDirection >= 4 && effectDirection <= 7
                10 -> return effectDirection >= 0 && effectDirection <= 3
                11 -> return effectDirection >= 0 && effectDirection <= 1
                13 -> return effectDirection >= 0 && effectDirection <= 3
                17, 18, 19 -> return effectDirection.toInt() == 0
                20 -> return effectDirection >= 0 && effectDirection <= 3
                21 -> return effectDirection >= 0 && effectDirection <= 1
                22, 23 -> return effectDirection.toInt() == 0
                26 -> return (effectDirection >= 1 && effectDirection <= 4) || (effectDirection.toInt() == 8)
                27 -> return effectDirection.toInt() == 0
            }

            return false
        }

    /**
     * We are of type 12011
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * At write-out time, update the references to PersistPtrs and
     * other UserEditAtoms to point to their new positions
     */
    public override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?) {
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }

    companion object {
        private const val _type: Long = 0x03F9
    }
}

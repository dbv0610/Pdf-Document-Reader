/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is mozilla.org code.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1998
 * the Initial Developer. All Rights Reserved.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package com.wxiwei.office.thirdpart.mozilla.intl.chardet

import kotlin.math.sqrt

class nsEUCSampler {
    var mTotal: Int = 0
    var mThreshold: Int = 200
    var mState: Int = 0
    var mFirstByteCnt: IntArray? = IntArray(94)
    var mSecondByteCnt: IntArray? = IntArray(94)
    var mFirstByteFreq: FloatArray? = FloatArray(94)
    var mSecondByteFreq: FloatArray? = FloatArray(94)

    init {
        Reset()
    }

    fun Reset() {
        mTotal = 0
        mState = 0
        for (i in 0..93) {
            mSecondByteCnt!![i] = 0
            mFirstByteCnt!![i] = mSecondByteCnt!![i]
        }
    }

    fun EnoughData(): Boolean {
        return mTotal > mThreshold
    }

    fun GetSomeData(): Boolean {
        return mTotal > 1
    }

    fun Sample(aIn: ByteArray?, aLen: Int): Boolean {
        if (mState == 1) return false

        var p = 0

        // if(aLen + mTotal > 0x80000000) 
        //    aLen = 0x80000000 - mTotal;
        var i: Int = 0
        i = 0
        while ((i < aLen) && (1 != mState)) {
            when (mState) {
                0 -> if ((aIn!![p].toInt() and 0x0080) != 0) {
                    if ((0xff == (0xff and aIn!![p].toInt())) || (0xa1 > (0xff and aIn!![p].toInt()))) {
                        mState = 1
                    } else {
                        mTotal++
                        mFirstByteCnt!![(0xff and aIn!![p].toInt()) - 0xa1] =
                            mFirstByteCnt!![(0xff and aIn!![p].toInt()) - 0xa1] + 1
                        mState = 2
                    }
                }

                1 -> {}
                2 -> if ((aIn!![p].toInt() and 0x0080) != 0) {
                    if ((0xff == (0xff and aIn!![p].toInt()))
                        || (0xa1 > (0xff and aIn!![p].toInt()))
                    ) {
                        mState = 1
                    } else {
                        mTotal++
                        mSecondByteCnt!![(0xff and aIn!![p].toInt()) - 0xa1] =
                            mSecondByteCnt!![(0xff and aIn!![p].toInt()) - 0xa1] + 1
                        mState = 0
                    }
                } else {
                    mState = 1
                }

                else -> mState = 1
            }
            i++
            p++
        }
        return (1 != mState)
    }


    fun CalFreq() {
        for (i in 0..93) {
            mFirstByteFreq!![i] = mFirstByteCnt!![i].toFloat() / mTotal.toFloat()
            mSecondByteFreq!![i] = mSecondByteCnt!![i].toFloat() / mTotal.toFloat()
        }
    }

    fun GetScore(
        aFirstByteFreq: FloatArray, aFirstByteWeight: Float,
        aSecondByteFreq: FloatArray, aSecondByteWeight: Float
    ): Float {
        return aFirstByteWeight * GetScore(aFirstByteFreq, mFirstByteFreq!!) +
                aSecondByteWeight * GetScore(aSecondByteFreq, mSecondByteFreq!!)
    }

    fun GetScore(array1: FloatArray, array2: FloatArray): Float {
        var s: Float = 0f
        var sum = 0.0f

        for (i in 0..93) {
            s = array1[i] - array2[i]
            sum += s * s
        }
        return sqrt(sum.toDouble()).toFloat() / 94.0f
    }
}



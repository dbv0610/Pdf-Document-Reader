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

abstract class nsPSMDetector {
    var mVerifier: Array<nsVerifier?>? = null
    var mStatisticsData: Array<nsEUCStatistics?>? = null

    var mSampler: nsEUCSampler = nsEUCSampler()
    var mState: ByteArray = ByteArray(MAX_VERIFIERS)
    var mItemIdx: IntArray = IntArray(MAX_VERIFIERS)

    var mItems: Int = 0
    var mClassItems: Int = 0

    var mDone: Boolean = false
    var mRunSampler: Boolean = false
    var mClassRunSampler: Boolean = false

    constructor() {
        initVerifiers(ALL)
        Reset()
    }

    constructor(langFlag: Int) {
        initVerifiers(langFlag)
        Reset()
    }

    constructor(
        aItems: Int,
        aVerifierSet: Array<nsVerifier?>?,
        aStatisticsSet: Array<nsEUCStatistics?>?
    ) {
        mClassRunSampler = (aStatisticsSet != null)
        mStatisticsData = aStatisticsSet
        mVerifier = aVerifierSet

        mClassItems = aItems
        Reset()
    }

    fun Reset() {
        mRunSampler = mClassRunSampler
        mDone = false
        mItems = mClassItems

        for (i in 0..<mItems) {
            mState[i] = 0
            mItemIdx[i] = i
        }

        mSampler.Reset()
    }

    protected fun initVerifiers(currVerSet: Int) {
        val idx = 0
        val currVerifierSet: Int

        if (currVerSet >= 0 && currVerSet < NO_OF_LANGUAGES) {
            currVerifierSet = currVerSet
        } else {
            currVerifierSet = ALL
        }

        mVerifier = null
        mStatisticsData = null

        if (currVerifierSet == TRADITIONAL_CHINESE) {
            mVerifier = arrayOf<nsVerifier?>(
                nsUTF8Verifier(), nsBIG5Verifier(),
                nsISO2022CNVerifier(), nsEUCTWVerifier(), nsCP1252Verifier(),
                nsUCS2BEVerifier(), nsUCS2LEVerifier()
            )

            mStatisticsData = arrayOf<nsEUCStatistics?>(
                null, Big5Statistics(), null,
                EUCTWStatistics(), null, null, null
            )
        } else if (currVerifierSet == KOREAN) {
            mVerifier = arrayOf<nsVerifier?>(
                nsUTF8Verifier(), nsEUCKRVerifier(),
                nsISO2022KRVerifier(), nsCP1252Verifier(), nsUCS2BEVerifier(),
                nsUCS2LEVerifier()
            )
        } else if (currVerifierSet == SIMPLIFIED_CHINESE) {
            mVerifier = arrayOf<nsVerifier?>(
                nsUTF8Verifier(), nsGB2312Verifier(),
                nsGB18030Verifier(), nsISO2022CNVerifier(), nsHZVerifier(),
                nsCP1252Verifier(), nsUCS2BEVerifier(), nsUCS2LEVerifier()
            )
        } else if (currVerifierSet == JAPANESE) {
            mVerifier = arrayOf<nsVerifier?>(
                nsUTF8Verifier(), nsSJISVerifier(),
                nsEUCJPVerifier(), nsISO2022JPVerifier(), nsCP1252Verifier(),
                nsUCS2BEVerifier(), nsUCS2LEVerifier()
            )
        } else if (currVerifierSet == CHINESE) {
            mVerifier = arrayOf<nsVerifier?>(
                nsUTF8Verifier(), nsGB2312Verifier(),
                nsGB18030Verifier(), nsBIG5Verifier(), nsISO2022CNVerifier(),
                nsHZVerifier(), nsEUCTWVerifier(), nsCP1252Verifier(),
                nsUCS2BEVerifier(), nsUCS2LEVerifier()
            )

            mStatisticsData = arrayOf<nsEUCStatistics?>(
                null, GB2312Statistics(), null,
                Big5Statistics(), null, null, EUCTWStatistics(), null, null, null
            )
        } else if (currVerifierSet == ALL) {
            mVerifier = arrayOf<nsVerifier?>(
                nsUTF8Verifier(), nsSJISVerifier(),
                nsEUCJPVerifier(), nsISO2022JPVerifier(), nsEUCKRVerifier(),
                nsISO2022KRVerifier(), nsBIG5Verifier(), nsEUCTWVerifier(),
                nsGB2312Verifier(), nsGB18030Verifier(), nsISO2022CNVerifier(),
                nsHZVerifier(), nsCP1252Verifier(), nsUCS2BEVerifier(),
                nsUCS2LEVerifier()
            )

            mStatisticsData = arrayOf<nsEUCStatistics?>(
                null, null, EUCJPStatistics(), null,
                EUCKRStatistics(), null, Big5Statistics(), EUCTWStatistics(),
                GB2312Statistics(), null, null, null, null, null, null
            )
        }

        mClassRunSampler = (mStatisticsData != null)
        mClassItems = mVerifier!!.size
    }

    abstract fun Report(charset: String?)

    fun HandleData(aBuf: ByteArray, len: Int): Boolean {
        var i: Int = 0
        var j: Int = 0
        var b: Byte
        var st: Byte

        i = 0
        while (i < len) {
            b = aBuf[i]
            j = 0
            while (j < mItems) {
                st = nsVerifier.Companion.getNextState(mVerifier!![mItemIdx[j]]!!, b, mState[j])
                if (st == nsVerifier.Companion.eItsMe) {
                    Report(mVerifier!![mItemIdx[j]]!!.charset())
                    mDone = true
                    return mDone
                } else if (st == nsVerifier.Companion.eError) {
                    mItems--
                    if (j < mItems) {
                        mItemIdx[j] = mItemIdx[mItems]
                        mState[j] = mState[mItems]
                    }
                } else {
                    mState[j++] = st
                }
            }
            if (mItems <= 1) {
                if (1 == mItems) {
                    Report(mVerifier!![mItemIdx[0]]!!.charset())
                }
                mDone = true
                return mDone
            } else {
                var nonUCS2Num = 0
                var nonUCS2Idx = 0
                j = 0
                while (j < mItems) {
                    if ((!(mVerifier!![mItemIdx[j]]!!.isUCS2()))
                        && (!(mVerifier!![mItemIdx[j]]!!.isUCS2()))
                    ) {
                        nonUCS2Num++
                        nonUCS2Idx = j
                    }
                    j++
                }
                if (1 == nonUCS2Num) {
                    Report(mVerifier!![mItemIdx[nonUCS2Idx]]!!.charset())
                    mDone = true
                    return mDone
                }
            }
            i++
        }
        if (mRunSampler) {
            Sample(aBuf, len)
        }
        return mDone
    }

    fun DataEnd() {
        if (mDone == true) {
            return
        }

        if (mItems == 2) {
            if ((mVerifier!![mItemIdx[0]]!!.charset()) == "GB18030") {
                Report(mVerifier!![mItemIdx[1]]!!.charset())
                mDone = true
            } else if ((mVerifier!![mItemIdx[1]]!!.charset()) == "GB18030") {
                Report(mVerifier!![mItemIdx[0]]!!.charset())
                mDone = true
            }
        }
        if (mItems == 4) {
            if ((mVerifier!![mItemIdx[1]]!!.charset()) == "Shift_JIS") {
                Report(mVerifier!![mItemIdx[1]]!!.charset())
                mDone = true
            }
        }
        if (mRunSampler) {
            Sample(null, 0, true)
        }
    }

    @JvmOverloads
    fun Sample(aBuf: ByteArray?, aLen: Int, aLastChance: Boolean = false) {
        var possibleCandidateNum = 0
        var j: Int = 0
        var eucNum = 0

        j = 0
        while (j < mItems) {
            if (null != mStatisticsData!![mItemIdx[j]]) eucNum++
            if ((!mVerifier!![mItemIdx[j]]!!.isUCS2())
                && ((mVerifier!![mItemIdx[j]]!!.charset()) != "GB18030")
            ) possibleCandidateNum++
            j++
        }

        mRunSampler = (eucNum > 1)

        if (mRunSampler) {
            mRunSampler = mSampler.Sample(aBuf, aLen)
            if (((aLastChance && mSampler.GetSomeData()) || mSampler.EnoughData())
                && (eucNum == possibleCandidateNum)
            ) {
                mSampler.CalFreq()

                var bestIdx = -1
                var eucCnt = 0
                var bestScore = 0.0f
                j = 0
                while (j < mItems) {
                    if ((null != mStatisticsData!![mItemIdx[j]])
                        && ((mVerifier!![mItemIdx[j]]!!.charset()) != "Big5")
                    ) {
                        val score = mSampler.GetScore(
                            mStatisticsData!![mItemIdx[j]]!!.mFirstByteFreq()!!,
                            mStatisticsData!![mItemIdx[j]]!!.mFirstByteWeight(),
                            mStatisticsData!![mItemIdx[j]]!!.mSecondByteFreq()!!,
                            mStatisticsData!![mItemIdx[j]]!!.mSecondByteWeight()
                        )
                        //System.out.println("FequencyScore("+mVerifier[mItemIdx[j]].charset()+")= "+ score);
                        if ((0 == eucCnt++) || (bestScore > score)) {
                            bestScore = score
                            bestIdx = j
                        } // if(( 0 == eucCnt++) || (bestScore > score )) 
                    } // if(null != ...)

                    j++
                }
                if (bestIdx >= 0) {
                    Report(mVerifier!![mItemIdx[bestIdx]]!!.charset())
                    mDone = true
                }
            } // if (eucNum == possibleCandidateNum)
        } // if(mRunSampler)
    }

    val probableCharsets: Array<String?>
        get() {
            if (mItems <= 0) {
                val nomatch = arrayOfNulls<String>(1)
                nomatch[0] = "nomatch"
                return nomatch
            }
            val ret: Array<String?>? = arrayOfNulls<String>(mItems)
            for (i in 0..<mItems) {
                ret!![i] = mVerifier!![mItemIdx[i]]!!.charset()
            }
            return ret!!
        }

    companion object {
        const val ALL: Int = 0
        const val JAPANESE: Int = 1
        const val CHINESE: Int = 2
        const val SIMPLIFIED_CHINESE: Int = 3
        const val TRADITIONAL_CHINESE: Int = 4
        const val KOREAN: Int = 5

        const val NO_OF_LANGUAGES: Int = 6
        const val MAX_VERIFIERS: Int = 16
    }
}

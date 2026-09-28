/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hpsf

/**
 * 
 * Defines constants of general use.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
object Constants {
    /** 
     *
     *Codepage 037, a special case  */
    const val CP_037: Int = 37

    /** 
     *
     *Codepage for SJIS  */
    const val CP_SJIS: Int = 932

    /** 
     *
     *Codepage for GBK, aka MS936  */
    const val CP_GBK: Int = 936

    /** 
     *
     *Codepage for MS949  */
    const val CP_MS949: Int = 949

    /** 
     *
     *Codepage for UTF-16  */
    const val CP_UTF16: Int = 1200

    /** 
     *
     *Codepage for UTF-16 big-endian  */
    const val CP_UTF16_BE: Int = 1201

    /** 
     *
     *Codepage for Windows 1250  */
    const val CP_WINDOWS_1250: Int = 1250

    /** 
     *
     *Codepage for Windows 1251  */
    const val CP_WINDOWS_1251: Int = 1251

    /** 
     *
     *Codepage for Windows 1252  */
    const val CP_WINDOWS_1252: Int = 1252

    /** 
     *
     *Codepage for Windows 1253  */
    const val CP_WINDOWS_1253: Int = 1253

    /** 
     *
     *Codepage for Windows 1254  */
    const val CP_WINDOWS_1254: Int = 1254

    /** 
     *
     *Codepage for Windows 1255  */
    const val CP_WINDOWS_1255: Int = 1255

    /** 
     *
     *Codepage for Windows 1256  */
    const val CP_WINDOWS_1256: Int = 1256

    /** 
     *
     *Codepage for Windows 1257  */
    const val CP_WINDOWS_1257: Int = 1257

    /** 
     *
     *Codepage for Windows 1258  */
    const val CP_WINDOWS_1258: Int = 1258

    /** 
     *
     *Codepage for Johab  */
    const val CP_JOHAB: Int = 1361

    /** 
     *
     *Codepage for Macintosh Roman (Java: MacRoman)  */
    const val CP_MAC_ROMAN: Int = 10000

    /** 
     *
     *Codepage for Macintosh Japan (Java: unknown - use SJIS, cp942 or
     * cp943)  */
    const val CP_MAC_JAPAN: Int = 10001

    /** 
     *
     *Codepage for Macintosh Chinese Traditional (Java: unknown - use Big5,
     * MS950, or cp937)  */
    const val CP_MAC_CHINESE_TRADITIONAL: Int = 10002

    /** 
     *
     *Codepage for Macintosh Korean (Java: unknown - use EUC_KR or
     * cp949)  */
    const val CP_MAC_KOREAN: Int = 10003

    /** 
     *
     *Codepage for Macintosh Arabic (Java: MacArabic)  */
    const val CP_MAC_ARABIC: Int = 10004

    /** 
     *
     *Codepage for Macintosh Hebrew (Java: MacHebrew)  */
    const val CP_MAC_HEBREW: Int = 10005

    /** 
     *
     *Codepage for Macintosh Greek (Java: MacGreek)  */
    const val CP_MAC_GREEK: Int = 10006

    /** 
     *
     *Codepage for Macintosh Cyrillic (Java: MacCyrillic)  */
    const val CP_MAC_CYRILLIC: Int = 10007

    /** 
     *
     *Codepage for Macintosh Chinese Simplified (Java: unknown - use
     * EUC_CN, ISO2022_CN_GB, MS936 or cp935)  */
    const val CP_MAC_CHINESE_SIMPLE: Int = 10008

    /** 
     *
     *Codepage for Macintosh Romanian (Java: MacRomania)  */
    const val CP_MAC_ROMANIA: Int = 10010

    /** 
     *
     *Codepage for Macintosh Ukrainian (Java: MacUkraine)  */
    const val CP_MAC_UKRAINE: Int = 10017

    /** 
     *
     *Codepage for Macintosh Thai (Java: MacThai)  */
    const val CP_MAC_THAI: Int = 10021

    /** 
     *
     *Codepage for Macintosh Central Europe (Latin-2)
     * (Java: MacCentralEurope)  */
    const val CP_MAC_CENTRAL_EUROPE: Int = 10029

    /** 
     *
     *Codepage for Macintosh Iceland (Java: MacIceland)  */
    const val CP_MAC_ICELAND: Int = 10079

    /** 
     *
     *Codepage for Macintosh Turkish (Java: MacTurkish)  */
    const val CP_MAC_TURKISH: Int = 10081

    /** 
     *
     *Codepage for Macintosh Croatian (Java: MacCroatian)  */
    const val CP_MAC_CROATIAN: Int = 10082

    /** 
     *
     *Codepage for US-ASCII  */
    const val CP_US_ACSII: Int = 20127

    /** 
     *
     *Codepage for KOI8-R  */
    const val CP_KOI8_R: Int = 20866

    /** 
     *
     *Codepage for ISO-8859-1  */
    const val CP_ISO_8859_1: Int = 28591

    /** 
     *
     *Codepage for ISO-8859-2  */
    const val CP_ISO_8859_2: Int = 28592

    /** 
     *
     *Codepage for ISO-8859-3  */
    const val CP_ISO_8859_3: Int = 28593

    /** 
     *
     *Codepage for ISO-8859-4  */
    const val CP_ISO_8859_4: Int = 28594

    /** 
     *
     *Codepage for ISO-8859-5  */
    const val CP_ISO_8859_5: Int = 28595

    /** 
     *
     *Codepage for ISO-8859-6  */
    const val CP_ISO_8859_6: Int = 28596

    /** 
     *
     *Codepage for ISO-8859-7  */
    const val CP_ISO_8859_7: Int = 28597

    /** 
     *
     *Codepage for ISO-8859-8  */
    const val CP_ISO_8859_8: Int = 28598

    /** 
     *
     *Codepage for ISO-8859-9  */
    const val CP_ISO_8859_9: Int = 28599

    /** 
     *
     *Codepage for ISO-2022-JP  */
    const val CP_ISO_2022_JP1: Int = 50220

    /** 
     *
     *Another codepage for ISO-2022-JP  */
    const val CP_ISO_2022_JP2: Int = 50221

    /** 
     *
     *Yet another codepage for ISO-2022-JP  */
    const val CP_ISO_2022_JP3: Int = 50222

    /** 
     *
     *Codepage for ISO-2022-KR  */
    const val CP_ISO_2022_KR: Int = 50225

    /** 
     *
     *Codepage for EUC-JP  */
    const val CP_EUC_JP: Int = 51932

    /** 
     *
     *Codepage for EUC-KR  */
    const val CP_EUC_KR: Int = 51949

    /** 
     *
     *Codepage for GB2312  */
    const val CP_GB2312: Int = 52936

    /** 
     *
     *Codepage for GB18030  */
    const val CP_GB18030: Int = 54936

    /** 
     *
     *Another codepage for US-ASCII  */
    const val CP_US_ASCII2: Int = 65000

    /** 
     *
     *Codepage for UTF-8  */
    const val CP_UTF8: Int = 65001

    /** 
     *
     *Codepage for Unicode  */
    val CP_UNICODE: Int = CP_UTF16
}

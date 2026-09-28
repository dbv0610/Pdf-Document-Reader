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
package com.wxiwei.office.fc.hssf.util

import com.wxiwei.office.fc.ss.usermodel.Color
import java.lang.reflect.Field
import java.util.Collections
import java.util.Hashtable

/**
 * Intends to provide support for the very evil index to triplet issue and
 * will likely replace the color constants interface for HSSF 2.0.
 * This class contains static inner class members for representing colors.
 * Each color has an index (for the standard palette in Excel (tm) ),
 * native (RGB) triplet and string triplet.  The string triplet is as the
 * color would be represented by Gnumeric.  Having (string) this here is a bit of a
 * collusion of function between HSSF and the HSSFSerializer but I think its
 * a reasonable one in this case.
 * 
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Brian Sanders (bsanders at risklabs dot com) - full default color palette
 */
open class HSSFColor
/** Creates a new instance of HSSFColor  */
    : Color {
    open val index: Short
        /**
         * @return index to the standard palette
         */
        get() = BLACK.index

    open val triplet: ShortArray
        /**
         * @return  triplet representation like that in Excel
         */
        get() = BLACK.triplet

    // its a hack but its a good hack
    open val hexString: String
        /**
         * @return a hex string exactly like a gnumeric triplet
         */
        get() = BLACK.hexString

    /**
     * Class BLACK
     * 
     */
    class BLACK

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x8
            val triplet: ShortArray = shortArrayOf(
                0, 0, 0
            )
            const val hexString: String = "0:0:0"
        }
    }

    /**
     * Class BROWN
     * 
     */
    class BROWN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x3c
            val triplet: ShortArray = shortArrayOf(
                153, 51, 0
            )
            const val hexString: String = "9999:3333:0"
        }
    }

    /**
     * Class OLIVE_GREEN
     * 
     */
    class OLIVE_GREEN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x3b
            val triplet: ShortArray = shortArrayOf(
                51, 51, 0
            )
            const val hexString: String = "3333:3333:0"
        }
    }

    /**
     * Class DARK_GREEN
     * 
     */
    class DARK_GREEN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x3a
            val triplet: ShortArray = shortArrayOf(
                0, 51, 0
            )
            const val hexString: String = "0:3333:0"
        }
    }

    /**
     * Class DARK_TEAL
     * 
     */
    class DARK_TEAL

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x38
            val triplet: ShortArray = shortArrayOf(
                0, 51, 102
            )
            const val hexString: String = "0:3333:6666"
        }
    }

    /**
     * Class DARK_BLUE
     * 
     */
    class DARK_BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x12
            const val index2: Short = 0x20
            val triplet: ShortArray = shortArrayOf(
                0, 0, 128
            )
            const val hexString: String = "0:0:8080"
        }
    }

    /**
     * Class INDIGO
     * 
     */
    class INDIGO

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x3e
            val triplet: ShortArray = shortArrayOf(
                51, 51, 153
            )
            const val hexString: String = "3333:3333:9999"
        }
    }

    /**
     * Class GREY_80_PERCENT
     * 
     */
    class GREY_80_PERCENT

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x3f
            val triplet: ShortArray = shortArrayOf(
                51, 51, 51
            )
            const val hexString: String = "3333:3333:3333"
        }
    }

    /**
     * Class DARK_RED
     * 
     */
    class DARK_RED

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x10
            const val index2: Short = 0x25
            val triplet: ShortArray = shortArrayOf(
                128, 0, 0
            )
            const val hexString: String = "8080:0:0"
        }
    }

    /**
     * Class ORANGE
     * 
     */
    class ORANGE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x35
            val triplet: ShortArray = shortArrayOf(
                255, 102, 0
            )
            const val hexString: String = "FFFF:6666:0"
        }
    }

    /**
     * Class DARK_YELLOW
     * 
     */
    class DARK_YELLOW

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x13
            val triplet: ShortArray = shortArrayOf(
                128, 128, 0
            )
            const val hexString: String = "8080:8080:0"
        }
    }

    /**
     * Class GREEN
     * 
     */
    class GREEN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x11
            val triplet: ShortArray = shortArrayOf(
                0, 128, 0
            )
            const val hexString: String = "0:8080:0"
        }
    }

    /**
     * Class TEAL
     * 
     */
    class TEAL

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x15
            const val index2: Short = 0x26
            val triplet: ShortArray = shortArrayOf(
                0, 128, 128
            )
            const val hexString: String = "0:8080:8080"
        }
    }

    /**
     * Class BLUE
     * 
     */
    class BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0xc
            const val index2: Short = 0x27
            val triplet: ShortArray = shortArrayOf(
                0, 0, 255
            )
            const val hexString: String = "0:0:FFFF"
        }
    }

    /**
     * Class BLUE_GREY
     * 
     */
    class BLUE_GREY

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x36
            val triplet: ShortArray = shortArrayOf(
                102, 102, 153
            )
            const val hexString: String = "6666:6666:9999"
        }
    }

    /**
     * Class GREY_50_PERCENT
     * 
     */
    class GREY_50_PERCENT

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x17
            val triplet: ShortArray = shortArrayOf(
                128, 128, 128
            )
            const val hexString: String = "8080:8080:8080"
        }
    }

    /**
     * Class RED
     * 
     */
    class RED

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0xa
            val triplet: ShortArray = shortArrayOf(
                255, 0, 0
            )
            const val hexString: String = "FFFF:0:0"
        }
    }

    /**
     * Class LIGHT_ORANGE
     * 
     */
    class LIGHT_ORANGE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x34
            val triplet: ShortArray = shortArrayOf(
                255, 153, 0
            )
            const val hexString: String = "FFFF:9999:0"
        }
    }

    /**
     * Class LIME
     * 
     */
    class LIME

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x32
            val triplet: ShortArray = shortArrayOf(
                153, 204, 0
            )
            const val hexString: String = "9999:CCCC:0"
        }
    }

    /**
     * Class SEA_GREEN
     * 
     */
    class SEA_GREEN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x39
            val triplet: ShortArray = shortArrayOf(
                51, 153, 102
            )
            const val hexString: String = "3333:9999:6666"
        }
    }

    /**
     * Class AQUA
     * 
     */
    class AQUA

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x31
            val triplet: ShortArray = shortArrayOf(
                51, 204, 204
            )
            const val hexString: String = "3333:CCCC:CCCC"
        }
    }

    /**
     * Class LIGHT_BLUE
     * 
     */
    class LIGHT_BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x30
            val triplet: ShortArray = shortArrayOf(
                51, 102, 255
            )
            const val hexString: String = "3333:6666:FFFF"
        }
    }

    /**
     * Class VIOLET
     * 
     */
    class VIOLET

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x14
            const val index2: Short = 0x24
            val triplet: ShortArray = shortArrayOf(
                128, 0, 128
            )
            const val hexString: String = "8080:0:8080"
        }
    }

    /**
     * Class GREY_40_PERCENT
     * 
     */
    class GREY_40_PERCENT

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x37
            val triplet: ShortArray = shortArrayOf(
                150, 150, 150
            )
            const val hexString: String = "9696:9696:9696"
        }
    }

    /**
     * Class PINK
     * 
     */
    class PINK

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0xe
            const val index2: Short = 0x21
            val triplet: ShortArray = shortArrayOf(
                255, 0, 255
            )
            const val hexString: String = "FFFF:0:FFFF"
        }
    }

    /**
     * Class GOLD
     * 
     */
    class GOLD

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x33
            val triplet: ShortArray = shortArrayOf(
                255, 204, 0
            )
            const val hexString: String = "FFFF:CCCC:0"
        }
    }

    /**
     * Class YELLOW
     * 
     */
    class YELLOW

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0xd
            const val index2: Short = 0x22
            val triplet: ShortArray = shortArrayOf(
                255, 255, 0
            )
            const val hexString: String = "FFFF:FFFF:0"
        }
    }

    /**
     * Class BRIGHT_GREEN
     * 
     */
    class BRIGHT_GREEN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val hexString: String
            get() = Companion.hexString

        override val triplet: ShortArray
            get() = Companion.triplet

        companion object {
            const val index: Short = 0xb
            const val index2: Short = 0x23
            val triplet: ShortArray = shortArrayOf(
                0, 255, 0
            )
            const val hexString: String = "0:FFFF:0"
        }
    }

    /**
     * Class TURQUOISE
     * 
     */
    class TURQUOISE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0xf
            const val index2: Short = 0x23
            val triplet: ShortArray = shortArrayOf(
                0, 255, 255
            )
            const val hexString: String = "0:FFFF:FFFF"
        }
    }

    /**
     * Class SKY_BLUE
     * 
     */
    class SKY_BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x28
            val triplet: ShortArray = shortArrayOf(
                0, 204, 255
            )
            const val hexString: String = "0:CCCC:FFFF"
        }
    }

    /**
     * Class PLUM
     * 
     */
    class PLUM

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x3d
            const val index2: Short = 0x19
            val triplet: ShortArray = shortArrayOf(
                153, 51, 102
            )
            const val hexString: String = "9999:3333:6666"
        }
    }

    /**
     * Class GREY_25_PERCENT
     * 
     */
    class GREY_25_PERCENT

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x16
            val triplet: ShortArray = shortArrayOf(
                192, 192, 192
            )
            const val hexString: String = "C0C0:C0C0:C0C0"
        }
    }

    /**
     * Class ROSE
     * 
     */
    class ROSE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x2d
            val triplet: ShortArray = shortArrayOf(
                255, 153, 204
            )
            const val hexString: String = "FFFF:9999:CCCC"
        }
    }

    /**
     * Class TAN
     * 
     */
    class TAN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x2f
            val triplet: ShortArray = shortArrayOf(
                255, 204, 153
            )
            const val hexString: String = "FFFF:CCCC:9999"
        }
    }

    /**
     * Class LIGHT_YELLOW
     * 
     */
    class LIGHT_YELLOW

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x2b
            val triplet: ShortArray = shortArrayOf(
                255, 255, 153
            )
            const val hexString: String = "FFFF:FFFF:9999"
        }
    }

    /**
     * Class LIGHT_GREEN
     * 
     */
    class LIGHT_GREEN

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x2a
            val triplet: ShortArray = shortArrayOf(
                204, 255, 204
            )
            const val hexString: String = "CCCC:FFFF:CCCC"
        }
    }

    /**
     * Class LIGHT_TURQUOISE
     * 
     */
    class LIGHT_TURQUOISE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x29
            const val index2: Short = 0x1b
            val triplet: ShortArray = shortArrayOf(
                204, 255, 255
            )
            const val hexString: String = "CCCC:FFFF:FFFF"
        }
    }

    /**
     * Class PALE_BLUE
     * 
     */
    class PALE_BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x2c
            val triplet: ShortArray = shortArrayOf(
                153, 204, 255
            )
            const val hexString: String = "9999:CCCC:FFFF"
        }
    }

    /**
     * Class LAVENDER
     * 
     */
    class LAVENDER

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x2e
            val triplet: ShortArray = shortArrayOf(
                204, 153, 255
            )
            const val hexString: String = "CCCC:9999:FFFF"
        }
    }

    /**
     * Class WHITE
     * 
     */
    class WHITE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x9
            val triplet: ShortArray = shortArrayOf(
                255, 255, 255
            )
            const val hexString: String = "FFFF:FFFF:FFFF"
        }
    }

    /**
     * Class CORNFLOWER_BLUE
     */
    class CORNFLOWER_BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x18
            val triplet: ShortArray = shortArrayOf(
                153, 153, 255
            )
            const val hexString: String = "9999:9999:FFFF"
        }
    }


    /**
     * Class LEMON_CHIFFON
     */
    class LEMON_CHIFFON

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x1a
            val triplet: ShortArray = shortArrayOf(
                255, 255, 204
            )
            const val hexString: String = "FFFF:FFFF:CCCC"
        }
    }

    /**
     * Class MAROON
     */
    class MAROON

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x19
            val triplet: ShortArray = shortArrayOf(
                127, 0, 0
            )
            const val hexString: String = "8000:0:0"
        }
    }

    /**
     * Class ORCHID
     */
    class ORCHID

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x1c
            val triplet: ShortArray = shortArrayOf(
                102, 0, 102
            )
            const val hexString: String = "6666:0:6666"
        }
    }

    /**
     * Class CORAL
     */
    class CORAL

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x1d
            val triplet: ShortArray = shortArrayOf(
                255, 128, 128
            )
            const val hexString: String = "FFFF:8080:8080"
        }
    }

    /**
     * Class ROYAL_BLUE
     */
    class ROYAL_BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x1e
            val triplet: ShortArray = shortArrayOf(
                0, 102, 204
            )
            const val hexString: String = "0:6666:CCCC"
        }
    }

    /**
     * Class LIGHT_CORNFLOWER_BLUE
     */
    class LIGHT_CORNFLOWER_BLUE

        : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = Companion.triplet

        override val hexString: String
            get() = Companion.hexString

        companion object {
            const val index: Short = 0x1f
            val triplet: ShortArray = shortArrayOf(
                204, 204, 255
            )
            const val hexString: String = "CCCC:CCCC:FFFF"
        }
    }

    /**
     * Special Default/Normal/Automatic color.
     * 
     * *Note:* This class is NOT in the default HashTables returned by HSSFColor.
     * The index is a special case which is interpreted in the various setXXXColor calls.
     * 
     * @author Jason
     */
    class AUTOMATIC : HSSFColor() {
        override val index: Short
            get() = Companion.index

        override val triplet: ShortArray
            get() = BLACK.triplet

        override val hexString: String
            get() = BLACK.hexString

        companion object {
            val instance: HSSFColor = AUTOMATIC()

            const val index: Short = 0x40
        }
    }

    companion object {
        @JvmStatic
        var indexHash: MutableMap<Int?, HSSFColor?>? = null
            /**
             * This function returns all the colours in an unmodifiable Map.
             * The map is cached on first use.
             * 
             * @return a Map containing all colours keyed by <tt>Integer</tt> excel-style palette indexes
             */
            get() {
                if (field == null) {
                    field =
                        Collections.unmodifiableMap<Int?, HSSFColor?>(createColorsByIndexMap())
                }

                return field
            }
            private set

        val mutableIndexHash: Hashtable<Int?, HSSFColor?>
            /**
             * This function returns all the Colours, stored in a Hashtable that
             * can be edited. No caching is performed. If you don't need to edit
             * the table, then call [.getIndexHash] which returns a
             * statically cached imuatable map of colours.
             */
            get() = createColorsByIndexMap()

        private fun createColorsByIndexMap(): Hashtable<Int?, HSSFColor?> {
            val colors: Array<HSSFColor> = allColors
            val result = Hashtable<Int?, HSSFColor?>(colors.size * 3 / 2)

            for (i in colors.indices) {
                val color = colors[i]

                val index1 = color.index.toInt()
                if (result.containsKey(index1)) {
                    val prevColor = result.get(index1)
                    throw RuntimeException(
                        ("Dup color index (" + index1
                                + ") for colors (" + prevColor!!.javaClass.getName()
                                + "),(" + color.javaClass.getName() + ")")
                    )
                }
                result.put(index1, color)
            }

            for (i in colors.indices) {
                val color = colors[i]
                val index2: Int? = getIndex2(color)
                if (index2 == null) {
                    // most colors don't have a second index
                    continue
                }
                if (result.containsKey(index2)) {
                    if (false) { // Many of the second indexes clash
                        val prevColor = result.get(index2)
                        throw RuntimeException(
                            ("Dup color index (" + index2
                                    + ") for colors (" + prevColor!!.javaClass.getName()
                                    + "),(" + color.javaClass.getName() + ")")
                        )
                    }
                }
                result.put(index2, color)
            }
            return result
        }

        private fun getIndex2(color: HSSFColor): Int? {
            val f: Field?
            try {
                f = color.javaClass.getDeclaredField("index2")
            } catch (e: NoSuchFieldException) {
                // can happen because not all colors have a second index
                return null
            }

            val s: Short?
            try {
                s = f.get(color) as Short?
            } catch (e: IllegalArgumentException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
            return s!!.toInt()
        }

        private val allColors: Array<HSSFColor>
            get() = arrayOf<HSSFColor>(
                BLACK(), BROWN(), OLIVE_GREEN(), DARK_GREEN(),
                DARK_TEAL(), DARK_BLUE(), INDIGO(), GREY_80_PERCENT(),
                ORANGE(), DARK_YELLOW(), GREEN(), TEAL(), BLUE(),
                BLUE_GREY(), GREY_50_PERCENT(), RED(), LIGHT_ORANGE(), LIME(),
                SEA_GREEN(), AQUA(), LIGHT_BLUE(), VIOLET(), GREY_40_PERCENT(),
                PINK(), GOLD(), YELLOW(), BRIGHT_GREEN(), TURQUOISE(),
                DARK_RED(), SKY_BLUE(), PLUM(), GREY_25_PERCENT(), ROSE(),
                LIGHT_YELLOW(), LIGHT_GREEN(), LIGHT_TURQUOISE(), PALE_BLUE(),
                LAVENDER(), WHITE(), CORNFLOWER_BLUE(), LEMON_CHIFFON(),
                MAROON(), ORCHID(), CORAL(), ROYAL_BLUE(),
                LIGHT_CORNFLOWER_BLUE(), TAN(),
            )

        val tripletHash: Hashtable<String?, HSSFColor?>
            /**
             * this function returns all colors in a hastable.  Its not implemented as a
             * static member/staticly initialized because that would be dirty in a
             * server environment as it is intended.  This means you'll eat the time
             * it takes to create it once per request but you will not hold onto it
             * if you have none of those requests.
             * 
             * @return a hashtable containing all colors keyed by String gnumeric-like triplets
             */
            get() = createColorsByHexStringMap()

        private fun createColorsByHexStringMap(): Hashtable<String?, HSSFColor?> {
            val colors: Array<HSSFColor> = allColors
            val result = Hashtable<String?, HSSFColor?>(colors.size * 3 / 2)

            for (i in colors.indices) {
                val color = colors[i]

                val hexString = color.hexString
                if (result.containsKey(hexString)) {
                    val other = result.get(hexString)
                    throw RuntimeException(
                        ("Dup color hexString (" + hexString
                                + ") for color (" + color.javaClass.getName() + ") - "
                                + " already taken by (" + other!!.javaClass.getName() + ")")
                    )
                }
                result.put(hexString, color)
            }
            return result
        }
    }
}

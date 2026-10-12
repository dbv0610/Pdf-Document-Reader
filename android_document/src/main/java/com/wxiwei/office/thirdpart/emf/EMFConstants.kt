// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf

/**
 * EMF Constants
 * 
 * @author Mark Donszelmann
 * @version $Id: EMFConstants.java 10363 2007-01-20 15:30:50Z duns $
 */
interface EMFConstants {
    companion object {
        const val UNITS_PER_PIXEL: Int = 1

        const val TWIPS: Int = 20

        const val GRADIENT_FILL_RECT_H: Int = 0x00000000

        const val GRADIENT_FILL_RECT_V: Int = 0x00000001

        const val GRADIENT_FILL_TRIANGLE: Int = 0x00000002

        const val SRCCOPY: Int = 0x00CC0020

        const val ICM_OFF: Int = 1

        const val ICM_ON: Int = 2

        const val ICM_QUERY: Int = 3

        const val ICM_DONE_OUTSIDEDC: Int = 4

        const val FW_DONTCARE: Int = 0

        const val FW_THIN: Int = 100

        const val FW_EXTRALIGHT: Int = 200

        const val FW_LIGHT: Int = 300

        const val FW_NORMAL: Int = 400

        const val FW_MEDIUM: Int = 500

        const val FW_SEMIBOLD: Int = 600

        const val FW_BOLD: Int = 700

        const val FW_EXTRABOLD: Int = 800

        const val FW_HEAVY: Int = 900

        const val PAN_ANY: Int = 0

        const val PAN_NO_FIT: Int = 1

        const val ETO_OPAQUE: Int = 0x0002

        const val ETO_CLIPPED: Int = 0x0004

        const val ETO_GLYPH_INDEX: Int = 0x0010

        const val ETO_RTLREADING: Int = 0x0080

        const val ETO_NUMERICSLOCAL: Int = 0x0400

        const val ETO_NUMERICSLATIN: Int = 0x0800

        const val ETO_IGNORELANGUAGE: Int = 0x1000

        const val ETO_PDY: Int = 0x2000

        const val GM_COMPATIBLE: Int = 1

        const val GM_ADVANCED: Int = 2

        const val FLOODFILLBORDER: Int = 0

        const val FLOODFILLSURFACE: Int = 1

        const val BLACKONWHITE: Int = 1

        const val WHITEONBLACK: Int = 2

        const val COLORONCOLOR: Int = 3

        const val HALFTONE: Int = 4

        val STRETCH_ANDSCANS: Int = BLACKONWHITE

        val STRETCH_ORSCANS: Int = WHITEONBLACK

        val STRETCH_DELETESCANS: Int = COLORONCOLOR

        val STRETCH_HALFTONE: Int = HALFTONE

        const val R2_BLACK: Int = 1

        const val R2_NOTMERGEPEN: Int = 2

        const val R2_MASKNOTPEN: Int = 3

        const val R2_NOTCOPYPEN: Int = 4

        const val R2_MASKPENNOT: Int = 5

        const val R2_NOT: Int = 6

        const val R2_XORPEN: Int = 7

        const val R2_NOTMASKPEN: Int = 8

        const val R2_MASKPEN: Int = 9

        const val R2_NOTXORPEN: Int = 10

        const val R2_NOP: Int = 11

        const val R2_MERGENOTPEN: Int = 12

        const val R2_COPYPEN: Int = 13

        const val R2_MERGEPENNOT: Int = 14

        const val R2_MERGEPEN: Int = 15

        const val R2_WHITE: Int = 16

        const val ALTERNATE: Int = 1

        const val WINDING: Int = 2

        const val TA_BASELINE: Int = 24

        const val TA_BOTTOM: Int = 8

        const val TA_TOP: Int = 0

        const val TA_CENTER: Int = 6

        const val TA_LEFT: Int = 0

        const val TA_RIGHT: Int = 2

        const val TA_NOUPDATECP: Int = 0

        const val TA_RTLREADING: Int = 256

        const val TA_UPDATECP: Int = 1

        const val MM_TEXT: Int = 1

        const val MM_LOMETRIC: Int = 2

        const val MM_HIMETRIC: Int = 3

        const val MM_LOENGLISH: Int = 4

        const val MM_HIENGLISH: Int = 5

        const val MM_TWIPS: Int = 6

        const val MM_ISOTROPIC: Int = 7

        const val MM_ANISOTROPIC: Int = 8

        const val AD_COUNTERCLOCKWISE: Int = 1

        const val AD_CLOCKWISE: Int = 2

        const val RGN_AND: Int = 1

        const val RGN_OR: Int = 2

        const val RGN_XOR: Int = 3

        const val RGN_DIFF: Int = 4

        const val RGN_COPY: Int = 5

        val RGN_MIN: Int = RGN_AND

        val RGN_MAX: Int = RGN_COPY

        const val BKG_TRANSPARENT: Int = 1

        const val BKG_OPAQUE: Int = 2

        const val PT_CLOSEFIGURE: Int = 0x01

        const val PT_LINETO: Int = 0x02

        const val PT_BEZIERTO: Int = 0x04

        const val PT_MOVETO: Int = 0x06

        const val MWT_IDENTITY: Int = 1

        const val MWT_LEFTMULTIPLY: Int = 2

        const val MWT_RIGHTMULTIPLY: Int = 3

        const val BI_RGB: Int = 0

        const val BI_RLE8: Int = 1

        const val BI_RLE4: Int = 2

        const val BI_BITFIELDS: Int = 3

        const val BI_JPEG: Int = 4

        const val BI_PNG: Int = 5

        const val BS_SOLID: Int = 0

        const val BS_NULL: Int = 1

        const val BS_HATCHED: Int = 2

        const val BS_PATTERN: Int = 3

        const val BS_INDEXED: Int = 4

        const val BS_DIBPATTERN: Int = 5

        const val BS_DIBPATTERNPT: Int = 6

        const val BS_PATTERN8X8: Int = 7

        const val BS_DIBPATTERN8X8: Int = 8

        const val BS_MONOPATTERN: Int = 9

        val BS_HOLLOW: Int = BS_NULL

        const val DIB_RGB_COLORS: Int = 0

        const val DIB_PAL_COLORS: Int = 1

        const val HS_HORIZONTAL: Int = 0 /* ----- */

        const val HS_VERTICAL: Int = 1 /* ||||| */

        const val HS_FDIAGONAL: Int = 2 /* \\\\\ */

        const val HS_BDIAGONAL: Int = 3 /* ///// */

        const val HS_CROSS: Int = 4 /* +++++ */

        const val HS_DIAGCROSS: Int = 5 /* xxxxx */

        const val PS_GEOMETRIC: Int = 0x00010000

        const val PS_COSMETIC: Int = 0x00000000

        const val PS_SOLID: Int = 0x00000000

        const val PS_DASH: Int = 0x00000001

        const val PS_DOT: Int = 0x00000002

        const val PS_DASHDOT: Int = 0x00000003

        const val PS_DASHDOTDOT: Int = 0x00000004

        const val PS_NULL: Int = 0x00000005

        const val PS_INSIDEFRAME: Int = 0x00000006

        const val PS_USERSTYLE: Int = 0x00000007

        const val PS_ENDCAP_ROUND: Int = 0x00000000

        const val PS_ENDCAP_SQUARE: Int = 0x00000100

        const val PS_ENDCAP_FLAT: Int = 0x00000200

        const val PS_JOIN_ROUND: Int = 0x00000000

        const val PS_JOIN_BEVEL: Int = 0x00001000

        const val PS_JOIN_MITER: Int = 0x00002000

        const val AC_SRC_OVER: Int = 0x00

        const val AC_SRC_ALPHA: Int = 0x01

        const val GDICOMMENT_BEGINGROUP: Int = 0x00000002

        const val GDICOMMENT_ENDGROUP: Int = 0x00000003

        const val GDICOMMENT_UNICODE_STRING: Int = 0x00000040

        const val GDICOMMENT_UNICODE_END: Int = 0x00000080

        const val GDICOMMENT_MULTIFORMATS: Int = 0x40000004

        const val GDICOMMENT_IDENTIFIER: Int = 0x43494447

        const val GDICOMMENT_WINDOWS_METAFILE: Int = -0x7fffffff
    }
}

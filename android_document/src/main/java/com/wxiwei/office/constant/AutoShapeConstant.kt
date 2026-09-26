/*
 * 文件名称:           AutoShapeConstant.java
 *
 * 编译器:             android2.2
 * 时间:               下午4:58:58
 */
package com.wxiwei.office.constant

object AutoShapeConstant {
    const val LINEWIDTH_ONE_PT = 12700
    const val LINEWIDTH_DEFAULT = 9525
    const val LINESTYLE_SOLID = 0              // Solid (continuous) pen
    const val LINESTYLE_DASHSYS = 1            // PS_DASH system   dash style
    const val LINESTYLE_DOTSYS = 2             // PS_DOT system   dash style
    const val LINESTYLE_DASHDOTSYS = 3         // PS_DASHDOT system dash style
    const val LINESTYLE_DASHDOTDOTSYS = 4      // PS_DASHDOTDOT system dash style
    const val LINESTYLE_DOTGEL = 5             // square dot style
    const val LINESTYLE_DASHGEL = 6            // dash style
    const val LINESTYLE_LONGDASHGEL = 7        // long dash style
    const val LINESTYLE_DASHDOTGEL = 8         // dash short dash
    const val LINESTYLE_LONGDASHDOTGEL = 9     // long dash short dash
    const val LINESTYLE_LONGDASHDOTDOTGEL = 10 // long dash short dash short dash
    const val LINESTYLE_NONE = -1

    /** Solid (continuous) pen */
    const val PEN_SOLID = 1
    /** PS_DASH system dash style */
    const val PEN_PS_DASH = 2
    /** PS_DOT system dash style */
    const val PEN_DOT = 3
    /** PS_DASHDOT system dash style */
    const val PEN_DASHDOT = 4
    /** PS_DASHDOTDOT system dash style */
    const val PEN_DASHDOTDOT = 5
    /** square dot style */
    const val PEN_DOTGEL = 6
    /** dash style */
    const val PEN_DASH = 7
    /** long dash style */
    const val PEN_LONGDASHGEL = 8
    /** dash short dash */
    const val PEN_DASHDOTGEL = 9
    /** long dash short dash */
    const val PEN_LONGDASHDOTGEL = 10
    /** long dash short dash short dash */
    const val PEN_LONGDASHDOTDOTGEL = 11

    /** Single line (of width lineWidth) */
    const val LINE_SIMPLE = 0
    /** Double lines of equal width */
    const val LINE_DOUBLE = 1
    /** Double lines, one thick, one thin */
    const val LINE_THICKTHIN = 2
    /** Double lines, reverse order */
    const val LINE_THINTHICK = 3
    /** Three lines, thin, thick, thin */
    const val LINE_TRIPLE = 4
}

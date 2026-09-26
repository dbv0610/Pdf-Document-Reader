// Copyright 2001-2006, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import com.wxiwei.office.java.awt.geom.Path2D
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.GeneralPath
import java.io.IOException

/**
 * GLYPH Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFGlyfTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFGlyfTable : TTFVersionTable() {
    abstract inner class Glyph {
        var xMin: Int = 0
        var yMin: Int = 0
        var xMax: Int = 0
        var yMax: Int = 0

        abstract fun getType(): String

        abstract fun getShape(): GeneralPath?

        @Throws(IOException::class)
        open fun read() {
            xMin = ttf.readFWord().toInt()
            yMin = ttf.readFWord().toInt()
            xMax = ttf.readFWord().toInt()
            yMax = ttf.readFWord().toInt()
        }

        val bBox: Rectangle
            get() = Rectangle(xMin, yMin, xMax - xMin, yMax - yMin)

        override fun toString(): String {
            return "[" + getType() + "] (" + xMin + "," + yMin + "):(" + xMax + "," + yMax + ")"
        }

        open fun toDetailedString(): String {
            return toString()
        }
    }

    // --------------------------------------------------------------------------------
    inner class SimpleGlyph(var numberOfContours: Int) : Glyph() {
        var endPtsOfContours: IntArray

        lateinit var instructions: IntArray

        lateinit var flags: IntArray

        lateinit var xCoordinates: IntArray
        lateinit var yCoordinates: IntArray

        lateinit var onCurve: BooleanArray

        private var shape: GeneralPath? = null

        init {
            this.endPtsOfContours = IntArray(numberOfContours)
        }

        override fun getType(): String {
            return "Simple Glyph"
        }

        @Throws(IOException::class)
        override fun read() {
            super.read()

            for (i in endPtsOfContours.indices) endPtsOfContours[i] = ttf.readUShort()

            instructions = IntArray(ttf.readUShort())
            for (i in instructions.indices) instructions[i] = ttf.readByte()

            val numberOfPoints = endPtsOfContours[endPtsOfContours.size - 1] + 1
            flags = IntArray(numberOfPoints)
            xCoordinates = IntArray(numberOfPoints)
            yCoordinates = IntArray(numberOfPoints)
            onCurve = BooleanArray(numberOfPoints)
            var repeatCount = 0
            var repeatFlag = 0
            for (i in 0..<numberOfPoints) {
                if (repeatCount > 0) {
                    flags[i] = repeatFlag
                    repeatCount--
                } else {
                    flags[i] = ttf.readRawByte()
                    if (TTFInput.Companion.flagBit(flags[i], REPEAT_FLAG)) {
                        repeatCount = ttf.readByte()
                        repeatFlag = flags[i]
                    }
                }
                TTFInput.Companion.checkZeroBit(flags[i], 6, "flags")
                TTFInput.Companion.checkZeroBit(flags[i], 7, "flags")
                onCurve[i] = TTFInput.Companion.flagBit(flags[i], ON_CURVE)
            }

            var last = 0
            for (i in 0..<numberOfPoints) {
                if (TTFInput.Companion.flagBit(flags[i], X_SHORT)) {
                    if (TTFInput.Companion.flagBit(flags[i], X_POSITIVE)) {
                        xCoordinates[i] = last + ttf.readByte()
                        last = xCoordinates[i]
                    } else {
                        xCoordinates[i] = last - ttf.readByte()
                        last = xCoordinates[i]
                    }
                } else {
                    if (TTFInput.Companion.flagBit(flags[i], X_SAME)) {
                        xCoordinates[i] = last
                        last = xCoordinates[i]
                    } else {
                        xCoordinates[i] = last + ttf.readShort()
                        last = xCoordinates[i]
                    }
                }
            }

            last = 0
            for (i in 0..<numberOfPoints) {
                if (TTFInput.Companion.flagBit(flags[i], Y_SHORT)) {
                    if (TTFInput.Companion.flagBit(flags[i], Y_POSITIVE)) {
                        yCoordinates[i] = last + ttf.readByte()
                        last = yCoordinates[i]
                    } else {
                        yCoordinates[i] = last - ttf.readByte()
                        last = yCoordinates[i]
                    }
                } else {
                    if (TTFInput.Companion.flagBit(flags[i], Y_SAME)) {
                        yCoordinates[i] = last
                        last = yCoordinates[i]
                    } else {
                        yCoordinates[i] = last + ttf.readShort()
                        last = yCoordinates[i]
                    }
                }
            }
        }

        override fun toString(): String {
            var str = super.toString() + ", " + numberOfContours + " contours, endPts={"
            for (i in 0..<numberOfContours) str += (if (i == 0) "" else ",") + endPtsOfContours[i]
            str += "}, " + instructions.size + " instructions"
            return str
        }

        override fun toDetailedString(): String {
            var str = toString() + "\n  instructions = {"
            for (i in instructions.indices) {
                str += Integer.toHexString(instructions[i]) + " "
            }
            return str + "}"
        }

        override fun getShape(): GeneralPath? {
            if (shape != null) {
                return shape
            }

            shape = GeneralPath(Path2D.WIND_NON_ZERO)
            var p = 0
            for (i in endPtsOfContours.indices) {
                val startIndex = p++
                shape!!.moveTo(
                    xCoordinates[startIndex].toFloat(),
                    yCoordinates[startIndex].toFloat()
                )
                var lastOnCurve = true
                while (p <= endPtsOfContours[i]) {
                    if (onCurve[p]) {
                        if (lastOnCurve) {
                            shape!!.lineTo(xCoordinates[p].toFloat(), yCoordinates[p].toFloat())
                        } else {
                            shape!!.quadTo(
                                xCoordinates[p - 1].toFloat(),
                                yCoordinates[p - 1].toFloat(),
                                xCoordinates[p].toFloat(),
                                yCoordinates[p].toFloat()
                            )
                        }
                        lastOnCurve = true
                    } else {
                        if (!lastOnCurve) {
                            val x1 = xCoordinates[p - 1]
                            val y1 = yCoordinates[p - 1]
                            val x2 = ((x1 + xCoordinates[p]) / 2.0).toInt()
                            val y2 = ((y1 + yCoordinates[p]) / 2.0).toInt()
                            shape!!.quadTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
                        }
                        lastOnCurve = false
                    }
                    p++
                }
                if (!onCurve[p - 1]) {
                    shape!!.quadTo(
                        xCoordinates[p - 1].toFloat(), yCoordinates[p - 1].toFloat(),
                        xCoordinates[startIndex].toFloat(), yCoordinates[startIndex].toFloat()
                    )
                } else if ((xCoordinates[p - 1] != xCoordinates[startIndex])
                    || (yCoordinates[p - 1] != yCoordinates[startIndex])
                ) {
                    shape!!.closePath()
                }
            }
            return shape
        }

    }

    // --------------------------------------------------------------------------------
    inner class CompositeGlyph : Glyph() {
        private var shape: GeneralPath? = null

        private var noComponents = 0

        override fun getType(): String {
            return "Composite Glyph"
        }

        override fun getShape(): GeneralPath {
            return shape!!
        }

        @Throws(IOException::class)
        override fun read() {
            super.read()
            shape = GeneralPath()

            noComponents = 0
            var more = true
            while (more) {
                noComponents++
                ttf.readUShortFlags()
                more = ttf.flagBit(MORE_COMPONENTS)
                val glyphIndex = ttf.readUShort()
                val arg1: Int
                val arg2: Int
                if (ttf.flagBit(ARGS_WORDS)) {
                    arg1 = ttf.readShort().toInt()
                    arg2 = ttf.readShort().toInt()
                } else {
                    arg1 = ttf.readChar().toInt()
                    arg2 = ttf.readChar().toInt()
                }
                val t = AffineTransform()
                if (ttf.flagBit(ARGS_XY)) {
                    t.translate(arg1.toDouble(), arg2.toDouble())
                } else {
                    System.err.println("TTFGlyfTable: ARGS_ARE_POINTS not implemented.")
                }

                if (ttf.flagBit(SCALE)) {
                    val scale = ttf.readF2Dot14()
                    t.scale(scale, scale)
                } else if (ttf.flagBit(XY_SCALE)) {
                    val scaleX = ttf.readF2Dot14()
                    val scaleY = ttf.readF2Dot14()
                    t.scale(scaleX, scaleY)
                } else if (ttf.flagBit(TWO_BY_TWO)) {
                    System.err.println("TTFGlyfTable: WE_HAVE_A_TWO_BY_TWO not implemented.")
                }

                val appendGlyph = GeneralPath(getGlyph(glyphIndex)!!.getShape()!!)
                appendGlyph.transform(t)
                shape!!.append(appendGlyph, false)
            }
        }

        override fun toString(): String {
            return super.toString() + ", " + noComponents + " components"
        }

    }

    // --------------------------------------------------------------------------------
    lateinit var glyphs: Array<Glyph?>

    private lateinit var offsets: LongArray

    override fun getTag(): String {
        return "glyf"
    }

    @Throws(IOException::class)
    override fun readTable() {
        glyphs = arrayOfNulls<Glyph>((getTable("maxp") as TTFMaxPTable).numGlyphs)
        offsets = (getTable("loca") as TTFLocaTable).offset!!

        if (READ_GLYPHS) {
            for (i in glyphs.indices) {
                if ((i > 0) && (offsets[i - 1] == offsets[i])) {
                    glyphs[i] = glyphs[i - 1]
                } else {
                    try {
                        getGlyph(i)
                    } catch (e: IOException) {
                        System.err.println(
                            ("While reading glyph #" + i + " (offset " + offsets[i]
                                    + "):")
                        )
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getGlyph(i: Int): Glyph? {
        if (glyphs[i] != null) {
            return glyphs[i]
        } else {
            ttf.pushPos()
            ttf.seek(offsets[i])
            val numberOfContours = ttf.readShort().toInt()
            if (numberOfContours >= 0) glyphs[i] = SimpleGlyph(numberOfContours)
            else glyphs[i] = CompositeGlyph()
            glyphs[i]!!.read()
            // System.out.println(i+": "+offsets[i]+"-"+ttf.getPointer());
            ttf.popPos()
            return glyphs[i]
        }
    }

    override fun toString(): String {
        var str = super.toString()
        for (i in glyphs.indices) str += "\n  #" + i + ": " + glyphs[i]
        return str
    }

    companion object {
        private const val ON_CURVE = 0
        private const val X_SHORT = 1
        private const val Y_SHORT = 2
        private const val REPEAT_FLAG = 3
        private const val X_SAME = 4
        private const val Y_SAME = 5
        private const val X_POSITIVE = 4
        private const val Y_POSITIVE = 5
        private const val ARGS_WORDS = 0
        private const val ARGS_XY = 1
        private const val SCALE = 3
        private const val XY_SCALE = 6
        private const val TWO_BY_TWO = 7
        private const val MORE_COMPONENTS = 5
        /**
         * If this variable is set to false then the glyphs will not be read until
         * they are retrieved with <tt>getGlyph(int)</tt>.
         */
        private const val READ_GLYPHS = false
    }
}

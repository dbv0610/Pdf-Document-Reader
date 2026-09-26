/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt

/**
 * The `Color` class is used to encapsulate colors in the default
 * sRGB color space.
 */
open class Color : java.io.Serializable {

    /**
     * Private data.
     */
    @Transient
    private var pData: Long = 0

    /**
     * The color value.
     */
    @JvmField
    internal var value = 0

    /**
     * The color value in the default sRGB `ColorSpace` as
     * `float` components (no alpha).
     */
    private var frgbvalue: FloatArray? = null

    /**
     * The color value in the native `ColorSpace` as
     * `float` components (no alpha).
     */
    private var fvalue: FloatArray? = null

    /**
     * The alpha value as a `float` component.
     */
    private var falpha = 0.0f

    /**
     * Creates an opaque sRGB color with the specified red, green,
     * and blue values in the range (0 - 255).
     */
    constructor(r: Int, g: Int, b: Int) : this(r, g, b, 255)

    /**
     * Creates an sRGB color with the specified red, green, blue, and alpha
     * values in the range (0 - 255).
     */
    constructor(r: Int, g: Int, b: Int, a: Int) {
        value = ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or ((b and 0xFF) shl 0)
        testColorValueRange(r, g, b, a)
    }

    /**
     * Creates an opaque sRGB color with the specified combined RGB value
     * consisting of the red component in bits 16-23, the green component
     * in bits 8-15, and the blue component in bits 0-7.
     */
    constructor(rgb: Int) {
        value = 0xff000000.toInt() or rgb
    }

    /**
     * Creates an sRGB color with the specified combined RGBA value.
     */
    constructor(rgba: Int, hasalpha: Boolean) {
        if (hasalpha) {
            value = rgba
        } else {
            value = 0xff000000.toInt() or rgba
        }
    }

    constructor(rgb: Int, a: Int) {
        value = ((a and 0xFF) shl 24) or (rgb and 0xFFFFFF)
    }

    /**
     * Creates an opaque sRGB color with the specified red, green, and blue
     * values in the range (0.0 - 1.0).
     */
    constructor(r: Float, g: Float, b: Float) : this((r * 255 + 0.5).toInt(), (g * 255 + 0.5).toInt(), (b * 255 + 0.5).toInt()) {
        testColorValueRange(r, g, b, 1.0f)
        val rgb = FloatArray(3)
        rgb[0] = r
        rgb[1] = g
        rgb[2] = b
        frgbvalue = rgb
        falpha = 1.0f
        fvalue = frgbvalue
    }

    /**
     * Creates an sRGB color with the specified red, green, blue, and
     * alpha values in the range (0.0 - 1.0).
     */
    constructor(r: Float, g: Float, b: Float, a: Float) : this((r * 255 + 0.5).toInt(), (g * 255 + 0.5).toInt(), (b * 255 + 0.5).toInt(), (a * 255 + 0.5).toInt()) {
        val rgb = FloatArray(3)
        rgb[0] = r
        rgb[1] = g
        rgb[2] = b
        frgbvalue = rgb
        falpha = a
        fvalue = frgbvalue
    }

    /**
     * Returns the red component in the range 0-255 in the default sRGB
     * space.
     */
    open fun getRed(): Int {
        return (getRGB() shr 16) and 0xFF
    }

    /**
     * Returns the green component in the range 0-255 in the default sRGB
     * space.
     */
    open fun getGreen(): Int {
        return (getRGB() shr 8) and 0xFF
    }

    /**
     * Returns the blue component in the range 0-255 in the default sRGB
     * space.
     */
    open fun getBlue(): Int {
        return (getRGB() shr 0) and 0xFF
    }

    /**
     * Returns the alpha component in the range 0-255.
     */
    open fun getAlpha(): Int {
        return (getRGB() shr 24) and 0xff
    }

    /**
     * Returns the RGB value representing the color in the default sRGB
     * ColorModel.
     */
    open fun getRGB(): Int {
        return value
    }

    /**
     * Creates a new `Color` that is a brighter version of this
     * `Color`.
     */
    open fun brighter(): Color {
        var r = getRed()
        var g = getGreen()
        var b = getBlue()

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        val i = (1.0 / (1.0 - FACTOR)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return Color(i, i, i)
        }
        if (r > 0 && r < i)
            r = i
        if (g > 0 && g < i)
            g = i
        if (b > 0 && b < i)
            b = i

        return Color(Math.min((r / FACTOR).toInt(), 255), Math.min((g / FACTOR).toInt(), 255),
            Math.min((b / FACTOR).toInt(), 255))
    }

    /**
     * Creates a new `Color` that is a darker version of this
     * `Color`.
     */
    open fun darker(): Color {
        return Color(Math.max((getRed() * FACTOR).toInt(), 0), Math.max(
            (getGreen() * FACTOR).toInt(), 0), Math.max((getBlue() * FACTOR).toInt(), 0))
    }

    /**
     * Computes the hash code for this `Color`.
     */
    override fun hashCode(): Int {
        return value
    }

    /**
     * Determines whether another object is equal to this
     * `Color`.
     */
    override fun equals(other: Any?): Boolean {
        return other is Color && other.getRGB() == this.getRGB()
    }

    /**
     * Returns a string representation of this `Color`.
     */
    override fun toString(): String {
        return (javaClass.name + "[r=" + getRed() + ",g=" + getGreen() + ",b=" + getBlue()
                + "]")
    }

    /**
     * Returns a `float` array containing the color and alpha
     * components of the `Color`, as represented in the default
     * sRGB color space.
     */
    open fun getRGBComponents(compArray: FloatArray?): FloatArray {
        val f: FloatArray
        if (compArray == null) {
            f = FloatArray(4)
        } else {
            f = compArray
        }
        val rgb = frgbvalue
        if (rgb == null) {
            f[0] = getRed().toFloat() / 255f
            f[1] = getGreen().toFloat() / 255f
            f[2] = getBlue().toFloat() / 255f
            f[3] = getAlpha().toFloat() / 255f
        } else {
            f[0] = rgb[0]
            f[1] = rgb[1]
            f[2] = rgb[2]
            f[3] = falpha
        }
        return f
    }

    /**
     * Returns a `float` array containing only the color
     * components of the `Color`, in the default sRGB color
     * space.
     */
    open fun getRGBColorComponents(compArray: FloatArray?): FloatArray {
        val f: FloatArray
        if (compArray == null) {
            f = FloatArray(3)
        } else {
            f = compArray
        }
        val rgb = frgbvalue
        if (rgb == null) {
            f[0] = getRed().toFloat() / 255f
            f[1] = getGreen().toFloat() / 255f
            f[2] = getBlue().toFloat() / 255f
        } else {
            f[0] = rgb[0]
            f[1] = rgb[1]
            f[2] = rgb[2]
        }
        return f
    }

    /**
     * Returns a `float` array containing the color and alpha
     * components of the `Color`, in the `ColorSpace` of the
     * `Color`.
     */
    open fun getComponents(compArray: FloatArray?): FloatArray {
        val fv = fvalue ?: return getRGBComponents(compArray)
        val f: FloatArray
        val n = fv.size
        if (compArray == null) {
            f = FloatArray(n + 1)
        } else {
            f = compArray
        }
        for (i in 0 until n) {
            f[i] = fv[i]
        }
        f[n] = falpha
        return f
    }

    /**
     * Returns a `float` array containing only the color
     * components of the `Color`, in the `ColorSpace` of the
     * `Color`.
     */
    open fun getColorComponents(compArray: FloatArray?): FloatArray {
        val fv = fvalue ?: return getRGBColorComponents(compArray)
        val f: FloatArray
        val n = fv.size
        if (compArray == null) {
            f = FloatArray(n)
        } else {
            f = compArray
        }
        for (i in 0 until n) {
            f[i] = fv[i]
        }
        return f
    }

    companion object {
        /**
         * The color white.  In the default sRGB space.
         */
        @JvmField
        val white = Color(255, 255, 255)

        /**
         * The color white.  In the default sRGB space.
         */
        @JvmField
        val WHITE = white

        /**
         * The color light gray.  In the default sRGB space.
         */
        @JvmField
        val lightGray = Color(192, 192, 192)

        /**
         * The color light gray.  In the default sRGB space.
         */
        @JvmField
        val LIGHT_GRAY = lightGray

        /**
         * The color gray.  In the default sRGB space.
         */
        @JvmField
        val gray = Color(128, 128, 128)

        /**
         * The color gray.  In the default sRGB space.
         */
        @JvmField
        val GRAY = gray

        /**
         * The color dark gray.  In the default sRGB space.
         */
        @JvmField
        val darkGray = Color(64, 64, 64)

        /**
         * The color dark gray.  In the default sRGB space.
         */
        @JvmField
        val DARK_GRAY = darkGray

        /**
         * The color black.  In the default sRGB space.
         */
        @JvmField
        val black = Color(0, 0, 0)

        /**
         * The color black.  In the default sRGB space.
         */
        @JvmField
        val BLACK = black

        /**
         * The color red.  In the default sRGB space.
         */
        @JvmField
        val red = Color(255, 0, 0)

        /**
         * The color red.  In the default sRGB space.
         */
        @JvmField
        val RED = red

        /**
         * The color pink.  In the default sRGB space.
         */
        @JvmField
        val pink = Color(255, 175, 175)

        /**
         * The color pink.  In the default sRGB space.
         */
        @JvmField
        val PINK = pink

        /**
         * The color orange.  In the default sRGB space.
         */
        @JvmField
        val orange = Color(255, 200, 0)

        /**
         * The color orange.  In the default sRGB space.
         */
        @JvmField
        val ORANGE = orange

        /**
         * The color yellow.  In the default sRGB space.
         */
        @JvmField
        val yellow = Color(255, 255, 0)

        /**
         * The color yellow.  In the default sRGB space.
         */
        @JvmField
        val YELLOW = yellow

        /**
         * The color green.  In the default sRGB space.
         */
        @JvmField
        val green = Color(0, 255, 0)

        /**
         * The color green.  In the default sRGB space.
         */
        @JvmField
        val GREEN = green

        /**
         * The color magenta.  In the default sRGB space.
         */
        @JvmField
        val magenta = Color(255, 0, 255)

        /**
         * The color magenta.  In the default sRGB space.
         */
        @JvmField
        val MAGENTA = magenta

        /**
         * The color cyan.  In the default sRGB space.
         */
        @JvmField
        val cyan = Color(0, 255, 255)

        /**
         * The color cyan.  In the default sRGB space.
         */
        @JvmField
        val CYAN = cyan

        /**
         * The color blue.  In the default sRGB space.
         */
        @JvmField
        val blue = Color(0, 0, 255)

        /**
         * The color blue.  In the default sRGB space.
         */
        @JvmField
        val BLUE = blue

        /*
         * JDK 1.1 serialVersionUID
         */
        private const val serialVersionUID = 118526816881161077L

        // Note: the original declared `private static native void initIDs()` and an
        // empty static initializer; neither was ever used.

        private const val FACTOR = 0.7

        /**
         * Checks the color integer components supplied for validity.
         * Throws an [IllegalArgumentException] if the value is out of
         * range.
         */
        private fun testColorValueRange(r: Int, g: Int, b: Int, a: Int) {
            var rangeError = false
            var badComponentString = ""

            if (a < 0 || a > 255) {
                rangeError = true
                badComponentString = "$badComponentString Alpha"
            }
            if (r < 0 || r > 255) {
                rangeError = true
                badComponentString = "$badComponentString Red"
            }
            if (g < 0 || g > 255) {
                rangeError = true
                badComponentString = "$badComponentString Green"
            }
            if (b < 0 || b > 255) {
                rangeError = true
                badComponentString = "$badComponentString Blue"
            }
            if (rangeError == true) {
                throw IllegalArgumentException("Color parameter outside of expected range:"
                        + badComponentString)
            }
        }

        /**
         * Checks the color `float` components supplied for
         * validity.
         * Throws an `IllegalArgumentException` if the value is out
         * of range.
         */
        private fun testColorValueRange(r: Float, g: Float, b: Float, a: Float) {
            var rangeError = false
            var badComponentString = ""
            if (a < 0.0 || a > 1.0) {
                rangeError = true
                badComponentString = "$badComponentString Alpha"
            }
            if (r < 0.0 || r > 1.0) {
                rangeError = true
                badComponentString = "$badComponentString Red"
            }
            if (g < 0.0 || g > 1.0) {
                rangeError = true
                badComponentString = "$badComponentString Green"
            }
            if (b < 0.0 || b > 1.0) {
                rangeError = true
                badComponentString = "$badComponentString Blue"
            }
            if (rangeError == true) {
                throw IllegalArgumentException("Color parameter outside of expected range:"
                        + badComponentString)
            }
        }

        /**
         * Converts a `String` to an integer and returns the
         * specified opaque `Color`.
         */
        @JvmStatic
        @Throws(NumberFormatException::class)
        fun decode(nm: String): Color {
            val intval = Integer.decode(nm)
            val i = intval.toInt()
            return Color((i shr 16) and 0xFF, (i shr 8) and 0xFF, i and 0xFF)
        }

        /**
         * Finds a color in the system properties.
         */
        @JvmStatic
        fun getColor(nm: String?): Color? {
            return getColor(nm, null)
        }

        /**
         * Finds a color in the system properties.
         */
        @JvmStatic
        fun getColor(nm: String?, v: Color?): Color? {
            val intval = Integer.getInteger(nm) ?: return v
            val i = intval.toInt()
            return Color((i shr 16) and 0xFF, (i shr 8) and 0xFF, i and 0xFF)
        }

        /**
         * Finds a color in the system properties.
         */
        @JvmStatic
        fun getColor(nm: String?, v: Int): Color {
            val intval = Integer.getInteger(nm)
            val i = intval?.toInt() ?: v
            return Color((i shr 16) and 0xFF, (i shr 8) and 0xFF, (i shr 0) and 0xFF)
        }

        /**
         * Converts the components of a color, as specified by the HSB
         * model, to an equivalent set of values for the default RGB model.
         */
        @JvmStatic
        fun HSBtoRGB(hue: Float, saturation: Float, brightness: Float): Int {
            var r = 0
            var g = 0
            var b = 0
            if (saturation == 0f) {
                b = (brightness * 255.0f + 0.5f).toInt()
                g = b
                r = g
            } else {
                val h = (hue - Math.floor(hue.toDouble()).toFloat()) * 6.0f
                val f = h - Math.floor(h.toDouble()).toFloat()
                val p = brightness * (1.0f - saturation)
                val q = brightness * (1.0f - saturation * f)
                val t = brightness * (1.0f - (saturation * (1.0f - f)))
                when (h.toInt()) {
                    0 -> {
                        r = (brightness * 255.0f + 0.5f).toInt()
                        g = (t * 255.0f + 0.5f).toInt()
                        b = (p * 255.0f + 0.5f).toInt()
                    }
                    1 -> {
                        r = (q * 255.0f + 0.5f).toInt()
                        g = (brightness * 255.0f + 0.5f).toInt()
                        b = (p * 255.0f + 0.5f).toInt()
                    }
                    2 -> {
                        r = (p * 255.0f + 0.5f).toInt()
                        g = (brightness * 255.0f + 0.5f).toInt()
                        b = (t * 255.0f + 0.5f).toInt()
                    }
                    3 -> {
                        r = (p * 255.0f + 0.5f).toInt()
                        g = (q * 255.0f + 0.5f).toInt()
                        b = (brightness * 255.0f + 0.5f).toInt()
                    }
                    4 -> {
                        r = (t * 255.0f + 0.5f).toInt()
                        g = (p * 255.0f + 0.5f).toInt()
                        b = (brightness * 255.0f + 0.5f).toInt()
                    }
                    5 -> {
                        r = (brightness * 255.0f + 0.5f).toInt()
                        g = (p * 255.0f + 0.5f).toInt()
                        b = (q * 255.0f + 0.5f).toInt()
                    }
                }
            }
            return 0xff000000.toInt() or (r shl 16) or (g shl 8) or (b shl 0)
        }

        /**
         * Converts the components of a color, as specified by the default RGB
         * model, to an equivalent set of values for hue, saturation, and
         * brightness that are the three components of the HSB model.
         */
        @JvmStatic
        fun RGBtoHSB(r: Int, g: Int, b: Int, hsbvals: FloatArray?): FloatArray {
            var hsbvals = hsbvals
            var hue: Float
            val saturation: Float
            val brightness: Float
            if (hsbvals == null) {
                hsbvals = FloatArray(3)
            }
            var cmax = if (r > g) r else g
            if (b > cmax)
                cmax = b
            var cmin = if (r < g) r else g
            if (b < cmin)
                cmin = b

            brightness = cmax.toFloat() / 255.0f
            if (cmax != 0)
                saturation = (cmax - cmin).toFloat() / cmax.toFloat()
            else
                saturation = 0f
            if (saturation == 0f)
                hue = 0f
            else {
                val redc = (cmax - r).toFloat() / (cmax - cmin).toFloat()
                val greenc = (cmax - g).toFloat() / (cmax - cmin).toFloat()
                val bluec = (cmax - b).toFloat() / (cmax - cmin).toFloat()
                if (r == cmax)
                    hue = bluec - greenc
                else if (g == cmax)
                    hue = 2.0f + redc - bluec
                else
                    hue = 4.0f + greenc - redc
                hue = hue / 6.0f
                if (hue < 0)
                    hue = hue + 1.0f
            }
            hsbvals[0] = hue
            hsbvals[1] = saturation
            hsbvals[2] = brightness
            return hsbvals
        }

        /**
         * Creates a `Color` object based on the specified values
         * for the HSB color model.
         */
        @JvmStatic
        fun getHSBColor(h: Float, s: Float, b: Float): Color {
            return Color(HSBtoRGB(h, s, b))
        }
    }
}

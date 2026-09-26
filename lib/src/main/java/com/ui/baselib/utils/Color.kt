package com.ui.baselib.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.graphics.toColorInt

@ColorInt
val transparent: Int = Color.TRANSPARENT

@ColorInt
fun fromColor(code: String): Int {
    return try {
        val cleanedCode = code.toHexColorOrNull() ?: return Color.TRANSPARENT
        if (cleanedCode.isValidHexColor()) {
            "#$cleanedCode".toColorInt()
        } else {
            Color.TRANSPARENT
        }
    } catch (e: Exception) {
        Color.TRANSPARENT
    }
}
@ColorInt
fun String.toColorOpacity(@IntRange(0, 100) opacity: Int): Int {
    val cleanColor = toHexColorOrNull()?.takeIf { it.isValidHexColor() }
        ?: return Color.TRANSPARENT
    val safeOpacity = opacity.coerceIn(0, 100)
    val alphaHex = (safeOpacity * 255 / 100).toString(16)
        .padStart(2, '0')
        .uppercase()
    val fullHex = when (cleanColor.length) {
        3 -> cleanColor.map { "$it$it" }.joinToString("")
        6 -> cleanColor
        else -> "000000"
    }
    val finalColor = "#$alphaHex$fullHex"
    return finalColor.toColorInt()
}
@ColorInt
fun fromColor(r: Int, g: Int, b: Int, opacity: Int = 100): Int {
    return try {
        val alpha = (opacity.coerceIn(0, 100) * 255 / 100)
        val red = r.coerceIn(0, 255)
        val green = g.coerceIn(0, 255)
        val blue = b.coerceIn(0, 255)
        Color.argb(alpha, red, green, blue)
    } catch (e: Exception) {
        Color.TRANSPARENT
    }
}

fun String.isValidHexColor(): Boolean {
    val hexPattern = "^#?(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})\$".toRegex()
    return trim().matches(hexPattern)
}

private fun String.toHexColorOrNull(): String? {
    val trimmed = trim().removePrefix("#")
    return trimmed.takeIf { '#' !in it && it.matches("^(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$".toRegex()) }
}

@ColorInt
fun Int.opacity(@IntRange(0, 100) opacity: Int = 0): Int {
    val clampedOpacity = opacity.coerceIn(0, 100)
    val alpha = (clampedOpacity * 255) / 100
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

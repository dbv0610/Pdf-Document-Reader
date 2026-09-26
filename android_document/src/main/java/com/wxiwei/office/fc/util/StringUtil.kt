package com.wxiwei.office.fc.util

import java.text.FieldPosition
import java.text.NumberFormat
import java.lang.StringBuffer
import java.util.Iterator

object StringUtil {
    private const val ENCODING_ISO_8859_1 = "ISO-8859-1"
    @JvmStatic fun getFromUnicodeLE(string: ByteArray, offset: Int, len: Int): String { if (offset < 0 || offset >= string.size) throw ArrayIndexOutOfBoundsException("Illegal offset $offset (String data is of length ${string.size})"); if (len < 0 || (string.size - offset) / 2 < len) throw IllegalArgumentException("Illegal length $len"); return String(string, offset, len * 2, Charsets.UTF_16LE) }
    @JvmStatic fun getFromUnicodeLE(string: ByteArray): String = if (string.isEmpty()) "" else getFromUnicodeLE(string, 0, string.size / 2)
    @JvmStatic fun getFromCompressedUnicode(string: ByteArray, offset: Int, len: Int): String = String(string, offset, minOf(len, string.size - offset), Charsets.ISO_8859_1)
    @JvmStatic fun readCompressedUnicode(input: LittleEndianInput, nChars: Int): String { val chars = CharArray(nChars); for (i in chars.indices) chars[i] = input.readUByte().toChar(); return String(chars) }
    @JvmStatic fun readUnicodeString(input: LittleEndianInput): String { val n = input.readUShort(); return if ((input.readByte().toInt() and 1) == 0) readCompressedUnicode(input, n) else readUnicodeLE(input, n) }
    @JvmStatic fun readUnicodeString(input: LittleEndianInput, nChars: Int): String = if ((input.readByte().toInt() and 1) == 0) readCompressedUnicode(input, nChars) else readUnicodeLE(input, nChars)
    @JvmStatic fun writeUnicodeString(out: LittleEndianOutput, value: String) { out.writeShort(value.length); val unicode = hasMultibyte(value); out.writeByte(if (unicode) 1 else 0); if (unicode) putUnicodeLE(value, out) else putCompressedUnicode(value, out) }
    @JvmStatic fun writeUnicodeStringFlagAndData(out: LittleEndianOutput, value: String) { val unicode = hasMultibyte(value); out.writeByte(if (unicode) 1 else 0); if (unicode) putUnicodeLE(value, out) else putCompressedUnicode(value, out) }
    @JvmStatic fun getEncodedSize(value: String): Int = 3 + value.length * if (hasMultibyte(value)) 2 else 1
    @JvmStatic fun putCompressedUnicode(input: String, output: ByteArray, offset: Int) { val bytes = input.toByteArray(Charsets.ISO_8859_1); System.arraycopy(bytes, 0, output, offset, bytes.size) }
    @JvmStatic fun putCompressedUnicode(input: String, out: LittleEndianOutput) = out.write(input.toByteArray(Charsets.ISO_8859_1))
    @JvmStatic fun putUnicodeLE(input: String, output: ByteArray, offset: Int) { val bytes = input.toByteArray(Charsets.UTF_16LE); System.arraycopy(bytes, 0, output, offset, bytes.size) }
    @JvmStatic fun putUnicodeLE(input: String, out: LittleEndianOutput) = out.write(input.toByteArray(Charsets.UTF_16LE))
    @JvmStatic fun readUnicodeLE(input: LittleEndianInput, nChars: Int): String { val chars = CharArray(nChars); for (i in chars.indices) chars[i] = input.readUShort().toChar(); return String(chars) }
    @JvmStatic fun format(message: String?, params: Array<Any?>): String { var p = 0; val out = StringBuffer(); var i = 0; val text = message!!; while (i < text.length) { if (text[i] == '%') { if (p >= params.size) out.append("?missing data?") else if (params[p] is Number && i + 1 < text.length) { i += matchOptionalFormatting(params[p++] as Number, text.substring(i + 1), out) } else out.append(params[p++].toString()) } else if (text[i] == '\\' && i + 1 < text.length && text[i + 1] == '%') { out.append('%'); i++ } else out.append(text[i]); i++ }; return out.toString() }
    private fun matchOptionalFormatting(number: Number, formatting: String, out: StringBuffer): Int { val f = NumberFormat.getInstance(); if (formatting.isNotEmpty() && formatting[0].isDigit()) { f.minimumIntegerDigits = formatting[0].toString().toInt(); if (formatting.length > 2 && formatting[1] == '.' && formatting[2].isDigit()) { f.maximumFractionDigits = formatting[2].toString().toInt(); f.format(number, out, FieldPosition(0)); return 3 }; f.format(number, out, FieldPosition(0)); return 1 }; if (formatting.length > 1 && formatting[0] == '.' && formatting[1].isDigit()) { f.maximumFractionDigits = formatting[1].toString().toInt(); f.format(number, out, FieldPosition(0)); return 2 }; f.format(number, out, FieldPosition(0)); return 1 }
    @JvmStatic fun getPreferredEncoding(): String = ENCODING_ISO_8859_1
    @JvmStatic fun hasMultibyte(value: String?): Boolean = value?.any { it.code > 0xff } == true
    @JvmStatic fun isUnicodeString(value: String): Boolean = value != String(value.toByteArray(Charsets.ISO_8859_1), Charsets.ISO_8859_1)
    class StringsIterator(strings: Array<String>?) : Iterator<String> { private val values = strings ?: emptyArray(); private var position = 0; override fun hasNext() = position < values.size; override fun next(): String { val i = position++; if (i >= values.size) throw ArrayIndexOutOfBoundsException(i); return values[i] }; override fun remove() {} }
}

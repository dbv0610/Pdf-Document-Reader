package com.wxiwei.office.editor.xlsx

/** Shifts relative A1 references, leaving quoted strings and sheet names untouched. */
object A1FormulaShifter {
    private val token = Regex("\"(?:\"\"|[^\"])*\"|'(?:''|[^'])*'|(?<![A-Za-z0-9_.])([$]?)([A-Za-z]{1,3})([$]?)([1-9][0-9]*)(?![A-Za-z0-9_]|\\s*[(])")
    fun shift(formula: String, rows: Int, columns: Int): String = token.replace(formula) { m ->
        if (m.value.startsWith('"') || m.value.startsWith('\'') || formula.getOrNull(m.range.last + 1) == '!') m.value
        else {
            val col = m.groupValues[2].uppercase().fold(0) { n, c -> n * 26 + c.code - 64 } - 1
            val row = m.groupValues[4].toIntOrNull()?.minus(1) ?: return@replace m.value
            if (col >= 16384 || row >= 1048576) return@replace m.value
            val c = col + if (m.groupValues[1].isEmpty()) columns else 0
            val r = row + if (m.groupValues[3].isEmpty()) rows else 0
            if (c !in 0..16383 || r !in 0..1048575) "#REF!"
            else m.groupValues[1] + column(c) + m.groupValues[3] + (r + 1)
        }
    }
    fun column(index: Int): String {
        var n = index + 1
        var result = ""
        while (n > 0) { n--; result = ('A' + n % 26) + result; n /= 26 }
        return result
    }
    fun address(row: Int, col: Int) = column(col) + (row + 1)
}

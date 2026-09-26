package com.wxiwei.office.system.search

import java.text.Normalizer

/** Matching that preserves offsets in the original text. */
object SearchText {
    fun fold(s: String): String {
        val out = CharArray(s.length)
        for (i in s.indices) {
            val c = s[i]
            out[i] = when (c) {
                ' ', '\t', '\u000b', '\u000c', ' ', ' ' -> ' '
                '‘', '’' -> '\''
                '“', '”' -> '"'
                else -> {
                    val lower = Character.toLowerCase(c)
                    // chỉ nhận chữ thường 1-1; trường hợp đặc biệt giữ nguyên
                    lower
                }
            }
        }
        return String(out)
    }

    fun queries(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return listOf(
            fold(Normalizer.normalize(value, Normalizer.Form.NFC)),
            fold(Normalizer.normalize(value, Normalizer.Form.NFD))
        ).distinct()
    }

    fun matchesIn(text: String, queries: List<String>): List<Int> {
        val folded = fold(text)
        val positions = sortedSetOf<Int>()
        for (q in queries) {
            var at = if (q.isEmpty()) -1 else folded.indexOf(q)
            while (at >= 0) {
                positions.add(at)
                at = folded.indexOf(q, at + maxOf(q.length, 1))
            }
        }
        return positions.toList()
    }

    fun contains(text: String, queries: List<String>): Boolean {
        val folded = fold(text)
        return queries.any { it.isNotEmpty() && folded.contains(it) }
    }
}

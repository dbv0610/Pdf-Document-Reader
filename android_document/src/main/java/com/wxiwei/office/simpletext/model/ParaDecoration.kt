/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.simpletext.model

/**
 * Paragraph shading and borders (DOCX pPr/shd, pPr/pBdr). Equal decorations of consecutive
 * paragraphs are drawn as one box, like Word does.
 */
data class ParaDecoration(
    val shading: Int?,
    val top: Side?,
    val bottom: Side?,
    val left: Side?,
    val right: Side?,
) {
    /** [eighths] of a point wide, [space] points between the text and the line. */
    data class Side(val eighths: Int, val color: Int, val space: Int)
}

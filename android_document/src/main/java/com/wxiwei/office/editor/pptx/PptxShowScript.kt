/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor.pptx

/**
 * A place to tap on a slide during the slideshow: a web [url], a [slideIndex] to show, or a
 * [jump] of the show (next, previous, first, last, end). [rectEmu] is the linked shape.
 */
data class SlideLink(val rectEmu: Rect, val url: String? = null, val slideIndex: Int? = null, val jump: String? = null)

/** What the slideshow needs of one slide. */
data class SlideScript(
    val index: Int,
    val title: String,
    val hidden: Boolean,
    val transition: SlideTransition?,
    val effects: List<SlideEffect>,
    val links: List<SlideLink>,
)

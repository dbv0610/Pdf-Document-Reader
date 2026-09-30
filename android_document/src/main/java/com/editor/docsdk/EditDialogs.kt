/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import android.net.Uri
import com.wxiwei.office.editor.pptx.SlideEffect

/**
 * Shows your own dialogs instead of the SDK's when an edit command asks the user something (a
 * color, a size, a name...). Return true when you handle [request]: answer it later with its
 * `answer(...)` (or `cancel()`); return false and the SDK shows its own dialog.
 *
 * ```
 * editor.dialogs = EditDialogs { request ->
 *     when (request) {
 *         is EditRequest.Color -> { myColorPicker.show { hex -> request.answer(hex) }; true }
 *         else -> false
 *     }
 * }
 * ```
 *
 * The bigger dialogs (Paragraph, Line spacing, Find & replace, cell Alignment, the shape list,
 * animations and transitions) come here too, with what they start from and what to call; the
 * SDK's own follow [DialogStyle].
 */
fun interface EditDialogs {
    fun ask(request: EditRequest): Boolean
}

/** A question an edit command asks; answer it once. [action] is the command that asks. */
sealed class EditRequest(val action: EditAction?, val title: String) {
    private var done = false

    /** Nothing is changed. */
    fun cancel() {
        done = true
    }

    internal fun <T> finish(value: T, deliver: (T) -> Unit) {
        if (done) return
        done = true
        deliver(value)
    }

    /** A color, "RRGGBB"; null ("no color") only when [allowsNone]. */
    class Color internal constructor(action: EditAction?, title: String, val allowsNone: Boolean,
                                     private val onAnswer: (String?) -> Unit) : EditRequest(action, title) {
        fun answer(hex: String?) = finish(hex?.removePrefix("#")?.uppercase(), onAnswer)
    }

    /** A font size in points; [sizes] are the usual ones. */
    class FontSize internal constructor(action: EditAction?, title: String, val sizes: List<Float>,
                                        private val onAnswer: (Float) -> Unit) : EditRequest(action, title) {
        fun answer(points: Float) = finish(points, onAnswer)
    }

    /** A font, one of [fonts]; [current] is the font of the selection, if known. */
    class Font internal constructor(action: EditAction?, title: String, val fonts: List<String>, val current: String?,
                                    private val onAnswer: (String) -> Unit) : EditRequest(action, title) {
        fun answer(font: String) = finish(font, onAnswer)
    }

    /** One of [options] (their index); [selected] is the current one, or -1. */
    class Choice internal constructor(action: EditAction?, title: String, val options: List<String>, val selected: Int,
                                      private val onAnswer: (Int) -> Unit) : EditRequest(action, title) {
        fun answer(index: Int) = finish(index, onAnswer)
    }

    /** A text, starting from [initial]. */
    class Text internal constructor(action: EditAction?, title: String, val hint: String, val initial: String,
                                    private val onAnswer: (String) -> Unit) : EditRequest(action, title) {
        fun answer(text: String) = finish(text, onAnswer)
    }

    /** A number, starting from [initial]. */
    class Number internal constructor(action: EditAction?, title: String, val initial: Double,
                                      private val onAnswer: (Double) -> Unit) : EditRequest(action, title) {
        fun answer(value: Double) = finish(value, onAnswer)
    }

    /** The size of a new table. */
    class Table internal constructor(action: EditAction?, title: String,
                                     private val onAnswer: (TableSize) -> Unit) : EditRequest(action, title) {
        fun answer(rows: Int, columns: Int) = finish(TableSize(rows, columns), onAnswer)
    }

    /** Yes or no: [message] says what happens. */
    class Confirm internal constructor(action: EditAction?, title: String, val message: String,
                                       private val onAnswer: () -> Unit) : EditRequest(action, title) {
        /** Goes on; [cancel] stops. */
        fun answer() = finish(Unit) { onAnswer() }
    }

    /** Word's Paragraph dialog, starting from the paragraph at the caret. */
    class Paragraph internal constructor(action: EditAction?, title: String, val current: ParagraphFormat,
                                         private val onAnswer: (ParagraphFormat) -> Unit) : EditRequest(action, title) {
        fun answer(format: ParagraphFormat) = finish(format, onAnswer)
    }

    /** Word's Line spacing dialog, starting from the paragraph at the caret. */
    class LineSpacing internal constructor(action: EditAction?, title: String, val current: LineSpacingFormat,
                                           private val onAnswer: (LineSpacingFormat) -> Unit) : EditRequest(action, title) {
        fun answer(format: LineSpacingFormat) = finish(format, onAnswer)
    }

    /** Excel's Alignment dialog, starting from the selected cell. */
    class CellAlignment internal constructor(action: EditAction?, title: String, val current: CellAlignmentFormat,
                                             private val onAnswer: (CellAlignmentFormat) -> Unit) : EditRequest(action, title) {
        fun answer(format: CellAlignmentFormat) = finish(format, onAnswer)
    }

    /** The transition of the slide shown; [current] is null when it has none. */
    class Transition internal constructor(action: EditAction?, title: String, val current: TransitionFormat?,
                                          private val onAnswer: (TransitionFormat) -> Unit) : EditRequest(action, title) {
        fun answer(format: TransitionFormat) = finish(format, onAnswer)
    }

    /**
     * Find and replace in a Word document: keep your dialog open and call these while it shows.
     * A match found is selected and scrolled into view.
     */
    class FindReplace internal constructor(action: EditAction?, title: String, private val finder: Finder) : EditRequest(action, title) {
        /** Selects the next match after the selection (the first after the last); null when there is none. */
        fun findNext(text: String, matchCase: Boolean = false): FindResult? = finder.findNext(text, matchCase)

        /** Replaces the selected match with [replacement] and selects the next one. */
        fun replace(text: String, replacement: String, matchCase: Boolean = false): FindResult? = finder.replace(text, replacement, matchCase)

        /** Replaces every match, as one undoable step; how many were replaced. */
        fun replaceAll(text: String, replacement: String, matchCase: Boolean = false): Int = finder.replaceAll(text, replacement, matchCase)
    }

    /** The shapes of the slide shown: list them, select one, change its layer. */
    class Shapes internal constructor(action: EditAction?, title: String,
                                      private val read: () -> List<ShapeItem>,
                                      private val onSelect: (Int) -> Unit,
                                      private val onReorder: (Int, ShapeOrder) -> Boolean) : EditRequest(action, title) {
        /** The top layer first. */
        val shapes: List<ShapeItem> get() = read()

        /** Selects shape [id] on the slide; the request is done. */
        fun select(id: Int) = finish(id, onSelect)

        /** Moves shape [id] to another layer; false when it could not (the user is told why). */
        fun reorder(id: Int, order: ShapeOrder): Boolean = onReorder(id, order)
    }

    /**
     * The animations of slide [slide] (0 based), in play order. [selectedShapeId] is the shape
     * selected, or -1: new effects usually go on it.
     */
    class Animations internal constructor(action: EditAction?, title: String, val slide: Int, val selectedShapeId: Int,
                                          private val read: () -> List<SlideEffect>,
                                          private val write: (List<SlideEffect>) -> Boolean) : EditRequest(action, title) {
        val effects: List<SlideEffect> get() = read()

        /** Replaces the slide's animations with [effects]; false when it could not (the user is told why). */
        fun setEffects(effects: List<SlideEffect>): Boolean = write(effects)
    }

    /** A picture to insert. */
    class Picture internal constructor(action: EditAction?, title: String,
                                       private val onAnswer: (Uri) -> Unit) : EditRequest(action, title) {
        fun answer(picture: Uri) = finish(picture, onAnswer)
    }
}

/** A match of [EditRequest.FindReplace]: its number (1 based) among [count]. */
data class FindResult(val number: Int, val count: Int)

/** A shape of a slide ([EditRequest.Shapes]). */
data class ShapeItem(val id: Int, val name: String, val kind: String, val text: String, val selected: Boolean)

/** Where [EditRequest.Shapes.reorder] moves a shape. */
enum class ShapeOrder { FRONT, FORWARD, BACKWARD, BACK }

internal interface Finder {
    fun findNext(text: String, matchCase: Boolean): FindResult?
    fun replace(text: String, replacement: String, matchCase: Boolean): FindResult?
    fun replaceAll(text: String, replacement: String, matchCase: Boolean): Int
}

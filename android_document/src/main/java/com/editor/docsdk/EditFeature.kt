/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.editor.docsdk

import java.util.EnumSet

/**
 * What the user may change while editing a Word (.docx), Excel (.xlsx) or PowerPoint (.pptx)
 * document. A feature turned off hides its buttons and its gestures on the document. Save is
 * always there. A feature that does not apply to a format is ignored for it.
 *
 * ```
 * documentView.startEditing(EnumSet.of(EditFeature.TEXT, EditFeature.FORMAT, EditFeature.UNDO_REDO))
 * ```
 */
enum class EditFeature {
    /** Type and delete text (Word), cell values and formulas (Excel), text of shapes (PowerPoint). */
    TEXT,
    /** Bold, italic, underline, colors, font, size, alignment; Excel: fill, borders, number format, rotation, wrap. */
    FORMAT,
    /** Word: bullets, numbering, alignment, indents, line and paragraph spacing. */
    PARAGRAPH,
    /** Word: copy, cut and paste. */
    CLIPBOARD,
    /** Word: find and replace. */
    FIND_REPLACE,
    /** Add pictures; move, resize and delete them. */
    PICTURES,
    /** Word: insert tables, add and delete rows and columns, move tables, resize columns and rows. */
    TABLES,
    /** Excel: insert and delete rows and columns, change their width and height. */
    ROWS_COLUMNS,
    /** Excel: merge and split cells. */
    MERGE_CELLS,
    /** Excel: add sheets. */
    SHEETS,
    /** PowerPoint: add text boxes and shapes; move, rotate, delete and reorder shapes. */
    SHAPES,
    /** PowerPoint: add, duplicate, move and delete slides. */
    SLIDES,
    /** PowerPoint: animations and slide transitions. */
    ANIMATIONS,
    /** PowerPoint: export a slide as PNG or PDF. */
    EXPORT,
    /** Undo and redo. */
    UNDO_REDO,
    /** Save a copy of the edited document where the user picks. */
    SAVE_COPY;

    companion object {
        /** Every feature: the default. */
        @JvmStatic
        fun all(): Set<EditFeature> = EnumSet.allOf(EditFeature::class.java)
    }
}

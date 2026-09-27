package com.wxiwei.office.editor.pptx

import android.graphics.Color
import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.common.shape.GroupShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.PictureShape
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.dom4j.DocumentHelper
import com.wxiwei.office.fc.ppt.attribute.SectionAttr
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.control.Presentation
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.system.IControl
import java.io.File

/**
 * What [LivePptxSession] needs from the screen. The Android implementation is [LiveSlideModel];
 * tests inject a fake. Every call returns false when the shape cannot be shown live, in which case
 * the edit is still saved to the file but the view only shows it after a reopen.
 */
interface LiveSlideDisplay {
    fun addTextBox(slideIndex: Int, id: Int, rectEmu: Rect, text: String, sizePt: Float, rgbHex: String, bold: Boolean): Boolean
    fun addImage(slideIndex: Int, id: Int, rectEmu: Rect, imageFile: File): Boolean
    /** Current text of a shape, or null when it has no text body in the model. */
    fun shapeText(slideIndex: Int, id: Int): String?
    /** [where]: the shape's box, for a shape the view does not draw yet (an empty text box). */
    fun setShapeText(slideIndex: Int, id: Int, text: String, where: Rect? = null): Boolean
    /** Current bounds, or null when the shape is not in the model. */
    fun shapeRect(slideIndex: Int, id: Int): Rect?
    fun moveShape(slideIndex: Int, id: Int, rectEmu: Rect): Boolean
    /** Remove the model shapes with [id]; returns a token that [restoreShape] puts back, or null. */
    fun removeShape(slideIndex: Int, id: Int): Any?
    fun restoreShape(slideIndex: Int, token: Any): Boolean
    /** Formats the shape's text; returns a token for [restoreFormat], or null when not shown live. */
    /** Rotation in degrees of the shape, or null when it cannot be shown live. */
    fun shapeRotation(slideIndex: Int, id: Int): Float? = null
    fun rotateShape(slideIndex: Int, id: Int, degrees: Float): Boolean = false
    fun setTextFormat(slideIndex: Int, id: Int, format: TextFormat): Any? = null
    /** How the shape's first text run is drawn, for an editor over it; null when not shown. */
    fun textStyle(slideIndex: Int, id: Int): TextStyle? = null
    fun restoreFormat(slideIndex: Int, token: Any): Boolean = false
}

/**
 * Edits the pg model of the open presentation and repaints, so changes show without a reopen.
 * Main thread only. Model bounds are pixels at 96 dpi, i.e. EMU / 9525, and model shape ids are the
 * XML cNvPr ids, so they match [PptxEditor] ids.
 */
/** Text size in points, ARGB color and typeface of a shape's text. */
data class TextStyle(val sizePt: Float, val color: Int, val typeface: android.graphics.Typeface)

class LiveSlideModel(private val control: IControl) : LiveSlideDisplay {
    private val presentation get() = control.getView() as? Presentation

    private fun slide(index: Int): PGSlide? = presentation?.getSlide(index)

    private fun px(emu: Long) = Math.round(emu / EMU_PER_PX.toDouble()).toInt()
    private fun emu(px: Int) = px.toLong() * EMU_PER_PX
    private fun rectangle(r: Rect) = Rectangle(px(r.x), px(r.y), maxOf(1, px(r.width)), maxOf(1, px(r.height)))

    /** Model shapes with [id], top level and inside groups, with the list that owns them. */
    private fun find(slide: PGSlide, id: Int): List<IShape> {
        val found = ArrayList<IShape>()
        fun visit(shapes: Array<IShape>) {
            for (shape in shapes) {
                if (shape.shapeID == id) found.add(shape)
                if (shape is GroupShape) visit(shape.getShapes())
            }
        }
        visit(slide.getShapes())
        return found
    }

    private fun repaint() {
        val p = presentation ?: return
        val list = p.getPrintMode().getListView()
        if (list != null) for (i in 0 until list.childCount) list.getChildAt(i).invalidate()
        p.postInvalidate()
    }

    /** One paragraph per line, laid out like the PPTX reader builds them (fc/ppt/attribute/RunAttr). */
    private fun buildSection(rect: Rectangle, text: String, paraAttr: IAttributeSet?, leafAttr: IAttributeSet?,
                             sectionAttr: IAttributeSet?): SectionElement {
        val section = SectionElement()
        section.setStartOffset(0)
        val attr = section.getAttribute()!!
        if (sectionAttr != null) attr.mergeAttribute(sectionAttr)
        else SectionAttr.instance().setSectionAttribute(DocumentHelper.createElement("bodyPr"), attr, null, null, false)
        AttrManage.instance().setPageWidth(attr, (rect.width * MainConstant.PIXEL_TO_TWIPS).toInt())
        AttrManage.instance().setPageHeight(attr, (rect.height * MainConstant.PIXEL_TO_TWIPS).toInt())
        var offset = 0L
        for (line in text.replace("\r\n", "\n").split('\n')) {
            val para = ParagraphElement()
            para.setStartOffset(offset)
            paraAttr?.let { para.getAttribute()!!.mergeAttribute(it) }
            val leaf = LeafElement(line.replace(160.toChar(), ' '))
            leafAttr?.let { leaf.getAttribute()!!.mergeAttribute(it) }
            leaf.setStartOffset(offset)
            offset += line.length
            leaf.setEndOffset(offset)
            // Like RunAttr.processRun: the paragraph mark is appended to the last leaf
            leaf.setText(line + "\n")
            offset++
            para.appendLeaf(leaf)
            para.setEndOffset(offset)
            section.appendParagraph(para, WPModelConstant.MAIN)
        }
        section.setEndOffset(offset)
        return section
    }

    private fun setText(box: TextBox, text: String, paraAttr: IAttributeSet?, leafAttr: IAttributeSet?, sectionAttr: IAttributeSet?) {
        box.rootView?.dispose()
        box.rootView = null // SlideDrawKit lays out a new root on the next draw
        box.element = buildSection(requireNotNull(box.bounds), text, paraAttr, leafAttr, sectionAttr)
    }

    override fun addTextBox(slideIndex: Int, id: Int, rectEmu: Rect, text: String, sizePt: Float, rgbHex: String, bold: Boolean): Boolean {
        val slide = slide(slideIndex) ?: return false
        val box = TextBox()
        box.bounds = rectangle(rectEmu)
        box.shapeID = id
        box.isWrapLine = true
        val leafAttr = com.wxiwei.office.simpletext.model.AttributeSetImpl()
        AttrManage.instance().setFontSize(leafAttr, Math.round(sizePt))
        AttrManage.instance().setFontColor(leafAttr, Color.parseColor("#" + rgbHex.removePrefix("#")))
        if (bold) AttrManage.instance().setFontBold(leafAttr, true)
        setText(box, text, null, leafAttr, null)
        slide.appendShapes(box)
        repaint()
        return true
    }

    override fun addImage(slideIndex: Int, id: Int, rectEmu: Rect, imageFile: File): Boolean {
        val slide = slide(slideIndex) ?: return false
        val picture = Picture()
        picture.data = imageFile.readBytes()
        picture.setPictureType(imageFile.extension.lowercase().let { if (it == "jpg") "jpeg" else it })
        val shape = PictureShape()
        shape.pictureIndex = control.getSysKit().getPictureManage().addPicture(picture)
        shape.bounds = rectangle(rectEmu)
        shape.shapeID = id
        slide.appendShapes(shape)
        repaint()
        return true
    }

    override fun shapeText(slideIndex: Int, id: Int): String? {
        val slide = slide(slideIndex) ?: return null
        val box = find(slide, id).filterIsInstance<TextBox>().firstOrNull() ?: return null
        return box.element?.getText(null)?.removeSuffix("\n")
    }

    override fun setShapeText(slideIndex: Int, id: Int, text: String, where: Rect?): Boolean {
        val slide = slide(slideIndex) ?: return false
        val shapes = find(slide, id)
        val box = shapes.filterIsInstance<TextBox>().firstOrNull()
        if (box == null) {
            // A shape without a text body in the model: add one over its bounds
            val bounds = shapes.firstOrNull()?.bounds ?: where?.let { rectangle(it) } ?: return false
            val created = TextBox()
            created.bounds = bounds
            created.shapeID = id
            created.isWrapLine = true
            setText(created, text, null, null, null)
            slide.appendShapes(created)
        } else {
            // Keep the look of the first run and paragraph, which already hold the inherited styles
            val section = box.element
            val para = section?.getElement(0) as? ParagraphElement
            val leaf = para?.getLeaf(0)
            setText(box, text, para?.getAttribute()?.clone(), leaf?.getAttribute()?.clone(), section?.getAttribute()?.clone())
        }
        repaint()
        return true
    }

    override fun textStyle(slideIndex: Int, id: Int): TextStyle? {
        val box = slide(slideIndex)?.let { find(it, id) }?.filterIsInstance<TextBox>()?.firstOrNull() ?: return null
        val para = box.element?.getParaCollection()?.getElementForIndex(0) as? ParagraphElement ?: return null
        val leaf = para.getElementForIndex(0) ?: return null
        val am = AttrManage.instance()
        val p = para.getAttribute(); val l = leaf.getAttribute()
        val bold = am.getFontBold(p, l); val italic = am.getFontItalic(p, l)
        return TextStyle(am.getFontSizeF(p, l), am.getFontColor(p, l),
            com.wxiwei.office.simpletext.font.FontTypefaceManage.instance().getFontTypeface(am.getFontName(p, l), bold, italic))
    }

    override fun setTextFormat(slideIndex: Int, id: Int, format: TextFormat): Any? {
        val slide = slide(slideIndex) ?: return null
        // no text drawn (an empty or undrawn text box): nothing to show
        val box = find(slide, id).filterIsInstance<TextBox>().firstOrNull() ?: return FormatToken(null, emptyList())
        val section = box.element ?: return FormatToken(null, emptyList())
        val am = AttrManage.instance()
        val saved = ArrayList<Pair<com.wxiwei.office.simpletext.model.IElement, IAttributeSet>>()
        val count = section.getParaCollection()?.size() ?: 0
        for (i in 0 until count) {
            val para = section.getParaCollection()!!.getElementForIndex(i) as? ParagraphElement ?: continue
            saved.add(para to para.getAttribute()!!.clone())
            format.align?.let {
                am.setParaHorizontalAlign(para.getAttribute(), when (it) {
                    "ctr" -> com.wxiwei.office.constant.wp.WPAttrConstant.PARA_HOR_ALIGN_CENTER.toInt()
                    "r" -> com.wxiwei.office.constant.wp.WPAttrConstant.PARA_HOR_ALIGN_RIGHT.toInt()
                    "just" -> com.wxiwei.office.constant.wp.WPAttrConstant.PARA_HOR_ALIGN_JUSTIFIED.toInt()
                    else -> com.wxiwei.office.constant.wp.WPAttrConstant.PARA_HOR_ALIGN_LEFT.toInt()
                })
            }
            for (j in 0 until para.leafCount()) {
                val leaf = para.getElementForIndex(j) ?: continue
                val attr = leaf.getAttribute()!!
                saved.add(leaf to attr.clone())
                format.bold?.let { am.setFontBold(attr, it) }
                format.italic?.let { am.setFontItalic(attr, it) }
                format.underline?.let { am.setFontUnderline(attr, if (it) 1 else 0) }
                format.sizePt?.let { am.setFontSize(attr, it) }
                format.rgbHex?.let { am.setFontColor(attr, (0xFF shl 24) or it.removePrefix("#").toInt(16)) }
            }
        }
        relayout(box)
        return FormatToken(box, saved)
    }

    private class FormatToken(val box: TextBox?, val saved: List<Pair<com.wxiwei.office.simpletext.model.IElement, IAttributeSet>>)

    override fun restoreFormat(slideIndex: Int, token: Any): Boolean {
        val t = token as? FormatToken ?: return false
        for ((element, attr) in t.saved) element.setAttribute(attr.clone())
        t.box?.let { relayout(it) }
        return true
    }

    private fun relayout(box: TextBox) {
        box.rootView?.dispose()
        box.rootView = null // laid out again on the next draw
        repaint()
    }

    override fun shapeRect(slideIndex: Int, id: Int): Rect? {
        val slide = slide(slideIndex) ?: return null
        val b = find(slide, id).firstOrNull()?.bounds ?: return null
        return Rect(emu(b.x), emu(b.y), emu(b.width), emu(b.height))
    }

    override fun moveShape(slideIndex: Int, id: Int, rectEmu: Rect): Boolean {
        val slide = slide(slideIndex) ?: return false
        val shapes = find(slide, id)
        if (shapes.isEmpty()) return true // not drawn (an empty text box): nothing to show
        val target = rectangle(rectEmu)
        for (shape in shapes) {
            val old = shape.bounds
            if (shape is GroupShape && old != null && old.width > 0 && old.height > 0) moveGroupChildren(shape, old, target)
            shape.bounds = Rectangle(target.x, target.y, target.width, target.height)
            if (shape is TextBox) {
                val section = shape.element
                if (section != null) {
                    AttrManage.instance().setPageWidth(section.getAttribute(), (target.width * MainConstant.PIXEL_TO_TWIPS).toInt())
                    AttrManage.instance().setPageHeight(section.getAttribute(), (target.height * MainConstant.PIXEL_TO_TWIPS).toInt())
                }
                shape.rootView?.dispose()
                shape.rootView = null
            }
        }
        repaint()
        return true
    }

    override fun shapeRotation(slideIndex: Int, id: Int): Float? {
        val shape = slide(slideIndex)?.let { find(it, id) }?.firstOrNull() ?: return null
        return if (shape is GroupShape) null else shape.rotation
    }

    // groups are not rotated live: their children carry their own drawing rotation
    override fun rotateShape(slideIndex: Int, id: Int, degrees: Float): Boolean {
        val shapes = slide(slideIndex)?.let { find(it, id) } ?: return false
        // not drawn at all (an empty text box): nothing to show
        if (shapes.isEmpty()) return true
        if (shapes.any { it is GroupShape }) return false
        val normalized = (degrees % 360f + 360f) % 360f
        shapes.forEach { it.rotation = normalized }
        repaint()
        return true
    }

    private fun moveGroupChildren(group: GroupShape, from: Rectangle, to: Rectangle) {
        val sx = to.width.toDouble() / from.width; val sy = to.height.toDouble() / from.height
        for (child in group.getShapes()) {
            val b = child.bounds ?: continue
            val moved = Rectangle(
                (to.x + (b.x - from.x) * sx).toInt(), (to.y + (b.y - from.y) * sy).toInt(),
                maxOf(1, (b.width * sx).toInt()), maxOf(1, (b.height * sy).toInt()))
            if (child is GroupShape) moveGroupChildren(child, b, moved)
            child.bounds = moved
            if (child is TextBox) { child.rootView?.dispose(); child.rootView = null
            }
        }
    }

    private class Removed(val entries: List<Triple<GroupShape?, Int, IShape>>)

    override fun removeShape(slideIndex: Int, id: Int): Any? {
        val slide = slide(slideIndex) ?: return null
        // top-level shapes and group children, each with where it was
        val entries = ArrayList<Triple<GroupShape?, Int, IShape>>()
        fun visit(group: GroupShape?, shapes: Array<IShape>) {
            for (shape in shapes) {
                if (shape.shapeID == id) entries.add(Triple(group, -1, shape))
                else if (shape is GroupShape) visit(shape, shape.getShapes())
            }
        }
        visit(null, slide.getShapes())
        val removed = entries.map { (group, _, shape) -> Triple(group, group?.removeShape(shape) ?: slide.removeShape(shape), shape) }
        // not drawn at all (an empty text box): nothing to take away
        if (removed.isNotEmpty()) repaint()
        return Removed(removed)
    }

    override fun restoreShape(slideIndex: Int, token: Any): Boolean {
        val slide = slide(slideIndex) ?: return false
        val removed = token as? Removed ?: return false
        for ((group, index, shape) in removed.entries.reversed()) {
            if (group != null) group.insertShape(index, shape) else slide.insertShape(index, shape)
        }
        repaint()
        return true
    }

    companion object {
        const val EMU_PER_PX = 9525L
    }
}

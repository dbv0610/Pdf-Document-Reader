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
    fun setShapeText(slideIndex: Int, id: Int, text: String): Boolean
    /** Current bounds, or null when the shape is not in the model. */
    fun shapeRect(slideIndex: Int, id: Int): Rect?
    fun moveShape(slideIndex: Int, id: Int, rectEmu: Rect): Boolean
    /** Remove the model shapes with [id]; returns a token that [restoreShape] puts back, or null. */
    fun removeShape(slideIndex: Int, id: Int): Any?
    fun restoreShape(slideIndex: Int, token: Any): Boolean
}

/**
 * Edits the pg model of the open presentation and repaints, so changes show without a reopen.
 * Main thread only. Model bounds are pixels at 96 dpi, i.e. EMU / 9525, and model shape ids are the
 * XML cNvPr ids, so they match [PptxEditor] ids.
 */
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
                if (shape.getShapeID() == id) found.add(shape)
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
        box.getRootView()?.dispose()
        box.setRootView(null) // SlideDrawKit lays out a new root on the next draw
        box.setElement(buildSection(box.getBounds(), text, paraAttr, leafAttr, sectionAttr))
    }

    override fun addTextBox(slideIndex: Int, id: Int, rectEmu: Rect, text: String, sizePt: Float, rgbHex: String, bold: Boolean): Boolean {
        val slide = slide(slideIndex) ?: return false
        val box = TextBox()
        box.setBounds(rectangle(rectEmu))
        box.setShapeID(id)
        box.setWrapLine(true)
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
        picture.setData(imageFile.readBytes())
        picture.setPictureType(imageFile.extension.lowercase().let { if (it == "jpg") "jpeg" else it })
        val shape = PictureShape()
        shape.setPictureIndex(control.getSysKit().getPictureManage().addPicture(picture))
        shape.setBounds(rectangle(rectEmu))
        shape.setShapeID(id)
        slide.appendShapes(shape)
        repaint()
        return true
    }

    override fun shapeText(slideIndex: Int, id: Int): String? {
        val slide = slide(slideIndex) ?: return null
        val box = find(slide, id).filterIsInstance<TextBox>().firstOrNull() ?: return null
        return box.getElement()?.getText(null)?.removeSuffix("\n")
    }

    override fun setShapeText(slideIndex: Int, id: Int, text: String): Boolean {
        val slide = slide(slideIndex) ?: return false
        val shapes = find(slide, id)
        val box = shapes.filterIsInstance<TextBox>().firstOrNull()
        if (box == null) {
            // A shape without a text body in the model: add one over its bounds
            val owner = shapes.firstOrNull() ?: return false
            val created = TextBox()
            created.setBounds(owner.getBounds())
            created.setShapeID(id)
            created.setWrapLine(true)
            setText(created, text, null, null, null)
            slide.appendShapes(created)
        } else {
            // Keep the look of the first run and paragraph, which already hold the inherited styles
            val section = box.getElement()
            val para = section?.getElement(0) as? ParagraphElement
            val leaf = para?.getLeaf(0)
            setText(box, text, para?.getAttribute()?.clone(), leaf?.getAttribute()?.clone(), section?.getAttribute()?.clone())
        }
        repaint()
        return true
    }

    override fun shapeRect(slideIndex: Int, id: Int): Rect? {
        val slide = slide(slideIndex) ?: return null
        val b = find(slide, id).firstOrNull()?.getBounds() ?: return null
        return Rect(emu(b.x), emu(b.y), emu(b.width), emu(b.height))
    }

    override fun moveShape(slideIndex: Int, id: Int, rectEmu: Rect): Boolean {
        val slide = slide(slideIndex) ?: return false
        val shapes = find(slide, id)
        if (shapes.isEmpty()) return false
        val target = rectangle(rectEmu)
        for (shape in shapes) {
            val old = shape.getBounds()
            if (shape is GroupShape && old != null && old.width > 0 && old.height > 0) moveGroupChildren(shape, old, target)
            shape.setBounds(Rectangle(target.x, target.y, target.width, target.height))
            if (shape is TextBox) {
                val section = shape.getElement()
                if (section != null) {
                    AttrManage.instance().setPageWidth(section.getAttribute(), (target.width * MainConstant.PIXEL_TO_TWIPS).toInt())
                    AttrManage.instance().setPageHeight(section.getAttribute(), (target.height * MainConstant.PIXEL_TO_TWIPS).toInt())
                }
                shape.getRootView()?.dispose()
                shape.setRootView(null)
            }
        }
        repaint()
        return true
    }

    private fun moveGroupChildren(group: GroupShape, from: Rectangle, to: Rectangle) {
        val sx = to.width.toDouble() / from.width; val sy = to.height.toDouble() / from.height
        for (child in group.getShapes()) {
            val b = child.getBounds() ?: continue
            val moved = Rectangle(
                (to.x + (b.x - from.x) * sx).toInt(), (to.y + (b.y - from.y) * sy).toInt(),
                maxOf(1, (b.width * sx).toInt()), maxOf(1, (b.height * sy).toInt()))
            if (child is GroupShape) moveGroupChildren(child, b, moved)
            child.setBounds(moved)
            if (child is TextBox) { child.getRootView()?.dispose(); child.setRootView(null) }
        }
    }

    private class Removed(val entries: List<Pair<Int, IShape>>)

    override fun removeShape(slideIndex: Int, id: Int): Any? {
        val slide = slide(slideIndex) ?: return null
        // Only top-level shapes can be removed from the model; a group child needs a reopen
        val entries = slide.getShapes().filter { it.getShapeID() == id }.map { shape -> slide.removeShape(shape) to shape }
        if (entries.isEmpty()) return null
        repaint()
        return Removed(entries)
    }

    override fun restoreShape(slideIndex: Int, token: Any): Boolean {
        val slide = slide(slideIndex) ?: return false
        val removed = token as? Removed ?: return false
        for ((index, shape) in removed.entries.reversed()) slide.insertShape(index, shape)
        repaint()
        return true
    }

    companion object {
        const val EMU_PER_PX = 9525L
    }
}

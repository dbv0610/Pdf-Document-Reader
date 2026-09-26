package com.wxiwei.office.fc.xls.Reader.shared

import com.wxiwei.office.common.bg.AShader
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.bg.LinearGradientShader
import com.wxiwei.office.common.bg.RadialGradientShader
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.ElementPath
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.sheetProperty.Palette
import com.wxiwei.office.ss.model.style.BorderStyle
import com.wxiwei.office.ss.model.style.BuiltinFormats
import com.wxiwei.office.ss.model.style.CellBorder
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.model.style.NumberFormat
import com.wxiwei.office.ss.model.table.TableFormatManager
import com.wxiwei.office.ss.util.ColorUtil
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.IReader
import java.util.HashMap

class StyleReader private constructor() {
    private var book: Workbook? = null
    private var iReader: IReader? = null
    private var numFmts: MutableMap<Int, NumberFormat>? = null
    private var cellBorders: MutableMap<Int, CellBorder>? = null
    private var fills: MutableMap<Int, BackgroundAndFill?>? = null
    private var tableFormatManager: TableFormatManager? = null
    private var fontIndex = 0
    private var fillIndex = 0
    private var borderIndex = 0
    private var styleIndex = 0
    private var indexedColor = 0

    fun getWorkBookStyle(styleParts: PackagePart, workbook: Workbook, reader: IReader) {
        book = workbook
        iReader = reader
        fontIndex = 0
        fillIndex = 0
        borderIndex = 0
        styleIndex = 0
        indexedColor = 0
        fills = HashMap(5)
        cellBorders = HashMap(5)
        getBuiltinNumberFormats()
        val saxReader = SAXReader()
        try {
            val handler = StyleSaxHandler()
            saxReader.addHandler("/styleSheet/numFmts/numFmt", handler)
            saxReader.addHandler("/styleSheet/fonts/font", handler)
            saxReader.addHandler("/styleSheet/fills/fill", handler)
            saxReader.addHandler("/styleSheet/borders/border", handler)
            saxReader.addHandler("/styleSheet/cellXfs/xf", handler)
            saxReader.addHandler("/styleSheet/colors/indexedColors/rgbColor", handler)
            saxReader.addHandler("/styleSheet/dxfs/dxf", handler)
            val input = styleParts.getInputStream()
            saxReader.read(input)
            input.close()
            dispose()
        } finally {
            saxReader.resetHandlers()
        }
    }

    private fun getBuiltinNumberFormats() {
        val formats = BuiltinFormats.getAll()
        numFmts = HashMap(formats.size + 20)
        for (i in formats.indices) numFmts!![i] = NumberFormat(i.toShort(), formats[i])
    }

    private fun colorIndex(element: Element?): Short {
        var index = 0
        if (element != null) {
            val theme = element.attribute("theme")
            val rgb = element.attribute("rgb")
            val indexed = element.attribute("indexed")
            if (theme != null) {
                index = book!!.getThemeColorIndex(element.attributeValue("theme").toInt())
                val tint = element.attribute("tint")
                if (tint != null) index = book!!.addColor(ColorUtil.instance().getColorWithTint(book!!.getColor(index), element.attributeValue("tint").toDouble()))
            } else if (rgb != null) {
                var value = element.attributeValue("rgb")
                if (value.length > 6) value = value.substring(value.length - 6)
                index = book!!.addColor((0xff shl 24) or value.toInt(16))
            } else if (indexed != null) {
                index = element.attributeValue("indexed").toInt()
                if (index == Palette.FIRST_COLOR_INDEX + Palette.STANDARD_PALETTE_SIZE) index = 0
                else if (index > Palette.FIRST_COLOR_INDEX + Palette.STANDARD_PALETTE_SIZE) index = Palette.FIRST_COLOR_INDEX + 1
            }
        }
        return index.toShort()
    }

    private fun processNumberFormat(element: Element): NumberFormat = NumberFormat(element.attributeValue("numFmtId").toShort(), element.attributeValue("formatCode"))

    private fun processFont(element: Element): Font {
        val font = Font()
        font.setIndex(fontIndex)
        val vert = element.element("fontElement")?.attributeValue("val")
        font.setSuperSubScript(when {
            vert.equals("superscript", true) -> Font.SS_SUPER.toByte()
            vert.equals("subscript", true) -> Font.SS_SUB.toByte()
            else -> Font.SS_NONE.toByte()
        })
        font.setFontSize(element.element("sz")?.attributeValue("val")?.toDouble() ?: 12.0)
        font.setColorIndex(colorIndex(element.element("color")).toInt())
        element.element("name")?.let { font.setName(it.attributeValue("val")) }
        element.element("b")?.let { font.setBold(it.attributeValue("val") == null || it.attributeValue("val").toBoolean()) }
        element.element("i")?.let { font.setItalic(it.attributeValue("val") == null || it.attributeValue("val").toBoolean()) }
        element.element("u")?.let {
            val value = it.attributeValue("val")
            if (value == null) font.setUnderline(Font.U_SINGLE.toInt()) else font.setUnderline(value)
        }
        element.element("strike")?.let { font.setStrikeline(it.attributeValue("val") == null || it.attributeValue("val").toBoolean()) }
        return font
    }

    private fun processFill(element: Element): BackgroundAndFill? {
        val pattern = element.element("patternFill")
        if (pattern != null) {
            if (pattern.attributeValue("patternType").equals("none", true)) return null
            val fill = BackgroundAndFill()
            pattern.element("fgColor")?.let { fill.setForegroundColor(book!!.getColor(colorIndex(it).toInt())) }
            pattern.element("fgColor")?.let { fill.setFillType(BackgroundAndFill.FILL_SOLID) }
            pattern.element("bgColor")?.let { fill.setBackgoundColor(book!!.getColor(colorIndex(it).toInt())) }
            return fill
        }
        val gradient = element.element("gradientFill") ?: return null
        val stops = gradient.elements("stop")
        val colors = IntArray(stops.size)
        val positions = FloatArray(stops.size)
        for (i in stops.indices) {
            val stop = stops[i] as Element
            positions[i] = stop.attributeValue("position").toFloat()
            colors[i] = book!!.getColor(colorIndex(stop.element("color")).toInt())
        }
        val fill = BackgroundAndFill()
        val shader: AShader
        if (!gradient.attributeValue("type").equals("path", true)) {
            fill.setFillType(BackgroundAndFill.FILL_SHADE_LINEAR)
            shader = LinearGradientShader((gradient.attributeValue("degree")?.toFloat() ?: 0f), colors, positions)
        } else {
            fill.setFillType(BackgroundAndFill.FILL_SHADE_RADIAL)
            shader = RadialGradientShader(radialCenter(gradient), colors, positions)
        }
        fill.setShader(shader)
        return fill
    }

    private fun radialCenter(element: Element): Int {
        val l = element.attributeValue("left")
        val t = element.attributeValue("top")
        val r = element.attributeValue("right")
        val b = element.attributeValue("bottom")
        if (l == "1" && t == "1" && r == "1" && b == "1") return RadialGradientShader.Center_BR
        if (b == "1" && t == "1") return RadialGradientShader.Center_BL
        if (l == "1" && r == "1") return RadialGradientShader.Center_TR
        if (l == "0.5" && t == "0.5" && r == "0.5" && b == "0.5") return RadialGradientShader.Center_Center
        return RadialGradientShader.Center_TL
    }

    private fun processBorder(element: Element): CellBorder {
        val border = CellBorder()
        fun side(name: String, setter: (BorderStyle) -> Unit) {
            element.element(name)?.let { setter(BorderStyle(it.attributeValue("style"), colorIndex(it.element("color")))) }
        }
        side("left") { border.setLeftBorder(it) }
        side("top") { border.setTopBorder(it) }
        side("right") { border.setRightBorder(it) }
        side("bottom") { border.setBottomBorder(it) }
        return border
    }

    private fun alignment(style: CellStyle, element: Element) {
        element.attributeValue("vertical")?.let { style.setVerticalAlign(it) }
        element.attributeValue("horizontal")?.let { style.setHorizontalAlign(it) }
        element.attributeValue("textRotation")?.let { style.setRotation(it.toShort()) }
        element.attributeValue("wrapText")?.let { style.setWrapText(it.toInt() != 0) }
        element.attributeValue("indent")?.let { style.setIndent(it.toShort()) }
    }

    private fun processCellStyle(element: Element): CellStyle {
        val style = CellStyle()
        val numberId = element.attributeValue("numFmtId")?.toInt() ?: 0
        numFmts!![numberId]?.let { style.setNumberFormat(it) }
        style.setFontIndex((element.attributeValue("fontId")?.toInt() ?: 0).toShort())
        style.setFillPattern(fills!![element.attributeValue("fillId")?.toInt() ?: 0])
        style.setBorder(cellBorders!![element.attributeValue("borderId")?.toInt() ?: 0])
        element.element("alignment")?.let { alignment(style, it) }
        return style
    }

    private fun processTableFormat(element: Element) {
        if (tableFormatManager == null) {
            tableFormatManager = TableFormatManager(5)
            book!!.setTableFormatManager(tableFormatManager)
        }
        val style = CellStyle()
        element.element("numFmt")?.let { style.setNumberFormat(processNumberFormat(it)) }
        element.element("font")?.let { book!!.addFont(fontIndex, processFont(it)); style.setFontIndex(fontIndex++.toShort()) }
        element.element("fill")?.let { style.setFillPattern(processFill(it)) }
        element.element("border")?.let { style.setBorder(processBorder(it)) }
        element.element("alignment")?.let { alignment(style, it) }
        tableFormatManager!!.addFormat(style)
    }

    private fun dispose() {
        book = null
        iReader = null
        tableFormatManager = null
        numFmts?.clear()
        cellBorders?.clear()
        fills?.clear()
        numFmts = null
        cellBorders = null
        fills = null
    }

    private inner class StyleSaxHandler : ElementHandler {
        override fun onStart(path: ElementPath) {}
        override fun onEnd(path: ElementPath) {
            if (iReader!!.isAborted()) throw AbortReaderError("abort Reader")
            val element = path.getCurrent()
            when (element.getName()) {
                "numFmt" -> { val format = processNumberFormat(element); numFmts!![format.getNumberFormatID().toInt()] = format }
                "font" -> book!!.addFont(fontIndex++, processFont(element))
                "fill" -> fills!![fillIndex++] = processFill(element)
                "border" -> cellBorders!![borderIndex++] = processBorder(element)
                "xf" -> book!!.addCellStyle(styleIndex++, processCellStyle(element))
                "rgbColor" -> book!!.addColor(indexedColor++, (0xff shl 24) or element.attributeValue("rgb").takeLast(6).toInt(16))
                "dxf" -> processTableFormat(element)
            }
            element.detach()
        }
    }

    companion object {
        private val READER = StyleReader()
        @JvmStatic fun instance(): StyleReader = READER
    }
}

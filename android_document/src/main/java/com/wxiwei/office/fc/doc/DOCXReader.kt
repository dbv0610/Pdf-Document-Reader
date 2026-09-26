/*
 * 文件名称:          DOCXReader.kt  (chuyển từ DOCXReader.java)
 *
 * 编译器:            android2.2
 * 时间:              下午1:32:45
 */
@file:Suppress("UNCHECKED_CAST", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.wxiwei.office.fc.doc

import com.wxiwei.office.editor.docx.DocxSourceMap
import java.util.IdentityHashMap

import java.io.File
import java.io.InputStream
import java.text.Normalizer

import org.xmlpull.v1.XmlPullParser

import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.common.autoshape.AutoShapeDataKit
import com.wxiwei.office.common.autoshape.ExtendPath
import com.wxiwei.office.common.autoshape.pathbuilder.LineArrowPathBuilder
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.bg.Gradient
import com.wxiwei.office.common.bg.LinearGradientShader
import com.wxiwei.office.common.bg.PatternShader
import com.wxiwei.office.common.bg.RadialGradientShader
import com.wxiwei.office.common.bg.TileShader
import com.wxiwei.office.common.bookmark.Bookmark
import com.wxiwei.office.common.borders.Border
import com.wxiwei.office.common.borders.Borders
import com.wxiwei.office.common.borders.Line
import com.wxiwei.office.common.bulletnumber.ListData
import com.wxiwei.office.common.bulletnumber.ListLevel
import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.common.pictureefftect.PictureEffectInfo
import com.wxiwei.office.common.pictureefftect.PictureEffectInfoFactory
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.GroupShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.PictureShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.common.shape.WPAbstractShape
import com.wxiwei.office.common.shape.WPAutoShape
import com.wxiwei.office.common.shape.WPChartShape
import com.wxiwei.office.common.shape.WPGroupShape
import com.wxiwei.office.common.shape.WPPictureShape
import com.wxiwei.office.common.shape.WatermarkShape
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SchemeClrConstant
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.FCKit
import com.wxiwei.office.fc.LineKit
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.ElementPath
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.exceptions.InvalidFormatException
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.PackageRelationship
import com.wxiwei.office.fc.openxml4j.opc.PackageRelationshipTypes
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.reader.ReaderKit
import com.wxiwei.office.fc.ppt.reader.ThemeReader
import com.wxiwei.office.fc.xls.Reader.drawing.ChartReader
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.font.FontTypefaceManage
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.simpletext.model.Style
import com.wxiwei.office.simpletext.model.StyleManage
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.AbstractReader
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart
import com.wxiwei.office.wp.model.CellElement
import com.wxiwei.office.wp.model.HFElement
import com.wxiwei.office.wp.model.RowElement
import com.wxiwei.office.wp.model.TableElement
import com.wxiwei.office.wp.model.WPDocument

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import android.util.Log
import android.util.Xml

/**
 * Helper: dom4j trong thư viện này trả về List "raw" (không generic),
 * nên ép kiểu về List<Element> một lần ở đây cho gọn.
 */
internal fun Element.childElements(name: String): List<Element> =
    (elements(name) as? List<Element>) ?: emptyList()

internal fun Element.childElements(): List<Element> =
    (elements() as? List<Element>) ?: emptyList()

/** Parse int an toàn: "12", " 12 ", "12.5" -> 12; null/lỗi -> default (Java cũ: crash cả file) */
internal fun String?.toIntSafe(default: Int = 0): Int {
    if (this == null) return default
    val s = trim()
    return s.toIntOrNull() ?: s.toFloatOrNull()?.toInt() ?: default
}

internal fun String?.toFloatSafe(default: Float = 0f): Float = this?.trim()?.toFloatOrNull() ?: default

/**
 * "FF0000" / "#FF0000" / "#80FF0000" -> màu ARGB. Giá trị lạ ("auto", rỗng...) -> default.
 * Nhanh hơn Color.parseColor("#" + v) (không nối chuỗi) và không ném exception.
 */
internal fun parseHexColor(value: String?, default: Int): Int {
    if (value.isNullOrEmpty()) return default
    val hex = if (value[0] == '#') value.substring(1) else value
    val n = hex.toLongOrNull(16) ?: return default
    return when (hex.length) {
        6 -> (0xFF000000L or n).toInt()
        8 -> n.toInt()
        else -> default
    }
}

/**
 * docx reader
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-1-19
 */
class DOCXReader(control: IControl?, private var filePath: String?) : AbstractReader() {

    // Source identities exist only for the current SAX body subtree.
    private val editMap = DocxSourceMap.begin(filePath ?: "")
    private val editRuns = IdentityHashMap<Element, Int>()
    private val editParas = IdentityHashMap<Element, Int>()
    private var editRunBase = 0
    private var editParaBase = 0
    private var editObjectRun: Element? = null
    private fun editMain() = offset >= WPModelConstant.MAIN && offset < WPModelConstant.HEADER
    private fun recordEdit(start: Long, text: String, run: Element?, kind: DocxSourceMap.Kind) {
        if (editMain() && start >= 0 && editParas.isNotEmpty()) {
            val id = run?.let { editRuns[it] }
            editMap.addLeaf(start, start + text.length, if (id == null) intArrayOf() else intArrayOf(id), text, kind)
        }
    }

    // picture高度、宽度转换到磅单位的一个值，我也知道是什么意思，只是是通过大量文档分得出来的值
    //private val PICTURE_CONVERSION_VALUE = 0x7F * 100

    // ===================== FIELDS (trong Java nằm cuối file) =====================
    private var isProcessSectionAttribute = false
    private var isProcessHF = false
    private var isProcessWatermark = false
    private var styleID = 0
    // style w:default="1" of type paragraph ("Normal"): Word applies it to paragraphs without pStyle
    private var defaultParaStyleID = -1
    // offset计数器，此值非常重要，需要小心行事
    private var offset: Long = 0L
    private var textboxIndex: Long = 0L
    private var secElem: SectionElement? = null
    private var zipPackage: ZipPackage? = null
    private var wpdoc: WPDocument? = null
    private var packagePart: PackagePart? = null
    private var hfPart: PackagePart? = null
    private val styleStrID: MutableMap<String, Int> = HashMap()
    private val tableStyle: MutableMap<String, Int> = HashMap()
    private val tableGridCol: MutableMap<Int, Int> = HashMap()
    // borders and cell margins of table styles (styles.xml tblPr), keyed by styleId
    private val tableStyleBorders = HashMap<String, Map<String, DocxBorder>>()
    private val tableStyleCellMar = HashMap<String, Map<String, Int>>()
    // state of the table being read: borders/margins after style + tblPr, size of the grid
    private var tblBorders: Map<String, DocxBorder> = emptyMap()
    private var tblCellMar: Map<String, Int> = emptyMap()
    private var tblRowCount = 0
    private var tblGridCount = 0
    // TỐI ƯU: HashMap thay Hashtable (không cần synchronized)
    private val bulletNumbersID: HashMap<String, String> = HashMap()
    // theme color
    private var themeColor: MutableMap<String, Int>? = null
    // TỐI ƯU: gộp relativeType (List) + relativeValue (Map) thành 1 LinkedHashMap giữ thứ tự
    private val relativeValue: LinkedHashMap<IShape, IntArray> = LinkedHashMap()

    // Getter non-null tiện dụng, tránh phải viết "!!" khắp nơi
    private val zip: ZipPackage get() = zipPackage!!
    private val mainPart: PackagePart get() = packagePart!!
    private val document: WPDocument get() = wpdoc!!
    private val section: SectionElement get() = secElem!!
    private val am: AttrManage get() = AttrManage.instance()

    init {
        this.control = control
    }

    /**
     *
     */
    @Throws(Exception::class)
    override fun getModel(): Any? {
        wpdoc?.let { return it }
        wpdoc = WPDocument()
        openFile()
        return document
    }

    /**
     *
     */
    @Throws(Exception::class)
    private fun openFile() {
        zipPackage = ZipPackage(filePath)

        /*URL url = new URL("http://172.25.3.147:8080/word_test_2007.docx");
        zipPackage = new ZipPackage(url.openStream());*/

        /*InputStream is = SocketClient.instance().getFile("E:/workdocument/reader/testdocument/word_test_2007.docx");
        zipPackage = new ZipPackage(is);*/
        if (zip.parts.size == 0) {
            throw Exception("Format error")
        }
        val coreRel = zip.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT)
            .getRelationship(0)
        if (coreRel.targetURI.toString() != "/word/document.xml") {
            throw Exception("Format error")
        }

        packagePart = zip.getPart(coreRel)
        // get theme color
        processThemeColor()
        // bullet and number
        processBulletNumber()
        // style
        processStyle()

        // section
        secElem = SectionElement()
        // document
        offset = WPModelConstant.MAIN
        val saxreader = SAXReader()
        val docxHandler = DOCXSaxHandler()
        saxreader.addHandler("/document/body/tbl", docxHandler)
        saxreader.addHandler("/document/body/p", docxHandler)
        saxreader.addHandler("/document/body/sdt", docxHandler)

        val doc = saxreader.read(mainPart.inputStream)

//        if (isPropertiesFileError(doc.rootElement.element("body").element("sectPr"))) {
//            throw Exception("File parsing error")
//        }
        // page background color
        val br = doc!!.rootElement!!.element("background")
        if (br != null) {
            var fill: BackgroundAndFill? = null
            if (br.element("background") != null) {
                //gradient background or tile
                fill = processBackgroundAndFill(br.element("background")!!)
            } else {
                val value = br.attributeValue("color")
                if (value != null) {
                    fill = BackgroundAndFill()
                    fill.foregroundColor = parseHexColor(value, Color.WHITE)
                }
            }
            document.setPageBackground(fill)
        }

        processSection(doc!!.rootElement!!.element("body")!!)

        processRelativeShapeSize()
    }

    private fun isPropertiesFileError(body: Element): Boolean {
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        val margin = body.element("pgMar")
        if (margin != null) {
            margin.attributeValue("left")?.let { left = it.toIntSafe(0) }
            margin.attributeValue("right")?.let { right = it.toIntSafe(0) }
            margin.attributeValue("top")?.let { top = it.toIntSafe(0) }
            margin.attributeValue("bottom")?.let { bottom = it.toIntSafe(0) }
        }
        return right <= 0 || left <= 0 || top <= 0 || bottom <= 0
    }

    /**
     * reader style
     */
    @Throws(Exception::class)
    private fun processStyle() {
        val styleRel = mainPart.getRelationshipsByType(PackageRelationshipTypes.STYLE_PART).getRelationship(0)
        //styleRel.get
        if (styleRel != null) {
            val part = zip.getPart(styleRel.targetURI)
            if (part != null) {
                val saxreader = SAXReader()
                val input: InputStream = part.inputStream
                val doc = saxreader.read(input)
                val root = doc!!.rootElement
                val styles = root!!.childElements("style")

                //docDefaults
                val docDefaults = root!!.element("docDefaults")
                if (docDefaults != null) {
                    val style = Style()
                    // id
                    styleStrID["docDefaults"] = styleID
                    style.setId(styleID)
                    styleID++

                    // type
                    style.setType(0.toByte())
                    style.setName("docDefaults")

                    // paragraph attribute set
                    val pPr = docDefaults.element("pPrDefault")?.element("pPr")
                    if (pPr != null) {
                        processParaAttribute(pPr, style.getAttrbuteSet()!!, 0)
                    }

                    // character attribute set
                    val rPr = docDefaults.element("rPrDefault")?.element("rPr")
                    if (rPr != null) {
                        processRunAttribute(rPr, style.getAttrbuteSet()!!)
                    }

                    StyleManage.instance().addStyle(style)
                }

                for (styleEle in styles) {
                    if (abortReader) {
                        break
                    }
                    if ("table" == styleEle.attributeValue("type")) {
                        styleEle.attributeValue("styleId")?.let { id ->
                            val tblPr = styleEle.element("tblPr")
                            tableStyleBorders[id] = parseBorders(tblPr?.element("tblBorders"))
                            tableStyleCellMar[id] = parseCellMargins(tblPr?.element("tblCellMar"))
                        }
                        val tlb = styleEle.element("tblStylePr")
                        if (tlb != null && "firstRow" == tlb.attributeValue("type")) {
                            val fillVal = tlb.element("tcPr")?.element("shd")?.attributeValue("fill")
                            if (fillVal != null && styleEle.attributeValue("styleId") != null) {
                                tableStyle[styleEle.attributeValue("styleId")!!] = parseHexColor(fillVal, Color.WHITE)
                            }
                        }
                    }
                    val style = Style()
                    // id
                    var v: String? = styleEle.attributeValue("styleId")
                    if (v != null) {
                        //style.setId(Integer.parseInt(val, 16));
                        val a = styleStrID[v]
                        if (a == null) {
                            styleStrID[v] = styleID
                            style.setId(styleID)
                            styleID++
                        } else {
                            style.setId(a)
                        }
                    }
                    // type
                    v = styleEle.attributeValue("type")
                    style.setType((if (v == "paragraph") 0 else 1).toByte())
                    // name
                    var temp = styleEle.element("name")
                    if (temp != null) {
                        style.setName(temp.attributeValue("val"))
                    }
                    // base id
                    temp = styleEle.element("basedOn")
                    if (temp != null) {
                        // id
                        v = temp.attributeValue("val")
                        if (v != null) {
                            val a = styleStrID[v]
                            if (a == null) {
                                styleStrID[v] = styleID
                                style.setBaseID(styleID)
                                styleID++
                            } else {
                                style.setBaseID(a)
                            }
                        }
                    } else if ("1" == styleEle.attributeValue("default")) {
                        // ID 0 belongs to the first real style when docDefaults is absent.
                        // Only inherit document defaults when that style was actually created.
                        styleStrID["docDefaults"]?.let { style.setBaseID(it) }
                    }

                    // paragraph attribute set
                    temp = styleEle.element("pPr")
                    if (temp != null) {
                        processParaAttribute(temp, style.getAttrbuteSet()!!, 0)
                    }
                    // character attribute set
                    temp = styleEle.element("rPr")
                    if (temp != null) {
                        processRunAttribute(temp, style.getAttrbuteSet()!!)
                    }
                    if ("paragraph" == styleEle.attributeValue("type") && "1" == styleEle.attributeValue("default")) {
                        defaultParaStyleID = style.getId()
                    }
                    StyleManage.instance().addStyle(style)
                }

                input.close()
            }
        }
    }

    /**
     *
     */
    @Throws(Exception::class)
    private fun processBulletNumber() {
        val styleRel = mainPart.getRelationshipsByType(PackageRelationshipTypes.BULLET_NUMBER_PART).getRelationship(0)
        //styleRel.get
        if (styleRel != null) {
            val part = zip.getPart(styleRel.targetURI)
            if (part != null) {
                val saxreader = SAXReader()
                val input: InputStream = part.inputStream
                val doc = saxreader.read(input)
                val root = doc!!.rootElement
                for (num in root!!.childElements("num")) {
                    val temp = num.element("abstractNumId")
                    if (temp != null) {
                        val v = temp.attributeValue("val")
                        val numID = num.attributeValue("numId")
                        if (numID != null && v != null) {
                            bulletNumbersID[numID] = v
                        }
                    }
                }
                // bullet and number object
                for (num in root!!.childElements("abstractNum")) {
                    val listData = ListData()
                    // ID
                    val abstractNumId = num.attributeValue("abstractNumId")
                    if (abstractNumId != null) {
                        listData.listID = abstractNumId.toIntSafe(0)
                    }
                    // list level
                    val levels = num.childElements("lvl")
                    val len = levels.size
                    val listLevels = Array(len) { ListLevel() }
                    listData.simpleList = len.toByte()
                    for (i in 0 until len) {
                        processListLevel(listLevels[i], levels[i])
                    }
                    listData.levels = listLevels
                    // simple list
                    listData.simpleList = len.toByte()
                    if (len == 0) {
                        val linkID = num.element("numStyleLink")?.attributeValue("val")
                        if (linkID != null) {
                            val a = styleStrID[linkID]
                            if (a != null) {
                                listData.linkStyleID = a.toShort()
                                // change style
                                val style = StyleManage.instance().getStyle(a) ?: continue
                                val styleListNumID = am.getParaListID(style!!.getAttrbuteSet()!!)
                                if (styleListNumID >= 0) {
                                    bulletNumbersID[styleListNumID.toString()]?.let {
                                        am.setParaListID(style.getAttrbuteSet()!!, it.toIntSafe(0))
                                    }
                                }
                            }
                        }
                    }
                    //
                    control!!.getSysKit().getListManage().putListData(listData.listID, listData)
                }
                input.close()
            }
        }
    }

    /**
     *
     */
    private fun processListLevel(level: ListLevel, elem: Element) {
        // start at
        var v: String?
        var temp = elem.element("start")
        if (temp != null) {
            level.startAt = temp.attributeValue("val").toIntSafe(1)
        }
        // horizontal alignment;
        temp = elem.element("lvlJc")
        if (temp != null) {
            when (temp.attributeValue("val")) {
                "left" -> level.align = WPAttrConstant.PARA_HOR_ALIGN_LEFT
                "center" -> level.align = WPAttrConstant.PARA_HOR_ALIGN_CENTER
                "right" -> level.align = WPAttrConstant.PARA_HOR_ALIGN_RIGHT
            }
        }
        // follow char
        temp = elem.element("suff")
        if (temp != null) {
            when (temp.attributeValue("val")) {
                "space" -> level.followChar = 1.toByte()
                "nothing" -> level.followChar = 2.toByte()
            }
        }
        // number format
        temp = elem.element("numFmt")
        if (temp != null) {
            level.numberFormat = convertedNumberFormat(temp.attributeValue("val"))
        }
        // number text
        temp = elem.element("lvlText")
        if (temp != null) {
            val sb = StringBuilder()
            val text = temp.attributeValue("val")
            var i = 0
            while (i < text!!.length) {
                val c = text!![i]
                if (c == '%') {
                    val a = text!!.substring(i + 1, minOf(i + 2, text!!.length)).toIntSafe(1)
                    sb.append((a - 1).toChar())
                    i++
                } else {
                    val code = c.code
                    val mapped = when {
                        code == 0xF06C -> '\u25CF'
                        code == 0xF06E -> '\u25A0'
                        code == 0xF075 -> '\u25C6'
                        code == 0xF0FC -> '\u221A'
                        code == 0xF0D8 -> '\u2605'
                        code == 0xF0B2 -> '\u2606'
                        code >= 0xF000 -> '\u25CF'
                        else -> c
                    }
                    sb.append(mapped)
                }
                i++
            }
            level.numberText = sb.toString().toCharArray()
        }
        // indent
        temp = elem.element("pPr")?.element("ind")
        if (temp != null) {
            // special indent, default 21 POINT
            v = temp.attributeValue("hanging")
            if (v != null) {
                level.specialIndent = -v.toIntSafe(0)
            }
            // left text indent, default 21 point * level
            v = temp.attributeValue("left")
            if (v != null) {
                level.textIndent = v.toIntSafe(0)
            }
        }
    }

    /**
     * = 0    decimal                           1、2、3、...
     * = 1    upperRoman                        I、II、III、...
     * = 2    lowerRoman                        i、ii、iii、...
     * = 3    upperLetter                       A、B、C、...
     * = 4    lowerLetter                       a、b、c、...
     * = 39   chineseCountingThousand           一、二、三、...
     * = 38   chineseLegalSimplified            壹、贰、叁、...
     * = 30   ideographTraditional              甲、乙、丙、...
     * = 31   ideographZodiac                   子、丑、寅、...
     * = 5    ordinal                           1st、2st、3st、...
     * = 6    cardinalText                      one、two、three、...
     * = 7    ordinalText                       First、Second、Third、...
     * = 22   decimalZero                       01、02、03、...
     */
    private fun convertedNumberFormat(numFormat: String?): Int {
        val f = numFormat ?: return 0
        return when {
            f.equals("decimal", ignoreCase = true) -> 0
            f.equals("upperRoman", ignoreCase = true) -> 1
            f.equals("lowerRoman", ignoreCase = true) -> 2
            f.equals("upperLetter", ignoreCase = true) -> 3
            f.equals("lowerLetter", ignoreCase = true) -> 4
            f.equals("chineseCountingThousand", ignoreCase = true) -> 39
            f.equals("chineseLegalSimplified", ignoreCase = true) -> 38
            f.equals("ideographTraditional", ignoreCase = true) -> 30
            f.equals("ideographZodiac", ignoreCase = true) -> 31
            f.equals("ordinal", ignoreCase = true) -> 5
            f.equals("cardinalText", ignoreCase = true) -> 6
            f.equals("ordinalText", ignoreCase = true) -> 7
            f.equals("decimalZero", ignoreCase = true) -> 22
            else -> 0
        }
    }

    /**
     * reader
     */
    @Throws(Exception::class)
    private fun processHeaderAndFooter(hfRel: PackageRelationship?, isHeader: Boolean) {
        if (hfRel != null) {
            val part = zip.getPart(hfRel.targetURI)
            hfPart = part
            if (part != null) {
                isProcessHF = true
                offset = if (isHeader) WPModelConstant.HEADER else WPModelConstant.FOOTER
                val saxreader = SAXReader()
                val input: InputStream = part.inputStream
                val doc = saxreader.read(input)
                val root = doc!!.rootElement
                val paras = root!!.childElements()

                val hfElem = HFElement(
                    if (isHeader) WPModelConstant.HEADER_ELEMENT else WPModelConstant.FOOTER_ELEMENT,
                    WPModelConstant.HF_ODD
                )
                hfElem.setStartOffset(offset)

                processParagraphs(paras)

                hfElem.setEndOffset(offset)
                document.appendElement(hfElem, offset)

                input.close()
                isProcessHF = false
            }
        }
    }

    /**
     *
     */
    @Throws(Exception::class)
    private fun processSection(body: Element) {
        // 建立章节
        // 开始Offset
        section.setStartOffset(0)
        // 属性 (mặc định giấy A4 – đã comment trong bản gốc)
        /*am.setPageWidth(attr, 11906)
        am.setPageHeight(attr, 16838)
        am.setPageMarginLeft(attr, 1800)
        am.setPageMarginRight(attr, 1800)
        am.setPageMarginTop(attr, 1440)
        am.setPageMarginBottom(attr, 1440)*/
        // 结束Offset
        section.setEndOffset(offset)
        document.appendSection(section)
        logD("DOCREADER " + "processSection offset = $offset")
        processSectionAttribute(body.element("sectPr"))
    }

    /**
     *
     */
    private fun processSectionAttribute(sectPr: Element?) {
        if (sectPr == null || isProcessSectionAttribute) {
            return
        }
        val attr = section.getAttribute()!!
        // 纸张
        val pgSz = sectPr.element("pgSz")
        if (pgSz != null) {
            val w = pgSz.attributeValue("w").toIntSafe(0)
            val h = pgSz.attributeValue("h").toIntSafe(0)
            // 宽度
            am.setPageWidth(attr, w)
            // 高度
            am.setPageHeight(attr, h)
            logD("DOCX reader " + "width = $w")
            logD("DOCX reader " + "height = $h")
        } else {
            am.setPageWidth(attr, 816)
            // 高度
            am.setPageHeight(attr, 1056)
//            am.setPageWidth(attr, 12240)
//            am.setPageHeight(attr, 15840)
        }
        // 边距
        val margin = sectPr.element("pgMar")
        if (margin != null) {
            // 左边距
            margin.attributeValue("left")?.let {
                am.setPageMarginLeft(attr, it.toIntSafe(0))
                logD("setPageMarginLeft " + "" + it.toIntSafe(0))
            }
            // 右边距
            margin.attributeValue("right")?.let {
                am.setPageMarginRight(attr, it.toIntSafe(0))
                logD("setPageMarginRight " + "" + it.toIntSafe(0))
            }
            // 上边距
            margin.attributeValue("top")?.let {
                am.setPageMarginTop(attr, it.toIntSafe(0))
                logD("setPageMarginTop " + "" + it.toIntSafe(0))
            }
            // 下边距
            margin.attributeValue("bottom")?.let {
                am.setPageMarginBottom(attr, it.toIntSafe(0))
                logD("setPageMarginBottom " + "" + it.toIntSafe(0))
            }
            // 页眉边距
            margin.attributeValue("header")?.let {
                am.setPageHeaderMargin(attr, it.toIntSafe(0))
            }
            // 页脚边距
            margin.attributeValue("footer")?.let {
                am.setPageFooterMargin(attr, it.toIntSafe(0))
            }
        }

        // page border
        val borderElem = sectPr.element("pgBorders")
        if (borderElem != null) {
            val borders = Borders()
            if ("page" == borderElem.attributeValue("offsetFrom")) {
                borders.onType = 1.toByte()
            }
            // topBorder
            borderElem.element("top")?.let {
                val border = Border()
                processBorder(it, border)
                borders.topBorder = border
            }
            // leftBorder
            borderElem.element("left")?.let {
                val border = Border()
                processBorder(it, border)
                borders.leftBorder = border
            }
            // rightBorder
            borderElem.element("right")?.let {
                val border = Border()
                processBorder(it, border)
                borders.rightBorder = border
            }
            // bottomBorder
            borderElem.element("bottom")?.let {
                val border = Border()
                processBorder(it, border)
                borders.bottomBorder = border
            }
            am.setPageBorder(attr, control!!.getSysKit().getBordersManage().addBorders(borders))
        }
        //line pitch
        val docGrid = sectPr.element("docGrid")
        if (docGrid != null) {
            val type = docGrid.attributeValue("type")
            if ("lines" == type || "linesAndChars" == type || "snapToChars" == type) {
                val linePitch = docGrid.attributeValue("linePitch")
                if (!linePitch.isNullOrEmpty()) {
                    am.setPageLinePitch(attr, linePitch.toIntSafe(0))
                    for (i in 0 until textboxIndex.toInt()) {
                        val textboxSec: IElement = document.getTextboxSectionElementForIndex(i) ?: continue
                        val secElemAttr = section.getAttribute()!!
                        am.setPageLinePitch(
                            textboxSec.getAttribute()!!,
                            am.getPageLinePitch(secElemAttr)
                        )
                    }
                }
            }
        }
        // header
        val a = offset
        //
        val headers = sectPr.childElements("headerReference")
        if (headers.isNotEmpty()) {
            var id: String? = ""
            if (headers.size == 1) {
                id = headers[0].attributeValue("id")
            } else {
                for (header in headers) {
                    if ("default" == header.attributeValue("type")) {
                        id = header.attributeValue("id")
                        break
                    }
                }
            }
            if (!id.isNullOrEmpty()) {
                try {
                    val hfRel = mainPart.getRelationshipsByType(PackageRelationshipTypes.HEADER_PART)
                        .getRelationshipByID(id)
                    if (hfRel != null) {
                        processHeaderAndFooter(hfRel, true)
                    }
                } catch (e: Exception) {
                    logD("writerLog " + "1")
                    control!!.getSysKit().getErrorKit().writerLog(e, true)
                }
            }
        }

        // footer
        val footers = sectPr.childElements("footerReference")
        if (footers.isNotEmpty()) {
            var id: String? = ""
            if (footers.size == 1) {
                id = footers[0].attributeValue("id")
            } else {
                for (footer in footers) {
                    if ("default" == footer.attributeValue("type")) {
                        id = footer.attributeValue("id")
                        break
                    }
                }
            }
            if (!id.isNullOrEmpty()) {
                try {
                    val hfRel = mainPart.getRelationshipsByType(PackageRelationshipTypes.FOOTER_PART)
                        .getRelationshipByID(id)
                    if (hfRel != null) {
                        processHeaderAndFooter(hfRel, false)
                    }
                } catch (e: Exception) {
                    logD("writerLog " + "2")
                    control!!.getSysKit().getErrorKit().writerLog(e, true)
                }
            }
        }
        offset = a
        isProcessSectionAttribute = true
    }

    /**
     *
     */
    private fun processBorder(borElem: Element, border: Border) {
        var value: String? = borElem.attributeValue("color")
        if (value == null || "auto" == value) {
            border.color = Color.BLACK
        } else {
            border.color = parseHexColor(value, Color.BLACK)
        }
        value = borElem.attributeValue("space")
        if (value == null) {
            border.space = 32.toShort()
        } else {
            border.space = (value.toIntSafe(0) * MainConstant.POINT_TO_PIXEL).toInt().toShort()
        }
    }

    // ===================== TABLE =====================

    /**
     *
     */
    private fun processTable(table: Element) {
        val tableElem = TableElement()
        tableElem.setStartOffset(offset)

        val tblPr = table.element("tblPr")
        var tableStyleId = ""
        if (tblPr != null) {
            processTableAttribute(tblPr, tableElem.getAttribute()!!)
            val tStyleId = tblPr.element("tblStyle")
            if (tStyleId != null) {
                tableStyleId = tStyleId.attributeValue("val") ?: ""
            }
        }
        // direct table properties override the table style, side by side
        tblBorders = (tableStyleBorders[tableStyleId] ?: emptyMap()) + parseBorders(tblPr?.element("tblBorders"))
        tblCellMar = (tableStyleCellMar[tableStyleId] ?: emptyMap()) + parseCellMargins(tblPr?.element("tblCellMar"))
        // table grid column width
        tableGridCol.clear()
        val tblGrid = table.element("tblGrid")
        if (tblGrid != null) {
            val grids = tblGrid.childElements("gridCol")
            for (i in grids.indices) {
                tableGridCol[i] = grids[i].attributeValue("w").toIntSafe(0)
            }
        }
        tblGridCount = tableGridCol.size

        val rows = table.childElements("tr")
        tblRowCount = rows.size
        for ((rowIndex, row) in rows.withIndex()) {
            processRow(row, tableElem, rowIndex, tableStyleId)
        }

        tableElem.setEndOffset(offset)
        document.appendParagraph(tableElem, offset)
    }

    /**
     *
     */
    private fun processTableAttribute(tblPr: Element, attr: IAttributeSet) {
        // table horizontal alignment
        tblPr.element("jc")?.let {
            when (it.attributeValue("val")) {
                "center" -> am.setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_CENTER.toInt())
                "right" -> am.setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_RIGHT.toInt())
            }
        }
        // table left indent
        tblPr.element("tblInd")?.attributeValue("w")?.let {
            am.setParaIndentLeft(attr, it.toIntSafe(0))
        }
    }

    /**
     *
     */
    private fun processRow(row: Element, tableElem: TableElement, rowIndex: Int, tableStyleId: String) {
        val rowElem = RowElement()
        rowElem.setStartOffset(offset)

        row.element("trPr")?.let { processRowAttribute(it, rowElem.getAttribute()!!) }

        var i = row.element("trPr")?.element("gridBefore")?.attributeValue("val").toIntSafe(0)
        for (cell in row.childElements("tc")) {
            i += processCell(cell, rowElem, i, rowIndex, tableStyleId)
        }

        rowElem.setEndOffset(offset)
        tableElem.appendRow(rowElem)
    }

    /**
     *
     */
    private fun processRowAttribute(trPr: Element, attr: IAttributeSet) {
        // 行高
        trPr.element("trHeight")?.let {
            am.setTableRowHeight(attr, it.attributeValue("val").toIntSafe(0))
        }
        // header row, repeated on each page
        trPr.element("tblHeader")?.let {
            if (isOnOff(it)) am.setTableHeaderRow(attr, true)
        }
    }

    /**
     *
     */
    private fun processCell(cell: Element, row: RowElement, gridColIndex: Int, rowIndex: Int, tableStyleId: String): Int {
        val cellElem = CellElement()
        cellElem.setStartOffset(offset)
        val firstRow = rowIndex == 0

        var gridSpan = 0
        val tcPr = cell.element("tcPr")
        if (tcPr != null) {
            gridSpan = processCellAttribute(tcPr, cellElem.getAttribute()!!, gridColIndex)
        }
        processCellBordersAndMargins(tcPr, cellElem.getAttribute()!!, rowIndex, gridColIndex, maxOf(gridSpan, 1))

        processParagraphs_Table(cell.childElements(), 1)

        cellElem.setEndOffset(offset)
        row.appendCell(cellElem)
        // cell background from the table style, unless the cell has its own shading
        if (firstRow && !am.hasAttribute(cellElem.getAttribute(), AttrIDConstant.PAGE_BACKGROUND_COLOR_ID)) {
            tableStyle[tableStyleId]?.let { am.setTableCellBackground(cellElem.getAttribute()!!, it) }
        }
        // 水平合并单元格
        for (i in 1 until gridSpan) {
            row.appendCell(CellElement())
        }
        return gridSpan
    }

    /**
     *
     */
    private fun processCellAttribute(tcPr: Element, attr: IAttributeSet, gridColIndex: Int): Int {
        var gridSpan = 1
        tcPr.element("gridSpan")?.let { gridSpan = it.attributeValue("val").toIntSafe(1) }

        // 宽度
        val tcW = tcPr.element("tcW")
        if (tcW != null) {
            var w = tcW.attributeValue("w").toIntSafe(0)
            val type = tcW.attributeValue("type")
            if ("pct" == type || "auto" == type) {
                var tW = 0
                for (i in gridColIndex until gridColIndex + gridSpan) {
                    tW += tableGridCol[i] ?: 0
                }
                w = maxOf(tW, w)
            }
            am.setTableCellWidth(attr, w)
        } else {
            var tW = 0
            for (i in gridColIndex until gridColIndex + gridSpan) {
                tW += tableGridCol[i] ?: 0
            }
            am.setTableCellWidth(attr, tW)
        }

        // 合并单元格
        tcPr.element("vMerge")?.let {
            am.setTableVerMerged(attr, true)
            if (it.attributeValue("val") != null) {
                am.setTableVerFirstMerged(attr, true)
            }
        }
        // cell shading
        tcPr.element("shd")?.attributeValue("fill")?.let { fill ->
            if (!fill.equals("auto", true)) am.setTableCellBackground(attr, parseHexColor(fill, Color.WHITE))
        }
        tcPr.element("vAlign")?.let {
            when (it.attributeValue("val")) {
                // vertical center alignment
                "center" -> am.setTableCellVerAlign(attr, WPAttrConstant.PARA_VER_ALIGN_CENTER.toInt())
                "bottom" -> am.setTableCellVerAlign(attr, WPAttrConstant.PARA_VER_ALIGN_BOTTOM.toInt())
            }
        }
        return gridSpan
    }

    /**
     * Resolves the four borders of a cell the way Word does: the cell's tcBorders win, then the
     * table's outer border on the table edge or its insideH/insideV border inside. Every side is
     * written (0 = no line) so [com.wxiwei.office.wp.view.TableView] draws exactly these.
     * Margins: tcMar, then tblCellMar, then Word's default of 108 twips left/right.
     */
    private fun processCellBordersAndMargins(tcPr: Element?, attr: IAttributeSet, rowIndex: Int, gridColIndex: Int, gridSpan: Int) {
        val own = parseBorders(tcPr?.element("tcBorders"))
        val firstRow = rowIndex == 0
        val lastRow = rowIndex >= tblRowCount - 1
        val firstCol = gridColIndex == 0
        val lastCol = tblGridCount == 0 || gridColIndex + gridSpan >= tblGridCount
        fun side(name: String, outer: Boolean, inside: String): DocxBorder =
            own[name] ?: tblBorders[if (outer) name else inside] ?: DocxBorder.NONE
        val top = side("top", firstRow, "insideH")
        val bottom = side("bottom", lastRow, "insideH")
        val left = side("left", firstCol, "insideV")
        val right = side("right", lastCol, "insideV")
        am.setTableTopBorder(attr, top.eighths); am.setTableTopBorderColor(attr, top.color)
        am.setTableBottomBorder(attr, bottom.eighths); am.setTableBottomBorderColor(attr, bottom.color)
        am.setTableLeftBorder(attr, left.eighths); am.setTableLeftBorderColor(attr, left.color)
        am.setTableRightBorder(attr, right.eighths); am.setTableRightBorderColor(attr, right.color)

        val mar = tblCellMar + parseCellMargins(tcPr?.element("tcMar"))
        am.setTableTopMargin(attr, mar["top"] ?: 0)
        am.setTableBottomMargin(attr, mar["bottom"] ?: 0)
        am.setTableLeftMargin(attr, mar["left"] ?: DEFAULT_CELL_MARGIN_LR)
        am.setTableRightMargin(attr, mar["right"] ?: DEFAULT_CELL_MARGIN_LR)
    }

    /** tblBorders / tcBorders / pBdr children by side; start/end are the bidi-neutral left/right. */
    private fun parseBorders(borders: Element?): Map<String, DocxBorder> {
        if (borders == null) return emptyMap()
        val result = HashMap<String, DocxBorder>()
        for (e in borders.childElements()) {
            val side = when (e.name) {
                "start" -> "left"
                "end" -> "right"
                else -> e.name ?: continue
            }
            result[side] = parseBorder(e)
        }
        return result
    }

    private fun parseBorder(e: Element): DocxBorder {
        val v = e.attributeValue("val")
        if (v == null || v == "nil" || v == "none") return DocxBorder.NONE
        val color = e.attributeValue("color")
        return DocxBorder(
            // sz is in eighths of a point; 0 is still a visible hairline
            maxOf(1, e.attributeValue("sz").toIntSafe(4)),
            if (color == null || color.equals("auto", true)) Color.BLACK else parseHexColor(color, Color.BLACK),
            e.attributeValue("space").toIntSafe(0),
        )
    }

    /** tblCellMar / tcMar in twips by side (type dxa; other types are rare and ignored). */
    private fun parseCellMargins(mar: Element?): Map<String, Int> {
        if (mar == null) return emptyMap()
        val result = HashMap<String, Int>()
        for (e in mar.childElements()) {
            val type = e.attributeValue("type")
            if (type != null && type != "dxa") continue
            val side = when (e.name) {
                "start" -> "left"
                "end" -> "right"
                else -> e.name ?: continue
            }
            result[side] = e.attributeValue("w").toIntSafe(0)
        }
        return result
    }

    private fun processParagraphs_Table(elems: List<Element>, level: Int) {
        for (elem in elems) {
            when (elem.name) {
                // FIX: không gán đè elem (tránh NPE)
                "sdt" -> elem.element("sdtContent")?.let { processParagraphs_Table(it.childElements(), level) }
                "p" -> processParagraph(elem, level)
                "tbl" -> processEmbeddedTable(elem, level)
            }
        }
    }

    /**
     *
     */
    private fun processEmbeddedTable(table: Element, level: Int) {
        for (row in table.childElements("tr")) {
            for (cell in row.childElements("tc")) {
                processParagraphs_Table(cell.childElements(), level)
            }
        }
    }

    // ===================== PARAGRAPH =====================

    /**
     *
     */
    private fun processParagraph(para: Element, level: Int) {
        val paraElem = ParagraphElement()
        val t = offset
        paraElem.setStartOffset(offset)
        // 段落属性
        processParaAttribute(para.element("pPr"), paraElem.getAttribute()!!, level)
        applyDefaultParaStyle(paraElem.getAttribute()!!)
        // 处理leaf
        processRun(para, paraElem, true)

        paraElem.setEndOffset(offset)
        if (editMain()) editParas[para]?.let { editMap.addParagraph(it, t, offset) }
        if (offset > t) {
            document.appendParagraph(paraElem, offset)
        }
    }

    /** A paragraph without pStyle uses the default paragraph style, which itself inherits docDefaults. */
    private fun applyDefaultParaStyle(attr: IAttributeSet) {
        if (defaultParaStyleID < 0) return
        val current = am.getParaStyleID(attr)
        if (current < 0 || current == styleStrID["docDefaults"]) {
            am.setParaStyleID(attr, defaultParaStyleID)
        }
    }

    /**
     * Cỡ chữ mặc định của docDefaults.
     * TỐI ƯU: bản Java tra style + try/catch ở MỖI đoạn văn (và ném NPE khi thiếu docDefaults).
     * Ở đây chỉ tra 1 lần, cache lại khi đã tìm thấy style.
     */
    private var cachedDefaultFontSize = -1

    private fun defaultFontSize(): Int {
        if (cachedDefaultFontSize > 0) return cachedDefaultFontSize
        val id = styleStrID["docDefaults"] ?: return 12
        val style = StyleManage.instance().getStyle(id) ?: return 12
        val defaultAttr = style.getAttrbuteSet() ?: return 12
        cachedDefaultFontSize = am.getFontSize(defaultAttr, defaultAttr)
        return cachedDefaultFontSize
    }

    /**
     * @param pPr
     * @param attr
     */
    private fun processParaAttribute(pPr: Element?, attr: IAttributeSet, level: Int) {
        if (level > 0) {
            am.setParaLevel(attr, level)
        }
        if (pPr == null) {
            return
        }

        // 样式
        var temp = pPr.element("pStyle")
        if (temp != null) {
            val v = temp.attributeValue("val")
            if (!v.isNullOrEmpty()) {
                // Java cũ: unboxing null => NPE khi style không tồn tại
                styleStrID[v]?.let { am.setParaStyleID(attr, it) }
            }
        } else {
            am.setParaStyleID(attr, 0)
        }

        // 段前段后
        temp = pPr.element("spacing")
        if (temp != null) {
            processParaSpacing(temp, attr)
        }
        // 左右缩进
        temp = pPr.element("ind")
        if (temp != null) {
            processParaIndent(temp, attr)
        }
        // 对齐方式
        temp = pPr.element("jc")
        if (temp != null) {
            when (temp.attributeValue("val")) {
                "left", "start" -> am.setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_LEFT.toInt())
                "both", "distribute" -> am.setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_JUSTIFIED.toInt())
                "center" -> am.setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_CENTER.toInt())
                "right", "end" -> am.setParaHorizontalAlign(attr, WPAttrConstant.PARA_HOR_ALIGN_RIGHT.toInt())
            }
        }

        // shading and borders
        pPr.element("shd")?.attributeValue("fill")?.let { fill ->
            if (!fill.equals("auto", true)) am.setParaShading(attr, parseHexColor(fill, Color.WHITE))
        }
        pPr.element("pBdr")?.let { pBdr ->
            val borders = parseBorders(pBdr)
            listOf("top", "bottom", "left", "right").forEachIndexed { i, side ->
                borders[side]?.let { am.setParaBorder(attr, i, it.eighths, it.color, it.space) }
            }
        }

        // bullet and number
        temp = pPr.element("numPr")
        if (temp != null) {
            // level
            temp.element("ilvl")?.let {
                am.setParaListLevel(attr, it.attributeValue("val").toIntSafe(0))
            }
            // list ID
            val v = temp.element("numId")?.attributeValue("val")
            if (v != null) {
                bulletNumbersID[v]?.let { am.setParaListID(attr, it.toIntSafe(0)) }
            }
        } else {
            //check from paragraph style
            val style = StyleManage.instance().getStyle(am.getParaStyleID(attr))
            if (style != null) {
                var paraListLevel = am.getParaListLevel(style.getAttrbuteSet())
                val paraListID = am.getParaListID(style.getAttrbuteSet())
                if (paraListID > -1) {
                    if (paraListLevel < 0) {
                        paraListLevel = 0
                    }
                    am.setParaListID(attr, paraListID)
                }
                if (paraListLevel > -1) {
                    am.setParaListLevel(attr, paraListLevel)
                }
            }
        }

        temp = pPr.element("tabs")
        if (temp != null) {
            for (tab in temp.childElements("tab")) {
                if ("clear" == tab.attributeValue("val")) {
                    val pos = tab.attributeValue("pos")
                    if (!pos.isNullOrEmpty()) {
                        am.setParaTabsClearPostion(attr, pos.toIntSafe(0))
                    }
                }
            }
        }
        if (!isProcessSectionAttribute) {
            processSectionAttribute(pPr.element("sectPr"))
        }
    }

    /** 段前段后 + 行距 (tách từ processParaAttribute cho dễ đọc) */
    private fun processParaSpacing(spacing: Element, attr: IAttributeSet) {
        val lineStr = spacing.attributeValue("line")
        if (lineStr != null) {
            // 行单位, 默认行单位是12磅
            var lineUnit = lineStr.toIntSafe(0)
            if (lineUnit == 0) lineUnit = 240
            // 段前
            var v = spacing.attributeValue("beforeLines")
            if (!v.isNullOrEmpty()) {
                am.setParaBefore(attr, (v.toIntSafe(0) / 100f * lineUnit).toInt())
            }
            if (v == null) {
                v = spacing.attributeValue("before")
                if (!v.isNullOrEmpty()) {
                    am.setParaBefore(attr, v.toIntSafe(0))
                }
            }
            // 段后
            v = spacing.attributeValue("afterLines")
            if (!v.isNullOrEmpty()) {
                am.setParaAfter(attr, (v.toIntSafe(0) / 100f * lineUnit).toInt())
            }
            if (v == null) {
                v = spacing.attributeValue("after")
                if (!v.isNullOrEmpty()) {
                    am.setParaAfter(attr, v.toIntSafe(0))
                }
            }
        } else {
            // 非单位
            // 段前
            var v = spacing.attributeValue("before")
            if (!v.isNullOrEmpty()) {
                am.setParaBefore(attr, v.toIntSafe(0))
            } else {
                v = spacing.attributeValue("beforeLines")
                if (!v.isNullOrEmpty()) {
                    am.setParaBefore(attr, (v.toIntSafe(0) / 100f * 240).toInt())
                }
            }
            // 段后
            v = spacing.attributeValue("after")
            if (!v.isNullOrEmpty()) {
                am.setParaAfter(attr, v.toIntSafe(0))
            } else {
                v = spacing.attributeValue("afterLines")
                if (!v.isNullOrEmpty()) {
                    am.setParaAfter(attr, (v.toIntSafe(0) / 100f * 240).toInt())
                }
            }
        }
        // 行距
        val lineSpace = lineStr.toFloatSafe(0f)
        when (spacing.attributeValue("lineRule")) {
            // 多倍行距
            "auto" -> {
                am.setParaLineSpaceType(attr, WPAttrConstant.LINE_SAPCE_MULTIPLE.toInt())
                if (lineSpace != 0f) am.setParaLineSpace(attr, lineSpace / 240f)
            }
            // 最小值
            "atLeast" -> {
                am.setParaLineSpaceType(attr, WPAttrConstant.LINE_SAPCE_LEAST.toInt())
                if (lineSpace != 0f) am.setParaLineSpace(attr, lineSpace)
            }
            // 固定值
            "exact" -> {
                am.setParaLineSpaceType(attr, WPAttrConstant.LINE_SPACE_EXACTLY.toInt())
                if (lineSpace != 0f) am.setParaLineSpace(attr, -lineSpace)
            }
            else -> {
                // No lineRule means "auto" in OOXML: a multiple of single spacing, like the "auto" case.
                // It was stored negated, which made every line height negative and collapsed tables to zero height.
                am.setParaLineSpaceType(attr, WPAttrConstant.LINE_SAPCE_MULTIPLE.toInt())
                if (lineSpace != 0f) am.setParaLineSpace(attr, lineSpace / 240f)
            }
        }
    }

    /** 左右缩进 / 首行 / 悬挂 (tách từ processParaAttribute) */
    private fun processParaIndent(ind: Element, attr: IAttributeSet) {
        var left = 0
        val fontSize = defaultFontSize()

        // 左缩进
        var v = ind.attributeValue("leftChars")
        if (!v.isNullOrEmpty() && v != "0") {
            val leftChars = v.toIntSafe(0) / 100f
            am.setParaIndentLeft(attr, Math.round(fontSize * leftChars * MainConstant.POINT_TO_TWIPS))
        } else {
            v = ind.attributeValue("left")
            if (!v.isNullOrEmpty()) {
                left = v.toIntSafe(0)
                am.setParaIndentLeft(attr, left)
            }
        }

        // 右缩进
        v = ind.attributeValue("rightChars")
        if (!v.isNullOrEmpty() && v != "0") {
            val rightChars = v.toIntSafe(0) / 100f
            am.setParaIndentRight(attr, Math.round(fontSize * rightChars * MainConstant.POINT_TO_TWIPS))
        } else {
            v = ind.attributeValue("right")
            if (!v.isNullOrEmpty()) {
                am.setParaIndentRight(attr, v.toIntSafe(0))
            }
        }

        // 首行缩进
        v = ind.attributeValue("firstLineChars")
        if (!v.isNullOrEmpty() && v != "0") {
            val firstLineChars = v.toIntSafe(0) / 100f
            am.setParaSpecialIndent(attr, Math.round(fontSize * firstLineChars * MainConstant.POINT_TO_TWIPS))
        } else {
            v = ind.attributeValue("firstLine")
            if (!v.isNullOrEmpty()) {
                am.setParaSpecialIndent(attr, v.toIntSafe(0))
            }
        }

        // 悬挂缩进
        v = ind.attributeValue("hangingChars")
        if (!v.isNullOrEmpty() && v != "0") {
            val hangingChars = v.toIntSafe(0) / 100f
            val hanging = -Math.round(fontSize * hangingChars * MainConstant.POINT_TO_TWIPS)
            am.setParaSpecialIndent(attr, hanging)
            // 悬挂缩进值也设置到左缩进，左缩进需要减去悬挂缩进
            if (left == 0) {
                //left indent maybe not init, so get it repeatly
                left = am.getParaIndentLeft(attr)
            }
            if (hanging < 0) {
                am.setParaIndentLeft(attr, left + hanging)
            }
        } else {
            v = ind.attributeValue("hanging")
            if (!v.isNullOrEmpty()) {
                val sp = -v.toIntSafe(0)
                am.setParaSpecialIndent(attr, sp)
                if (sp < 0) {
                    am.setParaIndentLeft(attr, left + sp)
                }
            }
        }
    }

    // ===================== RUN =====================

    private fun processRun(para: Element, paraElem: ParagraphElement, addBreakPage: Boolean): Boolean =
        processRun(para, paraElem, (-1).toByte(), addBreakPage)

    /**
     * @param para
     * @param paraElem
     * @param pgNumberType
     * @param addBreakPage
     * @return
     */
    private fun processRun(para: Element, paraElem: ParagraphElement, pgNumberType: Byte, addBreakPage: Boolean): Boolean {
        var leaf: LeafElement? = null
        var hasLeaf = false
        // TỐI ƯU: StringBuilder thay cho String "+=" trong vòng lặp
        val fieldCode = StringBuilder()
        val fieldText = StringBuilder()
        var editFieldRuns = IntArray(0)
        var editFieldCount = 0
        var hasField = false
        var pageBreak = false
        for (runElem in para.childElements()) {
            var run = runElem
            when (run.name) {
                "smartTag" -> {
                    hasLeaf = processRun(run, paraElem, false)
                }
                "hyperlink" -> {
                    leaf = processHyperlinkRun(run, paraElem)
                    hasLeaf = leaf != null
                }
                "bookmarkStart" -> {
                    val v = run.attributeValue("name")
                    if (v != null) {
                        control!!.getSysKit().getBookmarkManage().addBookmark(Bookmark(v, offset, offset))
                    }
                }
                "fldSimple" -> {
                    val instr = run.attributeValue("instr")
                    var pageNumberType: Byte = -1
                    if (instr != null) {
                        if (instr.contains("NUMPAGES")) {
                            pageNumberType = WPModelConstant.PN_TOTAL_PAGES
                        } else if (instr.contains("PAGE")) {
                            pageNumberType = WPModelConstant.PN_PAGE_NUMBER
                        }
                    }
                    hasLeaf = processRun(run, paraElem, pageNumberType, false)
                    leaf = null
                }
                "sdt" -> {
                    val sdtContent = run.element("sdtContent")
                    if (sdtContent != null) {
                        val docPartGallery = run.element("sdtPr")
                            ?.element("docPartObj")
                            ?.element("docPartGallery")
                            ?.attributeValue("val")
                        if (docPartGallery != null) {
                            if (isProcessHF && docPartGallery.contains("Margins")) {
                                return false
                            } else if (docPartGallery.contains("Watermarks")) {
                                isProcessWatermark = true
                            }
                        }
                        hasLeaf = processRun(sdtContent, paraElem, false)
                        leaf = null
                    }
                }
                "ins" -> {
                    hasLeaf = processRun(run, paraElem, false)
                }
                "r" -> {
                    if (editMain()) editObjectRun = run
                    if (editMain() && (hasField || run.element("fldChar") != null)) {
                        editRuns[run]?.let {
                            if (editFieldCount == editFieldRuns.size) editFieldRuns = editFieldRuns.copyOf(maxOf(8, editFieldCount * 2))
                            editFieldRuns[editFieldCount++] = it
                        }
                    }
                    // field
                    val fld = run.element("fldChar")
                    if (fld != null) {
                        when (fld.attributeValue("fldCharType")) {
                            "begin" -> {
                                hasField = true
                                continue
                            }
                            "separate" -> {
                                continue
                            }
                            "end" -> {
                                hasField = false
                                val code = fieldCode.toString()
                                var str: String
                                var encloseType: Byte = -1
                                var pageNumberType: Byte = -1
                                when {
                                    // 圆
                                    code.indexOf('\u25cb') > 0 -> {
                                        str = code.substring(code.indexOf(",") + 1, code.length - 1)
                                        encloseType = WPModelConstant.ENCLOSURE_TYPE_ROUND
                                    }
                                    // 正方形
                                    code.indexOf('\u25a1') > 0 -> {
                                        str = code.substring(code.indexOf(",") + 1, code.length - 1)
                                        encloseType = WPModelConstant.ENCLOSURE_TYPE_SQUARE
                                    }
                                    // 三角形
                                    code.indexOf('\u25b3') > 0 -> {
                                        str = code.substring(code.indexOf(",") + 1, code.length - 1)
                                        encloseType = WPModelConstant.ENCLOSURE_TYPE_TRIANGLE
                                    }
                                    // 菱形
                                    code.indexOf('\u25c7') > 0 -> {
                                        str = code.substring(code.indexOf(",") + 1, code.length - 1)
                                        encloseType = WPModelConstant.ENCLOSURE_TYPE_RHOMBUS
                                    }
                                    // page number property
                                    code.contains("NUMPAGES") -> {
                                        str = fieldText.toString()
                                        pageNumberType = WPModelConstant.PN_TOTAL_PAGES
                                    }
                                    code.contains("PAGE") -> {
                                        str = fieldText.toString()
                                        pageNumberType = WPModelConstant.PN_PAGE_NUMBER
                                    }
                                    else -> str = fieldText.toString()
                                }
                                // WPS writes PAGE/NUMPAGES without a cached result; the leaf text is
                                // only a placeholder, LeafView draws the real number of the page
                                if (str.isEmpty() && pageNumberType > 0 && isProcessHF) {
                                    str = "1"
                                }

                                if (str.isNotEmpty()) {
                                    hasLeaf = true
                                    if (editMain() && editParas.isNotEmpty()) editMap.addLeaf(offset, offset + str.length,
                                        editFieldRuns.copyOf(editFieldCount), str, DocxSourceMap.Kind.FIELD)
                                    val l = LeafElement(str)
                                    leaf = l
                                    // 属性
                                    run.element("rPr")?.let { processRunAttribute(it, l.getAttribute()!!) }

                                    l.setStartOffset(offset)
                                    offset += str.length
                                    l.setEndOffset(offset)

                                    if (encloseType >= 0) {
                                        am.setEncloseChanacterType(l.getAttribute()!!, encloseType.toInt())
                                    } else if (isProcessHF && hfPart != null && pageNumberType > 0) {
                                        am.setFontPageNumberType(l.getAttribute()!!, pageNumberType.toInt())
                                    }
                                    paraElem.appendLeaf(l)
                                }
                                editFieldCount = 0
                                fieldCode.setLength(0)
                                fieldText.setLength(0)
                                continue
                            }
                        }
                    }
                    if (hasField) {
                        val fldCode = run.element("instrText")
                        if (fldCode != null) {
                            fieldCode.append(fldCode.text)
                        } else {
                            fieldText.append(getRunText(run))
                        }
                        continue
                    }

                    //Inline Embedded Object
                    val obj = run.element("object")
                    if (obj != null) {
                        val it = obj.elementIterator()
                        while (it!!.hasNext()) {
                            processAutoShapeForPict(it!!.next() as Element, paraElem, null, 1.0f, 1.0f)
                        }
                        leaf = null
                        continue
                    }

                    //picture and diagram
                    val drawing = run.element("drawing")
                    if (drawing != null) {
                        processPictureAndDiagram(drawing, paraElem)
                        leaf = null
                        continue
                    }

                    //autoshape for 2007
                    val pict = run.element("pict")
                    if (pict != null) {
                        val it = pict.elementIterator()
                        while (it!!.hasNext()) {
                            processAutoShapeForPict(it!!.next() as Element, paraElem, null, 1.0f, 1.0f)
                        }
                        leaf = null
                        continue
                    }

                    //autoshape for 2010
                    val alternateContent = run.element("AlternateContent")
                    if (alternateContent != null) {
                        processAlternateContent(alternateContent, paraElem)
                        leaf = null
                        continue
                    }
                    val rubyBase = run.element("ruby")?.element("rubyBase")
                    if (rubyBase != null) {
                        run = rubyBase.element("r") ?: continue
                    }

                    // FIX search/highlight: lấy đủ text theo thứ tự (nhiều w:t, tab, br...)
                    val str = getRunText(run)
                    if (str.length == 1 && str[0] == '\u000c') {
                        // 分页符
                        pageBreak = true
                    }
                    val len = str.length
                    if (len > 0) {
                        hasLeaf = true
                        recordEdit(offset, str, run, if (para.name == "fldSimple") DocxSourceMap.Kind.FIELD else DocxSourceMap.Kind.TEXT)
                        val l = LeafElement(str)
                        leaf = l
                        // 属性
                        run.element("rPr")?.let { processRunAttribute(it, l.getAttribute()!!) }

                        if (isProcessHF && hfPart != null && pgNumberType > 0) {
                            am.setFontPageNumberType(l.getAttribute()!!, pgNumberType.toInt())
                        }

                        l.setStartOffset(offset)
                        offset += len
                        l.setEndOffset(offset)
                        paraElem.appendLeaf(l)
                        //
                        val code = fieldCode.toString()
                        var linkURL: String? = null
                        if (code.indexOf("mailto") >= 0) {
                            linkURL = code.substring(code.indexOf("mailto"))
                            val index = linkURL.indexOf("\"")
                            if (index > 0) {
                                linkURL = linkURL.substring(0, index)
                            }
                        } else if (code.indexOf("HYPERLINK") >= 0) {
                            linkURL = code.substring(code.indexOf("\"") + 1)
                            val index = linkURL.lastIndexOf("/")
                            if (index > 0) {
                                linkURL = linkURL.substring(0, index)
                            }
                        }
                        if (linkURL != null) {
                            val hyIndex = control!!.getSysKit().getHyperlinkManage().addHyperlink(linkURL, Hyperlink.LINK_URL)
                            if (hyIndex >= 0) {
                                setHyperlinkStyle(l.getAttribute()!!, hyIndex)
                            }
                        }
                        fieldCode.setLength(0)
                        fieldText.setLength(0)
                    }
                }
            }
        }
        // 如果没有 r 元素，说明只有一个回车符的段落
        if (!hasLeaf) {
            recordEdit(offset, "\n", null, DocxSourceMap.Kind.PARA_END)
            val l = LeafElement("\n")
            para.element("pPr")?.element("rPr")?.let { processRunAttribute(it, l.getAttribute()!!) }
            l.setStartOffset(offset)
            offset += 1
            l.setEndOffset(offset)
            paraElem.appendLeaf(l)
            return hasLeaf
        }
        if (addBreakPage && !pageBreak) {
            recordEdit(offset, "\n", null, DocxSourceMap.Kind.PARA_END)
            val last = leaf
            if (last != null) {
                last.setText(last.getText(wpdoc) + "\n")
                offset++
                // FIX: cập nhật endOffset để phạm vi leaf khớp với text (highlight không bị lệch)
                last.setEndOffset(offset)
            } else {
                // FIX: đoạn kết thúc bằng hình/shape/field vẫn cần ký tự xuống dòng,
                // nếu không chữ của đoạn này bị dính với đoạn sau khi tìm kiếm
                val endLeaf = LeafElement("\n")
                endLeaf.setStartOffset(offset)
                offset++
                endLeaf.setEndOffset(offset)
                paraElem.appendLeaf(endLeaf)
            }
        }
        return hasLeaf
    }

    /** Tô màu xanh + gạch chân cho hyperlink (bản Java lặp lại 4 dòng này nhiều chỗ) */
    private fun setHyperlinkStyle(attr: IAttributeSet, hyIndex: Int) {
        am.setFontColor(attr, Color.BLUE)
        am.setFontUnderline(attr, 1)
        am.setFontUnderlineColr(attr, Color.BLUE)
        am.setHyperlinkID(attr, hyIndex)
    }

    /**
     *
     */
    private fun processHyperlinkRun(hyperlink: Element, paraElem: ParagraphElement): LeafElement? {
        var hyRel: PackageRelationship? = null
        try {
            val id = hyperlink.attributeValue("id")
            if (id != null) {
                hyRel = mainPart.getRelationshipsByType(PackageRelationshipTypes.HYPERLINK_PART).getRelationshipByID(id)
            }
        } catch (e: InvalidFormatException) {
            logD("writerLog 3")
            control!!.getSysKit().getErrorKit().writerLog(e, true)
        }
        var hyIndex = -1
        if (hyRel != null) {
            hyIndex = control!!.getSysKit().getHyperlinkManage().addHyperlink(hyRel.targetURI.toString(), Hyperlink.LINK_URL)
        }
        if (hyIndex == -1) {
            val anchor = hyperlink.attributeValue("anchor")
            if (anchor != null) {
                hyIndex = control!!.getSysKit().getHyperlinkManage().addHyperlink(anchor, Hyperlink.LINK_BOOKMARK)
            }
        }
        var leaf: LeafElement? = null
        for (r in hyperlink.childElements("r")) {
            var run = r
            val instr = run.element("instrText")?.text
            if (instr != null && instr.contains("PAGEREF")) {
                hyIndex = -1
            }
            val ruby = run.element("ruby")
            if (ruby != null) {
                val rubyBase = ruby.element("rubyBase")
                if (rubyBase != null) {
                    run = rubyBase.element("r") ?: continue
                }
            }
            // FIX search/highlight: lấy đủ text của run
            val str = getRunText(run)
            if (str.isEmpty()) {
                if (editMain()) editObjectRun = run
                val drawing = run.element("drawing")
                if (drawing != null) {
                    processPictureAndDiagram(drawing, paraElem)
                    leaf = null
                }
                continue
            }
            recordEdit(offset, str, run, DocxSourceMap.Kind.TEXT)
            val l = LeafElement(str)
            leaf = l
            val attr = l.getAttribute()!!
            // 属性
            run.element("rPr")?.let { processRunAttribute(it, attr) }

            l.setStartOffset(offset)
            offset += str.length
            l.setEndOffset(offset)
            paraElem.appendLeaf(l)

            if (hyIndex >= 0) {
                setHyperlinkStyle(attr, hyIndex)
            }
        }
        return leaf
    }

    // ===================== SHAPE / PICTURE (DrawingML) =====================

    /** EMU -> pixel (bản Java lặp lại công thức này rất nhiều lần) */
    private fun emuToPixel(value: String?): Int =
        (value.toIntSafe(0) * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH).toInt()

    private fun addShape(shape: AbstractShape?, paraElem: ParagraphElement?) {
        if (shape != null && paraElem != null) {
            recordEdit(offset, "1", editObjectRun, DocxSourceMap.Kind.OBJECT)
            val leaf = LeafElement(1.toString())
            leaf.setStartOffset(offset)
            offset++
            leaf.setEndOffset(offset)
            paraElem.appendLeaf(leaf)

            am.setShapeID(leaf.getAttribute()!!, control!!.getSysKit().getWPShapeManage().addShape(shape))
        }
    }

    private fun addPicture(pic: Element?, rect: Rectangle?): PictureShape? {
        if (pic == null || rect == null) return null
        val blipFill = pic.element("blipFill") ?: return null
        val effectInfor: PictureEffectInfo? = PictureEffectInfoFactory.getPictureEffectInfor(blipFill)
        val embed = blipFill.element("blip")?.attributeValue("embed") ?: return null

        val hf = hfPart
        val picPart: PackagePart? = if (isProcessHF && hf != null) {
            zip.getPart(hf.getRelationship(embed).targetURI)
        } else {
            zip.getPart(mainPart.getRelationship(embed).targetURI)
        }
        if (picPart == null) return null

        val shape = PictureShape()
        try {
            shape.pictureIndex = control!!.getSysKit().getPictureManage().addPicture(picPart)
        } catch (e: Exception) {
            logD("writerLog 4")
            control!!.getSysKit().getErrorKit().writerLog(e)
        }
        shape.setZoomX(1000.toShort())
        shape.setZoomY(1000.toShort())
        shape.pictureEffectInfor = effectInfor
        shape.bounds = rect
        return shape
    }

    private fun getRelative(relativeFrom: String?): Byte {
        val r = relativeFrom ?: return WPAbstractShape.RELATIVE_COLUMN
        return when {
            // 相对于页边距
            r.equals("margin", true) -> WPAbstractShape.RELATIVE_MARGIN
            // 相对于页面
            r.equals("page", true) -> WPAbstractShape.RELATIVE_PAGE
            r.equals("column", true) -> WPAbstractShape.RELATIVE_COLUMN
            r.equals("character", true) -> WPAbstractShape.RELATIVE_CHARACTER
            r.equals("leftMargin", true) -> WPAbstractShape.RELATIVE_LEFT
            r.equals("rightMargin", true) -> WPAbstractShape.RELATIVE_RIGHT
            r.equals("insideMargin", true) -> WPAbstractShape.RELATIVE_INNER
            r.equals("outsideMargin", true) -> WPAbstractShape.RELATIVE_OUTER
            r.equals("paragraph", true) -> WPAbstractShape.RELATIVE_PARAGRAPH
            r.equals("line", true) -> WPAbstractShape.RELATIVE_LINE
            r.equals("topMargin", true) -> WPAbstractShape.RELATIVE_TOP
            r.equals("bottomMargin", true) -> WPAbstractShape.RELATIVE_BOTTOM
            else -> WPAbstractShape.RELATIVE_COLUMN
        }
    }

    private fun getAlign(align: String?): Byte {
        val a = align ?: return WPAbstractShape.ALIGNMENT_ABSOLUTE
        return when {
            a.equals("left", true) -> WPAbstractShape.ALIGNMENT_LEFT
            a.equals("right", true) -> WPAbstractShape.ALIGNMENT_RIGHT
            a.equals("top", true) -> WPAbstractShape.ALIGNMENT_TOP
            a.equals("bottom", true) -> WPAbstractShape.ALIGNMENT_BOTTOM
            a.equals("center", true) -> WPAbstractShape.ALIGNMENT_CENTER
            a.equals("inside", true) -> WPAbstractShape.ALIGNMENT_INSIDE
            a.equals("outside", true) -> WPAbstractShape.ALIGNMENT_OUTSIDE
            else -> WPAbstractShape.ALIGNMENT_ABSOLUTE
        }
    }

    private fun processWrapAndPosition_Drawing(shape: WPAbstractShape, anchor: Element, rect: Rectangle) {
        //behindDoc or not
        if ("1".equals(anchor.attributeValue("behindDoc"), ignoreCase = true)) {
            shape.setWrap(WPAbstractShape.WRAP_BOTTOM)
        }
        shape.setWrap(getDrawingWrapType(anchor))

        //horizontal position and horizontal relative position
        var positionHElement: Element? = null
        var positionVElement: Element? = null

        //relative position for word 2010
        for (item in anchor.childElements("AlternateContent")) {
            val choice = item.element("Choice") ?: continue
            choice.element("positionH")?.let { positionHElement = it }
            choice.element("positionV")?.let { positionVElement = it }
        }
        // 水平
        val posH = positionHElement ?: anchor.element("positionH")
        if (posH != null) {
            shape.horizontalRelativeTo = getRelative(posH.attributeValue("relativeFrom"))
            val align = posH.element("align")
            val posOffset = posH.element("posOffset")
            val pct = posH.element("pctPosHOffset")
            if (align != null) {
                shape.horizontalAlignment = getAlign(align.text)
            } else if (posOffset != null) {
                rect.translate(Math.round(posOffset.text.toIntSafe(0) * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH), 0)
            } else if (pct != null) {
                //horizontal relative position
                shape.horRelativeValue = pct.text.toIntSafe(0) / 100
                shape.horPositionType = WPAbstractShape.POSITIONTYPE_RELATIVE
            }
        }

        //vertical position and vertical relative position
        val posV = positionVElement ?: anchor.element("positionV")
        if (posV != null) {
            shape.verticalRelativeTo = getRelative(posV.attributeValue("relativeFrom"))
            val align = posV.element("align")
            val posOffset = posV.element("posOffset")
            val pct = posV.element("pctPosVOffset")
            if (align != null) {
                shape.verticalAlignment = getAlign(align.text)
            } else if (posOffset != null) {
                rect.translate(0, Math.round(posOffset.text.toIntSafe(0) * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH))
            } else if (pct != null) {
                shape.verRelativeValue = pct.text.toIntSafe(0) / 100
                shape.verPositionType = WPAbstractShape.POSITIONTYPE_RELATIVE
            }
        }
    }

    /**
     *
     */
    private fun processPictureAndDiagram(drawing: Element, paraElem: ParagraphElement) {
        // embed picture
        var inline = drawing.element("inline")
        var isInline = true
        if (inline == null) {
            isInline = false
            // wrap picture
            inline = drawing.element("anchor")
        }
        if (inline == null) return
        val graphicdata = inline.element("graphic")?.element("graphicData") ?: return

        val pic = graphicdata.element("pic")
        val chartEle = graphicdata.element("chart")
        val dgm = graphicdata.element("relIds")
        if (pic != null) {
            val spPr = pic.element("spPr") ?: return
            var picPart: PackagePart? = null
            val embed = spPr.element("blipFill")?.element("blip")?.attributeValue("embed")
            if (embed != null) {
                val hf = hfPart
                picPart = if (isProcessHF && hf != null) hf else packagePart
            }

            val shape = addPicture(pic, ReaderKit.instance().getShapeAnchor(spPr.element("xfrm"), 1.0f, 1.0f))
            if (shape != null) {
                AutoShapeDataKit.processPictureShape(control, zipPackage, picPart, spPr, themeColor, shape)
                val wpPictureShape = WPPictureShape()
                wpPictureShape.setPictureShape(shape)
                wpPictureShape.bounds = shape.bounds

                if (!isInline) {
                    processWrapAndPosition_Drawing(wpPictureShape, inline, requireNotNull(shape.bounds))
                } else {
                    wpPictureShape.setWrap(WPAbstractShape.WRAP_OLE)
                }
                addShape(wpPictureShape, paraElem)
            }
        } else if (chartEle != null) {
            //chart
            if (chartEle.attribute("id") == null) return
            val id = chartEle.attributeValue("id")
            val ship = mainPart.getRelationship(id) ?: return
            val chartPart = zip.getPart(ship.targetURI)
            try {
                val abstrChart: AbstractChart? = ChartReader.instance()
                    .read(control!!, zipPackage!!, chartPart, themeColor ?: emptyMap(), MainConstant.APPLICATION_TYPE_WP)
                if (abstrChart != null) {
                    val bounds = Rectangle()
                    inline.element("simplePos")?.let { simplePos ->
                        simplePos.attributeValue("x")?.let { bounds.x = emuToPixel(it) }
                        simplePos.attributeValue("y")?.let { bounds.y = emuToPixel(it) }
                    }
                    inline.element("extent")?.let { extent ->
                        extent.attributeValue("cx")?.let { bounds.width = emuToPixel(it) }
                        extent.attributeValue("cy")?.let { bounds.height = emuToPixel(it) }
                    }
                    val shape = WPChartShape()
                    shape.aChart = abstrChart
                    shape.bounds = bounds
                    if (!isInline) {
                        processWrapAndPosition_Drawing(shape, inline, bounds)
                    } else {
                        shape.setWrap(WPAbstractShape.WRAP_OLE)
                    }
                    addShape(shape, paraElem)
                }
            } catch (e: Exception) {
                return
            }
        } else if (dgm != null) {
            val dm = dgm.attributeValue("dm") ?: return
            try {
                val diagramRel = mainPart.getRelationshipsByType(PackageRelationshipTypes.DIAGRAM_DATA)
                    .getRelationshipByID(dm)
                if (diagramRel != null) {
                    val rect = Rectangle()
                    inline.element("extent")?.let { extent ->
                        val cx = extent.attributeValue("cx")
                        if (!cx.isNullOrEmpty()) rect.width = emuToPixel(cx)
                        val cy = extent.attributeValue("cy")
                        if (!cy.isNullOrEmpty()) rect.height = emuToPixel(cy)
                    }
                    val dataPart = zip.getPart(diagramRel.targetURI)
                    processSmart(control!!, zip, dataPart, paraElem, inline, rect, isInline)
                }
            } catch (e: Exception) {
                // bỏ qua SmartArt lỗi như bản gốc
            }
        }
    }

    @Throws(Exception::class)
    private fun processSmart(
        control: IControl, zipPackage: ZipPackage, dataPart: PackagePart?, paraElem: ParagraphElement,
        anchor: Element, rect: Rectangle, isInline: Boolean
    ) {
        if (dataPart == null) return
        val saxreader = SAXReader()
        val dataDoc = dataPart.inputStream?.use { saxreader.read(it) } ?: return
        var root = dataDoc.rootElement

        val fill = AutoShapeDataKit.processBackground(control, zipPackage, dataPart, root!!.element("bg"), themeColor)
        val line = LineKit.createLine(control, zipPackage, dataPart, root!!.element("whole")?.element("ln"), themeColor)
        var drawingPart: PackagePart? = null
        val relId = root!!.element("extLst")?.element("ext")?.element("dataModelExt")?.attributeValue("relId")
        if (relId != null) {
            val smartArDrawingRel = mainPart.getRelationship(relId)
            drawingPart = zipPackage.getPart(smartArDrawingRel.targetURI)
        }
        if (drawingPart == null) return

        val drawingDoc = drawingPart.inputStream.use { saxreader.read(it) } ?: return
        root = drawingDoc.rootElement ?: return
        val spTree = root.element("spTree") ?: return

        val groupShape = WPGroupShape()
        val autoShape = WPAutoShape()
        autoShape.addGroupShape(groupShape)

        var wrapType: Short = WPAbstractShape.WRAP_OLE
        if (!isInline) {
            processWrapAndPosition_Drawing(autoShape, anchor, rect)
            wrapType = getDrawingWrapType(anchor)
        }

        groupShape.bounds = rect
        autoShape.backgroundAndFill = fill
        autoShape.line = line
        autoShape.shapeType = ShapeTypes.Rectangle
        if (wrapType != WPAbstractShape.WRAP_OLE) {
            groupShape.wrapType = wrapType
            autoShape.setWrap(wrapType)
        } else {
            groupShape.wrapType = WPAbstractShape.WRAP_OLE
            autoShape.setWrap(WPAbstractShape.WRAP_OLE)
        }

        autoShape.bounds = rect
        val it = spTree.elementIterator()
        while (it!!.hasNext()) {
            processAutoShape2010(drawingPart, paraElem, it!!.next() as Element, groupShape, 1.0f, 1.0f, 0, 0, false)
        }
        addShape(autoShape, paraElem)
    }

    // ===================== VML SHAPE (Word 2007: w:pict) =====================

    /** Đọc "x,y" (coordorigin / coordsize); giá trị trống giữ mặc định. */
    private fun parseCoordPair(value: String?, def0: Float, def1: Float): FloatArray {
        val result = floatArrayOf(def0, def1)
        if (value.isNullOrEmpty()) return result
        val values = value.split(",")
        if (values.size == 2) {
            if (values[0].isNotEmpty()) result[0] = values[0].toFloatSafe(def0)
            result[1] = values[1].toFloatSafe(def1)
        } else if (values.size == 1) {
            result[0] = values[0].toFloatSafe(def0)
        }
        return result
    }

    private fun isIgnoredVmlShapeId(id: String?): Boolean =
        id != null && (id.startsWith("Genko") || id.startsWith("Header") || id.startsWith("Footer"))

    /**
     * @param sp
     * @param paraElem
     */
    private fun processAutoShapeForPict(sp: Element, paraElem: ParagraphElement, parent: WPGroupShape?, zoomX: Float, zoomY: Float) {
        val name = sp.name
        //ignore genko shapes
        if (isIgnoredVmlShapeId(sp.attributeValue("id"))) {
            return
        }
        if ("group".equals(name, ignoreCase = true)) {
            val groupShape = WPGroupShape()
            val shape: AbstractShape = if (parent == null) {
                WPAutoShape().also { it.addGroupShape(groupShape) }
            } else {
                groupShape
            }

            var rect = processAutoShapeStyle(sp, shape, parent, zoomX, zoomY)
            if (rect == null) {
                shape.dispose()
                return
            }
            val origin = if (sp.attribute("coordorigin") != null)
                parseCoordPair(sp.attributeValue("coordorigin"), 0f, 0f) else floatArrayOf(0f, 0f)
            val size = if (sp.attribute("coordsize") != null)
                parseCoordPair(sp.attributeValue("coordsize"), 0f, 0f) else floatArrayOf(0f, 0f)
            val x = origin[0]
            val y = origin[1]
            val w = size[0]
            val h = size[1]
            val zoom = floatArrayOf(1.0f, 1.0f)
            val childRect = Rectangle()
            if (w != 0f && h != 0f) {
                zoom[0] = rect.width * MainConstant.EMU_PER_INCH / MainConstant.PIXEL_DPI / zoomX / w
                zoom[1] = rect.height * MainConstant.EMU_PER_INCH / MainConstant.PIXEL_DPI / zoomY / h
            }
            rect = processGrpSpRect(parent, rect)

            childRect.x = (x * zoom[0] * zoomX * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH).toInt()
            childRect.y = (y * zoom[1] * zoomY * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH).toInt()
            childRect.width = (w * zoom[0] * zoomX * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH).toInt()
            childRect.height = (h * zoom[1] * zoomY * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH).toInt()
            if (parent == null) {
                childRect.x += rect.x
                childRect.y += rect.y
            }
            groupShape.setOffPostion(rect.x - childRect.x, rect.y - childRect.y)

            groupShape.bounds = rect
            groupShape.parent = parent
            groupShape.rotation = shape.rotation
            groupShape.flipHorizontal = shape.flipHorizontal
            groupShape.flipVertical = shape.flipVertical

            if (parent == null) {
                val wrapType = getShapeWrapType(sp)
                groupShape.wrapType = wrapType
                (shape as WPAutoShape).setWrap(wrapType)
            } else {
                groupShape.wrapType = parent.wrapType
            }

            val it = sp.elementIterator()
            while (it!!.hasNext()) {
                processAutoShapeForPict(it!!.next() as Element, paraElem, groupShape, zoom[0] * zoomX, zoom[1] * zoomY)
            }

            if (shape is WPAbstractShape) {
                for (sh in groupShape.getShapes()) {
                    if (sh is WPAbstractShape) {
                        copyWrapAndPosition(shape, sh)
                    }
                }
            }
            if (parent == null) {
                addShape(shape, paraElem)
            } else {
                parent.appendShapes(shape)
            }
        } else if ("shape".equals(name, ignoreCase = true)) {
            if (sp.element("imagedata") != null) {
                processPicture(sp, paraElem)
            } else {
                processAutoShape(sp, paraElem, parent, zoomX, zoomY, hasTextbox2007(sp))?.let {
                    processTextbox2007(packagePart, it, sp)
                }
            }
        } else if ("line".equals(name, true) || "polyline".equals(name, true) || "curve".equals(name, true)
            || "rect".equals(name, true) || "roundrect".equals(name, true) || "oval".equals(name, true)
        ) {
            processAutoShape(sp, paraElem, parent, zoomX, zoomY, hasTextbox2007(sp))?.let {
                processTextbox2007(packagePart, it, sp)
            }
        }
    }

    /** Copy kiểu wrap + vị trí từ shape cha sang shape con (bản Java viết 9 dòng cast lặp lại) */
    private fun copyWrapAndPosition(from: WPAbstractShape, to: WPAbstractShape) {
        to.setWrap(from.wrap.toShort())
        to.horPositionType = from.horPositionType
        to.horizontalRelativeTo = from.horizontalRelativeTo
        to.horRelativeValue = from.horRelativeValue
        to.horizontalAlignment = from.horizontalAlignment

        to.verPositionType = from.verPositionType
        to.verticalRelativeTo = from.verticalRelativeTo
        to.verRelativeValue = from.verRelativeValue
        to.verticalAlignment = from.verticalAlignment
    }

    /**
     * @param sp
     * @return
     */
    private fun getShapeWrapType(sp: Element): Short {
        val wrap = sp.element("wrap")
        if (wrap != null) {
            val type = wrap.attributeValue("type")
            when {
                "none".equals(type, true) -> return WPAbstractShape.WRAP_OLE
                "square".equals(type, true) -> return WPAbstractShape.WRAP_SQUARE
                "tight".equals(type, true) -> return WPAbstractShape.WRAP_TIGHT
                "topAndBottom".equals(type, true) -> return WPAbstractShape.WRAP_TOPANDBOTTOM
                "through".equals(type, true) -> return WPAbstractShape.WRAP_THROUGH
            }
        }
        val style = sp.attributeValue("style")
        if (style != null) {
            val styles = style.split(";")
            for (i in styles.indices.reversed()) {
                if (styles[i].contains("z-index:")) {
                    val zIndex = styles[i].replace("z-index:", "").toIntSafe(0)
                    return if (zIndex > 0) WPAbstractShape.WRAP_TOP else WPAbstractShape.WRAP_BOTTOM
                }
            }
        }
        return -1
    }

    private fun getDrawingWrapType(anchor: Element?): Short {
        if (anchor == null) return -1
        return when {
            anchor.element("wrapNone") != null ->
                if ("1".equals(anchor.attributeValue("behindDoc"), true)) WPAbstractShape.WRAP_BOTTOM
                else WPAbstractShape.WRAP_TOP
            anchor.element("wrapSquare") != null -> WPAbstractShape.WRAP_SQUARE
            anchor.element("wrapTight") != null -> WPAbstractShape.WRAP_TIGHT
            anchor.element("wrapThrough") != null -> WPAbstractShape.WRAP_THROUGH
            anchor.element("wrapTopAndBottom") != null -> WPAbstractShape.WRAP_TOPANDBOTTOM
            else -> WPAbstractShape.WRAP_OLE
        }
    }

    /**
     *
     */
    private fun processPicture(shapeElem: Element?, paraElem: ParagraphElement) {
        var shape = shapeElem ?: return
        var imagedata = shape.element("imagedata")
        // picture control
        if (imagedata == null) {
            val rectElem = shape.element("rect")
            if (rectElem != null) {
                shape = rectElem
                imagedata = rectElem.element("fill")
            }
        }
        if (imagedata == null) return
        val id = imagedata.attributeValue("id") ?: return

        val hf = hfPart
        val picPart = if (isProcessHF && hf != null) {
            zip.getPart(hf.getRelationship(id).targetURI)
        } else {
            zip.getPart(mainPart.getRelationship(id).targetURI)
        }
        val style = shape.attributeValue("style")
        if (picPart == null || style == null) return

        val s = shape.attributeValue("id")
        if (s != null && s.indexOf("PictureWatermark") > 0) {
            isProcessWatermark = true
        }
        try {
            val pictureIndex = control!!.getSysKit().getPictureManage().addPicture(picPart)
            val wrapType = getShapeWrapType(shape)
            val abstractShape: AbstractShape
            if (isProcessWatermark) {
                val wm = WatermarkShape()
                imagedata.attributeValue("blacklevel")?.let {
                    wm.blacklevel = it.toFloatSafe(0f) / 100000f
                }
                imagedata.attributeValue("gain")?.let { wm.gain = it.toFloatSafe(0f) / 100000f }
                wm.watermarkType = WatermarkShape.Watermark_Picture
                wm.pictureIndex = pictureIndex
                wm.setWrap(wrapType)
                abstractShape = wm
            } else {
                val effectInfor = PictureEffectInfoFactory.getPictureEffectInfor_ImageData(imagedata)
                val pictureShape = PictureShape()
                pictureShape.pictureIndex = pictureIndex
                pictureShape.setZoomX(1000.toShort())
                pictureShape.setZoomY(1000.toShort())
                pictureShape.pictureEffectInfor = effectInfor

                val wpPic = WPPictureShape()
                wpPic.setPictureShape(pictureShape)
                wpPic.setWrap(wrapType)
                abstractShape = wpPic
            }

            val rect = processAutoShapeStyle(shape, abstractShape, null, 1000f, 1000f)
            if (!isProcessWatermark) {
                val picShape = requireNotNull((abstractShape as WPPictureShape).getPictureShape())
                picShape.bounds = rect
                picShape.backgroundAndFill = processBackgroundAndFill(shape)
                picShape.line = processLine(shape)
            }
            addShape(abstractShape, paraElem)
            isProcessWatermark = false
        } catch (e: Exception) {
            logD("writerLog 5")
            control!!.getSysKit().getErrorKit().writerLog(e)
        }
    }

    /**
     * Màu VML: "#rrggbb", "#rgb", tên màu, "fill darken(118)"...
     * @param value
     * @param filled
     * @return
     */
    private fun getColor(value: String?, filled: Boolean): Int {
        val defaultColor = if (filled) Color.WHITE else Color.BLACK
        if (value.isNullOrEmpty()) return defaultColor   // Java cũ: chuỗi rỗng => crash charAt(0)

        var v: String = value
        val index = v.indexOf(" ")
        if (index > 0) {
            v = v.substring(0, index)
        }
        if (v[0] == '#') {
            if (v.length > 6) {
                return parseHexColor(v, defaultColor)
            } else if (v.length == 4) {
                // #fc9-----#ffcc99
                val builder = StringBuilder(7).append('#')
                for (i in 1 until 4) {
                    builder.append(v[i]).append(v[i])
                }
                return parseHexColor(builder.toString(), defaultColor)
            }
        }
        return when {
            v.contains("black") || v.contains("darken") -> Color.BLACK
            v.contains("green") -> 0xff008000.toInt()
            v.contains("silver") -> 0xffc0c0c0.toInt()
            v.contains("lime") -> 0xff00ff00.toInt()
            v.contains("gray") -> 0xff808080.toInt()
            v.contains("olive") -> 0xff808000.toInt()
            v.contains("white") -> Color.WHITE
            v.contains("yellow") -> 0xffffff00.toInt()
            v.contains("maroon") -> 0xff800000.toInt()
            v.contains("navy") -> 0xff000080.toInt()
            v.contains("red") -> Color.RED
            v.contains("blue") -> 0xff0000ff.toInt()
            v.contains("purple") -> 0xff800080.toInt()
            v.contains("teal") -> 0xff008080.toInt()
            v.contains("fuchsia") -> 0xffff00ff.toInt()
            v.contains("aqua") -> 0xff00ffff.toInt()
            else -> defaultColor
        }
    }

    fun processRotation(shape: AutoShape) {
        var angle = shape.rotation
        if (shape.flipHorizontal) {
            angle = -angle
        }
        if (shape.flipVertical) {
            angle = -angle
        }

        val shapeType = shape.shapeType
        if (shapeType == ShapeTypes.StraightConnector1
            || shapeType == ShapeTypes.BentConnector3 || shapeType == ShapeTypes.CurvedConnector3
        ) {
            if ((angle == 45f || angle == 135f || angle == 225f)
                && !shape.flipHorizontal
                && !shape.flipVertical
            ) {
                angle -= 90f
            }
        }
        shape.rotation = angle
    }

    /**
     * 重新计算group shape中的child shape的位置
     */
    private fun processGrpSpRect(parent: GroupShape?, rect: Rectangle): Rectangle {
        if (parent != null) {
            rect.x += parent.offX
            rect.y += parent.offY
        }
        return rect
    }

    // ===================== VML STYLE =====================

    /**
     * Duyệt thuộc tính style VML "key:value;key:value".
     * TỐI ƯU/AN TOÀN: bản Java dùng split(":") rồi truy cập temps[1] => crash khi item không có ':'.
     */
    private inline fun forEachVmlStyle(style: String, action: (key: String, value: String) -> Unit) {
        for (item in style.split(";")) {
            val parts = item.split(":")
            if (parts.size < 2) continue
            action(parts[0].trim(), parts[1].trim())
        }
    }

    private fun processPolygonZoom(shape: Element?, autoShape: AbstractShape?, parent: GroupShape?, zoomX: Float, zoomY: Float): Float {
        if (shape == null || autoShape == null) return 1.0f
        val styleStr = shape.attributeValue("style") ?: return 1.0f
        var w = 0f
        var h = 0f
        var x = 0f
        var y = 0f
        forEachVmlStyle(styleStr) { key, value ->
            when {
                "left".equals(key, true) -> x += getValueInPt(value, zoomX)
                "top".equals(key, true) -> y += getValueInPt(value, zoomY)
                "margin-left".equals(key, true) -> x += getValueInPt(value, 1.0f)
                "margin-top".equals(key, true) -> y += getValueInPt(value, 1.0f)
                "width".equals(key, true) -> w = getValueInPt(value, zoomX)
                "height".equals(key, true) -> h = getValueInPt(value, zoomY)
            }
        }
        val rect = Rectangle()
        rect.x = (x * MainConstant.POINT_TO_PIXEL).toInt()
        rect.y = (y * MainConstant.POINT_TO_PIXEL).toInt()
        rect.width = (w * MainConstant.POINT_TO_PIXEL).toInt()
        rect.height = (h * MainConstant.POINT_TO_PIXEL).toInt()

        if (autoShape is WPAutoShape && autoShape.shapeType == ShapeTypes.ArbitraryPolygon) {
            val coordsize = shape.attributeValue("coordsize")
            if (!coordsize.isNullOrEmpty()) {
                val size = coordsize.split(",")
                if (size.size >= 2) {
                    val width = size[0].toIntSafe(0).toFloat()
                    val height = size[1].toIntSafe(0).toFloat()
                    return minOf(width / rect.width, height / rect.height)
                }
            }
        }
        return 1.0f
    }

    /**
     * return value in point units
     * @param value
     * @param zoom
     * @return
     */
    private fun getValueInPt(value: String, zoom: Float): Float {
        var index = value.indexOf("pt")
        if (index >= 0) {
            return value.substring(0, index).toFloatSafe(0f)
        }
        index = value.indexOf("in")
        if (index >= 0) {
            return value.substring(0, index).toFloatSafe(0f) * MainConstant.POINT_DPI
        }
        index = value.indexOf("mm")
        if (index >= 0) {
            return value.substring(0, index).toFloatSafe(0f) * MainConstant.MM_TO_POINT
        }
        return value.toFloatSafe(0f) * zoom * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH
    }

    /**
     * @param shape
     * @param autoShape
     */
    private fun processAutoShapeStyle(shape: Element?, autoShape: AbstractShape?, parent: GroupShape?, zoomX: Float, zoomY: Float): Rectangle? {
        if (shape == null || autoShape == null) return null
        val styleStr = shape.attributeValue("style") ?: return null

        var w = 0f
        var h = 0f
        var x = 0f
        var y = 0f
        val topLevelShape = if (parent == null && autoShape.type != AbstractShape.SHAPE_GROUP) autoShape as? WPAutoShape else null
        forEachVmlStyle(styleStr) { key, value ->
            when {
                "position".equals(key, true) || "text-align".equals(key, true) -> Unit
                "left".equals(key, true) -> x += getValueInPt(value, zoomX)
                "top".equals(key, true) -> y += getValueInPt(value, zoomY)
                "margin-left".equals(key, true) -> x += getValueInPt(value, 1.0f)
                "margin-top".equals(key, true) -> y += getValueInPt(value, 1.0f)
                "width".equals(key, true) -> w = getValueInPt(value, zoomX)
                "height".equals(key, true) -> h = getValueInPt(value, zoomY)
                // TỐI ƯU: gộp relativeType(List, contains O(n)) + relativeValue thành 1 LinkedHashMap
                "mso-width-percent".equals(key, true) ->
                    relativeValue.getOrPut(autoShape) { IntArray(4) }[0] = value.toFloatSafe(0f).toInt()
                "mso-height-percent".equals(key, true) ->
                    relativeValue.getOrPut(autoShape) { IntArray(4) }[2] = value.toFloatSafe(0f).toInt()
                "flip".equals(key, true) -> {
                    if ("x".equals(value, true)) {
                        autoShape.flipHorizontal = true
                    } else if ("y".equals(value, true)) {
                        autoShape.flipVertical = true
                    }
                }
                "rotation".equals(key, true) -> {
                    if (value.indexOf("fd") > 0) {
                        autoShape.rotation = (value.substring(0, value.length - 2).toIntSafe(0) / 60000).toFloat()
                    } else {
                        autoShape.rotation = value.toIntSafe(0).toFloat()
                    }
                }
                "mso-width-relative".equals(key, true) || "mso-height-relative".equals(key, true) -> Unit
                topLevelShape == null -> Unit
                // 水平位置
                "mso-position-horizontal".equals(key, true) ->
                    topLevelShape.horizontalAlignment = getAlign(value)
                "mso-left-percent".equals(key, true) -> {
                    //horizontal relative position
                    topLevelShape.horRelativeValue = value.toIntSafe(0)
                    topLevelShape.horPositionType = WPAbstractShape.POSITIONTYPE_RELATIVE
                }
                // 水平相对于
                "mso-position-horizontal-relative".equals(key, true) -> when {
                    "margin".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN
                    "page".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE
                    "left-margin-area".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LEFT
                    "right-margin-area".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_RIGHT
                    "inner-margin-area".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER
                    "outer-margin-area".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER
                    "text".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_COLUMN
                    "char".equals(value, true) -> topLevelShape.horizontalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_CHARACTER
                }
                // 垂直位置
                "mso-position-vertical".equals(key, true) ->
                    topLevelShape.verticalAlignment = getAlign(value)
                "mso-top-percent".equals(key, true) -> {
                    //vertical relative position
                    topLevelShape.verRelativeValue = value.toIntSafe(0)
                    topLevelShape.verPositionType = WPAbstractShape.POSITIONTYPE_RELATIVE
                }
                // 垂直相对值
                "mso-position-vertical-relative".equals(key, true) -> when {
                    "line".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE
                    "text".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH
                    "margin".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN
                    "page".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE
                    "top-margin-area".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP
                    "bottom-margin-area".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM
                    "inner-margin-area".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER
                    "outer-margin-area".equals(value, true) -> topLevelShape.verticalRelativeTo = com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER
                }
            }
        }
        val rect = Rectangle()
        rect.x = (x * MainConstant.POINT_TO_PIXEL).toInt()
        rect.y = (y * MainConstant.POINT_TO_PIXEL).toInt()
        rect.width = (w * MainConstant.POINT_TO_PIXEL).toInt()
        rect.height = (h * MainConstant.POINT_TO_PIXEL).toInt()
        if (autoShape.type != AbstractShape.SHAPE_GROUP && (autoShape as? WPAutoShape)?.groupShape == null) {
            processGrpSpRect(parent, rect)
        }
        if (autoShape is WPAutoShape && autoShape.shapeType == ShapeTypes.ArbitraryPolygon) {
            val coordsize = shape.attributeValue("coordsize")
            if (!coordsize.isNullOrEmpty()) {
                val size = coordsize.split(",")
                if (size.size >= 2) {
                    val width = size[0].toIntSafe(0).toFloat()
                    val height = size[1].toIntSafe(0).toFloat()
                    if (width != 0f && height != 0f) {
                        val m = Matrix()
                        m.postScale(rect.width / width, rect.height / height)
                        for (path in autoShape.paths) {
                            path?.path?.transform(m)
                        }
                    }
                }
            }
        }
        autoShape.bounds = rect
        return rect
    }

    /**
     * @param type
     * @return
     */
    private fun getFillType(type: String?): Byte = when {
        "gradient".equals(type, true) -> BackgroundAndFill.FILL_SHADE_LINEAR
        //TTOD gradient Radial and gradient Center
        "gradientRadial".equals(type, true) -> BackgroundAndFill.FILL_SHADE_RADIAL
        "pattern".equals(type, true) -> BackgroundAndFill.FILL_PATTERN
        "tile".equals(type, true) -> BackgroundAndFill.FILL_SHADE_TILE
        "frame".equals(type, true) -> BackgroundAndFill.FILL_PICTURE
        else -> BackgroundAndFill.FILL_SOLID
    }

    private fun getRadialGradientPositionType(fill: Element): Int {
        var positionType = RadialGradientShader.Center_TL
        val focusposition = fill.attributeValue("focusposition")
        if (!focusposition.isNullOrEmpty()) {
            val xy = focusposition.split(",")
            if (xy.size == 2) {
                if (".5" == xy[0] && ".5" == xy[1]) {
                    //radial from center
                    positionType = RadialGradientShader.Center_Center
                } else if ("1" == xy[0] && "1" == xy[1]) {
                    positionType = RadialGradientShader.Center_BR
                } else if ("" == xy[0] && "1" == xy[1]) {
                    positionType = RadialGradientShader.Center_BL
                } else if ("1" == xy[0] && "" == xy[1]) {
                    positionType = RadialGradientShader.Center_TR
                }
            } else if (xy.size == 1 && "1" == xy[0]) {
                positionType = RadialGradientShader.Center_TR
            }
        }
        return positionType
    }

    private fun getAutoShapeType(shape: Element): Int {
        var shapeType = when (shape.name) {
            "rect" -> ShapeTypes.Rectangle
            "roundrect" -> ShapeTypes.RoundRectangle
            "oval" -> ShapeTypes.Ellipse
            "curve" -> ShapeTypes.Curve
            "polyline" -> ShapeTypes.DirectPolygon
            "line" -> ShapeTypes.WP_Line
            else -> ShapeTypes.NotPrimitive
        }
        // type
        if (shape.attribute("type") != null) {
            val v = shape.attributeValue("type")
            if (v != null && v.length > 9) {
                // "#_x0000_t202" -> 202
                shapeType = v.substring(9).toIntSafe(shapeType)
            }
        } else if (shape.attribute("path") != null) {
            shapeType = ShapeTypes.ArbitraryPolygon
        }
        return shapeType
    }

    private fun getValue(value: String): Float {
        var index = value.indexOf("pt")
        val w = if (index > 0) {
            value.substring(0, index).toFloatSafe(0f)
        } else {
            index = value.indexOf("in")
            if (index > 0) {
                value.substring(0, index).toFloatSafe(0f) * MainConstant.POINT_DPI
            } else {
                value.toFloatSafe(0f) * MainConstant.POINT_DPI / MainConstant.EMU_PER_INCH
            }
        }
        return w * MainConstant.POINT_TO_PIXEL
    }

    private fun processPoints(text: String?): Array<PointF> {
        if (text == null) return emptyArray()
        val pts = text.split(",")
        val ptList = ArrayList<PointF>(pts.size / 2)
        var index = 0
        while (index < pts.size - 1) {
            ptList.add(PointF(getValue(pts[index]), getValue(pts[index + 1])))
            index += 2
        }
        return ptList.toTypedArray()
    }

    private fun processArrow(autoShape: WPAutoShape, shape: Element) {
        //start and end arrow
        val stroke = shape.element("stroke") ?: return
        //start arrow
        var type = getArrowType(stroke.attributeValue("startarrow"))
        if (type > 0) {
            autoShape.createStartArrow(
                type,
                getArrowWidth(stroke.attributeValue("startarrowwidth")),
                getArrowLength(stroke.attributeValue("startarrowlength"))
            )
        }
        type = getArrowType(stroke.attributeValue("endarrow"))
        if (type > 0) {
            autoShape.createEndArrow(
                type,
                getArrowWidth(stroke.attributeValue("endarrowwidth")),
                getArrowLength(stroke.attributeValue("endarrowlength"))
            )
        }
    }

    private fun getArrowExtendPath(arrowPath: Path?, fill: BackgroundAndFill?, line: Line?, border: Boolean, arrowType: Byte): ExtendPath {
        // extendPath
        val pathExtend = ExtendPath()
        pathExtend.setArrowFlag(true)
        pathExtend.path = arrowPath
        if (border) {
            if (arrowType != Arrow.Arrow_Arrow) {
                if (line != null) {
                    pathExtend.backgroundAndFill = line.backgroundAndFill
                } else if (fill != null) {
                    pathExtend.backgroundAndFill = fill
                }
            } else {
                pathExtend.setLine(line)
            }
        } else if (fill != null) {
            pathExtend.backgroundAndFill = fill
        }
        return pathExtend
    }

    // ===================== AUTOSHAPE (VML) =====================

    /**
     *
     */
    private fun processAutoShape(
        shape: Element?, paraElem: ParagraphElement, parent: WPGroupShape?,
        zoomX: Float, zoomY: Float, hasTextbox: Boolean
    ): WPAutoShape? {
        if (shape == null) return null

        var values: Array<Float?>? = null
        val border = true
        val shapeType = getAutoShapeType(shape)
        var autoShape: WPAutoShape? = null
        // adjust values
        if (shape.attribute("adj") != null) {
            val adj = shape.attributeValue("adj")
            if (!adj.isNullOrEmpty()) {
                val adjustStr = adj.split(",")
                if (adjustStr.isNotEmpty()) {
                    values = Array(adjustStr.size) { i ->
                        val temp = adjustStr[i]
                        if (temp.isNotEmpty()) temp.toFloatSafe(0f) / 21600 else null
                    }
                }
            }
        }
        var fill = processBackgroundAndFill(shape)
        val line = processLine(shape)
        if (shapeType == ShapeTypes.StraightConnector1
            || shapeType == ShapeTypes.BentConnector2 || shapeType == ShapeTypes.BentConnector3
            || shapeType == ShapeTypes.CurvedConnector3
        ) {
            val s = WPAutoShape()
            autoShape = s
            s.shapeType = shapeType
            s.line = line
            processArrow(s, shape)
            if (s.shapeType == ShapeTypes.BentConnector2 && values == null) {
                s.adjustData = arrayOf<Float?>(1.0f)
            } else {
                s.adjustData = values
            }
        } else if (shapeType == ShapeTypes.ArbitraryPolygon) {
            val s = WPAutoShape()
            autoShape = s
            s.shapeType = ShapeTypes.ArbitraryPolygon
            processArrow(s, shape)
            val pathContext = shape.attributeValue("path")
            val pathzoom = processPolygonZoom(shape, s, parent, zoomX, zoomY)
            val lineWidth = Math.round((line?.lineWidth ?: 1) * pathzoom)
            val pathWithArrow = VMLPathParser.instance().createPath(s, pathContext ?: "", lineWidth)
            if (pathWithArrow != null) {
                pathWithArrow.getPolygonPath()?.forEach { p ->
                    val pathExtend = ExtendPath()
                    pathExtend.path = p
                    if (line != null) pathExtend.setLine(line)
                    if (fill != null) pathExtend.backgroundAndFill = fill
                    s.appendPath(pathExtend)
                }
                pathWithArrow.getStartArrow()?.let {
                    s.appendPath(getArrowExtendPath(it, fill, line, border, s.startArrow!!.type))
                }
                pathWithArrow.getEndArrow()?.let {
                    s.appendPath(getArrowExtendPath(it, fill, line, border, s.endArrow!!.type))
                }
            }
        } else if (shapeType == ShapeTypes.WP_Line
            || shapeType == ShapeTypes.Curve
            || shapeType == ShapeTypes.DirectPolygon
        ) {
            val s = WPAutoShape()
            autoShape = s
            s.shapeType = shapeType
            processArrow(s, shape)
            val path = Path()
            var startArrowPath: Path? = null
            var endArrowPath: Path? = null
            val lineWidth = line?.lineWidth ?: 1
            if (shapeType == ShapeTypes.Line) {
                if (line != null) {
                    fill = line.backgroundAndFill
                }
                //one line, not StraightConnector1
                var from = processPoints(shape.attributeValue("from")).firstOrNull() ?: PointF()
                var to = processPoints(shape.attributeValue("to")).firstOrNull() ?: PointF()
                var startArrowTailCenter: PointF? = null
                var endArrowTailCenter: PointF? = null
                if (s.startArrowhead) {
                    val apt = LineArrowPathBuilder.getDirectLineArrowPath(to.x, to.y, from.x, from.y, s.startArrow!!, lineWidth)
                    startArrowPath = apt.arrowPath
                    startArrowTailCenter = apt.arrowTailCenter
                }
                if (s.endArrowhead) {
                    val apt = LineArrowPathBuilder.getDirectLineArrowPath(from.x, from.y, to.x, to.y, s.endArrow!!, lineWidth)
                    endArrowPath = apt.arrowPath
                    endArrowTailCenter = apt.arrowTailCenter
                }
                if (startArrowTailCenter != null) {
                    from = LineArrowPathBuilder.getReferencedPosition(from.x, from.y, startArrowTailCenter.x, startArrowTailCenter.y, s.startArrow!!.type)
                }
                if (endArrowTailCenter != null) {
                    to = LineArrowPathBuilder.getReferencedPosition(to.x, to.y, endArrowTailCenter.x, endArrowTailCenter.y, s.endArrow!!.type)
                }
                path.moveTo(from.x, from.y)
                path.lineTo(to.x, to.y)
            } else if (shapeType == ShapeTypes.Curve) {
                //one beizer line
                var from = processPoints(shape.attributeValue("from")).firstOrNull() ?: PointF()
                val ctr1 = processPoints(shape.attributeValue("control1")).firstOrNull() ?: PointF()
                val ctr2 = processPoints(shape.attributeValue("control2")).firstOrNull() ?: PointF()
                var to = processPoints(shape.attributeValue("to")).firstOrNull() ?: PointF()
                var startArrowTailCenter: PointF? = null
                var endArrowTailCenter: PointF? = null
                if (s.startArrowhead) {
                    val apt = LineArrowPathBuilder.getCubicBezArrowPath(to.x, to.y, ctr2.x, ctr2.y, ctr1.x, ctr1.y, from.x, from.y, s.startArrow!!, lineWidth)
                    startArrowPath = apt.arrowPath
                    startArrowTailCenter = apt.arrowTailCenter
                }
                if (s.endArrowhead) {
                    val apt = LineArrowPathBuilder.getCubicBezArrowPath(from.x, from.y, ctr1.x, ctr1.y, ctr2.x, ctr2.y, to.x, to.y, s.endArrow!!, lineWidth)
                    endArrowPath = apt.arrowPath
                    endArrowTailCenter = apt.arrowTailCenter
                }
                if (startArrowTailCenter != null) {
                    from = LineArrowPathBuilder.getReferencedPosition(from.x, from.y, startArrowTailCenter.x, startArrowTailCenter.y, s.startArrow!!.type)
                }
                if (endArrowTailCenter != null) {
                    to = LineArrowPathBuilder.getReferencedPosition(to.x, to.y, endArrowTailCenter.x, endArrowTailCenter.y, s.endArrow!!.type)
                }
                path.moveTo(from.x, from.y)
                path.cubicTo(ctr1.x, ctr1.y, ctr2.x, ctr2.y, to.x, to.y)
            } else if (shapeType == ShapeTypes.DirectPolygon) {
                //direct polygon
                val pts = processPoints(shape.attributeValue("points"))
                val ptCnt = pts.size
                if (ptCnt >= 2) {
                    var startArrowTailCenter: PointF? = null
                    var endArrowTailCenter: PointF? = null
                    if (s.startArrowhead) {
                        val apt = LineArrowPathBuilder.getDirectLineArrowPath(pts[1].x, pts[1].y, pts[0].x, pts[0].y, s.startArrow!!, lineWidth)
                        startArrowPath = apt.arrowPath
                        startArrowTailCenter = apt.arrowTailCenter
                    }
                    if (s.endArrowhead) {
                        val apt = LineArrowPathBuilder.getDirectLineArrowPath(
                            pts[ptCnt - 2].x, pts[ptCnt - 2].y, pts[ptCnt - 1].x, pts[ptCnt - 1].y, s.endArrow!!, lineWidth
                        )
                        endArrowPath = apt.arrowPath
                        endArrowTailCenter = apt.arrowTailCenter
                    }
                    if (startArrowTailCenter != null) {
                        pts[0] = LineArrowPathBuilder.getReferencedPosition(pts[0].x, pts[0].y, startArrowTailCenter.x, startArrowTailCenter.y, s.startArrow!!.type)
                    }
                    if (endArrowTailCenter != null) {
                        pts[ptCnt - 1] = LineArrowPathBuilder.getReferencedPosition(
                            pts[ptCnt - 1].x, pts[ptCnt - 1].y, endArrowTailCenter.x, endArrowTailCenter.y, s.endArrow!!.type
                        )
                    }
                }
                if (ptCnt > 0) {
                    path.moveTo(pts[0].x, pts[0].y)
                    for (index in 1 until ptCnt) {
                        path.lineTo(pts[index].x, pts[index].y)
                    }
                }
            }
            val pathExtend = ExtendPath()
            pathExtend.path = path
            if (line != null) pathExtend.setLine(line)
            if (fill != null) pathExtend.backgroundAndFill = fill
            s.appendPath(pathExtend)
            if (startArrowPath != null) {
                s.appendPath(getArrowExtendPath(startArrowPath, fill, line, border, s.startArrow!!.type))
            }
            if (endArrowPath != null) {
                s.appendPath(getArrowExtendPath(endArrowPath, fill, line, border, s.endArrow!!.type))
            }
        } else if (hasTextbox || fill != null || border) {
            val id = shape.attributeValue("id")
            if (id != null && id.indexOf("WaterMarkObject") > 0) {
                isProcessWatermark = true
            }
            val s = if (isProcessWatermark) WatermarkShape() else WPAutoShape()
            autoShape = s
            s.shapeType = shapeType
            processArrow(s, shape)
            if (fill != null) s.backgroundAndFill = fill
            if (line != null) s.line = line
            s.adjustData = values
        }

        if (autoShape != null) {
            autoShape.setAuotShape07(true)
            if (parent == null) {
                autoShape.setWrap(getShapeWrapType(shape))
            } else {
                autoShape.setWrap(parent.wrapType)
            }
            processAutoShapeStyle(shape, autoShape, parent, zoomX, zoomY)
            processRotation(autoShape)
            if (isProcessWatermark) {
                (autoShape as? WatermarkShape)?.let { processWatermark(it, shape) }
                isProcessWatermark = false
            }
            if (parent == null) {
                addShape(autoShape, paraElem)
            } else {
                parent.appendShapes(autoShape)
            }
        }
        return autoShape
    }

    private fun processWatermark(waterMark: WatermarkShape, shape: Element) {
        val textpath = shape.element("textpath") ?: return
        // text watermark
        waterMark.watermarkType = WatermarkShape.Watermark_Text
        //font color
        val fillColor = shape.attributeValue("fillcolor")
        if (!fillColor.isNullOrEmpty()) {
            waterMark.fontColor = getColor(fillColor, false)
        }
        //opacity
        shape.element("fill")?.attributeValue("opacity")?.let {
            waterMark.setOpacity(it.toFloatSafe(1f))
        }
        //water mark context
        waterMark.watermartString = textpath.attributeValue("string")
        //water mark font size
        val style = textpath.attributeValue("style") ?: return
        forEachVmlStyle(style) { key, value ->
            if ("font-size".equals(key, true)) {
                val fontSize = value.replace("pt", "").toIntSafe(0)
                if (fontSize == 1) {
                    //auto font size
                    waterMark.isAutoFontSize = true
                } else {
                    waterMark.fontSize = fontSize
                }
            }
        }
    }

    // ===================== LINE / FILL (VML) =====================

    private fun isFalseValue(v: String?): Boolean = "f".equals(v, true) || "false".equals(v, true)

    private fun processLine(shape: Element): Line? {
        // border
        if (isFalseValue(shape.attributeValue("stroked"))) {
            return null
        }
        val type = shape.attributeValue("type")
        if (type != null && type.length > 1 && type[0] == '#') {
            // TỐI ƯU: so sánh trực tiếp, không nối chuỗi "#" + id cho từng shapetype
            val shapetype = shape.parent?.childElements("shapetype")?.firstOrNull {
                val id = it.attributeValue("id")
                id != null && type.length == id.length + 1 && type.regionMatches(1, id, 0, id.length)
            }
            if (shapetype != null && isFalseValue(shapetype.attributeValue("stroked"))) {
                return null
            }
        }

        var lineColor = 0xFF000000.toInt()
        val strokecolor = shape.attributeValue("strokecolor")
        if (!strokecolor.isNullOrEmpty()) {
            lineColor = getColor(strokecolor, false)
        }
        val lineFill = BackgroundAndFill()
        lineFill.foregroundColor = lineColor
        var lineWidth = 1
        var weight = shape.attributeValue("strokeweight")
        if (weight != null) {
            weight = when {
                weight.contains("pt") -> weight.replace("pt", "")
                weight.contains("mm") -> weight.replace("mm", "")
                weight.contains("cm") -> weight.replace("cm", "")
                else -> weight
            }
            lineWidth = Math.round(weight.toFloatSafe(1f) * MainConstant.POINT_TO_PIXEL)
        }
        val dash = shape.element("stroke")?.attributeValue("dashstyle") != null

        val line = Line()
        line.backgroundAndFill = lineFill
        line.lineWidth = lineWidth
        line.isDash = dash
        return line
    }

    private fun processBackgroundAndFill(shape: Element): BackgroundAndFill? {
        var fill: BackgroundAndFill? = null
        // background
        val filledAttr = shape.attributeValue("filled")
        val filled = !(filledAttr == "f" || filledAttr == "false")
        if (!filled) return null

        val fillElem = shape.element("fill")
        val fillId = fillElem?.attributeValue("id")
        if (fillElem != null && !fillId.isNullOrEmpty()) {
            //frame, pattern and tile
            val hf = hfPart
            val picPart = if (isProcessHF && hf != null) {
                zip.getPart(hf.getRelationship(fillId).targetURI)
            } else {
                zip.getPart(mainPart.getRelationship(fillId).targetURI)
            }
            if (picPart != null) {
                val f = BackgroundAndFill()
                fill = f
                val type = getFillType(fillElem.attributeValue("type"))
                try {
                    val pm = control!!.getSysKit().getPictureManage()
                    if (type == BackgroundAndFill.FILL_SHADE_TILE) {
                        f.fillType = BackgroundAndFill.FILL_SHADE_TILE
                        val index = pm.addPicture(picPart)
                        f.shader = TileShader(pm.getPicture(index), TileShader.Flip_None, 1f, 1.0f)
                    } else if (type == BackgroundAndFill.FILL_PATTERN) {
                        var foregroundColor = 0xFFFFFFFF.toInt()
                        val fc = shape.attributeValue("fillcolor")
                        if (!fc.isNullOrEmpty()) {
                            foregroundColor = getColor(fc, false)
                        }
                        var backgroundColor = 0xFFFFFFFF.toInt()
                        fillElem.attributeValue("color2")?.let { backgroundColor = getColor(it, true) }
                        f.fillType = BackgroundAndFill.FILL_PATTERN
                        val index = pm.addPicture(picPart)
                        f.shader =
                            PatternShader(pm.getPicture(index), backgroundColor, foregroundColor)
                    } else {
                        f.fillType = BackgroundAndFill.FILL_PICTURE
                        f.pictureIndex = pm.addPicture(picPart)
                    }
                } catch (e: Exception) {
                    control!!.getSysKit().getErrorKit().writerLog(e)
                }
            }
        }
        if (fill == null) {
            val f = BackgroundAndFill()
            fill = f
            val type = if (fillElem != null) getFillType(fillElem.attributeValue("type")) else BackgroundAndFill.FILL_SOLID
            if (fillElem == null || type == BackgroundAndFill.FILL_SOLID) {
                var fillColor = Color.WHITE
                f.fillType = BackgroundAndFill.FILL_SOLID
                val fc = shape.attributeValue("fillcolor")
                if (!fc.isNullOrEmpty()) {
                    fillColor = getColor(fc, true)
                }
                val opacityStr = fillElem?.attributeValue("opacity")
                if (opacityStr != null) {
                    var opacity = opacityStr.toFloatSafe(1f)
                    if (opacity > 1) {
                        opacity /= 65536f
                    }
                    opacity *= 255
                    fillColor = ((opacity.toInt() and 0xFF) shl 24) or (fillColor and 0xFFFFFF)
                }
                f.foregroundColor = fillColor
            } else {
                val gradient = readGradient(shape, fillElem, type)
                f.fillType = type
                f.shader = gradient
            }
        }
        return fill
    }

    private fun readGradient(shape: Element, fillElem: Element, type: Byte): Gradient? {
        var focus = 0
        fillElem.attributeValue("focus")?.let { focus = it.replace("%", "").toIntSafe(0) }
        var angle = fillElem.attributeValue("angle").toIntSafe(0)
        angle = when (angle) {
            -90, 0 -> angle + 90
            -45 -> 135
            -135 -> 45
            else -> angle
        }
        val colors: IntArray
        val positions: FloatArray
        val intermediateColors = fillElem.attributeValue("colors")
        if (intermediateColors != null) {
            val positionColor = intermediateColors.split(";")
            val length = positionColor.size
            colors = IntArray(length)
            positions = FloatArray(length)
            for (i in 0 until length) {
                val item = positionColor[i].trim()
                val index = item.indexOf(" ")
                if (index < 0) {
                    colors[i] = getColor(item, true)
                    continue
                }
                val pos = item.substring(0, index)
                positions[i] = if (pos.contains("f")) {
                    pos.toFloatSafe(0f) / 100000f
                } else {
                    pos.toFloatSafe(0f)
                }
                colors[i] = getColor(item.substring(index + 1), true)
            }
        } else {
            val color = getColor(shape.attributeValue("fillcolor"), true)
            var color2 = 0
            fillElem.attributeValue("color2")?.let { color2 = getColor(it.replace("fill ", ""), true) }
            colors = intArrayOf(color, color2)
            positions = floatArrayOf(0f, 1f)
        }
        val gradient: Gradient? = when (type) {
            BackgroundAndFill.FILL_SHADE_LINEAR -> LinearGradientShader(angle.toFloat(), colors, positions)
            BackgroundAndFill.FILL_SHADE_RADIAL ->
                RadialGradientShader(getRadialGradientPositionType(fillElem), colors, positions)
            else -> null
        }
        gradient?.focus = focus
        return gradient
    }

    // ===================== SHAPE 2010 (DrawingML wps/wpg) =====================

    private fun processAutoShape2010(
        paraElem: ParagraphElement, sp: Element?, parent: WPGroupShape?,
        zoomX: Float, zoomY: Float, x: Int, y: Int, bResetRect: Boolean
    ): AbstractShape? = processAutoShape2010(packagePart, paraElem, sp, parent, zoomX, zoomY, x, y, bResetRect)

    private fun processAutoShape2010(
        packagePart: PackagePart?, paraElem: ParagraphElement, sp: Element?, parent: WPGroupShape?,
        zoomX: Float, zoomY: Float, x: Int, y: Int, bResetRect: Boolean
    ): AbstractShape? {
        if (sp == null) return null
        var rect: Rectangle? = null
        var shape: AbstractShape? = null
        val name = sp.name
        val isGroup = name == "wgp" || name == "grpSp"
        // rect
        val temp = when {
            name == "wsp" || name == "sp" || name == "pic" -> sp.element("spPr")
            isGroup -> sp.element("grpSpPr")
            else -> null
        }
        if (temp != null) {
            rect = ReaderKit.instance().getShapeAnchor(temp.element("xfrm"), zoomX, zoomY)
            if (rect != null) {
                if (bResetRect) {
                    rect.x += x
                    rect.y += y
                }
                rect = processGrpSpRect(parent, rect)
            }
        }
        // shape
        if (rect != null && isGroup) {
            val grpSpPr = sp.element("grpSpPr")
            if (grpSpPr != null) {
                val zoomXY = ReaderKit.instance().getAnchorFitZoom(grpSpPr.element("xfrm"))
                val childRect = ReaderKit.instance()
                    .getChildShapeAnchor(grpSpPr.element("xfrm"), zoomXY[0] * zoomX, zoomXY[1] * zoomY)
                if (bResetRect) {
                    childRect!!.x += x
                    childRect!!.y += y
                }
                val grpShape = WPGroupShape()
                grpShape.setOffPostion(rect.x - childRect!!.x, rect.y - childRect!!.y)
                grpShape.bounds = rect
                ReaderKit.instance().processRotation(grpSpPr, grpShape)
                val it = sp.elementIterator()
                while (it!!.hasNext()) {
                    processAutoShape2010(
                        packagePart, paraElem, it!!.next() as Element, grpShape,
                        zoomXY[0] * zoomX, zoomXY[1] * zoomY, 0, 0, false
                    )
                }
                shape = if (parent == null) {
                    WPAutoShape().also { it.addGroupShape(grpShape) }
                } else {
                    grpShape
                }
            }
        } else if (rect != null) {
            if (name == "wsp" || name == "sp") {
                try {
                    val hf = hfPart
                    val part = if (isProcessHF && hf != null) hf else packagePart
                    shape = AutoShapeDataKit.getAutoShape(
                        control, zipPackage, part, sp,
                        rect, themeColor, MainConstant.APPLICATION_TYPE_WP.toInt(), hasTextbox2010(sp)
                    )
                    (shape as? WPAutoShape)?.let { processTextbox2010(packagePart, it, sp) }
                } catch (e: Exception) {
                    control!!.getSysKit().getErrorKit().writerLog(e)
                }
            } else if (name == "pic") {
                shape = addPicture(sp, rect)
            }
        }
        if (shape != null) {
            if (parent == null) {
                addShape(shape, paraElem)
            } else {
                shape.parent = parent
                if (shape is WPAutoShape) {
                    shape.setWrap(parent.wrapType)
                }
                parent.appendShapes(shape)
            }
        }
        return shape
    }

    private fun processAlternateContent(alternateContent: Element?, paraElem: ParagraphElement) {
        val drawing = alternateContent?.element("Choice")?.element("drawing") ?: return
        var anchor = drawing.element("anchor")
        var wrapType: Short = -1
        if (anchor == null) {
            //inline
            anchor = drawing.element("inline")
            wrapType = WPAbstractShape.WRAP_OLE
        }
        if (anchor == null) return
        //如果是稿纸生成autoshape就不需要处理
        if (isIgnoredVmlShapeId(anchor.element("docPr")?.attributeValue("name"))) {
            return
        }
        val graphicData = anchor.element("graphic")?.element("graphicData") ?: return
        val it = graphicData.elementIterator()
        while (it!!.hasNext()) {
            val shape = processAutoShape2010(paraElem, it!!.next() as Element, null, 1.0f, 1.0f, 0, 0, true) ?: continue
            if (shape is WPAutoShape && shape.groupShape != null) {
                val grp = shape.groupShape ?: continue
                if (wrapType.toInt() == -1) {
                    wrapType = getDrawingWrapType(anchor)
                }
                grp.wrapType = wrapType
                setShapeWrapType(grp, wrapType)
            }
            // Java cũ ép kiểu (WPAutoShape) => crash nếu là PictureShape
            (shape as? WPAbstractShape)?.let { processWrapAndPosition_Drawing(it, anchor, requireNotNull(shape.bounds)) }
        }
    }

    private fun setShapeWrapType(groupShape: WPGroupShape, wrapType: Short) {
        for (item in groupShape.getShapes()) {
            if (item is WPAbstractShape) {
                item.setWrap(wrapType)
            } else if (item is WPGroupShape) {
                setShapeWrapType(item, wrapType)
            }
        }
    }

    // ===================== RUN ATTRIBUTE =====================

    /** w:b, w:i... không có val hoặc val khác 0/false/off => bật */
    private fun isOnOff(elem: Element): Boolean {
        val v = elem.attributeValue("val") ?: return true
        return !(v == "0" || v.equals("false", true) || v.equals("off", true))
    }

    /**
     * @param rPr
     * @param attr
     */
    private fun processRunAttribute(rPr: Element, attr: IAttributeSet) {
        // 字号
        // sz is in half-points. szCs only applies to complex-script text (Arabic, Hebrew...), so it
        // must not enlarge Latin text; without sz the size is inherited from the style.
        val halfPoints = rPr.element("sz")?.attributeValue("val").toFloatSafe(0f)
        if (halfPoints > 0f) {
            am.setFontSize(attr, maxOf(1, Math.round(halfPoints / 2f)))
        }
        // 字体
        var temp = rPr.element("rFonts")
        if (temp != null) {
            val fontName = temp.attributeValue("hAnsi") ?: temp.attributeValue("eastAsia")
            if (fontName != null) {
                val index = FontTypefaceManage.instance().addFontName(fontName)
                if (index >= 0) {
                    am.setFontName(attr, index)
                }
            }
        }
        // 字符颜色
        temp = rPr.element("color")
        if (temp != null) {
            val v = temp.attributeValue("val")
            if ("auto" == v) {
                am.setFontColor(attr, Color.BLACK)
            } else {
                // AN TOÀN: Color.parseColor crash với giá trị lạ
                am.setFontColor(attr, parseHexColor(v, Color.BLACK))
            }
        }
        // 粗体  (FIX: <w:b w:val="0"/> nghĩa là KHÔNG đậm; bản Java cũ vẫn bật đậm)
        temp = rPr.element("b")
        if (temp != null && isOnOff(temp)) {
            am.setFontBold(attr, true)
        }
        // 斜体
        temp = rPr.element("i")
        if (temp != null && isOnOff(temp)) {
            am.setFontItalic(attr, true)
        }
        // 下划线
        val u = rPr.element("u")
        if (u != null) {
            val underlineType = u.attributeValue("val")
            if (underlineType != null && underlineType != "none") {
                am.setFontUnderline(attr, 1)
                val c = u.attributeValue("color")
                if (!c.isNullOrEmpty() && c != "auto") {
                    am.setFontUnderlineColr(attr, parseHexColor(c, Color.BLACK))
                }
            }
        }
        // 删除线
        temp = rPr.element("strike")
        if (temp != null) {
            am.setFontStrike(attr, "0" != temp.attributeValue("val"))
        }
        // 双删除线
        temp = rPr.element("dstrike")
        if (temp != null) {
            am.setFontDoubleStrike(attr, "0" != temp.attributeValue("val"))
        }
        // 上下标 (FIX: "baseline" không còn bị coi là chỉ số dưới)
        val s = rPr.element("vertAlign")
        if (s != null) {
            when (s.attributeValue("val")) {
                "superscript" -> am.setFontScript(attr, 1)
                "subscript" -> am.setFontScript(attr, 2)
            }
        }
        // 样式
        temp = rPr.element("rStyle")
        if (temp != null) {
            val v = temp.attributeValue("val")
            if (!v.isNullOrEmpty()) {
                styleStrID[v]?.let { am.setParaStyleID(attr, it) }
            }
        }
        // highlight
        temp = rPr.element("highlight")
        if (temp != null) {
            am.setFontHighLight(attr, FCKit.convertColor(temp.attributeValue("val")))
        }
    }

    private fun processValue(v: String?, isLeftRight: Boolean): Int {
        var a = if (isLeftRight) ShapeKit.DefaultMargin_Twip * 2 else ShapeKit.DefaultMargin_Twip
        if (v != null) {
            // 十六进制
            val n = if (ReaderKit.instance().isDecimal(v)) v.toIntSafe(0) else (v.toIntOrNull(16) ?: 0)
            a = (n * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH * MainConstant.PIXEL_TO_TWIPS).toInt()
        }
        return a
    }

    private fun processParagraphs(elems: List<Element>) {
        for (elem in elems) {
            when (elem.name) {
                // FIX: không gán đè elem (tránh NPE)
                "sdt" -> elem.element("sdtContent")?.let { processParagraphs(it.childElements()) }
                "p" -> processParagraph(elem, 0)
                "tbl" -> processTable(elem)
            }
        }
    }

    // ===================== TEXTBOX =====================

    private fun hasTextbox2007(sp: Element): Boolean {
        val txbx = sp.element("textbox")
        if (txbx != null) {
            return txbx.element("txbxContent") != null
        }
        val context = sp.element("textpath")?.attributeValue("string")
        return !context.isNullOrEmpty()
    }

    /** Tạo SectionElement cho textbox, trả về offset cũ để khôi phục */
    private fun beginTextbox(wpShape: WPAutoShape): Pair<Long, SectionElement> {
        val oldOffset = offset
        offset = WPModelConstant.TEXTBOX + (textboxIndex shl 32)
        wpShape.elementIndex = textboxIndex.toInt()
        val textboxElement = SectionElement()
        textboxElement.setStartOffset(offset)
        document.appendElement(textboxElement, offset)
        return oldOffset to textboxElement
    }

    private fun endTextbox(wpShape: WPAutoShape, textboxElement: SectionElement, oldOffset: Long) {
        wpShape.elementIndex = textboxIndex.toInt()
        textboxElement.setEndOffset(offset)
        textboxIndex++
        offset = oldOffset
    }

    private fun setTextboxSize(attr: IAttributeSet, wpShape: WPAutoShape) {
        // 宽度
        am.setPageWidth(attr, (wpShape.bounds!!.width * MainConstant.PIXEL_TO_TWIPS).toInt())
        // 高度
        am.setPageHeight(attr, (wpShape.bounds!!.height * MainConstant.PIXEL_TO_TWIPS).toInt())
    }

    /** v-text-anchor + mso-wrap-style (bản Java lặp lại khối này 2 lần) */
    private fun applyTextboxVmlStyle(styleStr: String?, attr: IAttributeSet, wpShape: WPAutoShape) {
        if (styleStr == null) return
        forEachVmlStyle(styleStr) { key, value ->
            when {
                "v-text-anchor".equals(key, true) -> when (value) {
                    //alignment in vertical
                    "middle" -> am.setPageVerticalAlign(attr, WPAttrConstant.PAGE_V_CENTER)
                    "bottom" -> am.setPageVerticalAlign(attr, WPAttrConstant.PAGE_V_BOTTOM)
                    "top" -> am.setPageVerticalAlign(attr, WPAttrConstant.PAGE_V_TOP)
                }
                "mso-wrap-style".equals(key, true) -> wpShape.isTextWrapLine =
                    !"none".equals(value, true)
            }
        }
    }

    private fun processTextbox2007(packagePart: PackagePart?, wpShape: WPAutoShape, sp: Element): Boolean {
        val txbx = sp.element("textbox")
        if (txbx != null) {
            val txbxContent = txbx.element("txbxContent") ?: return false
            val (oldOffset, textboxElement) = beginTextbox(wpShape)
            processParagraphs(txbxContent.childElements())
            // section属性
            val attr = textboxElement.getAttribute()!!
            setTextboxSize(attr, wpShape)
            var leftMargin = ShapeKit.DefaultMargin_Twip * 2
            var topMargin = ShapeKit.DefaultMargin_Twip
            var rightMargin = ShapeKit.DefaultMargin_Twip * 2
            var bottomMargin = ShapeKit.DefaultMargin_Twip
            val inset = txbx.attributeValue("inset")
            if (inset != null) {
                val insets = inset.split(",")
                fun insetTwips(i: Int, def: Int): Int =
                    if (insets.size > i && insets[i].isNotEmpty())
                        Math.round(getValueInPt(insets[i], 1.0f) * MainConstant.POINT_TO_TWIPS)
                    else def
                leftMargin = insetTwips(0, leftMargin)      // 左边距
                topMargin = insetTwips(1, topMargin)        // 上边距
                rightMargin = insetTwips(2, rightMargin)    // 右边距
                bottomMargin = insetTwips(3, bottomMargin)  // 下边距
            }
            am.setPageMarginTop(attr, topMargin)
            am.setPageMarginBottom(attr, bottomMargin)
            am.setPageMarginLeft(attr, leftMargin)
            am.setPageMarginRight(attr, rightMargin)
            applyTextboxVmlStyle(sp.attributeValue("style"), attr, wpShape)
            endTextbox(wpShape, textboxElement, oldOffset)
            return true
        }
        val textpath = sp.element("textpath") ?: return false

        //word art
        val context = textpath.attributeValue("string") ?: ""
        wpShape.backgroundAndFill = null
        val (oldOffset, textboxElement) = beginTextbox(wpShape)
        val paraElem = ParagraphElement()
        val t = offset
        paraElem.setStartOffset(offset)
        val len = context.length
        if (len > 0) {
            val leaf = LeafElement(context)
            // 属性
            val fc = sp.attributeValue("fillcolor")
            if (!fc.isNullOrEmpty()) {
                am.setFontColor(leaf.getAttribute()!!, getColor(fc, true))
            }
            val width = wpShape.bounds!!.width.toFloat() - ShapeKit.DefaultMargin_Twip * 4 * MainConstant.TWIPS_TO_PIXEL
            val height = wpShape.bounds!!.getHeight().toFloat() - ShapeKit.DefaultMargin_Twip * 2 * MainConstant.TWIPS_TO_PIXEL
            val fontsize = fitWordArtFontSize(context, width, height)
            am.setFontSize(leaf.getAttribute()!!, ((fontsize - 1) * MainConstant.PIXEL_TO_POINT).toInt())
            leaf.setStartOffset(offset)
            offset += len
            leaf.setEndOffset(offset)
            paraElem.appendLeaf(leaf)
        }
        paraElem.setEndOffset(offset)
        if (offset > t) {
            document.appendParagraph(paraElem, offset)
        }
        val attr = textboxElement.getAttribute()!!
        setTextboxSize(attr, wpShape)
        am.setPageMarginTop(attr, ShapeKit.DefaultMargin_Twip)
        am.setPageMarginBottom(attr, ShapeKit.DefaultMargin_Twip)
        am.setPageMarginLeft(attr, ShapeKit.DefaultMargin_Twip * 2)
        am.setPageMarginRight(attr, ShapeKit.DefaultMargin_Twip * 2)
        applyTextboxVmlStyle(sp.attributeValue("style"), attr, wpShape)
        endTextbox(wpShape, textboxElement, oldOffset)
        return true
    }

    /**
     * Cỡ chữ WordArt: giá trị nhỏ nhất (>=12) mà chữ KHÔNG còn vừa khung.
     * TỐI ƯU: bản Java tăng từng 1px và đo lại (có thể hàng trăm lần measureText);
     * ở đây dùng tìm kiếm nhị phân, kết quả giống hệt.
     */
    private fun fitWordArtFontSize(text: String, width: Float, height: Float): Int {
        val paint = PaintKit.instance().getPaint()
        fun fits(size: Int): Boolean {
            paint.textSize = size.toFloat()
            val fm = paint.fontMetrics
            return paint.measureText(text).toInt() < width && Math.ceil((fm.descent - fm.ascent).toDouble()).toInt() < height
        }
        var lo = 12
        if (!fits(lo)) return lo
        var hi = 24
        while (fits(hi) && hi < 4096) {
            lo = hi
            hi *= 2
        }
        // fits(lo) == true, fits(hi) == false (hoặc chạm trần)
        while (hi - lo > 1) {
            val mid = (lo + hi) ushr 1
            if (fits(mid)) lo = mid else hi = mid
        }
        paint.textSize = hi.toFloat()
        return hi
    }

    private fun hasTextbox2010(sp: Element): Boolean =
        sp.element("txbx")?.element("txbxContent") != null

    /**
     *
     */
    private fun processTextbox2010(packagePart: PackagePart?, wpShape: WPAutoShape, sp: Element): Boolean {
        val txbxContent = sp.element("txbx")?.element("txbxContent") ?: return false
        val oldOffset = offset
        offset = WPModelConstant.TEXTBOX + (textboxIndex shl 32)
        wpShape.elementIndex = textboxIndex.toInt()
        val textboxElement = SectionElement()
        textboxElement.setStartOffset(offset)
        document.appendElement(textboxElement, offset)
        processParagraphs(txbxContent.childElements())
        // section属性
        val attr = textboxElement.getAttribute()!!
        setTextboxSize(attr, wpShape)
        val bodyPr = sp.element("bodyPr")
        if (bodyPr != null) {
            am.setPageMarginTop(attr, processValue(bodyPr.attributeValue("tIns"), false))
            am.setPageMarginBottom(attr, processValue(bodyPr.attributeValue("bIns"), false))
            am.setPageMarginLeft(attr, processValue(bodyPr.attributeValue("lIns"), true))
            am.setPageMarginRight(attr, processValue(bodyPr.attributeValue("rIns"), true))
            // 垂直对齐
            when (bodyPr.attributeValue("anchor")) {
                "ctr" -> am.setPageVerticalAlign(attr, WPAttrConstant.PAGE_V_CENTER)
                "b" -> am.setPageVerticalAlign(attr, WPAttrConstant.PAGE_V_BOTTOM)
                "t" -> am.setPageVerticalAlign(attr, WPAttrConstant.PAGE_V_TOP)
            }
            // 文本框内自动换行
            val wrap = bodyPr.attributeValue("wrap")
            wpShape.isTextWrapLine = wrap == null || "square".equals(wrap, true)
            wpShape.elementIndex = textboxIndex.toInt()
        }
        textboxElement.setEndOffset(offset)
        textboxIndex++
        offset = oldOffset
        return true
    }

    // ===================== SEARCH =====================

    /**
     * Kiểm tra file có chứa từ khóa hay không (dùng cho tìm kiếm danh sách file).
     * - Đọc kiểu streaming (XmlPullParser) => không load cả DOM, dừng ngay khi tìm thấy
     * - Tìm cả trong bảng, hyperlink, textbox, content control, header, footer
     * - Gộp mọi w:t trong đoạn, không bỏ sót run có nhiều w:t
     * - Không phân biệt hoa/thường, chuẩn hóa Unicode NFC (tiếng Việt dựng sẵn / tổ hợp)
     * - Không đụng tới trạng thái của reader (zipPackage / packagePart)
     */
    @Throws(Exception::class)
    override fun searchContent(file: File?, key: String): Boolean {
        val needle = normalizeForSearch(key).trim()
        if (needle.isEmpty()) return false

        val path = file?.absolutePath ?: filePath
        val pkg = ZipPackage(path)
        val coreRel = pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0)
            ?: return false
        val docPart = pkg.getPart(coreRel) ?: return false
        if (partContainsText(docPart, needle)) return true
        return relatedPartsContainText(pkg, docPart, PackageRelationshipTypes.HEADER_PART, needle) ||
            relatedPartsContainText(pkg, docPart, PackageRelationshipTypes.FOOTER_PART, needle)
    }

    @Throws(Exception::class)
    private fun relatedPartsContainText(pkg: ZipPackage, docPart: PackagePart, relType: String, needle: String): Boolean {
        val rels = docPart.getRelationshipsByType(relType) ?: return false
        for (i in 0 until rels.size()) {
            val rel = rels.getRelationship(i) ?: continue
            val part = pkg.getPart(rel.targetURI) ?: continue
            if (partContainsText(part, needle)) return true
        }
        return false
    }

    /**
     * Quét XML theo từng đoạn (p). Đoạn lồng nhau (textbox nằm trong đoạn) có bộ đệm riêng.
     */
    @Throws(Exception::class)
    private fun partContainsText(part: PackagePart, needle: String): Boolean {
        part.inputStream.use { input ->
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            parser.setInput(input, null)

            val paraStack = ArrayList<StringBuilder>()
            var runDepth = 0
            var inText = false
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> when (val name = parser.name) {
                        "p" -> paraStack.add(StringBuilder())
                        "r" -> runDepth++
                        "t" -> inText = true
                        else -> if (runDepth > 0 && paraStack.isNotEmpty() &&
                            (name == "tab" || name == "br" || name == "cr")
                        ) {
                            paraStack[paraStack.size - 1].append(' ')
                        }
                    }
                    XmlPullParser.TEXT -> if (inText && paraStack.isNotEmpty()) {
                        paraStack[paraStack.size - 1].append(parser.text)
                    }
                    XmlPullParser.END_TAG -> when (parser.name) {
                        "t" -> inText = false
                        "r" -> if (runDepth > 0) runDepth--
                        "p" -> if (paraStack.isNotEmpty()) {
                            val sb = paraStack.removeAt(paraStack.size - 1)
                            if (sb.length >= needle.length && normalizeForSearch(sb).contains(needle)) {
                                return true
                            }
                        }
                    }
                }
                event = parser.next()
            }
        }
        return false
    }

    /**
     * Lấy toàn bộ text của một run theo ĐÚNG THỨ TỰ các phần tử con
     * (bản cũ chỉ lấy w:t đầu tiên, bỏ qua tab, và luôn đặt br lên trước text).
     * Text được chuẩn hóa NFC để khớp với từ khóa người dùng gõ.
     */
    private fun getRunText(run: Element): String {
        var sb: StringBuilder? = null
        var hasText = false
        val it = run.elementIterator()
        while (it!!.hasNext()) {
            val child = it!!.next() as Element
            val s: String? = when (child.name) {
                "t" -> child.text?.also { if (it.isNotEmpty()) hasText = true }
                "tab", "ptab" -> " "
                "br" -> if ("page" == child.attributeValue("type")) "\u000c" else "\u000b"
                "cr" -> "\u000b"
                "noBreakHyphen" -> "-"
                else -> null
            }
            if (!s.isNullOrEmpty()) {
                (sb ?: StringBuilder().also { sb = it }).append(s)
            }
        }
        var result = sb?.toString() ?: return ""
        // Giữ đúng hành vi cũ: ngắt trang '\f' chỉ đứng một mình trong leaf
        if (result.length > 1 && result.indexOf('\u000c') >= 0) {
            result = result.replace('\u000c', '\u000b')
        }
        if (hasText && !Normalizer.isNormalized(result, Normalizer.Form.NFC)) {
            result = Normalizer.normalize(result, Normalizer.Form.NFC)
        }
        return result
    }

    // ===================== SAX HANDLER =====================

    /**
     * fix very large XML documents
     */
    internal inner class DOCXSaxHandler : ElementHandler {
        override fun onStart(elementPath: ElementPath?) {
        }

        override fun onEnd(elementPath: ElementPath?) {
            if (abortReader) {
                throw AbortReaderError("abort Reader")
            }
            val elem = elementPath?.current
            val recording = editMain() && (elem!!.name == "p" || elem!!.name == "tbl" || elem!!.name == "sdt")
            var runCount = 0
            var paraCount = 0
            if (recording) {
                fun index(node: Element) {
                    if (node.namespaceURI == "http://schemas.openxmlformats.org/wordprocessingml/2006/main") {
                        if (node.name == "r") editRuns[node] = editRunBase + runCount++
                        if (node.name == "p") editParas[node] = editParaBase + paraCount++
                    }
                    val children = node.elementIterator()
                    while (children!!.hasNext()) index(children!!.next() as Element)
                }
                index(elem)
            }
            when (elem!!.name) {
                "p" -> processParagraph(elem, 0)
                // FIX: dùng biến riêng, không gán đè elem (tránh NPE và detach nhầm phần tử)
                "sdt" -> elem!!.element("sdtContent")?.let { processParagraphs(it.childElements()) }
                "tbl" -> processTable(elem)
                "pict" -> {
                    val paraElem = ParagraphElement()
                    val t = offset
                    paraElem.setStartOffset(offset)
                    processPicture(elem, paraElem)
                    paraElem.setEndOffset(offset)
                    if (offset > t) {
                        document.appendParagraph(paraElem, offset)
                    }
                }
            }
            if (recording) {
                editRunBase += runCount; editParaBase += paraCount
                editRuns.clear(); editParas.clear(); editObjectRun = null
            }
            elem!!.detach()
        }
    }

    // ===================== THEME / MISC =====================

    @Throws(Exception::class)
    private fun processThemeColor() {
        val part = packagePart ?: return
        val themeShip = part.getRelationshipsByType(PackageRelationshipTypes.THEME_PART).getRelationship(0) ?: return
        val themePart = zip.getPart(themeShip.targetURI) ?: return
        val colors: MutableMap<String, Int> = ThemeReader.instance().getThemeColorMap(themePart)?.toMutableMap() ?: return
        themeColor = colors
        colors[SchemeClrConstant.SCHEME_LT1]?.let { colors[SchemeClrConstant.SCHEME_BG1] = it }
        colors[SchemeClrConstant.SCHEME_DK1]?.let { colors[SchemeClrConstant.SCHEME_TX1] = it }
        colors[SchemeClrConstant.SCHEME_LT2]?.let { colors[SchemeClrConstant.SCHEME_BG2] = it }
        colors[SchemeClrConstant.SCHEME_DK2]?.let { colors[SchemeClrConstant.SCHEME_TX2] = it }
    }

    /**
     *
     */
    private fun processRelativeShapeSize() {
        val w = am.getPageWidth(section.getAttribute()!!)
        val h = am.getPageHeight(section.getAttribute()!!)
        for ((shape, v) in relativeValue) {
            val r = requireNotNull(shape.bounds)
            // width
            if (v[0] > 0) {
                r.width = (w * MainConstant.TWIPS_TO_PIXEL * v[0] / 1000f).toInt()
            }
            // height
            if (v[2] > 0) {
                r.height = (h * MainConstant.TWIPS_TO_PIXEL * v[2] / 1000f).toInt()
            }
        }
    }

    private fun getArrowType(arrowType: String?): Byte = when {
        "block".equals(arrowType, true) -> Arrow.Arrow_Triangle
        "classic".equals(arrowType, true) -> Arrow.Arrow_Stealth
        "oval".equals(arrowType, true) -> Arrow.Arrow_Oval
        "diamond".equals(arrowType, true) -> Arrow.Arrow_Diamond
        "open".equals(arrowType, true) -> Arrow.Arrow_Arrow
        else -> Arrow.Arrow_None
    }

    private fun getArrowWidth(width: String?): Int = when {
        "narrow".equals(width, true) -> 0
        "wide".equals(width, true) -> 2
        else -> 1
    }

    private fun getArrowLength(length: String?): Int = when {
        "short".equals(length, true) -> 0
        "long".equals(length, true) -> 2
        else -> 1
    }

    /**
     * FIX: bản Java quên dọn tableStyle, themeColor, hfPart, secElem.
     */
    override fun dispose() {
        if (isReaderFinish()) {
            filePath = null
            zipPackage = null
            wpdoc = null
            packagePart = null
            hfPart = null
            secElem = null
            themeColor = null
            styleStrID.clear()
            tableStyle.clear()
            tableGridCol.clear()
            relativeValue.clear()
            bulletNumbersID.clear()
            control = null
        }
    }

    companion object {
        /** Đặt true khi cần debug; production không ghi log (bản Java dùng Log.e ở nhiều chỗ). */
        @JvmField
        var DEBUG_LOG = false
        private const val TAG = "DOCXReader"

        internal fun logD(msg: String) {
            if (DEBUG_LOG) Log.d(TAG, msg)
        }

        /**
         * Chuẩn hóa chuỗi để so khớp:
         * NFC, chữ thường, mọi khoảng trắng (tab, nbsp, xuống dòng...) gộp thành 1 dấu cách,
         * nháy cong ‘ ’ “ ” thành nháy thẳng.
         * Có thể dùng lại trong phần Find/Highlight của view để chuẩn hóa từ khóa.
         */
        @JvmStatic
        fun normalizeForSearch(cs: CharSequence): String {
            var s = cs.toString()
            if (!Normalizer.isNormalized(s, Normalizer.Form.NFC)) {
                s = Normalizer.normalize(s, Normalizer.Form.NFC)
            }
            val sb = StringBuilder(s.length)
            var lastSpace = false
            for (ch in s) {
                var c = ch
                if (Character.isWhitespace(c) || c == '\u00A0' || c == '\u000b' || c == '\u000c') {
                    if (!lastSpace) {
                        sb.append(' ')
                        lastSpace = true
                    }
                    continue
                }
                lastSpace = false
                if (c == '\u2018' || c == '\u2019') {
                    c = '\''
                } else if (c == '\u201C' || c == '\u201D') {
                    c = '"'
                }
                sb.append(Character.toLowerCase(c))
            }
            return sb.toString()
        }
    }
}

/** A resolved DOCX border: width in eighths of a point (0 = none), ARGB color, space in points. */
internal class DocxBorder(val eighths: Int, val color: Int, val space: Int = 0) {
    companion object {
        @JvmField
        val NONE = DocxBorder(0, 0)
    }
}

/** Word's default left/right cell margin, in twips (0.075 inch). */
private const val DEFAULT_CELL_MARGIN_LR = 108

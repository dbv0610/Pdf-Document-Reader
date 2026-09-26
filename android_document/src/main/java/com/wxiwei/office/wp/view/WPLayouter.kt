/*
 * 文件名称:          WPLayouter.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:13:54
 */
package com.wxiwei.office.wp.view

import android.util.Log
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.ViewKit
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.wp.model.WPDocument

/**
 * Word 布局器
 */
class WPLayouter(root: PageRoot) {

    // 文档属性集
    private var docAttr: DocAttr? = null

    // 章节属性集
    private var pageAttr: PageAttr? = null

    // 段落
    private var paraAttr: ParaAttr? = null

    //
    private var root: PageRoot? = null

    //
    private var doc: IDocument? = null

    // ======== 布局过程用到一些布局状态的值 ==========
    private var section: IElement? = null

    // 当前布局的页码数
    private var currentPageNumber = 1

    // End offset of the last committed page. A page must always make forward
    // progress; otherwise malformed DOCX layout data can create pages forever.
    private var lastCommittedPageEndOffset: Long = Long.MIN_VALUE

    // 当前需要布局的开始的Offset，主要为了段落切页用到。
    private var currentLayoutOffset: Long = 0

    // 段落分页
    private var breakPara: ParagraphView? = null

    // header
    private var header: TitleView? = null

    // footer
    private var footer: TitleView? = null

    //
    private var tableLayout: TableLayoutKit? = null

    //
    private var hfTableLayout: TableLayoutKit? = null

    //
    private val shapeViews: MutableList<LeafView> = ArrayList()

    init {
        this.root = root
        docAttr = DocAttr()
        docAttr!!.rootType = WPViewConstant.PAGE_ROOT.toByte()
        pageAttr = PageAttr()
        paraAttr = ParaAttr()
        tableLayout = TableLayoutKit()
        hfTableLayout = TableLayoutKit()
    }

    fun doLayout() {
        Log.d("OfficePageLayout", "doLayout start pages=${root?.getPageCount()} currentOffset=$currentLayoutOffset")
        tableLayout!!.clearBreakPages()
        doc = root!!.getDocument()
        // 正文区或
        section = doc!!.getSection(0)
        //
        AttrManage.instance().fillPageAttr(pageAttr, section!!.getAttribute())
        //
        val pv = ViewFactory.createView(root!!.getControl()!!, section, null, WPViewConstant.PAGE_VIEW.toInt()) as PageView
        root!!.appendChlidView(pv)
        layoutPage(pv)
        LayoutKit.instance().layoutAllPage(root!!, 1.0f)
    }

    fun layoutPage(pageView: PageView): Int {
        val doc = doc!!
        val pageAttr = pageAttr!!
        val root = root!!
        pageView.setPageNumber(currentPageNumber++)

        layoutHeaderAndFooter(pageView)
        var breakType = WPViewConstant.BREAK_NO.toInt()
        pageView.setSize(pageAttr.pageWidth, pageAttr.pageHeight)
        pageView.setIndent(pageAttr.leftMargin, pageAttr.topMargin, pageAttr.rightMargin, pageAttr.bottomMargin)
        pageView.setStartOffset(currentLayoutOffset)
        Log.e("WPLayouter.LayoutPage", "pageView.setStartOffset = " + currentLayoutOffset)

        val dx = pageAttr.leftMargin
        var dy = pageAttr.topMargin
        val spanW = pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin
        var spanH = pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin
        var flag = ViewKit.instance().setBitValue(0, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), true)
        val maxEnd = doc.getAreaEnd(WPModelConstant.MAIN)

        var elem = if (breakPara != null) breakPara!!.getElement() else doc.getParagraph(currentLayoutOffset)
        Log.d(
            "OfficePageLayout",
            "layoutPage page=${pageView.getPageNumber()} modelOffset=$currentLayoutOffset " +
                "elemStart=${elem?.getStartOffset()} elemEnd=${elem?.getEndOffset()} maxEnd=$maxEnd " +
                "page=${pageAttr.pageWidth}x${pageAttr.pageHeight} margins=" +
                "${pageAttr.leftMargin},${pageAttr.topMargin},${pageAttr.rightMargin},${pageAttr.bottomMargin} " +
                "span=$spanW x $spanH"
        )
        // Header/footer layout can enlarge margins. Never let that remove the
        // entire body area; otherwise the first page cannot advance its model
        // offset and pagination becomes an empty-page loop.
        if (spanH <= 0 && pageAttr.pageHeight > 0) {
            Log.w("OfficePageLayout", "body spanH=$spanH; reset body margins for page layout")
            pageAttr.topMargin = 0
            pageAttr.bottomMargin = 0
            spanH = pageAttr.pageHeight
        }

        var para: ParagraphView
        if (breakPara != null) {
            para = breakPara!!
            // process table break;
            if (breakPara!!.getType() == WPViewConstant.TABLE_VIEW) {
                pageView.setHasBreakTable(true)
                (breakPara as TableView).setBreakPages(true)
            }
        } else if (AttrManage.instance().hasAttribute(elem!!.getAttribute(), AttrIDConstant.PARA_LEVEL_ID)) {
            elem = (doc as WPDocument).getParagraph0(currentLayoutOffset)
            para = ViewFactory.createView(root.getControl()!!, elem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
        } else {
            para = ViewFactory.createView(root.getControl()!!, elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
        }
        pageView.appendChlidView(para)

        Log.e("WPLayouter.LayoutPage", "para.setStartOffset = " + currentLayoutOffset)
        para.setStartOffset(currentLayoutOffset)
        Log.e("WPLayouter.115", "para.setEndOffset = " + elem!!.getEndOffset())
        para.setEndOffset(elem.getEndOffset())
        var keepOne = true
        // The last paragraph or table did not fit at all and was removed, the next page starts with it again
        var removedUnfitPara = false
        while (spanH > 0 && currentLayoutOffset < maxEnd && breakType != WPViewConstant.BREAK_LIMIT.toInt()
            && breakType != WPViewConstant.BREAK_PAGE.toInt()
        ) {
            para.setLocation(dx, dy)
            // 表格段落
            if (para.getType() == WPViewConstant.TABLE_VIEW) {
                if (para.getPreView() != null) {
                    if (para.getPreView()!!.getElement() !== elem) {
                        tableLayout!!.clearBreakPages()
                    }
                }
                breakType = tableLayout!!.layoutTable(root.getControl()!!, doc, root, docAttr, pageAttr, paraAttr!!,
                    para as TableView, currentLayoutOffset, dx, dy, spanW, spanH, flag, breakPara != null)
            } else {
                tableLayout!!.clearBreakPages()
                AttrManage.instance().fillParaAttr(root.getControl(), paraAttr, elem!!.getAttribute())
                breakType = LayoutKit.instance().layoutPara(root.getControl()!!, doc, docAttr!!, pageAttr, paraAttr!!,
                    para, currentLayoutOffset, dx, dy, spanW, spanH, flag)
            }
            val paraHeight = para.getLayoutSpan(WPViewConstant.Y_AXIS)
            if (!keepOne && para.getChildView() == null) {
                if (breakPara == null) {
                    elem = doc.getParagraph(currentLayoutOffset - 1)
                }
                pageView.deleteView(para, true)
                removedUnfitPara = true
                if (para.getType() == WPViewConstant.TABLE_VIEW) {
                    // Nothing of the table was laid out on this page, do not continue it as a broken table
                    tableLayout!!.clearBreakPages()
                }
                break
            }
            //
            if (para.getType() != WPViewConstant.TABLE_VIEW) {
                root.getViewContainer().add(para)
            }
            // 收集段落中的 shape view
            collectShapeView(pageView, para, false)

            dy += paraHeight
            val previousLayoutOffset = currentLayoutOffset
            val calculatedEndOffset = para.getEndOffset(null)
            // Empty paragraphs or unsupported runs can produce a paragraph view
            // whose calculated end is equal to its start. Do not let that stall
            // pagination forever: advance to the model paragraph end first.
            if (calculatedEndOffset <= previousLayoutOffset) {
                val paragraphEndOffset = elem?.getEndOffset() ?: previousLayoutOffset
                val fallbackEndOffset = if (paragraphEndOffset > previousLayoutOffset) {
                    paragraphEndOffset
                } else {
                    previousLayoutOffset + 1L
                }
                currentLayoutOffset = minOf(fallbackEndOffset, maxEnd)
                para.setEndOffset(currentLayoutOffset)
                Log.w(
                    "OfficePageLayout",
                    "layout paragraph made no progress start=$previousLayoutOffset " +
                        "calculatedEnd=$calculatedEndOffset fallbackEnd=$currentLayoutOffset maxEnd=$maxEnd"
                )
            } else {
                currentLayoutOffset = calculatedEndOffset
            }
            Log.e("currentLayoutOffset", "" + currentLayoutOffset)
            spanH -= paraHeight
            if (spanH > 0 && currentLayoutOffset < maxEnd && breakType != WPViewConstant.BREAK_LIMIT.toInt()
                && breakType != WPViewConstant.BREAK_PAGE.toInt()
            ) {
                elem = doc.getParagraph(currentLayoutOffset)
                if (AttrManage.instance().hasAttribute(elem!!.getAttribute(), AttrIDConstant.PARA_LEVEL_ID)) {
                    if (elem !== para.getElement()) {
                        tableLayout!!.clearBreakPages()
                    }
                    elem = (doc as WPDocument).getParagraph0(currentLayoutOffset)
                    para = ViewFactory.createView(root.getControl()!!, elem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
                } else {
                    para = ViewFactory.createView(root.getControl()!!, elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
                }
                Log.e("WPLayouter.LayoutPage.166", "para.setStartOffset = " + currentLayoutOffset)
                para.setStartOffset(currentLayoutOffset)
                pageView.appendChlidView(para)
            }
            flag = ViewKit.instance().setBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), false)
            breakPara = null
            keepOne = false
        }
        // table
        if (removedUnfitPara) {
            // Restart from the removed paragraph or table on the next page, from the model at currentLayoutOffset
            breakPara = null
        } else if (para.getType() == WPViewConstant.TABLE_VIEW && tableLayout!!.isTableBreakPages()) {
            breakPara = ViewFactory.createView(root.getControl()!!, elem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
            pageView.setHasBreakTable(true)
            (para as TableView).setBreakPages(true)
            Log.e("WPLayouter.layoutPage", "para.getType() = " + "WPViewConstant.TABLE_VIEW and " + (if (breakPara != null) "breakPara != null" else "breakPara == null"))
        } else if (elem != null && currentLayoutOffset < elem.getEndOffset()) {
            breakPara = ViewFactory.createView(root.getControl()!!, elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
            Log.e("WPLayouter.layoutPage", "else para.getType() other breakPara " + (if (breakPara != null) "breakPara != null" else "breakPara == null"))
        }
        // A DOCX may leave a stale break paragraph after the last paragraph has
        // already been consumed. Keeping it makes LayoutThread create empty pages
        // forever because isLayoutFinish() never becomes true.
        if (currentLayoutOffset >= maxEnd) {
            if (breakPara != null) {
                Log.d("OfficePageLayout", "clear stale breakPara at document end offset=$currentLayoutOffset maxEnd=$maxEnd")
            }
            breakPara = null
        }
        Log.e("WPLayouter.185", "pageView.setEndOffset = " + currentLayoutOffset)
        pageView.setEndOffset(currentLayoutOffset)
        //
        root.getViewContainer().sort()
        //
        val pageStartOffset = pageView.getStartOffset(null)
        val pageEndOffset = pageView.getEndOffset(null)
        if (pageEndOffset <= pageStartOffset || pageEndOffset <= lastCommittedPageEndOffset) {
            Log.e(
                "OfficePageLayout",
                "stop invalid page progress page=${pageView.getPageNumber()} " +
                    "start=$pageStartOffset end=$pageEndOffset lastEnd=$lastCommittedPageEndOffset " +
                    "currentOffset=$currentLayoutOffset maxEnd=$maxEnd"
            )
            breakPara = null
            currentLayoutOffset = maxEnd
            return breakType
        }
        lastCommittedPageEndOffset = pageEndOffset
        root.addPageView(pageView)
        //
        pageView.setPageBackgroundColor(pageAttr.pageBRColor)
        //
        pageView.setPageBorder(pageAttr.pageBorder)

        return breakType
    }

    private fun layoutHeaderAndFooter(pageView: PageView) {
        val pageAttr = pageAttr!!
        if (header == null) {
            header = layoutHFParagraph(pageView, true)
            if (header != null) {
                val h = header!!.getLayoutSpan(WPViewConstant.Y_AXIS)
                if (pageAttr.headerMargin + h > pageAttr.topMargin) {
                    pageAttr.topMargin = pageAttr.headerMargin + h
                }
                header!!.setParentView(pageView)
            }
        } else {
            for (sv in shapeViews) {
                if (WPViewKit.instance().getArea(sv.getStartOffset(null)) == WPModelConstant.HEADER) {
                    pageView.addShapeView(sv)
                }
            }
        }
        pageView.setHeader(header)
        if (footer == null) {
            footer = layoutHFParagraph(pageView, false)
            if (footer != null) {
                if (footer!!.getY() < pageAttr.pageHeight - pageAttr.bottomMargin) {
                    pageAttr.bottomMargin = pageAttr.pageHeight - footer!!.getY()
                }
                footer!!.setParentView(pageView)
            }
        } else {
            for (sv in shapeViews) {
                if (WPViewKit.instance().getArea(sv.getStartOffset(null)) == WPModelConstant.FOOTER) {
                    pageView.addShapeView(sv)
                }
            }
        }

        pageView.setFooter(footer)
    }

    private fun layoutHFParagraph(pageView: PageView, isHeader: Boolean): TitleView? {
        val doc = doc!!
        val pageAttr = pageAttr!!
        val root = root!!
        var offset = if (isHeader) WPModelConstant.HEADER else WPModelConstant.FOOTER
        var breakType = WPViewConstant.BREAK_NO.toInt()
        val hfElem = doc.getHFElement(offset, WPModelConstant.HF_ODD) ?: return null

        //ignore line pitch for header and footer layout
        val oldLinePitch = pageAttr.pageLinePitch
        pageAttr.pageLinePitch = -1f

        val titleView = ViewFactory.createView(root.getControl()!!, hfElem, null, WPViewConstant.TITLE_VIEW.toInt()) as TitleView
        titleView.setPageRoot(root)
        titleView.setLocation(pageAttr.leftMargin, pageAttr.headerMargin)

        val maxEnd = hfElem.getEndOffset()
        val spanW = pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin
        var spanH = (pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin - 100) / 2
        var flag = ViewKit.instance().setBitValue(0, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), true)
        var para: ParagraphView
        var paraElem = doc.getParagraph(offset)
        if (AttrManage.instance().hasAttribute(paraElem!!.getAttribute(), AttrIDConstant.PARA_LEVEL_ID)) {
            paraElem = (doc as WPDocument).getParagraph0(offset)
            para = ViewFactory.createView(root.getControl()!!, paraElem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
        } else {
            para = ViewFactory.createView(root.getControl()!!, paraElem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
        }
        titleView.appendChlidView(para)

        Log.e("WPLayouter.layoutHFParagraph.272", "para.setStartOffset = " + offset)
        para.setStartOffset(offset)
        Log.e("WPLayouter.275", "para.setEndOffset = " + paraElem!!.getEndOffset())
        para.setEndOffset(paraElem.getEndOffset())
        var keepOne = true
        val dx = 0
        var dy = 0
        var titleHeight = 0
        while (spanH > 0 && offset < maxEnd && breakType != WPViewConstant.BREAK_LIMIT.toInt()) {
            para.setLocation(dx, dy)
            // 表格段落
            if (para.getType() == WPViewConstant.TABLE_VIEW) {
                breakType = hfTableLayout!!.layoutTable(root.getControl()!!, doc, root, docAttr, pageAttr, paraAttr!!,
                    para as TableView, offset, dx, dy, spanW, spanH, flag, breakPara != null)
            } else {
                hfTableLayout!!.clearBreakPages()
                AttrManage.instance().fillParaAttr(root.getControl(), paraAttr, paraElem!!.getAttribute())
                breakType = LayoutKit.instance().layoutPara(root.getControl()!!, doc, docAttr!!, pageAttr, paraAttr!!,
                    para, offset, dx, dy, spanW, spanH, flag)
            }
            val paraHeight = para.getLayoutSpan(WPViewConstant.Y_AXIS)
            if (!keepOne && para.getChildView() == null) {
                titleView.deleteView(para, true)
                break
            }
            dy += paraHeight
            titleHeight += paraHeight
            offset = para.getEndOffset(null)
            spanH -= paraHeight
            // 收集段落中的 shape view
            collectShapeView(pageView, para, true)
            if (spanH > 0 && offset < maxEnd && breakType != WPViewConstant.BREAK_LIMIT.toInt()) {
                paraElem = doc.getParagraph(offset)
                if (AttrManage.instance().hasAttribute(paraElem!!.getAttribute(), AttrIDConstant.PARA_LEVEL_ID)) {
                    paraElem = (doc as WPDocument).getParagraph0(offset)
                    para = ViewFactory.createView(root.getControl()!!, paraElem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
                } else {
                    para = ViewFactory.createView(root.getControl()!!, paraElem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
                }
                Log.e("WPLayouter.310", "para.setStartOffset = " + offset)
                para.setStartOffset(offset)
                titleView.appendChlidView(para)
            }
            flag = ViewKit.instance().setBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), false)
            keepOne = false
        }
        titleView.setSize(spanW, titleHeight)
        if (!isHeader) {
            titleView.setY(pageAttr.pageHeight - titleHeight - pageAttr.footerMargin)
        }

        //restore line pitch
        pageAttr.pageLinePitch = oldLinePitch

        return titleView
    }

    fun backLayout() {
        if (doc == null || isLayoutFinish()) {
            Log.d(
                "OfficePageLayout",
                "backLayout skipped pages=${root?.getPageCount()} currentOffset=$currentLayoutOffset " +
                    "finish=${doc != null && isLayoutFinish()}"
            )
            return
        }
        Log.d(
            "OfficePageLayout",
            "backLayout start pages=${root?.getPageCount()} currentOffset=$currentLayoutOffset " +
                "breakPara=${breakPara != null} finish=${if (doc != null) isLayoutFinish() else false} " +
                "thread=${Thread.currentThread().name}"
        )
        val pv = ViewFactory.createView(root!!.getControl()!!, section, null, WPViewConstant.PAGE_VIEW.toInt()) as PageView
        // The UI thread draws the page as soon as it is linked, while it is still being filled
        // below; at its default (0, 0) that drew it over the first page until layoutAllPage
        // moved it. Place it under the last page first.
        root!!.getLastView()?.let { last ->
            val space = (root!!.getContainer() as? Word)?.getPageSpacing() ?: WPViewConstant.PAGE_SPACE.toInt()
            pv.setLocation(last.getX(), last.getY() + last.getHeight() + space)
        }
        root!!.appendChlidView(pv)
        layoutPage(pv)
        Log.d(
            "OfficePageLayout",
            "backLayout end pages=${root?.getPageCount()} currentOffset=$currentLayoutOffset " +
                "breakPara=${breakPara != null} finish=${isLayoutFinish()}"
        )
    }

    /**
     * @return Returns the currentLayoutOffset.
     */
    fun getCurrentLayoutOffset(): Long {
        return currentLayoutOffset
    }

    /**
     * @param currentLayoutOffset The currentLayoutOffset to set.
     */
    fun setCurrentLayoutOffset(currentLayoutOffset: Long) {
        this.currentLayoutOffset = currentLayoutOffset
    }

    fun isLayoutFinish(): Boolean {
        Log.e("WPLAYOUTER", "isLayoutFinish currentLayoutOffset " + currentLayoutOffset + " doc.getAreaEnd(WPModelConstant.MAIN) = " + doc!!.getAreaEnd(WPModelConstant.MAIN))
        Log.e("WPLAYOUTER", "breakPara " + (if (breakPara != null) " != null" else "== null"))
        // areaEnd is the authoritative end of the main document. breakPara can
        // be stale after the final paragraph and must not keep pagination alive.
        return currentLayoutOffset >= doc!!.getAreaEnd(WPModelConstant.MAIN)
    }

    private fun collectShapeView(page: PageView, para: ParagraphView, isHF: Boolean) {
        if (para.getType() == WPViewConstant.PARAGRAPH_VIEW) {
            collectShapeViewForPara(page, para, isHF)
        } else if (para.getType() == WPViewConstant.TABLE_VIEW) {
            var row = para.getChildView()
            while (row != null) {
                var cell = row.getChildView()
                while (cell != null) {
                    var paraView = cell.getChildView()
                    while (paraView != null) {
                        collectShapeViewForPara(page, para, isHF)
                        paraView = paraView.getNextView()
                    }
                    cell = cell.getNextView()
                }
                row = row.getNextView()
            }
        }
    }

    private fun collectShapeViewForPara(page: PageView, para: ParagraphView, isHF: Boolean) {
        var line = para.getChildView()
        while (line != null) {
            var leaf = line.getChildView()
            while (leaf != null) {
                if (leaf.getType() == WPViewConstant.SHAPE_VIEW) {
                    val shapeView = leaf as ShapeView
                    if (!shapeView.isInline()) {
                        page.addShapeView(shapeView)
                        if (isHF) {
                            shapeViews.add(shapeView)
                        }
                    }
                } else if (leaf.getType() == WPViewConstant.OBJ_VIEW) {
                    val objView = leaf as ObjView
                    if (!objView.isInline()) {
                        page.addShapeView(objView)
                        if (isHF) {
                            shapeViews.add(objView)
                        }
                    }
                }
                leaf = leaf.getNextView()
            }
            line = line.getNextView()
        }
    }

    fun dispose() {
        docAttr?.dispose()
        docAttr = null
        pageAttr?.dispose()
        pageAttr = null
        paraAttr?.dispose()
        paraAttr = null
        root = null
        doc = null
        breakPara = null
        header = null
        footer = null
        tableLayout = null
        hfTableLayout = null
        shapeViews.clear()
    }
}

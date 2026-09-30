/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.wp.view

import com.wxiwei.office.common.shape.WPAutoShape
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.PageAttr

class PositionLayoutKit private constructor() {

    companion object {
        private val kit = PositionLayoutKit()

        @JvmStatic
        fun instance(): PositionLayoutKit {
            return kit
        }
    }

    /**
     * Y of [view] on its page (or in its header/footer, which the layouter offsets later). A
     * paragraph in a table cell is placed relative to the cell, so the cell, row and table
     * positions are added; a body paragraph's own Y already is page based.
     */
    private fun pageY(view: IView): Int {
        var y = 0
        var v: IView? = view
        while (v != null && v !is PageView && v !is TitleView) {
            y += v.getY()
            v = v.getParentView()
        }
        return y
    }

    /**
     * Left edge of the "column" a shape is positioned against: the text area of the table cell
     * holding its anchor paragraph (Word's layoutInCell), else the page's left margin.
     */
    private fun columnLeft(leafView: LeafView, pageAttr: PageAttr): Int {
        var v: IView? = leafView.getParentView()
        var cell: CellView? = null
        while (v != null && v !is PageView && v !is TitleView) {
            if (v is CellView) {
                cell = v
                break
            }
            v = v.getParentView()
        }
        if (cell == null) return pageAttr.leftMargin
        var x = cell.getLeftIndent()
        var p: IView? = cell
        while (p != null && p !is PageView && p !is TitleView) {
            x += p.getX()
            p = p.getParentView()
        }
        return x
    }

    fun processShapePosition(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        processHorizontalPosition(leafView, wpShape, pageAttr)

        processVerticalPosition(leafView, wpShape, pageAttr)
    }

    private fun processHorizontalPosition(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val posType = wpShape.horPositionType.toInt()
        val horRelative = wpShape.horizontalRelativeTo.toInt()

        if (posType == com.wxiwei.office.common.shape.WPAbstractShape.POSITIONTYPE_RELATIVE.toInt()) {
            //relative postion
            val ratio = wpShape.horRelativeValue / 1000f

            if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                leafView.setX(Math.round(pageAttr.pageWidth * ratio))
            } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                leafView.setX(pageAttr.leftMargin + Math.round((pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin) * ratio))
            } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LEFT.toInt()) {
                leafView.setX(Math.round(pageAttr.leftMargin * ratio))
            } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_RIGHT.toInt()) {
                leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + Math.round(pageAttr.rightMargin * ratio))
            } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()) {
                if (leafView.getParentView() != null
                    && leafView.getParentView()!!.getParentView() != null
                    && leafView.getParentView()!!.getParentView()!!.getParentView() != null
                ) {
                    val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                    if (pageView.getPageNumber() % 2 == 1) {
                        //Odd page
                        leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + Math.round(pageAttr.rightMargin * ratio))
                    } else {
                        //Even page
                        leafView.setX(Math.round(pageAttr.leftMargin * ratio))
                    }
                }
            } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(Math.round(pageAttr.leftMargin * ratio))
                } else {
                    //Even page
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + Math.round(pageAttr.rightMargin * ratio))
                }
            }
        } else {
            val horPosition = wpShape.horizontalAlignment.toInt()
            if (horPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_ABSOLUTE.toInt()) {
                processHorizontalPosition_Absolute(leafView, wpShape, pageAttr)
            } else if (horPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_LEFT.toInt()) {
                //left alignment
                processHorizontalPosition_Left(leafView, wpShape, pageAttr)
            } else if (horPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_CENTER.toInt()) {
                //center alignment
                processHorizontalPosition_Center(leafView, wpShape, pageAttr)
            } else if (horPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_RIGHT.toInt()) {
                //right alignment
                processHorizontalPosition_Right(leafView, wpShape, pageAttr)
            } else if (horPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_INSIDE.toInt()) {
                processHorizontalPosition_Inside(leafView, wpShape, pageAttr)
            } else if (horPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_OUTSIDE.toInt()) {
                processHorizontalPosition_Outside(leafView, wpShape, pageAttr)
            }
        }
    }

    private fun processHorizontalPosition_Absolute(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)

        val horRelative = wpShape.horizontalRelativeTo.toInt()
        if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_COLUMN.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_CHARACTER.toInt()
        ) {
            leafView.setX(columnLeft(leafView, pageAttr) + r.x)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LEFT.toInt()
        ) {
            leafView.setX(r.x)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_RIGHT.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + r.x)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + r.x)
                } else {
                    //Even page
                    leafView.setX(r.x)
                }
            }
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(r.x)
                } else {
                    //Even page
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + r.x)
                }
            }
        }
    }

    private fun processHorizontalPosition_Left(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val horRelative = wpShape.horizontalRelativeTo.toInt()
        if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_COLUMN.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_CHARACTER.toInt()
        ) {
            leafView.setX(pageAttr.leftMargin)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LEFT.toInt()
        ) {
            leafView.setX(0)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_RIGHT.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin)
                } else {
                    //Even page
                    leafView.setX(0)
                }
            }
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(0)
                } else {
                    //Even page
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin)
                }
            }
        }
    }

    private fun processHorizontalPosition_Center(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)
        val halfShapeWidth = r.width / 2

        val horRelative = wpShape.horizontalRelativeTo.toInt()
        if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
            leafView.setX(pageAttr.pageWidth / 2 - halfShapeWidth)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_COLUMN.toInt()
        ) {
            leafView.setX(pageAttr.leftMargin + (pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin) / 2 - halfShapeWidth)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_CHARACTER.toInt()) {
            leafView.setX(pageAttr.leftMargin - halfShapeWidth)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LEFT.toInt()) {
            leafView.setX(pageAttr.leftMargin / 2 - halfShapeWidth)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_RIGHT.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin / 2 - halfShapeWidth)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin / 2 - halfShapeWidth)
                } else {
                    //Even page
                    leafView.setX(pageAttr.leftMargin / 2 - halfShapeWidth)
                }
            }
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(pageAttr.leftMargin / 2 - halfShapeWidth)
                } else {
                    //Even page
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin / 2 - halfShapeWidth)
                }
            }
        }
    }

    private fun processHorizontalPosition_Right(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)
        val horRelative = wpShape.horizontalRelativeTo.toInt()
        if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_RIGHT.toInt()
        ) {
            leafView.setX(pageAttr.pageWidth - r.width)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt() || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_COLUMN.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin - r.width)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_CHARACTER.toInt()
            || horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LEFT.toInt()
        ) {
            leafView.setX(pageAttr.leftMargin - r.width)
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(pageAttr.pageWidth - r.width)
                } else {
                    //Even page
                    leafView.setX(pageAttr.leftMargin - r.width)
                }
            }
        } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setX(pageAttr.leftMargin - r.width)
                } else {
                    //Even page
                    leafView.setX(pageAttr.pageWidth - r.width)
                }
            }
        }
    }

    private fun processHorizontalPosition_Inside(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        if (leafView.getParentView() != null
            && leafView.getParentView()!!.getParentView() != null
            && leafView.getParentView()!!.getParentView()!!.getParentView() != null
        ) {
            val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView

            val r = requireNotNull(wpShape.bounds)
            val horRelative = wpShape.horizontalRelativeTo.toInt()

            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(0)
                } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setX(pageAttr.leftMargin)
                }
            } else {
                //Even page
                if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(pageAttr.pageWidth - r.width)
                } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin - r.width)
                }
            }
        }
    }

    private fun processHorizontalPosition_Outside(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        if (leafView.getParentView() != null
            && leafView.getParentView()!!.getParentView() != null
            && leafView.getParentView()!!.getParentView()!!.getParentView() != null
        ) {
            val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView

            val r = requireNotNull(wpShape.bounds)
            val horRelative = wpShape.horizontalRelativeTo.toInt()

            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(pageAttr.pageWidth - r.width)
                } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin - r.width)
                }
            } else {
                //Even page
                if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(0)
                } else if (horRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setX(pageAttr.leftMargin)
                }
            }
        }
    }

    private fun processVerticalPosition(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val posType = wpShape.verPositionType.toInt()
        val verRelative = wpShape.verticalRelativeTo.toInt()

        if (posType == com.wxiwei.office.common.shape.WPAbstractShape.POSITIONTYPE_RELATIVE.toInt()) {
            //relative postion
            val ratio = wpShape.verRelativeValue / 1000f

            if (verRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                leafView.setY(Math.round(pageAttr.pageHeight * ratio))
            } else if (verRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                leafView.setY(pageAttr.topMargin + Math.round((pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin) * ratio))
            } else if (verRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()) {
                leafView.setY(Math.round(pageAttr.topMargin * ratio))
            } else if (verRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
                leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin + Math.round(pageAttr.bottomMargin * ratio))
            } else if (verRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
                || verRelative == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
            ) {
                if (leafView.getParentView() != null
                    && leafView.getParentView()!!.getParentView() != null
                    && leafView.getParentView()!!.getParentView()!!.getParentView() != null
                ) {
                    val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                    if (pageView.getPageNumber() % 2 == 1) {
                        //Odd page
                        leafView.setY(Math.round(pageAttr.topMargin * ratio))
                    } else {
                        //Even page
                        leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin + Math.round(pageAttr.bottomMargin * ratio))
                    }
                }
            }
        } else {
            val verPosition = wpShape.verticalAlignment.toInt()
            if (verPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_ABSOLUTE.toInt()) {
                processVerticalPosition_Absolute(leafView, wpShape, pageAttr)
            } else if (verPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_TOP.toInt()) {
                processVerticalPosition_Top(leafView, wpShape, pageAttr)
            } else if (verPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_CENTER.toInt()) {
                processVerticalPosition_Center(leafView, wpShape, pageAttr)
            } else if (verPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_BOTTOM.toInt()) {
                processVerticalPosition_Bottom(leafView, wpShape, pageAttr)
            } else if (verPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_INSIDE.toInt()) {
                processVerticalPosition_Inside(leafView, wpShape, pageAttr)
            } else if (verPosition == com.wxiwei.office.common.shape.WPAbstractShape.ALIGNMENT_OUTSIDE.toInt()) {
                processVerticalPosition_Outside(leafView, wpShape, pageAttr)
            }
        }
    }

    private fun processVerticalPosition_Absolute(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)
        val verRelativeTo = wpShape.verticalRelativeTo.toInt()

        if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()
        ) {
            leafView.setY(r.y)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setY(r.y)
                } else {
                    //Even page
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin + r.y)
                }
            }
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.topMargin + r.y)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(pageY(paraView) + r.y)
            }
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin + r.y)
        }
    }

    private fun processVerticalPosition_Top(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val verRelativeTo = wpShape.verticalRelativeTo.toInt()

        if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()
        ) {
            leafView.setY(0)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setY(0)
                } else {
                    //Even page
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
                }
            }
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.topMargin)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(pageY(paraView))
            }
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
        }
    }

    private fun processVerticalPosition_Center(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)
        val verRelativeTo = wpShape.verticalRelativeTo.toInt()
        val halfShapeHeight = r.height / 2
        if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
            leafView.setY(pageAttr.pageHeight / 2 - halfShapeHeight)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.topMargin + (pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin) / 2 - halfShapeHeight)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()) {
            leafView.setY(pageAttr.topMargin / 2 - halfShapeHeight)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setY(pageAttr.topMargin / 2 - halfShapeHeight)
                } else {
                    //Even page
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin / 2 - halfShapeHeight)
                }
            }
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin / 2 - halfShapeHeight)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(pageY(paraView) - halfShapeHeight)
            }
        }
    }

    private fun processVerticalPosition_Bottom(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)
        val verRelativeTo = wpShape.verticalRelativeTo.toInt()

        if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt() || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - r.height)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin - r.height)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(pageY(paraView) + paraView.getHeight() - r.height)
            }
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()) {
            leafView.setY(pageAttr.topMargin - r.height)
        } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
            || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() != null
                && leafView.getParentView()!!.getParentView()!!.getParentView() != null
            ) {
                val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
                if (pageView.getPageNumber() % 2 == 1) {
                    //Odd page
                    leafView.setY(pageAttr.topMargin - r.height)
                } else {
                    //Even page
                    leafView.setY(pageAttr.pageHeight - r.height)
                }
            }
        }
    }

    private fun processVerticalPosition_Inside(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)
        val verRelativeTo = wpShape.verticalRelativeTo.toInt()
        if (leafView.getParentView() != null
            && leafView.getParentView()!!.getParentView() != null
            && leafView.getParentView()!!.getParentView()!!.getParentView() != null
        ) {
            val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.headerMargin / 2)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.topMargin)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(pageY(paraView))
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(0)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(0)
                }
            } else {
                //Even page
                if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.footerMargin)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(pageY(paraView) + paraView.getHeight() - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(pageAttr.topMargin - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(pageAttr.pageHeight - r.height)
                }
            }
        }
    }

    private fun processVerticalPosition_Outside(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = requireNotNull(wpShape.bounds)
        val verRelativeTo = wpShape.verticalRelativeTo.toInt()
        if (leafView.getParentView() != null
            && leafView.getParentView()!!.getParentView() != null
            && leafView.getParentView()!!.getParentView()!!.getParentView() != null
        ) {
            val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.footerMargin)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(pageY(paraView) + paraView.getHeight() - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(pageAttr.topMargin - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - r.height)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(pageAttr.topMargin - r.height)
                }
            } else {
                //Even page
                if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.headerMargin / 2)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.topMargin)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(pageY(paraView))
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(0)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
                } else if (verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == com.wxiwei.office.common.shape.WPAbstractShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
                }
            }
        }
    }
}

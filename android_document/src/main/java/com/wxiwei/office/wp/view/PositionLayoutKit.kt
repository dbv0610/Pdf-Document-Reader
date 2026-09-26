package com.wxiwei.office.wp.view

import com.wxiwei.office.common.shape.WPAutoShape
import com.wxiwei.office.simpletext.view.PageAttr

class PositionLayoutKit private constructor() {

    companion object {
        private val kit = PositionLayoutKit()

        @JvmStatic
        fun instance(): PositionLayoutKit {
            return kit
        }
    }

    fun processShapePosition(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        processHorizontalPosition(leafView, wpShape, pageAttr)

        processVerticalPosition(leafView, wpShape, pageAttr)
    }

    private fun processHorizontalPosition(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val posType = wpShape.getHorPositionType().toInt()
        val horRelative = wpShape.getHorizontalRelativeTo().toInt()

        if (posType == WPAutoShape.POSITIONTYPE_RELATIVE.toInt()) {
            //relative postion
            val ratio = wpShape.getHorRelativeValue() / 1000f

            if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()) {
                leafView.setX(Math.round(pageAttr.pageWidth * ratio))
            } else if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                leafView.setX(pageAttr.leftMargin + Math.round((pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin) * ratio))
            } else if (horRelative == WPAutoShape.RELATIVE_LEFT.toInt()) {
                leafView.setX(Math.round(pageAttr.leftMargin * ratio))
            } else if (horRelative == WPAutoShape.RELATIVE_RIGHT.toInt()) {
                leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + Math.round(pageAttr.rightMargin * ratio))
            } else if (horRelative == WPAutoShape.RELATIVE_OUTER.toInt()) {
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
            } else if (horRelative == WPAutoShape.RELATIVE_INNER.toInt()) {
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
            val horPosition = wpShape.getHorizontalAlignment().toInt()
            if (horPosition == WPAutoShape.ALIGNMENT_ABSOLUTE.toInt()) {
                processHorizontalPosition_Absolute(leafView, wpShape, pageAttr)
            } else if (horPosition == WPAutoShape.ALIGNMENT_LEFT.toInt()) {
                //left alignment
                processHorizontalPosition_Left(leafView, wpShape, pageAttr)
            } else if (horPosition == WPAutoShape.ALIGNMENT_CENTER.toInt()) {
                //center alignment
                processHorizontalPosition_Center(leafView, wpShape, pageAttr)
            } else if (horPosition == WPAutoShape.ALIGNMENT_RIGHT.toInt()) {
                //right alignment
                processHorizontalPosition_Right(leafView, wpShape, pageAttr)
            } else if (horPosition == WPAutoShape.ALIGNMENT_INSIDE.toInt()) {
                processHorizontalPosition_Inside(leafView, wpShape, pageAttr)
            } else if (horPosition == WPAutoShape.ALIGNMENT_OUTSIDE.toInt()) {
                processHorizontalPosition_Outside(leafView, wpShape, pageAttr)
            }
        }
    }

    private fun processHorizontalPosition_Absolute(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = wpShape.getBounds()

        val horRelative = wpShape.getHorizontalRelativeTo().toInt()
        if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()
            || horRelative == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
            || horRelative == WPAutoShape.RELATIVE_COLUMN.toInt()
            || horRelative == WPAutoShape.RELATIVE_CHARACTER.toInt()
        ) {
            leafView.setX(pageAttr.leftMargin + r.x)
        } else if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()
            || horRelative == WPAutoShape.RELATIVE_LEFT.toInt()
        ) {
            leafView.setX(r.x)
        } else if (horRelative == WPAutoShape.RELATIVE_RIGHT.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin + r.x)
        } else if (horRelative == WPAutoShape.RELATIVE_OUTER.toInt()) {
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
        } else if (horRelative == WPAutoShape.RELATIVE_INNER.toInt()) {
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
        val horRelative = wpShape.getHorizontalRelativeTo().toInt()
        if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()
            || horRelative == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
            || horRelative == WPAutoShape.RELATIVE_COLUMN.toInt()
            || horRelative == WPAutoShape.RELATIVE_CHARACTER.toInt()
        ) {
            leafView.setX(pageAttr.leftMargin)
        } else if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()
            || horRelative == WPAutoShape.RELATIVE_LEFT.toInt()
        ) {
            leafView.setX(0)
        } else if (horRelative == WPAutoShape.RELATIVE_RIGHT.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin)
        } else if (horRelative == WPAutoShape.RELATIVE_OUTER.toInt()) {
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
        } else if (horRelative == WPAutoShape.RELATIVE_INNER.toInt()) {
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
        val r = wpShape.getBounds()
        val halfShapeWidth = r.width / 2

        val horRelative = wpShape.getHorizontalRelativeTo().toInt()
        if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()) {
            leafView.setX(pageAttr.pageWidth / 2 - halfShapeWidth)
        } else if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()
            || horRelative == WPAutoShape.RELATIVE_COLUMN.toInt()
        ) {
            leafView.setX(pageAttr.leftMargin + (pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin) / 2 - halfShapeWidth)
        } else if (horRelative == WPAutoShape.RELATIVE_CHARACTER.toInt()) {
            leafView.setX(pageAttr.leftMargin - halfShapeWidth)
        } else if (horRelative == WPAutoShape.RELATIVE_LEFT.toInt()) {
            leafView.setX(pageAttr.leftMargin / 2 - halfShapeWidth)
        } else if (horRelative == WPAutoShape.RELATIVE_RIGHT.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin / 2 - halfShapeWidth)
        } else if (horRelative == WPAutoShape.RELATIVE_OUTER.toInt()) {
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
        } else if (horRelative == WPAutoShape.RELATIVE_INNER.toInt()) {
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
        val r = wpShape.getBounds()
        val horRelative = wpShape.getHorizontalRelativeTo().toInt()
        if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()
            || horRelative == WPAutoShape.RELATIVE_RIGHT.toInt()
        ) {
            leafView.setX(pageAttr.pageWidth - r.width)
        } else if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt() || horRelative == WPAutoShape.RELATIVE_COLUMN.toInt()) {
            leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin - r.width)
        } else if (horRelative == WPAutoShape.RELATIVE_CHARACTER.toInt()
            || horRelative == WPAutoShape.RELATIVE_LEFT.toInt()
        ) {
            leafView.setX(pageAttr.leftMargin - r.width)
        } else if (horRelative == WPAutoShape.RELATIVE_OUTER.toInt()) {
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
        } else if (horRelative == WPAutoShape.RELATIVE_INNER.toInt()) {
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

            val r = wpShape.getBounds()
            val horRelative = wpShape.getHorizontalRelativeTo().toInt()

            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(0)
                } else if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setX(pageAttr.leftMargin)
                }
            } else {
                //Even page
                if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(pageAttr.pageWidth - r.width)
                } else if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()) {
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

            val r = wpShape.getBounds()
            val horRelative = wpShape.getHorizontalRelativeTo().toInt()

            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(pageAttr.pageWidth - r.width)
                } else if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setX(pageAttr.pageWidth - pageAttr.rightMargin - r.width)
                }
            } else {
                //Even page
                if (horRelative == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setX(0)
                } else if (horRelative == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setX(pageAttr.leftMargin)
                }
            }
        }
    }

    private fun processVerticalPosition(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val posType = wpShape.getVerPositionType().toInt()
        val verRelative = wpShape.getVerticalRelativeTo().toInt()

        if (posType == WPAutoShape.POSITIONTYPE_RELATIVE.toInt()) {
            //relative postion
            val ratio = wpShape.getVerRelativeValue() / 1000f

            if (verRelative == WPAutoShape.RELATIVE_PAGE.toInt()) {
                leafView.setY(Math.round(pageAttr.pageHeight * ratio))
            } else if (verRelative == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                leafView.setY(pageAttr.topMargin + Math.round((pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin) * ratio))
            } else if (verRelative == WPAutoShape.RELATIVE_TOP.toInt()) {
                leafView.setY(Math.round(pageAttr.topMargin * ratio))
            } else if (verRelative == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
                leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin + Math.round(pageAttr.bottomMargin * ratio))
            } else if (verRelative == WPAutoShape.RELATIVE_OUTER.toInt()
                || verRelative == WPAutoShape.RELATIVE_INNER.toInt()
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
            val verPosition = wpShape.getVerticalAlignment().toInt()
            if (verPosition == WPAutoShape.ALIGNMENT_ABSOLUTE.toInt()) {
                processVerticalPosition_Absolute(leafView, wpShape, pageAttr)
            } else if (verPosition == WPAutoShape.ALIGNMENT_TOP.toInt()) {
                processVerticalPosition_Top(leafView, wpShape, pageAttr)
            } else if (verPosition == WPAutoShape.ALIGNMENT_CENTER.toInt()) {
                processVerticalPosition_Center(leafView, wpShape, pageAttr)
            } else if (verPosition == WPAutoShape.ALIGNMENT_BOTTOM.toInt()) {
                processVerticalPosition_Bottom(leafView, wpShape, pageAttr)
            } else if (verPosition == WPAutoShape.ALIGNMENT_INSIDE.toInt()) {
                processVerticalPosition_Inside(leafView, wpShape, pageAttr)
            } else if (verPosition == WPAutoShape.ALIGNMENT_OUTSIDE.toInt()) {
                processVerticalPosition_Outside(leafView, wpShape, pageAttr)
            }
        }
    }

    private fun processVerticalPosition_Absolute(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = wpShape.getBounds()
        val verRelativeTo = wpShape.getVerticalRelativeTo().toInt()

        if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()
        ) {
            leafView.setY(r.y)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
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
        } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.topMargin + r.y)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(paraView.getY() + r.y)
            }
        } else if (verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin + r.y)
        }
    }

    private fun processVerticalPosition_Top(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val verRelativeTo = wpShape.getVerticalRelativeTo().toInt()

        if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()
        ) {
            leafView.setY(0)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
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
        } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.topMargin)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(paraView.getY())
            }
        } else if (verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
        }
    }

    private fun processVerticalPosition_Center(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = wpShape.getBounds()
        val verRelativeTo = wpShape.getVerticalRelativeTo().toInt()
        val halfShapeHeight = r.height / 2
        if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt()) {
            leafView.setY(pageAttr.pageHeight / 2 - halfShapeHeight)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.topMargin + (pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin) / 2 - halfShapeHeight)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()) {
            leafView.setY(pageAttr.topMargin / 2 - halfShapeHeight)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
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
        } else if (verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin / 2 - halfShapeHeight)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(paraView.getY() - halfShapeHeight)
            }
        }
    }

    private fun processVerticalPosition_Bottom(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = wpShape.getBounds()
        val verRelativeTo = wpShape.getVerticalRelativeTo().toInt()

        if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt() || verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
            leafView.setY(pageAttr.pageHeight - r.height)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
            leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin - r.height)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
        ) {
            if (leafView.getParentView() != null
                && leafView.getParentView()!!.getParentView() is ParagraphView
            ) {
                val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                leafView.setY(paraView.getY() + paraView.getHeight() - r.height)
            }
        } else if (verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()) {
            leafView.setY(pageAttr.topMargin - r.height)
        } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
            || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
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
        val r = wpShape.getBounds()
        val verRelativeTo = wpShape.getVerticalRelativeTo().toInt()
        if (leafView.getParentView() != null
            && leafView.getParentView()!!.getParentView() != null
            && leafView.getParentView()!!.getParentView()!!.getParentView() != null
        ) {
            val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.headerMargin / 2)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.topMargin)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(paraView.getY())
                } else if (verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(0)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(0)
                }
            } else {
                //Even page
                if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.footerMargin)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(paraView.getY() + paraView.getHeight() - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(pageAttr.topMargin - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(pageAttr.pageHeight - r.height)
                }
            }
        }
    }

    private fun processVerticalPosition_Outside(leafView: LeafView, wpShape: WPAutoShape, pageAttr: PageAttr) {
        val r = wpShape.getBounds()
        val verRelativeTo = wpShape.getVerticalRelativeTo().toInt()
        if (leafView.getParentView() != null
            && leafView.getParentView()!!.getParentView() != null
            && leafView.getParentView()!!.getParentView()!!.getParentView() != null
        ) {
            val pageView = leafView.getParentView()!!.getParentView()!!.getParentView() as PageView
            if (pageView.getPageNumber() % 2 == 1) {
                //Odd page
                if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.footerMargin)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(paraView.getY() + paraView.getHeight() - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(pageAttr.topMargin - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - r.height)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(pageAttr.topMargin - r.height)
                }
            } else {
                //Even page
                if (verRelativeTo == WPAutoShape.RELATIVE_PAGE.toInt()) {
                    leafView.setY(pageAttr.headerMargin / 2)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_MARGIN.toInt()) {
                    leafView.setY(pageAttr.topMargin)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_PARAGRAPH.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_LINE.toInt()
                ) {
                    val paraView = leafView.getParentView()!!.getParentView() as ParagraphView
                    leafView.setY(paraView.getY())
                } else if (verRelativeTo == WPAutoShape.RELATIVE_TOP.toInt()) {
                    leafView.setY(0)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_BOTTOM.toInt()) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
                } else if (verRelativeTo == WPAutoShape.RELATIVE_INNER.toInt()
                    || verRelativeTo == WPAutoShape.RELATIVE_OUTER.toInt()
                ) {
                    leafView.setY(pageAttr.pageHeight - pageAttr.bottomMargin)
                }
            }
        }
    }
}

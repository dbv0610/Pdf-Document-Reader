/*
 * 文件名称:           AutoShapeTypes.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:31:41
 */
package com.wxiwei.office.common.autoshape

import com.wxiwei.office.common.shape.ShapeTypes

/**
 * autoShape types
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-9-17
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class AutoShapeTypes

/**
 * 
 */
{
    fun getAutoShapeType(name: String?): Int {
        if (name != null) {
            // line
            if (name == "line") {
                return ShapeTypes.Line
            } else if (name == "straightConnector1") {
                return ShapeTypes.StraightConnector1
            } else if (name == "bentConnector2") {
                return ShapeTypes.BentConnector2
            } else if (name == "bentConnector3") {
                return ShapeTypes.BentConnector3
            } else if (name == "curvedConnector2") {
                return ShapeTypes.CurvedConnector2
            } else if (name == "curvedConnector3") {
                return ShapeTypes.CurvedConnector3
            } else if (name == "curvedConnector4") {
                return ShapeTypes.CurvedConnector4
            } else if (name == "curvedConnector5") {
                return ShapeTypes.CurvedConnector5
            } else if (name == "rect" || name == "Rect") {
                return ShapeTypes.Rectangle
            } else if (name == "roundRect") {
                return ShapeTypes.RoundRectangle
            } else if (name == "round1Rect") {
                return ShapeTypes.Round1Rect
            } else if (name == "round2SameRect") {
                return ShapeTypes.Round2SameRect
            } else if (name == "round2DiagRect") {
                return ShapeTypes.Round2DiagRect
            } else if (name == "snip1Rect") {
                return ShapeTypes.Snip1Rect
            } else if (name == "snip2SameRect") {
                return ShapeTypes.Snip2SameRect
            } else if (name == "snip2DiagRect") {
                return ShapeTypes.Snip2DiagRect
            } else if (name == "snipRoundRect") {
                return ShapeTypes.SnipRoundRect
            } else if (name == "ellipse") {
                return ShapeTypes.Ellipse
            } else if (name == "triangle") {
                return ShapeTypes.Triangle
            } else if (name == "rtTriangle") {
                return ShapeTypes.RtTriangle
            } else if (name == "parallelogram") {
                return ShapeTypes.Parallelogram
            } else if (name == "trapezoid") {
                return ShapeTypes.Trapezoid
            } else if (name == "diamond") {
                return ShapeTypes.Diamond
            } else if (name == "pentagon") {
                return ShapeTypes.Pentagon
            } else if (name == "hexagon") {
                return ShapeTypes.Hexagon
            } else if (name == "heptagon") {
                return ShapeTypes.Heptagon
            } else if (name == "octagon") {
                return ShapeTypes.Octagon
            } else if (name == "decagon") {
                return ShapeTypes.Decagon
            } else if (name == "dodecagon") {
                return ShapeTypes.Dodecagon
            } else if (name == "pie") {
                return ShapeTypes.Pie
            } else if (name == "chord") {
                return ShapeTypes.Chord
            } else if (name == "teardrop") {
                return ShapeTypes.Teardrop
            } else if (name == "frame") {
                return ShapeTypes.Frame
            } else if (name == "halfFrame") {
                return ShapeTypes.HalfFrame
            } else if (name == "corner") {
                return ShapeTypes.Corner
            } else if (name == "diagStripe") {
                return ShapeTypes.DiagStripe
            } else if (name == "plus") {
                return ShapeTypes.Plus
            } else if (name == "plaque") {
                return ShapeTypes.Plaque
            } else if (name == "can") {
                return ShapeTypes.Can
            } else if (name == "cube") {
                return ShapeTypes.Cube
            } else if (name == "bevel") {
                return ShapeTypes.Bevel
            } else if (name == "donut") {
                return ShapeTypes.Donut
            } else if (name == "noSmoking") {
                return ShapeTypes.NoSmoking
            } else if (name == "blockArc") {
                return ShapeTypes.BlockArc
            } else if (name == "foldedCorner") {
                return ShapeTypes.FoldedCorner
            } else if (name == "smileyFace") {
                return ShapeTypes.SmileyFace
            } else if (name == "heart") {
                return ShapeTypes.Heart
            } else if (name == "lightningBolt") {
                return ShapeTypes.LightningBolt
            } else if (name == "sun") {
                return ShapeTypes.Sun
            } else if (name == "moon") {
                return ShapeTypes.Moon
            } else if (name == "cloud") {
                return ShapeTypes.Cloud
            } else if (name == "arc") {
                return ShapeTypes.Arc
            } else if (name == "bracketPair") {
                return ShapeTypes.BracketPair
            } else if (name == "bracePair") {
                return ShapeTypes.BracePair
            } else if (name == "leftBracket") {
                return ShapeTypes.LeftBracket
            } else if (name == "rightBracket") {
                return ShapeTypes.RightBracket
            } else if (name == "leftBrace") {
                return ShapeTypes.LeftBrace
            } else if (name == "rightBrace") {
                return ShapeTypes.RightBrace
            } else if (name == "mathPlus") {
                return ShapeTypes.MathPlus
            } else if (name == "mathMinus") {
                return ShapeTypes.MathMinus
            } else if (name == "mathMultiply") {
                return ShapeTypes.MathMultiply
            } else if (name == "mathDivide") {
                return ShapeTypes.MathDivide
            } else if (name == "mathEqual") {
                return ShapeTypes.MathEqual
            } else if (name == "mathNotEqual") {
                return ShapeTypes.MathNotEqual
            } else if (name == "rightArrow")  /**/////////////arrow */
            {
                return ShapeTypes.RightArrow
            } else if (name == "leftArrow") {
                return ShapeTypes.LeftArrow
            } else if (name == "upArrow") {
                return ShapeTypes.UpArrow
            } else if (name == "downArrow") {
                return ShapeTypes.DownArrow
            } else if (name == "leftRightArrow") {
                return ShapeTypes.LeftRightArrow
            } else if (name == "upDownArrow") {
                return ShapeTypes.UpDownArrow
            } else if (name == "upDownArrow") {
                return ShapeTypes.UpDownArrow
            } else if (name == "quadArrow")  //十字箭头
            {
                return ShapeTypes.QuadArrow
            } else if (name == "leftRightUpArrow")  //丁字箭头
            {
                return ShapeTypes.LeftRightUpArrow
            } else if (name == "bentArrow")  //圆角右箭头
            {
                return ShapeTypes.BentArrow
            } else if (name == "uturnArrow")  //手杖形箭头
            {
                return ShapeTypes.UturnArrow
            } else if (name == "leftUpArrow")  //直角双向箭头
            {
                return ShapeTypes.LeftUpArrow
            } else if (name == "bentUpArrow")  //直角上箭头
            {
                return ShapeTypes.BentUpArrow
            } else if (name == "curvedRightArrow")  //左弧形箭头
            {
                return ShapeTypes.CurvedRightArrow
            } else if (name == "curvedLeftArrow")  //右弧形箭头
            {
                return ShapeTypes.CurvedLeftArrow
            } else if (name == "curvedUpArrow")  //下弧形箭头
            {
                return ShapeTypes.CurvedUpArrow
            } else if (name == "curvedDownArrow")  //上弧形箭头
            {
                return ShapeTypes.CurvedDownArrow
            } else if (name == "stripedRightArrow")  //虚尾箭头
            {
                return ShapeTypes.StripedRightArrow
            } else if (name == "notchedRightArrow")  //燕尾形箭头
            {
                return ShapeTypes.NotchedRightArrow
            } else if (name == "homePlate")  //五边形
            {
                return ShapeTypes.HomePlate
            } else if (name == "chevron")  //燕尾形
            {
                return ShapeTypes.Chevron
            } else if (name == "rightArrowCallout")  //右箭头标注
            {
                return ShapeTypes.RightArrowCallout
            } else if (name == "leftArrowCallout")  //
            {
                return ShapeTypes.LeftArrowCallout
            } else if (name == "downArrowCallout")  //
            {
                return ShapeTypes.DownArrowCallout
            } else if (name == "upArrowCallout")  //
            {
                return ShapeTypes.UpArrowCallout
            } else if (name == "leftRightArrowCallout")  //左右箭头标注
            {
                return ShapeTypes.LeftRightArrowCallout
            } else if (name == "quadArrowCallout")  //十字箭头标注
            {
                return ShapeTypes.QuadArrowCallout
            } else if (name == "circularArrow")  //环形箭头
            {
                return ShapeTypes.CircularArrow
            } else if (name == "flowChartProcess") {
                return ShapeTypes.FlowChartProcess
            } else if (name == "flowChartAlternateProcess") {
                return ShapeTypes.FlowChartAlternateProcess
            } else if (name == "flowChartDecision") {
                return ShapeTypes.FlowChartDecision
            } else if (name == "flowChartInputOutput") {
                return ShapeTypes.FlowChartInputOutput
            } else if (name == "flowChartPredefinedProcess") {
                return ShapeTypes.FlowChartPredefinedProcess
            } else if (name == "flowChartInternalStorage") {
                return ShapeTypes.FlowChartInternalStorage
            } else if (name == "flowChartDocument") {
                return ShapeTypes.FlowChartDocument
            } else if (name == "flowChartMultidocument") {
                return ShapeTypes.FlowChartMultidocument
            } else if (name == "flowChartTerminator") {
                return ShapeTypes.FlowChartTerminator
            } else if (name == "flowChartPreparation") {
                return ShapeTypes.FlowChartPreparation
            } else if (name == "flowChartManualInput") {
                return ShapeTypes.FlowChartManualInput
            } else if (name == "flowChartManualOperation") {
                return ShapeTypes.FlowChartManualOperation
            } else if (name == "flowChartConnector") {
                return ShapeTypes.FlowChartConnector
            } else if (name == "flowChartOffpageConnector") {
                return ShapeTypes.FlowChartOffpageConnector
            } else if (name == "flowChartPunchedCard") {
                return ShapeTypes.FlowChartPunchedCard
            } else if (name == "flowChartPunchedTape") {
                return ShapeTypes.FlowChartPunchedTape
            } else if (name == "flowChartSummingJunction") {
                return ShapeTypes.FlowChartSummingJunction
            } else if (name == "flowChartOr") {
                return ShapeTypes.FlowChartOr
            } else if (name == "flowChartCollate") {
                return ShapeTypes.FlowChartCollate
            } else if (name == "flowChartSort") {
                return ShapeTypes.FlowChartSort
            } else if (name == "flowChartExtract") {
                return ShapeTypes.FlowChartExtract
            } else if (name == "flowChartMerge") {
                return ShapeTypes.FlowChartMerge
            } else if (name == "flowChartOnlineStorage") {
                return ShapeTypes.FlowChartOnlineStorage
            } else if (name == "flowChartDelay") {
                return ShapeTypes.FlowChartDelay
            } else if (name == "flowChartMagneticTape") {
                return ShapeTypes.FlowChartMagneticTape
            } else if (name == "flowChartMagneticDisk") {
                return ShapeTypes.FlowChartMagneticDisk
            } else if (name == "flowChartMagneticDrum") {
                return ShapeTypes.FlowChartMagneticDrum
            } else if (name == "flowChartDisplay") {
                return ShapeTypes.FlowChartDisplay
            } else if (name == "wedgeRectCallout") {
                return ShapeTypes.WedgeRectCallout
            } else if (name == "wedgeRoundRectCallout") {
                return ShapeTypes.WedgeRoundRectCallout
            } else if (name == "wedgeEllipseCallout") {
                return ShapeTypes.WedgeEllipseCallout
            } else if (name == "cloudCallout") {
                return ShapeTypes.CloudCallout
            } else if (name == "borderCallout1") {
                return ShapeTypes.BorderCallout1
            } else if (name == "borderCallout2") {
                return ShapeTypes.BorderCallout2
            } else if (name == "borderCallout3") {
                return ShapeTypes.BorderCallout3
            } else if (name == "accentCallout1") {
                return ShapeTypes.AccentCallout1
            } else if (name == "accentCallout2") {
                return ShapeTypes.AccentCallout2
            } else if (name == "accentCallout3") {
                return ShapeTypes.AccentCallout3
            } else if (name == "callout1") {
                return ShapeTypes.Callout1
            } else if (name == "callout2") {
                return ShapeTypes.Callout2
            } else if (name == "callout3") {
                return ShapeTypes.Callout3
            } else if (name == "accentBorderCallout1") {
                return ShapeTypes.AccentBorderCallout1
            } else if (name == "accentBorderCallout2") {
                return ShapeTypes.AccentBorderCallout2
            } else if (name == "accentBorderCallout3") {
                return ShapeTypes.AccentBorderCallout3
            } else if (name == "actionButtonBackPrevious")  //actionButton
            {
                return ShapeTypes.ActionButtonBackPrevious
            } else if (name == "actionButtonForwardNext") {
                return ShapeTypes.ActionButtonForwardNext
            } else if (name == "actionButtonBeginning") {
                return ShapeTypes.ActionButtonBeginning
            } else if (name == "actionButtonEnd") {
                return ShapeTypes.ActionButtonEnd
            } else if (name == "actionButtonHome") {
                return ShapeTypes.ActionButtonHome
            } else if (name == "actionButtonInformation") {
                return ShapeTypes.ActionButtonInformation
            } else if (name == "actionButtonReturn") {
                return ShapeTypes.ActionButtonReturn
            } else if (name == "actionButtonMovie") {
                return ShapeTypes.ActionButtonMovie
            } else if (name == "actionButtonDocument") {
                return ShapeTypes.ActionButtonDocument
            } else if (name == "actionButtonSound") {
                return ShapeTypes.ActionButtonSound
            } else if (name == "actionButtonHelp") {
                return ShapeTypes.ActionButtonHelp
            } else if (name == "actionButtonBlank") {
                return ShapeTypes.ActionButtonBlank
            } else if (name == "irregularSeal1")  //star and flag
            {
                return ShapeTypes.IrregularSeal1
            } else if (name == "irregularSeal2") {
                return ShapeTypes.IrregularSeal2
            } else if (name == "star4") {
                return ShapeTypes.Star4
            } else if (name == "star5") {
                return ShapeTypes.Star5
            } else if (name == "star6") {
                return ShapeTypes.Star6
            } else if (name == "star7") {
                return ShapeTypes.Star7
            } else if (name == "star8") {
                return ShapeTypes.Star8
            } else if (name == "star10") {
                return ShapeTypes.Star10
            } else if (name == "star12") {
                return ShapeTypes.Star12
            } else if (name == "star16") {
                return ShapeTypes.Star16
            } else if (name == "star24") {
                return ShapeTypes.Star24
            } else if (name == "star32") {
                return ShapeTypes.Star32
            } else if (name == "ribbon2") {
                return ShapeTypes.Ribbon2
            } else if (name == "ribbon") {
                return ShapeTypes.Ribbon
            } else if (name == "ellipseRibbon2") {
                return ShapeTypes.EllipseRibbon2
            } else if (name == "ellipseRibbon") {
                return ShapeTypes.EllipseRibbon
            } else if (name == "verticalScroll") {
                return ShapeTypes.VerticalScroll
            } else if (name == "horizontalScroll") {
                return ShapeTypes.HorizontalScroll
            } else if (name == "wave") {
                return ShapeTypes.Wave
            } else if (name == "doubleWave") {
                return ShapeTypes.DoubleWave
            } else if (name == "funnel") {
                return ShapeTypes.Funnel
            } else if (name == "gear6") {
                return ShapeTypes.Gear6
            } else if (name == "gear9") {
                return ShapeTypes.Gear9
            } else if (name == "leftCircularArrow") {
                return ShapeTypes.LeftCircularArrow
            } else if (name == "leftRightRibbon") {
                return ShapeTypes.LeftRightRibbon
            } else if (name == "pieWedge") {
                return ShapeTypes.PieWedge
            } else if (name == "swooshArrow") {
                return ShapeTypes.SwooshArrow
            }
        }
        return ShapeTypes.NotPrimitive
    }

    companion object {
        //
        private val kit = AutoShapeTypes()

        /**
         * 
         * @return
         */
        fun instance(): AutoShapeTypes {
            return kit
        }
    }
}

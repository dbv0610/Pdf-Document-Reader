// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf

import com.wxiwei.office.thirdpart.emf.data.AbortPath
import com.wxiwei.office.thirdpart.emf.data.AlphaBlend
import com.wxiwei.office.thirdpart.emf.data.AngleArc
import com.wxiwei.office.thirdpart.emf.data.Arc
import com.wxiwei.office.thirdpart.emf.data.ArcTo
import com.wxiwei.office.thirdpart.emf.data.BeginPath
import com.wxiwei.office.thirdpart.emf.data.BitBlt
import com.wxiwei.office.thirdpart.emf.data.Chord
import com.wxiwei.office.thirdpart.emf.data.CloseFigure
import com.wxiwei.office.thirdpart.emf.data.CreateBrushIndirect
import com.wxiwei.office.thirdpart.emf.data.CreateDIBPatternBrushPt
import com.wxiwei.office.thirdpart.emf.data.CreatePen
import com.wxiwei.office.thirdpart.emf.data.DeleteObject
import com.wxiwei.office.thirdpart.emf.data.EMFPolygon
import com.wxiwei.office.thirdpart.emf.data.EMFRectangle
import com.wxiwei.office.thirdpart.emf.data.EOF
import com.wxiwei.office.thirdpart.emf.data.Ellipse
import com.wxiwei.office.thirdpart.emf.data.EndPath
import com.wxiwei.office.thirdpart.emf.data.ExcludeClipRect
import com.wxiwei.office.thirdpart.emf.data.ExtCreateFontIndirectW
import com.wxiwei.office.thirdpart.emf.data.ExtCreatePen
import com.wxiwei.office.thirdpart.emf.data.ExtFloodFill
import com.wxiwei.office.thirdpart.emf.data.ExtSelectClipRgn
import com.wxiwei.office.thirdpart.emf.data.ExtTextOutA
import com.wxiwei.office.thirdpart.emf.data.ExtTextOutW
import com.wxiwei.office.thirdpart.emf.data.FillPath
import com.wxiwei.office.thirdpart.emf.data.FlattenPath
import com.wxiwei.office.thirdpart.emf.data.GDIComment
import com.wxiwei.office.thirdpart.emf.data.GradientFill
import com.wxiwei.office.thirdpart.emf.data.IntersectClipRect
import com.wxiwei.office.thirdpart.emf.data.LineTo
import com.wxiwei.office.thirdpart.emf.data.ModifyWorldTransform
import com.wxiwei.office.thirdpart.emf.data.MoveToEx
import com.wxiwei.office.thirdpart.emf.data.OffsetClipRgn
import com.wxiwei.office.thirdpart.emf.data.Pie
import com.wxiwei.office.thirdpart.emf.data.PolyBezier
import com.wxiwei.office.thirdpart.emf.data.PolyBezier16
import com.wxiwei.office.thirdpart.emf.data.PolyBezierTo
import com.wxiwei.office.thirdpart.emf.data.PolyBezierTo16
import com.wxiwei.office.thirdpart.emf.data.PolyDraw
import com.wxiwei.office.thirdpart.emf.data.PolyDraw16
import com.wxiwei.office.thirdpart.emf.data.PolyPolygon
import com.wxiwei.office.thirdpart.emf.data.PolyPolygon16
import com.wxiwei.office.thirdpart.emf.data.PolyPolyline
import com.wxiwei.office.thirdpart.emf.data.PolyPolyline16
import com.wxiwei.office.thirdpart.emf.data.Polygon16
import com.wxiwei.office.thirdpart.emf.data.Polyline
import com.wxiwei.office.thirdpart.emf.data.Polyline16
import com.wxiwei.office.thirdpart.emf.data.PolylineTo
import com.wxiwei.office.thirdpart.emf.data.PolylineTo16
import com.wxiwei.office.thirdpart.emf.data.RealizePalette
import com.wxiwei.office.thirdpart.emf.data.ResizePalette
import com.wxiwei.office.thirdpart.emf.data.RestoreDC
import com.wxiwei.office.thirdpart.emf.data.RoundRect
import com.wxiwei.office.thirdpart.emf.data.SaveDC
import com.wxiwei.office.thirdpart.emf.data.ScaleViewportExtEx
import com.wxiwei.office.thirdpart.emf.data.ScaleWindowExtEx
import com.wxiwei.office.thirdpart.emf.data.SelectClipPath
import com.wxiwei.office.thirdpart.emf.data.SelectObject
import com.wxiwei.office.thirdpart.emf.data.SelectPalette
import com.wxiwei.office.thirdpart.emf.data.SetArcDirection
import com.wxiwei.office.thirdpart.emf.data.SetBkColor
import com.wxiwei.office.thirdpart.emf.data.SetBkMode
import com.wxiwei.office.thirdpart.emf.data.SetBrushOrgEx
import com.wxiwei.office.thirdpart.emf.data.SetICMMode
import com.wxiwei.office.thirdpart.emf.data.SetMapMode
import com.wxiwei.office.thirdpart.emf.data.SetMapperFlags
import com.wxiwei.office.thirdpart.emf.data.SetMetaRgn
import com.wxiwei.office.thirdpart.emf.data.SetMiterLimit
import com.wxiwei.office.thirdpart.emf.data.SetPixelV
import com.wxiwei.office.thirdpart.emf.data.SetPolyFillMode
import com.wxiwei.office.thirdpart.emf.data.SetROP2
import com.wxiwei.office.thirdpart.emf.data.SetStretchBltMode
import com.wxiwei.office.thirdpart.emf.data.SetTextAlign
import com.wxiwei.office.thirdpart.emf.data.SetTextColor
import com.wxiwei.office.thirdpart.emf.data.SetViewportExtEx
import com.wxiwei.office.thirdpart.emf.data.SetViewportOrgEx
import com.wxiwei.office.thirdpart.emf.data.SetWindowExtEx
import com.wxiwei.office.thirdpart.emf.data.SetWindowOrgEx
import com.wxiwei.office.thirdpart.emf.data.SetWorldTransform
import com.wxiwei.office.thirdpart.emf.data.StretchDIBits
import com.wxiwei.office.thirdpart.emf.data.StrokeAndFillPath
import com.wxiwei.office.thirdpart.emf.data.StrokePath
import com.wxiwei.office.thirdpart.emf.data.WidenPath
import com.wxiwei.office.thirdpart.emf.io.TagSet

/**
 * EMF specific tagset.
 * 
 * @author Mark Donszelmann
 * @version $Id: EMFTagSet.java 10515 2007-02-06 18:42:34Z duns $
 */
class EMFTagSet(version: Int) : TagSet() {
    init {
        if (version >= 1) {
            // Set for Windows 3
            addTag(PolyBezier()) // 2 02
            addTag(EMFPolygon()) // 3 03
            addTag(Polyline()) // 4 04
            addTag(PolyBezierTo()) // 5 05
            addTag(PolylineTo()) // 6 06
            addTag(PolyPolyline()) // 7 07
            addTag(PolyPolygon()) // 8 08
            addTag(SetWindowExtEx()) // 9 09
            addTag(SetWindowOrgEx()) // 10 0a
            addTag(SetViewportExtEx()) // 11 0b
            addTag(SetViewportOrgEx()) // 12 0c
            addTag(SetBrushOrgEx()) // 13 0d
            addTag(EOF()) // 14 0e
            addTag(SetPixelV()) // 15 0f
            addTag(SetMapperFlags()) // 16 10
            addTag(SetMapMode()) // 17 11
            addTag(SetBkMode()) // 18 12
            addTag(SetPolyFillMode()) // 19 13
            addTag(SetROP2()) // 20 14
            addTag(SetStretchBltMode()) // 21 15
            addTag(SetTextAlign()) // 22 16
            // addTag(new SetColorAdjustment()); // 23 17
            addTag(SetTextColor()) // 24 18
            addTag(SetBkColor()) // 25 19
            addTag(OffsetClipRgn()) // 26 1a
            addTag(MoveToEx()) // 27 1b
            addTag(SetMetaRgn()) // 28 1c
            addTag(ExcludeClipRect()) // 29 1d
            addTag(IntersectClipRect()) // 30 1e
            addTag(ScaleViewportExtEx()) // 31 1f
            addTag(ScaleWindowExtEx()) // 32 20
            addTag(SaveDC()) // 33 21
            addTag(RestoreDC()) // 34 22
            addTag(SetWorldTransform()) // 35 23
            addTag(ModifyWorldTransform()) // 36 24
            addTag(SelectObject()) // 37 25
            addTag(CreatePen()) // 38 26
            addTag(CreateBrushIndirect()) // 39 27
            addTag(DeleteObject()) // 40 28
            addTag(AngleArc()) // 41 29
            addTag(Ellipse()) // 42 2a
            addTag(EMFRectangle()) // 43 2b
            addTag(RoundRect()) // 44 2c
            addTag(Arc()) // 45 2d
            addTag(Chord()) // 46 2e
            addTag(Pie()) // 47 2f
            addTag(SelectPalette()) // 48 30
            // addTag(new CreatePalette()); // 49 31
            // addTag(new SetPaletteEntries()); // 50 32
            addTag(ResizePalette()) // 51 33
            addTag(RealizePalette()) // 52 34
            addTag(ExtFloodFill()) // 53 35
            addTag(LineTo()) // 54 36
            addTag(ArcTo()) // 55 37
            addTag(PolyDraw()) // 56 38
            addTag(SetArcDirection()) // 57 39
            addTag(SetMiterLimit()) // 58 3a
            addTag(BeginPath()) // 59 3b
            addTag(EndPath()) // 60 3c
            addTag(CloseFigure()) // 61 3d
            addTag(FillPath()) // 62 3e
            addTag(StrokeAndFillPath()) // 63 3f
            addTag(StrokePath()) // 64 40
            addTag(FlattenPath()) // 65 41
            addTag(WidenPath()) // 66 42
            addTag(SelectClipPath()) // 67 43
            addTag(AbortPath()) // 68 44
            // this tag does not exist // 69 45
            addTag(GDIComment()) // 70 46
            // addTag(new FillRgn()); // 71 47
            // addTag(new FrameRgn()); // 72 48
            // addTag(new InvertRgn()); // 73 49
            // addTag(new PaintRgn()); // 74 4a
            addTag(ExtSelectClipRgn()) // 75 4b
            addTag(BitBlt()) // 76 4c
            // addTag(new StretchBlt()); // 77 4d
            // addTag(new MaskBlt()); // 78 4e
            // addTag(new PlgBlt()); // 79 4f
            // addTag(new SetDIBitsToDevice()); // 80 50
            addTag(StretchDIBits()) // 81 51
            addTag(ExtCreateFontIndirectW()) // 82 52
            addTag(ExtTextOutA()) // 83 53
            addTag(ExtTextOutW()) // 84 54
            addTag(PolyBezier16()) // 85 55
            addTag(Polygon16()) // 86 56
            addTag(Polyline16()) // 87 57
            addTag(PolyBezierTo16()) // 88 58
            addTag(PolylineTo16()) // 89 59
            addTag(PolyPolyline16()) // 90 5a
            addTag(PolyPolygon16()) // 91 5b
            addTag(PolyDraw16()) // 92 5c
            // addTag(new CreateMonoBrush()); // 93 5d
            addTag(CreateDIBPatternBrushPt()) // 94 5e
            addTag(ExtCreatePen()) // 95 5f

            // addTag(new PolyTextOutA()); // 96 60
            // addTag(new PolyTextOutW()); // 97 61

            // Set for Windows 4 (NT)
            addTag(SetICMMode()) // 98 62

            // addTag(new CreateColorSpace()); // 99 63
            // addTag(new SetColorSpace()); // 100 64
            // addTag(new DeleteColorSpace()); // 101 65
            // addTag(new GLSRecord()); // 102 66
            // addTag(new GLSBoundedRecord()); // 103 67
            // addTag(new PixelFormat()); // 104 68

            // Set for Windows 5 (2000/XP)
            // addTag(new DrawEscape()); // 105 69
            // addTag(new ExtEscape()); // 106 6a
            // addTag(new StartDoc()); // 107 6b
            // addTag(new SmallTextOut()); // 108 6c
            // addTag(new ForceUFIMapping()); // 109 6d
            // addTag(new NamedEscape()); // 110 6e
            // addTag(new ColorCorrectPalette()); // 111 6f
            // addTag(new SetICMProfileA()); // 112 70
            // addTag(new SetICMProfileW()); // 113 71
            addTag(AlphaBlend()) // 114 72
            // addTag(new AlphaDIBBlend()); // 115 73
            // addTag(new TransparentBlt()); // 116 74
            // addTag(new TransparentDIB()); // 117 75
            addTag(GradientFill()) // 118 76
            // addTag(new SetLinkedUFIs()); // 119 77
            // addTag(new SetTextJustification()); // 120 78
        }
    }
}

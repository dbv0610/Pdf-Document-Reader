/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.common.shape

interface ShapeTypes {
    companion object {
        const val NotPrimitive: Int = 0
        const val Rectangle: Int = 1
        const val RoundRectangle: Int = 2
        const val Ellipse: Int = 3
        const val Diamond: Int = 4
        const val Triangle: Int = 5
        const val RtTriangle: Int = 6
        const val Parallelogram: Int = 7
        const val Trapezoid: Int = 8
        const val Hexagon: Int = 9
        const val Octagon: Int = 10
        const val Plus: Int = 11
        const val Star: Int = 12
        const val RightArrow: Int = 13
        const val ThickArrow: Int = 14
        const val HomePlate: Int = 15 // 五边形
        const val Cube: Int = 16
        const val Balloon: Int = 17
        const val Seal: Int = 18
        const val Arc: Int = 19
        const val Line: Int = 20
        const val Plaque: Int = 21
        const val Can: Int = 22
        const val Donut: Int = 23
        const val TextSimple: Int = 24
        const val TextOctagon: Int = 25
        const val TextHexagon: Int = 26
        const val TextCurve: Int = 27
        const val TextWave: Int = 28
        const val TextRing: Int = 29
        const val TextOnCurve: Int = 30
        const val TextOnRing: Int = 31
        const val StraightConnector1: Int = 32
        const val BentConnector2: Int = 33
        const val BentConnector3: Int = 34
        const val BentConnector4: Int = 35
        const val BentConnector5: Int = 36
        const val CurvedConnector2: Int = 37
        const val CurvedConnector3: Int = 38
        const val CurvedConnector4: Int = 39
        const val CurvedConnector5: Int = 40
        const val Callout2: Int = 41
        const val Callout3: Int = 42
        const val Callout4: Int = 43
        const val AccentCallout2: Int = 44
        const val AccentCallout3: Int = 45
        const val AccentCallout4: Int = 46
        const val BorderCallout2: Int = 47
        const val BorderCallout3: Int = 48
        const val BorderCallout4: Int = 49
        const val AccentBorderCallout2: Int = 50
        const val AccentBorderCallout3: Int = 51
        const val AccentBorderCallout4: Int = 52
        const val Ribbon: Int = 53 // 前凸带形
        const val Ribbon2: Int = 54 // 上凸带形
        const val Chevron: Int = 55 // 燕尾形
        const val Pentagon: Int = 56
        const val NoSmoking: Int = 57
        const val Star8: Int = 58
        const val Star16: Int = 59
        const val Star32: Int = 60
        const val WedgeRectCallout: Int = 61
        const val WedgeRoundRectCallout: Int = 62
        const val WedgeEllipseCallout: Int = 63
        const val Wave: Int = 64
        const val FoldedCorner: Int = 65
        const val LeftArrow: Int = 66
        const val DownArrow: Int = 67
        const val UpArrow: Int = 68
        const val LeftRightArrow: Int = 69
        const val UpDownArrow: Int = 70
        const val IrregularSeal1: Int = 71 // 爆炸形1
        const val IrregularSeal2: Int = 72 // 爆炸形2
        const val LightningBolt: Int = 73
        const val Heart: Int = 74
        const val PictureFrame: Int = 75
        const val QuadArrow: Int = 76
        const val LeftArrowCallout: Int = 77
        const val RightArrowCallout: Int = 78
        const val UpArrowCallout: Int = 79
        const val DownArrowCallout: Int = 80
        const val LeftRightArrowCallout: Int = 81
        const val UpDownArrowCallout: Int = 82
        const val QuadArrowCallout: Int = 83
        const val Bevel: Int = 84
        const val LeftBracket: Int = 85
        const val RightBracket: Int = 86
        const val LeftBrace: Int = 87
        const val RightBrace: Int = 88
        const val LeftUpArrow: Int = 89 // 直角双向箭头
        const val BentUpArrow: Int = 90 // 直角上箭头
        const val BentArrow: Int = 91 // 圆角右箭头
        const val Star24: Int = 92
        const val StripedRightArrow: Int = 93 // 虚尾箭头
        const val NotchedRightArrow: Int = 94 // 燕尾形箭头
        const val BlockArc: Int = 95
        const val SmileyFace: Int = 96
        const val VerticalScroll: Int = 97
        const val HorizontalScroll: Int = 98
        const val CircularArrow: Int = 99 // 环形箭头
        const val NotchedCircularArrow: Int = 100
        const val UturnArrow: Int = 101 // 手杖形箭头
        const val CurvedRightArrow: Int = 102 // 左弧形箭头
        const val CurvedLeftArrow: Int = 103 // 右弧形箭头
        const val CurvedUpArrow: Int = 104 // 下弧形箭头
        const val CurvedDownArrow: Int = 105 // 上弧形箭头
        const val CloudCallout: Int = 106
        const val EllipseRibbon: Int = 107 // 前凸弯带形
        const val EllipseRibbon2: Int = 108 // 上凸弯带形
        const val FlowChartProcess: Int = 109
        const val FlowChartDecision: Int = 110
        const val FlowChartInputOutput: Int = 111
        const val FlowChartPredefinedProcess: Int = 112
        const val FlowChartInternalStorage: Int = 113
        const val FlowChartDocument: Int = 114
        const val FlowChartMultidocument: Int = 115
        const val FlowChartTerminator: Int = 116
        const val FlowChartPreparation: Int = 117
        const val FlowChartManualInput: Int = 118
        const val FlowChartManualOperation: Int = 119
        const val FlowChartConnector: Int = 120
        const val FlowChartPunchedCard: Int = 121
        const val FlowChartPunchedTape: Int = 122
        const val FlowChartSummingJunction: Int = 123
        const val FlowChartOr: Int = 124
        const val FlowChartCollate: Int = 125
        const val FlowChartSort: Int = 126
        const val FlowChartExtract: Int = 127
        const val FlowChartMerge: Int = 128
        const val FlowChartOfflineStorage: Int = 129
        const val FlowChartOnlineStorage: Int = 130
        const val FlowChartMagneticTape: Int = 131
        const val FlowChartMagneticDisk: Int = 132
        const val FlowChartMagneticDrum: Int = 133
        const val FlowChartDisplay: Int = 134
        const val FlowChartDelay: Int = 135
        const val TextPlainText: Int = 136
        const val TextStop: Int = 137
        const val TextTriangle: Int = 138
        const val TextTriangleInverted: Int = 139
        const val TextChevron: Int = 140
        const val TextChevronInverted: Int = 141
        const val TextRingInside: Int = 142
        const val TextRingOutside: Int = 143
        const val TextArchUpCurve: Int = 144
        const val TextArchDownCurve: Int = 145
        const val TextCircleCurve: Int = 146
        const val TextButtonCurve: Int = 147
        const val TextArchUpPour: Int = 148
        const val TextArchDownPour: Int = 149
        const val TextCirclePour: Int = 150
        const val TextButtonPour: Int = 151
        const val TextCurveUp: Int = 152
        const val TextCurveDown: Int = 153
        const val TextCascadeUp: Int = 154
        const val TextCascadeDown: Int = 155
        const val TextWave1: Int = 156
        const val TextWave2: Int = 157
        const val TextWave3: Int = 158
        const val TextWave4: Int = 159
        const val TextInflate: Int = 160
        const val TextDeflate: Int = 161
        const val TextInflateBottom: Int = 162
        const val TextDeflateBottom: Int = 163
        const val TextInflateTop: Int = 164
        const val TextDeflateTop: Int = 165
        const val TextDeflateInflate: Int = 166
        const val TextDeflateInflateDeflate: Int = 167
        const val TextFadeRight: Int = 168
        const val TextFadeLeft: Int = 169
        const val TextFadeUp: Int = 170
        const val TextFadeDown: Int = 171
        const val TextSlantUp: Int = 172
        const val TextSlantDown: Int = 173
        const val TextCanUp: Int = 174
        const val TextCanDown: Int = 175
        const val FlowChartAlternateProcess: Int = 176
        const val FlowChartOffpageConnector: Int = 177
        const val Callout1: Int = 178
        const val AccentCallout1: Int = 179
        const val BorderCallout1: Int = 180
        const val AccentBorderCallout1: Int = 181
        const val LeftRightUpArrow: Int = 182 // 丁字箭头
        const val Sun: Int = 183
        const val Moon: Int = 184
        const val BracketPair: Int = 185
        const val BracePair: Int = 186
        const val Star4: Int = 187 // 十字星
        const val DoubleWave: Int = 188 // 双波形   
        const val ActionButtonBlank: Int = 189
        const val ActionButtonHome: Int = 190
        const val ActionButtonHelp: Int = 191
        const val ActionButtonInformation: Int = 192
        const val ActionButtonForwardNext: Int = 193
        const val ActionButtonBackPrevious: Int = 194
        const val ActionButtonEnd: Int = 195
        const val ActionButtonBeginning: Int = 196
        const val ActionButtonReturn: Int = 197
        const val ActionButtonDocument: Int = 198
        const val ActionButtonSound: Int = 199
        const val ActionButtonMovie: Int = 200
        const val HostControl: Int = 201
        const val TextBox: Int = 202
        const val IsocelesTriangle: Int = 203
        const val RightTriangle: Int = 204
        const val Arrow: Int = 205
        const val Callout90: Int = 206
        const val AccentCallout90: Int = 207
        const val BorderCallout90: Int = 208
        const val AccentBorderCallout90: Int = 209
        // add new
        const val Round1Rect: Int = 210
        const val Round2SameRect: Int = 211
        const val Round2DiagRect: Int = 212
        const val Snip1Rect: Int = 213
        const val Snip2SameRect: Int = 214
        const val Snip2DiagRect: Int = 215
        const val SnipRoundRect: Int = 216
        const val Heptagon: Int = 217
        const val Decagon: Int = 218
        const val Dodecagon: Int = 219
        const val Pie: Int = 220
        const val Chord: Int = 221
        const val Teardrop: Int = 222
        const val Frame: Int = 223
        const val HalfFrame: Int = 224
        const val Corner: Int = 225
        const val DiagStripe: Int = 226
        const val MathPlus: Int = 227
        const val MathMinus: Int = 228
        const val MathMultiply: Int = 229
        const val MathDivide: Int = 230
        const val MathEqual: Int = 231
        const val MathNotEqual: Int = 232
        //Arbitrary polygon
        const val ArbitraryPolygon: Int = 233
        const val Cloud: Int = 234
        const val Star5: Int = 235
        const val Star6: Int = 236
        const val Star7: Int = 237
        const val Star10: Int = 238
        const val Star12: Int = 239
        /**
         * for smart art shape
         */
        const val Funnel: Int = 240
        const val Gear6: Int = 241
        const val Gear9: Int = 242
        const val LeftCircularArrow: Int = 243
        const val LeftRightRibbon: Int = 244
        const val PieWedge: Int = 245
        const val SwooshArrow: Int = 246
        const val WP_Line: Int = 247
        const val Curve: Int = 248
        //direct line polygon
        const val DirectPolygon: Int = 249
    }
}

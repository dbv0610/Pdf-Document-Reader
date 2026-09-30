/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.java.awt.Shape
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.Arc2D
import com.wxiwei.office.java.awt.geom.Ellipse2D
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.java.awt.geom.Line2D
import com.wxiwei.office.java.awt.geom.Rectangle2D
import com.wxiwei.office.java.awt.geom.RoundRectangle2D

/**
 * Stores definition of auto-shapes.
 * See the Office Drawing 97-2007 Binary Format Specification for details.
 * 
 * TODO: follow the spec and define all the auto-shapes
 * 
 * @author Yegor Kozlov
 */
object AutoShapes {
    internal var shapes: Array<ShapeOutline?>

    /**
     * Return shape outline by shape type
     * @param type shape type see [ShapeTypes]
     * 
     * @return the shape outline
     */
    fun getShapeOutline(type: Int): ShapeOutline? {
        val outline = shapes[type]
        return outline
    }

    /**
     * Auto-shapes are defined in the [0,21600] coordinate system.
     * We need to transform it into normal slide coordinates
     * 
     */
    fun transform(outline: Shape?, anchor: Rectangle2D): Shape? {
        val at = AffineTransform()
        at.translate(anchor.getX(), anchor.getY())
        at.scale(1.0f / 21600 * anchor.getWidth(), 1.0f / 21600 * anchor.getHeight())
        return at.createTransformedShape(outline)
    }

    init {
        shapes = arrayOfNulls<ShapeOutline>(255)

        shapes[ShapeTypes.Rectangle] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val path: Rectangle2D = Rectangle2D.Float(0f, 0f, 21600f, 21600f)
                return path
            }
        }

        shapes[ShapeTypes.RoundRectangle] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )
                val path: RoundRectangle2D = RoundRectangle2D.Float(
                    0f, 0f, 21600f, 21600f, adjval.toFloat(),
                    adjval.toFloat()
                )
                return path
            }
        }

        shapes[ShapeTypes.Ellipse] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val path: Ellipse2D = Ellipse2D.Float(0f, 0f, 21600f, 21600f)
                return path
            }
        }

        shapes[ShapeTypes.Diamond] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val path = GeneralPath()
                path.moveTo(10800f, 0f)
                path.lineTo(21600f, 10800f)
                path.lineTo(10800f, 21600f)
                path.lineTo(0f, 10800f)
                path.closePath()
                return path
            }
        }

        //m@0,l,21600r21600
        shapes[ShapeTypes.IsocelesTriangle] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    10800
                )
                val path = GeneralPath()
                path.moveTo(adjval.toFloat(), 0f)
                path.lineTo(0f, 21600f)
                path.lineTo(21600f, 21600f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.RightTriangle] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val path = GeneralPath()
                path.moveTo(0f, 0f)
                path.lineTo(21600f, 21600f)
                path.lineTo(0f, 21600f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Parallelogram] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )

                val path = GeneralPath()
                path.moveTo(adjval.toFloat(), 0f)
                path.lineTo(21600f, 0f)
                path.lineTo((21600 - adjval).toFloat(), 21600f)
                path.lineTo(0f, 21600f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Trapezoid] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )

                val path = GeneralPath()
                path.moveTo(0f, 0f)
                path.lineTo(adjval.toFloat(), 21600f)
                path.lineTo((21600 - adjval).toFloat(), 21600f)
                path.lineTo(21600f, 0f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Hexagon] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )

                val path = GeneralPath()
                path.moveTo(adjval.toFloat(), 0f)
                path.lineTo((21600 - adjval).toFloat(), 0f)
                path.lineTo(21600f, 10800f)
                path.lineTo((21600 - adjval).toFloat(), 21600f)
                path.lineTo(adjval.toFloat(), 21600f)
                path.lineTo(0f, 10800f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Octagon] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    6326
                )

                val path = GeneralPath()
                path.moveTo(adjval.toFloat(), 0f)
                path.lineTo((21600 - adjval).toFloat(), 0f)
                path.lineTo(21600f, adjval.toFloat())
                path.lineTo(21600f, (21600 - adjval).toFloat())
                path.lineTo((21600 - adjval).toFloat(), 21600f)
                path.lineTo(adjval.toFloat(), 21600f)
                path.lineTo(0f, (21600 - adjval).toFloat())
                path.lineTo(0f, adjval.toFloat())
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Plus] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )

                val path = GeneralPath()
                path.moveTo(adjval.toFloat(), 0f)
                path.lineTo((21600 - adjval).toFloat(), 0f)
                path.lineTo((21600 - adjval).toFloat(), adjval.toFloat())
                path.lineTo(21600f, adjval.toFloat())
                path.lineTo(21600f, (21600 - adjval).toFloat())
                path.lineTo((21600 - adjval).toFloat(), (21600 - adjval).toFloat())
                path.lineTo((21600 - adjval).toFloat(), 21600f)
                path.lineTo(adjval.toFloat(), 21600f)
                path.lineTo(adjval.toFloat(), (21600 - adjval).toFloat())
                path.lineTo(0f, (21600 - adjval).toFloat())
                path.lineTo(0f, adjval.toFloat())
                path.lineTo(adjval.toFloat(), adjval.toFloat())
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Pentagon] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                val path = GeneralPath()
                path.moveTo(10800f, 0f)
                path.lineTo(21600f, 8259f)
                path.lineTo((21600 - 4200).toFloat(), 21600f)
                path.lineTo(4200f, 21600f)
                path.lineTo(0f, 8259f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.DownArrow] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                //m0@0 l@1@0 @1,0 @2,0 @2@0,21600@0,10800,21600xe
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    16200
                )
                val adjval2 = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUST2VALUE,
                    5400
                )
                val path = GeneralPath()
                path.moveTo(0f, adjval.toFloat())
                path.lineTo(adjval2.toFloat(), adjval.toFloat())
                path.lineTo(adjval2.toFloat(), 0f)
                path.lineTo((21600 - adjval2).toFloat(), 0f)
                path.lineTo((21600 - adjval2).toFloat(), adjval.toFloat())
                path.lineTo(21600f, adjval.toFloat())
                path.lineTo(10800f, 21600f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.UpArrow] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                //m0@0 l@1@0 @1,21600@2,21600@2@0,21600@0,10800,xe
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )
                val adjval2 = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUST2VALUE,
                    5400
                )
                val path = GeneralPath()
                path.moveTo(0f, adjval.toFloat())
                path.lineTo(adjval2.toFloat(), adjval.toFloat())
                path.lineTo(adjval2.toFloat(), 21600f)
                path.lineTo((21600 - adjval2).toFloat(), 21600f)
                path.lineTo((21600 - adjval2).toFloat(), adjval.toFloat())
                path.lineTo(21600f, adjval.toFloat())
                path.lineTo(10800f, 0f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Arrow] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                //m@0, l@0@1 ,0@1,0@2@0@2@0,21600,21600,10800xe
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    16200
                )
                val adjval2 = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUST2VALUE,
                    5400
                )
                val path = GeneralPath()
                path.moveTo(adjval.toFloat(), 0f)
                path.lineTo(adjval.toFloat(), adjval2.toFloat())
                path.lineTo(0f, adjval2.toFloat())
                path.lineTo(0f, (21600 - adjval2).toFloat())
                path.lineTo(adjval.toFloat(), (21600 - adjval2).toFloat())
                path.lineTo(adjval.toFloat(), 21600f)
                path.lineTo(21600f, 10800f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.LeftArrow] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                //m@0, l@0@1,21600@1,21600@2@0@2@0,21600,,10800xe
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )
                val adjval2 = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUST2VALUE,
                    5400
                )
                val path = GeneralPath()
                path.moveTo(adjval.toFloat(), 0f)
                path.lineTo(adjval.toFloat(), adjval2.toFloat())
                path.lineTo(21600f, adjval2.toFloat())
                path.lineTo(21600f, (21600 - adjval2).toFloat())
                path.lineTo(adjval.toFloat(), (21600 - adjval2).toFloat())
                path.lineTo(adjval.toFloat(), 21600f)
                path.lineTo(0f, 10800f)
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.Can] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                //m10800,qx0@1l0@2qy10800,21600,21600@2l21600@1qy10800,xem0@1qy10800@0,21600@1nfe
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    5400
                )

                val path = GeneralPath()

                path.append(
                    Arc2D.Float(0f, 0f, 21600f, adjval.toFloat(), 0f, 180f, Arc2D.OPEN),
                    false
                )
                path.moveTo(0f, (adjval / 2).toFloat())

                path.lineTo(0f, (21600 - adjval / 2).toFloat())
                path.closePath()

                path.append(
                    Arc2D.Float(
                        0f,
                        (21600 - adjval).toFloat(),
                        21600f,
                        adjval.toFloat(),
                        180f,
                        180f,
                        Arc2D.OPEN
                    ), false
                )
                path.moveTo(21600f, (21600 - adjval / 2).toFloat())

                path.lineTo(21600f, (adjval / 2).toFloat())
                path.append(
                    Arc2D.Float(0f, 0f, 21600f, adjval.toFloat(), 180f, 180f, Arc2D.OPEN),
                    false
                )
                path.moveTo(0f, (adjval / 2).toFloat())
                path.closePath()
                return path
            }
        }

        shapes[ShapeTypes.LeftBrace] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                //m21600,qx10800@0l10800@2qy0@11,10800@3l10800@1qy21600,21600e
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    1800
                )
                val adjval2 = ShapeKit.getEscherProperty(
                    shape.spContainer, EscherProperties.GEOMETRY__ADJUST2VALUE,
                    10800
                )

                val path = GeneralPath()
                path.moveTo(21600f, 0f)

                path.append(
                    Arc2D.Float(
                        10800f,
                        0f,
                        21600f,
                        (adjval * 2).toFloat(),
                        90f,
                        90f,
                        Arc2D.OPEN
                    ), false
                )
                path.moveTo(10800f, adjval.toFloat())

                path.lineTo(10800f, (adjval2 - adjval).toFloat())

                path.append(
                    Arc2D.Float(
                        -10800f,
                        (adjval2 - 2 * adjval).toFloat(),
                        21600f,
                        (adjval * 2).toFloat(),
                        270f,
                        90f,
                        Arc2D.OPEN
                    ), false
                )
                path.moveTo(0f, adjval2.toFloat())

                path.append(
                    Arc2D.Float(
                        -10800f,
                        adjval2.toFloat(),
                        21600f,
                        (adjval * 2).toFloat(),
                        0f,
                        90f,
                        Arc2D.OPEN
                    ),
                    false
                )
                path.moveTo(10800f, (adjval2 + adjval).toFloat())

                path.lineTo(10800f, (21600 - adjval).toFloat())

                path.append(
                    Arc2D.Float(
                        10800f,
                        (21600 - 2 * adjval).toFloat(),
                        21600f,
                        (adjval * 2).toFloat(),
                        180f,
                        90f,
                        Arc2D.OPEN
                    ), false
                )

                return path
            }
        }

        shapes[ShapeTypes.RightBrace] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                //m,qx10800@0 l10800@2qy21600@11,10800@3l10800@1qy,21600e
                val adjval = ShapeKit.getEscherProperty(
                    shape.spContainer,
                    EscherProperties.GEOMETRY__ADJUSTVALUE,
                    1800
                )
                val adjval2 = ShapeKit.getEscherProperty(
                    shape.spContainer, EscherProperties.GEOMETRY__ADJUST2VALUE,
                    10800
                )

                val path = GeneralPath()
                path.moveTo(0f, 0f)

                path.append(
                    Arc2D.Float(
                        -10800f,
                        0f,
                        21600f,
                        (adjval * 2).toFloat(),
                        0f,
                        90f,
                        Arc2D.OPEN
                    ), false
                )
                path.moveTo(10800f, adjval.toFloat())

                path.lineTo(10800f, (adjval2 - adjval).toFloat())

                path.append(
                    Arc2D.Float(
                        10800f,
                        (adjval2 - 2 * adjval).toFloat(),
                        21600f,
                        (adjval * 2).toFloat(),
                        180f,
                        90f,
                        Arc2D.OPEN
                    ), false
                )
                path.moveTo(21600f, adjval2.toFloat())

                path.append(
                    Arc2D.Float(
                        10800f,
                        adjval2.toFloat(),
                        21600f,
                        (adjval * 2).toFloat(),
                        90f,
                        90f,
                        Arc2D.OPEN
                    ),
                    false
                )
                path.moveTo(10800f, (adjval2 + adjval).toFloat())

                path.lineTo(10800f, (21600 - adjval).toFloat())

                path.append(
                    Arc2D.Float(
                        -10800f,
                        (21600 - 2 * adjval).toFloat(),
                        21600f,
                        (adjval * 2).toFloat(),
                        270f,
                        90f,
                        Arc2D.OPEN
                    ), false
                )

                return path
            }
        }

        shapes[ShapeTypes.StraightConnector1] = object : ShapeOutline {
            override fun getOutline(shape: com.wxiwei.office.fc.hslf.model.Shape): Shape {
                return Line2D.Float(0f, 0f, 21600f, 21600f)
            }
        }
    }
}

/*
 * 文件名称:           SlidePic.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:58:22
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.PackageRelationshipTypes
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.ss.model.drawing.AnchorPoint
import com.wxiwei.office.ss.model.drawing.CellAnchor
import java.util.Hashtable

/**
 * 解析部件中的 picture
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
 * 日期:           2012-3-1
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
class PictureReader {
    /**
     * get picture part
     */
    @Throws(Exception::class)
    fun getOLEPart(
        zipPackage: ZipPackage, packagePart: PackagePart, spid: String?,
        bExcel: Boolean
    ): PackagePart? {
        if (vmlDrawingPart == null) {
            val ships = packagePart.getRelationshipsByType(
                PackageRelationshipTypes.VMLDRAWING_PART
            )
            for (oleShip in ships) {
                vmlDrawingPart = zipPackage.getPart(oleShip.getTargetURI())
                getShapeIds(bExcel)
            }
        }
        if (spIDs != null) {
            val id = spIDs!!.get(spid)
            if (id != null) {
                val imageShip = vmlDrawingPart!!.getRelationship(id)
                if (imageShip != null) {
                    return zipPackage.getPart(imageShip.getTargetURI())
                }
            }
        }
        return null
    }

    /**
     * 获取 spid
     */
    @Throws(Exception::class)
    private fun getShapeIds(bExcel: Boolean) {
        if (vmlDrawingPart != null) {
            val saxreader = SAXReader()
            val `in` = vmlDrawingPart!!.getInputStream()
            val poiVml = saxreader.read(`in`)
            val root = poiVml.getRootElement()
            if (root != null) {
                if (spIDs == null) {
                    spIDs = Hashtable<String?, String?>()
                }
                if (bExcel && spIDAnchors == null) {
                    spIDAnchors = Hashtable<String?, CellAnchor?>()
                }
                val shapes: MutableList<Element> = root.elements("shape") as MutableList<Element>
                for (shape in shapes) {
                    val imagedata = shape.element("imagedata")
                    if (imagedata != null) {
                        var `val` = shape.attributeValue("spid")

                        if (bExcel) {
                            if (`val` == null) {
                                `val` = shape.attributeValue("id")
                            }
                            if (`val` != null && `val`.length > 8) {
                                `val` = `val`.substring(8)
                                spIDs!!.put(`val`, imagedata.attributeValue("relid"))
                            } else {
                                return
                            }

                            val clientData = shape.element("ClientData")
                            if (clientData != null) {
                                val anchor = clientData.element("Anchor")
                                if (anchor != null) {
                                    var text = anchor.getText()
                                    if (text != null && text.length > 0) {
                                        text = text.trim { it <= ' ' }.replace(" ".toRegex(), "")

                                        val values: Array<String?>? =
                                            text.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                                .toTypedArray()
                                        if (values != null && values.size == 8) {
                                            val anchorFrom = AnchorPoint()
                                            anchorFrom.setColumn(values[0]!!.toInt().toShort())
                                            anchorFrom.setDX(values[1]!!.toInt().toShort().toInt())
                                            anchorFrom.setRow(values[2]!!.toInt().toShort().toInt())
                                            anchorFrom.setDY(values[3]!!.toInt().toShort().toInt())

                                            val anchorTo = AnchorPoint()
                                            anchorTo.setColumn(values[4]!!.toInt().toShort())
                                            anchorTo.setDX(values[5]!!.toInt().toShort().toInt())
                                            anchorTo.setRow(values[6]!!.toInt().toShort().toInt())
                                            anchorTo.setDY(values[7]!!.toInt().toShort().toInt())

                                            val cellAnchor = CellAnchor(CellAnchor.TWOCELLANCHOR)
                                            //from
                                            cellAnchor.setStart(anchorFrom)
                                            //to
                                            cellAnchor.setEnd(anchorTo)

                                            spIDAnchors!!.put(`val`, cellAnchor)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (`val` != null && `val`.length > 0) {
                                spIDs!!.put(`val`, imagedata.attributeValue("relid"))
                            } else {
                                spIDs!!.put(
                                    shape.attributeValue("id"),
                                    imagedata.attributeValue("relid")
                                )
                            }
                        }
                    }
                }
            }
            `in`.close()
        }
    }

    /**
     * for excel
     * @param shapeId
     * @return
     */
    fun getExcelShapeAnchor(shapeId: String?): CellAnchor? {
        if (shapeId != null && spIDAnchors != null && spIDAnchors!!.size > 0) {
            return spIDAnchors!!.get(shapeId)
        }
        return null
    }

    /**
     * 
     */
    fun dispose() {
        vmlDrawingPart = null
        if (spIDs != null) {
            spIDs!!.clear()
            spIDs = null
        }

        if (spIDAnchors != null) {
            val iter = spIDAnchors!!.keys.iterator()
            while (iter.hasNext()) {
                spIDAnchors!!.get(iter.next())!!.dispose()
            }
            spIDAnchors!!.clear()
            spIDAnchors = null
        }
    }

    //
    private var vmlDrawingPart: PackagePart? = null

    //
    private var spIDs: MutableMap<String?, String?>? = null

    // excel spid and rect
    private var spIDAnchors: MutableMap<String?, CellAnchor?>? = null

    companion object {
        private val picReader = PictureReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): PictureReader {
            return picReader
        }
    }
}

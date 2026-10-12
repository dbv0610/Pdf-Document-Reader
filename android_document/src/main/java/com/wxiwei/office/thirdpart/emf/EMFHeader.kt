// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf

import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle

/**
 * EMF File Header.
 * 
 * @author Mark Donszelmann
 * @version $Id: EMFHeader.java 10526 2007-02-12 08:14:31Z duns $
 */
class EMFHeader : EMFConstants {
    /**
     * Specifies the dimensions, in device units, of the smallest rectangle that
     * can be drawn around the picture stored in the metafile. This rectangle is
     * supplied by graphics device interface (GDI). Its dimensions include the
     * right and bottom edges.
     * @return bounds of device
     */
    val bounds: Rectangle?

    /**
     * Specifies the dimensions, in .01 millimeter units, of a rectangle that
     * surrounds the picture stored in the metafile. This rectangle must be
     * supplied by the application that creates the metafile. Its dimensions
     * include the right and bottom edges.
     * @return bounds of frame
     */
    val frame: Rectangle?

    /**
     * Specifies a double word signature. This member must specify the value
     * assigned to the ENHMETA_SIGNATURE constant.
     * @return signature
     */
    val signature: String?

    private val versionMajor: Int

    private val versionMinor: Int

    private val bytes: Int

    private val records: Int

    private val handles: Int

    /**
     * @return the description of the enhanced metafile's contents
     */
    val description: String

    private val palEntries: Int

    /**
     * Specifies the resolution of the reference device, in pixels.
     * @return resolution of the reference device, in pixels
     */
    val device: Dimension?

    /**
     * Specifies the resolution of the reference device, in millimeters.
     * @return size in millimeters
     */
    val millimeters: Dimension?

    /**
     * Windows 98/Me, Windows 2000/XP: Size of the reference device in
     * micrometers.
     * @return size in micrometers
     */
    var micrometers: Dimension? = null
        private set

    /**
     * Windows 95/98/Me, Windows NT 4.0 and later: Specifies whether any OpenGL
     * records are present in a metafile. bOpenGL is a simple Boolean flag that
     * you can use to determine whether an enhanced metafile requires OpenGL
     * handling. When a metafile contains OpenGL records, bOpenGL is TRUE;
     * otherwise it is FALSE.
     * 
     * @return false is default
     */
    var isOpenGL: Boolean = false
        private set

    constructor(
        bounds: Rectangle, versionMajor: Int, versionMinor: Int,
        bytes: Int, records: Int, handles: Int, application: String,
        name: String, device: Dimension
    ) {
        this.bounds = bounds

        // this assumes you use MM_ANISOTROPIC or MM_ISOTROPIC as MapMode
        val pixelWidth = screenMM.width.toDouble() / device.width
        val pixelHeight = screenMM.height.toDouble() / device.height
        this.frame = Rectangle(
            (bounds.x * 100 * pixelWidth).toInt(),
            (bounds.y * 100 * pixelHeight).toInt(),
            (bounds.width * 100 * pixelWidth).toInt(),
            (bounds.height * 100 * pixelHeight).toInt()
        )

        this.signature = " EMF"
        this.versionMajor = versionMajor
        this.versionMinor = versionMinor
        this.bytes = bytes
        this.records = records
        this.handles = handles
        this.description =
            application.trim { it <= ' ' } + "\u0000" + name.trim { it <= ' ' } + "\u0000\u0000"

        this.palEntries = 0
        this.device = device
        this.millimeters = screenMM

        this.isOpenGL = false
        this.micrometers = Dimension(
            screenMM.width * 1000,
            screenMM.height * 1000
        )
    }

    internal constructor(emf: EMFInputStream) {
        // FIXME: incomplete
        emf.readUnsignedInt() // 4

        val length = emf.readDWORD() // 8

        bounds = emf.readRECTL() // 24
        frame = emf.readRECTL() // 40
        signature = String(emf.readBYTE(4)) // 44

        val version = emf.readDWORD() // 48
        versionMajor = version shr 16
        versionMinor = version and 0xFFFF
        bytes = emf.readDWORD() // 52
        records = emf.readDWORD() // 56
        handles = emf.readWORD() // 58
        emf.readWORD() // 60

        val dLen = emf.readDWORD() // 64
        val dOffset = emf.readDWORD() // 68
        palEntries = emf.readDWORD() // 72
        device = emf.readSIZEL() // 80
        millimeters = emf.readSIZEL() // 88

        var bytesRead = 88
        if (dOffset > 88) {
            emf.readDWORD() // 92
            emf.readDWORD() // 96
            this.isOpenGL = if (emf.readDWORD() != 0) true else false // 100
            bytesRead += 12
            if (dOffset > 100) {
                micrometers = emf.readSIZEL() // 108
                bytesRead += 8
            }
        }

        // Discard any bytes leading up to the description (usually zero, but safer not to assume.)
        if (bytesRead < dOffset) {
            emf.skipBytes(dOffset - bytesRead)
            bytesRead = dOffset
        }

        description = emf.readWCHAR(dLen)
        bytesRead += dLen * 2

        // Discard bytes after the description up to the end of the header.
        if (bytesRead < length) {
            emf.skipBytes(length - bytesRead)
        }
    }

    /**
     * @return size of emf file in bytes ?
     */
    fun size(): Int {
        return 108 + (2 * description.length)
    }

    override fun toString(): String {
        val s = StringBuffer("EMF Header\n")
        s.append("  bounds: ").append(bounds).append("\n")
        s.append("  frame: ").append(frame).append("\n")
        s.append("  signature: ").append(signature).append("\n")
        s.append("  versionMajor: ").append(versionMajor).append("\n")
        s.append("  versionMinor: ").append(versionMinor).append("\n")
        s.append("  #bytes: ").append(bytes).append("\n")
        s.append("  #records: ").append(records).append("\n")
        s.append("  #handles: ").append(handles).append("\n")
        s.append("  description: ").append(description).append("\n")
        s.append("  #palEntries: ").append(palEntries).append("\n")
        s.append("  device: ").append(device).append("\n")
        s.append("  millimeters: ").append(millimeters).append("\n")

        s.append("  openGL: ").append(this.isOpenGL).append("\n")
        s.append("  micrometers: ").append(micrometers)

        return s.toString()
    }

    companion object {
        private val screenMM = Dimension(320, 240)
    }
}

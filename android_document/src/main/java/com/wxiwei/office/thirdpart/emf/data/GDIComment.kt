// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.ByteArrayInputStream
import java.io.IOException

//import javax.imageio.ImageIO;
/**
 * GDIComment TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: GDIComment.java 12753 2007-06-12 22:32:31Z duns $
 */
class GDIComment() : EMFTag(70, 1) {
    private var type = 0

    private var comment = ""

    //private Image image;
    private var image: Bitmap? = null

    constructor(comment: String) : this() {
        this.comment = comment
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val result = GDIComment()

        var l = emf.readDWORD()

        result.type = emf.readDWORD()

        // not documented, but embedded GIF / PNG images
        // start with that tag
        if (result.type == 726027589) {
            /*byte[] bytes = */
            emf.readByte(l - 4)
            if (l % 4 != 0) {
                emf.readBYTE(4 - l % 4)
            }
        } else if (result.type == EMFConstants.GDICOMMENT_BEGINGROUP) {
            // This is the bounding rectangle for the
            // object in logical coordinates.
            /* Rectangle rclOutput = */
            emf.readRECTL()

            // This is the number of characters in the
            // optional Unicode description string that
            // follows. This is zero if there is no
            // description string.
            val nDescription = emf.readDWORD()

            // read the description
            if (nDescription > 0) {
                result.comment = String(emf.readByte(nDescription))
            }
        } else if (result.type == EMFConstants.GDICOMMENT_ENDGROUP) {
            // nothing to to
        } else if (result.type == EMFConstants.GDICOMMENT_MULTIFORMATS) {
            // This is the bounding rectangle for the
            // picture in logical coordinates.
            /* Rectangle rclOutput = */
            emf.readRECTL()

            // This contains the number of formats in
            // the comment.
            /* nFormats = */
            emf.readDWORD()

            // This is an array of EMRFORMAT structures
            // in the order of preference.  The data
            // for each format follows the last
            // EMRFORMAT structure.

            // TODO read tagEMRFORMAT

            /*
            typedef struct tagEMRFORMAT {
                  DWORD   dSignature;
                  DWORD   nVersion;
                  DWORD   cbData;
                  DWORD   offData;
                } EMRFORMAT;
            */
            l = l - 4 - 8

            result.comment = String(emf.readBYTE(l))
            // Align to 4-byte boundary
            if (l % 4 != 0) emf.readBYTE(4 - l % 4)
        } else if (result.type == EMFConstants.GDICOMMENT_WINDOWS_METAFILE) {
            // This contains the version number of the
            // Windows-format metafile.
            /*int version =*/
            emf.readDWORD()

            // This is the additive DWORD checksum for
            // the enhanced metafile.  The checksum
            // for the enhanced metafile data including
            // this comment record must be zero.
            // Otherwise, the enhanced metafile has been
            //  modified and the Windows-format
            // metafile is no longer valid.
            /*int checksum =*/
            emf.readDWORD()

            // This must be zero.
            /* int fFlags = */
            emf.readDWORD()

            // This is the size, in bytes. of the
            // Windows-format metafile data that follows.
            val size = emf.readDWORD()

            // read the image data
            val bytes = emf.readByte(size)
            //            result.image = ImageIO.read(new ByteArrayInputStream(bytes));
            result.image = BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
            return this
        } else {
            l = l - 4

            if (l > 0) {
                result.comment = String(emf.readBYTE(l))
                // Align to 4-byte boundary
                if (l % 4 != 0) {
                    emf.readBYTE(4 - l % 4)
                }
            } else {
                comment = ""
            }
        }
        return result
    }

    override fun toString(): String {
        return super.toString() + "\n  length: " + comment.length
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // do nothing
    }
}

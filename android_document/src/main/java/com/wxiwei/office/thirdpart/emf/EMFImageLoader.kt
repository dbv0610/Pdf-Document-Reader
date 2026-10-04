package com.wxiwei.office.thirdpart.emf

import android.graphics.Bitmap
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.data.BitmapInfoHeader
import com.wxiwei.office.thirdpart.emf.data.BlendFunction
import java.io.IOException
import java.util.Arrays


/**
 * this class creates a BufferedImage from EMF imaga data stored in
 * a byte[].
 * 
 * @author Steffen Greiffenberg
 * @version $Id$
 */
object EMFImageLoader {
    /**
     * creates a BufferedImage from an EMFInputStream using
     * BitmapInfoHeader data
     * 
     * @param bmi BitmapInfoHeader storing Bitmap informations
     * @param width expected image width
     * @param height expected image height
     * @param emf EMF stream
     * @param len length of image data
     * @param blendFunction contains values for transparency
     * @return BufferedImage or null
     * @throws IOException thrown by EMFInputStream
     */
    @Throws(IOException::class)
    fun readImage(
        bmi: BitmapInfoHeader,
        width: Int,
        height: Int,
        emf: EMFInputStream,
        len: Int,
        blendFunction: BlendFunction?
    ): Bitmap? {
        // 0    Windows 98/Me, Windows 2000/XP: The number of bits-per-pixel
        // is specified or is implied by the JPEG or PNG format.

        var width = width
        var height = height
        var len = len
        if (bmi.bitCount == 1) {
            // 1 	The bitmap is monochrome, and the bmiColors
            // member of BITMAPINFO contains two entries. Each
            // bit in the bitmap array represents a pixel. If
            // the bit is clear, the pixel is displayed with
            // the color of the first entry in the bmiColors
            // table; if the bit is set, the pixel has the color
            // of the second entry in the table.
            // byte[] bytes = emf.readByte(len);

            var blue = emf.readUnsignedByte()
            var green = emf.readUnsignedByte()
            var red = emf.readUnsignedByte()
            /*int unused =*/
            emf.readUnsignedByte()

            val color1 = Color(red, green, blue).getRGB()

            blue = emf.readUnsignedByte()
            green = emf.readUnsignedByte()
            red = emf.readUnsignedByte()
            /*unused = */
            emf.readUnsignedByte()

            val color2 = Color(red, green, blue).getRGB()

            //BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            val data = emf.readUnsignedByte(len - 8)

            // TODO: this is highly experimental and does
            // not work for the tested examples
            var strangeOffset = width % 8
            if (strangeOffset != 0) {
                strangeOffset = 8 - strangeOffset
            }

            // iterator for pixel data
            var pixel = 0

            // mask for getting the bits from a pixel data byte
            val mask = intArrayOf(0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80)

            // image data are swapped compared to java standard
            for (y in height - 1 downTo -1 + 1) {
                for (x in 0..<width) {
                    val pixelDataGroup = data[pixel / 8]
                    val pixelData = pixelDataGroup and mask[pixel % 8]
                    pixel++

                    if (pixelData > 0) {
                        //result.setRGB(x, y, color2);
                        result.setPixel(x, y, color2)
                    } else {
                        //result.setRGB(x, y, color1);
                        result.setPixel(x, y, color1)
                    }
                }
                // add the extra width
                pixel = pixel + strangeOffset
            }

            /* for debugging: shows every loaded image
            javax.swing.JFrame f = new javax.swing.JFrame("test");
            f.getContentPane().setBackground(Color.green);
            f.getContentPane().setLayout(
                new java.awt.BorderLayout(0, 0));
            f.getContentPane().add(
                java.awt.BorderLayout.CENTER,
                new javax.swing.JLabel(
                    new javax.swing.ImageIcon(result)));
            f.setSize(new com.wxiwei.office.java.awt.Dimension(width + 20, height + 20));
            f.setVisible(true);*/
            return result
        } else if ((bmi.bitCount == 4)
            && (bmi.compression == EMFConstants.Companion.BI_RGB)
        ) {
            // 4 The bitmap has a maximum of 256 colors, and the bmiColors
            // member
            // of BITMAPINFO contains up to 256 entries. In this case, each byte
            // in
            // the array represents a single pixel.

            // TODO has to be done in BitMapInfoHeader?
            // read the color table

            val colorsUsed = bmi.clrUsed

            // typedef struct tagRGBQUAD {
            // BYTE rgbBlue;
            // BYTE rgbGreen;
            // BYTE rgbRed;
            // BYTE rgbReserved;
            // } RGBQUAD;
            val colors = emf.readUnsignedByte(colorsUsed * 4)

            // data a indexes to a certain color in the colortable.
            // Each byte represents a pixel
            // int[] data = emf.readUnsignedByte(len - (colorsUsed * 4));
            val data = IntArray(len - (colorsUsed * 4))
            for (i in 0..<(len - (colorsUsed * 4)) / 12) {
                val bytes = emf.readUnsignedByte(10)
                emf.readUnsignedByte(2)
                System.arraycopy(bytes, 0, data, i * 10, 10)
            }

            // convert it to a color table
            val colorTable = IntArray(256)
            // iterator for color data
            var color = 0
            var i = 0
            while (i < colorsUsed) {
                colorTable[i] = Color(
                    colors[color + 2],
                    colors[color + 1],
                    colors[color]
                ).getRGB()
                i++
                color = i * 4
            }

            // fill with black to avoid ArrayIndexOutOfBoundExceptions;
            // somme images seem to use more colors than stored in ClrUsed
            if (colorsUsed < 256) {
                Arrays.fill(colorTable, colorsUsed, 256, 0)
            }

            // create the image
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // iterator for pixel data
            var pixel = 0

            // image data are swapped compared to java standard
            for (y in height - 1 downTo -1 + 1) {
                var x = 0
                while (x < width) {
                    if (pixel < data.size) {
                        result.setPixel(x, y, colorTable[data[pixel] % 8])
                        result.setPixel(x + 1, y, colorTable[data[pixel] % 8])
                        pixel++
                    } else {
                        break
                    }
                    x += 2
                }
            }
            return result
        } else if ((bmi.bitCount == 8) &&
            (bmi.compression == EMFConstants.Companion.BI_RGB)
        ) {
            // 8 	The bitmap has a maximum of 256 colors, and the bmiColors member
            // of BITMAPINFO contains up to 256 entries. In this case, each byte in
            // the array represents a single pixel.

            // TODO has to be done in BitMapInfoHeader?
            // read the color table

            val colorsUsed = bmi.clrUsed

            // typedef struct tagRGBQUAD {
            //   BYTE    rgbBlue;
            //   BYTE    rgbGreen;
            //   BYTE    rgbRed;
            //   BYTE    rgbReserved;
            // } RGBQUAD;
            val colors = emf.readUnsignedByte(colorsUsed * 4)

            // data a indexes to a certain color in the colortable.
            // Each byte represents a pixel
            val data = emf.readUnsignedByte(len - (colorsUsed * 4))

            // convert it to a color table
            val colorTable = IntArray(256)
            // iterator for color data
            var color = 0
            var i = 0
            while (i < colorsUsed) {
                colorTable[i] = Color(
                    colors[color + 2],
                    colors[color + 1],
                    colors[color]
                ).getRGB()
                i++
                color = i * 4
            }

            // fill with black to avoid ArrayIndexOutOfBoundExceptions;
            // somme images seem to use more colors than stored in ClrUsed
            if (colorsUsed < 256) {
                Arrays.fill(colorTable, colorsUsed, 256, 0)
            }

            // don't know why, but the width has to be adjusted ...
            // it took more than an hour to determine the strangeOffset
            var strangeOffset = width % 4
            if (strangeOffset != 0) {
                strangeOffset = 4 - strangeOffset
            }

            // create the image
            //BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // iterator for pixel data
            var pixel = 0

            // image data are swapped compared to java standard
            for (y in height - 1 downTo -1 + 1) {
                for (x in 0..<width) {
                    //result.setRGB(x, y, colorTable[data[pixel++]]);
                    result.setPixel(x, y, colorTable[data[pixel++]])
                }
                // add the extra width
                pixel = pixel + strangeOffset
            }

            return result
        } else if ((bmi.bitCount == 16) &&
            (bmi.compression == EMFConstants.Companion.BI_RGB)
        ) {
            // Each WORD in the bitmap array represents a single pixel. The
            // relative intensities of red, green, and blue are represented with
            // five bits for each color component. The value for blue is in the least
            // significant five bits, followed by five bits each for green and red.
            // The most significant bit is not used. The bmiColors color table is used
            // for optimizing colors used on palette-based devices, and must contain
            // the number of entries specified by the biClrUsed member of the
            // BITMAPINFOHEADER.

            val data = emf.readDWORD(len / 4)

            // don't know why, by the width has to be the half ...
            // maybe that has something to do with sie HALFTONE rendering setting.
            width = (width + (width % 2)) / 2
            // to avoid ArrayIndexOutOfBoundExcesptions
            height = data.size / width / 2

            // create a non transparent image
            //BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // found no sample and color model to mak this work
            // tag.image.setRGB(0, 0, tag.widthSrc, tag.heightSrc, data, 0, 0);

            // used in the loop
            var off = 0
            var pixel: Int
            var neighbor: Int

            // image data are swapped compared to java standard
            var y = height - 1
            while (y > -1) {
                for (x in 0..<width) {
                    neighbor = data[off + width]
                    pixel = data[off++]

                    // compute the average of the pixel and it's neighbor
                    // and set the reulting color values
                    //result.setRGB(x, y, new Color(
                    result.setPixel(
                        x, y, Color( // 0xF800 = 2 * 0x7C00
                            ((pixel and 0x7C00) + (neighbor and 0x7C00)).toFloat() / 0xF800,
                            ((pixel and 0x3E0) + (neighbor and 0x3E0)).toFloat() / 0x7C0,
                            ((pixel and 0x1F) + (neighbor and 0x1F)).toFloat() / 0x3E
                        ).getRGB()
                    )
                }
                y--
                off = off + width
            }

            /* for debugging: shows every loaded image
            javax.swing.JFrame f = new javax.swing.JFrame("test");
            f.getContentPane().setBackground(Color.green);
            f.getContentPane().setLayout(
                new java.awt.BorderLayout(0, 0));
            f.getContentPane().add(
                java.awt.BorderLayout.CENTER,
                new javax.swing.JLabel(
                    new javax.swing.ImageIcon(result)));
            f.pack();
            f.setVisible(true);*/
            return result
        } else if ((bmi.bitCount == 32) &&
            (bmi.compression == EMFConstants.Companion.BI_RGB)
        ) {
            // Each DWORD in the bitmap array represents the relative intensities of blue,
            // green, and red, respectively, for a pixel. The high byte in each DWORD is not
            // used. The bmiColors color table is used for optimizing colors used on
            // palette-based devices, and must contain the number of entries specified
            // by the biClrUsed member of the BITMAPINFOHEADER.

            //width = (width + (width % 20)) / 20;
            //height = (height + (height % 20)) / 20;

            // create a transparent image
            //BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            // read the image data
            //int[] data = emf.readDWORD(len / 4);
            len /= 4

            // used to iterate the pixels later
            var off = 0
            var pixel: Int
            var alpha: Int

            // The SourceConstantaAlpha member of BLENDFUNCTION specifies an alpha transparency
            // value to be used on the entire source bitmap. The SourceConstantAlpha value is
            // combined with any per-pixel alpha values. If SourceConstantAlpha is 0, it is
            // assumed that the image is transparent. Set the SourceConstantAlpha value to 255
            // (which indicates that the image is opaque) when you only want to use per-pixel
            // alpha values.
            //int sourceConstantAlpha = blendFunction.getSourceConstantAlpha();
            var sourceConstantAlpha = 0xFF
            var alphaFormat: Int = EMFConstants.Companion.AC_SRC_OVER

            if (blendFunction != null) {
                sourceConstantAlpha = blendFunction.sourceConstantAlpha
                alphaFormat = blendFunction.alphaFormat
            }

            //if (blendFunction.getAlphaFormat() != EMFConstants.AC_SRC_ALPHA) {
            if (alphaFormat != EMFConstants.Companion.AC_SRC_ALPHA) {
                // If the source bitmap has no per-pixel alpha value (that is, AC_SRC_ALPHA is not
                // set), the SourceConstantAlpha value determines the blend of the source and
                // destination bitmaps, as shown in the following table. Note that SCA is used
                // for SourceConstantAlpha here. Also, SCA is divided by 255 because it has a
                // value that ranges from 0 to 255.

                // Dst.Red 	= Src.Red * (SCA/255.0) 	+ Dst.Red * (1.0 - (SCA/255.0))
                // Dst.Green 	= Src.Green * (SCA/255.0) 	+ Dst.Green * (1.0 - (SCA/255.0))
                // Dst.Blue 	= Src.Blue * (SCA/255.0) 	+ Dst.Blue * (1.0 - (SCA/255.0))

                // If the destination bitmap has an alpha channel, then the blend is as follows.
                // Dst.Alpha 	= Src.Alpha * (SCA/255.0) 	+ Dst.Alpha * (1.0 - (SCA/255.0))

                var y = height - 1
                while (y > -1 && off < len) {
                    var x = 0
                    while (x < width && off < len) {
                        //pixel = data[off++];
                        pixel = emf.readDWORD()

                        //result.setRGB(x, y, new Color(
                        result.setPixel(
                            x, y, Color(
                                (pixel and 0xFF0000) shr 16,
                                (pixel and 0xFF00) shr 8,
                                (pixel and 0xFF),  // TODO not tested
                                sourceConstantAlpha
                            ).getRGB()
                        )
                        x++
                        off++
                    }
                    y--
                }
            } else {
                // If the source bitmap does not use SourceConstantAlpha (that is, it equals
                // 0xFF), the per-pixel alpha determines the blend of the source and destination
                // bitmaps, as shown in the following table.
                if (sourceConstantAlpha == 0xFF) {
                    // Dst.Red 	= Src.Red 	+ (1 - Src.Alpha) * Dst.Red
                    // Dst.Green 	= Src.Green 	+ (1 - Src.Alpha) * Dst.Green
                    // Dst.Blue 	= Src.Blue 	+ (1 - Src.Alpha) * Dst.Blue

                    // If the destination bitmap has an alpha channel, then the blend is as follows.
                    // Dest.alpha 	= Src.Alpha 	+ (1 - SrcAlpha) * Dst.Alpha

                    // image data are swapped compared to java standard

                    var y = height - 1
                    while (y > -1 && off < len) {
                        var x = 0
                        while (x < width && off < len) {
                            //pixel = data[off++];
                            pixel = emf.readDWORD()
                            alpha = (pixel and -0x1000000) shr 24
                            if (alpha == -1) {
                                alpha = 0xFF
                            }

                            //result.setRGB(x, y, new Color(
                            result.setPixel(
                                x, y, Color(
                                    (pixel and 0xFF0000) shr 16,
                                    (pixel and 0xFF00) shr 8,
                                    (pixel and 0xFF),
                                    alpha
                                ).getRGB()
                            )
                            x++
                            off++
                        }
                        y--
                    }
                } else {
                    // Src.Red 	= Src.Red 	* SourceConstantAlpha / 255.0;
                    // Src.Green 	= Src.Green 	* SourceConstantAlpha / 255.0;
                    // Src.Blue 	= Src.Blue 	* SourceConstantAlpha / 255.0;
                    // Src.Alpha 	= Src.Alpha 	* SourceConstantAlpha / 255.0;

                    // Dst.Red 	= Src.Red 	+ (1 - Src.Alpha) * Dst.Red
                    // Dst.Green 	= Src.Green 	+ (1 - Src.Alpha) * Dst.Green
                    // Dst.Blue 	= Src.Blue 	+ (1 - Src.Alpha) * Dst.Blue
                    // Dst.Alpha 	= Src.Alpha 	+ (1 - Src.Alpha) * Dst.Alpha

                    var y = height - 1
                    while (y > -1 && off < len) {
                        var x = 0
                        while (x < width && off < len) {
                            //pixel = data[off++];
                            pixel = emf.readDWORD()

                            alpha = (pixel and -0x1000000) shr 24
                            if (alpha == -1) {
                                alpha = 0xFF
                            }

                            // TODO not tested
                            alpha = alpha * sourceConstantAlpha / 0xFF

                            //result.setRGB(x, y, new Color(
                            result.setPixel(
                                x, y, Color(
                                    (pixel and 0xFF0000) shr 16,
                                    (pixel and 0xFF00) shr 8,
                                    (pixel and 0xFF),
                                    alpha
                                ).getRGB()
                            )
                            x++
                            off++
                        }
                        y--
                    }
                }
            }


            /* for debugging: shows every loaded image
            javax.swing.JFrame f = new javax.swing.JFrame("test");
            f.getContentPane().setBackground(Color.green);
            f.getContentPane().setLayout(
                new java.awt.BorderLayout(0, 0));
            f.getContentPane().add(
                java.awt.BorderLayout.CENTER,
                new javax.swing.JLabel(
                    new javax.swing.ImageIcon(result)));
            f.setSize(new com.wxiwei.office.java.awt.Dimension(width + 20, height + 20));
            f.setVisible(true);*/
            return result
        } else if ((bmi.bitCount == 32) &&
            (bmi.compression == EMFConstants.Companion.BI_BITFIELDS)
        ) {
            /* byte[] bytes =*/
            emf.readByte(len)
            return null
        } else {
            /* byte[] bytes =*/
            emf.readByte(len)
            return null
        }
    }
}

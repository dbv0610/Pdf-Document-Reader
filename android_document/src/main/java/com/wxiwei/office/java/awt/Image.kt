/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.java.awt

/**
 * The abstract class <code>Image</code> is the superclass of all
 * classes that represent graphical images. The image must be
 * obtained in a platform-specific manner.
 *
 * @author  Sami Shaio
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
abstract class Image {
    companion object {
        /**
         * Use the default image-scaling algorithm.
         * @since JDK1.1
         */
        const val SCALE_DEFAULT = 1

        /**
         * Choose an image-scaling algorithm that gives higher priority
         * to scaling speed than smoothness of the scaled image.
         * @since JDK1.1
         */
        const val SCALE_FAST = 2

        /**
         * Choose an image-scaling algorithm that gives higher priority
         * to image smoothness than scaling speed.
         * @since JDK1.1
         */
        const val SCALE_SMOOTH = 4

        const val SCALE_REPLICATE = 8

        const val SCALE_AREA_AVERAGING = 16
    }
}

/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt

/**
 * The <code>Stroke</code> interface allows a
 * <code>Graphics2D</code> object to obtain a [Shape] that is the
 * decorated outline, or stylistic representation of the outline,
 * of the specified <code>Shape</code>.
 * Stroking a <code>Shape</code> is like tracing its outline with a
 * marking pen of the appropriate size and shape.
 * The area where the pen would place ink is the area enclosed by the
 * outline <code>Shape</code>.
 */
interface Stroke {
    /**
     * Returns an outline <code>Shape</code> which encloses the area that
     * should be painted when the <code>Shape</code> is stroked according
     * to the rules defined by the
     * object implementing the <code>Stroke</code> interface.
     * @param p a <code>Shape</code> to be stroked
     * @return the stroked outline <code>Shape</code>.
     */
    fun createStrokedShape(p: Shape?): Shape?
}

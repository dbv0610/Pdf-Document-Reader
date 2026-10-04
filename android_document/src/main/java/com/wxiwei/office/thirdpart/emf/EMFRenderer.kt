package com.wxiwei.office.thirdpart.emf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Region
import android.graphics.Typeface
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Image
import com.wxiwei.office.java.awt.Shape
import com.wxiwei.office.java.awt.Stroke
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.AffineTransform.Companion.getScaleInstance
import com.wxiwei.office.java.awt.geom.Area
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.java.awt.geom.IllegalPathStateException
import com.wxiwei.office.java.awt.geom.Path2D
import com.wxiwei.office.java.awt.geom.PathIterator
import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.thirdpart.emf.data.BasicStroke
import com.wxiwei.office.thirdpart.emf.data.GDIObject
import com.wxiwei.office.thirdpart.emf.io.Tag
import java.util.Stack
import java.util.Vector
import java.util.logging.Logger

/**
 * Standalone EMF renderer.
 * 
 * @author Daniel Noll (daniel@nuix.com)
 * @version $Id$
 */
class EMFRenderer
    (`is`: EMFInputStream) {
    /**
     * Header read from the EMFInputStream
     */
    private val header: EMFHeader

    /**
     * affect by all XXXTo methods, e.g. LinTo. ExtMoveTo creates the
     * starting point. CloseFigure closes the figure.
     */
    var figure: GeneralPath? = null

    /**
     * AffineTransform which is the base for all rendering
     * operations.
     */
    //    private AffineTransform initialTransform;
    private var initialMatrix: Matrix? = null

    /**
     * origin of the emf window, set by SetWindowOrgEx
     */
    private var windowOrigin: Point? = null

    /**
     * origin of the emf viewport, set By SetViewportOrgEx
     */
    private var viewportOrigin: Point? = null

    /**
     * size of the emf window, set by SetWindowExtEx
     */
    private var windowSize: Dimension? = null

    /**
     * size of the emf viewport, set by SetViewportExtEx
     */
    private var viewportSize: Dimension? = null

    /**
     * The MM_ISOTROPIC mode ensures a 1:1 aspect ratio.
     * The MM_ANISOTROPIC mode allows the x-coordinates
     * and y-coordinates to be adjusted independently.
     */
    private var mapModeIsotropic = false

    /**
     * AffineTransform defined by SetMapMode. Used for
     * resizing the emf to propper device bounds.
     */
    var mapModeTransform: AffineTransform? = getScaleInstance(
        TWIP_SCALE,
        TWIP_SCALE
    )

    /**
     * clipping area which is the base for all rendering
     * operations.
     */
    //    private Shape initialClip;
    private var initialClip: Shape? = null

    /**
     * current Graphics2D to paint on. It is set during
     * [.paint]
     */
    //    private Graphics2D g2;
    private var mCanvas: Canvas? = null

    /**
     * objects used by [org.freehep.graphicsio.emf.gdi.SelectObject].
     * The array is filled by CreateXXX functions, e.g.
     * [org.freehep.graphicsio.emf.gdi.CreatePen]
     */
    private val gdiObjects = arrayOfNulls<GDIObject>(256) // TODO: Make this more flexible.

    // Rendering state.
    //    private Paint brushPaint = new Color(0, 0, 0, 0);
    //    private Paint penPaint = Color.BLACK;
    var penStroke: Stroke? = BasicStroke()
    private val brushPaint = Paint()
    private val penPaint = Paint()

    var textAlignMode: Int = 0

    /**
     * color for simple text rendering
     */
    private var textColor = Color.BLACK

    // ---------------------------------------------------------------------
    //            simple getter / setter methods
    // ---------------------------------------------------------------------
    /**
     * written by [org.freehep.graphicsio.emf.gdi.SetPolyFillMode] used by
     * e.g. [org.freehep.graphicsio.emf.gdi.PolyPolygon16]
     */
    var windingRule: Int = Path2D.WIND_EVEN_ODD

    /**
     * Defined by SetBkModes, either [EMFConstants.BKG_OPAQUE] or
     * [EMFConstants.BKG_TRANSPARENT]. Used in
     * [.fillAndDrawOrAppend]
     */
    private var bkMode: Int = EMFConstants.Companion.BKG_OPAQUE

    /**
     * The SetBkMode function affects the line styles for lines drawn using a
     * pen created by the CreatePen function. SetBkMode does not affect lines
     * drawn using a pen created by the ExtCreatePen function.
     */
    private var useCreatePen = true

    /**
     * The miter length is defined as the distance from the intersection
     * of the line walls on the inside of the join to the intersection of
     * the line walls on the outside of the join. The miter limit is the
     * maximum allowed ratio of the miter length to the line width.
     */
    private var meterLimit = 10

    /**
     * The SetROP2 function sets the current foreground mix mode.
     * Default is to use the pen.
     */
    private var rop2: Int = EMFConstants.Companion.R2_COPYPEN

    /**
     * e.g. [Image.SCALE_SMOOTH] for rendering images
     */
    private var scaleMode = Image.SCALE_SMOOTH

    /**
     * The brush origin is a pair of coordinates specifying the location of one
     * pixel in the bitmap. The default brush origin coordinates are (0,0). For
     * horizontal coordinates, the value 0 corresponds to the leftmost column
     * of pixels; the width corresponds to the rightmost column. For vertical
     * coordinates, the value 0 corresponds to the uppermost row of pixels;
     * the height corresponds to the lowermost row.
     */
    var brushOrigin: Point? = Point(0, 0)

    /**
     * stores the parsed tags. Filled by the constructor. Read by
     * [.paint]
     */
    private val tags: Vector<Tag?> = Vector<Tag?>(0)

    /**
     * Created by BeginPath and closed by EndPath.
     */
    var path: GeneralPath? = null

    /**
     * The transformations set by ModifyWorldTransform are redirected to
     * that AffineTransform. They do not affect the current paint context,
     * after BeginPath is called. Only the figures appended to path
     * are transformed by this AffineTransform.
     * BeginPath clears the transformation, ModifyWorldTransform changes ist.
     */
    var pathTransform: AffineTransform? = AffineTransform()

    /**
     * [org.freehep.graphicsio.emf.gdi.SaveDC] stores
     * an Instance of DC if saveDC is read. RestoreDC pops an object.
     */
    private val dcStack: Stack<DC> = Stack<DC>()

    /**
     * default direction is counterclockwise
     */
    var arcDirection: Int = EMFConstants.Companion.AD_COUNTERCLOCKWISE

    private var mCurrClip: Area? = null

    private var escapement = 0

    /**
     * Class the encapsulate the state of a Graphics2D object.
     * Instances are store in dcStack by
     * [org.freehep.graphicsio.emf.EMFRenderer.paint]
     */
    private inner class DC {
        var paint: Paint? = null
        val stroke: Stroke? = null
        val transform: AffineTransform? = null
        var clip: Shape? = null

        //private Rect clip;
        // Added by wangtang
        var matrix: Matrix? = null
        var path: GeneralPath? = null
        var bkMode: Int = 0
        var windingRule: Int = 0
        var meterLimit: Int = 0
        var useCreatePen: Boolean = false
        var scaleMode: Int = 0
        var pathTransform: AffineTransform? = null
    }

    /**
     * Constructs the renderer.
     * 
     * @param is the input stream to read the EMF records from.
     * @throws IOException if an error occurs reading the header.
     */
    init {
        // Initialize Paint
        brushPaint.setColor(Color(0, 0, 0, 0).getRGB())
        penPaint.setColor(Color.BLACK.getRGB())

        this.header = `is`.readHeader()

        // read all tags
        var tag: Tag?
        while ((`is`.readTag().also { tag = it }) != null) {
            tags.add(tag)
        }
        `is`.close()
    }

    val size: Dimension
        /**
         * Gets the size of a canvas which would be required to render the EMF.
         * 
         * @return the size.
         */
        get() = header.bounds!!.getSize()
    // TODO see the mapModePart of resetTransformation()
    // if uncommented size is too small
    /* Dimension bounds = header.getBounds().getSize();
         return new Dimension(
         (int)Math.ceil(bounds.width * TWIP_SCALE),
         (int)Math.ceil(bounds.height * TWIP_SCALE));*/

    /**
     * Paints the EMF onto the provided graphics context.
     * 
     * @param g2 the graphics context to paint onto.
     */
    //    public void paint(Graphics2D g2)
    fun paint(canvas: Canvas) {
        //this.g2 = g2;
        this.mCanvas = canvas

        // store at leat clip and transformation
        //Shape clip = g2.getClip();
        val rect = canvas.getClipBounds()


        //AffineTransform at = g2.getTransform();
        val matrix = canvas.getMatrix()
        //        mM = mC.getMatrix();
//        Rect r = mC.getClipBounds();
        val cl: IntArray? = intArrayOf(
            -1,
            rect.top,
            rect.left,
            -2,
            rect.top,
            rect.right,
            -2,
            rect.bottom,
            rect.right,
            -2,
            rect.bottom,
            rect.left
        )
        mCurrClip = Area(createShape(cl!!))


//        Map hints = g2.getRenderingHints();

        // some quality settings
//        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
//            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        penPaint.setAntiAlias(true)
        penPaint.setFilterBitmap(true)
        penPaint.setDither(true)

        // used by SetWorldTransform to reset transformation
//        initialTransform = g2.getTransform();
        initialMatrix = canvas.getMatrix()

        // set the initial value, defaults for EMF
        path = null
        figure = null
        meterLimit = 10
        windingRule = Path2D.WIND_EVEN_ODD
        bkMode = EMFConstants.Companion.BKG_OPAQUE
        useCreatePen = true
        scaleMode = Image.SCALE_SMOOTH

        windowOrigin = null
        viewportOrigin = null
        windowSize = null
        viewportSize = null

        mapModeIsotropic = false
        mapModeTransform = getScaleInstance(TWIP_SCALE, TWIP_SCALE)

        // apply all default settings
        //resetTransformation(g2);
        resetMatrix(canvas)

        // determin initial clip after all basic transformations
//        initialClip = g2.getClip();
        initialClip = mCurrClip

        // iterate and render all tags
        var tag: Tag?
        for (i in tags.indices) {
            tag = tags.get(i) as Tag?
            if (tag is EMFTag) {
                (tags.get(i) as EMFTag).render(this)
            } else {
                logger.warning("unknown tag: " + tag)
            }
        }

        // reset Transform and clip
//        g2.setRenderingHints(hints);
//        g2.setTransform(at);
//        g2.setClip(clip);
        penPaint.setAntiAlias(true)
        penPaint.setFilterBitmap(true)
        penPaint.setDither(true)

        canvas.setMatrix(matrix)
        //        canvas.clipRect(rect);
        this.clip = initialClip
    }

    // ---------------------------------------------------------------------
    //            complex drawing methods for EMFTags
    // ---------------------------------------------------------------------
    /**
     * set the initial transform, the windowOrigin and viewportOrigin,
     * scales by viewportSize and windowSize
     * @param g2 Context to apply transformations
     */
    //    private void resetTransformation(Graphics2D g2)
    //    {
    //        // rest to device configuration
    //        if (initialTransform != null)
    //        {
    //            g2.setTransform(initialTransform);
    //        }
    //        else
    //        {
    //            g2.setTransform(new AffineTransform());
    //        }
    //
    //        /* TODO mapModeTransform dows not work correctly
    //        if (mapModeTransform != null) {
    //            g2.transform(mapModeTransform);
    //        }*/
    //
    //        // move to window origin
    //        if (windowOrigin != null)
    //        {
    //            g2.translate(-windowOrigin.getX(), -windowOrigin.getY());
    //        }
    //        // move to window origin
    //        if (viewportOrigin != null)
    //        {
    //            g2.translate(-viewportOrigin.getX(), -viewportOrigin.getY());
    //        }
    //
    //        // TWIP_SCALE by window and viewport size
    //        if (viewportSize != null && windowSize != null)
    //        {
    //            double scaleX = viewportSize.getWidth() / windowSize.getWidth();
    //            double scaleY = viewportSize.getHeight() / windowSize.getHeight();
    //            g2.scale(scaleX, scaleY);
    //        }
    //    }
    private fun resetMatrix(canvas: Canvas) {
        // rest to device configuration
        if (initialMatrix != null) {
//            g2.setTransform(initialTransform);
            canvas.setMatrix(initialMatrix)
        } else {
            canvas.setMatrix(Matrix())
        }

        /* TODO mapModeTransform dows not work correctly
        if (mapModeTransform != null) {
            g2.transform(mapModeTransform);
        }*/

        // move to window origin
//        if (windowOrigin != null)
//        {
//        	canvas.translate(-windowOrigin.x, -windowOrigin.y);
//        }
        // move to window origin
//        if (viewportOrigin != null)
//        {
//        	canvas.translate(-viewportOrigin.x, -viewportOrigin.y);
//        }

        // TWIP_SCALE by window and viewport size
        if (viewportSize != null && windowSize != null) {
            val scaleX = (viewportSize!!.getWidth() / windowSize!!.getWidth()).toFloat()
            val scaleY = (viewportSize!!.getHeight() / windowSize!!.getHeight()).toFloat()
            canvas.scale(scaleX, scaleY)
        }
    }

    /**
     * Stores the current state. Used by
     * [org.freehep.graphicsio.emf.gdi.SaveDC]
     */
    fun saveDC() {
        // create a DC instance with current settings
        val dc: DC = DC()
        //        dc.paint = g2.getPaint();
        dc.paint = penPaint


        //dc.stroke = g2.getStroke();
//        dc.transform = g2.getTransform();
//        dc.pathTransform = pathTransform;
        dc.matrix = mCanvas!!.getMatrix()
        //        dc.clip = g2.getClip();
        dc.clip = mCurrClip
        dc.path = path
        dc.meterLimit = meterLimit
        dc.windingRule = windingRule
        dc.bkMode = bkMode
        dc.useCreatePen = useCreatePen
        dc.scaleMode = scaleMode
        // push it on top of the stack
        dcStack.push(dc)
        mCanvas!!.save()
    }

    /**
     * Retores a saved state. Used by
     * [org.freehep.graphicsio.emf.gdi.RestoreDC]
     */
    fun retoreDC() {
        // is somethoing stored?
        if (!dcStack.empty()) {
            // read it
            val dc = dcStack.pop() as DC

            // use it
            meterLimit = dc.meterLimit
            windingRule = dc.windingRule
            path = dc.path
            bkMode = dc.bkMode
            useCreatePen = dc.useCreatePen
            scaleMode = dc.scaleMode
            pathTransform = dc.pathTransform
            //            g2.setPaint(dc.paint);
//            g2.setStroke(dc.stroke);
//            g2.setTransform(dc.transform);
//            g2.setClip(dc.clip);
            setStroke(penStroke)

            mCanvas!!.setMatrix(dc.matrix)
            this.clip = dc.clip
        } else {
            // set the default values
        }
        mCanvas!!.restore()
    }

    /**
     * closes and appends the current open figure to the
     * path
     */
    fun closeFigure() {
        if (figure == null) {
            return
        }

        try {
            figure!!.closePath()
            appendToPath(figure!!)
            figure = null
        } catch (e: IllegalPathStateException) {
            logger.warning("no figure to close")
        }
    }

    /**
     * appends the current open figure to the
     * path
     */
    fun appendFigure() {
        if (figure == null) {
            return
        }

        try {
            appendToPath(figure!!)
            figure = null
        } catch (e: IllegalPathStateException) {
            logger.warning("no figure to append")
        }
    }

    /**
     * Logical units are mapped to arbitrary units with equally scaled axes;
     * that is, one unit along the x-axis is equal to one unit along the y-axis.
     * Use the SetWindowExtEx and SetViewportExtEx functions to specify the
     * units and the orientation of the axes. Graphics device interface (GDI)
     * makes adjustments as necessary to ensure the x and y units remain the
     * same size (When the window extent is set, the viewport will be adjusted
     * to keep the units isotropic).
     */
    fun fixViewportSize() {
        if (mapModeIsotropic && (windowSize != null && viewportSize != null)) {
            viewportSize!!.setSize(
                viewportSize!!.getWidth(),
                viewportSize!!.getWidth() * (windowSize!!.getHeight() / windowSize!!.getWidth())
            )
        }
    }

    /**
     * fills a shape using the brushPaint,  penPaint and penStroke
     * @param g2 Painting context
     * @param s Shape to fill with current brush
     */
    //    private void fillAndDrawOrAppend(Graphics2D g2, Shape s)
    private fun fillAndDrawOrAppend(canvas: Canvas, s: Shape) {
        // don't draw, just append the shape if BeginPath
        // has opened the path
        if (!appendToPath(s)) {
            // The SetBkMode function affects the line styles for lines drawn using a
            // pen created by the CreatePen function. SetBkMode does not affect lines
            // drawn using a pen created by the ExtCreatePen function.
            if (useCreatePen) {
                // OPAQUE 	Background is filled with the current background
                // color before the text, hatched brush, or pen is drawn.
                if (bkMode == EMFConstants.Companion.BKG_OPAQUE) {
//                    fillShape(g2, s);
                    fillShape(s)
                } else {
                    // TRANSPARENT 	Background remains untouched.
                    // TODO: if we really do nothing some drawings are incomplete
                    // this needs definitly a fix
//                    fillShape(g2, s);
                    fillShape(s)
                }
            } else {
                // always fill the background if ExtCreatePen is set
//                fillShape(g2, s);
                fillShape(s)
            }
            //            drawShape(g2, s);
            drawShape(canvas, s)
        }
    }

    /**
     * draws a shape using the penPaint and penStroke
     * @param g2 Painting context
     * @param s Shape to draw with current paen
     */
    //    private void drawOrAppend(Graphics2D g2, Shape s)
    //    {
    //        // don't draw, just append the shape if BeginPath
    //        // opens a GeneralPath
    //        if (!appendToPath(s))
    //        {
    //            drawShape(g2, s);
    //        }
    //    }
    private fun drawOrAppend(canvas: Canvas, s: Shape) {
        // don't draw, just append the shape if BeginPath
        // opens a GeneralPath
        if (!appendToPath(s)) {
            drawShape(canvas, s)
        }
    }

    /**
     * draws the text
     * 
     * @param text Text
     * @param x x-Position
     * @param y y-Position
     */
    //    public void drawOrAppendText(String text, double x, double y)
    fun drawOrAppendText(text: String, x: Float, y: Float) {
        // TODO: Use explicit widths to pixel-position each character, if present.
        // TODO: Implement alignment properly.  What we have already seems to work well enough.
        //            FontRenderContext frc = g2.getFontRenderContext();
        //            TextLayout layout = new TextLayout(str, g2.getFont(), frc);
        //            if ((textAlignMode & EMFConstants.TA_CENTER) != 0) {
        //                layout.draw(g2, x + (width - textWidth) / 2, y);
        //            } else if ((textAlignMode & EMFConstants.TA_RIGHT) != 0) {
        //                layout.draw(g2, x + width - textWidth, y);
        //            } else {
        //                layout.draw(g2, x, y);
        //            }

//        if (path != null)
//        {
        // do not use g2.drawString(str, x, y) to be aware of path
//            TextLayout tl = new TextLayout(text, g2.getFont(), g2.getFontRenderContext());
//            path.append(tl.getOutline(null), false);
//        }
//        else
//        {
//            g2.setPaint(textColor);
//            g2.drawString(text, (int)x, (int)y);

        var y = y
        val tmp = penPaint.getStyle()
        // text is filled; the pen is left on stroke by the lines drawn before it
        penPaint.setStyle(Paint.Style.FILL)
        penPaint.setColor(textColor.getRGB())
        penPaint.setStrokeWidth(0f)
        // Vertical text
        if (2700 == escapement) {
            for (i in 0..<text.length) {
                mCanvas!!.drawText(
                    text.get(i).toString(),
                    x,
                    y + i * penPaint.getTextSize(),
                    penPaint
                )
            }
        } else {
            if (EMFConstants.Companion.TA_TOP == textAlignMode) {
                y += penPaint.getTextSize() - 3
            }
            mCanvas!!.drawText(text, x, y, penPaint)
        }
        penPaint.setStyle(tmp)
        //        }
    }

    /**
     * Append the shape to the current path
     * 
     * @param s Shape to fill with current brush
     * @return true, if path was changed
     */
    private fun appendToPath(s: Shape): Boolean {
        // don't draw, just append the shape if BeginPath
        // opens a GeneralPath
        var s = s
        if (path != null) {
            // aplly transformation if set
            if (pathTransform != null) {
                s = pathTransform!!.createTransformedShape(s)!!
            }
            // append the shape
            path!!.append(s, false)
            // current path set
            return true
        }
        // current path not set
        return false
    }

    /**
     * closes the path opened by [org.freehep.graphicsio.emf.gdi.BeginPath]
     */
    fun closePath() {
        if (path != null) {
            try {
                path!!.closePath()
            } catch (e: IllegalPathStateException) {
                logger.warning("no figure to close")
            }
        }
    }

    private fun getCurrentSegment(pi: PathIterator, path: Path) {
        val coordinates = FloatArray(6)
        val type = pi.currentSegment(coordinates)
        when (type) {
            PathIterator.SEG_MOVETO -> path.moveTo(coordinates[0], coordinates[1])
            PathIterator.SEG_LINETO -> path.lineTo(coordinates[0], coordinates[1])
            PathIterator.SEG_QUADTO -> path.quadTo(
                coordinates[0], coordinates[1], coordinates[2],
                coordinates[3]
            )

            PathIterator.SEG_CUBICTO -> path.cubicTo(
                coordinates[0], coordinates[1], coordinates[2],
                coordinates[3], coordinates[4], coordinates[5]
            )

            PathIterator.SEG_CLOSE -> path.close()
            else -> {}
        }
    }

    private fun getPath(s: Shape): Path {
        val path = Path()
        val pi = s.getPathIterator(null)
        while (pi.isDone() == false) {
            getCurrentSegment(pi, path)
            pi.next()
        }
        return path
    }

    /**
     * fills a shape using the brushPaint,  penPaint and penStroke.
     * This method should only be called for path painting. It doesn't check for a
     * current path.
     * 
     * @param g2 Painting context
     * @param s Shape to fill with current brush
     */
    //    private void fillShape(Graphics2D g2, Shape s)
    //    {
    ////        g2.setPaint(brushPaint);
////        g2.fill(s);
    //        Paint.Style tmp = brushPaint.getStyle();
    //        brushPaint.setStyle(Paint.Style.FILL);
    //        mCanvas.drawPath(getPath(s), brushPaint);
    //        brushPaint.setStyle(tmp);
    //    }
    private fun setStroke(stroke: Stroke?) {
//		penPaint.setStyle(Style.STROKE);
//		penPaint.setStrokeCap(Cap.SQUARE);
//		penPaint.setStrokeMiter(10.0f);
        val bs = stroke as BasicStroke
        penPaint.setStyle(Paint.Style.STROKE)
        penPaint.setStrokeWidth(bs.lineWidth)

        val cap = bs.endCap
        if (cap == 0) {
            penPaint.setStrokeCap(Paint.Cap.BUTT)
        } else if (cap == 1) {
            penPaint.setStrokeCap(Paint.Cap.ROUND)
        } else if (cap == 2) {
            penPaint.setStrokeCap(Paint.Cap.SQUARE)
        }

        val join = bs.lineJoin
        if (join == 0) {
            penPaint.setStrokeJoin(Paint.Join.MITER)
        } else if (join == 1) {
            penPaint.setStrokeJoin(Paint.Join.ROUND)
        } else if (join == 2) {
            penPaint.setStrokeJoin(Paint.Join.BEVEL)
        }
        penPaint.setStrokeMiter(bs.miterLimit)
    }

    /**
     * draws a shape using the penPaint and penStroke
     * This method should only be called for path drawing. It doesn't check for a
     * current path.
     * 
     * @param g2 Painting context
     * @param s Shape to draw with current pen
     */
    //    private void drawShape(Graphics2D g2, Shape s)
    private fun drawShape(canvas: Canvas, s: Shape) {
//        g2.setStroke(penStroke);
        setStroke(penStroke)

        // R2_BLACK 	Pixel is always 0.
        if (rop2 == EMFConstants.Companion.R2_BLACK) {
            //g2.setComposite(AlphaComposite.SrcOver);
            penPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
            //            g2.setPaint(Color.black);
            penPaint.setColor(Color.black.getRGB())
        } else if (rop2 == EMFConstants.Companion.R2_COPYPEN) {
//            g2.setComposite(AlphaComposite.SrcOver);
            penPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
            //g2.setPaint(penPaint);
        } else if (rop2 == EMFConstants.Companion.R2_NOP) {
//            g2.setComposite(AlphaComposite.SrcOver);
            penPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
            //            g2.setPaint(penPaint);
        } else if (rop2 == EMFConstants.Companion.R2_WHITE) {
//            g2.setComposite(AlphaComposite.SrcOver);
            penPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
            //            g2.setPaint(Color.white);
            penPaint.setColor(Color.white.getRGB())
        } else if (rop2 == EMFConstants.Companion.R2_NOTCOPYPEN) {
//            g2.setComposite(AlphaComposite.SrcOver);
            penPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
            // TODO: set at least inverted color if paint is a color
        } else if (rop2 == EMFConstants.Companion.R2_XORPEN) {
//            g2.setComposite(AlphaComposite.Xor);
            penPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.XOR))
        } else {
            logger.warning("got unsupported ROP" + rop2)
            // TODO:
            //R2_MASKNOTPEN 	Pixel is a combination of the colors common to both the screen and the inverse of the pen.
            //R2_MASKPEN 	Pixel is a combination of the colors common to both the pen and the screen.
            //R2_MASKPENNOT 	Pixel is a combination of the colors common to both the pen and the inverse of the screen.
            //R2_MERGENOTPEN 	Pixel is a combination of the screen color and the inverse of the pen color.
            //R2_MERGEPEN 	Pixel is a combination of the pen color and the screen color.
            //R2_MERGEPENNOT 	Pixel is a combination of the pen color and the inverse of the screen color.
            //R2_NOT 	Pixel is the inverse of the screen color.
            //R2_NOTCOPYPEN 	Pixel is the inverse of the pen color.
            //R2_NOTMASKPEN 	Pixel is the inverse of the R2_MASKPEN color.
            //R2_NOTMERGEPEN 	Pixel is the inverse of the R2_MERGEPEN color.
            //R2_NOTXORPEN 	Pixel is the inverse of the R2_XORPEN color.
        }

        //g2.draw(s);
        canvas.drawPath(getPath(s), penPaint)
    }

    // ---------------------------------------------------------------------
    //            simple wrapping methods to the painting context
    // ---------------------------------------------------------------------
    //    public void setFont(Font font)
    //    {
    //        g2.setFont(font);
    //    }
    fun setFont(font: Font?) {
        //g2.setFont(font);
        if (font == null) {
            return
        }

        //        mFnt = font;
        var tf: Typeface? = null
        val nam = font.getName()
        val sty = font.getStyle()
        var aF = ""
        if (nam != null) {
            if (nam.equals("Serif", ignoreCase = true)
                || nam.equals("TimesRoman", ignoreCase = true)
            ) {
                aF = "serif"
            } else if (nam.equals("SansSerif", ignoreCase = true)
                || nam.equals("Helvetica", ignoreCase = true)
            ) {
                aF = "sans-serif"
            } else if (nam.equals("Monospaced", ignoreCase = true)
                || nam.equals("Courier", ignoreCase = true)
            ) {
                aF = "monospace"
            } else {
                aF = "sans-serif"
            }
        }

        when (sty) {
            Font.PLAIN -> tf = Typeface.create(aF, Typeface.NORMAL)
            Font.BOLD -> tf = Typeface.create(aF, Typeface.BOLD)
            Font.ITALIC -> tf = Typeface.create(aF, Typeface.ITALIC)
            Font.BOLD or Font.ITALIC -> tf = Typeface.create(aF, Typeface.BOLD_ITALIC)
            else -> tf = Typeface.DEFAULT
        }

        penPaint.setTextSize(font.getFontSize().toFloat())
        penPaint.setTypeface(tf)
    }

    fun setEscapement(escapement: Int) {
        this.escapement = escapement
    }

    var matrix: Matrix?
        //    public AffineTransform getTransform()
        get() = mCanvas!!.getMatrix()
        //    public void setTransform(AffineTransform at)
        set(matrix) {
            mCanvas!!.setMatrix(matrix)
        }

    fun transform(transform: AffineTransform) {
//        g2.transform(transform);
//    	Matrix matrix = new Matrix();
//    	matrix.setValues(createMatrix(transform));
//    	mCanvas.setMatrix(matrix);
        val matrix = Matrix()
        matrix.setValues(createMatrix(transform))
        mCanvas!!.concat(matrix)
    }

    fun resetTransformation() {
//        resetTransformation(g2);
        resetMatrix(mCanvas!!)
    }

    fun clip(shape: Shape) {
//        g2.clip(shape);
        // as Graphics2D.clip: the clip so far cut down to the shape (Android no longer has
        // Region.Op.REPLACE, which threw here on every picture with a clip record)
        mCanvas!!.clipPath(getPath(shape))
    }

    var clip: Shape?
        get() =//        return g2.getClip();
            mCurrClip
        set(shape) {
//        g2.setClip(shape);
            mCurrClip = Area(shape!!)
            //mCanvas.clipPath(getPath(shape), Region.Op.REPLACE);
        }

    //    public void drawImage(BufferedImage image, AffineTransform transform)
    //    {
    //        g2.drawImage(image, transform, null);
    //    }
    private fun createShape(arr: IntArray): Shape {
        val s: Shape = GeneralPath()
        var i = 0
        while (i < arr.size) {
            val type = arr[i]
            when (type) {
                -1 ->                 //MOVETO
                    (s as GeneralPath).moveTo(arr[++i].toFloat(), arr[++i].toFloat())

                -2 ->                 //LINETO
                    (s as GeneralPath).lineTo(arr[++i].toFloat(), arr[++i].toFloat())

                -3 ->                 //QUADTO
                    (s as GeneralPath).quadTo(
                        arr[++i].toFloat(), arr[++i].toFloat(), arr[++i].toFloat(),
                        arr[++i].toFloat()
                    )

                -4 ->                 //CUBICTO
                    (s as GeneralPath).curveTo(
                        arr[++i].toFloat(), arr[++i].toFloat(), arr[++i].toFloat(),
                        arr[++i].toFloat(), arr[++i].toFloat(), arr[++i].toFloat()
                    )

                -5 ->                 //CLOSE
                    return s

                else -> {}
            }
            i++
        }
        return s
    }

    fun drawImage(bitmap: Bitmap, transform: AffineTransform) {
//        g2.drawImage(image, transform, null);
        val matrix = Matrix()
        matrix.setValues(createMatrix(transform))
        mCanvas!!.drawBitmap(bitmap, matrix, penPaint)
    }

    //    public void drawImage(BufferedImage image, int x, int y, int width, int height)
    //    {
    //        g2.drawImage(image, x, y, width, height, null);
    //    }
    fun drawImage(bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int) {
//        mCanvas.drawBitmap(bitmap, x, y, penPaint);
//    	mCanvas.drawBitmap(bitmap, x, y, null);
        val dst = Rect(x, y, x + width, y + height)
        mCanvas!!.drawBitmap(bitmap, null, dst, null)
    }

    fun drawShape(shape: Shape) {
//        drawShape(g2, shape);
        drawShape(mCanvas!!, shape)
    }

    fun fillShape(shape: Shape) {
//        fillShape(g2, shape);
        val tmp = brushPaint.getStyle()
        brushPaint.setStyle(Paint.Style.FILL)
        mCanvas!!.drawPath(getPath(shape), brushPaint)
        brushPaint.setStyle(tmp)
    }

    fun fillAndDrawShape(shape: Shape) {
        val tmp = brushPaint.getStyle()
        brushPaint.setStyle(Paint.Style.FILL)
        drawShape(mCanvas!!, shape)
        brushPaint.setStyle(tmp)
    }

    fun fillAndDrawOrAppend(s: Shape) {
//        fillAndDrawOrAppend(g2, s);
        fillAndDrawOrAppend(mCanvas!!, s)
    }

    fun drawOrAppend(s: Shape) {
//        drawOrAppend(g2, s);
        drawOrAppend(mCanvas!!, s)
    }

    fun getInitialClip(): Shape {
        return initialClip!!
    }

    fun setMapModeIsotropic(mapModeIsotropic: Boolean) {
        this.mapModeIsotropic = mapModeIsotropic
    }

    fun setWindowOrigin(windowOrigin: Point?) {
        this.windowOrigin = windowOrigin
        if (windowOrigin != null) {
            mCanvas!!.translate(-windowOrigin.x.toFloat(), -windowOrigin.y.toFloat())
        }
    }

    fun setViewportOrigin(viewportOrigin: Point?) {
        this.viewportOrigin = viewportOrigin
        if (viewportOrigin != null) {
            mCanvas!!.translate(-viewportOrigin.x.toFloat(), -viewportOrigin.y.toFloat())
        }
    }

    fun setViewportSize(viewportSize: Dimension?) {
        this.viewportSize = viewportSize
        fixViewportSize()
        resetTransformation()
    }

    fun setWindowSize(windowSize: Dimension?) {
        this.windowSize = windowSize
        fixViewportSize()
        resetTransformation()
    }

    fun getGDIObject(index: Int): GDIObject? {
        return gdiObjects[index]
    }

    fun storeGDIObject(index: Int, tag: GDIObject?) {
        gdiObjects[index] = tag
    }

    fun setUseCreatePen(useCreatePen: Boolean) {
        this.useCreatePen = useCreatePen
    }

    //    public void setPenPaint(Paint penPaint)
    //    {
    //        this.penPaint = penPaint;
    //    }
    fun setPenPaint(color: Color) {
        penPaint.setColor(color.getRGB())
    }

    //    public void setBrushPaint(Paint brushPaint)
    //    {
    //        this.brushPaint = brushPaint;
    //    }
    fun setBrushPaint(color: Color) {
        brushPaint.setColor(color.getRGB())
    }

    fun setBrushPaint(bitmap: Bitmap?) {
//    	int[] pixels = new int[16 * 16];
//    	bitmap.getPixels(pixels, 0, 16, 0, 0, 16, 16);
//    	brushPaint.setColor(pixels);
//    	brushPaint.setColor(bitmap.getPixel(0, 0));
        mCanvas!!.clipRect(0, 0, 16, 16)
        mCanvas!!.setBitmap(bitmap)
    }

    fun getMeterLimit(): Float {
        return meterLimit.toFloat()
    }

    fun setMeterLimit(meterLimit: Int) {
        this.meterLimit = meterLimit
    }

    fun setTextColor(textColor: Color) {
        this.textColor = textColor
    }

    fun setTextBkColor() {
        setBrushPaint(textColor)
    }

    fun setRop2(rop2: Int) {
        this.rop2 = rop2
    }

    fun setBkMode(bkMode: Int) {
        this.bkMode = bkMode
    }

    fun setScaleMode(scaleMode: Int) {
        this.scaleMode = scaleMode
    }

    companion object {
        private val logger: Logger = Logger.getLogger("com.wxiwei.office.thirdpart.emf")

        /**
         * Each logical unit is mapped to one twentieth of a
         * printer's point (1/1440 inch, also called a twip).
         */
        var TWIP_SCALE: Double = 1.0 / 1440 * 254

        fun createMatrix(Tx: AffineTransform): FloatArray {
            val at = DoubleArray(9)
            Tx.getMatrix(at)
            val f = FloatArray(at.size)
            f[0] = at[0].toFloat()
            f[1] = at[2].toFloat()
            f[2] = at[4].toFloat()
            f[3] = at[1].toFloat()
            f[4] = at[3].toFloat()
            f[5] = at[5].toFloat()
            f[6] = 0f
            f[7] = 0f
            f[8] = 1f
            return f
        }
    }
}

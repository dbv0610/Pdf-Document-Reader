package com.wxiwei.office.common;

import com.wxiwei.office.common.bg.AShader;
import com.wxiwei.office.common.bg.BackgroundAndFill;
import com.wxiwei.office.common.bg.Gradient;
import com.wxiwei.office.common.bg.LinearGradientShader;
import com.wxiwei.office.common.bg.PatternShader;
import com.wxiwei.office.common.bg.TileShader;
import com.wxiwei.office.common.picture.PictureKit;
import com.wxiwei.office.common.pictureefftect.PictureStretchInfo;
import com.wxiwei.office.common.shape.AbstractShape;
import com.wxiwei.office.java.awt.Dimension;
import com.wxiwei.office.pg.animate.IAnimation;
import com.wxiwei.office.pg.control.Presentation;
import com.wxiwei.office.system.IControl;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;

public class BackgroundDrawer
{
	/**
	 * draw picture shape line and background
	 * @param canvas
	 * @param control
	 * @param shape
	 * @param rect
	 * @param zoom
	 */
	public static void drawLineAndFill(Canvas canvas, IControl control, int viewIndex, AbstractShape shape, Rect rect, float zoom)
	{
		if(shape.hasLine())
        {
        	Paint paint = PaintKit.instance().getPaint();
        	paint.setStyle(Style.STROKE);
        	paint.setStrokeWidth(shape.getLine().getLineWidth() * zoom);
        	drawBackground(canvas, control, viewIndex, shape.getLine().getBackgroundAndFill(), rect, null, zoom, paint);
        }

        if(shape.getBackgroundAndFill() != null)
        {
        	drawBackground(canvas, control, viewIndex,
        			shape.getBackgroundAndFill(), rect, null, zoom);
        }
	}

	public static void drawPathBackground(Canvas canvas, IControl control, int viewIndex, BackgroundAndFill fill, Rect rect, IAnimation animation, float zoom,  Path path, Paint paint)
    {
		if(fill == null)
		{
			return;
		}

		canvas.save();

		if(fill.isSlideBackgroundFill() && control != null && control.getView() instanceof Presentation)
    	{
			canvas.clipRect(rect);
			canvas.rotate(0);
    		Dimension d = ((Presentation)control.getView()).getPGModel().getPageSize();
            rect.set(0, 0, (int)(d.width* zoom), (int)(d.height * zoom));
    	}

    	switch (fill.getFillType())
        {
            case BackgroundAndFill.FILL_SOLID:
                paint.setColor(fill.getForegroundColor());
                if(animation != null)
                {
                    int newAlpha = (fill.getForegroundColor() >> 24) & 0xff;
                    newAlpha = (int)(animation.getCurrentAnimationInfor().getAlpha() / 255f * newAlpha);
                    paint.setAlpha(newAlpha);
                }
                canvas.drawPath(path, paint);
                break;

            case BackgroundAndFill.FILL_PICTURE:
                canvas.clipPath(path);
                float x = rect.left;
                float y = rect.top;
                float w = rect.width();
                float h = rect.height();
                PictureStretchInfo stretch = fill.getStretch();
                if(stretch != null)
                {
                	x += stretch.getLeftOffset() * w;
                	y += stretch.getTopOffset() * h;

                	w *= (1 - stretch.getLeftOffset() - stretch.getRightOffset());
                	h *= (1 - stretch.getTopOffset() - stretch.getBottomOffset());
                }
                PictureKit.instance().drawPicture(canvas, control, viewIndex, fill.getPicture(control),
                    x, y, zoom, w, h, null, animation);
                break;
            case BackgroundAndFill.FILL_PATTERN:
            case BackgroundAndFill.FILL_SHADE_LINEAR:
            case BackgroundAndFill.FILL_SHADE_RADIAL:
            case BackgroundAndFill.FILL_SHADE_RECT:
            case BackgroundAndFill.FILL_SHADE_SHAPE:
            case BackgroundAndFill.FILL_SHADE_TILE:
            	drawGradientAndTile(canvas, control, viewIndex, fill, rect, animation, zoom,
					path, paint);
            	break;
            default:
                break;
        }

    	canvas.restore();
    }

	private static void drawGradientAndTile(Canvas canvas, IControl control, int viewIndex,
			BackgroundAndFill fill, Rect rect, IAnimation animation,
			float zoom, Path path, Paint paint)
	{
		if (fill == null || paint == null || rect == null || canvas == null) {
			return;
		}

		AShader aShader = fill.getShader();
		if(aShader != null)
		{
			if(aShader instanceof LinearGradientShader)
			{
				float lineWidth = paint.getStrokeWidth();
				//vertical or horizontal direct line
				if(Math.abs(rect.left - rect.right) <= lineWidth )
				{
					rect.set(Math.round(rect.left - lineWidth / 2),
							 Math.round(rect.top),
							 Math.round(rect.right + lineWidth / 2),
							 Math.round(rect.bottom));
				}
				else if(Math.abs(rect.top - rect.bottom) <= lineWidth)
				{
					rect.set(Math.round(rect.left),
							 Math.round(rect.top - lineWidth / 2),
							 Math.round(rect.right),
							 Math.round(rect.bottom + lineWidth / 2));
				}
			}

			// Clear any existing shader to prevent memory leaks
			paint.setShader(null);

			// For BitmapShader, we need to be extra careful
			if (aShader instanceof TileShader) {
				// TileShader uses BitmapShader internally
				try {
					// Force recreation of the shader to avoid using recycled bitmaps
					float r = 1 / zoom;
					Rect scaledRect = new Rect(
						Math.round(rect.left * r),
						Math.round(rect.top * r),
						Math.round(rect.right * r),
						Math.round(rect.bottom * r)
					);

					// Check if the rect is valid
					if (scaledRect.width() <= 0 || scaledRect.height() <= 0) {
						return;
					}

					// Create a new shader with the scaled rect
					Shader shader = aShader.createShader(control, viewIndex, scaledRect);

					// If shader creation failed, return early
					if (shader == null) {
						return;
					}

					// Set up the matrix for the shader
					Matrix m = new Matrix();
					TileShader tileShader = (TileShader)aShader;
					float offX = rect.left + tileShader.getOffsetX() * zoom;
					float offY = rect.top + tileShader.getOffsetY() * zoom;

					m.postScale(zoom, zoom);
					m.postScale(rect.width() / (float)Gradient.COORDINATE_LENGTH,
								rect.height() / (float)Gradient.COORDINATE_LENGTH);
					m.postTranslate(offX, offY);

					// Apply the shader
					try {
						shader.setLocalMatrix(m);
						paint.setShader(shader);

						int newAlpha = aShader.getAlpha();
						if(animation != null)
						{
							newAlpha = (int)(animation.getCurrentAnimationInfor().getAlpha() / 255f * newAlpha);
						}
						paint.setAlpha(newAlpha);

						if(path != null) {
							canvas.drawPath(path, paint);
						} else {
							canvas.drawRect(rect, paint);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// Always clear the shader when done
						paint.setShader(null);
					}
				} catch (Exception e) {
					e.printStackTrace();
					paint.setShader(null);
				}
				return;
			}

			// For other shader types
			Shader shader = null;
			try {
				shader = aShader.getShader();
				if(shader == null)
				{
					float r = 1 / zoom;
					Rect scaledRect = new Rect(
						Math.round(rect.left * r),
						Math.round(rect.top * r),
						Math.round(rect.right * r),
						Math.round(rect.bottom * r)
					);

					// Check if the rect is valid
					if (scaledRect.width() <= 0 || scaledRect.height() <= 0) {
						return;
					}

					shader = aShader.createShader(control, viewIndex, scaledRect);
				}

				// If we still don't have a valid shader, return early
				if(shader == null)
				{
					return;
				}

				Matrix m = new Matrix();
				float offX = rect.left;
				float offY = rect.top;

				if(aShader instanceof PatternShader)
				{
					// Pattern shader specific code if needed
				}
				else if(aShader instanceof LinearGradientShader)
				{
					LinearGradientShader gradient = (LinearGradientShader)aShader;
					float focusX = 1f;
					float focusY = 1f;

					if(gradient.getAngle() == 90)
					{
						switch(gradient.getFocus())
						{
							case 100:
								focusX = 0f;
								focusY = 0;
								break;
							case 0:
								focusX = 1f;
								break;
							case -50:
								focusX = 0.5f;
								focusY = 0.5f;
								break;
							case 50:
								focusX = -0.5f;
								focusY = -0.5f;
								break;
						}
					}
					else
					{
						switch(gradient.getFocus())
						{
							case 100:
								focusX = 0f;
								focusY = 0;
								break;
							case 0:
								focusX = 1f;
								break;
							case 50:
								focusX = 0.5f;
								focusY = 0.5f;
								break;
							case -50:
								focusX = -0.5f;
								focusY = -0.5f;
								break;
						}
					}

					offX += focusX * rect.width();
					offY +=  focusY * rect.height();
				}

				// Check if the rect dimensions are valid before scaling
				if (rect.width() <= 0 || rect.height() <= 0) {
					return;
				}

				m.postScale(rect.width() / (float)Gradient.COORDINATE_LENGTH,
							rect.height() / (float)Gradient.COORDINATE_LENGTH);
				m.postTranslate(offX, offY);

				try {
					shader.setLocalMatrix(m);
					paint.setShader(shader);

					int newAlpha = aShader.getAlpha();
					if(animation != null)
					{
						newAlpha = (int)(animation.getCurrentAnimationInfor().getAlpha() / 255f * newAlpha);
					}
					paint.setAlpha(newAlpha);

					if(path != null) {
						canvas.drawPath(path, paint);
					} else {
						canvas.drawRect(rect, paint);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					paint.setShader(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				paint.setShader(null);
			}
		}
	}

	/**
	 *
	 * @param canvas
	 * @param control
	 * @param br
	 * @param rect
	 * @param animation
	 * @param zoom
	 * @return
	 */
	public static boolean drawBackground(Canvas canvas, IControl control, int viewIndex, BackgroundAndFill br, Rect rect, IAnimation animation, float zoom)
	{
		return drawBackground(canvas, control, viewIndex, br, rect, animation, zoom, PaintKit.instance().getPaint());
	}

    /**
     * 绘制背景
     * @param canvas
     * @param br
     * @param rect
     * @return
     */
    public static boolean drawBackground(Canvas canvas, IControl control, int viewIndex, BackgroundAndFill br, Rect rect, IAnimation animation, float zoom, Paint paint)
    {
        if (br != null)
        {
        	canvas.save();

        	if(br.isSlideBackgroundFill() && control != null && control.getView() instanceof Presentation)
        	{
        		canvas.clipRect(rect);
    			canvas.rotate(0);

        		Dimension d = ((Presentation)control.getView()).getPGModel().getPageSize();
                rect.set(0, 0, (int)(d.width* zoom), (int)(d.height * zoom));
        	}

            switch (br.getFillType())
            {
                case BackgroundAndFill.FILL_SOLID:
                    int color = paint.getColor();
                    paint.setColor(br.getForegroundColor());
                    if(animation != null)
                    {
                        paint.setAlpha(animation.getCurrentAnimationInfor().getAlpha());
                    }
                    canvas.drawRect(rect, paint);

                    //restore
                    paint.setColor(color);
                    canvas.restore();
                    return true;

                case BackgroundAndFill.FILL_PICTURE:
                	float x = rect.left;
                    float y = rect.top;
                    float w = rect.width();
                    float h = rect.height();
                    PictureStretchInfo stretch = br.getStretch();
                    if(stretch != null)
                    {
                    	x += stretch.getLeftOffset() * w;
                    	y += stretch.getTopOffset() * h;

                    	w *= (1 - stretch.getLeftOffset() - stretch.getRightOffset());
                    	h *= (1 - stretch.getTopOffset() - stretch.getBottomOffset());
                    }
                    PictureKit.instance().drawPicture(canvas, control, viewIndex, br.getPicture(control),
                        x, y, zoom, w, h, null, animation);
                    canvas.restore();
                    return true;
                case BackgroundAndFill.FILL_PATTERN:
                case BackgroundAndFill.FILL_SHADE_LINEAR:
                case BackgroundAndFill.FILL_SHADE_RADIAL:
                case BackgroundAndFill.FILL_SHADE_RECT:
                case BackgroundAndFill.FILL_SHADE_SHAPE:
                case BackgroundAndFill.FILL_SHADE_TILE:
                	drawGradientAndTile(canvas, control, viewIndex, br, rect, animation, zoom, null, paint);
                	canvas.restore();
                	return true;
                default:
                    break;
            }

            canvas.restore();
        }
        return false;
    }
}

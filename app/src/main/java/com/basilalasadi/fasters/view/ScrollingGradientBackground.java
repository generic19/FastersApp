package com.basilalasadi.fasters.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;


/**
 * Dynamic drawable meant to be used as background to a scrolling widget. Use `setScroll()` inside
 * a scroll change listener to update the scroll. This drawable invalidates itself when the scroll
 * is set.
 *
 * As the scroll value increases, the gradient simultaneously scrolls down and shifts gradually
 * from `firstGradient` to `secondGradient`.
 */
class ScrollingGradientBackground extends Drawable {
	private int[] firstGradient;
	private int[] secondGradient;
	private int alpha;
	private ColorFilter colorFilter;
	private int scroll;
	private float gradientExtent;
	private float parallaxFactor;
	
	/**
	 * @param firstGradient  The ARGB colors of the first gradient. firstGradient and secondGradient
	 *                       must have the same length.
	 * @param secondGradient The ARGB colors of the second gradient. firstGradient and
	 *                       secondGradient must have the same length.
	 * @param gradientExtent The extent of the gradient in multiples of scroll length.
	 * @param parallaxFactor Controls how much the gradient moves compared to scroll.
	 * @param alpha          The alpha of the gradient (this alpha value gets blended with the individual
	 *                       alpha values of the gradients.
	 * @param colorFilter    Color filter to apply to the drawable.
	 */
	public ScrollingGradientBackground(int[] firstGradient, int[] secondGradient,
			float gradientExtent, float parallaxFactor, int alpha, ColorFilter colorFilter) {
		
		this.firstGradient = firstGradient.clone();
		this.secondGradient = secondGradient.clone();
		this.gradientExtent = gradientExtent;
		this.parallaxFactor = parallaxFactor;
		this.alpha = alpha;
		this.colorFilter = colorFilter;
		scroll = 0;
	}
	
	/**
	 * @param firstGradient  The ARGB colors of the first gradient. firstGradient and secondGradient
	 *                       must have the same length.
	 * @param secondGradient The ARGB colors of the second gradient. firstGradient and
	 *                       secondGradient must have the same length.
	 * @param gradientExtent The extent of the gradient in multiples of scroll length.
	 * @param parallaxFactor Controls how much the gradient moves compared to scroll.
	 */
	public ScrollingGradientBackground(int[] firstGradient, int[] secondGradient,
			float gradientExtent,float parallaxFactor) {
		
		this(firstGradient, secondGradient, gradientExtent, parallaxFactor, 0xff, null);
	}
	
	/**
	 * Sets the scroll and invalidates this drawable.
	 *
	 * @param scroll Scroll value in pixels.
	 */
	public void setScroll(int scroll) {
		this.scroll = scroll;
		invalidateSelf();
	}
	
	/**
	 * Sets the gradients and invalidates this drawable.
	 *
	 * @param firstGradient  The ARGB colors of the first gradient. firstGradient and secondGradient
	 *                       must have the same length.
	 * @param secondGradient The ARGB colors of the second gradient. firstGradient and
	 *                       secondGradient must have the same length.
	 */
	public void setGradients(int[] firstGradient, int[] secondGradient) {
		this.firstGradient = firstGradient.clone();
		this.secondGradient = secondGradient.clone();
		invalidateSelf();
	}
	
	@Override
	public void draw(@NonNull Canvas canvas) {
		Rect bounds = getBounds();
		
		
		int[] gradientColors = new int[firstGradient.length];
		
		int factor = 256 * (int)(scroll * parallaxFactor) / bounds.height();
		factor = Math.min(256, Math.max(0, factor));
		
		for (int i = 0; i < firstGradient.length; i++) {
			gradientColors[i] = lerpColors(firstGradient[i], secondGradient[i], factor);
		}
		
		if (alpha != 0xFF) {
			int f = alpha * 256 / 255;
			
			for (int i = 0; i < firstGradient.length; i++) {
				gradientColors[i] = (((gradientColors[i] >> 24) * f) << 16) + (gradientColors[i] & 0x00FFFFFF);
			}
		}
		
		Paint paint = new Paint();
		
		int offset = (int) (scroll * parallaxFactor);
		int gradientStart = -offset;
		int gradientEnd = (int) (bounds.height() * gradientExtent) - offset;
		
		LinearGradient gradient = new LinearGradient(0, gradientStart, 0, gradientEnd, gradientColors, null, TileMode.CLAMP);
		paint.setShader(gradient);
		
		paint.setColorFilter(colorFilter);
		
		canvas.drawRect(bounds, paint);
	}
	
	/**
	 * Linearly interpolates between two colors.
	 *
	 * @param color0 First ARGB color.
	 * @param color1 Second ARGB color.
	 * @param factor Blending factor (0-256).
	 * @return Blended ARGB color.
	 */
	private static int lerpColors(int color0, int color1, int factor) {
		int a0, r0, g0, b0;
		int a1, r1, g1, b1;
		
		a0 = (color0 & 0xFF000000) >> 24;
		r0 = (color0 & 0x00FF0000) >> 16;
		g0 = (color0 & 0x0000FF00) >> 8;
		b0 = (color0 & 0x000000FF);
		
		a1 = (color1 & 0xFF000000) >> 24;
		r1 = (color1 & 0x00FF0000) >> 16;
		g1 = (color1 & 0x0000FF00) >> 8;
		b1 = (color1 & 0x000000FF);
		
		int a = (((a1 - a0) * factor) >> 8) + a0;
		int r = (((r1 - r0) * factor) >> 8) + r0;
		int g = (((g1 - g0) * factor) >> 8) + g0;
		int b = (((b1 - b0) * factor) >> 8) + b0;
		
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	@Override
	public void setAlpha(int i) {
		alpha = i;
	}
	
	@Override
	public void setColorFilter(@Nullable ColorFilter colorFilter) {
		this.colorFilter = colorFilter;
	}
	
	@Override
	public int getOpacity() {
		return alpha;
	}
	
	@NonNull
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("ScrollingGradientBackground(firstGradient: { ");
		
		for (int c : firstGradient) {
			buf.append(String.format("#%08X", c));
			buf.append(" ");
		}
		
		buf.append("}, secondGradient: { ");
		
		for (int c : secondGradient) {
			buf.append(String.format("#%08X", c));
			buf.append(" ");
		}
		
		buf.append("})");
		
		return buf.toString();
	}
}

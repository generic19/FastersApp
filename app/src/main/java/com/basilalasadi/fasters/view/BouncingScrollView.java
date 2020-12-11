package com.basilalasadi.fasters.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class BouncingScrollView extends View {
	private static final int MAX_Y_OVERSCROLL_DISTANCE = 200;
	
	private Context mContext;
	private int mMaxYOverscrollDistance;
	
	public BouncingScrollView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	public BouncingScrollView(Context context, AttributeSet attributes) {
		super(context, attributes);
		mContext = context;
		init();
	}
	
	public BouncingScrollView(Context context, AttributeSet attributes, int defaultStyle) {
		super(context, attributes, defaultStyle);
		mContext = context;
		init();
	}
	
	private void init() {
		final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		
		mMaxYOverscrollDistance = (int)(metrics.density * MAX_Y_OVERSCROLL_DISTANCE);
	}
	
	@Override
	protected boolean overScrollBy(
			int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
				maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
	}
}

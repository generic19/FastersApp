package com.basilalasadi.fasters.view;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.basilalasadi.fasters.BuildConfig;
import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;
import com.basilalasadi.fasters.view.AppTheme;
import com.basilalasadi.fasters.view.LocationFragment;
import com.basilalasadi.fasters.view.ScrollingGradientBackground;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
	ScrollingGradientBackground scrollingBackground;
	boolean isLandscape;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		
		SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_preferences_name), 0);
		
		int appThemeOrdinal = prefs.getInt("app_theme", AppTheme.Morning.ordinal());
		AppTheme appTheme = AppTheme.fromOrdinal(appThemeOrdinal);
		
		setAppTheme(appTheme);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		adjustViewsBasedOnTheme(appTheme);
		
		addListeners();
	}
	
	protected void addListeners() {
		final View root = findViewById(isLandscape? R.id.constraintLayoutRoot : R.id.scrollViewRoot);
		final ScrollView scrollView = findViewById(isLandscape? R.id.scrollViewInfo : R.id.scrollViewRoot);
		
		final Button buttonSwitchTheme = (Button) findViewById(R.id.buttonDebugSwitchTheme);
		final Button buttonShowLocationDialog = (Button) findViewById(R.id.buttonDebugShowLocationDialog);
		
		/*
		 * Pre-draw listener. Executes once, then removes itself.
		 */
		root.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				scrollingBackground.setScroll(scrollView.getScrollY());
				
				scrollView.getViewTreeObserver().removeOnPreDrawListener(this);
				return true;
			}
		});
		
		/*
		 * Root ScrollView on scroll change listener. Responsible for updating the scrolling
		 * gradient background.
		 */
		scrollView.setOnScrollChangeListener(
				(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> scrollingBackground.setScroll(scrollY)
		);
		
		/*
		 * "Switch Theme" button on-click listener. Switches AppTheme in shared preferences and
		 * recreates this activity.
		 */
		buttonSwitchTheme.setOnClickListener((View v) -> {
			SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_preferences_name), 0);
			int appThemeIndex = prefs.getInt("app_theme", AppTheme.Morning.ordinal());
			
			SharedPreferences.Editor editor = prefs.edit();
			
			try {
				if (appThemeIndex == AppTheme.Morning.ordinal()) {
					editor.putInt("app_theme", AppTheme.Evening.ordinal());
				} else {
					editor.putInt("app_theme", AppTheme.Morning.ordinal());
				}
			}
			finally {
				editor.apply();
			}
			
			recreate();
		});
		
		
		buttonShowLocationDialog.setOnClickListener((View v) -> {
			Intent intent = new Intent(this, SetLocationActivity.class);
			
			startActivity(intent);
		});
		
		findViewById(R.id.buttonExecuteDebugFunction).setOnClickListener((v) -> {
			Toast.makeText(this, "Add code to run.", Toast.LENGTH_SHORT).show();
		});
	}
	
	/**
	 * Sets the base theme of this activity to the corresponding value of `appTheme`.
	 *
	 * @param appTheme The AppTheme to set.
	 */
	private void setAppTheme(AppTheme appTheme) {
		switch (appTheme) {
			case Evening:
				setTheme(R.style.Theme_App_Evening);
				break;
			
			case Morning:
			default:
				setTheme(R.style.Theme_App_Morning);
		}
	}
	
	/**
	 * Modifies the attributes of this activity's views based on `appTheme`.
	 *
	 * @param appTheme The AppTheme to base the modifications on.
	 */
	private void adjustViewsBasedOnTheme(AppTheme appTheme) {
		final View root = findViewById(isLandscape? R.id.constraintLayoutRoot : R.id.scrollViewRoot);
		
		int[] firstGradient = new int[]{
				getColorAttribute(R.attr.colorGradient1Color1),
				getColorAttribute(R.attr.colorGradient1Color2)
		};
		
		int[] secondGradient = new int[]{
				getColorAttribute(R.attr.colorGradient2Color1),
				getColorAttribute(R.attr.colorGradient2Color2)
		};
		
		final Resources res = getResources();
		
		final float extent = getFloatValue(R.dimen.scrollingGradientExtent);
		final float parallaxFactor = getFloatValue(R.dimen.scrollingGradientParallaxFactor);
		
		Log.d("Res", String.format("extent %.2f parallaxFactor %.2f", extent, parallaxFactor));
		
		scrollingBackground = new ScrollingGradientBackground(firstGradient, secondGradient, extent, parallaxFactor);
		
		root.setBackground(scrollingBackground);
	}
	
	/**
	 * Gets the color attribute with id `resId` and asserts that it's a color value.
	 *
	 * @param resId The resource id of the color attribute.
	 * @return The color int.
	 * @throws AssertionError if the resource id does not belong to a color value.
	 */
	private int getColorAttribute(int resId) throws AssertionError {
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(resId, typedValue, true);
		
		if (BuildConfig.DEBUG &&
			typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
			typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			
			return typedValue.data;
		}
		else {
			throw new AssertionError("Value is not a color.");
		}
	}
	
	/**
	 * Gets a float value with id `resId` and asserts that the value is a float value.
	 *
	 * @param resId The resource id of the float value.
	 * @return The float value.
	 * @throws AssertionError if the resource id does not belong to a float value.
	 */
	private float getFloatValue(int resId) throws AssertionError {
		TypedValue typedValue = new TypedValue();
		getResources().getValue(resId, typedValue, true);
		
		if (BuildConfig.DEBUG &&
				typedValue.type >= TypedValue.TYPE_FLOAT) {
			
			return typedValue.getFloat();
		}
		else {
			throw new AssertionError("Value is not a float.");
		}
	}
}
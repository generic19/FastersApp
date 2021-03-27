package com.basilalasadi.fasters.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.threeten.bp.ZonedDateTime;

import com.basilalasadi.fasters.BuildConfig;
import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.controller.MainActivityController;
import com.basilalasadi.fasters.model.CountdownViewModel;
import com.basilalasadi.fasters.state.ActivityState;
import com.basilalasadi.fasters.state.MainActivityState;
import com.basilalasadi.fasters.view.settings.SettingsActivity;


public class MainActivity extends AppCompatActivity implements MainActivityController {
	private static final String TAG = "MainActivity";
	
	private TextView    tvTitle;
	private TextView    tvLocation;
	private TextView    tvCountdownHours;
	private TextView    tvCountdownMinutes;
	private TextView    tvCountdownSeconds;
	private ProgressBar progressBar;
	private TextView    tvCurrentTime;
	private TextView    tvNextTimingLabel;
	private TextView    tvNextTiming;
	private TextView    tvFajrTiming;
	private TextView    tvDuhrTiming;
	private TextView    tvAsrTiming;
	private TextView    tvMagribTiming;
	private TextView    tvIshaTiming;
	
	private ScrollingGradientBackground scrollingBackground;
	private Handler handler;
	private boolean isLandscape;
	
	private MainActivityState state;
	
	{
		state = (MainActivityState) ActivityState.stateOf(MainActivity.class);
		
		if (state == null) {
			state = new MainActivityState(MainActivity.class);
		}
		
		state.bindController(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MainActivityState.bindApplication(getApplication());
		
		isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int appThemeOrdinal = prefs.getInt("app_theme", AppTheme.Morning.ordinal());
		AppTheme appTheme = AppTheme.fromOrdinal(appThemeOrdinal);
		
		setAppTheme(appTheme);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tvTitle = findViewById(R.id.textViewTitle);
		tvLocation = findViewById(R.id.textViewCountdownLocation);
		tvCountdownHours = findViewById(R.id.textViewCountdownHours);
		tvCountdownMinutes = findViewById(R.id.textViewCountdownMinutes);
		tvCountdownSeconds = findViewById(R.id.textViewCountdownSeconds);
		progressBar = findViewById(R.id.progressBar);
		tvCurrentTime = findViewById(R.id.textViewCurrentTime);
		tvNextTimingLabel = findViewById(R.id.textViewTimeTillNextTimingLabel);
		tvNextTiming = findViewById(R.id.textViewTimeTillNextTiming);
		tvFajrTiming = findViewById(R.id.textViewFajrTiming);
		tvDuhrTiming = findViewById(R.id.textViewDuhrTiming);
		tvAsrTiming = findViewById(R.id.textViewAsrTiming);
		tvMagribTiming = findViewById(R.id.textViewMagribTiming);
		tvIshaTiming = findViewById(R.id.textViewIshaaTiming);
		
		
		handler = new Handler(new HandlerCallback());
		
		adjustViewsBasedOnTheme();
		
		addListeners();
		
		Log.d(TAG, "onCreate: created hours tv " + tvCountdownHours.hashCode() + ".");
	}
	
	@Override
	public Context getCurrentContext() {
		return this;
	}
	
	public void updateView() {
		ZonedDateTime now = ZonedDateTime.now();
		CountdownViewModel viewModel = state.getViewModel();
		
		if (viewModel != null) {
			if (viewModel.isDataAvailable()) {
				tvTitle.setText(viewModel.isEvening ? R.string.time_until_fasting : R.string.time_until_break_fast);
				tvLocation.setText(viewModel.location);
				
				updateCountdown(now);
				
				String[] timings = viewModel.getFormattedTimings();
				String nextTiming = viewModel.getFormattedNextTiming();
				String nextTimingLabel = getString(R.string.time_till) + " " + viewModel.nextPrayer;
				
				tvCurrentTime.setText(CountdownViewModel.getFormattedTime(now));
				tvNextTimingLabel.setText(nextTimingLabel);
				tvNextTiming.setText(nextTiming);
				tvFajrTiming.setText(timings[0]);
				tvDuhrTiming.setText(timings[1]);
				tvAsrTiming.setText(timings[2]);
				tvMagribTiming.setText(timings[3]);
				tvIshaTiming.setText(timings[4]);
			}
			else if (viewModel.isDataLoading()) {
				clearViewData("please wait...");
			}
			else if (viewModel.isError()) {
				switch (viewModel.getError()) {
					case CountdownViewModel.ERROR_NO_LOCATION:
						clearViewData(getString(R.string.error_no_location_message));
						break;
					
					case CountdownViewModel.ERROR_INVALID_SETTINGS:
						clearViewData(getString(R.string.invalid_settings));
						break;
					
					default:
						clearViewData(getString(R.string.error_unknown_message));
				}
			}
			else {
				clearViewData("Unknown state");
			}
		}
		else {
			clearViewData("");
		}
	}
	
	public Handler getHandler() {
		return handler;
	}
	
	protected void clearViewData(String title) {
		progressBar.setProgress(0);
		tvTitle.setText(title);
		tvLocation.setText("");
		tvCountdownHours.setText("00");
		tvCountdownMinutes.setText("00");
		tvCountdownSeconds.setText("00");
		tvCurrentTime.setText("");
		tvNextTimingLabel.setText(getString(R.string.time_till_next_timing));
		tvNextTiming.setText("");
		tvFajrTiming.setText("");
		tvDuhrTiming.setText("");
		tvAsrTiming.setText("");
		tvMagribTiming.setText("");
		tvIshaTiming.setText("");
	}
	
	public void updateCountdown() {
		ZonedDateTime now = ZonedDateTime.now();
		updateCountdown(now);
	}
	
	public void updateCountdown(ZonedDateTime now) {
		CountdownViewModel viewModel = state.getViewModel();
		
		if (viewModel == null || !viewModel.isDataAvailable()) {
			return;
		}
		
		CountdownViewModel.CountdownDisplay countdown = viewModel.getCountdownDisplay(now);
		
		tvCountdownHours.setText(countdown.hours);
		tvCountdownMinutes.setText(countdown.minute);
		tvCountdownSeconds.setText(countdown.second);
		
		progressBar.setProgress((int) (viewModel.countDownProgress * 100));
		
		Log.d(TAG, "updateCountdown: updated hours tv " + tvCountdownHours.hashCode() + ".");
	}
	
	protected void addListeners() {
		final View root = findViewById(isLandscape? R.id.constraintLayoutRoot : R.id.scrollViewRoot);
		final ScrollView scrollView = findViewById(isLandscape? R.id.scrollViewInfo : R.id.scrollViewRoot);
		
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
		findViewById(R.id.tableLayoutQuickInfo).setOnClickListener((View v) -> {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			int appTheme = prefs.getInt("app_theme", AppTheme.THEME_MORNING);
			
			SharedPreferences.Editor editor = prefs.edit();
			
			try {
				if (appTheme == AppTheme.THEME_MORNING) {
					editor.putInt("app_theme", AppTheme.THEME_EVENING);
				}
				else {
					editor.putInt("app_theme", AppTheme.THEME_MORNING);
				}
			}
			finally {
				editor.apply();
			}
			
			getWindow().setWindowAnimations(R.style.Window_Transition_ThemeSwitch);
			
			recreate();
		});
		
		
		findViewById(R.id.buttonSettings).setOnClickListener(view -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			
			startActivity(intent);
		});
		
		
		findViewById(R.id.buttonDebugLaunchActivity).setOnClickListener((View v) -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		});
		
		findViewById(R.id.buttonExecuteDebugFunction).setOnClickListener(
				view -> Toast.makeText(this, "No code to run.", Toast.LENGTH_SHORT).show());
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
	 */
	private void adjustViewsBasedOnTheme() {
		final View root = findViewById(isLandscape? R.id.constraintLayoutRoot : R.id.scrollViewRoot);
		
		int[] firstGradient = new int[]{
				getColorAttribute(R.attr.colorGradient1Color1),
				getColorAttribute(R.attr.colorGradient1Color2)
		};
		
		int[] secondGradient = new int[]{
				getColorAttribute(R.attr.colorGradient2Color1),
				getColorAttribute(R.attr.colorGradient2Color2)
		};
		
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
		
		if (BuildConfig.DEBUG && typedValue.type != TypedValue.TYPE_FLOAT) {
			throw new AssertionError("Value is not a float.");
		}
		else {
			return typedValue.getFloat();
		}
	}
	
	
	private class HandlerCallback implements Handler.Callback {
		@Override
		public boolean handleMessage(@NonNull Message msg) {
			switch (msg.what) {
				case MainActivityController.MSG_UPDATE_COUNTDOWN:
					updateCountdown();
					return true;
				
				case MainActivityController.MSG_UPDATE_VIEW:
					updateView();
					return true;
				
				default:
					return false;
			}
		}
	}
}
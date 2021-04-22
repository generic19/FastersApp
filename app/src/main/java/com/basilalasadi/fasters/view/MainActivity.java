package com.basilalasadi.fasters.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.basilalasadi.fasters.BuildConfig;
import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.controller.MainActivityController;
import com.basilalasadi.fasters.model.CountdownViewModel;
import com.basilalasadi.fasters.provider.SettingsManager;
import com.basilalasadi.fasters.provider.TimeProvider;
import com.basilalasadi.fasters.state.ActivityState;
import com.basilalasadi.fasters.state.MainActivityState;
import com.basilalasadi.fasters.view.settings.SettingsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
	private AppTheme currentApTheme = null;
	
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
		
		updateAppTheme();
		
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
	
	private boolean updateAppTheme() {
		AppTheme appTheme = SettingsManager.getInstance().getTheme(this);
		
		if (appTheme == null) {
			CountdownViewModel viewModel = state.getViewModel();
			
			if (viewModel != null && viewModel.isDataAvailable()) {
				if (viewModel.isEvening) {
					appTheme = AppTheme.Evening;
				}
				else {
					appTheme = AppTheme.Morning;
				}
			}
			else {
				appTheme = AppTheme.Evening;
			}
		}
		
		return setAppTheme(appTheme);
	}
	
	@Override
	public Context getCurrentContext() {
		return this;
	}
	
	public void updateView() {
		ZonedDateTime now = TimeProvider.getDateTime();
		CountdownViewModel viewModel = state.getViewModel();
		
		if (viewModel != null) {
			if (viewModel.isDataAvailable()) {
				tvTitle.setText(viewModel.isEvening ? R.string.time_until_fasting : R.string.time_until_break_fast);
				tvLocation.setText(viewModel.location);
				
				updateCountdown(now);
				
				String[] timings = viewModel.getFormattedTimings();
				String nextTimingLabel = getString(R.string.time_till) + " " + viewModel.nextPrayer;
				
				tvNextTimingLabel.setText(nextTimingLabel);
				tvFajrTiming.setText(timings[0]);
				tvDuhrTiming.setText(timings[1]);
				tvAsrTiming.setText(timings[2]);
				tvMagribTiming.setText(timings[3]);
				tvIshaTiming.setText(timings[4]);
				
				if (updateAppTheme()) {
					recreate();
				}
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
		ZonedDateTime now = TimeProvider.getDateTime();
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
		
		progressBar.setProgress((int) (viewModel.getProgress(now) * 180));
		tvCurrentTime.setText(CountdownViewModel.getFormattedTime(now));
		
		long nextPrayerHours = viewModel.timeTillNextPrayer / 3600;
		long nextPrayerMinute = viewModel.timeTillNextPrayer / 60 - nextPrayerHours * 60;
		
		tvNextTiming.setText(String.format(Locale.US, "%d:%02d", nextPrayerHours, nextPrayerMinute));
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
		
		
		findViewById(R.id.textViewPrayerTimesLabel).setOnClickListener(view -> {
			MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
			builder.setTitle("Set date and time");
			builder.setView(R.layout.dialog_input_datetime);
			
			builder.setPositiveButton("Set", (dialog, which) -> {
				String str = ((EditText) ((AlertDialog)dialog).findViewById(R.id.textbox)).getText().toString();
				
				try {
					Matcher m = Pattern.compile("(?:(\\d{1,2})/(\\d{1,2})/(\\d{2,4}) )?(\\d+):(\\d+) ([ap])m", Pattern.CASE_INSENSITIVE).matcher(str);
					
					if (m.find()) {
						ZonedDateTime dt = ZonedDateTime.now();
						
						if (m.group(1) != null) {
							int month = Integer.parseInt(m.group(1));
							int day = Integer.parseInt(m.group(2));
							int year = Integer.parseInt(m.group(3));
							
							if (year < 100) {
								year += 2000;
							}
							
							dt = dt.withYear(year).withMonth(month).withDayOfMonth(day);
						}
						
						int hour = Integer.parseInt(m.group(4));
						int minute = Integer.parseInt(m.group(5));
						boolean pm = m.group(6).toUpperCase().equals("P");
						
						if (pm) {
							if (hour != 12) {
								hour += 12;
							}
						}
						else {
							if (hour == 12) {
								hour = 0;
							}
						}
						
						dt = dt.withHour(hour).withMinute(minute);
						
						TimeProvider.setDateTime(dt);
						Toast.makeText(this, "Datetime set.", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(this, "Invalid datetime format.", Toast.LENGTH_LONG).show();
					}
				}
				catch (Exception e) {
					Toast.makeText(this, "Invalid datetime format.", Toast.LENGTH_LONG).show();
				}
			});
			
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		});
	}
	
	/**
	 * Sets the base theme of this activity to the corresponding value of `appTheme`.
	 *
	 * @param appTheme The AppTheme to set.
	 * @return true if appTheme changed, false otherwise.
	 */
	private boolean setAppTheme(AppTheme appTheme) {
		if (currentApTheme != appTheme) {
			switch (appTheme) {
				case Evening:
					setTheme(R.style.Theme_App_Evening);
					break;
				
				case Morning:
				default:
					setTheme(R.style.Theme_App_Morning);
			}
			
			currentApTheme = appTheme;
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.edit().putInt("app_theme", appTheme.ordinal()).apply();
			
			return true;
		}
		return false;
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
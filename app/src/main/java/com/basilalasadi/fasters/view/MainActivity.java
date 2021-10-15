package com.basilalasadi.fasters.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.basilalasadi.fasters.BuildConfig;
import com.basilalasadi.fasters.FastersApplication;
import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.controller.MainActivityController;
import com.basilalasadi.fasters.model.CountdownViewModel;
import com.basilalasadi.fasters.logic.ReminderConstants;
import com.basilalasadi.fasters.logic.settings.SettingsManager;
import com.basilalasadi.fasters.logic.TimeProvider;
import com.basilalasadi.fasters.service.ReminderIntent;
import com.basilalasadi.fasters.service.RemindersService;
import com.basilalasadi.fasters.state.ActivityState;
import com.basilalasadi.fasters.state.MainActivityState;
import com.basilalasadi.fasters.view.settings.SettingsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements MainActivityController {
	private static final String TAG = "MainActivity";
	private static final boolean DEBUG_TOOLS = false;
	
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
	
	private AppTheme activityAppTheme = null;
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
		
		// Starts reminders service.
		{
			JobScheduler jobScheduler = getSystemService(JobScheduler.class);
			
			ComponentName remindersService = new ComponentName(this, RemindersService.class);
			
			JobInfo.Builder builder = new JobInfo.Builder(RemindersService.JOB_ID, remindersService);
			builder.setOverrideDeadline(System.currentTimeMillis() + 15 * 1000);
			
			jobScheduler.cancel(RemindersService.JOB_ID);
			jobScheduler.schedule(builder.build());
		}
		
		isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		
		setActivityTheme();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		final View root = findViewById(isLandscape? R.id.constraintLayoutRoot : R.id.scrollViewRoot);
		{
			final int statusBarHeight = (int) Math.ceil(25 * getResources().getDisplayMetrics().density);
			
			final int paddingLeft = root.getPaddingLeft();
			final int paddingTop = root.getPaddingTop();
			final int paddingRight = root.getPaddingRight();
			final int paddingBottom = root.getPaddingBottom();
			
			root.setPadding(paddingLeft, paddingTop + statusBarHeight, paddingRight, paddingBottom);
		}
		
		tvTitle             = findViewById(R.id.textViewTitle);
		tvLocation          = findViewById(R.id.textViewCountdownLocation);
		tvCountdownHours    = findViewById(R.id.textViewCountdownHours);
		tvCountdownMinutes  = findViewById(R.id.textViewCountdownMinutes);
		tvCountdownSeconds  = findViewById(R.id.textViewCountdownSeconds);
		progressBar         = findViewById(R.id.progressBar);
		tvCurrentTime       = findViewById(R.id.textViewCurrentTime);
		tvNextTimingLabel   = findViewById(R.id.textViewTimeTillNextTimingLabel);
		tvNextTiming        = findViewById(R.id.textViewTimeTillNextTiming);
		tvFajrTiming        = findViewById(R.id.textViewFajrTiming);
		tvDuhrTiming        = findViewById(R.id.textViewDuhrTiming);
		tvAsrTiming         = findViewById(R.id.textViewAsrTiming);
		tvMagribTiming      = findViewById(R.id.textViewMagribTiming);
		tvIshaTiming        = findViewById(R.id.textViewIshaaTiming);
		
		adjustViewsBasedOnTheme();
		
		handler = new Handler(new HandlerCallback());
		
		addListeners();
		
		Log.d(TAG, "onCreate: created hours tv " + tvCountdownHours.hashCode() + ".");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (setActivityTheme()) {
			recreate();
		}
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
				String nextTimingLabel = getString(R.string.time_till) + " " + viewModel.nextPrayerName;
				
				tvNextTimingLabel.setText(nextTimingLabel);
				tvFajrTiming.setText(timings[0]);
				tvDuhrTiming.setText(timings[1]);
				tvAsrTiming.setText(timings[2]);
				tvMagribTiming.setText(timings[3]);
				tvIshaTiming.setText(timings[4]);
				
				AppTheme appTheme = viewModel.isEvening ? AppTheme.Evening : AppTheme.Morning;
				
				if (FastersApplication.get(this).updateTheme(appTheme)) {
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
	
	@SuppressLint("SetTextI18n")
	protected void clearViewData(String title) {
		progressBar.setProgress(0);
		tvTitle.setText(title);
		tvLocation.setText(getString(R.string.countdown_location_initial_text));
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
		
		long timeTillNextPrayer = viewModel.getTimeTillNextPrayer(now);
		
		long nextPrayerHours = timeTillNextPrayer / 3600;
		long nextPrayerMinute = timeTillNextPrayer / 60 - nextPrayerHours * 60;
		
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
		
		
		findViewById(R.id.buttonSettings).setOnClickListener(view -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		});
		
		if (DEBUG_TOOLS) {
			findViewById(R.id.textViewPrayerTimesLabel).setOnClickListener(view -> {
				MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
				builder.setTitle("Set date and time");
				builder.setView(R.layout.dialog_input_datetime);
				
				builder.setPositiveButton("Set", (dialog, which) -> {
					try {
						AlertDialog alertDialog = (AlertDialog) Objects.requireNonNull(dialog);
						EditText editInput = Objects.requireNonNull(alertDialog.findViewById(R.id.textbox));
						String str = editInput.getText().toString();
						
						Matcher m = Pattern.compile(
								"(?:(\\d{1,2})/(\\d{1,2})/(\\d{2,4}) )?(\\d+):(\\d+) ([ap])m", Pattern.CASE_INSENSITIVE).matcher(str);
						
						if (m.find()) {
							ZonedDateTime dt = ZonedDateTime.now();
							
							if (m.group(1) != null) {
								int month = Integer.parseInt(Objects.requireNonNull(m.group(1)));
								int day = Integer.parseInt(Objects.requireNonNull(m.group(2)));
								int year = Integer.parseInt(Objects.requireNonNull(m.group(3)));
								
								if (year < 100) {
									year += 2000;
								}
								
								dt = dt.withYear(year).withMonth(month).withDayOfMonth(day);
							}
							
							int hour = Integer.parseInt(Objects.requireNonNull(m.group(4)));
							int minute = Integer.parseInt(Objects.requireNonNull(m.group(5)));
							boolean pm = Objects.requireNonNull(m.group(6)).equalsIgnoreCase("P");
							
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
			
			findViewById(R.id.textViewCurrentTimeLabel).setOnClickListener(view -> {
				Intent intent = new ReminderIntent(this, ReminderConstants.REMINDER_PREFAST_MEAL, null);
				sendBroadcast(intent);
			});
		}
	}
	
	private boolean setActivityTheme() {
		final CountdownViewModel viewModel = state.getViewModel();
		AppTheme appTheme = SettingsManager.getInstance(this).getTheme(this);
		
		if (appTheme == null) {
			if (viewModel != null && viewModel.isDataAvailable() && viewModel.isEvening) {
				appTheme = AppTheme.Evening;
				setTheme(R.style.Theme_App_Evening);
			}
			else {
				appTheme = AppTheme.Morning;
				setTheme(R.style.Theme_App_Morning);
			}
		}
		
		
		switch (appTheme) {
			case Morning:
				setTheme(R.style.Theme_App_Morning);
				break;
			
			case Evening:
				setTheme(R.style.Theme_App_Evening);
				break;
		}
		
		boolean themeChanged = activityAppTheme != appTheme;
		activityAppTheme = appTheme;
		
		return themeChanged;
	}
	
	/**
	 * Modifies the attributes of this activity's views based on `appTheme`.
	 */
	private void adjustViewsBasedOnTheme() {
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
		
		final View root = getWindow().getDecorView().getRootView();
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
		
		if (BuildConfig.DEBUG) {
			boolean isColor = typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT;
			if (!isColor) {
				throw new AssertionError("Value is not a color.");
			}
		}
		return typedValue.data;
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
	
	/**
	 * Handles messages from MainActivityState
	 */
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
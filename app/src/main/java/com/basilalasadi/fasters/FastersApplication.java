package com.basilalasadi.fasters;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.basilalasadi.fasters.database.CitiesDatabase;
import com.basilalasadi.fasters.logic.settings.SettingsChangeListener;
import com.basilalasadi.fasters.logic.settings.SettingsManager;
import com.basilalasadi.fasters.view.AppTheme;


public class FastersApplication extends Application implements SettingsChangeListener {
	
	public static final String TAG = "FastersApplication";
	
	/**
	 * Convenience method for getting <em>FastersApplication</em> instance.
	 * @param activity an activity in a <em>FastersApplication</em>.
	 * @return <em>FastersApplication</em> instance.
	 */
	public static FastersApplication get(AppCompatActivity activity) {
		return (FastersApplication) activity.getApplication();
	}
	
	
	private AppTheme currentAppTheme = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		CitiesDatabase.getInstance(this);
		
		SettingsManager settingsManager = SettingsManager.getInstance(this);
		settingsManager.ensureSettingsInitialized(this);
		settingsManager.addSettingsChangeListener(this);
	}
	
	@Override
	public void onSettingsChnage(SharedPreferences prefs, String key) {
		if (key.equals(getString(R.string.settings_key_theme))) {
			updateTheme(null);
		}
	}
	
	/**
	 * Updates the theme to the corresponding value of <em>appTheme</em>.
	 * @param appTheme the <em>AppTheme</em> according to time of day.
	 * @return <em>true</em> only if the theme has changed as a result of calling this method.
	 */
	public boolean updateTheme(AppTheme appTheme) {
		Log.d(TAG, "updateTheme: called with " + appTheme + ".");
		
		final AppTheme appThemeSetting = SettingsManager.getInstance(this).getTheme(this);
		Log.d(TAG, "updateTheme: theme setting is " + appThemeSetting + ".");
		Log.d(TAG, "updateTheme: current theme is " + currentAppTheme + ".");
		
		if (appThemeSetting == null && appTheme != null && currentAppTheme != appTheme) {
			switch (appTheme) {
				case Evening:
					setTheme(R.style.Theme_App_Evening);
					break;
					
				case Morning:
					setTheme(R.style.Theme_App_Morning);
					break;
			}
			
			Log.d(TAG, "updateTheme: theme changed to " + appTheme + ".");
			currentAppTheme = appTheme;
			return true;
		}
		else if (appThemeSetting != null && appTheme == null && currentAppTheme != appThemeSetting) {
			switch (appThemeSetting) {
				case Evening:
					setTheme(R.style.Theme_App_Evening);
					break;
				
				case Morning:
					setTheme(R.style.Theme_App_Morning);
					break;
			}
			
			Log.d(TAG, "updateTheme: theme changed to " + appThemeSetting + ".");
			currentAppTheme = appThemeSetting;
			return true;
		}
		else {
			Log.d(TAG, "updateTheme: theme unchanged.");
			return false;
		}
	}
}

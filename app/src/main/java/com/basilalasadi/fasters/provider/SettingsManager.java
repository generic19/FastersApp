package com.basilalasadi.fasters.provider;

import android.content.Context;
import android.content.SharedPreferences;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;


public final class SettingsManager {
	public static final String ADMIN_CITY_SEPARATOR = "\u200b, ";
	
	private static final SettingsManager instance = new SettingsManager();
	
	static SettingsManager getInstance() {
		return instance;
	}
	
	private SettingsManager() {
	
	}
	
	public synchronized void initializeSettingsWithDefaults(Context context) {
		SharedPreferences prefs =
				context.getSharedPreferences(context.getString(R.string.shared_preferences_name), 0);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.remove(context.getString(R.string.settings_key_country));
		editor.remove(context.getString(R.string.settings_key_city));
		editor.remove(context.getString(R.string.settings_key_longitude));
		editor.remove(context.getString(R.string.settings_key_latitude));
		
		editor.putString(context.getString(R.string.settings_key_calculation_method),      context.getString(R.string.calculation_method_value_automatic));
		editor.putString(context.getString(R.string.settings_key_isha_calculation_method), context.getString(R.string.isha_calculation_method_value_sun_angle));
		editor.putString(context.getString(R.string.settings_key_theme),                   context.getString(R.string.theme_value_automatic));
		
		editor.putLong(context.getString(R.string.settings_key_fajr_sun_angle),           doubleToLong(15));
		editor.putLong(context.getString(R.string.settings_key_isha_sun_angle),           doubleToLong(19));
		editor.putLong(context.getString(R.string.settings_key_isha_time_offset),         doubleToLong(90));
		editor.putLong(context.getString(R.string.settings_key_ramadan_isha_time_offset), doubleToLong(120));
		
		editor.putBoolean(context.getString(R.string.settings_key_shafai_method),              false);
		editor.putBoolean(context.getString(R.string.settings_key_use_ramadan_offset),         false);
		editor.putBoolean(context.getString(R.string.settings_key_all_notifications),          true);
		editor.putBoolean(context.getString(R.string.settings_key_prefast_meal_reminder),      true);
		editor.putBoolean(context.getString(R.string.settings_key_water_reminder),             true);
		editor.putBoolean(context.getString(R.string.settings_key_prepare_breakfast_reminder), true);
		editor.putBoolean(context.getString(R.string.settings_key_breakfast_near_reminder),    true);
		
		editor.putBoolean(context.getString(R.string.settings_key_initialized), true);
		
		editor.apply();
	}
	
	public synchronized void ensureSettingsInitialized(Context context) {
		SharedPreferences prefs =
				context.getSharedPreferences(context.getString(R.string.shared_preferences_name), 0);
		
		if (!prefs.getBoolean(context.getString(R.string.settings_key_initialized), false)) {
			initializeSettingsWithDefaults(context);
		}
	}
	
	public synchronized String[] getLocation(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		String country = prefs.getString(context.getString(R.string.settings_key_country), null);
		String city = prefs.getString(context.getString(R.string.settings_key_city), null);
		
		if (country == null || city == null) {
			return null;
		}
		else {
			return new String[]{ country, city };
		}
	}
	
	private synchronized void setAddress(Context context, String country, String adminCity) {
		SharedPreferences prefs = getPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString(context.getString(R.string.settings_key_country), country);
		editor.putString(context.getString(R.string.settings_key_city), adminCity);
		
		editor.apply();
	}
	
	private synchronized void setCoordinates(Context context, double longitude, double latitude) {
		SharedPreferences prefs = getPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putLong(context.getString(R.string.settings_key_longitude), doubleToLong(longitude));
		editor.putLong(context.getString(R.string.settings_key_latitude), doubleToLong(latitude));
		
		editor.apply();
	}
	
	public synchronized boolean setLocation(Context context, String country, String adminCity) {
		CitiesDatabase citiesDb = CitiesDatabase.getInstance(context);
		
		String[] adminAndCity = adminCity.split(ADMIN_CITY_SEPARATOR);
		
		if (adminAndCity.length != 2) {
			return false;
		}
		
		String admin = adminAndCity[0];
		String city = adminAndCity[1];
		
		CitiesDatabase.CityLocation location = citiesDb.getAdminCityLocation(country, admin, city);
		
		if (location == null) {
			return false;
		}
		
		setAddress(context, country, adminCity);
		setCoordinates(context, location.longitude, location.latitude);
		
		return true;
	}
	
	public synchronized CustomMethod getCustomMethod(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		if (prefs.getString(context.getString(R.string.settings_key_calculation_method), "automatic").equals("automatic")) {
			return new CustomMethod();
		}
		
		String fajrAngleString = prefs.getString(context.getString(R.string.settings_key_fajr_sun_angle), null);
		
		if (fajrAngleString == null) {
			return null;
		}
		
		double fajrAngle = Double.parseDouble(fajrAngleString);
		
		boolean useShafaiMethod = prefs.getBoolean(context.getString(R.string.settings_key_shafai_method), false);
		
		boolean useFixedOffset = prefs.getString(context.getString(R.string.settings_key_isha_calculation_method), "sun_angle").equals("fixed_offset");
		
		if (useFixedOffset) {
			String timeOffsetString = prefs.getString(context.getString(R.string.settings_key_isha_time_offset), null);
			
			if (timeOffsetString == null) {
				return null;
			}
			
			int timeOffset = Integer.parseInt(timeOffsetString);
			
			if (prefs.getBoolean(context.getString(R.string.settings_key_use_ramadan_offset), false)) {
				String ramadanTimeOffsetString = prefs.getString(context.getString(R.string.settings_key_ramadan_isha_time_offset), null);
				
				if (ramadanTimeOffsetString == null) {
					return null;
				}
				
				int ramadanTimeOffset = Integer.parseInt(ramadanTimeOffsetString);
				
				return new CustomMethod(fajrAngle, timeOffset, ramadanTimeOffset, useShafaiMethod);
			}
			else {
				return new CustomMethod(fajrAngle, timeOffset, useShafaiMethod);
			}
		}
		else {
			String ishaAngleString = prefs.getString(context.getString(R.string.settings_key_fajr_sun_angle), null);
			
			if (ishaAngleString == null) {
				return null;
			}
			
			double ishaAngle = Double.parseDouble(ishaAngleString);
			
			return new CustomMethod(fajrAngle, ishaAngle, useShafaiMethod);
		}
	}
	
	private SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(context.getString(R.string.shared_preferences_name), 0);
	}
	
	private double doubleFromLong(long bits) {
		return Double.longBitsToDouble(bits);
	}
	
	private long doubleToLong(double val) {
		return Double.doubleToLongBits(val);
	}
	
	
	public static class CustomMethod {
		public final double fajrAngle;
		public final boolean useShafaiMethod;
		public final double ishaAngle;
		public final int fixedTimeOffsetForIsha;
		public final int ramadanFixedTimeOffsetForIsha;
		
		public CustomMethod() {
			this.fajrAngle = Double.NaN;
			this.useShafaiMethod = false;
			this.ishaAngle = Double.NaN;
			this.fixedTimeOffsetForIsha = -1;
			this.ramadanFixedTimeOffsetForIsha = -1;
		}
		
		public CustomMethod(double fajrAngle, double ishaAngle, boolean useShafaiMethod) {
			this.fajrAngle = fajrAngle;
			this.useShafaiMethod = useShafaiMethod;
			this.ishaAngle = ishaAngle;
			this.fixedTimeOffsetForIsha = -1;
			this.ramadanFixedTimeOffsetForIsha = -1;
		}
		
		public CustomMethod(double fajrAngle, int ishaTimeOffset, boolean useShafaiMethod) {
			this.fajrAngle = fajrAngle;
			this.useShafaiMethod = useShafaiMethod;
			this.ishaAngle = Double.NaN;
			this.fixedTimeOffsetForIsha = ishaTimeOffset;
			this.ramadanFixedTimeOffsetForIsha = -1;
		}
		
		public CustomMethod(double fajrAngle, int ishaTimeOffset, int ramadanIshaTimeOffset, boolean useShafaiMethod) {
			this.fajrAngle = fajrAngle;
			this.useShafaiMethod = useShafaiMethod;
			this.ishaAngle = Double.NaN;
			this.fixedTimeOffsetForIsha = ishaTimeOffset;
			this.ramadanFixedTimeOffsetForIsha = ramadanIshaTimeOffset;
		}
		
		public boolean useAutomatic() {
			return Double.isNaN(fajrAngle);
		}
		
		public boolean useFixedTimeOffset() {
			return fixedTimeOffsetForIsha != -1;
		}
		
		public boolean useRamadanFixedTimeOffset() {
			return ramadanFixedTimeOffsetForIsha != -1;
		}
	}
}

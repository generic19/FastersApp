package com.basilalasadi.fasters.provider;

import android.content.Context;
import android.content.SharedPreferences;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;


public final class SettingsManager {
	
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_CITY = "city";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_CALCULATION_METHOD = "calculation_method";
	public static final String KEY_FAJR_SUN_ANGLE = "fajr_sun_angle";
	public static final String KEY_SHAFAI_METHOD = "shafai_method";
	public static final String KEY_ISHA_CALCULATION_METHOD = "isha_calculation_method";
	public static final String KEY_ISHA_TIME_OFFSET = "isha_time_offset";
	public static final String KEY_USE_RAMADAN_OFFSET = "use_ramadan_offset";
	public static final String KEY_RAMADAN_ISHA_TIME_OFFSET = "ramadan_isha_time_offset";
	
	public static final String ADMIN_CITY_SEPARATOR = "\u200b, ";
	
	private static final SettingsManager instance = new SettingsManager();
	
	static SettingsManager getInstance() {
		return instance;
	}
	
	private SettingsManager() {
	
	}
	
	public synchronized String[] getLocation(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		String country = prefs.getString(KEY_COUNTRY, null);
		String city = prefs.getString(KEY_CITY, null);
		
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
		
		editor.putString(KEY_COUNTRY, country);
		editor.putString(KEY_CITY, adminCity);
		
		editor.apply();
	}
	
	private synchronized void setCoordinates(Context context, double longitude, double latitude) {
		SharedPreferences prefs = getPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putLong(KEY_LONGITUDE, doubleToLong(longitude));
		editor.putLong(KEY_LATITUDE, doubleToLong(latitude));
		
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
		
		if (prefs.getString(KEY_CALCULATION_METHOD, "automatic").equals("automatic")) {
			return new CustomMethod();
		}
		
		String fajrAngleString = prefs.getString(KEY_FAJR_SUN_ANGLE, null);
		
		if (fajrAngleString == null) {
			return null;
		}
		
		double fajrAngle = Double.parseDouble(fajrAngleString);
		
		boolean useShafaiMethod = prefs.getBoolean(KEY_SHAFAI_METHOD, false);
		
		boolean useFixedOffset = prefs.getString(KEY_ISHA_CALCULATION_METHOD, "sun_angle").equals("fixed_offset");
		
		if (useFixedOffset) {
			String timeOffsetString = prefs.getString(KEY_ISHA_TIME_OFFSET, null);
			
			if (timeOffsetString == null) {
				return null;
			}
			
			int timeOffset = Integer.parseInt(timeOffsetString);
			
			if (prefs.getBoolean(KEY_USE_RAMADAN_OFFSET, false)) {
				String ramadanTimeOffsetString = prefs.getString(KEY_RAMADAN_ISHA_TIME_OFFSET, null);
				
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
			String ishaAngleString = prefs.getString(KEY_FAJR_SUN_ANGLE, null);
			
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

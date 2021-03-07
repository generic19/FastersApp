package com.basilalasadi.fasters.provider;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;


/**
 * Singleton class that interfaces app's shared prefernces.
 */
public final class SettingsManager {
	public static final String CITY_ADMIN_SEPARATOR = "\u200b, ";
	
	private static final SettingsManager instance = new SettingsManager();
	
	public static SettingsManager getInstance() {
		return instance;
	}
	
	private SettingsManager() {
	
	}
	
	/**
	 * Initializes setttings with default values.
	 * @param context The current context.
	 */
	public synchronized void initializeSettingsWithDefaults(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.remove(context.getString(R.string.settings_key_country));
		editor.remove(context.getString(R.string.settings_key_city));
		editor.remove(context.getString(R.string.settings_key_longitude));
		editor.remove(context.getString(R.string.settings_key_latitude));
		
		editor.putString(context.getString(R.string.settings_key_calculation_method),      context.getString(R.string.calculation_method_value_automatic));
		editor.putString(context.getString(R.string.settings_key_isha_calculation_method), context.getString(R.string.isha_calculation_method_value_sun_angle));
		editor.putString(context.getString(R.string.settings_key_theme),                   context.getString(R.string.theme_value_automatic));
		
		editor.putString(context.getString(R.string.settings_key_fajr_sun_angle),           "15");
		editor.putString(context.getString(R.string.settings_key_isha_sun_angle),           "19");
		editor.putString(context.getString(R.string.settings_key_isha_time_offset),         "90");
		editor.putString(context.getString(R.string.settings_key_ramadan_isha_time_offset), "120");
		
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
	
	/**
	 * Checks if settings are initialized; if not, initializeSettingsWithDefaults() will be called.
	 * @param context The current context.
	 */
	public synchronized void ensureSettingsInitialized(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		if (!prefs.getBoolean(context.getString(R.string.settings_key_initialized), false)) {
			initializeSettingsWithDefaults(context);
		}
	}
	
	/**
	 * Gets location set in the settings.
	 * @param context The current context.
	 * @return address of the city.
	 */
	public synchronized Address getAddress(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		String country = prefs.getString(context.getString(R.string.settings_key_country), null);
		String cityAdmin = prefs.getString(context.getString(R.string.settings_key_city), null);
		
		if (country == null || cityAdmin == null) {
			return null;
		}
		
		String[] cityAdminPair = cityAdmin.split(CITY_ADMIN_SEPARATOR);
		
		if (cityAdminPair.length != 2) {
			return null;
		}
		
		String city = cityAdminPair[0];
		String admin = cityAdminPair[1];
		
		return new Address(country, admin, city);
	}
	
	
	/**
	 * Sets the address of the city.
	 * @param context The current context.
	 * @param address The address of the city.
	 */
	private synchronized void setAddress(Context context, Address address) {
		SharedPreferences prefs = getPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		String cityAdmin = address.city + CITY_ADMIN_SEPARATOR + address.admin;
		
		editor.putString(context.getString(R.string.settings_key_country), address.country);
		editor.putString(context.getString(R.string.settings_key_city), cityAdmin);
		
		editor.apply();
	}
	
	/**
	 * Gets coordinates in settings.
	 * @param context The current context.
	 * @return the coordinates.
	 */
	private synchronized Coordinates getCoordinates(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		long longitudeBits = prefs.getLong(context.getString(R.string.settings_key_longitude), -1);
		long latitudeBits = prefs.getLong(context.getString(R.string.settings_key_latitude), -1);
		
		if (longitudeBits == -1 || latitudeBits == -1) {
			return null;
		}
		
		return new Coordinates(doubleFromLong(longitudeBits), doubleFromLong(latitudeBits));
	}
	
	/**
	 * Sets location coordinates.
	 * @param context The current context.
	 * @param coordinates Location coordinates.
	 */
	private synchronized void setCoordinates(Context context, Coordinates coordinates) {
		SharedPreferences prefs = getPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putLong(context.getString(R.string.settings_key_longitude), doubleToLong(coordinates.longitude));
		editor.putLong(context.getString(R.string.settings_key_latitude), doubleToLong(coordinates.latitude));
		
		editor.apply();
	}
	
	/**
	 * Sets the address, and sets the location coordinates corresponding to the address.
	 * @param context The corrent context.
	 * @param address The address of the city.
	 * @return true on success, false otherwise.
	 */
	public synchronized boolean setLocation(Context context, Address address) {
		CitiesDatabase citiesDb = CitiesDatabase.getInstance(context);
		
		CitiesDatabase.CityLocation location =
				citiesDb.getAdminCityLocation(address.country, address.admin, address.city);
		
		if (location == null) {
			return false;
		}
		
		setAddress(context, address);
		setCoordinates(context, new Coordinates(location));
		
		return true;
	}
	
	/**
	 * Gets custom method from settings.
	 * @param context The current context.
	 * @return the custom method.
	 */
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
	
	/**
	 * Utility function for getting shared preferences instance.
	 * @param context The current context.
	 * @return shared preferences instance.
	 */
	private SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 * Gets double value from bits.
	 * @param bits double bits stored as long.
	 * @return double value.
	 */
	private double doubleFromLong(long bits) {
		return Double.longBitsToDouble(bits);
	}
	
	/**
	 * Gets bits of double value, stored as long.
	 * @param val double value.
	 * @return Bits of double value, stored in long.
	 */
	private long doubleToLong(double val) {
		return Double.doubleToLongBits(val);
	}
	
	
	/**
	 * Container class for city address.
	 */
	public static class Address {
		public final String country;
		public final String admin;
		public final String city;
		
		/**
		 * Container class for city address.
		 * @param country Country of the city.
		 * @param admin Administrative division of the city.
		 * @param city The city.
		 */
		public Address(String country, String admin, String city) {
			this.country = country;
			this.admin = admin;
			this.city = city;
		}
	}
	
	/**
	 * Container class for city coordinates.
	 */
	public static class Coordinates {
		public final double longitude;
		public final double latitude;
		
		/**
		 * Container class for city coordinates.
		 * @param longitude The longitude.
		 * @param latitude The latitude.
		 */
		public Coordinates(double longitude, double latitude) {
			this.longitude = longitude;
			this.latitude = latitude;
		}
		
		/**
		 * Container class for city coordinates.
		 * @param location The location returned from CitiesDatabase.
		 */
		public Coordinates(CitiesDatabase.CityLocation location) {
			this.longitude = location.longitude;
			this.latitude = location.latitude;
		}
	}
	
	
	/**
	 * Class containing all necessary values to calculate prayer times.
	 */
	public static class CustomMethod {
		public final double fajrAngle;
		public final boolean useShafaiMethod;
		public final double ishaAngle;
		public final int fixedTimeOffsetForIsha;
		public final int ramadanFixedTimeOffsetForIsha;
		
		/**
		 * Class containing all necessary values to calculate prayer times. Use this constructor
		 * to indicate that automatic values should be used.
		 */
		public CustomMethod() {
			this.fajrAngle = Double.NaN;
			this.useShafaiMethod = false;
			this.ishaAngle = Double.NaN;
			this.fixedTimeOffsetForIsha = -1;
			this.ramadanFixedTimeOffsetForIsha = -1;
		}
		
		/**
		 * Class containing all necessary values to calculate prayer times.
		 * @param fajrAngle
		 * @param ishaAngle
		 * @param useShafaiMethod
		 */
		public CustomMethod(double fajrAngle, double ishaAngle, boolean useShafaiMethod) {
			this.fajrAngle = fajrAngle;
			this.useShafaiMethod = useShafaiMethod;
			this.ishaAngle = ishaAngle;
			this.fixedTimeOffsetForIsha = -1;
			this.ramadanFixedTimeOffsetForIsha = -1;
		}
		
		/**
		 * Class containing all necessary values to calculate prayer times.
		 * @param fajrAngle
		 * @param ishaTimeOffset
		 * @param useShafaiMethod
		 */
		public CustomMethod(double fajrAngle, int ishaTimeOffset, boolean useShafaiMethod) {
			this.fajrAngle = fajrAngle;
			this.useShafaiMethod = useShafaiMethod;
			this.ishaAngle = Double.NaN;
			this.fixedTimeOffsetForIsha = ishaTimeOffset;
			this.ramadanFixedTimeOffsetForIsha = -1;
		}
		
		/**
		 * Class containing all necessary values to calculate prayer times.
		 * @param fajrAngle
		 * @param ishaTimeOffset
		 * @param ramadanIshaTimeOffset
		 * @param useShafaiMethod
		 */
		public CustomMethod(double fajrAngle, int ishaTimeOffset, int ramadanIshaTimeOffset, boolean useShafaiMethod) {
			this.fajrAngle = fajrAngle;
			this.useShafaiMethod = useShafaiMethod;
			this.ishaAngle = Double.NaN;
			this.fixedTimeOffsetForIsha = ishaTimeOffset;
			this.ramadanFixedTimeOffsetForIsha = ramadanIshaTimeOffset;
		}
		
		/**
		 * Checks whether automatic values should be used.
		 * @return true if automatic values should be used.
		 */
		public boolean useAutomatic() {
			return Double.isNaN(fajrAngle);
		}
		
		/**
		 * Checks whether fixed time offset for isha should be used.
		 * @return true if fixed time offset for isha should be used.
		 */
		public boolean useFixedTimeOffset() {
			return fixedTimeOffsetForIsha != -1;
		}
		
		/**
		 * Checks whether fixed time offset for isha in Ramadan should be used.
		 * @return true if fixed time offset for isha in Ramadan should be used.
		 */
		public boolean useRamadanFixedTimeOffset() {
			return ramadanFixedTimeOffsetForIsha != -1;
		}
	}
}

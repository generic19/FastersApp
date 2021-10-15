package com.basilalasadi.fasters.logic.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;
import com.basilalasadi.fasters.util.WeakSet;
import com.basilalasadi.fasters.view.AppTheme;

import static com.basilalasadi.fasters.logic.ReminderConstants.*;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Set;


/**
 * Singleton class that interfaces app's shared prefernces.
 */
public final class SettingsManager implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	public static final String TAG                   = "SettingsManager";
	public static final String CITY_ADMIN_SEPARATOR  = ", ";
	
	private static SettingsManager instance;
	private final Set<SettingsChangeListener> listeners = new WeakSet<>();
	private final HashMap<String, WeakSet<ValueListeners.ValueListener>> valueListeners = new HashMap<>();
	private WeakReference<SharedPreferences> lastPreference = new WeakReference<>(null);
	
	
	public static SettingsManager getInstance(Context context) {
		if (instance == null) {
			instance = new SettingsManager(context);
		}
		return instance;
	}
	
	private SettingsManager(Context context) {
		getPrefs(context);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		SharedPreferences lastPref = lastPreference.get();
		if (lastPref != null) {
			lastPref.unregisterOnSharedPreferenceChangeListener(this);
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		for (SettingsChangeListener listener : listeners) {
			if (listener != null) {
				listener.onSettingsChnage(prefs, key);
			}
		}
		
		WeakSet<ValueListeners.ValueListener> set = this.valueListeners.get(key);
		
		if (set != null) {
			for (ValueListeners.ValueListener listener : set) {
				if (listener != null) {
					listener.onChange(prefs, key);
				}
			}
		}
	}
	
	/**
	 * Add a listener to be notified when a setting is changed.
	 * @param listener listener
	 */
	public void addSettingsChangeListener(SettingsChangeListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes specified listener.
	 * @param listener listener to remove
	 */
	public void removeSettingsChangeListener(SettingsChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Adds a listener to be notified when the setting with <em>key</em> is changed.
	 * @param key setting key to watch.
	 * @param listener implementation of one of the interfaces provided in <em>ValueListeners</em>.
	 */
	public void addSettingsValueListener(String key, ValueListeners.ValueListener listener) {
		WeakSet<ValueListeners.ValueListener> set = valueListeners.get(key);
		
		if (set == null) {
			set = new WeakSet<>();
			valueListeners.put(key, set);
		}
		
		set.add(listener);
	}
	
	/**
	 * Removes specified value listener from key.
	 * @param key setting key
	 * @param listener the listener to remove
	 */
	public void removeSettingsValueListener(String key, ValueListeners.ValueListener listener) {
		WeakSet<ValueListeners.ValueListener> set = valueListeners.get(key);
		
		if (set != null) {
			set.remove(listener);
		}
	}
	
	/**
	 * Initializes setttings with default values.
	 * @param context The current context.
	 */
	public synchronized void initializeSettingsWithDefaults(Context context) {
		Log.d(TAG, "initializeSettingsWithDefaults: called.");
		
		SharedPreferences prefs = getPrefs(context);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.remove(context.getString(R.string.settings_key_country))
		      .remove(context.getString(R.string.settings_key_city))
		      .remove(context.getString(R.string.settings_key_longitude))
		      .remove(context.getString(R.string.settings_key_latitude));
		
		editor.putString(context.getString(R.string.settings_key_calculation_method), context.getString(R.string.calculation_method_value_automatic))
		      .putString(context.getString(R.string.settings_key_isha_calculation_method), context.getString(R.string.isha_calculation_method_value_sun_angle))
		      .putString(context.getString(R.string.settings_key_theme), context.getString(R.string.theme_value_automatic));
		
		editor.putString(context.getString(R.string.settings_key_fajr_sun_angle), "15")
		      .putString(context.getString(R.string.settings_key_isha_sun_angle), "19")
		      .putString(context.getString(R.string.settings_key_isha_time_offset), "90")
		      .putString(context.getString(R.string.settings_key_ramadan_isha_time_offset), "120");
		
		editor.putBoolean(context.getString(R.string.settings_key_shafai_method), false)
		      .putBoolean(context.getString(R.string.settings_key_use_ramadan_offset), false)
		      .putBoolean(context.getString(R.string.settings_key_all_notifications), true)
		      .putBoolean(context.getString(R.string.settings_key_prefast_meal_reminder), true)
		      .putBoolean(context.getString(R.string.settings_key_water_reminder), true)
		      .putBoolean(context.getString(R.string.settings_key_prepare_breakfast_reminder), true)
		      .putBoolean(context.getString(R.string.settings_key_breakfast_near_reminder), true);
		
		editor.putBoolean(context.getString(R.string.settings_key_initialized), true);
		
		editor.apply();
	}
	
	/**
	 * Checks if settings are initialized; if not, initializeSettingsWithDefaults() will be called.
	 * @param context The current context.
	 */
	public synchronized void ensureSettingsInitialized(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		boolean isInitialized = prefs.getBoolean(context.getString(R.string.settings_key_initialized), false);
		
		if (!isInitialized) {
			initializeSettingsWithDefaults(context);
		}
	}
	
	/**
	 * Gets theme setting.
	 * @param context current context.
	 * @return AppTheme corresponding to setting, or null if set to automatic.
	 */
	public synchronized AppTheme getTheme(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		String setting = prefs.getString(context.getString(R.string.settings_key_theme), null);
		
		if (setting.equals(context.getString(R.string.theme_value_dawn))) {
			return AppTheme.Morning;
		}
		else if (setting.equals(context.getString(R.string.theme_value_dusk))) {
			return AppTheme.Evening;
		}
		else {
			return null;
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
	public synchronized Coordinates getCoordinates(Context context) {
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
	 *
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
	 * Sets coordinates and closest address to the coordinates.
	 *
	 * @param context Current context.
	 * @param coordinates The coordinates.
	 * @return true on success, false otherwise.
	 */
	public synchronized boolean setLocation(Context context, Coordinates coordinates) {
		CitiesDatabase citiesDb = CitiesDatabase.getInstance(context);
		CitiesDatabase.CountryAdminCity addr = citiesDb.findClosestCountryAdminCity(coordinates.longitude, coordinates.latitude);
		
		if (addr == null) {
			return false;
		}
		
		setAddress(context, new Address(addr.country, addr.admin, addr.city));
		setCoordinates(context, coordinates);
		
		return true;
	}
	
	public synchronized void clearLocation(Context context) {
		getPrefs(context).edit()
				.remove(context.getString(R.string.settings_key_country))
				.remove(context.getString(R.string.settings_key_city))
				.remove(context.getString(R.string.settings_key_longitude))
				.remove(context.getString(R.string.settings_key_latitude))
				.apply();
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ensureRegistered(prefs);
		return prefs;
	}
	
	/**
	 * Ensures that the preference change listener is registered in the latest SharedPreferences instance,
	 * and unregisters the listener from the outdated SharedPreferences instance.
	 * @param prefs fresh SharedPreferences instance.
	 */
	private void ensureRegistered(SharedPreferences prefs) {
		SharedPreferences oldPrefs = lastPreference.get();
		
		if (oldPrefs != null) {
			if (prefs.hashCode() != oldPrefs.hashCode()) {
				oldPrefs.unregisterOnSharedPreferenceChangeListener(this);
				prefs.registerOnSharedPreferenceChangeListener(this);
				lastPreference = new WeakReference<>(prefs);
			}
		}
		else {
			prefs.registerOnSharedPreferenceChangeListener(this);
			lastPreference = new WeakReference<>(prefs);
		}
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
	
	public int getEnabledReminders(Context context) {
		SharedPreferences prefs = getPrefs(context);
		
		if (prefs.getBoolean(context.getString(R.string.settings_key_all_notifications), false)) {
			return 0;
		}
		
		int flags = 0;
		
		if (prefs.getBoolean(context.getString(R.string.settings_key_prefast_meal_reminder), false)) {
			flags |= 1 << REMINDER_PREFAST_MEAL;
		}
		if (prefs.getBoolean(context.getString(R.string.settings_key_water_reminder), false)) {
			flags |= 1 << REMINDER_WATER;
		}
		if (prefs.getBoolean(context.getString(R.string.settings_key_prepare_breakfast_reminder), false)) {
			flags |= 1 << REMINDER_PREPARE_BREAKFAST;
		}
		if (prefs.getBoolean(context.getString(R.string.settings_key_breakfast_near_reminder), false)) {
			flags |= 1 << REMINDER_BREAKFAST_CLOSE;
		}
		
		return flags;
	}
	
	public boolean isReminderEnabled(Context context, int reminderIndex) {
		int keyId;
		switch (reminderIndex) {
			case REMINDER_PREFAST_MEAL: keyId = R.string.settings_key_prefast_meal_reminder; break;
			case REMINDER_WATER: keyId = R.string.settings_key_water_reminder; break;
			case REMINDER_PREPARE_BREAKFAST: keyId = R.string.settings_key_prepare_breakfast_reminder; break;
			case REMINDER_BREAKFAST_CLOSE: keyId = R.string.settings_key_breakfast_near_reminder; break;
			default:
				throw new IllegalArgumentException("Invalid reminder index.");
		}
		
		return getPrefs(context).getBoolean(context.getString(keyId), false);
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

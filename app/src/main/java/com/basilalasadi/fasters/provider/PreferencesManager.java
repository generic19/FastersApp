package com.basilalasadi.fasters.provider;

import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.basilalasadi.fasters.model.LocationModel;

public abstract class PreferencesManager {
	public static final String PREF_LOCATION_COUNTRY = "location_country";
	public static final String PREF_LOCATION_ADMIN = "location_admin";
	public static final String PREF_LOCATION_CITY = "location_city";
	public static final String PREF_LOCATION_LONGITUDE = "location_longitude";
	public static final String PREF_LOCATION_LATITUDE = "location_latitude";
	
	protected static final String TEMPLATE_DATABASE_VERSION = "database_%s_version";
	
	
	private static final Object mutex = new Object();
	
	
	public static void setLocation(SharedPreferences prefs, LocationModel model) {
		synchronized (mutex) {
			SharedPreferences.Editor editor = prefs.edit();
			
			editor.putString(PREF_LOCATION_COUNTRY, model.country);
			editor.putString(PREF_LOCATION_ADMIN, model.admin);
			editor.putString(PREF_LOCATION_CITY, model.city);
			editor.putLong(PREF_LOCATION_LONGITUDE, Double.doubleToLongBits(model.longitude));
			editor.putLong(PREF_LOCATION_LATITUDE, Double.doubleToLongBits(model.latitude));
			
			editor.apply();
		}
	}
	
	public static LocationModel getLocationModel(SharedPreferences prefs) {
		return new LocationModel(
				prefs.getString(PREF_LOCATION_COUNTRY, null),
				prefs.getString(PREF_LOCATION_ADMIN, null),
				prefs.getString(PREF_LOCATION_CITY, null),
				Double.longBitsToDouble(prefs.getLong(PREF_LOCATION_LONGITUDE, 0)),
				Double.longBitsToDouble(prefs.getLong(PREF_LOCATION_LATITUDE, 0))
		);
	}
	
	public static String getAddress(SharedPreferences prefs) {
		return TextUtils.join(", ", new String[]{
				prefs.getString(PREF_LOCATION_COUNTRY, null),
				prefs.getString(PREF_LOCATION_ADMIN, null),
				prefs.getString(PREF_LOCATION_CITY, null)});
	}
	
	public static Location getLocation(SharedPreferences prefs) {
		return new Location(
				Double.longBitsToDouble(prefs.getLong(PREF_LOCATION_LONGITUDE, 0)),
				Double.longBitsToDouble(prefs.getLong(PREF_LOCATION_LATITUDE, 0))
		);
	}
	
	public static int getDatabaseVersion(SharedPreferences prefs, @NonNull String name) {
		return prefs.getInt(String.format(TEMPLATE_DATABASE_VERSION, name), 0);
	}
	
	
	public static final class Location {
		public final double longitude;
		public final double latitude;
		
		public Location(double longitude, double latitude) {
			this.longitude = longitude;
			this.latitude = latitude;
		}
	}
}

package com.basilalasadi.fasters.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemClock;
import android.util.Log;

import com.basilalasadi.fasters.MyApplication;
import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.executors.AppExecutors;
import com.basilalasadi.fasters.provider.PreferencesManager;
import com.basilalasadi.fasters.util.BinaryReader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public final class CitiesDatabase {
	private static final String DATABASE_NAME = "worldcities";
	
	private static CitiesDatabase instance;
	
	private SQLiteDatabase database;
	
	protected final Object mutex = new Object();
	
	public static synchronized CitiesDatabase getInstance(Context context) {
		if (instance == null) {
			instance = new CitiesDatabase(context);
		}
		return instance;
	}
	
	private CitiesDatabase(Context context) {
		AppExecutors.ioExecutor.submit(() -> {
			try {
				initializeDatabase(context);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	private void initializeDatabase(Context context) throws IOException {
		synchronized (mutex) {
			SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_name), 0);
			
			int version = PreferencesManager.getDatabaseVersion(prefs, DATABASE_NAME);
			
			if (version < 1) {
				context.deleteDatabase(DATABASE_NAME + ".db");
				copyDatabaseFromAssets(context);
			}
			else {
				try {
					database = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME + ".db").getPath(), null, SQLiteDatabase.OPEN_READONLY);
				}
				catch (SQLiteException e) {
					context.deleteDatabase(DATABASE_NAME + ".db");
					copyDatabaseFromAssets(context);
					
					database = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME + ".db").getPath(), null, SQLiteDatabase.OPEN_READONLY);
				}
			}
		}
	}
	
	private void copyDatabaseFromAssets(Context context) throws IOException {
		synchronized (mutex) {
			long t = SystemClock.elapsedRealtimeNanos();
			database = new AssetDatabase(context, DATABASE_NAME).openDatabase();
			t = SystemClock.elapsedRealtimeNanos() - t;
			
			Log.d("CitiesDatabase", "copied database in " + (t / 1000000000d) + " s.");
		}
	}
	
	@Override
	protected synchronized void finalize() throws Throwable {
		synchronized (mutex) {
			if (database != null) {
				database.close();
				database = null;
			}
		}
		super.finalize();
	}
	
	public String[] getCountries() {
		synchronized (mutex) {
			Cursor cursor = database.query(true, "cities", new String[]{"country"}, null, null, null, null, "country", null);
			
			int colCountry = cursor.getColumnIndexOrThrow("country");
			
			ArrayList<String> countries = new ArrayList<>(250);
			
			while (cursor.moveToNext()) {
				countries.add(cursor.getString(colCountry));
			}
			
			cursor.close();
			return countries.toArray(new String[0]);
		}
		
	}
	
	public AdminCity[] getCountryAdminCities(String country) {
		synchronized (mutex) {
			Cursor cursor = database.query("cities", new String[]{"admin", "city"}, "country == ?", new String[]{country}, null, null, "admin, city", null);
			
			int colAdmin = cursor.getColumnIndexOrThrow("admin");
			int colCity = cursor.getColumnIndexOrThrow("city");
			
			ArrayList<AdminCity> adminCities = new ArrayList<>();
			
			while (cursor.moveToNext()) {
				String admin = cursor.getString(colAdmin);
				String city = cursor.getString(colCity);
				
				adminCities.add(new AdminCity(admin, city));
			}
			
			cursor.close();
			return adminCities.toArray(new AdminCity[0]);
		}
	}
	
	public CityLocation getAdminCityLocation(String country, String admin, String city) {
		synchronized (mutex) {
			Cursor cursor = database.query("Cities", new String[]{"longitude", "latitude"}, "country == ? AND admin == ? AND city == ?", new String[]{country, admin, city}, null, null, null, "1");
			
			if (!cursor.moveToFirst()) {
				cursor.close();
				return null;
			} else {
				int colLongitude = cursor.getColumnIndexOrThrow("longitude");
				int colLatitude = cursor.getColumnIndexOrThrow("latitude");
				
				double longitude = cursor.getDouble(colLongitude);
				double latitude = cursor.getDouble(colLatitude);
				
				cursor.close();
				return new CityLocation(longitude, latitude);
			}
		}
	}
	
	public CountryAdminCity findClosestCountryAdminCity(double longitude, double latitude) {
		synchronized (mutex) {
			final double tolerance = 2;
			
			@SuppressLint("DefaultLocale")
			final String orderBy = String.format(
					"(longitude - %1$f) * (longitude - %1$f) + (latitude - %2$f) * (latitude - %2$f)",
					longitude, latitude);
			
			Cursor cursor = database.query("cities",
					new String[]{"country", "admin", "city"},
					"longitude BETWEEN ? AND ? AND latitude BETWEEN ? AND ?",
					new String[]{
							String.valueOf(longitude - tolerance),
							String.valueOf(longitude + tolerance),
							String.valueOf(latitude - tolerance),
							String.valueOf(latitude + tolerance)
					},
					null,
					null,
					orderBy,
					"1");
			
			if (!cursor.moveToFirst()) {
				cursor.close();
				return null;
			} else {
				int colCountry = cursor.getColumnIndexOrThrow("country");
				int colAdmin = cursor.getColumnIndexOrThrow("admin");
				int colCity = cursor.getColumnIndexOrThrow("city");
				
				CountryAdminCity result = new CountryAdminCity(cursor.getString(colCountry), cursor.getString(colAdmin), cursor.getString(colCity));
				
				cursor.close();
				return result;
			}
		}
	}
	
	
	public static final class CityLocation {
		public final double longitude;
		public final double latitude;
		
		public CityLocation(double longitude, double latitude) {
			this.longitude = longitude;
			this.latitude = latitude;
		}
	}
	
	
	public static class AdminCity {
		public final String admin;
		public final String city;
		
		public AdminCity(String admin, String city) {
			this.admin = admin;
			this.city = city;
		}
	}
	
	
	public static final class CountryAdminCity extends AdminCity {
		public final String country;
		
		
		public CountryAdminCity(String country, String admin, String city) {
			super(admin, city);
			this.country = country;
		}
	}
}

package com.basilalasadi.fasters.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.executors.AppExecutors;
import com.basilalasadi.fasters.provider.SettingsManager;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Handler singleton class for worldcities database.
 */
public final class CitiesDatabase {
	private static final String DATABASE_NAME = "worldcities";

	private static CitiesDatabase instance;
	private SQLiteDatabase database;
	protected final Object mutex = new Object();
	
	/**
	 * Returns singleton instance of CitiesDatabase, or construsts it if not already constructed.
	 * Constructing the CitiesDatabase instance will begin asyncronous initialization of the
	 * database.
	 *
	 * @param context The current context.
	 * @return CitiesDatabase instrance.
	 */
	public static synchronized CitiesDatabase getInstance(Context context) {
		if (instance == null) {
			instance = new CitiesDatabase(context);
		}
		return instance;
	}
	
	/**
	 * Private constructor. Only one instrance of CitiesDatabase should exist.
	 * @param context The current context.
	 */
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
	
	/**
	 * Initializes the CitiesDatabase instance.
	 * @param context The current context.
	 * @throws IOException if database asset is not found.
	 */
	private void initializeDatabase(Context context) throws IOException {
		synchronized (mutex) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			SettingsManager settingsManager = SettingsManager.getInstance();
			
			final String versionKey = context.getString(R.string.key_database_version_template, DATABASE_NAME);
			
			final int latestVersion = Integer.parseInt(context.getString(R.string.latest_database_worldcities_version));
			int version = prefs.getInt(versionKey, 1);
			
			AssetDatabase assetDatabase = new AssetDatabase(context, DATABASE_NAME);
			
			if (version < latestVersion) {
				assetDatabase.deleteDatabase();
			}
			
			database = assetDatabase.openDatabase();
			
			if (database != null) {
				prefs.edit().putInt(versionKey, latestVersion).apply();
			}
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
	
	/**
	 * Fetches all countries.
	 * @return a string array of all countries.
	 */
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
	
	/**
	 * Fetches all admin cities of selected country.
	 * @param country Country.
	 * @return An array of all admin cities of selected country.
	 */
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
	
	/**
	 * Fetches location of selected city.
	 * @param country Country of city.
	 * @param admin Administrative division of city.
	 * @param city The city.
	 * @return CityLocation of the city.
	 */
	public CityLocation getAdminCityLocation(String country, String admin, String city) {
		synchronized (mutex) {
			Cursor cursor = database.query(
					"Cities",
					new String[]{"longitude", "latitude"},
					"country == ? AND admin == ? AND city == ?",
					new String[]{country, admin, city},
					null, null, null,
					"1");

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
	
	/**
	 * Finds closest city to specified location.
	 * @param longitude location longitude.
	 * @param latitude Location latitude.
	 * @return closest CountryAdminCity to specified location.
	 */
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
	
	
	/**
	 * Container class for the location of a city.
	 */
	public static final class CityLocation {
		public final double longitude;
		public final double latitude;
		
		/**
		 * Container class for the location of a city.
		 * @param longitude location longitude.
		 * @param latitude Location latitude.
		 */
		public CityLocation(double longitude, double latitude) {
			this.longitude = longitude;
			this.latitude = latitude;
		}
	}
	
	
	/**
	 * Container class for a city and its administrative division.
	 */
	public static class AdminCity {
		public final String admin;
		public final String city;
		
		/**
		 * Container class for a city and its administrative division.
		 * @param admin Administrative division of a city.
		 * @param city The city.
		 */
		public AdminCity(String admin, String city) {
			this.admin = admin;
			this.city = city;
		}
	}
	
	
	/**
	 * Container class for a city and its country and administrative division.
	 */
	public static final class CountryAdminCity extends AdminCity {
		public final String country;
		
		/**
		 * Container class for a city and its country and administrative division.
		 * @param country Country of the city.
		 * @param admin Administrative division of a city.
		 * @param city The city.
		 */
		public CountryAdminCity(String country, String admin, String city) {
			super(admin, city);
			this.country = country;
		}
	}
}

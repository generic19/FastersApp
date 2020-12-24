package com.basilalasadi.fasters.provider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import com.basilalasadi.fasters.executors.AppExecutors;
import com.basilalasadi.fasters.view.PermissionsActivity;

import java.util.concurrent.Future;

import static android.content.Context.LOCATION_SERVICE;


@SuppressWarnings("unused")
public abstract class LocationProvider {
	
	public interface OnLocationResultCallback {
		void onLocationResult(LocationResult result);
	}
	
	/**
	 * Uses system's location service to get geolocation.
	 *
	 * Possible error states are:
	 *     ERROR_NO_PROVIDERS when there aren't any available location providers.
	 *     ERROR_PERMISSION when location permission is denied.
	 *     ERROR_NO_LOCATION when the location provider returns no location
	 *     ERROR_EXECUTION when a concurrent error occurs.
	 *
	 * @return Location result.
	 */
	public static LocationResult getLocation(PermissionsActivity activity) {
		return getLocation(activity, false);
	}
	
	/**
	 * Uses system's location service to get geolocation.
	 *
	 * Possible error states are:
	 *     ERROR_NO_PROVIDERS when there aren't any available location providers.
	 *     ERROR_PERMISSION when location permission is denied.
	 *     ERROR_NO_LOCATION when the location provider returns no location
	 *     ERROR_EXECUTION when a concurrent error occurs.
	 *     ERROR_NETWORK when geo-coding fails due to network error.
	 *
	 * @return Location result.
	 */
	public static LocationResult getLocation(PermissionsActivity activity, boolean includeAddress) {
		try {
			if (!ensureLocationPermission(activity)) {
				return new LocationResult(LocationResult.ERROR_PERMISSION);
			}
		}
		catch (InterruptedException e) {
			return new LocationResult(LocationResult.ERROR_INTERRUPTED);
		}
		
		LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
		
		assert locationManager != null;
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setCostAllowed(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		
		String provider = locationManager.getBestProvider(criteria, true);
		
		if (provider == null) {
			return new LocationResult(LocationResult.ERROR_NO_PROVIDERS);
		}
		
		@SuppressLint("MissingPermission")
		Location geoLocation = locationManager.getLastKnownLocation(provider);
		
		if (geoLocation == null) {
			return new LocationResult(LocationResult.ERROR_NO_LOCATION);
		}
		
		if (!includeAddress) {
			return new LocationResult(geoLocation);
		}
		else {
			try {
				Geocoder geocoder = new Geocoder(activity.getApplicationContext());
				
				List<Address> addresses = geocoder.getFromLocation(geoLocation.getLatitude(), geoLocation.getLongitude(), 1);
				
				if (addresses == null || addresses.isEmpty()) {
					return new LocationResult(geoLocation);
				} else {
					Address address = addresses.get(0);
					String addressString = String.format("%s, %s", address.getAdminArea(), address.getCountryName());
					return new LocationResult(addressString, geoLocation);
				}
			} catch (IOException e) {
				return new LocationResult(LocationResult.ERROR_NETWORK);
			}
		}
	}
	
	/**
	 * Executes {@link #getLocation(PermissionsActivity)} asynchronously.
	 *
	 * @return Future to location result.
	 */
	public static Future<LocationResult> getLocationAsync(PermissionsActivity activity) {
		return AppExecutors.ioExecutor.submit(() -> getLocation(activity));
	}
	
	/**
	 * Executes {@link #getLocation(PermissionsActivity, boolean)} asynchronously.
	 *
	 * @return Future to location result.
	 */
	public static Future<LocationResult> getLocationAsync(PermissionsActivity activity, boolean includeAddress) {
		return AppExecutors.ioExecutor.submit(() -> getLocation(activity, includeAddress));
	}
	
	/**
	 * Executes {@link #getLocation(PermissionsActivity)} asynchronously and calls `callback` wih the result.
	 *
	 * @param callback The callback to call when location result is available.
	 */
	public static void getLocationAsync(PermissionsActivity activity, OnLocationResultCallback callback) {
		AppExecutors.ioExecutor.submit(() -> callback.onLocationResult(getLocation(activity)));
	}
	
	/**
	 * Executes {@link #getLocation(PermissionsActivity, boolean)} asynchronously and calls `callback` wih the result.
	 *
	 * @param callback The callback to call when location result is available.
	 */
	public static void getLocationAsync(PermissionsActivity activity, OnLocationResultCallback callback, boolean includeAddress) {
		AppExecutors.ioExecutor.submit(() -> callback.onLocationResult(getLocation(activity, includeAddress)));
	}
	
	protected static String geoLocationToAddress(PermissionsActivity activity, Location geoLocation) throws IOException {
		Geocoder geocoder = new Geocoder(activity.getApplicationContext());
		
		List<Address> addresses = geocoder.getFromLocation(geoLocation.getLatitude(), geoLocation.getLongitude(), 1);
		
		if (addresses == null || addresses.isEmpty()) {
			return null;
		}
		else {
			Address address = addresses.get(0);
			return String.format("%s, %s", address.getAdminArea(), address.getCountryName());
		}
	}
	
	@NonNull
	public static LocationResult locationFromAddress(PermissionsActivity activity, @NonNull String addressString) {
		Geocoder geocoder = new Geocoder(activity.getApplicationContext());
		
		try {
			List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
			
			if (addresses == null || addresses.isEmpty()) {
				return new LocationResult(LocationResult.ERROR_NO_LOCATION);
			}
			else {
				return new LocationResult(addresses.get(0));
			}
		}
		catch (IOException e) {
			return new LocationResult(LocationResult.ERROR_NETWORK);
		}
	}
	
	public static Future<LocationResult> locationFromAddressAsync(PermissionsActivity activity, String addressString) {
		return AppExecutors.ioExecutor.submit(() -> locationFromAddress(activity, addressString));
	}
	
	public static void locationFromAddressAsync(PermissionsActivity activity, String addressString, OnLocationResultCallback callback) {
		AppExecutors.ioExecutor.submit(() -> callback.onLocationResult(locationFromAddress(activity, addressString)));
	}
	
	protected static boolean ensureLocationPermission(PermissionsActivity activity) throws InterruptedException {
		return activity.ensurePermission(Manifest.permission.ACCESS_COARSE_LOCATION);
	}
	
	
	public static class LocationResult {
		public static final int STATUS_LONG_LAT_PRESENT  = 0x01;
		public static final int STATUS_ADDRESS_PRESENT   = 0x02;
		public static final int STATUS_ERROR             = 0x04;
		public static final int ERROR_PERMISSION         = 0x10;
		public static final int ERROR_NETWORK            = 0x20;
		public static final int ERROR_NO_PROVIDERS       = 0x30;
		public static final int ERROR_NO_LOCATION        = 0x40;
		public static final int ERROR_EXECUTION          = 0x50;
		public static final int ERROR_INTERRUPTED        = 0x60;
		
		public final String address;
		public final double longitude;
		public final double latitude;
		public final int status;
		
		public LocationResult(String address) {
			this.address = address;
			this.longitude = 0;
			this.latitude = 0;
			this.status = STATUS_ADDRESS_PRESENT;
		}
		
		public LocationResult(double longitude, double latitude) {
			this.address = null;
			this.longitude = longitude;
			this.latitude = latitude;
			this.status = STATUS_LONG_LAT_PRESENT;
		}
		
		public LocationResult(String address, double longitude, double latitude) {
			this.address = address;
			this.longitude = longitude;
			this.latitude = latitude;
			this.status = STATUS_LONG_LAT_PRESENT | STATUS_ADDRESS_PRESENT;
		}
		
		public LocationResult(String address, Location geolocation) {
			this(address, geolocation.getLongitude(), geolocation.getLatitude());
		}
		
		public LocationResult(Location location) {
			this(location.getLongitude(), location.getLatitude());
		}
		
		public LocationResult(Address address) {
			this(String.format("%s, %s", address.getAdminArea(), address.getCountryName()),
					address.getLongitude(), address.getLatitude());
		}
		
		public LocationResult(int error) {
			this.address = null;
			this.longitude = 0;
			this.latitude = 0;
			this.status = STATUS_ERROR | error;
		}
		
		public boolean isError() {
			return (status & 0xf) == STATUS_ERROR;
		}
		
		public int getError() {
			return (status & 0xf0);
		}
		
		public boolean geoLocationAvailable() {
			return (status & STATUS_LONG_LAT_PRESENT) != 0;
		}
		
		public boolean addressAvailable() {
			return (status & STATUS_ADDRESS_PRESENT) != 0;
		}
	}
	
}

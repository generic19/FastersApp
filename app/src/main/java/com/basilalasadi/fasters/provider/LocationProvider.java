package com.basilalasadi.fasters.provider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import com.basilalasadi.fasters.executors.AppExecutors;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public abstract class LocationProvider {
	
	public interface OnLocationResultCallback {
		void onLocationResult(LocationResult result);
	}
	
	private static final PermissionsActivity permissionsActivity = new PermissionsActivity();
	
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
	public static LocationResult getLocation() {
		return getLocation(false);
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
	public static LocationResult getLocation(boolean includeAddress) {
		LocationManager locationManager = (LocationManager) permissionsActivity.getSystemService(LOCATION_SERVICE);
		
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
		else {
			try {
				if (!ensureLocationPermission()) {
					return new LocationResult(LocationResult.ERROR_PERMISSION);
				}
			}
			catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
				return new LocationResult(LocationResult.ERROR_EXECUTION);
			}
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
				Geocoder geocoder = new Geocoder(permissionsActivity.getApplicationContext());
				
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
	 * Executes {@link #getLocation()} asynchronously.
	 *
	 * @return Future to location result.
	 */
	public static Future<LocationResult> getLocationAsync() {
		return AppExecutors.ioExecutor.submit((Callable<LocationResult>) LocationProvider::getLocation);
	}
	
	/**
	 * Executes {@link #getLocation(boolean)} asynchronously.
	 *
	 * @return Future to location result.
	 */
	public static Future<LocationResult> getLocationAsync(boolean includeAddress) {
		return AppExecutors.ioExecutor.submit(() -> getLocation(includeAddress));
	}
	
	/**
	 * Executes {@link #getLocation()} asynchronously and calls `callback` wih the result.
	 *
	 * @param callback The callback to call when location result is available.
	 */
	public static void getLocationAsync(OnLocationResultCallback callback) {
		AppExecutors.ioExecutor.submit(() -> callback.onLocationResult(getLocation()));
	}
	
	/**
	 * Executes {@link #getLocation(boolean)} asynchronously and calls `callback` wih the result.
	 *
	 * @param callback The callback to call when location result is available.
	 */
	public static void getLocationAsync(OnLocationResultCallback callback, boolean includeAddress) {
		AppExecutors.ioExecutor.submit(() -> callback.onLocationResult(getLocation(includeAddress)));
	}
	
	protected static String geoLocationToAddress(Location geoLocation) throws IOException {
		Geocoder geocoder = new Geocoder(permissionsActivity.getApplicationContext());
		
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
	public static LocationResult locationFromAddress(@NonNull String addressString) {
		Geocoder geocoder = new Geocoder(permissionsActivity.getApplicationContext());
		
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
	
	public static Future<LocationResult> locationFromAddressAsync(String addressString) {
		return AppExecutors.ioExecutor.submit(() -> locationFromAddress(addressString));
	}
	
	public static void locationFromAddressAsync(String addressString, OnLocationResultCallback callback) {
		AppExecutors.ioExecutor.submit(() -> callback.onLocationResult(locationFromAddress(addressString)));
	}
	
	protected static boolean ensureLocationPermission() throws ExecutionException, InterruptedException {
		final int permissionStatus = permissionsActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
		
		if (permissionStatus == PERMISSION_GRANTED) {
			return true;
		}
		else {
			return permissionsActivity.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION).get();
		}
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
	
	
	protected static class PermissionsActivity extends Activity {
		protected static final ExecutorService executor = Executors.newSingleThreadExecutor();
		
		protected Boolean permissionResult = null;
		
		
		public synchronized Future<Boolean> requestPermission(String permission) {
			return executor.submit(() -> {
				if (checkSelfPermission(permission) == PERMISSION_GRANTED) {
					return true;
				}
				else {
					synchronized (permission) {
						permissionResult = null;
						
						requestPermissions(new String[]{permission}, 0);
						
						while (permissionResult == null) {
							permission.wait();
						}
						
						return permissionResult;
					}
				}
			});
		}
		
		@Override
		public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
			String permission = permissions[0];
			
			permissionResult = grantResults[0] == PERMISSION_GRANTED;
			
			permission.notifyAll();
		}
	}
}

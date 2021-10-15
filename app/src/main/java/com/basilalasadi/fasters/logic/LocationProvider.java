package com.basilalasadi.fasters.logic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.basilalasadi.fasters.executors.AppExecutors;

import static android.content.Context.LOCATION_SERVICE;


public abstract class LocationProvider {
	
	public interface OnLocationResultCallback {
		void onLocationResult(LocationResult result);
	}
	
	/**
	 * Uses system's location service to get device's coarse location. Assumes that coarse location permission is granted.
	 *
	 * Possible error states are:
	 *     ERROR_NO_PROVIDERS when there aren't any available location providers.
	 *     ERROR_NO_LOCATION when the location provider returns no location.
	 *     ERROR_NETWORK when geo-coding fails due to network error.
	 *
	 * @param context current context.
	 * @return Location result.
	 */
	public static LocationResult getLocationNoCheck(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
		
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
		else {
			return new LocationResult(geoLocation);
		}
	}
	
	public static void getLocationNoCheck(Context context, OnLocationResultCallback callback) {
		AppExecutors.ioExecutor.execute(() -> {
			try {
				callback.onLocationResult(getLocationNoCheck(context));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	
	public static class LocationResult {
		public static final int STATUS_LONG_LAT_PRESENT  = 0x01;
		public static final int STATUS_ADDRESS_PRESENT   = 0x02;
		public static final int STATUS_ERROR             = 0x04;
//		public static final int ERROR_PERMISSION         = 0x10;
//		public static final int ERROR_NETWORK            = 0x20;
		public static final int ERROR_NO_PROVIDERS       = 0x30;
		public static final int ERROR_NO_LOCATION        = 0x40;
//		public static final int ERROR_EXECUTION          = 0x50;
//		public static final int ERROR_INTERRUPTED        = 0x60;
		
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

package com.basilalasadi.fasters.model;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class LocationViewModel extends ViewModel {
	public final String country;
	public final String city;
	
	public LocationViewModel(String country, String city) {
		this.country = country;
		this.city = city;
	}
	
	public LocationViewModel(Bundle bundle) {
		this.country = bundle.getString("LocationViewModel.country");
		this.city = bundle.getString("LocationViewModel.city");
	}
	
	public void saveToBundle(Bundle outBundle) {
		outBundle.putString("LocationViewModel.country", country);
		outBundle.putString("LocationViewModel.city", city);
	}
}
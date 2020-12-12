package com.basilalasadi.fasters.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;
import com.basilalasadi.fasters.model.LocationModel;
import com.basilalasadi.fasters.model.LocationViewModel;
import com.basilalasadi.fasters.provider.PreferencesManager;
import com.basilalasadi.fasters.provider.LocationProvider;
import com.basilalasadi.fasters.util.ArrayAdapterWithFuzzyFilter;

import java.io.IOException;

public class SetLocationActivity extends AppCompatActivity {
	SharedPreferences prefs;
	CitiesDatabase citiesDatabase;
	
	ArrayAdapterWithFuzzyFilter<String> countriesAdapter;
	ArrayAdapterWithFuzzyFilter<String> citiesAdapter;
	
	String selectedCountry;
	String selectedCity;
	
	
	LocationViewModel viewModel;
	
	
	private final Object mutex = new Object();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			loadInstanceState(savedInstanceState);
		}
		
		prefs = getSharedPreferences(getString(R.string.shared_preferences_name), 0);
		
		int appThemeOrdinal = prefs.getInt("app_theme", AppTheme.Morning.ordinal());
		AppTheme appTheme = AppTheme.fromOrdinal(appThemeOrdinal);
		
		setAppTheme(appTheme);
		
		citiesDatabase = CitiesDatabase.getInstance(this);
		
		countriesAdapter = new ArrayAdapterWithFuzzyFilter<>(this, android.R.layout.simple_dropdown_item_1line);
		citiesAdapter = new ArrayAdapterWithFuzzyFilter<>(this, android.R.layout.simple_dropdown_item_1line);
		
		countriesAdapter.addAll(citiesDatabase.getCountries());
		updateCityOptions();
		
		
		setContentView(R.layout.activity_set_location);
		
		
		AutoCompleteTextView actvCountry = findViewById(R.id.autoCompleteTextViewCountry);
		AutoCompleteTextView actvCity = findViewById(R.id.autoCompleteTextViewCity);
		
		if (selectedCountry != null) {
			actvCountry.setText(selectedCountry);
			
			if (selectedCity != null) {
				actvCity.setText(selectedCity);
			}
			else if (viewModel != null) {
				actvCity.setText(viewModel.city);
			}
		}
		else if (viewModel != null) {
			actvCountry.setText(viewModel.country);
			actvCity.setText(viewModel.city);
		}
		
		
		addListeners();
	}
	
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("selectedCountry", selectedCountry);
		outState.putString("selectedCity", selectedCity);
		
		AutoCompleteTextView actvCountry = findViewById(R.id.autoCompleteTextViewCountry);
		AutoCompleteTextView actvCity = findViewById(R.id.autoCompleteTextViewCity);
		
		if (actvCountry != null && actvCity != null) {
			viewModel = new LocationViewModel(actvCountry.getText().toString(), actvCity.getText().toString());
		}
		
		if (viewModel != null) {
			viewModel.saveToBundle(outState);
		}
	}
	
	protected void loadInstanceState(Bundle savedInstanceState) {
		selectedCountry = savedInstanceState.getString("selectedCountry");
		selectedCity = savedInstanceState.getString("selectedCity");
		
		viewModel = new LocationViewModel(savedInstanceState);
	}
	
	private void addListeners() {
		Button buttonLocate = findViewById(R.id.buttonLocate);
		Button buttonSetLocation = findViewById(R.id.buttonSetLocation);
		AutoCompleteTextView actvCountry = findViewById(R.id.autoCompleteTextViewCountry);
		AutoCompleteTextView actvCity = findViewById(R.id.autoCompleteTextViewCity);
		
		
		buttonLocate.setOnClickListener((v) -> {
			String errorMessage = fetchLocation();
			
			if (errorMessage != null) {
				Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			}
		});
		
		
		buttonSetLocation.setOnClickListener((v) -> {
			String errorMessage = setLocationFromSelected();
			
			if (errorMessage != null) {
				Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			}
		});
		
		
		actvCountry.setOnItemClickListener((parent, view, position, id) -> {
			selectedCountry = countriesAdapter.getItem(position);
			selectedCity = null;
			updateCityOptions();
		});
		
		
		actvCountry.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (selectedCountry != null) {
					selectedCountry = null;
					selectedCity = null;
					updateCityOptions();
				}
			}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		
		actvCity.setOnItemClickListener((parent, view, position, id) -> {
			selectedCity = citiesAdapter.getItem(position);
		});
	}
	
	
	private String fetchLocation() {
		LocationProvider.LocationResult locationResult = LocationProvider.getLocation();
		
		if (locationResult.isError()) {
			switch (locationResult.getError()) {
				case LocationProvider.LocationResult.ERROR_NO_PROVIDERS:
					return "No location providers. Turn on location services.";
					
				case LocationProvider.LocationResult.ERROR_PERMISSION:
					return "Permission denied.";
					
				case LocationProvider.LocationResult.ERROR_NO_LOCATION:
					return "No location found. Turn on location services.";
					
				case LocationProvider.LocationResult.ERROR_EXECUTION:
					return "Could not get location; execution error.";
					
				default:
					return "Could not get location; unknown error.";
			}
		}
		else {
			CitiesDatabase.CountryAdminCity countryAdminCity =
					citiesDatabase.findClosestCountryAdminCity(locationResult.longitude, locationResult.latitude);
			
			LocationModel model;
			
			if (countryAdminCity == null) {
				model = new LocationModel(null, null, null, locationResult.longitude, locationResult.latitude);
			}
			else {
				model = new LocationModel(countryAdminCity.country, countryAdminCity.admin,
						countryAdminCity.city, locationResult.longitude, locationResult.latitude);
			}
			
			PreferencesManager.setLocation(prefs, model);
			
			return null;
		}
	}
	
	
	private void updateCityOptions() {
		synchronized (mutex) {
			if (selectedCountry == null) {
				citiesAdapter.clear();
			}
			else {
				CitiesDatabase.AdminCity[] adminCities = citiesDatabase.getCountryAdminCities(selectedCountry);
				citiesAdapter.clear();
				
				for (CitiesDatabase.AdminCity adminCity : adminCities) {
					citiesAdapter.add(String.format("%s\u200B, %s", adminCity.city, adminCity.admin));
				}
			}
		}
	}
	
	
	private String setLocationFromSelected() {
		if (selectedCountry == null) {
			return "Select a country and a city.";
		}
		else if (countriesAdapter.indexOf(selectedCountry) == -1) {
			return "Select a valid country.";
		}
		else if (selectedCity == null) {
			return "Select a city.";
		}
		
		String[] cityAdminPair = selectedCity.split("\u200B, ");
		
		if (cityAdminPair.length != 2) {
			return "Select a valid city.";
		}
		
		CitiesDatabase.CityLocation cityLocation =
				citiesDatabase.getAdminCityLocation(selectedCountry, cityAdminPair[1], cityAdminPair[0]);
		
		if (cityLocation == null) {
			return "Select a valid city.";
		}
		
		LocationModel model = new LocationModel(
				selectedCountry,
				cityAdminPair[1],
				cityAdminPair[0],
				cityLocation.longitude,
				cityLocation.latitude
		);
		
		PreferencesManager.setLocation(prefs, model);
		
		return null;
	}
	
	
	/**
	 * Sets the base theme of this activity to the corresponding value of `appTheme`.
	 *
	 * @param appTheme The AppTheme to set.
	 */
	private void setAppTheme(AppTheme appTheme) {
		switch (appTheme) {
			case Evening:
				setTheme(R.style.Theme_App_Evening);
				break;
			
			case Morning:
			default:
				setTheme(R.style.Theme_App_Morning);
		}
	}
}

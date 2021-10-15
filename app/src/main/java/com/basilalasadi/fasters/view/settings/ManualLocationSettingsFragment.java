package com.basilalasadi.fasters.view.settings;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;
import com.basilalasadi.fasters.executors.AppExecutors;
import com.basilalasadi.fasters.logic.settings.SettingsManager;
import com.basilalasadi.fasters.util.ArrayAdapterWithFuzzyFilter;

import java.util.concurrent.Executor;


public class ManualLocationSettingsFragment extends PreferenceFragmentCompat {
	private static final String TAG = "Autocomplete";
	private final Executor executor = AppExecutors.cpuExecutor;
	
	private String keyCountry;
	private String keyCity;
	private SearchableListPreference countryPreference;
	private SearchableListPreference cityPreference;
	private ArrayAdapterWithFuzzyFilter<String> countryAdapter;
	private ArrayAdapterWithFuzzyFilter<String> cityAdapter;
	
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		keyCountry = requireContext().getString(R.string.settings_key_country);
		keyCity = requireContext().getString(R.string.settings_key_city);
		
		countryAdapter = new ArrayAdapterWithFuzzyFilter<>(requireContext(), R.layout.list_item, R.id.textview);
		cityAdapter = new ArrayAdapterWithFuzzyFilter<>(requireContext(), R.layout.list_item, R.id.textview);
		
		executor.execute(() -> {
			countryAdapter.addAll(CitiesDatabase.getInstance(getContext()).getCountries());
			
			String country = countryPreference.getText();
			
			if (country != null && !country.isEmpty()) {
				CitiesDatabase.AdminCity[] adminCities = CitiesDatabase.getInstance(getContext()).getCountryAdminCities(country);
				
				String[] options = new String[adminCities.length];
				
				for (int i = 0; i < adminCities.length; i++) {
					options[i] = adminCities[i].city + SettingsManager.CITY_ADMIN_SEPARATOR + adminCities[i].admin;
				}
				
				cityAdapter.clear();
				cityAdapter.addAll(options);
			}
		});
		
		setPreferencesFromResource(R.xml.manual_location_settings, rootKey);
		PreferenceScreen prefScreen = getPreferenceScreen();
		
		countryPreference = new SearchableListPreference(getContext(), R.string.settings_key_country, R.string.country);
		cityPreference = new SearchableListPreference(getContext(), R.string.settings_key_city, R.string.city);
		
		CountryPreferenceChangeListener countryChangeListener = new CountryPreferenceChangeListener();
		CityPreferenceChangeListener cityChangeListener = new CityPreferenceChangeListener();
		
		countryPreference.setOnPreferenceChangeListener(countryChangeListener);
		cityPreference.setOnPreferenceChangeListener(cityChangeListener);
		
		cityPreference.setSummaryProvider(null);
		
		prefScreen.addPreference(countryPreference);
		prefScreen.addPreference(cityPreference);
	}
	
	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		final String key = preference.getKey();
		
		if (key.equals(keyCountry)) {
			PreferenceDialogFragmentCompat fragment =
					SearchaableListDialogFragment.newInstance(getString(R.string.settings_key_country), countryAdapter);
			
			fragment.setTargetFragment(this, 0);
			fragment.show(getParentFragmentManager(), TAG);
		}
		else if (key.equals(keyCity)) {
			PreferenceDialogFragmentCompat fragment = SearchaableListDialogFragment.newInstance(getString(R.string.settings_key_city), cityAdapter);
			
			fragment.setTargetFragment(this, 0);
			fragment.show(getParentFragmentManager(), TAG);
		}
		else {
			super.onDisplayPreferenceDialog(preference);
		}
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		requireActivity().setTitle(R.string.manual_location);
	}
	
	
	private class CountryPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			cityAdapter.clear();
			SettingsManager.getInstance(requireContext()).clearLocation(requireContext());
			
			cityPreference.setText(null);
			cityPreference.setSummary(R.string.not_set);
			
			executor.execute(() -> {
				CitiesDatabase.AdminCity[] adminCities = CitiesDatabase.getInstance(getContext()).getCountryAdminCities((String) newValue);
				
				Log.d(TAG, adminCities.length + " cities found.");
				
				String[] options = new String[adminCities.length];
				
				for (int i = 0; i < adminCities.length; i++) {
					options[i] = adminCities[i].city + SettingsManager.CITY_ADMIN_SEPARATOR + adminCities[i].admin;
				}
				
				cityAdapter.addAll(options);
			});
			
			return true;
		}
	}
	
	
	private class CityPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String country = countryPreference.getText();
			String cityAdmin = (String) newValue;
			
			if (country == null || cityAdmin == null) {
				return false;
			}
			
			String[] cityAdminArray = cityAdmin.split(SettingsManager.CITY_ADMIN_SEPARATOR);
			
			if (cityAdminArray.length != 2) {
				return false;
			}
			
			String city = cityAdminArray[0];
			String admin = cityAdminArray[1];
			
			boolean locationSet = SettingsManager.getInstance(getContext())
					.setLocation(getContext(), new SettingsManager.Address(country, admin, city));
			
			if (locationSet) {
				String text = cityPreference.getText();
				
				if (text == null) {
					cityPreference.setSummary(R.string.not_set);
				}
				else {
					cityPreference.setSummary(text);
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
}

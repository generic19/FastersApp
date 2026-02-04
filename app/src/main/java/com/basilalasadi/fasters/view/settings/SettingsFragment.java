package com.basilalasadi.fasters.view.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import org.jetbrains.annotations.NotNull;

import com.basilalasadi.fasters.R;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {
	
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.settings, rootKey);
		
		//noinspection ConstantConditions
		findPreference("location").setSummaryProvider(preference -> {
			SharedPreferences prefs = preference.getSharedPreferences();
			
			String country = prefs.getString("country", null);
			String city = prefs.getString("city", null);
			
			if (country != null && city != null) {
				return String.format("%s, %s", city, country);
			}
			else {
				return getString(R.string.not_set);
			}
		});
		
		//noinspection ConstantConditions
		findPreference("method").setSummaryProvider(preference -> {
			SharedPreferences prefs = preference.getSharedPreferences();
			
			String calculationMethod = prefs.getString("calculation_method", null);
			
			if (calculationMethod == null) {
				return getString(R.string.not_set);
			}
			else {
				String[] calculationMethods =
						getResources().getStringArray(R.array.calculation_method_entry_values);
				
				String[] calculationMethodsText =
						getResources().getStringArray(R.array.calculation_method_entries);
				
				String text = null;
				
				for (int i = 0; i < calculationMethods.length; i++) {
					if (calculationMethods[i].equals(calculationMethod)) {
						text = calculationMethodsText[i];
						break;
					}
				}
				
				if (text == null) {
					throw new RuntimeException("Stored value of calculation_method in shared preferences does not match any of the entries.");
				}
				
				return text;
			}
		});
		
		findPreference("open_source_licenses").setOnPreferenceClickListener(preference -> {
			new LibsBuilder()
					.withSearchEnabled(true)
					.start(getActivity().getApplicationContext());
			return true;
		});
	}
	
	@Override
	public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		requireActivity().setTitle(R.string.settings);
	}
}
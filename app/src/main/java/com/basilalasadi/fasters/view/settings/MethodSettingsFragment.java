package com.basilalasadi.fasters.view.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.basilalasadi.fasters.R;


public class MethodSettingsFragment extends PreferenceFragmentCompat {
	
	@SuppressWarnings("ConstantConditions")
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.method_settings, rootKey);
		
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		
		
		String calculationMethod = prefs.getString("calculation_method", "automatic");
		
		findPreference("custom").setVisible(calculationMethod.equals("custom"));
		
		findPreference("calculation_method").setOnPreferenceChangeListener((preference, newValue) -> {
			boolean isCustom = newValue.equals("custom");
			
			findPreference("custom").setVisible(isCustom);
			return true;
		});
		
		
		Preference ishaSunAnglePreference = findPreference("isha_sun_angle");
		Preference ishaTimeOffsetPreference = findPreference("isha_time_offset");
		Preference useRamadanOffsetPreference = findPreference("use_ramadan_offset");
		Preference ramadanIshaTimeOffsetPreference = findPreference("ramadan_isha_time_offset");
		
		if (prefs.getString("isha_calculation_method", "sun_angle").equals("time_offset")) {
			ishaSunAnglePreference.setEnabled(false);
			ishaTimeOffsetPreference.setEnabled(true);
			useRamadanOffsetPreference.setEnabled(true);
			ramadanIshaTimeOffsetPreference.setEnabled(prefs.getBoolean("use_ramadan_offset", false));
		}
		else {
			ishaSunAnglePreference.setEnabled(true);
			ishaTimeOffsetPreference.setEnabled(false);
			useRamadanOffsetPreference.setEnabled(false);
			ramadanIshaTimeOffsetPreference.setEnabled(false);
		}
		
		findPreference("isha_calculation_method").setOnPreferenceChangeListener((preference, newValue) -> {
			if (newValue.equals("time_offset")) {
				ishaSunAnglePreference.setEnabled(false);
				ishaTimeOffsetPreference.setEnabled(true);
				useRamadanOffsetPreference.setEnabled(true);
				ramadanIshaTimeOffsetPreference.setEnabled(prefs.getBoolean("use_ramadan_offset", false));
			}
			else {
				ishaSunAnglePreference.setEnabled(true);
				ishaTimeOffsetPreference.setEnabled(false);
				useRamadanOffsetPreference.setEnabled(false);
				ramadanIshaTimeOffsetPreference.setEnabled(false);
			}
			return true;
		});
		
		useRamadanOffsetPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			ramadanIshaTimeOffsetPreference.setEnabled((boolean) newValue);
			return true;
		});
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//noinspection ConstantConditions
		getActivity().setTitle(R.string.method);
	}
}

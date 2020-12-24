package com.basilalasadi.fasters.view.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.basilalasadi.fasters.R;


public class ManualLocationSettingsFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.manual_location_settings, rootKey);
	}
	
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//noinspection ConstantConditions
		getActivity().setTitle(R.string.manual_location);
	}
}

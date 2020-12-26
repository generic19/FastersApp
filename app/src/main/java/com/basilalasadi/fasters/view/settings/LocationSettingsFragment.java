package com.basilalasadi.fasters.view.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.basilalasadi.fasters.R;

public class LocationSettingsFragment extends PreferenceFragmentCompat {
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.location_settings, rootKey);
    
        //noinspection ConstantConditions
        findPreference("automatic_settings").setOnPreferenceClickListener(preference -> {
            Toast.makeText(getActivity(), "TODO: Add set location code.", Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(R.string.location);
    }
}
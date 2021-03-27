package com.basilalasadi.fasters.view.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.view.AppTheme;
import com.google.android.material.appbar.MaterialToolbar;


public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int appTheme = prefs.getInt("app_theme", AppTheme.THEME_MORNING);
		
		switch (appTheme) {
			case AppTheme.THEME_MORNING:
				setTheme(R.style.Theme_App_Morning);
				break;
				
			case AppTheme.THEME_EVENING:
				setTheme(R.style.Theme_App_Evening);
		}
		
		
		setContentView(R.layout.settings_activity);
		
		((MaterialToolbar) findViewById(R.id.appBar)).setNavigationOnClickListener(v -> super.onBackPressed());
		
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, new SettingsFragment())
				.commit();
	}
	
	@Override
	public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		
		Fragment fragment =
				fragmentManager.getFragmentFactory().instantiate(getClassLoader(), pref.getFragment());
		
		fragment.setArguments(pref.getExtras());
		fragment.setTargetFragment(caller, 0);
		
		fragmentManager
				.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.addToBackStack(null)
				.commit();
		
		return true;
	}
	
	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		((MaterialToolbar) findViewById(R.id.appBar)).setTitle(title);
	}
	
	@Override
	public void setTitle(int resId) {
		super.setTitle(resId);
		((MaterialToolbar) findViewById(R.id.appBar)).setTitle(resId);
	}
}

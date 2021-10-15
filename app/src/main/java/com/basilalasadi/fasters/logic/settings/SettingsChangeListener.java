package com.basilalasadi.fasters.logic.settings;

import android.content.SharedPreferences;


public interface SettingsChangeListener {
	void onSettingsChnage(SharedPreferences prefs, String key);
}

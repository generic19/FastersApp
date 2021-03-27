package com.basilalasadi.fasters;

import android.app.Application;

import com.basilalasadi.fasters.database.CitiesDatabase;
import com.basilalasadi.fasters.provider.SettingsManager;


public class FastersApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		CitiesDatabase.getInstance(this);
		SettingsManager.getInstance().ensureSettingsInitialized(this);
	}
}

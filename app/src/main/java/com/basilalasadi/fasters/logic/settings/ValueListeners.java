package com.basilalasadi.fasters.logic.settings;

import android.content.SharedPreferences;
import java.util.Set;


public abstract class ValueListeners {
	interface ValueListener {
		void onChange(SharedPreferences prefs, String key);
	}
	
	public interface BooleanValueListener extends ValueListener {
		void onChange(boolean newValue);
		
		default void onChange(SharedPreferences prefs, String key) {
			onChange(prefs.getBoolean(key, false));
		}
	}
	
	public interface FloatValueListener extends ValueListener {
		void onChange(float newValue);
		
		default void onChange(SharedPreferences prefs, String key) {
			onChange(prefs.getFloat(key, 0));
		}
	}
	
	public interface IntValueListener extends ValueListener {
		void onChange(int newValue);
		
		default void onChange(SharedPreferences prefs, String key) {
			onChange(prefs.getInt(key, 0));
		}
	}
	
	public interface LongValueListener extends ValueListener {
		void onChange(long newValue);
		
		default void onChange(SharedPreferences prefs, String key) {
			onChange(prefs.getLong(key, 0));
		}
	}
	
	public interface StringValueListener extends ValueListener {
		void onChange(String newValue);
		
		default void onChange(SharedPreferences prefs, String key) {
			onChange(prefs.getString(key, null));
		}
	}
	
	public interface StringSetValueListener extends ValueListener {
		void onChange(Set<String> newValue);
		
		default void onChange(SharedPreferences prefs, String key) {
			onChange(prefs.getStringSet(key, null));
		}
	}
}
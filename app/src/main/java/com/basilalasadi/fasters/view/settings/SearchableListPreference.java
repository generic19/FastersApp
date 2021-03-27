package com.basilalasadi.fasters.view.settings;

import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceManager;

import com.basilalasadi.fasters.R;


public class SearchableListPreference extends DialogPreference {
	
	private PreferenceManager prefManager;
	
	public SearchableListPreference(Context context, @StringRes int key, @StringRes int title, @LayoutRes int layoutResource) {
		super(context);
		setKey(context.getString(key));
		setTitle(context.getString(title));
		setSummaryProvider(SimpleSummaryProvider.getInstance());
		setLayoutResource(layoutResource);
	}
	
	public SearchableListPreference(Context context, @StringRes int key, @StringRes int title) {
		this(context, key, title, R.layout.preference_card);
	}
	
	public String getText() {
		PreferenceDataStore dataStore = prefManager.getPreferenceDataStore();
		
		if (dataStore != null) {
			return dataStore.getString(getKey(), null);
		}
		else {
			return prefManager.getSharedPreferences().getString(getKey(), null);
		}
	}
	
	public void setText(String value) {
		if (callChangeListener(value)) {
			PreferenceDataStore dataStore = prefManager.getPreferenceDataStore();
			
			if (dataStore != null) {
				dataStore.putString(getKey(), value);
			}
			else {
				prefManager.getSharedPreferences().edit().putString(getKey(), value).apply();
			}
			
			notifyChanged();
		}
	}
	
	@Override
	protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
		super.onAttachedToHierarchy(preferenceManager);
		prefManager = preferenceManager;
	}
	
	public static final class SimpleSummaryProvider implements SummaryProvider<SearchableListPreference> {
		private static SimpleSummaryProvider instance;
		
		public static SimpleSummaryProvider getInstance() {
			if (instance == null) {
				instance = new SimpleSummaryProvider();
			}
			return instance;
		}
		
		private SimpleSummaryProvider() {}
		
		@Override
		public CharSequence provideSummary(SearchableListPreference preference) {
			String text = preference.getText();
			
			if (text == null) {
				text = preference.getContext().getString(R.string.not_set);
			}
			
			return text;
		}
	}
}

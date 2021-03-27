package com.basilalasadi.fasters.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.database.CitiesDatabase;
import com.basilalasadi.fasters.util.ArrayAdapterWithFuzzyFilter;


public class SearchaableListDialogFragment extends PreferenceDialogFragmentCompat {
	public static final String TAG = "SearchaableList";
	private final ArrayAdapterWithFuzzyFilter<String> adapter;
	private String selected = null;
	
	
	public static SearchaableListDialogFragment newInstance(String key, ArrayAdapterWithFuzzyFilter<String> adapter) {
		final SearchaableListDialogFragment fragment = new SearchaableListDialogFragment(adapter);
		
		final Bundle bundle = new Bundle(1);
		bundle.putString(ARG_KEY, key);
		fragment.setArguments(bundle);
		
		return fragment;
	}
	
	private SearchaableListDialogFragment(ArrayAdapterWithFuzzyFilter<String> adapter) {
		adapter.getFilter().filter("");
		this.adapter = adapter;
	}
	
	@Override
	protected View onCreateDialogView(Context context) {
		View view = getLayoutInflater().inflate(R.layout.list_view_with_search, null);
		
		EditText editSearch = view.findViewById(R.id.edit_search);
		ListView listOptions = view.findViewById(R.id.list_options);
		
		listOptions.setOnItemClickListener((parent, view1, position, id) -> {
			selected = adapter.getItem(position);
			
			onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
			dismiss();
		});
		
		listOptions.setAdapter(adapter);
		
		editSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				adapter.getFilter().filter(s.toString());
			}
		});
		
		return view;
	}
	
	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult && selected != null) {
			SearchableListPreference pref = (SearchableListPreference) getPreference();
			pref.setText(selected);
		}
	}
}

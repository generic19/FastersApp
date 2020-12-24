package com.basilalasadi.fasters.view.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.EditTextPreference;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.basilalasadi.fasters.util.ArrayAdapterWithFuzzyFilter;


public class AutocompletePreference extends EditTextPreference
		implements AdapterView.OnItemClickListener, AutoCompleteTextView.OnDismissListener {
	
	protected final AutoCompleteTextView editText;
	protected final ArrayAdapterWithFuzzyFilter<String> adapter;
	
	public AutocompletePreference(Context context, String[] options) {
		super(context);
		
		adapter = new ArrayAdapterWithFuzzyFilter<>(context, android.R.layout.simple_dropdown_item_1line);
		adapter.addAll(options);
		
		editText = new AutoCompleteTextView(context);
		editText.setAdapter(adapter);
		editText.setThreshold(0);
		editText.setOnItemClickListener(this);
		editText.setOnDismissListener(this);
	}
	
	public synchronized void setOptions(String[] options) {
		adapter.clear();
		adapter.addAll(options);
		
		adapter.notifyDataSetChanged();
	}
	
	@SuppressLint("MissingSuperCall")
	@Override
	protected synchronized void onBindDialogView(View view) {
		editText.setText(getText());
		
		ViewParent oldParent = editText.getParent();
		
		if (oldParent != view) {
			if (oldParent != null) {
				((ViewGroup) oldParent).removeView(editText);
			}
			
			onAddEditTextToDialogView(view, editText);
		}
	}
	
	@Override
	public synchronized void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String selected = (String) parent.getItemAtPosition(position);
		
		if (callChangeListener(selected)) {
			setText(selected);
		}
	}
	
	@Override
	public synchronized void onDismiss() {
		editText.setText(getText());
	}
}

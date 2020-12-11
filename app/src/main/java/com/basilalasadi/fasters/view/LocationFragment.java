package com.basilalasadi.fasters.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.model.LocationViewModel;
import com.basilalasadi.fasters.resource.Countries;
import com.basilalasadi.fasters.util.ArrayAdapterWithFuzzyFilter;

import java.util.Objects;


public class LocationFragment extends DialogFragment {
	public static final String TAG = "location_dialog";
	
	ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;
	View activityRootView;
	View dialogRootView;
	
	private ArrayAdapterWithFuzzyFilter<String> countriesAdapter;
	private ArrayAdapterWithFuzzyFilter<String> subdivisionsAdapter;
	
	private LocationViewModel viewModel;
	
	private int selectedCountry = -1;
	private int selectedSubdivision = -1;
	
	
	public static LocationFragment newInstance() {
		return new LocationFragment();
	}
	
	private void setCountries(String[] countries) {
		countriesAdapter.clear();
		countriesAdapter.addAll(countries);
	}
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (selectedCountry != -1) {
			String country = Countries.getCountryName(getContext(), selectedCountry);
			outState.putString("country", country);
			
			if (selectedSubdivision != -1) {
				String subdivision = Countries.getSubdivisionName(getContext(), selectedCountry, selectedSubdivision);
				outState.putString("subdivision", subdivision);
			}
		}
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			String country = savedInstanceState.getString("country", null);
			String subdivision = savedInstanceState.getString("subdivision", null);
			
			if (country != null) {
				int countryIndex = Countries.getCountryIndex(getContext(), country);
				setSelectedCountry(countryIndex);
				
				if (subdivision != null) {
					int subdivisionIndex = Countries.getSubdivisionIndex(getContext(), countryIndex, subdivision);
					setSelectedSubdivision(subdivisionIndex);
				}
			}
		}
		
		dialogRootView = inflater.inflate(R.layout.location_fragment, container, false);
		
		AutoCompleteTextView autoCompleteTextViewCountry = dialogRootView.findViewById(R.id.autoCompleteTextViewCountry);
		AutoCompleteTextView autoCompleteTextViewSubdivision = dialogRootView.findViewById(R.id.autoCompleteTextViewSubdivision);
		
		Context context = requireContext();
		countriesAdapter = new ArrayAdapterWithFuzzyFilter<>(context, android.R.layout.simple_dropdown_item_1line);
		subdivisionsAdapter = new ArrayAdapterWithFuzzyFilter<>(context, android.R.layout.simple_dropdown_item_1line);
		
		autoCompleteTextViewCountry.setAdapter(countriesAdapter);
		autoCompleteTextViewSubdivision.setAdapter(subdivisionsAdapter);
		
		
		addListeners(dialogRootView);
		
		
		return dialogRootView;
	}
	
	private void addListeners(View view) {
		final AutoCompleteTextView autoCompleteTextViewCountry = view.findViewById(R.id.autoCompleteTextViewCountry);
		final AutoCompleteTextView autoCompleteTextViewSubdivision = view.findViewById(R.id.autoCompleteTextViewSubdivision);
		final Button buttonSetLocation = view.findViewById(R.id.buttonSetLocation);
		
		/*
		 * When an autocomplete item is clicked, this listener updates selected country index
		 * and calls `updateSubdivisionsAutoCompleteItems()`.
		 */
		autoCompleteTextViewCountry.setOnItemClickListener((AdapterView<?> parent, View innerView, int position, long id) -> {
			int index = Countries.getCountryIndex(getContext(), (String) parent.getItemAtPosition(position));
			
			setSelectedCountry(index);
			updateSubdivisionsAutoCompleteItems();
		});
		
		/*
		 * When text changes, this listener sets selected country index to -1.
		 */
		autoCompleteTextViewCountry.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				setSelectedCountry(-1);
			}
		});
		
		/*
		 * When an autocomplete item is clicked, this listener updates the selected subdivision
		 * index.
		 */
		autoCompleteTextViewSubdivision.setOnItemClickListener((AdapterView<?> parent, View innerView, int position, long id) -> {
			int index = Countries.getSubdivisionIndex(getContext(), selectedCountry, (String) parent.getItemAtPosition(position));
			
			setSelectedSubdivision(index);
		});
		
		/*
		 * When text changes, this listener sets the selected subdivision index to -1.
		 */
		autoCompleteTextViewSubdivision.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				setSelectedSubdivision(-1);
			}
		});
		
		/*
		 * When the button is clicked, this listener checks if a country and a subdivision are
		 * selected, sets `viewModel` to reflect selected country and subdivision, and dismisses
		 * the dialog.
		 */
		buttonSetLocation.setOnClickListener((View v) -> {
			if (selectedCountry == -1 || selectedSubdivision == -1) {
				return;
			}
			
			String countryName = Countries.getCountryName(getContext(), selectedCountry);
			String subdivisionName = Countries.getSubdivisionName(getContext(), selectedCountry, selectedSubdivision);
			
			viewModel = new LocationViewModel(countryName, subdivisionName);
			
			dismiss();
		});
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setCountries(Countries.getCountries(getContext()));
		updateSubdivisionsAutoCompleteItems();
		
		activityRootView = getActivity().findViewById(R.id.scrollViewRoot);
		
		Window window = getDialog().getWindow();
		
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		
		centerDialog();
		
		globalLayoutListener = () -> {
			centerDialog();
		};
		
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
	}
	
	@Override
	public void onDestroyView() {
		if (activityRootView != null && globalLayoutListener != null) {
			activityRootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
		}
		
		super.onDestroyView();
	}
	
	private void centerDialog() {
		Rect visibleRect = new Rect();
		activityRootView.getWindowVisibleDisplayFrame(visibleRect);
		
		int visibleHeightPx = visibleRect.bottom - visibleRect.top;
		
		int dialogHeightPx = dialogRootView.getHeight();
		
		
		Window window = getDialog().getWindow();
		
		WindowManager.LayoutParams lp = window.getAttributes();
		
		if (dialogHeightPx == 0) {
			lp.gravity = Gravity.CENTER;
			lp.y = 0;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		}
		else {
			DisplayMetrics metrics = new DisplayMetrics();
			activityRootView.getDisplay().getMetrics(metrics);
			
			int offsetPxY = visibleHeightPx - dialogHeightPx - visibleRect.top;
			
			int offsetDpY = (int)(offsetPxY / metrics.density);
			
			lp.gravity = Gravity.TOP;
			lp.y = offsetDpY;
			
			if (dialogHeightPx < visibleHeightPx) {
				lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			}
			else {
				lp.height = visibleHeightPx;
			}
		}
		
		window.setAttributes(lp);
	}
	
	private float getHiddenViewHeightDp(View view) {
		int heightPx = view.getHeight();
		
		Rect visibleRect = new Rect();
		view.getWindowVisibleDisplayFrame(visibleRect);
		int visibleHeightPx = visibleRect.height();
		
		int hiddenHeightPx = heightPx - visibleHeightPx;
		
		DisplayMetrics metrics = new DisplayMetrics();
		view.getDisplay().getMetrics(metrics);
		
		return hiddenHeightPx / metrics.density;
	}
	
	private void updateSubdivisionsAutoCompleteItems() {
		subdivisionsAdapter.clear();
		
		if (selectedCountry != -1) {
			String[] subdivisions = Countries.getCountrySubdivisions(getContext(), selectedCountry);
			subdivisionsAdapter.addAll(subdivisions);
		}
		
		subdivisionsAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Ensures that `country` is a key in countriesCities and sets selectedCountry. Sets
	 * selectedCity to null.
	 *
	 * @param countryIndex The country index to be selected.
	 * @throws IllegalArgumentException if `country` is not a key in `countriesCities`.
	 */
	private void setSelectedCountry(int countryIndex) {
		selectedCountry = countryIndex;
		selectedSubdivision = -1;
	}
	
	/**
	 * Ensures that `city` belongs to `selectedCountry` in `countriesCities` sets selectedCity.
	 *
	 * @param subdivisionIndex The subdivision index to be selected.
	 * @throws IllegalArgumentException if `city` does not belong to `selectedCountry`.
	 */
	private void setSelectedSubdivision(int subdivisionIndex) throws IllegalArgumentException {
		if (selectedCountry == -1 && subdivisionIndex != -1) {
			throw new IllegalArgumentException("No country is selected.");
		}
		
		selectedSubdivision = subdivisionIndex;
	}
}
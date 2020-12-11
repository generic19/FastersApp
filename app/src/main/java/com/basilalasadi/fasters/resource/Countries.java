package com.basilalasadi.fasters.resource;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

import com.basilalasadi.fasters.R;

import java.util.Arrays;
import java.util.LinkedHashMap;


 /**
 * Static class for loading countries of the world and their subdivisions from resources.
 */
abstract public class Countries {
	
	 /**
	  * Gets all countries and their subdivisions from resources.
	  *
	  * @param context The current context.
	  * @return Linked hash map of countries and their subdivisions. Keys are sorted, as well as
	  *         individual string arrays.
	  */
	public static LinkedHashMap<String, String[]> getAllCountriesSubdivisions(Context context) {
		LinkedHashMap<String, String[]> countriesSubdivisions = new LinkedHashMap<>();
		
		Resources res = context.getResources();
		
		String[] countries = res.getStringArray(R.array.countries);
		
		int[] countriesIds;
		{
			TypedArray ta = res.obtainTypedArray(R.array.country_subdivisions_ids);
			
			countriesIds = new int[ta.length()];
			
			for (int i = 0; i < ta.length(); i++) {
				countriesIds[i] = ta.getResourceId(i, 0);
			}
			
			ta.recycle();
		}
		
		for (int i = 0; i < countries.length; i++) {
			countriesSubdivisions.put(countries[i], res.getStringArray(countriesIds[i]));
		}
		
		return countriesSubdivisions;
	}
	
	/**
	 * Gets all countries' names.
	 *
	 * @param context   The current context.
	 * @return          A sorted string array of all countries' names. Index of each element can be
	 *                  used in `getCountrySubdivisions()` and `getCountryName()`.
	 */
	public static String[] getCountries(Context context) {
		return context.getResources().getStringArray(R.array.countries);
	}
	
	 /**
	  * Gets country's name from its index.
	  *
	  * @param context   The current context.
	  * @param index     The index of the country.
	  * @return          Name of the country.
	  */
	public static String getCountryName(Context context, int index) {
		return context.getResources().getStringArray(R.array.countries)[index];
	}
	
	 /**
	  * Gets subdivision's name from country index and subdivision index.
	  *
	  * @param context            The current context.
	  * @param countryIndex       Index of the country intended subdivision belongs to.
	  * @param subdivisionIndex   Index of intended subdivision.
	  * @return                   Name of the subdivision.
	  */
	public static String getSubdivisionName(Context context, int countryIndex, int subdivisionIndex) {
		Resources res = context.getResources();
		return res.getStringArray(getCountrySubdivisionsResourceId(res, countryIndex))[subdivisionIndex];
	}
	
	 /**
	  * Gets country's subdivisions.
	  *
	  * @param context   The current index.
	  * @param index     The index of the country.
	  * @return          A sorted string array of the country's subdivisions.
	  */
	public static String[] getCountrySubdivisions(Context context, int index) {
		Resources res = context.getResources();
		return res.getStringArray(getCountrySubdivisionsResourceId(res, index));
	}
	
	 /**
	  * Searches for {@code countryName} in countries and returns its index, or {@code -1}
	  * if not found.
	  *
	  * @param context       The current context.
	  * @param countryName   Country name.
	  * @return              Index of country or {@code -1} if not found.
	  */
	public static int getCountryIndex(Context context, String countryName) {
		String[] countries = context.getResources().getStringArray(R.array.countries);
		
		int i = Arrays.binarySearch(countries, countryName);
		return i >= 0? i : -1;
	}
	
	 /**
	  * Searches for {@code subdivisionName} in the subdivisions of country and returns its index,
	  * or {@code -1} if not found.
	  *
	  * @param context           The current context.
	  * @param countryIndex      Country index of the subdivision.
	  * @param subdivisionName   Subdivision name.
	  * @return                  Index of subdivision or {@code -1} if not found.
	  */
	public static int getSubdivisionIndex(Context context, int countryIndex, String subdivisionName) {
		Resources res = context.getResources();
		String[] subdivisions = res.getStringArray(getCountrySubdivisionsResourceId(res, countryIndex));
		
		int i = Arrays.binarySearch(subdivisions, subdivisionName);
		return i >= 0? i : -1;
	}
	
	 /**
	  * Gets the resource id of the country subdivisions string array.
	  *
	  * @param res            Resources object.
	  * @param countryIndex   Index of country.
	  * @return               Resource id of country subdivisions string array.
	  */
	private static int getCountrySubdivisionsResourceId(Resources res, int countryIndex) {
		TypedArray ta = res.obtainTypedArray(R.array.country_subdivisions_ids);
		int subdivisionsId =  ta.getResourceId(countryIndex, 0);
		ta.recycle();
		
		return subdivisionsId;
	}
}

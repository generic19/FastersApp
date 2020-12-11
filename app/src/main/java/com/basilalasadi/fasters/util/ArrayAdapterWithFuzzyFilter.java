package com.basilalasadi.fasters.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ArrayAdapterWithFuzzyFilter<T> extends BaseAdapter implements Filterable, ThemedSpinnerAdapter {
	
	protected final Object lock = new Object();
	
	protected final LayoutInflater inflater;
	
	protected final Context context;
	
	protected final int layoutResource;
	
	protected int dropdownResource;
	
	protected List<T> objects;
	
	protected boolean areObjectsFromResources;
	
	protected int fieldId = 0;
	
	protected boolean notifyOnChange = true;
	
	protected ArrayList<T> originalValues;
	
	protected LayoutInflater dropdownInflater;
	
	protected FuzzyArrayFilter filter;
	
	
	public ArrayAdapterWithFuzzyFilter(@NonNull Context context, @LayoutRes int layoutResource) {
		this(context, layoutResource, 0, new ArrayList<T>());
	}
	
	public ArrayAdapterWithFuzzyFilter(@NonNull Context context, @LayoutRes int layoutResource,
			@IdRes int textViewResourceId) {
		
		this(context, layoutResource, textViewResourceId, new ArrayList<T>());
	}
	
	public ArrayAdapterWithFuzzyFilter(@NonNull Context context, @LayoutRes int layoutResource,
			@NonNull T[] objects) {
		
		this(context, layoutResource, 0, objects);
	}
	
	public ArrayAdapterWithFuzzyFilter(@NonNull Context context, @LayoutRes int layoutResource,
			@IdRes int textViewResourceId, @NonNull T[] objects) {
		
		this(context, layoutResource, textViewResourceId, Arrays.asList(objects));
	}
	
	public ArrayAdapterWithFuzzyFilter(@NonNull Context context, @LayoutRes int layoutResource,
			@NonNull List<T> objects) {
		
		this(context, layoutResource, 0, objects);
	}
	
	public ArrayAdapterWithFuzzyFilter(@NonNull Context context, @LayoutRes int layoutResource,
			@IdRes int textViewResourceId, @NonNull List<T> objects) {
		
		this(context, layoutResource, textViewResourceId, objects, false);
	}
	
	private ArrayAdapterWithFuzzyFilter(@NonNull Context context, @LayoutRes int layoutResource,
			@IdRes int textViewResourceId, @NonNull List<T> objects,
			boolean areObjectsFromResources) {
		
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.layoutResource = this.dropdownResource = layoutResource;
		this.objects = objects;
		this.areObjectsFromResources = areObjectsFromResources;
		this.fieldId = textViewResourceId;
	}
	
	public void add(@Nullable T object) {
		synchronized (lock) {
			if (originalValues != null) {
				originalValues.add(object);
			} else {
				objects.add(object);
			}
			areObjectsFromResources = false;
		}
		if (notifyOnChange) notifyDataSetChanged();
	}
	
	public void addAll(@NonNull Collection<? extends T> collection) {
		synchronized (lock) {
			if (originalValues != null) {
				originalValues.addAll(collection);
			} else {
				objects.addAll(collection);
			}
			areObjectsFromResources = false;
		}
		if (notifyOnChange) notifyDataSetChanged();
	}
	
	
	public void addAll(@NonNull T[] arr) {
		synchronized (lock) {
			if (originalValues != null) {
				originalValues.addAll(Arrays.asList(arr));
			} else {
				objects.addAll(Arrays.asList(arr));
			}
			areObjectsFromResources = false;
		}
		if (notifyOnChange) notifyDataSetChanged();
	}
	
	public void clear() {
		synchronized (lock) {
			if (originalValues != null) {
				originalValues.clear();
			} else {
				objects.clear();
			}
			areObjectsFromResources = false;
		}
		if (notifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Finds the index of the provided object in the original objects array.
	 *
	 * @param  object   Object to find.
	 * @return          The index of the first occurrence of the specified element in this list,
	 *                  or -1 if this list does not contain the element.
	 */
	public int indexOf(T object) {
		synchronized (lock) {
			if (originalValues == null) {
				return objects.indexOf(object);
			}
			else {
				return originalValues.indexOf(object);
			}
		}
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		notifyOnChange = true;
	}
	
	public void setNotifyOnChange(boolean notifyOnChange) {
		this.notifyOnChange = notifyOnChange;
	}
	
	public @NonNull Context getContext() {
		return context;
	}
	
	@Override
	public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
		if (theme == null) {
			dropdownInflater = null;
		}
		else if (theme == inflater.getContext().getTheme()) {
			dropdownInflater = inflater;
		}
		else {
			final Context context = new ContextThemeWrapper(this.context, theme);
			dropdownInflater = LayoutInflater.from(context);
		}
	}
	
	@Nullable
	@Override
	public Resources.Theme getDropDownViewTheme() {
		if (dropdownInflater == null) {
			return null;
		}
		else {
			return dropdownInflater.getContext().getTheme();
		}
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		final LayoutInflater inflater = dropdownInflater == null? this.inflater : dropdownInflater;
		return createViewFromResource(inflater, position, convertView, parent, dropdownResource);
	}
	
	private @NonNull View createViewFromResource(@NonNull LayoutInflater inflater, int position,
			@Nullable View convertView, @NonNull ViewGroup parent, int resource) {
		
		final View view;
		final TextView text;
		
		if (convertView == null) {
			view = inflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}
		
		try {
			if (fieldId == 0) {
				//  If no custom field is assigned, assume the whole resource is a TextView
				text = (TextView) view;
			} else {
				//  Otherwise, find the TextView field within the layout
				text = view.findViewById(fieldId);
				
				if (text == null) {
					throw new RuntimeException("Failed to find view with ID "
							+ context.getResources().getResourceName(fieldId)
							+ " in item layout");
				}
			}
		} catch (ClassCastException e) {
			Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
			throw new IllegalStateException(
					"ArrayAdapter requires the resource ID to be a TextView", e);
		}
		
		//noinspection unchecked
		final T item = (T) getItem(position);
		
		if (item instanceof CharSequence) {
			text.setText((CharSequence) item);
		} else {
			text.setText(item.toString());
		}
		
		return view;
	}
	
	@Override
	public int getCount() {
		return objects.size();
	}
	
	@Override
	public T getItem(int position) {
		return objects.get(position);
	}
	
	public int getPosition(T item) {
		return objects.indexOf(item);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(inflater, position, convertView, parent, layoutResource);
	}
	
	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new FuzzyArrayFilter();
		}
		return filter;
	}
	
	
	protected class FuzzyArrayFilter extends Filter {
		
		@Override
		protected FilterResults performFiltering(CharSequence lookupValue) {
			
			final FilterResults results = new FilterResults();
			
			if (originalValues == null) {
				synchronized (lock) {
					originalValues = new ArrayList<>(objects);
				}
			}
			
			
			if (lookupValue == null || lookupValue.length() == 0) {
				final ArrayList<T> values;
				
				synchronized (lock) {
					values = new ArrayList<>(originalValues);
				}
				
				results.values = values;
				results.count = values.size();
			} else {
				final ArrayList<T> sourceValues;
				
				synchronized (lock) {
					sourceValues = new ArrayList<T>(originalValues);
				}
				
				final ArrayList<T> firstDegreeValues = new ArrayList<>();
				final ArrayList<T> secondDegreeValues = new ArrayList<>();
				final ArrayList<T> thirdDegreeValues = new ArrayList<>();
				
				final String lookupValueString = lookupValue.toString().toLowerCase();
				
				for (T value : sourceValues) {
					String valueString = value.toString().toLowerCase();
					
					if (valueString.startsWith(lookupValueString)) {
						firstDegreeValues.add(value);
					} else if (valueString.endsWith(lookupValueString)) {
						secondDegreeValues.add(value);
					} else if (valueString.contains(lookupValueString)) {
						thirdDegreeValues.add(value);
					}
				}
				
				final ArrayList<T> values = new ArrayList<>();
				
				values.addAll(firstDegreeValues);
				values.addAll(secondDegreeValues);
				values.addAll(thirdDegreeValues);
				
				results.values = values;
				results.count = values.size();
			}
			
			return results;
		}
		
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			//noinspection unchecked
			objects = (List<T>) results.values;
			
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}

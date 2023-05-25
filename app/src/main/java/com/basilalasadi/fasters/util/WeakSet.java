package com.basilalasadi.fasters.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class WeakSet<T> implements Set<T> {
	private final HashSet<WeakReference<T>> references = new HashSet<>();
	private final ReferenceQueue<T> refQueue = new ReferenceQueue<>();
	
	public WeakSet() {}
	
	private void clearStaleReferences() {
		for (Reference<? extends T> ref = refQueue.poll(); ref != null; ref = refQueue.poll()) {
			synchronized (refQueue) {
				references.remove(ref);
			}
		}
	}
	
	@Override
	public int size() {
		clearStaleReferences();
		return references.size();
	}
	
	@Override
	public boolean isEmpty() {
		return references.isEmpty();
	}
	
	@Override
	public boolean contains(@Nullable Object o) {
		clearStaleReferences();
		return references.contains(o);
	}
	
	@NonNull
	@Override
	public Iterator<T> iterator() {
		clearStaleReferences();
		
		final Iterator<WeakReference<T>> iterator = references.iterator();
		
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
			
			@Override
			public T next() {
				return iterator.next().get();
			}
		};
	}
	
	@NonNull
	@Override
	public Object[] toArray() {
		final Iterator<T> iterator = iterator();
		Object[] arr = new Object[references.size()];
		
		for (int i = 0; i < arr.length; i++) {
			arr[i] = iterator.next();
		}
		
		return arr;
	}
	
	@NonNull
	@Override
	public <T1> T1[] toArray(@NonNull T1[] a) {
		final Iterator<T> iterator = iterator();
		T1[] arr = (T1[]) Array.newInstance(a.getClass().getComponentType(), references.size());
		
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (T1) iterator.next();
		}
		
		return arr;
	}
	
	@Override
	public boolean add(T item) {
		synchronized (refQueue) {
			WeakReference<T> ref = new WeakReference<>(item, refQueue);
			return references.add(ref);
		}
	}
	
	@Override
	public boolean remove(@Nullable Object o) {
		return references.remove(o);
	}
	
	/**
	 * Unsupported operation
	 */
	@Override
	public boolean containsAll(@NonNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(@NonNull Collection<? extends T> c) {
		boolean b = false;
		for (T t : c) {
			b |= add(t);
		}
		return b;
	}
	
	@Override
	public boolean retainAll(@NonNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeAll(@NonNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear() {
		references.clear();
	}
}

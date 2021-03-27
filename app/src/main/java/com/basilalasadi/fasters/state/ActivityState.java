package com.basilalasadi.fasters.state;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.HashSet;


@SuppressWarnings("unchecked")
public abstract class ActivityState<A extends AppCompatActivity> {
	private static final HashMap<Class<? extends AppCompatActivity>, ActivityState<?>> states = new HashMap<>();
	
	private static final Application.ActivityLifecycleCallbacks lifecycleCallback = new Application.ActivityLifecycleCallbacks() {
		@Override
		public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
			ActivityState<?> state = stateOf(activity);
			
			if (state != null) {
				state.onCreateActivity(savedInstanceState);
			}
		}
		
		@Override
		public void onActivityStarted(@NonNull Activity activity) {
			ActivityState<?> state = stateOf(activity);
			if (state != null) {
				state.onStartActivity();
			}
		}
		
		@Override
		public void onActivityResumed(@NonNull Activity activity) {
			ActivityState<?> state = stateOf(activity);
			if (state != null) {
				state.onResumeActivity();
			}
		}
		
		@Override
		public void onActivityPaused(@NonNull Activity activity) {
			ActivityState<?> state = stateOf(activity);
			if (state != null) {
				state.onPauseActivity();
			}
		}
		
		@Override
		public void onActivityStopped(@NonNull Activity activity) {
			ActivityState<?> state = stateOf(activity);
			if (state != null) {
				state.onStopActivity();
			}
		}
		
		@Override
		public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
			ActivityState<?> state = stateOf(activity);
			if (state != null) {
				state.onSaveInstanceState(outState);
			}
		}
		
		@Override
		public void onActivityDestroyed(@NonNull Activity activity) {
			ActivityState<?> state = stateOf(activity);
			if (state != null) {
				state.onDestroyActivity();
			}
		}
	};
	
	public static HashSet<Integer> boundApplicationsHashes = new HashSet<>();
	
	public static void bindApplication(Application application) {
		Integer hash = application.hashCode();
		if (!boundApplicationsHashes.contains(hash)) {
			application.registerActivityLifecycleCallbacks(lifecycleCallback);
			boundApplicationsHashes.add(hash);
		}
	}
	
	public static void unbindApplication(Application application) {
		Integer hash = application.hashCode();
		if (boundApplicationsHashes.contains(hash)) {
			application.unregisterActivityLifecycleCallbacks(lifecycleCallback);
			boundApplicationsHashes.remove(hash);
		}
	}
	
	public static <A extends AppCompatActivity> ActivityState<A> stateOf(Class<A> _class) {
		//noinspection unchecked
		return (ActivityState<A>) states.get(_class);
	}
	
	public static <A extends AppCompatActivity> ActivityState<A> stateOf(Activity activity) {
		//noinspection unchecked
		return (ActivityState<A>) states.get(activity.getClass());
	}
	
	
	protected abstract void onCreateActivity(Bundle savedInstanceState);
	protected abstract void onDestroyActivity();
	protected abstract void onStartActivity();
	protected abstract void onStopActivity();
	protected abstract void onResumeActivity();
	protected abstract void onPauseActivity();
	protected abstract void onSaveInstanceState(Bundle outState);
	
	
	protected ActivityState(Class<A> _class) {
		onCreateState(_class);
	}
	
	@CallSuper
	protected void onCreateState(Class<A> _class) {
		states.put(_class, this);
	}
	
	@CallSuper
	protected void onDestroyState() {
		states.remove(this.getClass());
	}
	
	@Override
	protected void finalize() throws Throwable {
		onDestroyState();
		super.finalize();
	}
}

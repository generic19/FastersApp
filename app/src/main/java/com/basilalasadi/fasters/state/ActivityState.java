package com.basilalasadi.fasters.state;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;


public abstract class ActivityState<A extends AppCompatActivity> {
	private static HashMap<Class<? extends AppCompatActivity>, ActivityState<?>> states = new HashMap<>();
	
	protected void onCreateActivity(Bundle savedInstanceState) {}
	protected void onDestroyActivity() {}
	protected void onStartActivity() {}
	protected void onStopActivity() {}
	protected void onResumeActivity() {}
	protected void onPauseActivity() {}
	protected void onSaveInstanceState(Bundle outState) {}
	
	private A activity;
	
	public static <A extends AppCompatActivity> ActivityState<A> stateOf(Class<A> _class) {
		//noinspection unchecked
		return (ActivityState<A>) states.get(_class);
	}
	
	public ActivityState(Class<A> _class) {
		onCreateState(_class);
	}
	
	@CallSuper
	protected void onCreateState(Class<A> _class) {
		states.put(_class, this);
	}
	
	@CallSuper
	protected void onDestroyState() {
		this.unbindActivity();
		states.remove(this.getClass());
	}
	
	@Override
	protected void finalize() throws Throwable {
		onDestroyState();
		super.finalize();
	}
	
	private final Application.ActivityLifecycleCallbacks lifecycleCallback = new Application.ActivityLifecycleCallbacks() {
		@Override
		public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
			onCreateActivity(savedInstanceState);
		}
		
		@Override
		public void onActivityStarted(@NonNull Activity activity) {
			onStartActivity();
		}
		
		@Override
		public void onActivityResumed(@NonNull Activity activity) {
			onResumeActivity();
		}
		
		@Override
		public void onActivityPaused(@NonNull Activity activity) {
			onPauseActivity();
		}
		
		@Override
		public void onActivityStopped(@NonNull Activity activity) {
			onStopActivity();
		}
		
		@Override
		public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
			onSaveInstanceState(outState);
		}
		
		@Override
		public void onActivityDestroyed(@NonNull Activity activity) {
			onDestroyActivity();
			unbindActivity();
		}
	};
	
	public final void bindActivity(A activity) {
		this.activity = activity;
		activity.getApplication().registerActivityLifecycleCallbacks(lifecycleCallback);
	}
	
	public final void unbindActivity() {
		if (activity != null) {
			activity.getApplication().unregisterActivityLifecycleCallbacks(lifecycleCallback);
			activity = null;
		}
	}
}

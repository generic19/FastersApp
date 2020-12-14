package com.basilalasadi.fasters.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.SparseArray;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class PermissionsActivity extends Activity {
	
	public interface PermissionsCallback {
		void onGranted();
		void onDenied(String[] permissionsDenied);
		void onCancelled();
	}
	
	private final SparseArray<PermissionsCallback> callbacks = new SparseArray<>();
	private int nextCallbackId = 1;
	
	@Override
	@CallSuper
	public synchronized void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		PermissionsCallback callback = callbacks.get(requestCode);
		
		if (callback != null) {
			if (permissions.length == 0) {
				callbacks.get(requestCode).onCancelled();
			}
			else {
				ArrayList<String> deniedPermissions = new ArrayList<>();
				
				for (int i = 0; i < grantResults.length; i++) {
					if (grantResults[i] != PERMISSION_GRANTED) {
						deniedPermissions.add(permissions[i]);
					}
				}
				
				if (deniedPermissions.size() > 0) {
					callbacks.get(requestCode).onDenied(deniedPermissions.toArray(new String[0]));
				}
				else {
					callbacks.get(requestCode).onGranted();
				}
			}
			
			callbacks.remove(requestCode);
		}
	}
	
	public synchronized int submitPermissionsRequest(String[] permissions, PermissionsCallback callback) {
		int id = nextCallbackId++;
		callbacks.append(id, callback);
		
		requestPermissions(permissions, id);
		return id;
	}
	
	public synchronized void cancelPermissionsRequest(int id) {
		callbacks.remove(id);
	}
	
	public boolean requestPermissionsSync(String[] permissions) throws InterruptedException {
		BlockingQueue<Boolean> results = new ArrayBlockingQueue<>(1);
		
		submitPermissionsRequest(permissions, new PermissionsCallback() {
			@Override
			public void onGranted() {
				results.offer(true);
			}
			
			@Override
			public void onDenied(String[] permissionsDenied) {
				results.offer(false);
			}
			
			@Override
			public void onCancelled() {
				results.offer(false);
			}
		});
		
		return results.take();
	}
	
	public boolean requestPermissionSync(String permission) throws InterruptedException {
		return requestPermissionsSync(new String[]{ permission });
	}
	
	public boolean ensurePermission(String permission) throws InterruptedException {
		if (checkSelfPermission(permission) == PERMISSION_GRANTED) {
			return true;
		}
		else {
			return requestPermissionSync(permission);
		}
	}
}

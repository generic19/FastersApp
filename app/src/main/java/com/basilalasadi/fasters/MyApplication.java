package com.basilalasadi.fasters;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.basilalasadi.fasters.database.CitiesDatabase;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
	private static MyApplication instance;
	
	private final Object mutex = new Object();
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		//TODO Uncomment this
//		try {
//			CitiesDatabase.getInstance(this);
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public static MyApplication getInstance() {
		return instance;
	}
}

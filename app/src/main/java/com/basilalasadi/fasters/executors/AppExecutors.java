package com.basilalasadi.fasters.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AppExecutors {
	public static final ExecutorService ioExecutor = Executors.newCachedThreadPool();
	public static final ExecutorService cpuExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
}

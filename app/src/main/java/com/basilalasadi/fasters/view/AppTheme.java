package com.basilalasadi.fasters.view;

public enum AppTheme {
	Morning, Evening;
	
	public static AppTheme fromOrdinal(int i) {
		switch (i) {
			case 0:
				return Morning;
			case 1:
				return Evening;
			default:
				throw new IllegalArgumentException("Index out of range.");
		}
	}
}

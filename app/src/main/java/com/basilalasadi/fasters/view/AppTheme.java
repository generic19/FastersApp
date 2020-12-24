package com.basilalasadi.fasters.view;

public enum AppTheme {
	Morning, Evening;
	
	public static final int THEME_MORNING = 0;
	public static final int THEME_EVENING = 1;
	
	public static AppTheme fromOrdinal(int i) {
		switch (i) {
			case THEME_MORNING:
				return Morning;
				
			case THEME_EVENING:
				return Evening;
				
			default:
				throw new IllegalArgumentException("Index out of range.");
		}
	}
}

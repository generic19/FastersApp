package com.basilalasadi.fasters.util;

import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;


public abstract class TimeProvider {
	private static Duration offset = Duration.ZERO;
	
	public static void setDateTime(ZonedDateTime datetime) {
		offset = Duration.between(ZonedDateTime.now(), datetime);
	}
	
	public static ZonedDateTime now() {
		ZonedDateTime now = ZonedDateTime.now();
		return now.plus(offset);
	}
}

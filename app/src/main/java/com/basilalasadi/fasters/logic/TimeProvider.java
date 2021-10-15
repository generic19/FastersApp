package com.basilalasadi.fasters.logic;


import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;


public abstract class TimeProvider {
	private static Duration offset = Duration.ZERO;
	
	public static void setDateTime(ZonedDateTime datetime) {
		offset = Duration.between(ZonedDateTime.now(), datetime);
	}
	
	public static ZonedDateTime getDateTime() {
		ZonedDateTime now = ZonedDateTime.now();
		return (ZonedDateTime)(offset.addTo(now));
	}
	
	public static LocalDateTime getLocalDateTime() {
		LocalDateTime now = LocalDateTime.now();
		return (LocalDateTime)(offset.addTo(now));
	}
	
	public static ZonedDateTime toActualDateTime(ZonedDateTime datetime) {
		return (ZonedDateTime) offset.subtractFrom(datetime);
	}
}

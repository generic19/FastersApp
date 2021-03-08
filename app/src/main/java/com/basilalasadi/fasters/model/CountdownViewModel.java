package com.basilalasadi.fasters.model;

import android.os.Bundle;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.ChronoField;


public final class CountdownViewModel {
	public static final String TAG = "countdownViewModel";
	public static final int TIMING_FAJR = 0;
	public static final int TIMING_DUHR = 1;
	public static final int TIMING_ASR = 2;
	public static final int TIMING_MAGRIB = 3;
	public static final int TIMING_ISHA = 4;
	
	public static final int FLAG_DATA_AVAILABLE = 0x01;
	public static final int FLAG_DATA_LOADING = 0x02;
	
	public static final int ERROR_NO_LOCATION = 0x10;
	public static final int ERROR_INVALID_SETTINGS = 0x20;
	
	private static final String KEY_FLAGS = "flags";
	private static final String KEY_IS_EVENING = "isEvening";
	private static final String KEY_COUNT_DOWN_END_TIME = "countDownEndTime";
	private static final String KEY_COUNT_DOWN_PROGRESS = "countDownProgress";
	private static final String KEY_LOCATION = "location";
	private static final String KEY_TIME_TILL_NEXT_PRAYER = "timeTillNextPrayer";
	private static final String KEY_NEXT_PRAYER = "nextPrayer";
	private static final String KEY_PRAYER_TIMES = "prayerTimes";
	private static final String KEY_NEXT_PRAYER_TIME = "nextPrayerTime";
	
	private static final DateTimeFormatter TIME_FORMATTER_HH_MM_AMPM =
			new DateTimeFormatterBuilder().parseCaseInsensitive()
					.appendValue(ChronoField.HOUR_OF_AMPM)
					.appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR)
					.appendLiteral(' ')
					.appendValue(ChronoField.AMPM_OF_DAY)
					.toFormatter();
	
	public final int flags;
	public final boolean isEvening;
	public final long countDownEndTime;
	public final double countDownProgress;
	public final String location;
	public final long timeTillNextPrayer;
	public final String nextPrayer;
	public final double[] prayerTimes;
	public final int nextPrayerTime;
	
	public CountdownViewModel(int flags, boolean isEvening, long countDownEndTime,
			double countDownProgress, String location, long timeTillNextPrayer, String nextPrayer,
			double[] prayerTimes, int nextPrayerTime) {
		
		this.flags = flags;
		this.isEvening = isEvening;
		this.countDownEndTime = countDownEndTime;
		this.countDownProgress = countDownProgress;
		this.location = location;
		this.timeTillNextPrayer = timeTillNextPrayer;
		this.nextPrayer = nextPrayer;
		this.prayerTimes = prayerTimes;
		this.nextPrayerTime = nextPrayerTime;
	}
	
	public CountdownViewModel(Bundle bundle) throws IllegalArgumentException {
		Bundle b = bundle.getBundle(TAG);
		
		if (b == null) {
			throw new IllegalArgumentException("Bundle does not contain view model data.");
		}
		
		this.flags = b.getInt(KEY_FLAGS);
		this.isEvening = b.getBoolean(KEY_IS_EVENING);
		this.countDownEndTime = b.getLong(KEY_COUNT_DOWN_END_TIME);
		this.countDownProgress = b.getDouble(KEY_COUNT_DOWN_PROGRESS);
		this.location = b.getString(KEY_LOCATION);
		this.timeTillNextPrayer = b.getLong(KEY_TIME_TILL_NEXT_PRAYER);
		this.nextPrayer = b.getString(KEY_NEXT_PRAYER);
		this.prayerTimes = b.getDoubleArray(KEY_PRAYER_TIMES);
		this.nextPrayerTime = b.getInt(KEY_NEXT_PRAYER_TIME);
	}
	
	public CountdownViewModel(int flags) {
		this.flags = flags;
		this.isEvening = false;
		this.countDownEndTime = -1;
		this.countDownProgress = -1;
		this.location = null;
		this.timeTillNextPrayer = -1;
		this.nextPrayer = null;
		this.prayerTimes = null;
		this.nextPrayerTime = -1;
	}
	
	public boolean isDataAvailable() {
		return (flags & FLAG_DATA_AVAILABLE) != 0;
	}
	
	public boolean isDataLoading() {
		return (flags & FLAG_DATA_LOADING) != 0;
	}
	
	public boolean isError() {
		return (flags & 0xf0) != 0;
	}
	
	public int getError() {
		return flags & 0xf0;
	}
	
	public static String getFormattedTime(LocalDateTime time) {
		return TIME_FORMATTER_HH_MM_AMPM.format(time);
	}
	
	public static String getFormattedTime(LocalTime time) {
		return TIME_FORMATTER_HH_MM_AMPM.format(time);
	}
	
	public static String getFormattedTime(double days) {
		return getFormattedTime(daysToLocalTime(days));
	}
	
	public static LocalTime daysToLocalTime(double days) {
		return LocalTime.ofSecondOfDay((long) (days * 24 * 60 * 60));
	}
	
	public static double toDays(LocalTime time) {
		return time.toSecondOfDay() / 60d / 60d / 24d;
	}
	
	public void putIntoBundle(Bundle bundle) {
		Bundle b = new Bundle();
		
		b.putInt(KEY_FLAGS, flags);
		b.putBoolean(KEY_IS_EVENING, isEvening);
		b.putLong(KEY_COUNT_DOWN_END_TIME, countDownEndTime);
		b.putDouble(KEY_COUNT_DOWN_PROGRESS, countDownProgress);
		b.putString(KEY_LOCATION, location);
		b.putLong(KEY_TIME_TILL_NEXT_PRAYER, timeTillNextPrayer);
		b.putString(KEY_NEXT_PRAYER, nextPrayer);
		b.putDoubleArray(KEY_PRAYER_TIMES, prayerTimes);
		b.putInt(KEY_NEXT_PRAYER_TIME, nextPrayerTime);
		
		bundle.putBundle(TAG, b);
	}
	
	public int[] getCurrentCountdown(LocalDateTime datetime) {
		LocalTime endTime = LocalTime.ofSecondOfDay((long) (countDownEndTime * 24 * 60 * 60));
		LocalDateTime endDatetime = LocalDateTime.of(datetime.toLocalDate(), endTime);
		
		Duration timeLeft = Duration.between(datetime, endDatetime);
		
		return new int[]{(int) timeLeft.toHours(), timeLeft.toMinutesPart(), timeLeft.toSecondsPart()};
	}
	
	public String getFormattedTiming(int timing) {
		return getFormattedTime(prayerTimes[timing]);
	}
	
	public String getFormattedNextTiming(LocalDateTime now) {
		return getFormattedTime(prayerTimes[nextPrayerTime]);
	}
}

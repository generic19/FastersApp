package com.basilalasadi.fasters.model;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.ChronoField;

import java.util.Arrays;
import java.util.Locale;


public final class CountdownViewModel {
	public static class CountdownDisplay {
		public final String hours;
		public final String minute;
		public final String second;
		
		CountdownDisplay(int hours, int minute, int second) {
			this.hours = String.valueOf(hours);
			this.minute = String.valueOf(minute);
			this.second = String.valueOf(second);
		}
		
		CountdownDisplay(int seconds) {
			int hours = seconds / 3600;
			int minute = seconds / 60 - hours * 60;
			int second = seconds - hours * 3600 - minute * 60;
			
			this.hours = String.format(Locale.US, "%02d", hours);
			this.minute = String.format(Locale.US, "%02d", minute);
			this.second = String.format(Locale.US,"%02d", second);
		}
	}
	
	
	public static final String TAG = "countdownViewModel";
	
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
	private static final String KEY_ZONE = "zone";
	
	private static final DateTimeFormatter TIME_FORMATTER_HH_MM_AMPM =
			new DateTimeFormatterBuilder().parseCaseInsensitive()
					.appendValue(ChronoField.CLOCK_HOUR_OF_AMPM)
					.appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
					.appendLiteral(' ')
					.appendText(ChronoField.AMPM_OF_DAY)
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
	public final int zone;
	
	public CountdownViewModel(int flags, boolean isEvening, long countDownEndTime,
			double countDownProgress, String location, long timeTillNextPrayer, String nextPrayer,
			double[] prayerTimes, int nextPrayerTime, int zone) {
		
		this.flags = flags;
		this.isEvening = isEvening;
		this.countDownEndTime = countDownEndTime;
		this.countDownProgress = countDownProgress;
		this.location = location;
		this.timeTillNextPrayer = timeTillNextPrayer;
		this.nextPrayer = nextPrayer;
		this.prayerTimes = prayerTimes;
		this.nextPrayerTime = nextPrayerTime;
		this.zone = zone;
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
		this.zone = b.getInt(KEY_ZONE);
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
		this.zone = 0;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("CountdownViewModel(flags: {");
		
		if (isDataAvailable()) {
			buf.append("data_available ");
		}
		if (isDataLoading()) {
			buf.append("data_loading ");
		}
		if (isError()) {
			switch (getError()) {
				case ERROR_NO_LOCATION:
					buf.append("error_no_location ");
					break;
				
				case ERROR_INVALID_SETTINGS:
					buf.append("error_invalid_settings ");
					break;
					
				default:
					buf.append("error_")
							.append(Integer.toHexString(getError()).toUpperCase())
							.append(" ");
			}
		}
		
		if (buf.charAt(buf.length() - 1) == ' ') {
			buf.deleteCharAt(buf.length() - 1);
		}
		
		buf.append("}");
		
		buf.append(", isEvening: ").append(isEvening);
		buf.append(", countDownEndTime: ").append(countDownEndTime);
		buf.append(", countDownProgress: ").append(countDownProgress);
		buf.append(", location: ").append(location);
		buf.append(", timeTillNextPrayer: ").append(timeTillNextPrayer);
		buf.append(", nextPrayer: ").append(nextPrayer);
		buf.append(", prayerTimes: ").append(Arrays.toString(prayerTimes));
		buf.append(", nextPrayerTime: ").append(nextPrayerTime);
		buf.append(", zone: ").append(zone);
		
		buf.append(")");
		
		return buf.toString();
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
	
	public static String getFormattedTime(ZonedDateTime time) {
		return TIME_FORMATTER_HH_MM_AMPM.format(time);
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
		b.putInt(KEY_ZONE, zone);
		
		bundle.putBundle(TAG, b);
	}
	
	public CountdownDisplay getCountdownDisplay(ZonedDateTime now) {
		int seconds = (int)(countDownEndTime - now.toEpochSecond());
		return new CountdownDisplay(seconds);
	}
	
	public String[] getFormattedTimings() {
		String[] timings = new String[5];
		
		for (int i = 0; i < 5; i++) {
			timings[i] = formatTiming(prayerTimes[i]);
		}
		
		return timings;
	}
	
	public String getFormattedNextTiming() {
		return formatTiming(prayerTimes[nextPrayerTime]);
	}
	
	public String formatTiming(double timing) {
		LocalTime t = LocalTime.ofSecondOfDay((long)(timing * 3600));
		return TIME_FORMATTER_HH_MM_AMPM.format(t);
	}
}

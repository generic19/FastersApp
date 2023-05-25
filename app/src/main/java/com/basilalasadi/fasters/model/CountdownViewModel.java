package com.basilalasadi.fasters.model;

import com.basilalasadi.fasters.bloc.CountdownBloc;
import com.basilalasadi.fasters.util.TimeProvider;

import org.jetbrains.annotations.NotNull;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.Duration;
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
	public final long countDownStartTime;
	public final long countDownEndTime;
	public final String location;
	public final int nextPrayerIndex;
	public final String nextPrayerName;
	public final double[] prayerTimes;
	public final int nextPrayerId;
	public final int zone;
	public final ZonedDateTime expires;
	
	public CountdownViewModel(int flags, boolean isEvening, long countDownStartTime, long countDownEndTime,
			String location, int nextPrayerIndex, String nextPrayerName, double @NotNull [] prayerTimes,
			int nextPrayerId, int zone, ZonedDateTime expires) {
		
		this.flags = flags;
		this.isEvening = isEvening;
		this.countDownStartTime = countDownStartTime;
		this.countDownEndTime = countDownEndTime;
		this.location = location;
		this.nextPrayerIndex = nextPrayerIndex;
		this.nextPrayerName = nextPrayerName;
		this.prayerTimes = prayerTimes;
		this.nextPrayerId = nextPrayerId;
		this.zone = zone;
		this.expires = expires;
	}
	
	private CountdownViewModel(int flags) {
		this.flags = flags;
		this.isEvening = false;
		this.countDownStartTime = -1;
		this.countDownEndTime = -1;
		this.location = null;
		this.nextPrayerIndex = -1;
		this.nextPrayerName = null;
		this.prayerTimes = null;
		this.nextPrayerId = -1;
		this.zone = 0;
		this.expires = TimeProvider.now().minusSeconds(1);
	}
	
	public static CountdownViewModel dataLoading() {
		return new CountdownViewModel(FLAG_DATA_LOADING);
	}
	
	public static CountdownViewModel errorNoLocation() {
		return new CountdownViewModel(ERROR_NO_LOCATION);
	}
	
	public static CountdownViewModel errorInvalidSettings() {
		return new CountdownViewModel(ERROR_INVALID_SETTINGS);
	}
	
	@NotNull
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
		buf.append(", countDownStartTime: ").append(countDownStartTime);
		buf.append(", countDownEndTime: ").append(countDownEndTime);
		buf.append(", location: ").append(location);
		buf.append(", timeTillNextPrayer: ").append(nextPrayerIndex);
		buf.append(", nextPrayer: ").append(nextPrayerName);
		buf.append(", prayerTimes: ").append(Arrays.toString(prayerTimes));
		buf.append(", nextPrayerTime: ").append(nextPrayerId);
		buf.append(", zone: ").append(zone);
		buf.append(", expires: ").append(expires);
		
		buf.append(")");
		
		return buf.toString();
	}
	
	public String statusString() {
		int status = flags & 0x0f;
		int error = flags & 0xf0;
		
		if (error != 0) {
			switch (error) {
				case ERROR_INVALID_SETTINGS:
					return "ERROR_INVALID_SETTINGS";
					
				case ERROR_NO_LOCATION:
					return "ERROR_NO_LOCATION";
				
				default:
					return "ERROR_CODE_" + error;
			}
		}
		
		switch (status) {
			case FLAG_DATA_AVAILABLE:
				return "FLAG_DATA_AVAILABLE";
				
			case FLAG_DATA_LOADING:
				return "FLAG_DATA_LOADING";
			
			default:
				return "STATUS_CODE_" + status;
		}
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
	
	public double getProgress(ZonedDateTime now) {
		return (now.toEpochSecond() - countDownStartTime) / (double)(countDownEndTime - countDownStartTime);
	}
	
	public CountdownDisplay getCountdownDisplay(ZonedDateTime now) {
		int seconds = (int)(countDownEndTime - now.toEpochSecond());
		return new CountdownDisplay(seconds);
	}
	
	public boolean isExpired() {
		return !TimeProvider.now().isBefore(expires);
	}
	
	public String[] getFormattedTimings() {
		String[] timings = new String[5];
		
		for (int i = 0; i < 5; i++) {
			timings[i] = formatTiming(prayerTimes[i]);
		}
		
		return timings;
	}
	
	public long getTimeTillNextPrayer(ZonedDateTime now) {
		ZonedDateTime nextPrayerTime;
		
		if (nextPrayerIndex == 5) {
			nextPrayerTime = CountdownBloc.datetimeFromTiming(now, prayerTimes[5], 1);
		}
		else {
			nextPrayerTime = CountdownBloc.datetimeFromTiming(now, prayerTimes[nextPrayerId]);
		}
		
		return Duration.between(now, nextPrayerTime).getSeconds();
	}
	
	public String formatTiming(double timing) {
		while (timing < 0) {
			timing += 24;
		}
		while (timing >= 24) {
			timing -= 24;
		}
		
		LocalTime t = LocalTime.ofSecondOfDay((long)(timing * 3600));
		return TIME_FORMATTER_HH_MM_AMPM.format(t);
	}
	
	public ZonedDateTime[] getDayTimings(ZonedDateTime now) {
		ZonedDateTime[] times = new ZonedDateTime[6];
		
		for (int i = 0; i < 5; i++) {
			times[i] = CountdownBloc.datetimeFromTiming(now, prayerTimes[i]);
		}
		times[5] = CountdownBloc.datetimeFromTiming(now, prayerTimes[5], 1);
		
		return times;
	}
}

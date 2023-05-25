package com.basilalasadi.fasters.bloc;

import android.content.Context;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.math.PrayerTimings;
import com.basilalasadi.fasters.math.TimingsMethod;
import com.basilalasadi.fasters.model.CountdownViewModel;
import com.basilalasadi.fasters.logic.settings.SettingsManager;
import com.basilalasadi.fasters.util.TimeProvider;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.chrono.HijrahDate;
import org.threeten.bp.temporal.ChronoField;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Business logic component for a countdown activity.
 */
public class CountdownBloc {
	public static final int EVENT_LOAD_TIMINGS = 1;
	private final Thread thread;
	private final ArrayBlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(10);
	private final StateStreamConsumer consumer;
	
	public CountdownBloc(StateStreamConsumer consumer) {
		this.consumer = consumer;
		
		Runnable handler = () -> {
			try {
				while (true) {
					Event event = eventQueue.poll(60, TimeUnit.SECONDS);
					
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					else if (event == null) {
						continue;
					}
					
					if (event.getEventType() == EVENT_LOAD_TIMINGS) {
						loadTimings((LoadTimingsEvent) event);
					}
				}
			}
			catch (InterruptedException ignored) {
			}
		};
		
		this.thread = new Thread(handler);
		this.thread.start();
	}

	@Override
	protected void finalize() throws Throwable {
		this.thread.interrupt();
		super.finalize();
	}

	public void addEvent(Event event) throws IllegalStateException {
		this.eventQueue.add(event);
	}
	
	private void loadTimings(LoadTimingsEvent event) {
		consumer.onState(CountdownViewModel.dataLoading());
		
		SettingsManager settingsManager = SettingsManager.getInstance(event.context);
		
		SettingsManager.Coordinates coords = settingsManager.getCoordinates(event.context);
		
		if (coords == null) {
			consumer.onState(CountdownViewModel.errorNoLocation());
			return;
		}
		
		SettingsManager.Address address = settingsManager.getAddress(event.context);
		SettingsManager.CustomMethod customMethod = settingsManager.getCustomMethod(event.context);
		
		if (address == null || customMethod == null) {
			consumer.onState(CountdownViewModel.errorInvalidSettings());
			return;
		}
		
		ZonedDateTime now = TimeProvider.now();
		HijrahDate hijriDate = HijrahDate.from(now);
		boolean isRamadan = hijriDate.get(ChronoField.MONTH_OF_YEAR) == 9;
		
		double daysSinceEpoch = PrayerTimings.toDaysSinceEpoch2000(now);
		int timeZone = now.getOffset().getTotalSeconds() / 3600;
		double longitude = coords.longitude;
		double latitude = coords.latitude;
		
		boolean useFixedTimeOffset;
		double fajrAngleDegrees;
		double ishaAngleDegrees = Double.NaN;
		int ishaFixedTimeOffset = Integer.MAX_VALUE;
		boolean useShafaiMethod;
		
		
		if (customMethod.useAutomatic()) {
			TimingsMethod method = TimingsMethod.getDefaultForCountry(address.country);
			
			fajrAngleDegrees = method.getFajrAngleDegrees();
			useFixedTimeOffset = method.usesFixedOffsetForIsha();
			
			if (useFixedTimeOffset) {
				ishaFixedTimeOffset = (int) method.getIshaFixedOffset(isRamadan);
			}
			else {
				ishaAngleDegrees = method.getIshaAngleDegrees();
			}
			
			useShafaiMethod = false;
		}
		else {
			fajrAngleDegrees = customMethod.fajrAngle;
			useFixedTimeOffset = customMethod.useFixedTimeOffset();
			
			if (useFixedTimeOffset) {
				if (isRamadan && customMethod.useRamadanFixedTimeOffset()) {
					ishaFixedTimeOffset = customMethod.ramadanFixedTimeOffsetForIsha;
				}
				else {
					ishaFixedTimeOffset = customMethod.fixedTimeOffsetForIsha;
				}
			}
			else {
				ishaAngleDegrees = customMethod.ishaAngle;
			}
			
			useShafaiMethod = customMethod.useShafaiMethod;
		}
		
		
		double[] timings;
		
		if (useFixedTimeOffset) {
			timings =
					PrayerTimings.getTimings(fajrAngleDegrees, useShafaiMethod, ishaFixedTimeOffset,
							daysSinceEpoch, timeZone, longitude, latitude);
		}
		else {
			timings = PrayerTimings.getTimings(fajrAngleDegrees, useShafaiMethod, ishaAngleDegrees,
					daysSinceEpoch, timeZone, longitude, latitude);
		}
		
		double nextDayFajrTiming =
				PrayerTimings.getFajr(fajrAngleDegrees, daysSinceEpoch + 1, timeZone,
						coords.longitude, coords.latitude);
		
		double lastDayMagribTiming =
				PrayerTimings.getMagrib(daysSinceEpoch - 1, timeZone, coords.longitude, coords.latitude);
		
		
		double timeNow = now.getHour() + now.getMinute() / 60.0 + now.getSecond() / 3600.0;
		boolean isEvening = (timeNow < timings[0] || timeNow > timings[3]);
		
		
		int nextPrayerIndex = 5;
		for (int i = 0; i < 5; i++) {
			if (timeNow < timings[i]) {
				nextPrayerIndex = i;
				break;
			}
		}
		
		int nextPrayerId = nextPrayerIndex == 5 ? 0 : nextPrayerIndex;
		
		long countDownStartTime;
		long countDownEndTime;
		String nextPrayerName;
		
		if (timeNow < timings[0]) {
			countDownStartTime = datetimeFromTiming(now, lastDayMagribTiming, -1).toEpochSecond();
			countDownEndTime = datetimeFromTiming(now, timings[0]).toEpochSecond();
		}
		else if (timeNow < timings[3]) {
			countDownStartTime = datetimeFromTiming(now, timings[0]).toEpochSecond();
			countDownEndTime = datetimeFromTiming(now, timings[3]).toEpochSecond();
		}
		else {
			countDownStartTime = datetimeFromTiming(now, timings[3]).toEpochSecond();
			countDownEndTime = datetimeFromTiming(now, nextDayFajrTiming, 1).toEpochSecond();
		}
		
		int stringId = R.string.loading_lowercase;
		switch (nextPrayerId) {
			case 0:
				stringId = R.string.fajr;
				break;
			case 1:
				stringId = R.string.duhr;
				break;
			case 2:
				stringId = R.string.asr;
				break;
			case 3:
				stringId = R.string.magrib;
				break;
			case 4:
				stringId = R.string.ishaa;
				break;
		}
		
		nextPrayerName = event.context.getString(stringId);
		
		ZonedDateTime expiry;
		
		if (nextPrayerIndex == 5) {
			expiry = datetimeFromTiming(now, 0, 1);
		}
		else {
			expiry = datetimeFromTiming(now, timings[nextPrayerIndex]);
		}
		
		double[] prayerTimes = new double[6];
		
		System.arraycopy(timings, 0, prayerTimes, 0, 5);
		prayerTimes[5] = nextDayFajrTiming;
		
		CountdownViewModel viewModel =
				new CountdownViewModel(CountdownViewModel.FLAG_DATA_AVAILABLE, isEvening, countDownStartTime, countDownEndTime, address.city,
						nextPrayerIndex, nextPrayerName, prayerTimes, nextPrayerId, timeZone, expiry);
		
		consumer.onState(viewModel);
	}
	
	public static ZonedDateTime datetimeFromTiming(ZonedDateTime now, double time) {
		int daysOffset = 0;
		
		while (time < 0) {
			time += 24;
			daysOffset--;
		}
		while (time >= 24) {
			time -= 24;
			daysOffset++;
		}
		
		int hour = (int) time;
		int minute = (int) ((time - hour) * 60);
		int second = (int) ((time - hour - minute / 60.0) * 3600);
		
		return now
				.plusDays(daysOffset)
				.withHour(hour)
				.withMinute(minute)
				.withSecond(second);
	}
	
	public static ZonedDateTime datetimeFromTiming(ZonedDateTime now, double time, int daysOffset) {
		return datetimeFromTiming(now, time).plusDays(daysOffset);
	}
	
	/**
	 * Consumer interface for CountdownBloc.
	 */
	public interface StateStreamConsumer {
		/**
		 * State handler.
		 *
		 * @param viewModel View model.
		 */
		void onState(CountdownViewModel viewModel);
	}
	
	/**
	 * Abstract event for which CountdownBloc responds to.
	 */
	private static abstract class Event {
		/**
		 * @return Unique identifier for type of event.
		 */
		abstract int getEventType();
	}
	
	/**
	 * Event to trigger timings loading.
	 */
	public static class LoadTimingsEvent extends Event {
		Context context;
		
		public LoadTimingsEvent(Context context) {
			this.context = context;
		}
		
		int getEventType() {
			return EVENT_LOAD_TIMINGS;
		}
	}
}

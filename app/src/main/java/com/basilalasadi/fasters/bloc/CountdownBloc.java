package com.basilalasadi.fasters.bloc;

import android.content.Context;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.math.PrayerTimings;
import com.basilalasadi.fasters.model.CountdownViewModel;
import com.basilalasadi.fasters.provider.SettingsManager;

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
					Event event = eventQueue.poll(30, TimeUnit.SECONDS);
					
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
		consumer.onState(new CountdownViewModel(CountdownViewModel.FLAG_DATA_LOADING));
		
		SettingsManager settingsManager = SettingsManager.getInstance();
		
		SettingsManager.Coordinates coords = settingsManager.getCoordinates(event.context);
		
		if (coords == null) {
			consumer.onState(new CountdownViewModel(CountdownViewModel.ERROR_NO_LOCATION));
			return;
		}
		
		SettingsManager.Address address = settingsManager.getAddress(event.context);
		SettingsManager.CustomMethod customMethod = settingsManager.getCustomMethod(event.context);
		
		if (address == null || customMethod == null) {
			consumer.onState(new CountdownViewModel(CountdownViewModel.ERROR_INVALID_SETTINGS));
			return;
		}
		
		ZonedDateTime now = ZonedDateTime.now();
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
			PrayerTimings.Method method = PrayerTimings.getMethodForCountry(address.country);
			
			fajrAngleDegrees = PrayerTimings.getFajrAngleDegrees(method);
			useFixedTimeOffset = PrayerTimings.getHasFixedIshaOffset(method);
			
			if (useFixedTimeOffset) {
				ishaFixedTimeOffset = (int) PrayerTimings.getIshaFixedOffset(method, isRamadan);
			}
			else {
				ishaAngleDegrees = PrayerTimings.getIshaAngleDegrees(method);
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
		
		double nextDayFajrTimming =
				PrayerTimings.getFajr(fajrAngleDegrees, daysSinceEpoch + 1, timeZone,
						coords.longitude, coords.latitude);
		
		double timeNow = now.getHour() + now.getMinute() / 60.0;
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
		long timeTillNextPrayer;
		String nextPrayerName;
		
		if (isEvening) {
			countDownStartTime = datetimeFromTiming(now, timings[3]).toEpochSecond();
			countDownEndTime = datetimeFromTiming(now, nextDayFajrTimming, 1).toEpochSecond();
		}
		else {
			countDownStartTime = datetimeFromTiming(now, timings[0]).toEpochSecond();
			countDownEndTime = datetimeFromTiming(now, timings[3]).toEpochSecond();
		}
		
		if (nextPrayerIndex == 5) {
			timeTillNextPrayer = datetimeFromTiming(now, nextDayFajrTimming,
					1).toEpochSecond() - now.toEpochSecond();
		}
		else {
			timeTillNextPrayer = datetimeFromTiming(now,
					timings[nextPrayerId]).toEpochSecond() - now.toEpochSecond();
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
		
		
		CountdownViewModel viewModel =
				new CountdownViewModel(CountdownViewModel.FLAG_DATA_AVAILABLE, isEvening,
						countDownEndTime, (double) countDownStartTime / countDownEndTime,
						address.city, timeTillNextPrayer, nextPrayerName, timings, nextPrayerId,
						timeZone);
		
		consumer.onState(viewModel);
	}
	
	private ZonedDateTime datetimeFromTiming(ZonedDateTime now, double time) {
		int nextDayFajrHour = (int) time;
		int nextDayFajrMinute = (int) ((time - nextDayFajrHour) * 60);
		int nextDayFajrSecond = (int) ((time - nextDayFajrHour - nextDayFajrMinute / 60.0) * 3600);
		
		return now.withHour(nextDayFajrHour)
				.withMinute(nextDayFajrMinute)
				.withSecond(nextDayFajrSecond);
	}
	
	private ZonedDateTime datetimeFromTiming(ZonedDateTime now, double time, int daysOffset) {
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

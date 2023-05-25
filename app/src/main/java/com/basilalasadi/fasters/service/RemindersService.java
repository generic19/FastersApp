package com.basilalasadi.fasters.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.basilalasadi.fasters.bloc.CountdownBloc;
import com.basilalasadi.fasters.model.CountdownViewModel;
import com.basilalasadi.fasters.util.TimeProvider;

import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;

import static com.basilalasadi.fasters.logic.ReminderConstants.*;


public class RemindersService extends JobService implements CountdownBloc.StateStreamConsumer {
	public static final String TAG = "RemindersService";
	
	public final CountdownBloc bloc  = new CountdownBloc(this);
	public static final int JOB_ID = 1;
	
	private JobScheduler jobScheduler;
	private AlarmManager alarmManager;
	private JobParameters jobParameters = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ComponentName serviceComponent = new ComponentName(this, RemindersService.class);
		
		JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
		builder.setOverrideDeadline(System.currentTimeMillis() + 60 * 1000);
		
		JobInfo jobInfo = builder.build();
		
		jobScheduler.cancel(JOB_ID);
		jobScheduler.schedule(jobInfo);
		
		Log.d(TAG, "Starting reminders service from boot (up to 1 minute)..");
		
		stopSelf(startId);
		return START_NOT_STICKY;
	}
	
	@Override
	public boolean onStartJob(JobParameters params) {
		Log.d(TAG, "Job started.");
		
		jobScheduler = getSystemService(JobScheduler.class);
		alarmManager = getSystemService(AlarmManager.class);
		jobParameters = params;
		
		bloc.addEvent(new CountdownBloc.LoadTimingsEvent(this));
		
		return true;
	}
	
	@Override
	public boolean onStopJob(JobParameters params) {
		Log.d(TAG, "Job stopped.");
		return true;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "Service destroyed.");
	}
	
	@Override
	public void onState(CountdownViewModel viewModel) {
		Log.d(TAG, "New state (" + viewModel.statusString() + ")");
		
		if (viewModel.isError()) {
			String errorMessage;
			
			switch (viewModel.getError()) {
				case CountdownViewModel.ERROR_INVALID_SETTINGS:
					errorMessage = "Error starting reminders service; invalid settings.";
					break;
					
				case CountdownViewModel.ERROR_NO_LOCATION:
					errorMessage = "Error starting reminders service; location not set.";
					break;
					
				default:
					errorMessage = "Error starting reminders service.";
			}
			
			Log.e(TAG, errorMessage);
			jobFinished(jobParameters, false);
		}
		else if (viewModel.isDataAvailable()) {
			ZonedDateTime now = TimeProvider.now();
			
			ZonedDateTime[] dayTimings = viewModel.getDayTimings(now);
			
			ZonedDateTime fajrTime = CountdownBloc.datetimeFromTiming(now, viewModel.prayerTimes[0]);
			ZonedDateTime magribTime = CountdownBloc.datetimeFromTiming(now, viewModel.prayerTimes[3]);
			
			long secondsToFajr = Duration.between(now, fajrTime).getSeconds();
			long secondsToMagrib = Duration.between(now, magribTime).getSeconds();
			ZonedDateTime nextUpdate;
			
			if (secondsToFajr >= 3600) {
				long delay = secondsToFajr - 3600;
				scheduleReminder(now.plusSeconds(delay), REMINDER_PREFAST_MEAL, null);
				nextUpdate = fajrTime;
			}
			else if (secondsToFajr >= 15 * 60) {
				long delay = secondsToFajr - 15 * 60;
				scheduleReminder(now.plusSeconds(delay), REMINDER_WATER, CountdownViewModel.getFormattedTime(dayTimings[0]));
				nextUpdate = fajrTime;
			}
			else if (secondsToMagrib >= 2 * 3600) {
				long delay = secondsToMagrib - 2 * 3600;
				scheduleReminder(now.plusSeconds(delay), REMINDER_PREPARE_BREAKFAST, CountdownViewModel.getFormattedTime(dayTimings[3]));
				nextUpdate = magribTime;
			}
			else if (secondsToMagrib >= 15 * 60) {
				long delay = secondsToMagrib - 15 * 60;
				scheduleReminder(now.plusSeconds(delay), REMINDER_BREAKFAST_CLOSE, CountdownViewModel.getFormattedTime(dayTimings[3]));
				nextUpdate = magribTime;
			}
			else {
				nextUpdate = CountdownBloc.datetimeFromTiming(now, 0.001, 1);
			}
			
			
			ComponentName service = new ComponentName(this, RemindersService.class);
			
			long executionDelay = getMillisTo(now, nextUpdate);
			
			JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, service);
			builder.setMinimumLatency(Math.max(1, executionDelay));
			builder.setOverrideDeadline(15 * 60 * 1000);
			
			JobInfo jobInfo = builder.build();
			
			jobScheduler.cancel(JOB_ID);
			jobScheduler.schedule(jobInfo);
			
			Log.d(TAG, String.format("Scheduled reminder service to run after %d.", executionDelay));
			
			jobFinished(jobParameters, false);
		}
	}
	
	private void scheduleReminder(ZonedDateTime time, int reminderIndex, String extra) {
		Log.d(TAG, "Scheduling reminder (" + reminderIndex + ")..");
		
		ReminderIntent intent = new ReminderIntent(this, reminderIndex, extra);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getEpochMillis(time), pendingIntent);
		
		Log.d(TAG, String.format("Scheduled %s for %s.", intent, time));
	}
	
	private static long getEpochMillis(ZonedDateTime dt) {
		return dt.toEpochSecond() * 1000;
	}
	
	private static long getMillisTo(ZonedDateTime now, ZonedDateTime target) {
		Duration dt = Duration.between(now, target);
		return dt.getSeconds() * 1000 + dt.getNano() / 1000;
	}
}

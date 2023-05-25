package com.basilalasadi.fasters.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.logic.settings.SettingsManager;
import com.basilalasadi.fasters.util.TimeProvider;
import com.basilalasadi.fasters.view.MainActivity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static com.basilalasadi.fasters.logic.ReminderConstants.*;


public class ReminderPublisher extends BroadcastReceiver {
	public static final String TAG = "ReminderPublisher";
	public static final String NOTIFICATION_GROUP_REMINDERS = "RemindersGroup";
	public static final String CHANNEL_PREFAST_MEAL_REMINDER = "PreFastMealReminder";
	public static final String CHANNEL_WATER_REMINDER = "WaterReminder";
	public static final String CHANNEL_PREPARE_BREAKFAST_REMINDER = "PrepareBreakfastReminder";
	public static final String CHANNEL_BREAKFAST_REMINDER = "BreakfastReminder";
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Recieved " + intent + ".");
		
		if (!Objects.equals(intent.getAction(), ReminderIntent.ACTION_SEND_NOTIFICATION)) {
			Log.d(TAG, "Not send notificaion intent.");
			return;
		}
		
		ReminderIntent reminderIntent = new ReminderIntent(intent);
		
		Log.d(TAG, reminderIntent.toString());
		
		SettingsManager settingsManager = SettingsManager.getInstance(context);
		
		if (!settingsManager.areNotificationsEnabled(context) ||
				!settingsManager.isReminderEnabled(context, reminderIntent.getReminderIndex())) {
			return;
		}
		
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		createChannels(context, manager);
		
		int dayOfYear = TimeProvider.now().getDayOfYear();
		int randInt = Math.abs((new Random(dayOfYear)).nextInt());
		
		String title;
		String message;
		
		switch (reminderIntent.getReminderIndex()) {
			case REMINDER_PREFAST_MEAL:
				title = context.getString(R.string.prefast_meal_reminder_title);
				String[] messages = context.getResources().getStringArray(R.array.prefast_meal_reminer_detail_messages);
				message = messages[randInt % messages.length];
				break;
				
			case REMINDER_WATER:
				title = context.getString(R.string.water_reminder_title);
				message = context.getString(R.string.fasting_begins_at_template, reminderIntent.getExtraString());
				break;
				
			case REMINDER_PREPARE_BREAKFAST:
				title = context.getString(R.string.prepare_breakfast_meal_reminder_title);
				message = context.getString(R.string.breakfast_at_template, reminderIntent.getExtraString());
				break;
				
			case REMINDER_BREAKFAST_CLOSE:
				title = context.getString(R.string.breakfast_reminder_title);
				message = context.getString(R.string.breakfast_at_template, reminderIntent.getExtraString());
				break;
				
			default:
				return;
		}
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_PREFAST_MEAL_REMINDER);
		
		builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		builder.setDefaults(Notification.DEFAULT_ALL);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setAllowSystemGeneratedContextualActions(false);
		builder.setSmallIcon(R.mipmap.ic_notification);
		builder.setAutoCancel(true);
		
		Intent mainActivityIntent =  new Intent(context, MainActivity.class);
		mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		builder.setContentIntent(PendingIntent.getActivity(context, 1, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		
		Notification notification = builder.build();
		
		Log.d(TAG, "Sending " + notification + "..");
		
		manager.notify(dayOfYear << 16 | (reminderIntent.getReminderIndex() & 0xff), notification);
	}
	
	private static void createChannels(Context context, NotificationManager manager) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannelGroup group = new NotificationChannelGroup(NOTIFICATION_GROUP_REMINDERS, "Reminders");
			manager.createNotificationChannelGroup(group);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				group.setDescription("Provides timely fasting reminders.");
			}
			
			ArrayList<NotificationChannel> channels = makeReminderChannels(
					context,
					new String[]{CHANNEL_PREFAST_MEAL_REMINDER, CHANNEL_WATER_REMINDER, CHANNEL_PREPARE_BREAKFAST_REMINDER, CHANNEL_BREAKFAST_REMINDER},
					new int[]{
							R.string.preference_title_prefast_meal_reminder,
							R.string.preference_title_water_reminder,
							R.string.preference_title_prepare_breakfast_reminder,
							R.string.preference_title_breakfast_reminder},
					new int[]{
							R.string.preference_summary_prefast_meal_reminder,
							R.string.preference_summary_water_reminder,
							R.string.preference_summary_prepare_breakfast_reminder,
							R.string.preference_summary_breakfast_reminder}
					);
			
			manager.createNotificationChannels(channels);
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	private static ArrayList<NotificationChannel> makeReminderChannels(Context context, String[] ids, int[] names, int[] descriptions) {
		ArrayList<NotificationChannel> channels = new ArrayList<>(ids.length);
		
		for (int i = 0; i < ids.length; i++) {
			NotificationChannel channel = new NotificationChannel(ids[i], context.getString(names[i]), NotificationManager.IMPORTANCE_HIGH);
			channel.setDescription(context.getString(descriptions[i]));
			channel.setGroup(NOTIFICATION_GROUP_REMINDERS);
			
			channels.add(channel);
		}
		
		return channels;
	}
	
	
	
}

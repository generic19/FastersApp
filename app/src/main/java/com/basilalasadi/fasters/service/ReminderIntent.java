package com.basilalasadi.fasters.service;

import androidx.annotation.NonNull;
import android.content.Context;
import android.content.Intent;

import static com.basilalasadi.fasters.logic.ReminderConstants.*;

public class ReminderIntent extends Intent {
	public static final String EXTRA_REMINDER_INDEX = "reminder";
	public static final String EXTRA_STRING = "string";
	public static final String ACTION_SEND_NOTIFICATION = "com.basilalasadi.fasters.action.SEND_NOTIFICATION";
	
	/**
	 * Creates an explicit intent that sends a reminder notification to the user.
	 *
	 * The required string extra for these reminders are:
	 *     REMINDER_PREFAST_MEAL:       Not required. null.
	 *     REMINDER_WATER:              Formatted time of next fajr.
	 *     REMINDER_PREPARE_BREAKFAST:  Formatted time to next magrib.
	 *     REMINDER_BREAKFAST_CLOSE:    Formatted time to next magrib.
	 *
	 * @param context Current context.
	 * @param reminderIndex One of reminder indexes in ReminderConstants class.
	 * @param extraString The string extra.
	 */
	public ReminderIntent(Context context, int reminderIndex, String extraString) {
		super(context, ReminderPublisher.class);
		super.setAction(ACTION_SEND_NOTIFICATION);
		super.putExtra(EXTRA_REMINDER_INDEX, reminderIndex);
		super.putExtra(EXTRA_STRING, extraString);
	}
	
	public ReminderIntent(Intent intent) {
		super(intent);
	}
	
	public int getReminderIndex() {
		return super.getIntExtra(EXTRA_REMINDER_INDEX, -1);
	}
	
	public String getExtraString() {
		return super.getStringExtra(EXTRA_STRING);
	}
	
	@NonNull
	@Override
	public String toString() {
		int reminderIndex = getReminderIndex();
		String reminderType;
		switch (reminderIndex) {
			case REMINDER_PREFAST_MEAL: reminderType = "REMINDER_PREFAST_MEAL"; break;
			case REMINDER_WATER: reminderType = "REMINDER_WATER"; break;
			case REMINDER_PREPARE_BREAKFAST: reminderType = "REMINDER_PREPARE_BREAKFAST"; break;
			case REMINDER_BREAKFAST_CLOSE: reminderType = "REMINDER_BREAKFAST_CLOSE"; break;
			default: reminderType = "INVALID(" + reminderIndex + ")";
		}
		
		String strExtra = getExtraString();
		String extra = strExtra == null ? "null" : "\"" + strExtra + "\"";
		return String.format("ReminderIntent(type: %s, extra: %s)", reminderType, extra);
	}
}
package com.basilalasadi.fasters.receivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.basilalasadi.fasters.R;

import java.util.Objects;

public class ExactAlarmPermissionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Objects.equals(intent.getAction(), AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

            if (!alarmManager.canScheduleExactAlarms()) {
                return;
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.getBoolean(context.getString(R.string.preference_should_enable_all_notifications), false)) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.settings_key_all_notifications), true)
                    .putBoolean(context.getString(R.string.preference_should_enable_all_notifications), false)
                    .apply();
        }

        if (prefs.getBoolean(context.getString(R.string.settings_key_all_notifications), false)) {
            Intent broadcast = new Intent(context.getString(R.string.action_start_reminders_service));
            context.sendBroadcast(broadcast);
        }
    }
}

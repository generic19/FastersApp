package com.basilalasadi.fasters.view.settings;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.basilalasadi.fasters.R;


public class NotificationsSettingsFragment extends PreferenceFragmentCompat {
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notifications_settings, rootKey);
        
        //noinspection ConstantConditions
        findPreference("app_notifications").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        
            FragmentActivity activity = requireActivity();
        
            intent.putExtra("app_package", activity.getPackageName());
            intent.putExtra("app_uid", activity.getApplicationInfo().uid);
        
            intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());
        
            startActivity(intent);
        
            return true;
        });

        findPreference(getString(R.string.settings_key_all_notifications)).setOnPreferenceChangeListener((preference, newValue) -> {
            if (Build.VERSION.SDK_INT >= 34 &&
                    !requireContext().getSystemService(AlarmManager.class).canScheduleExactAlarms()) {

                new AlertDialog.Builder(requireContext())
                        .setTitle("Allow reminders?")
                        .setMessage("Fasters needs your permission to schedule timely reminders.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            PreferenceManager.getDefaultSharedPreferences(requireContext())
                                    .edit()
                                    .putBoolean(getString(R.string.preference_should_enable_all_notifications), true)
                                    .apply();

                             requireContext().startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                             dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();

                return false;
            }

            return true;
        });
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(R.string.notifications);
    }
}
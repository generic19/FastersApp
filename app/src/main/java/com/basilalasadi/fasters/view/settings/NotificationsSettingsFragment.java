package com.basilalasadi.fasters.view.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(R.string.notifications);
    }
}
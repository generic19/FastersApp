package com.basilalasadi.fasters.view.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.basilalasadi.fasters.R;
import com.basilalasadi.fasters.logic.LocationProvider;
import com.basilalasadi.fasters.logic.settings.SettingsManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class LocationSettingsFragment extends PreferenceFragmentCompat
        implements LocationProvider.OnLocationResultCallback, Handler.Callback {
    
    private final Handler handler = new Handler(this);
    
    private static final String[] toastMessages = new String[]{
            "Could not get location; no available location providers.",
            "Could not get location.",
            "Geo-location not available.",
            "Could not set location.",
            "Location set.",
    };
    
    private static final int MSG_SEND_TOAST = 100;
    private static final int TOAST_NO_PROVIDERS = 0;
    private static final int TOAST_COULD_NOT_GET_LOCATION = 1;
    private static final int TOAST_LOCATION_NOT_AVAILABLE = 2;
    private static final int TOAST_COULD_NOT_SET_LOCATION = 3;
    private static final int TOAST_LOCATION_SET = 4;
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.location_settings, rootKey);
        
        
        
        //noinspection ConstantConditions
        findPreference("automatic_settings").setOnPreferenceClickListener(preference -> {
            
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                    
                    builder.setIcon(android.R.drawable.ic_menu_mylocation);
                    builder.setTitle(R.string.access_your_general_location);
                    builder.setMessage(R.string.location_permission_rationale);
                    builder.setPositiveButton("Okay", (dialog, which) -> {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    });
                    builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                    
                    builder.create().show();
                }
                else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                }
            }
            else {
                LocationProvider.getLocationNoCheck(getContext(), this);
            }
            
            return true;
        });
    }
    
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (msg.what != MSG_SEND_TOAST) {
            return false;
        }
        String message;
        
        switch (msg.arg1) {
            case TOAST_NO_PROVIDERS: message = "Could not get location; no available location providers."; break;
            case TOAST_COULD_NOT_GET_LOCATION: message = "Could not get location."; break;
            case TOAST_LOCATION_NOT_AVAILABLE: message = "Geo-location not available."; break;
            case TOAST_COULD_NOT_SET_LOCATION: message = "Could not set location."; break;
            case TOAST_LOCATION_SET: message = "Location set."; break;
            default: return false;
        }
        Toast.makeText(getContext(), message, msg.arg2).show();
        return true;
    }
    
    private void showToast(int toast) {
        Message msg = new Message();
        msg.what = MSG_SEND_TOAST;
        msg.arg1 = toast;
        
        if (toast == TOAST_LOCATION_SET) {
            msg.arg2 = Toast.LENGTH_SHORT;
        }
        else {
            msg.arg2 = Toast.LENGTH_LONG;
        }
        
        handler.sendMessage(msg);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationProvider.getLocationNoCheck(getContext(), this);
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(R.string.location);
    }
    
    @Override
    public void onLocationResult(LocationProvider.LocationResult result) {
        if (result.isError()) {
            if (result.getError() == LocationProvider.LocationResult.ERROR_NO_PROVIDERS) {
                showToast(TOAST_NO_PROVIDERS);
            }
            else {
                showToast(TOAST_COULD_NOT_GET_LOCATION);
            }
        }
        else if (!result.geoLocationAvailable()) {
            showToast(TOAST_LOCATION_NOT_AVAILABLE);
        }
        else {
            SettingsManager.Coordinates coordinates = new SettingsManager.Coordinates(result.longitude, result.latitude);
            boolean success = SettingsManager.getInstance(getContext()).setLocation(getContext(), coordinates);
            
            if (!success) {
                showToast(TOAST_COULD_NOT_SET_LOCATION);
            }
            else {
                showToast(TOAST_LOCATION_SET);
            }
        }
    }
}
package com.example.dashcam;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.example.dashcam.activity.MainActivity;

public class GpsChangeReceiver extends android.content.BroadcastReceiver{
    private MainActivity mainActivity;
    public GpsChangeReceiver(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive (Context context, Intent intent){
        if(intent.getAction() != null && intent.getAction().matches("android.location.PROVIDERS_CHANGED")){
            boolean isGpsEnabled = checkGpsStatus(context);
            mainActivity.updateGpsStatusIcon(isGpsEnabled);
        }
    }

    public boolean checkGpsStatus(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }
}

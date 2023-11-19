package com.example.dashcam;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.dashcam.activity.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Context context;
    private LocationListener locationListener;

    public interface LocationListener{
        void onLocationUpdated(String address);
    }
    public LocationHelper(Context context, LocationListener listener){
        this.context = context;
        this.locationListener = listener;
        initLocationClient();
    }

    private void initLocationClient(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult != null){
                    Location location = locationResult.getLastLocation();
                    if(location != null){
                        getAddressFromLocation(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };
    }

    public void checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestSingleUpdate() {
        // 위치 권한이 있는 경우에만 단일 업데이트 요청
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
        }
    }
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY).build();
        return locationRequest;
    }
    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.KOREA);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // 시, 구, 동 정보 추출
                String city = address.getLocality(); // 시
                String district = address.getSubLocality(); // 구
                String street = address.getThoroughfare(); // 동

                // 주소를 조합하여 표시
                StringBuilder addressStringBuilder = new StringBuilder();
                if (city != null) {
                    addressStringBuilder.append(city);
                }
                if (district != null) {
                    addressStringBuilder.append(" ").append(district);
                }
                if (street != null) {
                    addressStringBuilder.append(" ").append(street);
                }

                String addressString = addressStringBuilder.toString();
                locationListener.onLocationUpdated(addressString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용된 경우에만 단일 업데이트 요청
                requestSingleUpdate();
            }
        }
    }
}

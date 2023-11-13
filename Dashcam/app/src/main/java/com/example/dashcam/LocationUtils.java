package com.example.dashcam;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {
    private static LocationUtils instance;
    private LocationUtils(){

    }
    public static synchronized LocationUtils getInstance(){
        if(instance ==  null){
            instance = new LocationUtils();
        }
        return instance;
    }


    public String getAddressFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

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


                return addressString;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }
}

package com.example.dashcam.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dashcam.GpsChangeReceiver;
import com.example.dashcam.LocationHelper;
import com.example.dashcam.R;
import com.example.dashcam.SQLiteHelper;
import com.example.dashcam.SQLiteHelperSingleton;
import com.example.dashcam.StorageUtils;

public class MainActivity extends AppCompatActivity
    implements LocationHelper.LocationListener {
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase db;
    private GpsChangeReceiver gpsChangeReceiver;
    private ImageView imageGps;
    private TextView textGps;
    private TextView textSavePath;
    private TextView textStorageSpace;
    private ImageButton btnRefreshing;
    private Button btnFileExplorer;
    private Button btnCamera;
    private Button btn2;
    private TextView textNowLocation;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sqLiteHelper = SQLiteHelperSingleton.getInstance(this);
        db = sqLiteHelper.getWritableDatabase();

        gpsChangeReceiver = new GpsChangeReceiver(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        registerReceiver(gpsChangeReceiver, intentFilter);
        locationHelper = new LocationHelper(this, this);
        locationHelper.checkLocationPermission();

        initialView();

        refreshing();

        boolean isGpsEnabled = gpsChangeReceiver.checkGpsStatus(this);
        updateGpsStatusIcon(isGpsEnabled);

        //지도
        btn2.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
               Intent intent = new Intent(getApplicationContext(), RoutelistActivity.class);
               startActivity(intent);
           }
        });

        //카메라
        btnCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), CameraXActivity.class);
                startActivity(intent);
            }
        });

        //파일 탐색기
        btnFileExplorer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), FileExplorerActivity.class);
                startActivity(intent);
            }
        });

        btnRefreshing.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Animation rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_animation);
                btnRefreshing.startAnimation(rotateAnimation);
                refreshing();
            }
        });

    }


    @Override
    protected void onDestroy(){
        if (gpsChangeReceiver != null) {
            unregisterReceiver(gpsChangeReceiver);
        }
        db.close();

        super.onDestroy();
    }

    private void initialView(){
        imageGps = (ImageView) findViewById(R.id.imageGps);
        textGps = (TextView) findViewById(R.id.textGPSonoff);
        textSavePath = (TextView) findViewById(R.id.textSave);
        textStorageSpace = (TextView) findViewById(R.id.textStorageSpace);
        btnRefreshing = (ImageButton) findViewById(R.id.btnRefreshing);
        btnFileExplorer = (Button) findViewById(R.id.btn1);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btn2 = (Button) findViewById(R.id.btn2);
        textNowLocation = (TextView) findViewById(R.id.textNowLocation);
    }


    public void updateGpsStatusIcon(boolean isGpsEnabled){
        if(isGpsEnabled){
            imageGps.setImageResource(R.drawable.baseline_location_on_24);
            textGps.setText("위치 ON");
        }
        else{
            imageGps.setImageResource(R.drawable.baseline_location_off_24);
            textGps.setText("위치 OFF");
            textNowLocation.setText("현재 위치 : 위치를 켜주세요");
        }
    }

    private void refreshing(){
        textSavePath.setText("저장 경로 : Movies/Recording");

        String storage = storageInfo();
        textStorageSpace.setText("저장공간 " + storage + "GB" );

        locationHelper.requestSingleUpdate();

    }

    private String storageInfo(){
        String totalSpace = StorageUtils.getExternalStorageTotalSpace();
        String freeSpace = StorageUtils.getExternalStorageFreeSpace();

        return freeSpace + "/" + totalSpace;

    }

    @Override
    public void onLocationUpdated(String address){
        textNowLocation.setText("현재 위치 : " + address);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // LocationHelper에게 권한 요청 결과 전달
        locationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
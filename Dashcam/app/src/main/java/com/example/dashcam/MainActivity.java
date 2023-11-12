package com.example.dashcam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase db;
    private GpsChangeReceiver gpsChangeReceiver;
    private ImageView imageGps;
    private TextView textGps;


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
        imageGps = (ImageView) findViewById(R.id.imageGps);
        textGps = (TextView) findViewById(R.id.textGPSonoff);

        //지도
        Button btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
               Intent intent = new Intent(getApplicationContext(), RoutelistActivity.class);
               startActivity(intent);
           }
        });

        //카메라
        Button btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), CameraXActivity.class);
                startActivity(intent);
            }
        });

        //파일 탐색기
        Button btnFileExplorer = (Button) findViewById(R.id.btn1);
        btnFileExplorer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), FileExplorerActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        db.close();
    }

    public void updateGpsStatusIcon(boolean isGpsEnabled){
        if(isGpsEnabled){
            imageGps.setImageResource(R.drawable.baseline_location_on_24);
            textGps.setText("위치 ON");
        }
        else{
            imageGps.setImageResource(R.drawable.baseline_location_off_24);
            textGps.setText("위치 OFF");
        }
    }

}
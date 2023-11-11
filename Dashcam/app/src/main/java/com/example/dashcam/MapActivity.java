package com.example.dashcam;

import static android.icu.util.MeasureUnit.DOT;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener{
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    SQLiteHelper sqLiteHelper;

    private Button btn;

    private String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        sqLiteHelper = SQLiteHelperSingleton.getInstance(this);

        Intent intent = getIntent(); //데이터를 전달받을 인텐트
        tableName = intent.getStringExtra("TableName");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }






    public void onMapReady(GoogleMap googleMap){
        ArrayList<String> timeList = sqLiteHelper.getTime(tableName);

        LatLng zoomPoint = new LatLng(37.5136944, 126.735084 );

        LatLng lastPoint = null;

        for(String start : timeList){
            List<LatLng> latLngs = sqLiteHelper.getLatLng(tableName, start);

            if(lastPoint != null){
                latLngs.add(0, lastPoint);
            }


            //googleMap.addMarker(new MarkerOptions()
            //        .position(latLngs.get(0))
            //        .title("marker"));

            Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(latLngs));

            polyline1.setTag(start);

            lastPoint = latLngs.get(latLngs.size()-1);

        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomPoint, 15));

        googleMap.setOnPolylineClickListener(this);

    }

    @Override
    public void onPolylineClick(Polyline polyline){
        /*
        if((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else{
            polyline.setPattern(null);
        }*/
        Toast.makeText(this, "Route type" + polyline.getTag().toString(), Toast.LENGTH_SHORT).show();
    }
}
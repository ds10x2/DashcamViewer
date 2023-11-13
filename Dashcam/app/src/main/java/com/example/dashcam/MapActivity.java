package com.example.dashcam;

import static android.icu.util.MeasureUnit.DOT;
import static android.os.Environment.DIRECTORY_MOVIES;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dashcam.databinding.ActivityMapBinding;
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

    private String tableName;

    private ActivityMapBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_map);
        viewBinding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        sqLiteHelper = SQLiteHelperSingleton.getInstance(this);

        Intent intent = getIntent(); //데이터를 전달받을 인텐트
        tableName = intent.getStringExtra("TableName");

        MediaController controller = new MediaController(getApplicationContext());
        viewBinding.videoPreview.setMediaController(controller);
        viewBinding.videoPreview.requestFocus();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
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
        Toast.makeText(this, polyline.getTag().toString(), Toast.LENGTH_SHORT).show();
        viewBinding.textPreview.setVisibility(View.GONE);
        viewBinding.layoutPreview.setVisibility(View.VISIBLE);
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath() + "/Recording/"
                + polyline.getTag().toString() + ".mp4";
        viewBinding.videoPreview.setVideoPath(path);
        viewBinding.textPreviewTitle.setText(polyline.getTag().toString());

    }

    public void videoViewSetting(){
        viewBinding.videoPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                int videoDuration = viewBinding.videoPreview.getDuration();
            }
        });

        viewBinding.videoPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //동영상 재생 완료 후

            }
        });
    }
}
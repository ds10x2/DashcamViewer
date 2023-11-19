package com.example.dashcam.activity;

import static android.icu.util.MeasureUnit.DOT;
import static android.os.Environment.DIRECTORY_MOVIES;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dashcam.FileExistsChecker;
import com.example.dashcam.LocationUtils;
import com.example.dashcam.R;
import com.example.dashcam.SQLiteHelper;
import com.example.dashcam.SQLiteHelperSingleton;
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
    private Polyline prevClickedPolyline;


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

        //LatLng zoomPoint = new LatLng(37.5136944, 126.735084 );

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
        double zoomLatitude = lastPoint.latitude;
        double zoomLongitude = lastPoint.longitude;

        LatLng zoomPoint = new LatLng(zoomLatitude, zoomLongitude);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomPoint, 15));

        googleMap.setOnPolylineClickListener(this);

        String address = LocationUtils.getInstance().getAddressFromLocation(getApplicationContext(), zoomLatitude, zoomLongitude);
        viewBinding.textAddress.setText(address);

    }

    @Override
    public void onPolylineClick(Polyline polyline){

        if (prevClickedPolyline != null) {
            prevClickedPolyline.setColor(Color.BLACK); // 이전에 사용한 기본 색상으로 변경
        }

        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setColor(Color.RED);
        } else {
            polyline.setPattern(null);
        }
        prevClickedPolyline = polyline;

        Toast.makeText(this, polyline.getTag().toString(), Toast.LENGTH_SHORT).show();
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath() + "/Recording/"
                + polyline.getTag().toString() + ".mp4";

        if(FileExistsChecker.isFileExisit(path)){
            viewBinding.textPreview.setVisibility(View.GONE);
            viewBinding.layoutPreview.setVisibility(View.VISIBLE);

            viewBinding.btnFavorite.setVisibility(View.VISIBLE);
            if(sqLiteHelper.isFileExists(polyline.getTag().toString())){
                viewBinding.btnFavorite.setText("즐겨찾기에서 삭제");
                viewBinding.btnFavorite.setBackgroundColor(Color.rgb(102, 106, 115));
                viewBinding.btnFavorite.setTextColor(Color.rgb(255, 255, 255));
            }else{
                viewBinding.btnFavorite.setText("즐겨찾기에 추가");
            }
            manageFav(polyline.getTag().toString());


            viewBinding.videoPreview.setVideoPath(path);
            videoViewSetting();
            viewBinding.textPreviewTitle.setText(polyline.getTag().toString());


        }else{
            viewBinding.textPreview.setText("동영상이 존재하지 않습니다.");
        }

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
                viewBinding.btnPlayPreview.setText("재생하기");

            }
        });

        viewBinding.btnPlayPreview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!viewBinding.videoPreview.isPlaying()){
                    viewBinding.videoPreview.start();
                    viewBinding.btnPlayPreview.setText("일시정지");
                }
            }
        });
    }

    public void manageFav(String fileName){

        viewBinding.btnFavorite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(sqLiteHelper.isFileExists(fileName)){
                    //즐겨찾기에서 삭제
                    sqLiteHelper.deleteFavorite(fileName);
                    viewBinding.btnFavorite.setText("즐겨찾기에 추가");
                }else{
                    //즐겨찾기 등록
                    sqLiteHelper.insertFavorite(fileName);
                    viewBinding.btnFavorite.setText("즐겨찾기에서 삭제");
                    viewBinding.btnFavorite.setBackgroundColor(Color.rgb(102, 106, 115));
                    viewBinding.btnFavorite.setTextColor(Color.rgb(255, 255, 255));
                }
            }
        });

    }
}
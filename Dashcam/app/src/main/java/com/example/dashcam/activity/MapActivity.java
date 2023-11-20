package com.example.dashcam.activity;

import static android.icu.util.MeasureUnit.DOT;
import static android.os.Environment.DIRECTORY_MOVIES;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
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

import java.io.File;
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
    private boolean isVideoPlaying = false;
    private Handler handler = new Handler();
    private MediaController controller;
    private boolean isUserSeeking = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_map);
        viewBinding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        sqLiteHelper = SQLiteHelperSingleton.getInstance(this);

        Intent intent = getIntent(); //데이터를 전달받을 인텐트
        tableName = intent.getStringExtra("TableName");

        controller = new MediaController(getApplicationContext());
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
            }else{
                viewBinding.btnFavorite.setText("즐겨찾기에 추가");
                viewBinding.btnFavorite.setBackgroundColor(Color.rgb(124, 134, 222));
            }
            manageFav(polyline.getTag().toString(), tableName);

            viewBinding.btnShare.setVisibility(View.VISIBLE);
            viewBinding.btnShare.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("video/*");
                    Uri videoUri = FileProvider.getUriForFile(MapActivity.this, getApplicationContext().getPackageName() + ".fileprovider", new File(path));

                    shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "비디오 공유하기"));
                }
            });


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
                viewBinding.seekBar.setMax(videoDuration);
            }
        });


        viewBinding.videoPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //동영상 재생 완료 후

            }
        });

        viewBinding.videoPreview.setOnTouchListener((view, motionEvent)->{
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                if(isVideoPlaying){
                    viewBinding.videoPreview.pause();
                }else{
                    viewBinding.videoPreview.start();
                    updateSeekBar();
                }
                isVideoPlaying = !isVideoPlaying;
            }
            return true;
        });

        viewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    isUserSeeking = true;
                    viewBinding.videoPreview.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
            }
        });

    }

    public void manageFav(String fileName, String tableName){

        viewBinding.btnFavorite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(sqLiteHelper.isFileExists(fileName)){
                    //즐겨찾기에서 삭제
                    sqLiteHelper.deleteFavorite(fileName);
                    viewBinding.btnFavorite.setText("즐겨찾기에 추가");
                    viewBinding.btnFavorite.setBackgroundColor(Color.rgb(124, 134, 222));
                }else{
                    //즐겨찾기 등록
                    sqLiteHelper.insertFavorite(fileName, tableName);
                    viewBinding.btnFavorite.setText("즐겨찾기에서 삭제");
                    viewBinding.btnFavorite.setBackgroundColor(Color.rgb(102, 106, 115));
                }
            }
        });

    }

    private void updateSeekBar(){
        if(viewBinding.videoPreview.isPlaying() && !isUserSeeking){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int currentPosition = viewBinding.videoPreview.getCurrentPosition();
                    viewBinding.seekBar.setProgress(currentPosition);

                    if(viewBinding.videoPreview.isPlaying())
                        updateSeekBar();
                }
            }, 1000);

        }
    }
}
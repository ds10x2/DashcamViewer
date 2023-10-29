package com.example.dashcam;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.example.dashcam.databinding.ActivityCameraxBinding;
import com.example.dashcam.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraXActivity extends AppCompatActivity {

    private ActivityCameraxBinding viewBinding;
    private ImageCapture imageCapture;
    VideoCapture<Recorder> videoCapture = VideoCapture.withOutput(new Recorder.Builder().build());
    Recording recording = null;
    ExecutorService cameraExecutor;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private boolean isRecording = false;

    private static final long RECORDING_DURATION = 10000; //10초 (밀리초) 타이머가 n번 돌아가는 총 시간

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(cameraFacing);
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCameraxBinding.inflate(getLayoutInflater()); //바인딩 클래스의 인스턴스 생성
        setContentView(viewBinding.getRoot());



        if(allPermissionsGranted()){
            startCamera(cameraFacing);
        }else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        getLocation();

        //viewBinding.imageCaptureButton.setOnClickListener(v -> takePhoto());
        //viewBinding.videoCaptureButton.setOnClickListener(v -> captureVideo());

        viewBinding.btnRecordStart.setOnClickListener(v -> repeatRecording());

        cameraExecutor = Executors.newSingleThreadExecutor();
    }


    private CountDownTimer timer = new CountDownTimer(RECORDING_DURATION, 5000){ //countDownInterval : 타이머 1번 돌아가는 시간
        @Override
        public void onTick(long milliisUntilFinished){
            startRecord();
        }
        @Override
        public void onFinish(){
            stopRecording(); //설정된 시간이 끝나면 반복을 멈춤
            viewBinding.btnRecordStart.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
            isRecording = false;
        }
    };

    private void stopRecording(){
        Recording recording1 = recording;
        if(recording1 != null){
            recording1.stop();
            recording = null;
        }
    }

    private void repeatRecording(){

        if(isRecording) {
            timer.cancel();
            stopRecording(); //설정된 시간이 끝나면 반복을 멈춤
            viewBinding.btnRecordStart.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
            isRecording = false;
        }
        else{
            viewBinding.btnRecordStart.setImageResource(R.drawable.ic_baseline_stop_circle_24);
            isRecording = true;
            timer.start();

        }
    }

    @SuppressWarnings("MissingPermission")
    private void startRecord(){

        Recording recording1 = recording;
        if(recording1 != null){
            recording1.stop();
            recording = null;
        //    return;
        }

        //private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
        String fileName = new SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Recording");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(getApplicationContext(), options).withAudioEnabled()
                .start(ContextCompat.getMainExecutor(getApplicationContext()), videoRecordEvent -> {

                   if(videoRecordEvent instanceof VideoRecordEvent.Start){
                       Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
                       viewBinding.btnRecordStart.setEnabled(true);

                   }else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                       if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                           String msg = "Video capture succeeded: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                           Log.i(TAG, msg);
                           Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                       } else {
                           recording.close();
                           recording = null;
                           String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                           Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                       }
                   }
                });

    }

    private void startCamera(int cameraFacing) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(
                        viewBinding.viewFinder.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);

            } catch (Exception exc) {
                Log.e(TAG, "Use case binding failed", exc);

            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static final String TAG = "CameraXApp";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(cameraFacing);
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //위치 정보 불러오기

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double currentLatitude = 1;
    private double currentLongitude = 1;

    public void getLocation(){

        viewBinding.locationTextView.setText("위도 : " + currentLatitude + ", 경도: "+currentLongitude);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        //현재 위치 설정 받기
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);


        //위치 업데이트 콜백 정의
        locationCallback = new LocationCallback() {

            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null)
                    return;
                for(Location location : locationResult.getLocations()){
                    //위치 정보로 UI 업데이트
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    viewBinding.locationTextView.setText("위도 : " + currentLatitude + ", 경도: "+currentLongitude);

                }
            }

        };

    }

    //위치 업데이트 중지
    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
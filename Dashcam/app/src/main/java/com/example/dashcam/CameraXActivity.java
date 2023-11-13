package com.example.dashcam;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
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
    private long RECORDING_DURATION = 30000; //30초 (밀리초) 타이머가 n번 돌아가는 총 시간
    private long COUNTDOWN_INTERVAL = 10000;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLatitude = 1;
    private double currentLongitude = 1;
    SQLiteHelper sqLiteHelper;
    private String startTime = null;
    private String arriveTime;
    private long cnt = 0;
    private long cntEntire = 0;



    /*
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(cameraFacing);
        }
    });
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCameraxBinding.inflate(getLayoutInflater()); //바인딩 클래스의 인스턴스 생성
        setContentView(viewBinding.getRoot());
        sqLiteHelper = SQLiteHelperSingleton.getInstance(this);

        //권한
        if (allPermissionsGranted()) {
            startCamera(cameraFacing);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        setTextRec();
        viewBinding.btnRecordStart.setOnClickListener(v -> repeatRecording());
        viewBinding.btnSettingRec.setOnClickListener(v -> settingRecCycle());
        cameraExecutor = Executors.newSingleThreadExecutor();

        //오늘 날짜
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        String currentDate = dateFormat.format(date);
        viewBinding.dateTextView.setText(currentDate);

        cntEntire = RECORDING_DURATION / COUNTDOWN_INTERVAL;
        viewBinding.textRecNow.setText("0/" + cntEntire);

        //위치 정보
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }else{
            getLocation();
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    private void settingRecCycle(){
        viewBinding.settingRec.setVisibility(View.VISIBLE);
        viewBinding.textRec.setVisibility(View.INVISIBLE);
        viewBinding.textRecCycle.setVisibility(View.INVISIBLE);

        viewBinding.seekBarMaxRec.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Seekbar 값 변경될 때마다 호출
                viewBinding.textSeekMaxRec.setText(progress + "분");
                //30000 == 30초
                RECORDING_DURATION = progress * 60000;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){
                //첫 눌림에 호출
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                //드래그 떼어냈을 때
            }
        });

        viewBinding.seekBarRecCycle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                viewBinding.textSeekRecCycle.setText(progress * 10 + "초");
                COUNTDOWN_INTERVAL = progress * 10000;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });

        viewBinding.btnSettingDone.setOnClickListener(view -> {
            setTextRec();
            viewBinding.settingRec.setVisibility(View.GONE);
            viewBinding.textRec.setVisibility(View.VISIBLE);
            viewBinding.textRecCycle.setVisibility(View.VISIBLE);

            cntEntire = RECORDING_DURATION / COUNTDOWN_INTERVAL;
            viewBinding.textRecNow.setText("0/" + cntEntire);

            timer = new CountDownTimer(RECORDING_DURATION, COUNTDOWN_INTERVAL){ //countDownInterval : 타이머 1번 돌아가는 시간
                @Override
                public void onTick(long milliisUntilFinished){
                    startRecord();
                }
                @Override
                public void onFinish(){
                    stopRecording(); //설정된 시간이 끝나면 반복을 멈춤
                    viewBinding.btnRecordStart.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
                    isRecording = false;
                    sqLiteHelper.insertDriving(startTime, arriveTime);
                    startTime = null;
                    cnt = 0;
                }
            };
        });
    }


    private CountDownTimer timer = new CountDownTimer(RECORDING_DURATION, COUNTDOWN_INTERVAL){ //countDownInterval : 타이머 1번 돌아가는 시간
        @Override
        public void onTick(long milliisUntilFinished){
            startRecord();
        }
        @Override
        public void onFinish(){
            stopRecording(); //설정된 시간이 끝나면 반복을 멈춤
            viewBinding.btnRecordStart.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
            isRecording = false;
            sqLiteHelper.insertDriving(startTime, arriveTime);
            startTime = null;
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
            sqLiteHelper.insertDriving(startTime, arriveTime);
            startTime = null;
            cnt = 0;
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

        String FILENAME_FORMAT = "yyyyMMddHHmmss_SSS";
        String fileName = new SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(new Date());
        //String fileName = new SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Recording");

        cnt++;
        viewBinding.textRecNow.setText(cnt + "/" + cntEntire);

        if(startTime == null) {
            startTime = fileName;
            sqLiteHelper.createTable(startTime);
        }
        arriveTime = fileName;


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
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
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


    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public void getLocation(){

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult != null){
                    Location location = locationResult.getLastLocation();
                    if(location != null){
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        String address = LocationUtils.getInstance().getAddressFromLocation(getApplicationContext(), currentLatitude, currentLongitude);
                        //viewBinding.locationTextView.setText(currentLatitude + " " + currentLongitude);
                        viewBinding.locationTextView.setText(address);
                        if(isRecording){
                            sqLiteHelper.insertLocation(startTime, arriveTime, currentLatitude, currentLongitude);
                        }
                    }
                }
            }
        };

    }

    private void setTextRec(){
        long maxRec = RECORDING_DURATION / 60000;
        viewBinding.textRec.setText("최대 녹화 시간 " + maxRec + "분");

        long recCycle = COUNTDOWN_INTERVAL / 1000;
        viewBinding.textRecCycle.setText("녹화 주기 " +recCycle + "초");
    }




}
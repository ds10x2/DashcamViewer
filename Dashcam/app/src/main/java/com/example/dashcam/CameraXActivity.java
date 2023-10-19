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
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import com.example.dashcam.databinding.ActivityCameraxBinding;
import com.example.dashcam.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraXActivity extends AppCompatActivity {

    private ActivityCameraxBinding viewBinding;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture = null;
    private Recording recording = null;
    private ExecutorService cameraExecutor;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;

    private static final long RECORDING_DURATION = 10000; //10초 (밀리초)



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCameraxBinding.inflate(getLayoutInflater()); //바인딩 클래스의 인스턴스 생성
        setContentView(viewBinding.getRoot());

        if(allPermissionsGranted()){
            startCamera();
        }else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //viewBinding.imageCaptureButton.setOnClickListener(v -> takePhoto());
        //viewBinding.videoCaptureButton.setOnClickListener(v -> captureVideo());

        viewBinding.btnRecordStart.setOnClickListener(v -> startRecord());

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void stopRecording(){
        Recording recording1 = recording;
        if(recording1 != null){
            recording1.stop();
            recording = null;
        }
    }


    @SuppressWarnings("MissingPermission")
    private void startRecord(){
        viewBinding.btnRecordStart.setImageResource(R.drawable.ic_baseline_stop_circle_24);

        Recording recording1 = recording;
        if(recording1 != null){
            recording1.stop();
            recording = null;
            return;
        }

        //private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
        String fileName = new SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Recording");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        //녹화 타이머 설정
        CountDownTimer timer = new CountDownTimer(RECORDING_DURATION, 1000){
            @Override
            public void onTick(long milliisUntilFinished){

            }
            @Override
            public void onFinish(){
                stopRecording();
            }
        };


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(CameraXActivity.this, options).withAudioEnabled()
                .start(ContextCompat.getMainExecutor(CameraXActivity.this), videoRecordEvent -> {
                   if(videoRecordEvent instanceof VideoRecordEvent.Start){
                       viewBinding.btnRecordStart.setEnabled(true);
                       timer.start(); //녹화 시작 시 타이머 시작
                   }else if(videoRecordEvent instanceof VideoRecordEvent.Finalize){
                       String msg = "Video capture succeeded: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                       Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                       timer.cancel(); //녹화 완료 시 타이머 중지
                   }else{
                       recording.close();
                       recording = null;
                       String msg = "Error : " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                       Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                       timer.cancel(); //녹화 오류 시 타이머 중지
                   }
                   viewBinding.btnRecordStart.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
                });

    }

    private void startCamera() {
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
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

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
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
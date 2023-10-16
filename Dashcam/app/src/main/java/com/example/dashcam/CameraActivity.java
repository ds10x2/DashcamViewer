package com.example.dashcam;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class CameraActivity extends AppCompatActivity {

    private TextureView mCameraTextureView;
    private Camera2Preview mPreview;

    Activity cameraActivity = this;

    private static final String TAG = "CAMERAACTIVITY";

    static final int REQUEST_CAMERA = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        mPreview = new Camera2Preview(this, mCameraTextureView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case REQUEST_CAMERA:
                for(int i = 0; i < permissions.length; i++){
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if(permission.equals(android.Manifest.permission.CAMERA)){
                        if(grantResult == PackageManager.PERMISSION_GRANTED){
                            mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
                            mPreview = new Camera2Preview(cameraActivity, mCameraTextureView);
                            mPreview.openCamera();
                            Log.d(TAG, "mPreview set");
                        }else{
                            Toast.makeText(this, "Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        mPreview.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mPreview.onPause();
    }

}
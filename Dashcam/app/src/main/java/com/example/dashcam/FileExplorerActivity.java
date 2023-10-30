package com.example.dashcam;

import static android.os.Environment.DIRECTORY_MOVIES;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FileExplorerActivity extends AppCompatActivity {

    String mCurrent;
    String mRoot;
    TextView mCurrentTxt;
    ListView mFileList;
    ArrayAdapter mAdapter;
    ArrayList arFiles;
    String mMovies;
    ImageButton mBack;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static String[] permission = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String TAG = FileExplorerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);

        mCurrentTxt = (TextView) findViewById(R.id.current);
        mFileList = (ListView) findViewById(R.id.filelist);
        arFiles = new ArrayList();
        mBack = (ImageButton) findViewById(R.id.btnBack);

        mRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        //mCurrent = mRoot;

        mMovies = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath() + "/Recording";
        mCurrent = mMovies;

        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arFiles);
        mFileList.setAdapter(mAdapter); // 리스트 뷰에 어댑터 연결
        mFileList.setOnItemClickListener(mItemClickListener);


        allPermissionsGranted();
        refreshFiles();


        mBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }


    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @SuppressLint("WrongConstant")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String Name = (String) arFiles.get(position);
            if(Name.startsWith("[") && Name.endsWith("]")){
                Name = Name.substring(1, Name.length()-1);
            }
            String Path = mCurrent + "/"  + Name;
            File f = new File(Path);
            if(f.isDirectory()){ //폴더일 경우
                mCurrent = Path;
                refreshFiles();
            }else{
                //파일일 경우 파일 확장자 구하기
                String extension = "";
                int lastDotIndex = Name.lastIndexOf(".");
                if (lastDotIndex != -1) {
                    extension = Name.substring(lastDotIndex + 1);
                }

                //Toast.makeText(getApplicationContext(), extension, 0).show();

                if(extension.equals("mp4")){

                    Intent intent = new Intent(getApplicationContext(), VideoplayerActivity.class);
                    intent.putExtra("Path", Path); //파일 경로 전달
                    startActivity(intent);

                }else{
                    Toast.makeText(getApplicationContext(), (String) arFiles.get(position), 0).show();
                }

            }
        }
    };

    public void mOnClick(View v){
        switch(v.getId()){
            case R.id.btnroot:
                if(mCurrent.compareTo(mRoot) != 0){
                    mCurrent = mRoot;
                    refreshFiles();
                }
                break;
            case R.id.btnup:
                if(mCurrent.compareTo(mRoot) != 0){
                    int end = mCurrent.lastIndexOf("/");
                    String uppath = mCurrent.substring(0, end);
                    mCurrent = uppath;
                    refreshFiles();
                }
                break;
        }
    }

    void refreshFiles(){
        mCurrentTxt.setText(mCurrent);
        arFiles.clear();
        File current = new File(mCurrent);
        String[] files = current.list();

        if(files != null) {
            // 파일이 있을 때
            for(int i = 0; i < files.length; i++){
                String Path = mCurrent + "/" + files[i];
                String Name = "";

                File f = new File(Path);
                if(f.isDirectory()){
                    Name = "[" + files[i] + "]";
                }else{
                    Name = files[i];
                }

                arFiles.add(Name);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private void allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                return;
            }
        }
    }

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean allGranted = true;
            for(int grantResult : grantResults){
                if(grantResult != PackageManager.PERMISSION_GRANTED){
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.i(TAG, "Permissions granted");
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
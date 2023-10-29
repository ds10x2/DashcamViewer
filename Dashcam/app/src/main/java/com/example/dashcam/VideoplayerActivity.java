package com.example.dashcam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.dashcam.databinding.ActivityCameraxBinding;
import com.example.dashcam.databinding.ActivityVideoplayerBinding;

import java.io.File;

public class VideoplayerActivity extends AppCompatActivity {
    private ActivityVideoplayerBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityVideoplayerBinding.inflate(getLayoutInflater()); //바인딩 클래스의 인스턴스 생성
        setContentView(viewBinding.getRoot());

        Intent intent = getIntent(); //데이터를 전달받을 인텐트
        String path = intent.getStringExtra("Path");

        Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();

        MediaController controller = new MediaController(getApplicationContext());
        viewBinding.videoView.setMediaController(controller);

        viewBinding.videoView.requestFocus();

        viewBinding.videoView.setVideoPath(path);

        viewBinding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Toast.makeText(getApplicationContext(), "동영상이 준비되었습니다", Toast.LENGTH_SHORT).show();
            }
        });

        viewBinding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //동영상 재생 완료 후
                viewBinding.playbtn.setVisibility(View.VISIBLE);
            }
        });

        viewBinding.playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewBinding.videoView.isPlaying()) {
                    viewBinding.videoView.start();
                    viewBinding.playbtn.setVisibility(View.INVISIBLE);
                }
            }
        });


        String fileName = extractFileName(path);
        viewBinding.textTitle.setText(fileName);

    }

    public String extractFileName(String path) {
        if (path != null) {
            int lastSeparatorIndex = path.lastIndexOf(File.separator);
            if (lastSeparatorIndex != -1)
                return path.substring(lastSeparatorIndex + 1);
        }
        return null;
    }


}
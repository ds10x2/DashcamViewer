package com.example.dashcam.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.dashcam.R;
import com.example.dashcam.databinding.ActivityCameraxBinding;
import com.example.dashcam.databinding.ActivityVideoplayerBinding;

import java.io.File;

public class VideoplayerActivity extends AppCompatActivity {
    private ActivityVideoplayerBinding viewBinding;
    Handler handler;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityVideoplayerBinding.inflate(getLayoutInflater()); //바인딩 클래스의 인스턴스 생성
        setContentView(viewBinding.getRoot());

        Intent intent = getIntent(); //데이터를 전달받을 인텐트
        String path = intent.getStringExtra("Path");

        //Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();

        MediaController controller = new MediaController(getApplicationContext());
        viewBinding.videoView.setMediaController(controller);
        viewBinding.videoView.requestFocus();
        viewBinding.videoView.setVideoPath(path);
        viewBinding.seekBar.bringToFront();

        handler = new Handler();

        viewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    viewBinding.videoView.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        viewBinding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Toast.makeText(getApplicationContext(), "동영상이 준비되었습니다", Toast.LENGTH_SHORT).show();

                int videoDuration = viewBinding.videoView.getDuration();
                viewBinding.seekBar.setMax(videoDuration);


            }
        });


        //재생 끝났을 때
        viewBinding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //동영상 재생 완료 후
                viewBinding.playbtn.setVisibility(View.VISIBLE);
                viewBinding.playbtn.setEnabled(true);
                viewBinding.playbtn2.setImageResource(R.drawable.baseline_play_arrow_24);
            }
        });

        //파일명 가져오기
        String fileName = extractFileName(path);
        viewBinding.textTitle.setText("파일명: " + fileName);

        //재생 버튼
        viewBinding.playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewBinding.videoView.isPlaying()) {
                    viewBinding.videoView.start();
                    viewBinding.playbtn.setVisibility(View.INVISIBLE);
                    viewBinding.playbtn.setEnabled(false);

                    viewBinding.playbtn2.setImageResource(R.drawable.baseline_pause_24);

                    updateSeekBar();
                }
            }
        });

        viewBinding.playbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!viewBinding.videoView.isPlaying()){
                    viewBinding.videoView.start();
                    viewBinding.playbtn2.setImageResource(R.drawable.baseline_pause_24);

                    viewBinding.playbtn.setVisibility(View.INVISIBLE);
                    viewBinding.playbtn.setEnabled(false);

                    updateSeekBar();
                }
                else{
                    viewBinding.videoView.pause();
                    viewBinding.playbtn2.setImageResource(R.drawable.baseline_play_arrow_24);
                    viewBinding.playbtn.setVisibility(View.VISIBLE);
                    viewBinding.playbtn.setEnabled(true);
                }
            }
        });



    }

    public String extractFileName(String path) {
        if (path != null) {
            int lastSeparatorIndex = path.lastIndexOf(File.separator);
            if (lastSeparatorIndex != -1)
                return path.substring(lastSeparatorIndex + 1);
        }
        return null;
    }

    private void updateSeekBar(){
        if(viewBinding.videoView.isPlaying()){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int currentPosition = viewBinding.videoView.getCurrentPosition();
                    viewBinding.seekBar.setProgress(currentPosition);

                    if(viewBinding.videoView.isPlaying())
                        updateSeekBar();
                }
            }, 1000);

            }
        }
}


package com.example.gesturecontrolapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.gesturecontrolapplication.util.Util;

public class secondActivity extends AppCompatActivity {

    private VideoView videoShow;
    private String VideoPath;
    private String selected;
    private String videoName;
    private TextView text;
    private int count;
    private Button replay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selected = getIntent().getStringExtra("selected");
        setContentView(R.layout.activity_second);
        replay = (Button) findViewById(R.id.reply);

        videoShow = (VideoView)findViewById(R.id.videoView);
        videoName =  Util.getVideoName(selected);
        VideoPath = "android.resource://" + getPackageName() + "/raw/"+videoName;
        videoShow.setVideoURI(Uri.parse(VideoPath));
        videoShow.start();
        count = 0;
//        videoShow.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mp.setLooping(true);
//            }
//        });
        videoShow.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            int maxCount = 4;
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(count < maxCount) {
                    count++;
                    mp.seekTo(0);
                    mp.start();
                }
            }
        });

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoShow.isPlaying()) {
                    videoShow.resume();
                }else{
                    videoShow.start();
                    count = 0;
                }
            }
        });
    }
}
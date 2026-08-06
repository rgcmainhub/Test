package com.music.rgc.app;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements MusicService.OnTrackChangeListener {

    private MusicService musicService;
    private boolean isBound = false;
    private View layoutMiniPlayer;
    private TextView tvMiniTitle, tvMiniArtist;
    private ImageButton btnMiniPlayPause, btnMiniNext;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MusicBinder) service).getService();
            isBound = true;
            musicService.setTrackChangeListener(MainActivity.this);
            updateMiniPlayerState();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { isBound = false; }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        Intent serviceIntent = new Intent(this, MusicService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer);
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        tvMiniArtist = findViewById(R.id.tvMiniArtist);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        btnMiniNext = findViewById(R.id.btnMiniNext);

        btnMiniPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isBound && musicService != null) {
                    if (musicService.isPlaying()) musicService.pause(); else musicService.play();
                }
            }
        });

        btnMiniNext.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { if (isBound && musicService != null) musicService.next(); }
        });
    }

    private void updateMiniPlayerState() {
        if (isBound && musicService != null && musicService.getCurrentSong() != null) {
            Song song = musicService.getCurrentSong();
            layoutMiniPlayer.setVisibility(View.VISIBLE);
            tvMiniTitle.setText(song.getTitle());
            tvMiniArtist.setText(song.getArtist());
            btnMiniPlayPause.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        } else {
            layoutMiniPlayer.setVisibility(View.GONE);
        }
    }

    @Override public void OnTrackChanged(Song song, boolean isPlaying) { updateMiniPlayerState(); }
    @Override public void onPlaybackStateChanged(boolean isPlaying) { updateMiniPlayerState(); }
}
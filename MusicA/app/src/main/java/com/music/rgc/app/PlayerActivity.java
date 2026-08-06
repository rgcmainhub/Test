package com.music.rgc.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity implements MusicService.OnTrackChangeListener {

    private MusicService musicService;
    private boolean isBound = false;

    private TextView tvTitle, tvArtist, tvAlbum, tvCurrentTime, tvTotalTime;
    private ImageView imgAlbumArt;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat, btnLike, btnBack;
    private SeekBar seekBar;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MusicBinder) service).getService();
            isBound = true;
            musicService.setTrackChangeListener(PlayerActivity.this);
            updatePlayerUi();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { isBound = false; }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();

        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvAlbum = findViewById(R.id.tvAlbum);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        imgAlbumArt = findViewById(R.id.imgAlbumArt);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnLike = findViewById(R.id.btnLike);
        btnBack = findViewById(R.id.btnBack);
        seekBar = findViewById(R.id.seekBar);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isBound && musicService != null) {
                    if (musicService.isPlaying()) musicService.pause(); else musicService.play();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { if (isBound && musicService != null) musicService.next(); }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { if (isBound && musicService != null) musicService.previous(); }
        });
    }

    private void updatePlayerUi() {
        if (isBound && musicService != null && musicService.getCurrentSong() != null) {
            Song song = musicService.getCurrentSong();
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            tvAlbum.setText(song.getAlbum());
            btnPlayPause.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    @Override public void OnTrackChanged(Song song, boolean isPlaying) { updatePlayerUi(); }
    @Override public void onPlaybackStateChanged(boolean isPlaying) { updatePlayerUi(); }

    @Override
    protected void onDestroy() {
        if (isBound) { unbindService(serviceConnection); isBound = false; }
        super.onDestroy();
    }
}
package com.music.rgc.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String ACTION_PLAY = "com.music.rgc.app.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.music.rgc.app.ACTION_PAUSE";
    public static final String ACTION_TOGGLE = "com.music.rgc.app.ACTION_TOGGLE";
    public static final String ACTION_NEXT = "com.music.rgc.app.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "com.music.rgc.app.ACTION_PREVIOUS";
    public static final String CHANNEL_ID = "MusicA_Playback_Channel";
    public static final int NOTIFICATION_ID = 101;

    public class MusicBinder extends Binder {
        public MusicService getService() { return MusicService.this; }
    }

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentPosition = -1;
    private boolean isPrepared = false;
    private MediaSessionCompat mediaSession;
    private DataManager dataManager;

    public interface OnTrackChangeListener {
        void OnTrackChanged(Song song, boolean isPlaying);
        void onPlaybackStateChanged(boolean isPlaying);
    }

    private OnTrackChangeListener trackChangeListener;
    public void setTrackChangeListener(OnTrackChangeListener listener) { this.trackChangeListener = listener; }

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = new DataManager(this);
        initMediaPlayer();
        initMediaSession();
        createNotificationChannel();
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
        }
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MusicASession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override public void onPlay() { play(); }
            @Override public void onPause() { pause(); }
            @Override public void onSkipToNext() { next(); }
            @Override public void onSkipToPrevious() { previous(); }
        });
        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) play();
            else if (ACTION_PAUSE.equals(action)) pause();
            else if (ACTION_TOGGLE.equals(action)) { if (isPlaying()) pause(); else play(); }
            else if (ACTION_NEXT.equals(action)) next();
            else if (ACTION_PREVIOUS.equals(action)) previous();
        }
        return START_STICKY;
    }

    public void setPlaylist(List<Song> songs, int startPosition) {
        this.playlist = new ArrayList<>(songs);
        if (dataManager.isShuffle()) Collections.shuffle(this.playlist);
        this.currentPosition = startPosition;
        playSongAtPosition(this.currentPosition);
    }

    public void playSongAtPosition(int position) {
        if (playlist == null || playlist.isEmpty() || position < 0 || position >= playlist.size()) return;
        currentPosition = position;
        Song song = playlist.get(currentPosition);
        try {
            mediaPlayer.reset();
            isPrepared = false;
            mediaPlayer.setDataSource(song.getDataPath());
            mediaPlayer.prepareAsync();
            dataManager.addRecentSong(song.getId());
            updateNotification();
            if (trackChangeListener != null) trackChangeListener.OnTrackChanged(song, true);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void play() {
        if (isPrepared && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateNotification();
            if (trackChangeListener != null) trackChangeListener.onPlaybackStateChanged(true);
        } else if (currentPosition != -1) {
            playSongAtPosition(currentPosition);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification();
            if (trackChangeListener != null) trackChangeListener.onPlaybackStateChanged(false);
        }
    }

    public void next() {
        if (playlist.isEmpty()) return;
        if (dataManager.getRepeatMode() == 1) { playSongAtPosition(currentPosition); return; }
        currentPosition = (currentPosition + 1) % playlist.size();
        playSongAtPosition(currentPosition);
    }

    public void previous() {
        if (playlist.isEmpty()) return;
        currentPosition = (currentPosition - 1 + playlist.size()) % playlist.size();
        playSongAtPosition(currentPosition);
    }

    public boolean isPlaying() { return mediaPlayer != null && mediaPlayer.isPlaying(); }
    public Song getCurrentSong() { return (playlist != null && currentPosition >= 0 && currentPosition < playlist.size()) ? playlist.get(currentPosition) : null; }

    @Override public void onPrepared(MediaPlayer mp) { isPrepared = true; mp.start(); updateNotification(); }
    @Override public void onCompletion(MediaPlayer mp) { next(); }
    @Override public boolean onError(MediaPlayer mp, int what, int extra) { isPrepared = false; return false; }

    private void updateNotification() {
        Song song = getCurrentSong();
        if (song == null) return;
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setContentIntent(pendingIntent)
                .setOngoing(isPlaying())
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MusicA Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override public IBinder onBind(Intent intent) { return binder; }
    @Override public void onDestroy() { if (mediaPlayer != null) mediaPlayer.release(); super.onDestroy(); }
}
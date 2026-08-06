package com.music.rgc.app;

import android.net.Uri;
import java.io.Serializable;

public class Song implements Serializable {
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String dataPath;
    private String albumArtUri;
    private boolean isLiked;

    public Song(long id, String title, String artist, String album, long duration, String dataPath, String albumArtUri) {
        this.id = id;
        this.title = (title != null && !title.isEmpty()) ? title : "Unknown Title";
        this.artist = (artist != null && !artist.isEmpty()) ? artist : "Unknown Artist";
        this.album = (album != null && !album.isEmpty()) ? album : "Unknown Album";
        this.duration = duration;
        this.dataPath = dataPath;
        this.albumArtUri = albumArtUri;
        this.isLiked = false;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public long getDuration() { return duration; }
    public String getDataPath() { return dataPath; }
    public String getAlbumArtUri() { return albumArtUri; }
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public String getFormattedDuration() {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = duration / (1000 * 60 * 60);

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
package com.music.rgc.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Playlist implements Serializable {
    private String id;
    private String name;
    private List<Long> songIds;
    private long createdAt;

    public Playlist(String id, String name) {
        this.id = id;
        this.name = name;
        this.songIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Long> getSongIds() {
        if (songIds == null) songIds = new ArrayList<>();
        return songIds;
    }

    public void addSongId(long songId) {
        if (songIds == null) songIds = new ArrayList<>();
        if (!songIds.contains(songId)) songIds.add(songId);
    }

    public void removeSongId(long songId) {
        if (songIds != null) songIds.remove(Long.valueOf(songId));
    }

    public long getCreatedAt() { return createdAt; }
}
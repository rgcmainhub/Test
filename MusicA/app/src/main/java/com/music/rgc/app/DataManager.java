package com.music.rgc.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataManager {
    private static final String PREF_NAME = "MusicAPrefs";
    private static final String KEY_LIKED_SONGS = "liked_songs";
    private static final String KEY_PLAYLISTS = "playlists_json";
    private static final String KEY_RECENT_SONGS = "recent_songs";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_REPEAT_MODE = "repeat_mode";
    private static final String KEY_SHUFFLE = "shuffle_mode";

    private SharedPreferences prefs;

    public DataManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Set<Long> getLikedSongIds() {
        Set<String> stringSet = prefs.getStringSet(KEY_LIKED_SONGS, new HashSet<String>());
        Set<Long> idSet = new HashSet<>();
        for (String idStr : stringSet) {
            try { idSet.add(Long.parseLong(idStr)); } catch (NumberFormatException ignored) {}
        }
        return idSet;
    }

    public void setSongLiked(long songId, boolean liked) {
        Set<Long> current = getLikedSongIds();
        if (liked) current.add(songId); else current.remove(songId);
        Set<String> stringSet = new HashSet<>();
        for (Long id : current) stringSet.add(String.valueOf(id));
        prefs.edit().putStringSet(KEY_LIKED_SONGS, stringSet).apply();
    }

    public boolean isSongLiked(long songId) {
        return getLikedSongIds().contains(songId);
    }

    public List<Playlist> getPlaylists() {
        List<Playlist> playlistList = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_PLAYLISTS, "[]");
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Playlist playlist = new Playlist(obj.getString("id"), obj.getString("name"));
                JSONArray songsArr = obj.getJSONArray("songIds");
                for (int j = 0; j < songsArr.length(); j++) playlist.addSongId(songsArr.getLong(j));
                playlistList.add(playlist);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return playlistList;
    }

    public void savePlaylists(List<Playlist> playlists) {
        JSONArray array = new JSONArray();
        for (Playlist p : playlists) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", p.getId());
                obj.put("name", p.getName());
                JSONArray songsArr = new JSONArray();
                for (Long sId : p.getSongIds()) songsArr.put(sId);
                obj.put("songIds", songsArr);
                array.put(obj);
            } catch (JSONException e) { e.printStackTrace(); }
        }
        prefs.edit().putString(KEY_PLAYLISTS, array.toString()).apply();
    }

    public void createPlaylist(String name) {
        List<Playlist> list = getPlaylists();
        list.add(new Playlist("pl_" + System.currentTimeMillis(), name));
        savePlaylists(list);
    }

    public void renamePlaylist(String playlistId, String newName) {
        List<Playlist> list = getPlaylists();
        for (Playlist p : list) {
            if (p.getId().equals(playlistId)) { p.setName(newName); break; }
        }
        savePlaylists(list);
    }

    public void deletePlaylist(String playlistId) {
        List<Playlist> list = getPlaylists();
        Playlist target = null;
        for (Playlist p : list) {
            if (p.getId().equals(playlistId)) { target = p; break; }
        }
        if (target != null) { list.remove(target); savePlaylists(list); }
    }

    public void addSongToPlaylist(String playlistId, long songId) {
        List<Playlist> list = getPlaylists();
        for (Playlist p : list) {
            if (p.getId().equals(playlistId)) { p.addSongId(songId); break; }
        }
        savePlaylists(list);
    }

    public List<Long> getRecentSongIds() {
        List<Long> recentList = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_RECENT_SONGS, "[]");
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) recentList.add(array.getLong(i));
        } catch (JSONException e) { e.printStackTrace(); }
        return recentList;
    }

    public void addRecentSong(long songId) {
        List<Long> recents = getRecentSongIds();
        recents.remove(Long.valueOf(songId));
        recents.add(0, songId);
        if (recents.size() > 20) recents = recents.subList(0, 20);
        JSONArray array = new JSONArray();
        for (Long id : recents) array.put(id);
        prefs.edit().putString(KEY_RECENT_SONGS, array.toString()).apply();
    }

    public boolean isDarkTheme() { return prefs.getBoolean(KEY_DARK_THEME, true); }
    public void setDarkTheme(boolean dark) { prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply(); }
    public int getRepeatMode() { return prefs.getInt(KEY_REPEAT_MODE, 0); }
    public void setRepeatMode(int mode) { prefs.edit().putInt(KEY_REPEAT_MODE, mode).apply(); }
    public boolean isShuffle() { return prefs.getBoolean(KEY_SHUFFLE, false); }
    public void setShuffle(boolean shuffle) { prefs.edit().putBoolean(KEY_SHUFFLE, shuffle).apply(); }

    public String exportBackupJson() {
        JSONObject root = new JSONObject();
        try {
            root.put("version", 1);
            root.put("app", "MusicA");
            root.put("author", "DevNameGelo, RGC");
            JSONArray likedArr = new JSONArray();
            for (Long id : getLikedSongIds()) likedArr.put(id);
            root.put("likedSongs", likedArr);
            JSONArray playlistsArr = new JSONArray();
            for (Playlist p : getPlaylists()) {
                JSONObject pObj = new JSONObject();
                pObj.put("id", p.getId());
                pObj.put("name", p.getName());
                JSONArray sArr = new JSONArray();
                for (Long sId : p.getSongIds()) sArr.put(sId);
                pObj.put("songIds", sArr);
                playlistsArr.put(pObj);
            }
            root.put("playlists", playlistsArr);
            return root.toString(2);
        } catch (JSONException e) { return "{}"; }
    }

    public boolean importBackupJson(String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);
            if (root.has("likedSongs")) {
                JSONArray likedArr = root.getJSONArray("likedSongs");
                Set<String> set = new HashSet<>();
                for (int i = 0; i < likedArr.length(); i++) set.add(String.valueOf(likedArr.getLong(i)));
                prefs.edit().putStringSet(KEY_LIKED_SONGS, set).apply();
            }
            if (root.has("playlists")) {
                JSONArray playlistsArr = root.getJSONArray("playlists");
                List<Playlist> newPlaylists = new ArrayList<>();
                for (int i = 0; i < playlistsArr.length(); i++) {
                    JSONObject obj = playlistsArr.getJSONObject(i);
                    Playlist p = new Playlist(obj.getString("id"), obj.getString("name"));
                    JSONArray sArr = obj.getJSONArray("songIds");
                    for (int j = 0; j < sArr.length(); j++) p.addSongId(sArr.getLong(j));
                    newPlaylists.add(p);
                }
                savePlaylists(newPlaylists);
            }
            return true;
        } catch (Exception e) { return false; }
    }
}
package com.chenwei.tvautoplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores only non-sensitive playback preferences; video content is never copied. */
public final class PlaybackConfigStore {
    private static final String TAG = "PlaybackConfigStore";
    private static final String PREFS_NAME = "playback_configuration";
    private static final String KEY_PLAYLIST = "playlist_json";
    private static final String KEY_VIDEO_URI = "video_uri";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_AUTO_START = "auto_start";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_SCALE_MODE = "scale_mode";
    private static final String KEY_HINT_SHOWN = "settings_hint_shown";

    private final SharedPreferences preferences;

    public PlaybackConfigStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public PlaybackConfiguration load() {
        List<PlaylistItem> playlist = loadPlaylist();
        return new PlaybackConfiguration(
                playlist,
                preferences.getBoolean(KEY_AUTO_START, true),
                preferences.getBoolean(KEY_SOUND, true),
                ScaleMode.fromStoredValue(preferences.getString(KEY_SCALE_MODE, null))
        );
    }

    /** Retained for V1.0.0 call compatibility; new code should save the full playlist. */
    public void saveVideo(Uri uri, String displayName) {
        savePlaylist(Collections.singletonList(new PlaylistItem(uri.toString(), displayName)));
    }

    public void savePlaylist(List<PlaylistItem> playlist) {
        List<PlaylistItem> normalized = PlaylistEditor.replace(playlist);
        JSONArray array = new JSONArray();
        for (PlaylistItem item : normalized) {
            JSONObject object = new JSONObject();
            try {
                object.put("uri", item.uri());
                object.put("name", item.displayName());
                array.put(object);
            } catch (JSONException exception) {
                throw new IllegalStateException("Unable to serialize playlist", exception);
            }
        }
        preferences.edit()
                .putString(KEY_PLAYLIST, array.toString())
                .remove(KEY_VIDEO_URI)
                .remove(KEY_DISPLAY_NAME)
                .apply();
    }

    public void clearVideo() {
        clearPlaylist();
    }

    public void clearPlaylist() {
        preferences.edit()
                .remove(KEY_PLAYLIST)
                .remove(KEY_VIDEO_URI)
                .remove(KEY_DISPLAY_NAME)
                .apply();
    }

    public void setAutoStartEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AUTO_START, enabled).apply();
    }

    public void setSoundEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public void setScaleMode(ScaleMode mode) {
        preferences.edit().putString(KEY_SCALE_MODE, mode.name()).apply();
    }

    public boolean isSettingsHintShown() {
        return preferences.getBoolean(KEY_HINT_SHOWN, false);
    }

    public void markSettingsHintShown() {
        preferences.edit().putBoolean(KEY_HINT_SHOWN, true).apply();
    }

    private List<PlaylistItem> loadPlaylist() {
        if (preferences.contains(KEY_PLAYLIST)) {
            return parsePlaylist(preferences.getString(KEY_PLAYLIST, "[]"));
        }

        String legacyUri = preferences.getString(KEY_VIDEO_URI, null);
        if (legacyUri == null || legacyUri.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<PlaylistItem> migrated = Collections.singletonList(new PlaylistItem(
                legacyUri,
                preferences.getString(KEY_DISPLAY_NAME, null)
        ));
        savePlaylist(migrated);
        return migrated;
    }

    private List<PlaylistItem> parsePlaylist(String storedValue) {
        List<PlaylistItem> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(storedValue == null ? "[]" : storedValue);
            for (int index = 0; index < array.length(); index++) {
                JSONObject object = array.optJSONObject(index);
                if (object == null) {
                    continue;
                }
                String uri = object.optString("uri", null);
                if (uri == null || uri.trim().isEmpty()) {
                    continue;
                }
                items.add(new PlaylistItem(uri, object.optString("name", null)));
            }
        } catch (JSONException | IllegalArgumentException exception) {
            Log.e(TAG, "Stored playlist is invalid; selection is required again", exception);
            return Collections.emptyList();
        }
        return PlaylistEditor.replace(items);
    }
}
